package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.GolfMain;
import com.pas.beans.PlayerTeePreference;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoPlayerTeePreference;

import jakarta.inject.Inject;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
 
public class PlayerTeePreferenceDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(PlayerTeePreferenceDAO.class);
		
	private Map<String,PlayerTeePreference> playerTeePreferencesMap = new HashMap<>();
	private List<PlayerTeePreference> playerTeePreferencesList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoPlayerTeePreference> playerTeePreferencesTable;
	private static final String AWS_TABLE_NAME = "playerteepreferences";

	@Inject GolfMain golfmain;

	public PlayerTeePreferenceDAO(DynamoClients dynamoClients2, GolfMain golfmain) 
	{
		this.golfmain = golfmain;
		
	   try 
	   {
	       dynamoClients = dynamoClients2;
	       playerTeePreferencesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerTeePreference.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing CourseTeeDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}

	public List<PlayerTeePreference> readPlayerTeePreferencesFromDB(DynamoGroup grp)
    {
		Iterator<DynamoPlayerTeePreference> results = playerTeePreferencesTable.scan().items().iterator();
	  	
		while (results.hasNext()) 
        {
			DynamoPlayerTeePreference dynamoPlayerTeePreference = results.next();
          	
			PlayerTeePreference playerTeePreference = new PlayerTeePreference();
			playerTeePreference.setPlayerTeePreferenceID(dynamoPlayerTeePreference.getPlayerTeePreferenceID());
			playerTeePreference.setPlayerID(dynamoPlayerTeePreference.getPlayerID());
			playerTeePreference.setPlayerUserName(dynamoPlayerTeePreference.getPlayerUserName());
			playerTeePreference.setPlayerFullName(dynamoPlayerTeePreference.getPlayerFullName());
			playerTeePreference.setCourseID(dynamoPlayerTeePreference.getCourseID());
			playerTeePreference.setCourseName(dynamoPlayerTeePreference.getCourseName());
			playerTeePreference.setCourseTeeID(dynamoPlayerTeePreference.getCourseTeeID());
			playerTeePreference.setTeeColor(dynamoPlayerTeePreference.getTeeColor());
					
            this.getPlayerTeePreferencesList().add(playerTeePreference);			
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:playerteepreference; rows:" + playerTeePreferencesList.size());
		
		playerTeePreferencesMap = playerTeePreferencesList.stream().collect(Collectors.toMap(PlayerTeePreference::getPlayerTeePreferenceID, PlayerTeePreference -> PlayerTeePreference));
			
		return playerTeePreferencesList;
    }
	
	public List<DynamoPlayerTeePreference> getPlayerSpecificTeePreferencesList(DynamoPlayer dynamoPlayer) 
	{
		DynamoDbIndex<DynamoPlayerTeePreference> gsi = playerTeePreferencesTable.index("gsi_PlayerID");
		
		Key key = Key.builder().partitionValue(dynamoPlayer.getPlayerID()).build();
    	QueryConditional qc = QueryConditional.keyEqualTo(key);
    	
    	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                .queryConditional(qc)
                .build();
    	SdkIterable<Page<DynamoPlayerTeePreference>> pmsByGameID = gsi.query(qer);
    	     
    	PageIterable<DynamoPlayerTeePreference> pages = PageIterable.create(pmsByGameID);
    	
    	List<DynamoPlayerTeePreference> dtList = pages.items().stream().toList();
    	
    	return dtList;
	}
	
	public PlayerTeePreference getPlayerTeePreference(String playerID, String courseID)
    {
		PlayerTeePreference playerTeePreference = null;
		
		for (int i = 0; i < this.getPlayerTeePreferencesList().size(); i++)
		{
			playerTeePreference = this.getPlayerTeePreferencesList().get(i);
			
			//logger.info("ptp id: " + playerTeePreference.getPlayerTeePreferenceID() + " player id: " + playerTeePreference.getPlayerID() + " course tee id: " + playerTeePreference.getCourseTeeID());
			
			if (playerTeePreference.getPlayerID().equalsIgnoreCase(playerID) 
			&& playerTeePreference.getCourseID().equalsIgnoreCase(courseID))
			{
				break; //this is the one we want
			}
		}
	
		return playerTeePreference;
    }
	
	public PlayerTeePreference getPlayerTeePreference(String playerTeePreferenceID)
    {
		PlayerTeePreference playerTeePreference = this.getPlayerTeePreferencesMap().get(playerTeePreferenceID);				
    	return playerTeePreference;
    }

	public void addPlayerTeePreference(PlayerTeePreference playerTeePreference) throws Exception
	{
		DynamoPlayerTeePreference dptp = dynamoUpsert(playerTeePreference);
		playerTeePreference.setPlayerTeePreferenceID(dptp.getPlayerTeePreferenceID());
		
		logger.info("LoggedDBOperation: function-add; table:playerteepreference; rows:1");
		
		refreshListsAndMaps("add",playerTeePreference);
		
		logger.info("addPlayerTeePreference complete");	
	}
	
	public void updatePlayerTeePreference(PlayerTeePreference playerTeePreference) throws Exception
	{
		dynamoUpsert(playerTeePreference);
     	logger.info("LoggedDBOperation: function-update; table:playerteepreference; rows:1");
		
		refreshListsAndMaps("update", playerTeePreference);
			
		logger.debug("update player tee preference table complete");		
	}
	
	private DynamoPlayerTeePreference dynamoUpsert(PlayerTeePreference ptp) throws Exception 
	{
		DynamoPlayerTeePreference dynamoPtp = new DynamoPlayerTeePreference();
        
		if (ptp.getPlayerTeePreferenceID() == null)
		{
			dynamoPtp.setPlayerTeePreferenceID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoPtp.setPlayerTeePreferenceID(ptp.getPlayerTeePreferenceID());
		}
		
		dynamoPtp.setCourseID(ptp.getCourseID());
		dynamoPtp.setCourseTeeID(ptp.getCourseTeeID());
		dynamoPtp.setPlayerID(ptp.getPlayerID());
		dynamoPtp.setPlayerFullName(ptp.getPlayerFullName());
		dynamoPtp.setPlayerUserName(ptp.getPlayerUserName());
		dynamoPtp.setCourseName(ptp.getCourseName());
		dynamoPtp.setTeeColor(ptp.getTeeColor());
				
		PutItemEnhancedRequest<DynamoPlayerTeePreference> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayerTeePreference.class).item(dynamoPtp).build();
		playerTeePreferencesTable.putItem(putItemEnhancedRequest);
				
		return dynamoPtp;
	}
	
	//deletes a particular player tee preference row
	public void deletePlayerTeePreference(String ptpID) 
	{
		Key key2 = Key.builder().partitionValue(ptpID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key2).build();
		playerTeePreferencesTable.deleteItem(deleteItemEnhancedRequest);
	
		logger.info("LoggedDBOperation: function-delete; table:playerteePreferences; rows:1");  			
		
		this.getPlayerTeePreferencesList().clear();
		Collection<PlayerTeePreference> values = this.getPlayerTeePreferencesMap().values();
		this.setPlayerTeePreferencesList(new ArrayList<>(values));
		
		logger.info("deletePlayerTeePreferences complete"); 		
	}	
		
	//deletes all player tee preferences rows from the db for this player
	public void deletePlayerTeePreferences(DynamoPlayer dynamoPlayer) 
	{
		DynamoDbIndex<DynamoPlayerTeePreference> gsi = playerTeePreferencesTable.index("gsi_PlayerID");
		
		Key key = Key.builder().partitionValue(dynamoPlayer.getPlayerID()).build();
    	QueryConditional qc = QueryConditional.keyEqualTo(key);
    	
    	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                .queryConditional(qc)
                .build();
    	SdkIterable<Page<DynamoPlayerTeePreference>> pmsByGameID = gsi.query(qer);
    	     
    	PageIterable<DynamoPlayerTeePreference> pages = PageIterable.create(pmsByGameID);
    	
    	List<DynamoPlayerTeePreference> dtList = pages.items().stream().toList();
    	
    	if (dtList != null && dtList.size() > 0)
    	{
    		for (int i = 0; i < dtList.size(); i++) 
    		{
    			DynamoPlayerTeePreference dpm = dtList.get(i);
        		String playerTeePreferenceID = dpm.getPlayerTeePreferenceID();
        		
        		Key key2 = Key.builder().partitionValue(playerTeePreferenceID).build();
        		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key2).build();
        		playerTeePreferencesTable.deleteItem(deleteItemEnhancedRequest);
        	
        		logger.info("LoggedDBOperation: function-delete; table:playerteePreferences; rows:1");        		
			}
    		
    		for (int j = 0; j < playerTeePreferencesList.size(); j++)
    		{
    			PlayerTeePreference playerTeePreference = playerTeePreferencesList.get(j);
    			
    			if (playerTeePreference.getPlayerID().equalsIgnoreCase(dynamoPlayer.getPlayerID()))
    			{
    				this.getPlayerTeePreferencesMap().remove(playerTeePreference.getPlayerTeePreferenceID());
    			}
    		}	
    		
    	}			
		
		this.getPlayerTeePreferencesList().clear();
		Collection<PlayerTeePreference> values = this.getPlayerTeePreferencesMap().values();
		this.setPlayerTeePreferencesList(new ArrayList<>(values));
		
		logger.info("deletePlayerTeePreferences complete"); 		
	}	
	
	private void refreshListsAndMaps(String function, PlayerTeePreference ptp)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getPlayerTeePreferencesMap().remove(ptp.getPlayerTeePreferenceID());			
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getPlayerTeePreferencesMap().put(ptp.getPlayerTeePreferenceID(), ptp);		
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getPlayerTeePreferencesMap().remove(ptp.getPlayerTeePreferenceID());		
			this.getPlayerTeePreferencesMap().put(ptp.getPlayerTeePreferenceID(), ptp);		
		}
		
		this.getPlayerTeePreferencesList().clear();
		Collection<PlayerTeePreference> values = this.getPlayerTeePreferencesMap().values();
		this.setPlayerTeePreferencesList(new ArrayList<>(values));
		
		Collections.sort(this.getPlayerTeePreferencesList(), new Comparator<PlayerTeePreference>() 
		{
		   public int compare(PlayerTeePreference o1, PlayerTeePreference o2) 
		   {
		      return o1.getPlayerFullName().compareTo(o2.getPlayerFullName());
		   }
		});
		
	}
	
	public Map<String, PlayerTeePreference> getPlayerTeePreferencesMap() {
		return playerTeePreferencesMap;
	}

	public void setPlayerTeePreferencesMap(Map<String, PlayerTeePreference> playerTeePreferencesMap) {
		this.playerTeePreferencesMap = playerTeePreferencesMap;
	}

	public List<PlayerTeePreference> getPlayerTeePreferencesList() {
		return playerTeePreferencesList;
	}

	public void setPlayerTeePreferencesList(List<PlayerTeePreference> playerTeePreferencesList) {
		this.playerTeePreferencesList = playerTeePreferencesList;
	}

	
}

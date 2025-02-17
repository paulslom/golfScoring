package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.GolfMain;
import com.pas.beans.TeeTime;
import com.pas.dynamodb.DateToStringConverter;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoTeeTime;

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

public class TeeTimeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(TeeTimeDAO.class);
		
	private Map<String,TeeTime> fullTeeTimesMap = new HashMap<>();
	private List<TeeTime> fullTeeTimesList = new ArrayList<TeeTime>();	

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoTeeTime> teeTimesTable;
	private static final String AWS_TABLE_NAME = "teetimes";
	
	@Inject GolfMain golfmain;
	
	public TeeTimeDAO(DynamoClients dynamoClients2) 
	{
	   try 
	   {
	       dynamoClients = dynamoClients2;
	       teeTimesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoTeeTime.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing TeeTimeDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
	
	public List<TeeTime> getTeeTimesByGame(DynamoGame game)
    {
		List<TeeTime> ttList = new ArrayList<>();
		
		for (int i = 0; i < this.getFullTeeTimesList().size(); i++)
		{
			TeeTime teeTime = this.getFullTeeTimesList().get(i);
			if (teeTime.getGameID().equalsIgnoreCase(game.getGameID()))
			{
				ttList.add(teeTime);
			}
		}
		
		Collections.sort(ttList, new Comparator<TeeTime>() 
		{
			public int compare(TeeTime o1, TeeTime o2) 
			{
			   Integer o1Int = o1.getPlayGroupNumber();
			   Integer o2Int = o2.getPlayGroupNumber();
			   return o1Int.compareTo(o2Int);
			}
			
		});
		
    	return ttList;
    }
	
	public List<TeeTime> readTeeTimesForGame(DynamoGroup defaultGroup, String gameID)
	{
		DynamoDbIndex<DynamoTeeTime> gsi = teeTimesTable.index("gsi_GameID");
		
		Key key = Key.builder().partitionValue(gameID).build();
    	QueryConditional qc = QueryConditional.keyEqualTo(key);
    	
    	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                .queryConditional(qc)
                .build();
    	SdkIterable<Page<DynamoTeeTime>> pmsByGameID = gsi.query(qer);
    	     
    	PageIterable<DynamoTeeTime> pages = PageIterable.create(pmsByGameID);
    	
    	List<DynamoTeeTime> dtList = pages.items().stream().toList();
    	List<TeeTime> returnList = new ArrayList<>();
    	
    	for (int i = 0; i < dtList.size(); i++) 
    	{
    		DynamoTeeTime dtt = dtList.get(i);
    		TeeTime tt = new TeeTime();
    		tt.setTeeTimeID(dtt.getTeeTimeID());
    		tt.setTeeTimeString(dtt.getTeeTimeString());
    		returnList.add(tt);
		}
    	return returnList;
	}
	
	public void readTeeTimesFromDB(DynamoGroup defaultGroup, Map<String, DynamoGame> fullGameMap) throws Exception
    {
		Iterator<DynamoTeeTime> results = teeTimesTable.scan().items().iterator();
				
		while (results.hasNext()) 
        {
			DynamoTeeTime dynamoTeeTime = results.next();
          	
			if (fullGameMap != null && fullGameMap.containsKey(dynamoTeeTime.getGameID()))
			{
				DynamoGame game = fullGameMap.get(dynamoTeeTime.getGameID());
				TeeTime teeTime = new TeeTime();
				teeTime.setGameDate(DateToStringConverter.unconvert(game.getGameDate()));
				teeTime.setCourseName(game.getCourseName());
				teeTime.setTeeTimeID(dynamoTeeTime.getTeeTimeID());
				teeTime.setGameID(dynamoTeeTime.getGameID());	
				teeTime.setTeeTimeString(dynamoTeeTime.getTeeTimeString());
				teeTime.setPlayGroupNumber(dynamoTeeTime.getPlayGroupNumber());
				
				this.getFullTeeTimesList().add(teeTime);			
			}
	    }		 
		
		Collections.sort(this.getFullTeeTimesList(), new Comparator<TeeTime>() 
		{
			public int compare(TeeTime teeTime1, TeeTime teeTime2) 
			{
				if (teeTime1.getGameDate().equals(teeTime2.getGameDate()))
				{
					return Integer.valueOf(teeTime1.getPlayGroupNumber()).compareTo(Integer.valueOf(teeTime2.getPlayGroupNumber()));					
				}
				
				return teeTime1.getGameDate().compareTo(teeTime2.getGameDate());				
			}
						
		});
		
		logger.info("LoggedDBOperation: function-inquiry; table:teetimes; rows:" + this.getFullTeeTimesList().size());
		
		this.setFullTeeTimesMap(this.getFullTeeTimesList().stream().collect(Collectors.toMap(TeeTime::getTeeTimeID, tt -> tt)));    	
    }
	
	public TeeTime getTeeTimeByTeeTimeID(String teeTimeID)
    {
		TeeTime teeTime = this.getFullTeeTimesMap().get(teeTimeID);
		return teeTime;
    }	
	
	public void addTeeTime(TeeTime teeTime) throws Exception
	{
		DynamoTeeTime dtt = dynamoUpsert(teeTime);
		teeTime.setTeeTimeID(dtt.getTeeTimeID());
		
		logger.info("LoggedDBOperation: function-add; table:teetimes; rows:1");		
		
		refreshListsAndMaps("add", teeTime); 
				
		logger.info("addTeeTime complete");	
	}
	
	public void updateTeeTime(TeeTime teeTime) throws Exception
	{
		dynamoUpsert(teeTime);
		
		logger.info("LoggedDBOperation: function-update; table:teetimes; rows:1");
		
		refreshListsAndMaps("update", teeTime);
		
		logger.info("updateTeeTime complete");	
	}
	
	public void addTeeTimes(String newGameID, String teeTimesString, Date gameDate, String courseName) throws Exception
	{
		StringTokenizer st = new StringTokenizer(teeTimesString, " ");
			
		int tokenCount = 0;
	     
	 	while (st.hasMoreTokens()) 
	 	{	 			
 			String teeTimeStr = st.nextToken();
 			tokenCount++;
 			
 			TeeTime teeTime = new TeeTime();	
 			teeTime.setTeeTimeID(new String());
 			teeTime.setGameID(newGameID);
 			teeTime.setPlayGroupNumber(tokenCount);
 			teeTime.setTeeTimeString(teeTimeStr);
 			teeTime.setGameDate(gameDate);
 			teeTime.setCourseName(courseName);
 			
 			DynamoTeeTime dtt = dynamoUpsert(teeTime);
 			
 			logger.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
 			
 	 		teeTime.setTeeTimeID(dtt.getTeeTimeID());
 			
 			this.getFullTeeTimesList().add(teeTime);
 			this.getFullTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);
 			
 			refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
	 	}
	 	
	 	logger.info("addTeeTimes complete");				
	}	
	
	private DynamoTeeTime dynamoUpsert(TeeTime teeTime) throws Exception 
	{
		DynamoTeeTime dynamoTeeTime = new DynamoTeeTime();
        
		if (dynamoTeeTime.getTeeTimeID() == null)
		{
			dynamoTeeTime.setTeeTimeID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoTeeTime.setTeeTimeID(teeTime.getTeeTimeID());
		}
		
		dynamoTeeTime.setGameID(teeTime.getGameID());		
		dynamoTeeTime.setPlayGroupNumber(teeTime.getPlayGroupNumber());
		dynamoTeeTime.setTeeTimeString(teeTime.getTeeTimeString());
		
		PutItemEnhancedRequest<DynamoTeeTime> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoTeeTime.class).item(dynamoTeeTime).build();
		teeTimesTable.putItem(putItemEnhancedRequest);
			
		return dynamoTeeTime;
	}
	
	//deletes all tee times for a specific game
	public void deleteTeeTimesForGameFromDB(String gameID) 
	{
		//first identify the games we're talking about, so that we can fix up the list and map after the DB interaction.
		List<String> teeTimeIDs = new ArrayList<>();
		for (int i = 0; i < this.getFullTeeTimesList().size(); i++)
		{
			TeeTime teeTime = this.getFullTeeTimesList().get(i);
			if (teeTime.getGameID().equalsIgnoreCase(gameID))
			{
				teeTimeIDs.add(teeTime.getTeeTimeID());
				deleteTeeTimeFromDB(teeTime.getTeeTimeID());
			}
		}
		
		for (int i = 0; i < teeTimeIDs.size(); i++) 
		{
			this.getFullTeeTimesMap().remove(teeTimeIDs.get(i));
		}		
		
		refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
		
		logger.info("deleteTeeTimeForGameFromDB complete");			
	}
		
	//deletes a particular tee time
	public void deleteTeeTimeFromDB(String teeTimeID)
    {
		Key key = Key.builder().partitionValue(teeTimeID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		teeTimesTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:teetimes; rows:1");
		TeeTime teeTime = new TeeTime();
		teeTime.setTeeTimeID(teeTimeID);
		
		refreshListsAndMaps("delete", teeTime); 		
		
		logger.info("deleteTeeTimeFromDB complete");	
    }

	private void refreshListsAndMaps(String function, TeeTime teeTime)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getFullTeeTimesMap().remove(teeTime.getTeeTimeID());	
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getFullTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullTeeTimesMap().remove(teeTime.getTeeTimeID());	
			this.getFullTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		
		this.getFullTeeTimesList().clear();
		Collection<TeeTime> values = this.getFullTeeTimesMap().values();
		this.setFullTeeTimesList(new ArrayList<>(values));
		
		Collections.sort(this.getFullTeeTimesList(), new Comparator<TeeTime>() 
		{
		   public int compare(TeeTime o1, TeeTime o2) 
		   {
		      return o1.getTeeTimeString().compareTo(o2.getTeeTimeString());
		   }
		});
		
	}

	public Map<String, TeeTime> getFullTeeTimesMap() {
		return fullTeeTimesMap;
	}

	public void setFullTeeTimesMap(Map<String, TeeTime> fullTeeTimesMap) {
		this.fullTeeTimesMap = fullTeeTimesMap;
	}

	public List<TeeTime> getFullTeeTimesList() {
		return fullTeeTimesList;
	}

	public void setFullTeeTimesList(List<TeeTime> fullTeeTimesList) {
		this.fullTeeTimesList = fullTeeTimesList;
	}	
	
	}

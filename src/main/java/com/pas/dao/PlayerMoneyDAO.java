package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.GolfMain;
import com.pas.beans.PlayerMoney;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoPlayerMoney;

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

public class PlayerMoneyDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(PlayerMoneyDAO.class);
		
	private List<PlayerMoney> playerMoneyList = new ArrayList<>();
	private Map<String,PlayerMoney> playerMoneyMap = new HashMap<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoPlayerMoney> playerMoneyTable;
	private static final String AWS_TABLE_NAME = "playermoney";
	
	@Inject GolfMain golfmain;

	public PlayerMoneyDAO(DynamoClients dynamoClients2)
	{
	   try
	   {
	       dynamoClients = dynamoClients2;
	       playerMoneyTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerMoney.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing PlayerMoneyDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
		
	public List<PlayerMoney> getPlayerMoneyByPlayer(DynamoPlayer dynamoPlayer)
    {
		List<PlayerMoney> playerMoneyListByPlayer = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getPlayerID().equalsIgnoreCase(dynamoPlayer.getPlayerID()))
			{
				playerMoneyListByPlayer.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByPlayer;
    }
	
	public List<PlayerMoney> getPlayerMoneyByGame(DynamoGame dynamoGame)
    {
		List<PlayerMoney> playerMoneyListByGame = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getGameID().equalsIgnoreCase(dynamoGame.getGameID()))
			{
				playerMoneyListByGame.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByGame;
    }
	
	public void readPlayerMoneyFromDB()
    {
		Iterator<DynamoPlayerMoney> results = playerMoneyTable.scan().items().iterator();
		
		//since this full read is only done at app startup, we can't use golfmain's jsf bean to get it... so just redo the playerdao read
		PlayerDAO playerDAO = new PlayerDAO(dynamoClients);		
		playerDAO.readPlayersFromDB();
		
		while (results.hasNext()) 
        {
			DynamoPlayerMoney dynamoPlayerMoney = results.next();
            PlayerMoney playerMoney = new PlayerMoney();
			playerMoney.setPlayerMoneyID(dynamoPlayerMoney.getPlayerMoneyID());
			playerMoney.setPlayerID(dynamoPlayerMoney.getPlayerID());
			playerMoney.setGameID(dynamoPlayerMoney.getGameID());
			playerMoney.setDescription(dynamoPlayerMoney.getDescription());
			playerMoney.setAmount(dynamoPlayerMoney.getAmount());
			        	
            this.getPlayerMoneyList().add(playerMoney);			
        }
		
		this.setPlayerMoneyMap(this.getPlayerMoneyList().stream().collect(Collectors.toMap(PlayerMoney::getPlayerMoneyID, gm -> gm)));		
		logger.info("LoggedDBOperation: function-inquiry; table:playermoney; rows:" + playerMoneyList.size());		
    }
	
	public void addPlayerMoney(PlayerMoney playerMoney) throws Exception
	{
		DynamoPlayerMoney dpm = dynamoUpsert(playerMoney);
		logger.info("LoggedDBOperation: function-add; table:playermoney; rows:1");
		
		playerMoney.setPlayerMoneyID(dpm.getPlayerMoneyID());
		
		if (golfmain != null)
		{
			DynamoPlayer player = golfmain.getPlayerByPlayerID(dpm.getPlayerID());
			playerMoney.setPlayer(player);
		}
			
		this.getPlayerMoneyList().add(playerMoney);
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		
		logger.info("addPlayerMoney complete");	
	}
	
	public void updatePlayerMoney(PlayerMoney playerMoney) throws Exception
	{
		dynamoUpsert(playerMoney);
		
		logger.info("LoggedDBOperation: function-update; table:playermoney; rows:1");
			
		this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		logger.info("updatePlayerMoney complete");		
	}
	
	//deletes all player money rows from the db for this player
	public void deletePlayerMoneyFromDB(DynamoPlayer dynamoPlayer) 
	{
		DynamoDbIndex<DynamoPlayerMoney> gsi = playerMoneyTable.index("gsi_PlayerID");
		
		Key key = Key.builder().partitionValue(dynamoPlayer.getPlayerID()).build();
    	QueryConditional qc = QueryConditional.keyEqualTo(key);
    	
    	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                .queryConditional(qc)
                .build();
    	SdkIterable<Page<DynamoPlayerMoney>> pmsByGameID = gsi.query(qer);
    	     
    	PageIterable<DynamoPlayerMoney> pages = PageIterable.create(pmsByGameID);
    	
    	List<DynamoPlayerMoney> dtList = pages.items().stream().toList();
    	
    	if (dtList != null && dtList.size() > 0)
    	{
    		for (int i = 0; i < dtList.size(); i++) 
    		{
    			DynamoPlayerMoney dpm = dtList.get(i);
        		String playerMoneyID = dpm.getPlayerMoneyID();
        		
        		Key key2 = Key.builder().partitionValue(playerMoneyID).build();
        		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key2).build();
        		playerMoneyTable.deleteItem(deleteItemEnhancedRequest);
        	
        		logger.info("LoggedDBOperation: function-delete; table:playermoney; rows:1");        		
			}
    		
    		for (int j = 0; j < playerMoneyList.size(); j++)
    		{
    			PlayerMoney playerMoney = playerMoneyList.get(j);
    			
    			if (playerMoney.getPlayerID().equalsIgnoreCase(dynamoPlayer.getPlayerID()))
    			{
    				this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
    			}
    		}	
    		
    	}			
		
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		logger.info("deletePlayerMoneyFromDB complete");    	
		
	}	
	
	//deletes all player money rows from the db for this game
	public void deletePlayerMoneyFromDB(String gameID) throws Exception
    {
		DynamoDbIndex<DynamoPlayerMoney> gsi = playerMoneyTable.index("gsi_GameID");
		
		Key key = Key.builder().partitionValue(gameID).build();
    	QueryConditional qc = QueryConditional.keyEqualTo(key);
    	
    	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                .queryConditional(qc)
                .build();
    	SdkIterable<Page<DynamoPlayerMoney>> pmsByGameID = gsi.query(qer);
    	     
    	PageIterable<DynamoPlayerMoney> pages = PageIterable.create(pmsByGameID);
    	
    	List<DynamoPlayerMoney> dtList = pages.items().stream().toList();
    	
    	if (dtList != null && dtList.size() > 0)
    	{
    		for (int i = 0; i < dtList.size(); i++) 
    		{
    			DynamoPlayerMoney dpm = dtList.get(i);
        		String playerMoneyID = dpm.getPlayerMoneyID();
        		
        		Key key2 = Key.builder().partitionValue(playerMoneyID).build();
        		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key2).build();
        		playerMoneyTable.deleteItem(deleteItemEnhancedRequest);
        	
        		logger.info("LoggedDBOperation: function-delete; table:playermoney; rows:1");        		
			}
    		
    		for (int j = 0; j < playerMoneyList.size(); j++)
    		{
    			PlayerMoney playerMoney = playerMoneyList.get(j);
    			
    			if (playerMoney.getGameID().equalsIgnoreCase(gameID))
    			{
    				this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
    			}
    		}	
    		
    	}			
		
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		logger.info("deletePlayerMoneyFromDB complete");    	
    }

	private DynamoPlayerMoney dynamoUpsert(PlayerMoney playerMoney) throws Exception 
	{
		DynamoPlayerMoney dynamoPlayerMoney = new DynamoPlayerMoney();
        
		if (playerMoney.getPlayerMoneyID() == null)
		{
			dynamoPlayerMoney.setPlayerMoneyID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoPlayerMoney.setPlayerMoneyID(playerMoney.getPlayerMoneyID());
		}
		
		dynamoPlayerMoney.setPlayerID(playerMoney.getPlayerID());
		dynamoPlayerMoney.setAmount(playerMoney.getAmount());
		dynamoPlayerMoney.setDescription(playerMoney.getDescription());
		dynamoPlayerMoney.setGameID(playerMoney.getGameID());
		
		PutItemEnhancedRequest<DynamoPlayerMoney> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayerMoney.class).item(dynamoPlayerMoney).build();
		playerMoneyTable.putItem(putItemEnhancedRequest);
		
		return dynamoPlayerMoney;
	}
	
	public List<PlayerMoney> getPlayerMoneyList() 
	{
		return playerMoneyList;
	}

	public void setPlayerMoneyList(List<PlayerMoney> playerMoneyList) 
	{
		this.playerMoneyList = playerMoneyList;
	}

	public Map<String, PlayerMoney> getPlayerMoneyMap() {
		return playerMoneyMap;
	}

	public void setPlayerMoneyMap(Map<String, PlayerMoney> playerMoneyMap) {
		this.playerMoneyMap = playerMoneyMap;
	}

	

}

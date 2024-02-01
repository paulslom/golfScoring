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
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.Player;
import com.pas.beans.PlayerMoney;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoPlayerMoney;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedResponse;

@Repository
public class PlayerMoneyDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(PlayerMoneyDAO.class);
		
	private List<PlayerMoney> playerMoneyList = new ArrayList<>();
	private Map<String,PlayerMoney> playerMoneyMap = new HashMap<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoPlayerMoney> playerMoneyTable;
	private static final String AWS_TABLE_NAME = "playermoney";

	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       playerMoneyTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerMoney.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing CourseTeeDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
		
	public List<PlayerMoney> getPlayerMoneyByPlayer(Player player)
    {
		List<PlayerMoney> playerMoneyListByPlayer = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getPlayerID() == player.getPlayerID())
			{
				playerMoneyListByPlayer.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByPlayer;
    }
	
	public List<PlayerMoney> getPlayerMoneyByGame(Game game)
    {
		List<PlayerMoney> playerMoneyListByGame = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getGameID() == game.getGameID())
			{
				playerMoneyListByGame.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByGame;
    }
	
	public void readPlayerMoneyFromDB()
    {
		Iterator<DynamoPlayerMoney> results = playerMoneyTable.scan().items().iterator();
		
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
		log.info("LoggedDBOperation: function-inquiry; table:playermoney; rows:" + playerMoneyList.size());		
    }
	
	public void addPlayerMoney(PlayerMoney playerMoney) throws Exception
	{
		DynamoPlayerMoney dpm = dynamoUpsert(playerMoney);
		playerMoney.setPlayerMoneyID(dpm.getPlayerMoneyID());
		
		log.info("LoggedDBOperation: function-add; table:playermoney; rows:1");
		
		this.getPlayerMoneyList().add(playerMoney);
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		
		log.info("addPlayerMoney complete");	
	}
	
	public void updatePlayerMoney(PlayerMoney playerMoney) throws Exception
	{
		dynamoUpsert(playerMoney);
		
		log.info("LoggedDBOperation: function-update; table:playermoney; rows:1");
			
		this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		log.info("updatePlayerMoney complete");		
	}
	
	//deletes all player money rows from the db for this game
	public void deletePlayerMoneyFromDB(String pmID)
    {
		Key key = Key.builder().partitionValue(pmID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		playerMoneyTable.deleteItem(deleteItemEnhancedRequest);
	
		log.info("LoggedDBOperation: function-delete; table:playermoney; rows:1");
		
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getGameID() == pmID)
			{
				this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
			}
		}		
		
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		log.info("deletePlayerMoneyFromDB complete");    	
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
		
		dynamoPlayerMoney.setPlayerID(UUID.randomUUID().toString());
		dynamoPlayerMoney.setAmount(playerMoney.getAmount());
		dynamoPlayerMoney.setDescription(playerMoney.getDescription());
		dynamoPlayerMoney.setGameID(playerMoney.getGameID());
		
		PutItemEnhancedRequest<DynamoPlayerMoney> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayerMoney.class).item(dynamoPlayerMoney).build();
		PutItemEnhancedResponse<DynamoPlayerMoney> putItemEnhancedResponse = playerMoneyTable.putItemWithResponse(putItemEnhancedRequest);
		DynamoPlayerMoney returnedObject = putItemEnhancedResponse.attributes();
		
		if (!returnedObject.equals(dynamoPlayerMoney))
		{
			throw new Exception("something went wrong with dynamo player upsert - returned item not the same as what we attempted to put");
		}	
		
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

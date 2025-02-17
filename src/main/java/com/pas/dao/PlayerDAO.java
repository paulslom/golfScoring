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
import com.pas.beans.Player;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.util.Utils;

import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

public class PlayerDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(PlayerDAO.class);
	
	private Map<String,DynamoPlayer> fullPlayersMapByPlayerID = new HashMap<>(); 
	private Map<String,DynamoPlayer> fullPlayersMapByUserName = new HashMap<String, DynamoPlayer>(); 
	private List<DynamoPlayer> fullPlayerList = new ArrayList<>();
	private List<DynamoPlayer> activePlayerList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME = "players";
	
	@Inject GolfMain golfmain;
	@Inject Player player;

	public PlayerDAO(DynamoClients dynamoClients2) 
	{
	   try 
	   {
	       dynamoClients = dynamoClients2;
	       playersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayer.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing PlayersDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
	
	public String addPlayer(DynamoPlayer dynamoPlayer) throws Exception
	{
		dynamoUpsert(dynamoPlayer);	
		
		logger.info("LoggedDBOperation: function-add; table:player; rows:1");
		
		refreshListsAndMaps("add", dynamoPlayer);	
				
		logger.info("addPlayer complete");		
		
		return dynamoPlayer.getPlayerID(); //this is the key that was just added
	}
	
	private DynamoPlayer dynamoUpsert(DynamoPlayer dynamoPlayer) throws Exception 
	{	    
		if (dynamoPlayer.getPlayerID() == null)
		{
			dynamoPlayer.setPlayerID(UUID.randomUUID().toString());
		}
				
		PutItemEnhancedRequest<DynamoPlayer> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayer.class).item(dynamoPlayer).build();
		playersTable.putItem(putItemEnhancedRequest);
			
		return dynamoPlayer;
	}

	public void updatePlayer(DynamoPlayer dynamoPlayer)  throws Exception
	{
		dynamoUpsert(dynamoPlayer);		
			
		logger.info("LoggedDBOperation: function-update; table:player; rows:1");
		
		refreshListsAndMaps("update", dynamoPlayer);	
		
		logger.debug("update player table complete");		
	}
	
	public void deletePlayer(DynamoPlayer dynamoPlayer) throws Exception 
	{
		Key key = Key.builder().partitionValue(dynamoPlayer.getPlayerID()).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		playersTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:player; rows:1");
		
		refreshListsAndMaps("delete", dynamoPlayer);		
		
		logger.info(Utils.getLoggedInUserName() + " deletePlayer complete");	
	}
	
	public void readPlayersFromDB() 
    {
		Iterator<DynamoPlayer> results = playersTable.scan().items().iterator();
		
		while (results.hasNext()) 
        {
			DynamoPlayer dynamoPlayer = results.next();
          				
            this.getFullPlayerList().add(dynamoPlayer);
            
            if (dynamoPlayer.isActive())
            {
            	this.getActivePlayerList().add(dynamoPlayer);
            }
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:player; rows:" + this.getFullPlayerList().size());
		
		this.setFullPlayersMapByPlayerID(this.getFullPlayerList().stream().collect(Collectors.toMap(DynamoPlayer::getPlayerID, ply -> ply)));
		this.setFullPlayersMapByUserName(this.getFullPlayerList().stream().collect(Collectors.toMap(DynamoPlayer::getUsername, ply -> ply)));	
		
		Collections.sort(this.getFullPlayerList(), new Comparator<DynamoPlayer>() 
		{
		   public int compare(DynamoPlayer o1, DynamoPlayer o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
	}
	
	private void refreshListsAndMaps(String function, DynamoPlayer player)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getFullPlayersMapByPlayerID().remove(player.getPlayerID());	
			this.getFullPlayersMapByUserName().remove(player.getUsername());		
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getFullPlayersMapByPlayerID().put(player.getPlayerID(), player);	
			this.getFullPlayersMapByUserName().put(player.getUsername(), player);
			
			if (player.isActive())
	        {
	           	this.getActivePlayerList().add(player);
	        }
			
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullPlayersMapByPlayerID().remove(player.getPlayerID());	
			this.getFullPlayersMapByUserName().remove(player.getUsername());		
			this.getFullPlayersMapByPlayerID().put(player.getPlayerID(), player);	
			this.getFullPlayersMapByUserName().put(player.getUsername(), player);		
		}
		
		this.getFullPlayerList().clear();
		Collection<DynamoPlayer> values = this.getFullPlayersMapByUserName().values();
		this.setFullPlayerList(new ArrayList<>(values));
		
		Collections.sort(this.getFullPlayerList(), new Comparator<DynamoPlayer>() 
		{
		   public int compare(DynamoPlayer o1, DynamoPlayer o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
		
		Collections.sort(this.getActivePlayerList(), new Comparator<DynamoPlayer>() 
		{
		   public int compare(DynamoPlayer o1, DynamoPlayer o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
		
	}

	public Map<String, DynamoPlayer> getFullPlayersMapByPlayerID() {
		return fullPlayersMapByPlayerID;
	}

	public void setFullPlayersMapByPlayerID(Map<String, DynamoPlayer> fullPlayersMapByPlayerID) {
		this.fullPlayersMapByPlayerID = fullPlayersMapByPlayerID;
	}

	public Map<String, DynamoPlayer> getFullPlayersMapByUserName() {
		return fullPlayersMapByUserName;
	}

	public void setFullPlayersMapByUserName(Map<String, DynamoPlayer> fullPlayersMapByUserName) {
		this.fullPlayersMapByUserName = fullPlayersMapByUserName;
	}

	public List<DynamoPlayer> getFullPlayerList() {
		return fullPlayerList;
	}

	public void setFullPlayerList(List<DynamoPlayer> fullPlayerList) {
		this.fullPlayerList = fullPlayerList;
	}

	public List<DynamoPlayer> getActivePlayerList() {
		return activePlayerList;
	}

	public void setActivePlayerList(List<DynamoPlayer> activePlayerList) {
		this.activePlayerList = activePlayerList;
	}
	

}

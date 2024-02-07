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
import org.springframework.stereotype.Repository;

import com.pas.beans.Player;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

@Repository
public class PlayerDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(PlayerDAO.class);
	
	private Map<String,Player> fullPlayersMapByPlayerID = new HashMap<>(); 
	private Map<String,Player> fullPlayersMapByUserName = new HashMap<String, Player>(); 
	private List<Player> fullPlayerList = new ArrayList<Player>();	
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME = "players";
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       playersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayer.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing PlayersDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
	
	public String addPlayer(Player player) throws Exception
	{
		DynamoPlayer dynamoPlayer = dynamoUpsert(player);		
		 
		player.setPlayerID(dynamoPlayer.getPlayerID());
		
		log.info("LoggedDBOperation: function-add; table:player; rows:1");
		
		refreshListsAndMaps("add", player);	
				
		log.info("addPlayer complete");		
		
		return dynamoPlayer.getPlayerID(); //this is the key that was just added
	}
	
	private DynamoPlayer dynamoUpsert(Player player) throws Exception 
	{
		DynamoPlayer dynamoPlayer = new DynamoPlayer();
        
		if (player.getPlayerID() == null)
		{
			dynamoPlayer.setPlayerID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoPlayer.setPlayerID(player.getPlayerID());
		}
				
		dynamoPlayer.setActive(player.isActive());
		dynamoPlayer.setEmailAddress(player.getEmailAddress());
		dynamoPlayer.setFirstName(player.getFirstName());
		dynamoPlayer.setLastName(player.getLastName());
		dynamoPlayer.setHandicap(player.getHandicap());
		dynamoPlayer.setUsername(player.getUsername());
		
		PutItemEnhancedRequest<DynamoPlayer> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayer.class).item(dynamoPlayer).build();
		playersTable.putItem(putItemEnhancedRequest);
			
		return dynamoPlayer;
	}

	public void updatePlayer(Player player)  throws Exception
	{
		dynamoUpsert(player);		
			
		log.info("LoggedDBOperation: function-update; table:player; rows:1");
		
		refreshListsAndMaps("update", player);	
		
		log.debug("update player table complete");		
	}
	
	public void readPlayersFromDB() 
    {
		Iterator<DynamoPlayer> results = playersTable.scan().items().iterator();
		
		while (results.hasNext()) 
        {
			DynamoPlayer dynamoPlayer = results.next();
            
			Player player = convertDynamoPlayerToPlayer(dynamoPlayer);
						
            this.getFullPlayerList().add(player);			
        }
		
		log.info("LoggedDBOperation: function-inquiry; table:player; rows:" + this.getFullPlayerList().size());
		
		this.setFullPlayersMapByPlayerID(this.getFullPlayerList().stream().collect(Collectors.toMap(Player::getPlayerID, ply -> ply)));
		this.setFullPlayersMapByUserName(this.getFullPlayerList().stream().collect(Collectors.toMap(Player::getUsername, ply -> ply)));	
		
		Collections.sort(this.getFullPlayerList(), new Comparator<Player>() 
		{
		   public int compare(Player o1, Player o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
	}
	
	private void refreshListsAndMaps(String function, Player player)
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
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullPlayersMapByPlayerID().remove(player.getPlayerID());	
			this.getFullPlayersMapByUserName().remove(player.getUsername());		
			this.getFullPlayersMapByPlayerID().put(player.getPlayerID(), player);	
			this.getFullPlayersMapByUserName().put(player.getUsername(), player);		
		}
		
		this.getFullPlayerList().clear();
		Collection<Player> values = this.getFullPlayersMapByUserName().values();
		this.setFullPlayerList(new ArrayList<>(values));
		
		Collections.sort(this.getFullPlayerList(), new Comparator<Player>() 
		{
		   public int compare(Player o1, Player o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
		
	}
	
	public List<Player> getFullPlayerList() 
	{
		return fullPlayerList;
	}

	public void setFullPlayerList(List<Player> fullPlayerList) 
	{
		this.fullPlayerList = fullPlayerList;
	}

	public Map<String, Player> getFullPlayersMapByUserName() 
	{
		return fullPlayersMapByUserName;
	}

	public void setFullPlayersMapByUserName(Map<String, Player> fullPlayersMapByUserName) 
	{
		this.fullPlayersMapByUserName = fullPlayersMapByUserName;
	}

	public Map<String, Player> getFullPlayersMapByPlayerID() {
		return fullPlayersMapByPlayerID;
	}

	public void setFullPlayersMapByPlayerID(Map<String, Player> fullPlayersMapByPlayerID) {
		this.fullPlayersMapByPlayerID = fullPlayersMapByPlayerID;
	}

	public static Player convertDynamoPlayerToPlayer(DynamoPlayer dynamoPlayer) 
	{
		Player player = new Player();
		
		player.setPlayerID(dynamoPlayer.getPlayerID());
		player.setActive(dynamoPlayer.isActive());
		player.setEmailAddress(dynamoPlayer.getEmailAddress());
		player.setFirstName(dynamoPlayer.getFirstName());
		player.setLastName(dynamoPlayer.getLastName());
		player.setHandicap(dynamoPlayer.getHandicap());
		player.setUsername(dynamoPlayer.getUsername());
		
		return player;
	}
	

}

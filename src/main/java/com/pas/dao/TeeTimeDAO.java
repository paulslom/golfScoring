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
import org.springframework.beans.factory.annotation.Autowired;

import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.Group;
import com.pas.beans.TeeTime;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoTeeTime;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

public class TeeTimeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(TeeTimeDAO.class);
		
	private Map<String,TeeTime> teeTimesMap = new HashMap<>();
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();	

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoTeeTime> teeTimesTable;
	private static final String AWS_TABLE_NAME = "teetimes";
	
	@Autowired private final GolfMain golfmain;
	//@Autowired private final Game game;
	
	public TeeTimeDAO(DynamoClients dynamoClients2, GolfMain golfmain) 
	{
		this.golfmain = golfmain;
		//this.game = game;
		
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
	
	public List<TeeTime> getTeeTimesByGame(Game game)
    {
		List<TeeTime> ttList = new ArrayList<>();
		
		for (int i = 0; i < this.getTeeTimeList().size(); i++)
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
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
	
	public void readTeeTimesFromDB(Group defaultGroup) throws Exception
    {
		Iterator<DynamoTeeTime> results = teeTimesTable.scan().items().iterator();
	  	
		Map<String, Game> fullGameMap = new HashMap<>();
		if (golfmain == null  || golfmain.getGameDAO() == null)
		{
			//if golfmain jsf bean unavailable... so just redo the gamedao read
			GameDAO gameDAO = new GameDAO(dynamoClients, golfmain);		
			gameDAO.readGamesFromDB(defaultGroup);
			fullGameMap = gameDAO.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
		}
		else
		{
			fullGameMap = golfmain.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
		}
		
		while (results.hasNext()) 
        {
			DynamoTeeTime dynamoTeeTime = results.next();
          	
			if (fullGameMap != null && fullGameMap.containsKey(dynamoTeeTime.getGameID()))
			{
				Game game = fullGameMap.get(dynamoTeeTime.getGameID());
				
				TeeTime teeTime = new TeeTime(golfmain);
				teeTime.setGameDate(game.getGameDate());
				teeTime.setCourseName(game.getCourseName());
				teeTime.setTeeTimeID(dynamoTeeTime.getTeeTimeID());
				teeTime.setGameID(dynamoTeeTime.getGameID());	
				teeTime.setTeeTimeString(dynamoTeeTime.getTeeTimeString());
				teeTime.setPlayGroupNumber(dynamoTeeTime.getPlayGroupNumber());
				
				this.getTeeTimeList().add(teeTime);			
			}
	    }		 
		
		logger.info("LoggedDBOperation: function-inquiry; table:teetimes; rows:" + teeTimeList.size());
		
		this.setTeeTimesMap(this.getTeeTimeList().stream().collect(Collectors.toMap(TeeTime::getTeeTimeID, tt -> tt)));    	
    }
	
	public TeeTime getTeeTimeByTeeTimeID(String teeTimeID)
    {
		TeeTime teeTime = this.getTeeTimesMap().get(teeTimeID);
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
 				
 			TeeTime teeTime = new TeeTime(golfmain);
 			teeTime.setTeeTimeID(new String());
 			teeTime.setGameID(newGameID);
 			teeTime.setPlayGroupNumber(tokenCount);
 			teeTime.setTeeTimeString(teeTimeStr);
 			teeTime.setGameDate(gameDate);
 			teeTime.setCourseName(courseName);
 			
 			DynamoTeeTime dtt = dynamoUpsert(teeTime);
 			
 			logger.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
 			
 	 		teeTime.setTeeTimeID(dtt.getTeeTimeID());
 			
 			this.getTeeTimeList().add(teeTime);
 			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);
 			
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
		for (int i = 0; i < this.getTeeTimeList().size(); i++)
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
			if (teeTime.getGameID().equalsIgnoreCase(gameID))
			{
				teeTimeIDs.add(teeTime.getTeeTimeID());
				deleteTeeTimeFromDB(teeTime.getTeeTimeID());
			}
		}
		
		for (int i = 0; i < teeTimeIDs.size(); i++) 
		{
			this.getTeeTimesMap().remove(teeTimeIDs.get(i));
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
		
		TeeTime teeTime = new TeeTime(golfmain);
		teeTime.setTeeTimeID(teeTimeID);
		
		refreshListsAndMaps("delete", teeTime); 		
		
		logger.info("deleteTeeTimeFromDB complete");	
    }

	private void refreshListsAndMaps(String function, TeeTime teeTime)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getTeeTimesMap().remove(teeTime.getTeeTimeID());	
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getTeeTimesMap().remove(teeTime.getTeeTimeID());	
			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		
		this.getTeeTimeList().clear();
		Collection<TeeTime> values = this.getTeeTimesMap().values();
		this.setTeeTimeList(new ArrayList<>(values));
		
		Collections.sort(this.getTeeTimeList(), new Comparator<TeeTime>() 
		{
		   public int compare(TeeTime o1, TeeTime o2) 
		   {
		      return o1.getTeeTimeString().compareTo(o2.getTeeTimeString());
		   }
		});
		
	}	
	
	public List<TeeTime> getTeeTimeList() 
	{
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) 
	{
		this.teeTimeList = teeTimeList;
	}

	public Map<String, TeeTime> getTeeTimesMap() {
		return teeTimesMap;
	}

	public void setTeeTimesMap(Map<String, TeeTime> teeTimesMap) {
		this.teeTimesMap = teeTimesMap;
	}	
}

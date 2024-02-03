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
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.TeeTime;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoTeeTime;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedResponse;

@Repository
public class TeeTimeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(TeeTimeDAO.class);
		
	private Map<String,TeeTime> teeTimesMap = new HashMap<>(); //we need this for the TeeTimeConverter class
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();	

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoTeeTime> teeTimesTable;
	private static final String AWS_TABLE_NAME = "teetimes";
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       teeTimesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoTeeTime.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing TeeTimeDAO. Ex = " + ex.getMessage(), ex);
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
	
	public void readTeeTimesFromDB()
    {
		Iterator<DynamoTeeTime> results = teeTimesTable.scan().items().iterator();
	  	
		while (results.hasNext()) 
        {
			DynamoTeeTime dynamoTeeTime = results.next();
          	
			TeeTime teeTime = new TeeTime();

			teeTime.setTeeTimeID(dynamoTeeTime.getTeeTimeID());
			teeTime.setGameID(dynamoTeeTime.getGameID());	
			teeTime.setTeeTimeString(dynamoTeeTime.getTeeTimeString());
			teeTime.setPlayGroupNumber(dynamoTeeTime.getPlayGroupNumber());
			
            this.getTeeTimeList().add(teeTime);			
        }		 
		
		log.info("LoggedDBOperation: function-inquiry; table:teetimes; rows:" + teeTimeList.size());
		
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
		
		log.info("LoggedDBOperation: function-add; table:teetimes; rows:1");		
		
		this.getTeeTimesMap().put(dtt.getTeeTimeID(), teeTime);
		
		refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
				
		log.info("addTeeTime complete");	
	}
	
	public void updateTeeTime(TeeTime teeTime) throws Exception
	{
		dynamoUpsert(teeTime);
		
		log.info("LoggedDBOperation: function-update; table:teetimes; rows:1");
		
		refreshListsAndMaps("update", teeTime);
		
		log.info("updateTeeTime complete");	
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
 			
 			log.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
 			
 	 		teeTime.setTeeTimeID(dtt.getTeeTimeID());
 			
 			this.getTeeTimeList().add(teeTime);
 			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);
 			
 			refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
	 	}
	 	
	 	log.info("addTeeTimes complete");				
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
		PutItemEnhancedResponse<DynamoTeeTime> putItemEnhancedResponse = teeTimesTable.putItemWithResponse(putItemEnhancedRequest);
		DynamoTeeTime returnedObject = putItemEnhancedResponse.attributes();
		
		if (!returnedObject.equals(dynamoTeeTime))
		{
			throw new Exception("something went wrong with dynamo tee timee upsert - returned item not the same as what we attempted to put");
		}	
		
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
		
		log.info("deleteTeeTimeForGameFromDB complete");			
	}
		
	//deletes a particular tee time
	public void deleteTeeTimeFromDB(String teeTimeID)
    {
		Key key = Key.builder().partitionValue(teeTimeID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		teeTimesTable.deleteItem(deleteItemEnhancedRequest);
		
		log.info("LoggedDBOperation: function-delete; table:teetimes; rows:1");
		
		TeeTime teeTime = new TeeTime();
		teeTime.setTeeTimeID(teeTimeID);
		
		refreshListsAndMaps("delete", teeTime); 		
		
		log.info("deleteTeeTimeFromDB complete");	
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

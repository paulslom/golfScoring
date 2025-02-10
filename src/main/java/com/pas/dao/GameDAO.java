package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.pas.dynamodb.*;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.Course;
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.PlayerTeePreference;
import com.pas.beans.Round;
import com.pas.util.Utils;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class GameDAO implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogger(GameDAO.class);
	
	@Inject GolfMain golfmain;
	@Inject Game game;

	private List<DynamoGame> fullGameList = new ArrayList<>();
	private Map<String, DynamoGame> fullGamesMap = new HashMap<>();

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME = "games";
		
	public GameDAO(DynamoClients dynamoClients2)
	{
	   try
	   {
	       dynamoClients = dynamoClients2;
	       gamesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGame.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing GameDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
		
	public String addGame(DynamoGame dynamoGame) throws Exception
	{
		dynamoGame = dynamoUpsert(dynamoGame);	
		
		logger.info("LoggedDBOperation: function-add; table:game; rows:1");
		
		refreshGameList("add", dynamoGame.getGameID(), dynamoGame);
		
		return dynamoGame.getGameID();
	}
	
	public void updateGame(DynamoGame game2) throws Exception
	{
		dynamoUpsert(game2);		
			
		logger.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		refreshGameList("update", game2.getGameID(), game2);	
       		
		logger.debug(getTempUserName() + " update game table complete");		
	}
	
	public void deleteGame(String gameID) throws Exception 
	{
		Key key = Key.builder().partitionValue(gameID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		gamesTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:game; rows:1");
		
		refreshGameList("delete", gameID, null);		
		
		logger.info(getTempUserName() + " deleteGame complete");	
	}
	
	private DynamoGame dynamoUpsert(DynamoGame dynamoGame) throws Exception 
	{
		if (dynamoGame.getGameID() == null)
		{
			dynamoGame.setGameID(UUID.randomUUID().toString());
		}
				
		PutItemEnhancedRequest<DynamoGame> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoGame.class).item(dynamoGame).build();
		gamesTable.putItem(putItemEnhancedRequest);
		
		logger.info("gameID: " + dynamoGame.getGameID());
		
		return dynamoGame;
	}

	public void readGamesFromDB(DynamoGroup defaultGroup, Map<String, Course> coursesMap) throws Exception
    {
		logger.info("entering readGamesFromDB");
	
		String oneMonthAgo = Utils.getOneMonthAgoDate();
		
		logger.info("looking for games newer than: " + oneMonthAgo);
		
		Map<String, AttributeValue> av = Map.of(":min_value", AttributeValue.fromS(oneMonthAgo));
		
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .consistentRead(true)
                .filterExpression(Expression.builder()
                        .expression("gameDate >= :min_value")
                        .expressionValues(av)
                        .build())
                .build();
		
		Iterator<DynamoGame> results = gamesTable.scan(request).items().iterator();
	  	
		int gameCount = 0;
		while (results.hasNext()) 
        {
			gameCount++;
			logger.info("iterating game " + gameCount);
			DynamoGame dynamoGame = results.next();

			String gameDate = dynamoGame.getGameDate();
			Date dGameDate = DateToStringConverter.unconvert(gameDate);
			dynamoGame.setGameDateJava(dGameDate);

			if (coursesMap != null && coursesMap.containsKey(dynamoGame.getCourseID()))
			{
				Course course = coursesMap.get(dynamoGame.getCourseID());
				dynamoGame.setCourse(course);
				dynamoGame.setCourseName(course.getCourseName());
			}

            this.getFullGameList().add(dynamoGame);

			this.setFullGamesMap(fullGameList.stream().collect(Collectors.toMap(DynamoGame::getGameID, gm -> gm)));
        }
		
		Collections.sort(this.getFullGameList(), new Comparator<DynamoGame>()
		{
		   public int compare(DynamoGame o1, DynamoGame o2)
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
		logger.info("LoggedDBOperation: function-inquiry; table:game; rows:" + this.getFullGameList().size());
	}
	
	public List<DynamoGame> getAvailableGames(String playerID) 
    {
		logger.info("entering getAvailableGames for player id: " + playerID);
		
		List<DynamoGame> gameList = new ArrayList<>();
		
		Calendar todayMidnight = new GregorianCalendar();
		todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
		todayMidnight.set(Calendar.MINUTE, 0);
		todayMidnight.set(Calendar.SECOND, 0);
		todayMidnight.set(Calendar.MILLISECOND, 0);
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			DynamoGame availableGame = this.getFullGameList().get(i);
			if (availableGame.getGameDateJava().after(todayMidnight.getTime()))
			{
				gameList.add(availableGame);
			}
		}
				
    	for (int i = 0; i < gameList.size(); i++) 
    	{
			DynamoGame dynamoGame = gameList.get(i);
			Round rd = golfmain.getRoundByGameandPlayer(dynamoGame.getGameID(), playerID);
			
			Integer spotsTaken = golfmain.countRoundsForGameFromDB(dynamoGame);
			Integer spotsAvailable = dynamoGame.getFieldSize() - spotsTaken;
			dynamoGame.setSpotsAvailable(spotsAvailable);
			
			if (rd == null)
			{
				dynamoGame.setRenderSignUp(true);
				dynamoGame.setRenderWithdraw(false);
				dynamoGame.setSelectedCourseTeeID(getTeePreference(playerID, dynamoGame.getCourseID()));
				golfmain.assignCourseToGame(dynamoGame);
			}
			else
			{
				dynamoGame.setRenderSignUp(false);
				dynamoGame.setRenderWithdraw(true);
				dynamoGame.setSelectedCourseTeeID(rd.getCourseTeeID());
			}
			
			logger.info("in getAvailableGames, game id: " + dynamoGame.getGameID() + " game date: " + dynamoGame.getGameDateJava() + " player id: " + playerID + " renderSignup: " + dynamoGame.isRenderSignUp());
			
		} 
    	
    	Collections.sort(gameList, new Comparator<DynamoGame>() 
		{
		   public int compare(DynamoGame o1, DynamoGame o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
    	
    	return gameList;
	}
	
	public String getTeePreference(String playerID, String courseID) 
	{
		PlayerTeePreference ptp = golfmain.getPlayerTeePreference(playerID, courseID);
		if (ptp != null)
		{
			return ptp.getCourseTeeID();
		}
		return null;
	}

	public List<DynamoGame> getFutureGames() 
    {
		List<DynamoGame> gameList = new ArrayList<>();
		
		Calendar todayMidnight = new GregorianCalendar();
		todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
		todayMidnight.set(Calendar.MINUTE, 0);
		todayMidnight.set(Calendar.SECOND, 0);
		todayMidnight.set(Calendar.MILLISECOND, 0);
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			DynamoGame availableGame = this.getFullGameList().get(i);
			if (availableGame.getGameDateJava().after(todayMidnight.getTime()))
			{
				gameList.add(availableGame);
			}
		}
		
		Collections.sort(gameList, new Comparator<DynamoGame>() 
		{
		   public int compare(DynamoGame o1, DynamoGame o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
    	return gameList;
	}
		
	public DynamoGame getGameByGameID(String gameID) 
    {
		Map<String, DynamoGame> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(DynamoGame::getGameID, game -> game));
		DynamoGame game = fullGameMap.get(gameID);
    	return game;		
	}

	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}
		
	private void refreshGameList(String function, String gameID, DynamoGame dynamoGame) throws Exception
	{	
		if (function.equalsIgnoreCase("add"))
		{			
			this.getFullGamesMap().put(gameID,dynamoGame);
		}
		else if (function.equalsIgnoreCase("delete"))
		{
			this.getFullGamesMap().remove(gameID);
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullGamesMap().replace(gameID, dynamoGame);
		}
			
		this.getFullGameList().clear();
		Collection<DynamoGame> values = this.getFullGamesMap().values();
		this.setFullGameList(new ArrayList<DynamoGame>(values));

		Collections.sort(this.getFullGameList(), new Comparator<DynamoGame>()
		{
		   public int compare(DynamoGame o1, DynamoGame o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
		/* for debugging purposes 
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		
	}

	public Map<String, DynamoGame> getFullGamesMap()
	{
		return fullGamesMap;
	}

	public void setFullGamesMap(Map<String, DynamoGame> fullGamesMap)
	{
		this.fullGamesMap = fullGamesMap;
	}

	public List<DynamoGame> getFullGameList()
	{
		/* for debugging purposes
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		return fullGameList;
	}

	public void setFullGameList(List<DynamoGame> fullGameList)
	{
		this.fullGameList = fullGameList;
	}
	
}

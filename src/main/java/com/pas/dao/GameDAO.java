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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.pas.beans.Course;
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.Group;
import com.pas.beans.PlayerTeePreference;
import com.pas.beans.Round;
import com.pas.dynamodb.DateToStringConverter;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoUtil;
import com.pas.util.BeanUtilJSF;
import com.pas.util.Utils;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;


@Repository
public class GameDAO implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	
	private static Logger log = LogManager.getLogger(GameDAO.class);
	
	private List<Game> fullGameList = new ArrayList<Game>();		

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME = "games";
		
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       gamesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGame.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing GameDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
		
	public String addGame(Game game) throws Exception
	{
		DynamoGame dynamoGame = dynamoUpsert(game);	
		game.setGameID(dynamoGame.getGameID());
		
		log.info("LoggedDBOperation: function-add; table:game; rows:1");
		
		refreshGameList("add", game.getGameID(), game);
		
		return game.getGameID();
	}
	
	public void updateGame(Game game) throws Exception
	{
		dynamoUpsert(game);		
			
		log.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		refreshGameList("update", game.getGameID(), game);	
       		
		log.debug(getTempUserName() + " update game table complete");		
	}
	
	public void deleteGame(String gameID) throws Exception 
	{
		Key key = Key.builder().partitionValue(gameID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		gamesTable.deleteItem(deleteItemEnhancedRequest);
		
		log.info("LoggedDBOperation: function-delete; table:game; rows:1");
		
		refreshGameList("delete", gameID, null);		
		
		log.info(getTempUserName() + " deleteGame complete");	
	}
	
	private DynamoGame dynamoUpsert(Game game) throws Exception 
	{
		DynamoGame dynamoGame = new DynamoGame();
        
		if (game.getGameID() == null)
		{
			dynamoGame.setGameID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoGame.setGameID(game.getGameID());
		}
		
		dynamoGame.setOldGameID(game.getOldGameID());		
		dynamoGame.setGameDate(DateToStringConverter.convertDateToDynamoStringFormat(game.getGameDate()));
		dynamoGame.setCourseID(game.getCourseID());
		dynamoGame.setFieldSize(game.getFieldSize());
		dynamoGame.setTotalPlayers(game.getTotalPlayers());
		dynamoGame.setTotalTeams(game.getTotalTeams());
		dynamoGame.setSkinsPot(game.getSkinsPot());
		dynamoGame.setTeamPot(game.getTeamPot());
		dynamoGame.setBetAmount(game.getBetAmount());
		dynamoGame.setHowManyBalls(game.getHowManyBalls());
		dynamoGame.setPurseAmount(game.getPurseAmount());
		dynamoGame.setEachBallWorth(game.getEachBallWorth());
		dynamoGame.setIndividualGrossPrize(game.getIndividualGrossPrize());
		dynamoGame.setIndividualNetPrize(game.getIndividualNetPrize());
		dynamoGame.setPlayTheBallMethod(game.getPlayTheBallMethod());
		dynamoGame.setGameClosedForSignups(game.isGameClosedForSignups());
		dynamoGame.setGameNoteForEmail(game.getGameNoteForEmail());
		
		PutItemEnhancedRequest<DynamoGame> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoGame.class).item(dynamoGame).build();
		gamesTable.putItem(putItemEnhancedRequest);
				
		return dynamoGame;
	}

	public void readGamesFromDB() throws Exception 
    {
		String oneMonthAgo = Utils.getOneMonthAgoDate();
		Map<String, AttributeValue> av = Map.of(":min_value", AttributeValue.fromS(oneMonthAgo));
		
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .consistentRead(true)
                .filterExpression(Expression.builder()
                        .expression("gameDate >= :min_value")
                        .expressionValues(av)
                        .build())
                .build();
		
		Iterator<DynamoGame> results = gamesTable.scan(request).items().iterator();
	  	
		while (results.hasNext()) 
        {
			DynamoGame dynamoGame = results.next();
          	
			Game game = new Game();

			game.setGameID(dynamoGame.getGameID());
			game.setOldGameID(dynamoGame.getOldGameID());		
			
			String gameDate = dynamoGame.getGameDate();
			DateToStringConverter dsc = new DateToStringConverter();
			Date dGameDate = dsc.unconvert(gameDate);
			game.setGameDate(dGameDate);
			
			game.setCourseID(dynamoGame.getCourseID());
			
			GolfMain golfmain = null;
			try
			{
				golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
			}
			catch (Exception e)
			{			
			}
			
			Map<String, Course> coursesMap = new HashMap<>();
			if (golfmain == null) //if golfmain jsf bean unavailable... so just redo the gamedao read
			{
				GroupDAO groupDAO = new GroupDAO();
				groupDAO.readGroupsFromDB();
				Group defaultGroup = groupDAO.getGroupsList().get(0);
				CourseDAO courseDAO = new CourseDAO();		
				courseDAO.readCoursesFromDB(defaultGroup);
				coursesMap = courseDAO.getCourseSelections().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
			}
			else
			{
				coursesMap = golfmain.getCoursesMap();
			}
			
			if (coursesMap != null && coursesMap.containsKey(game.getCourseID()))
			{
				Course course = coursesMap.get(game.getCourseID());
				game.setCourse(course);
				game.setCourseName(course.getCourseName());
			}
			
			game.setFieldSize(dynamoGame.getFieldSize());
			game.setTotalPlayers(dynamoGame.getTotalPlayers());
			game.setTotalTeams(dynamoGame.getTotalTeams());
			game.setSkinsPot(dynamoGame.getSkinsPot());
			game.setTeamPot(dynamoGame.getTeamPot());
			game.setBetAmount(dynamoGame.getBetAmount());
			game.setHowManyBalls(dynamoGame.getHowManyBalls());
			game.setPurseAmount(dynamoGame.getPurseAmount());
			game.setEachBallWorth(dynamoGame.getEachBallWorth());
			game.setIndividualGrossPrize(dynamoGame.getIndividualGrossPrize());
			game.setIndividualNetPrize(dynamoGame.getIndividualNetPrize());
			game.setPlayTheBallMethod(dynamoGame.getPlayTheBallMethod());
			game.setGameClosedForSignups(dynamoGame.isGameClosedForSignups());
			game.setGameNoteForEmail(dynamoGame.getGameNoteForEmail());	
			
            this.getFullGameList().add(game);			
        }
		
		Collections.sort(this.getFullGameList(), new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
		log.info("LoggedDBOperation: function-inquiry; table:game; rows:" + this.getFullGameList().size());
	}
	
	public List<Game> getAvailableGames(String playerID) 
    {
		List<Game> gameList = new ArrayList<>();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		Calendar todayMidnight = new GregorianCalendar();
		todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
		todayMidnight.set(Calendar.MINUTE, 0);
		todayMidnight.set(Calendar.SECOND, 0);
		todayMidnight.set(Calendar.MILLISECOND, 0);
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			Game availableGame = this.getFullGameList().get(i);
			if (availableGame.getGameDate().after(todayMidnight.getTime()))
			{
				gameList.add(availableGame);
			}
		}
				
    	for (int i = 0; i < gameList.size(); i++) 
    	{
			Game gm = gameList.get(i);
			Round rd = golfmain.getRoundByGameandPlayer(gm.getGameID(), playerID);
			
			Integer spotsTaken = golfmain.countRoundsForGameFromDB(gm);
			Integer spotsAvailable = gm.getFieldSize() - spotsTaken;
			gm.setSpotsAvailable(spotsAvailable);
			
			if (rd == null)
			{
				gm.setRenderSignUp(true);
				gm.setRenderWithdraw(false);
				gm.setCourseTeeID(getTeePreference(playerID, gm.getCourseID()));
			}
			else
			{
				gm.setRenderSignUp(false);
				gm.setRenderWithdraw(true);
				gm.setCourseTeeID(rd.getCourseTeeID());
			}			
			
		} 
    	
    	Collections.sort(gameList, new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
    	
    	return gameList;
	}
	
	public String getTeePreference(String playerID, String courseID) 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		PlayerTeePreference ptp = golfmain.getPlayerTeePreference(playerID, courseID);
		if (ptp != null)
		{
			return ptp.getCourseTeeID();
		}
		return null;
	}

	public List<Game> getFutureGames() 
    {
		List<Game> gameList = new ArrayList<>();
		
		Calendar todayMidnight = new GregorianCalendar();
		todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
		todayMidnight.set(Calendar.MINUTE, 0);
		todayMidnight.set(Calendar.SECOND, 0);
		todayMidnight.set(Calendar.MILLISECOND, 0);
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			Game availableGame = this.getFullGameList().get(i);
			if (availableGame.getGameDate().after(todayMidnight.getTime()))
			{
				gameList.add(availableGame);
			}
		}
		
		Collections.sort(gameList, new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
    	return gameList;
	}
	
	public List<Game> getAvailableGamesByPlayerID(int playerID) 
    {
		List<Game> gameList = new ArrayList<>();
		
		Calendar todayMidnight = new GregorianCalendar();
		todayMidnight.set(Calendar.HOUR_OF_DAY, 0);
		todayMidnight.set(Calendar.MINUTE, 0);
		todayMidnight.set(Calendar.SECOND, 0);
		todayMidnight.set(Calendar.MILLISECOND, 0);
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			Game availableGame = this.getFullGameList().get(i);
			if (availableGame.getGameDate().after(todayMidnight.getTime()))
			{
				gameList.add(availableGame);
				//this is not enough though - need to know if the player is a part of the game.
			}
		}
			
		Collections.sort(gameList, new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
    	return gameList;
	}
	
	public Game getGameByGameID(String gameID) 
    {
		Map<String, Game> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
		Game game = fullGameMap.get(gameID);
    	return game;		
	}

	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}
		
	private void refreshGameList(String function, String gameID, Game inputgame) throws Exception
	{	
		Game gm = new Game();
		
		if (!function.equalsIgnoreCase("delete"))
		{
			gm.setGameID(inputgame.getGameID());
					
			if (inputgame.getCourseName() != null)
			{
				gm.setCourseName(inputgame.getCourseName());
			}
			else if (inputgame.getCourse() != null)
			{
				gm.setCourseName(inputgame.getCourse().getCourseName());
			}
			
			gm.setCourse(inputgame.getCourse());		
			gm.setCourseID(inputgame.getCourseID());
			gm.setGameDate(inputgame.getGameDate());
			gm.setBetAmount(inputgame.getBetAmount());
			gm.setEachBallWorth(inputgame.getEachBallWorth());
			gm.setHowManyBalls(inputgame.getHowManyBalls());
			gm.setIndividualGrossPrize(inputgame.getIndividualGrossPrize());
			gm.setIndividualNetPrize(inputgame.getIndividualNetPrize());
			gm.setPurseAmount(inputgame.getPurseAmount());
			gm.setSkinsPot(inputgame.getSkinsPot());
			gm.setTeamPot(inputgame.getTeamPot());
			gm.setFieldSize(inputgame.getFieldSize());
			gm.setTotalPlayers(inputgame.getTotalPlayers());
			gm.setTotalTeams(inputgame.getTotalTeams());
			gm.setPlayTheBallMethod(inputgame.getPlayTheBallMethod());
		}		
		
		if (function.equalsIgnoreCase("add"))
		{			
			this.getFullGameList().add(gm);
		}
		else
		{
			Map<String, Game> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
			
			if (function.equalsIgnoreCase("delete"))
			{
				fullGameMap.remove(gameID);			
			}
			else if (function.equalsIgnoreCase("update"))
			{
				fullGameMap.remove(inputgame.getGameID());
				fullGameMap.put(inputgame.getGameID(), gm);
			}
			
			this.getFullGameList().clear();
			Collection<Game> values = fullGameMap.values();
			this.setFullGameList(new ArrayList<>(values));
		}		
		
		Collections.sort(this.getFullGameList(), new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
		/* for debugging purposes 
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			log.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		
	}

	public List<Game> getFullGameList() 
	{
		/* for debugging purposes
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			log.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		return fullGameList;
	}

	public void setFullGameList(List<Game> fullGameList) 
	{
		this.fullGameList = fullGameList;
	}
	
}

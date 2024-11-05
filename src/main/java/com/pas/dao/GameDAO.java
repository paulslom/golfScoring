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
import org.springframework.beans.factory.annotation.Autowired;

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
	
	@Autowired private final GolfMain golfmain;

	private List<Game> fullGameList = new ArrayList<Game>();		

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME = "games";
		
	public GameDAO(DynamoClients dynamoClients2, GolfMain golfmain) 
	{
		this.golfmain = golfmain;
		
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
		
	public String addGame(Game game, String teeTimesString) throws Exception
	{
		Date gameDate = Utils.getGameDateTimeUsingTeeTimeString(game.getGameDate(), teeTimesString);
		game.setGameDate(gameDate);	
		
		DynamoGame dynamoGame = dynamoUpsert(game);	
		game.setGameID(dynamoGame.getGameID());
		
		logger.info("LoggedDBOperation: function-add; table:game; rows:1");
		
		refreshGameList("add", game.getGameID(), game);
		
		return game.getGameID();
	}
	
	public void updateGame(Game game) throws Exception
	{
		dynamoUpsert(game);		
			
		logger.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		refreshGameList("update", game.getGameID(), game);	
       		
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
		dynamoGame.setGameFee(game.getGameFee());
		
		PutItemEnhancedRequest<DynamoGame> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoGame.class).item(dynamoGame).build();
		gamesTable.putItem(putItemEnhancedRequest);
				
		return dynamoGame;
	}

	

	public void readGamesFromDB(Group defaultGroup) throws Exception 
    {
		logger.info("entering readGamesFromDB");
		
		Map<String, Course> coursesMap = new HashMap<>();
		if (golfmain == null || golfmain.getCourseDAO() == null) //if golfmain jsf bean unavailable... so just redo the gamedao read
		{
			DynamoClients dynamoClients = DynamoUtil.getDynamoClients();				
			CourseDAO courseDAO = new CourseDAO(dynamoClients);		
			courseDAO.readCoursesFromDB(defaultGroup);
			coursesMap = courseDAO.getCourseSelections().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
		}
		else
		{
			coursesMap = golfmain.getCoursesMap();
		}
		
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
          	
			Game game = new Game(golfmain);

			game.setGameID(dynamoGame.getGameID());
			game.setOldGameID(dynamoGame.getOldGameID());		
			
			String gameDate = dynamoGame.getGameDate();
			DateToStringConverter dsc = new DateToStringConverter();
			Date dGameDate = dsc.unconvert(gameDate);
			game.setGameDate(dGameDate);
			
			game.setCourseID(dynamoGame.getCourseID());				
			
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
			game.setGameFee(dynamoGame.getGameFee());
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
		
		logger.info("LoggedDBOperation: function-inquiry; table:game; rows:" + this.getFullGameList().size());
	}
	
	public List<Game> getAvailableGames(String playerID) 
    {
		logger.info("entering getAvailableGames for player id: " + playerID);
		
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
			
			logger.info("in getAvailableGames, game id: " + gm.getGameID() + " game date: " + gm.getGameDateDisplay() + " player id: " + playerID + " renderSignup: " + gm.isRenderSignUp());
			
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
		Game gm = new Game(golfmain);
		
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
			gm.setGameFee(inputgame.getGameFee());
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
			logger.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
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
			logger.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		return fullGameList;
	}

	public void setFullGameList(List<Game> fullGameList) 
	{
		this.fullGameList = fullGameList;
	}
	
}

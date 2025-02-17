package com.pas.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.Course;
import com.pas.beans.Round;
import com.pas.beans.TeeTime;
import com.pas.dao.CourseDAO;
import com.pas.dao.GameDAO;
import com.pas.dao.GroupDAO;
import com.pas.dao.PlayerDAO;
import com.pas.dao.RoundDAO;
import com.pas.dao.TeeTimeDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoUtil;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DailyEmailJob implements Runnable 
{
	private static DynamoClients dynamoClients;
	private static Logger logger = LogManager.getLogger(DailyEmailJob.class);
	
	private static long TEN_HOURS = 36000000; //in milliseconds
	private static long SIX_DAYS = 518400000; //in milliseconds
	private static DynamoGroup defaultGroup;

	private List<DynamoGame> gameList = new ArrayList<>();
		
	@Override
	public void run() 
	{
		logger.info("Running Daily email job");
		
		Date todaysDate = new Date();
		
		try 
		{
			dynamoClients = DynamoUtil.getDynamoClients();
			
			GroupDAO groupDAO = new GroupDAO(dynamoClients);
			groupDAO.readGroupsFromDB();
			defaultGroup = groupDAO.getGroupsList().get(0);
			
			CourseDAO courseDAO = new CourseDAO(dynamoClients);
			courseDAO.readCoursesFromDB(defaultGroup); 
			
			GameDAO gameDAO = new GameDAO(dynamoClients);
			gameDAO.readGamesFromDB(defaultGroup, courseDAO.getCoursesMap());
			gameList = gameDAO.getFullGameList();	
			
			PlayerDAO playerDAO = new PlayerDAO(dynamoClients); 
			playerDAO.readPlayersFromDB();
			List<DynamoPlayer> fullPlayerList = playerDAO.getFullPlayerList();
			
			TeeTimeDAO teeTimeDAO = new TeeTimeDAO(dynamoClients);			
			RoundDAO roundDAO = new RoundDAO(dynamoClients);
			
			List<DynamoGame> futuregameslist = getFutureGames(courseDAO);
			
			//if there is a game coming up within 6 days, email about it.  Don't do today's though.
			
			for (int i = 0; i < futuregameslist.size(); i++) 
			{
				DynamoGame dynamoGame = futuregameslist.get(i);
				
				Date gameDate = dynamoGame.getGameDateJava();
				
				long diffInMillies = Math.abs(gameDate.getTime() - todaysDate.getTime());
			
			    if (diffInMillies >= TEN_HOURS && diffInMillies <= SIX_DAYS)
			    {
			    	List<TeeTime> teeTimeList = teeTimeDAO.readTeeTimesForGame(defaultGroup, dynamoGame.getGameID());
			    	List<String> gameIDList = new ArrayList<>();
			    	gameIDList.add(dynamoGame.getGameID());
			    	roundDAO.readAllRoundsFromDB(gameIDList);
			    	List<Round> roundsForGame = roundDAO.getRoundsForGame(dynamoGame);
					String futureGameEmailMessage = Utils.composeFutureGameEmail(dynamoGame, fullPlayerList, teeTimeList, roundsForGame);
					Utils.sendFutureGameEmail(dynamoGame, fullPlayerList, futureGameEmailMessage);
			    }
			}
			
		} 
		catch (Exception e) 
		{
			logger.error("Exception encountered in DailyEmailJob: " + e.getMessage(), e);
		}		
   
	}
	
	private List<DynamoGame> getFutureGames(CourseDAO courseDAO) throws Exception
	{
		List<DynamoGame> tempList = new ArrayList<>();
				
		Date today = new Date();
		LocalDate localDateToday = today.toInstant().atZone(ZoneId.of(Utils.MY_TIME_ZONE)).toLocalDate();

		for (int i = 0; i < gameList.size(); i++) 
		{
			DynamoGame loopedGame = gameList.get(i);
			Date tempDate = loopedGame.getGameDateJava();
			Calendar calGameDate = Calendar.getInstance();
			calGameDate.setTime(tempDate);
			LocalDate localGameDate = LocalDate.of(calGameDate.get(Calendar.YEAR), calGameDate.get(Calendar.MONTH) + 1, calGameDate.get(Calendar.DAY_OF_MONTH));
		
			if (localGameDate.isAfter(localDateToday))
			{
				long noOfDaysBetween = ChronoUnit.DAYS.between(localDateToday, localGameDate);
				
				if (noOfDaysBetween <=6)
				{
					tempList.add(loopedGame);
				}				
			}
			
		}
		
	    for (int i = 0; i < tempList.size(); i++) 
		{
			DynamoGame tempGame = tempList.get(i);
			Course tempCourse = courseDAO.getCoursesMap().get(tempGame.getCourseID());
			logger.info("LoggedDBOperation: function-inquiry; table:course; rows:1");
			tempGame.setCourseName(tempCourse.getCourseName());
		}
        
		return tempList;
	}
		
}

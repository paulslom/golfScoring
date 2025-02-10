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
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.dao.CourseDAO;
import com.pas.dao.GroupDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class DailyEmailJob implements Runnable 
{
	private static DynamoClients dynamoClients;
	private static Logger logger = LogManager.getLogger(DailyEmailJob.class);
	
	private static long TEN_HOURS = 36000000; //in milliseconds
	private static long SIX_DAYS = 518400000; //in milliseconds
	private static DynamoGroup defaultGroup;

	@Inject GolfMain golfmain;
	@Inject Game game;

	@PostConstruct
	public void init() throws Exception
	{
		dynamoClients = DynamoUtil.getDynamoClients();
		
		GroupDAO groupDAO = new GroupDAO(dynamoClients);
		groupDAO.readGroupsFromDB();
		defaultGroup = groupDAO.getGroupsList().get(0);
	}
	
	@Override
	public void run() 
	{
		logger.info("Running Daily email job");
		
		Date todaysDate = new Date();
		
		try 
		{
			List<DynamoGame> futuregameslist = getFutureGames();
			
			//if there is a game coming up within 6 days, email about it.  Don't do today's though.
			
			for (int i = 0; i < futuregameslist.size(); i++) 
			{
				DynamoGame dynamoGame = futuregameslist.get(i);
				Date gameDate = dynamoGame.getGameDateJava();
				
				long diffInMillies = Math.abs(gameDate.getTime() - todaysDate.getTime());
			
			    if (diffInMillies >= TEN_HOURS && diffInMillies <= SIX_DAYS)
			    {
					game.setSelectedGame(dynamoGame);
					game.composeFutureGameEmail();
			    	game.sendFutureGameEmail(game.getSelectedGame());
			    }
			}
			
		} 
		catch (Exception e) 
		{
			logger.error("Exception encountered in DailyEmailJob: " + e.getMessage(), e);
		}		
   
	}
	
	private List<DynamoGame> getFutureGames() throws Exception
	{
		List<DynamoGame> gameList = golfmain.getFullGameList();
		
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
		
		DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
		CourseDAO courseDAO = new CourseDAO(dynamoClients);
		courseDAO.readCoursesFromDB(defaultGroup);
		
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

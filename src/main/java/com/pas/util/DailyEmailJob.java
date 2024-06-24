package com.pas.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.Course;
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.Group;
import com.pas.beans.Round;
import com.pas.beans.TeeTime;
import com.pas.dao.CourseDAO;
import com.pas.dao.GameDAO;
import com.pas.dao.GroupDAO;
import com.pas.dao.PlayerDAO;
import com.pas.dao.RoundDAO;
import com.pas.dao.TeeTimeDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

public class DailyEmailJob implements Runnable 
{
	private static DynamoClients dynamoClients;
	private static Logger logger = LogManager.getLogger(DailyEmailJob.class);
	
	private static long TEN_HOURS = 36000000; //in milliseconds
	private static long SIX_DAYS = 518400000; //in milliseconds
	
	private static String NEWLINE = "<br/>";
	
	private static Group defaultGroup;
	
	public DailyEmailJob() throws Exception 
	{
		dynamoClients = DynamoUtil.getDynamoClients();
		
		GroupDAO groupDAO = new GroupDAO(dynamoClients, new GolfMain("ignore"));
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
			List<Game> futuregameslist = getFutureGames();
			
			//if there is a game coming up within 6 days, email about it.  Don't do today's though.
			
			for (int i = 0; i < futuregameslist.size(); i++) 
			{
				Game game = futuregameslist.get(i);
				Date gameDate = game.getGameDate();
				
				long diffInMillies = Math.abs(gameDate.getTime() - todaysDate.getTime());
			
			    if (diffInMillies >= TEN_HOURS && diffInMillies <= SIX_DAYS)
			    {
					game.setSelectedGame(game);
					game.composeFutureGameEmail(false); //the false will allow it to skip email recipients jsf bean call; we'll have to do that ourselves.			
			    	sendFutureGameEmail(game);
			    }
			}
			
		} 
		catch (Exception e) 
		{
			logger.error("Exception encountered in DailyEmailJob: " + e.getMessage(), e);
		}		
   
	}

	private void sendFutureGameEmail(Game inputGame) throws Exception 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	
		
		String gameDateStr = sdf.format(inputGame.getGameDate());	
		logger.info("I've got a game with date = " + gameDateStr + " that I will set up email for.");
		
		String subjectLine = "Golf game on " + Utils.getDayofWeekString(inputGame.getGameDate()) + " " + sdf.format(inputGame.getGameDate()) + " on " + inputGame.getCourseName();
		
		inputGame.setFutureGameEmailMessage(inputGame.getFutureGameEmailMessage().replace("~~~teeTimes~~~", getTeeTimes(inputGame)));
		inputGame.setFutureGameEmailMessage(inputGame.getFutureGameEmailMessage().replace("~~~gameDetails~~~", getGameParticipants(inputGame)));
		
		logger.info("establishing email recipients");		
		ArrayList<String> emailRecipients = establishEmailRecipients(inputGame);		
		logger.info("email recipients successfully established");
		
		SAMailUtility.sendEmail(subjectLine, inputGame.getFutureGameEmailMessage(), emailRecipients, false); //last false parameter means do not use jsf
		logger.info("email successfully sent");
	}

	private String getTeeTimes(Game inputGame) throws Exception 
	{
		TeeTimeDAO teeTimeDAO = new TeeTimeDAO(dynamoClients, new GolfMain("ignore"));
		teeTimeDAO.readTeeTimesFromDB(defaultGroup);
		List<TeeTime> teeTimeList = teeTimeDAO.getTeeTimesByGame(inputGame);
		
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < teeTimeList.size(); i++) 
		{
			TeeTime teeTime = teeTimeList.get(i);
			sb.append(teeTime.getTeeTimeString() + " ");
		}
		
		sb.append(NEWLINE);
		
		return sb.toString();
	}

	private ArrayList<String> establishEmailRecipients(Game inputGame) 
	{
		PlayerDAO playerDAO = new PlayerDAO(dynamoClients, new GolfMain("ignore"));
		playerDAO.readPlayersFromDB();
		ArrayList<String> emailRecips = Utils.setEmailFullRecipientList(playerDAO.getFullPlayerList());		
		
		/*
		ArrayList<String> emailRecips = new ArrayList<>();
		emailRecips.add("paulslomkowski@yahoo.com");
		*/
		
		return emailRecips;
	}
	
	private List<Game> getFutureGames() throws Exception
	{
		GameDAO gameDAO = new GameDAO(dynamoClients, new GolfMain("ignore"));		
		gameDAO.readGamesFromDB(defaultGroup);
		List<Game> gameList = gameDAO.getFullGameList();
		
		List<Game> tempList = new ArrayList<>();
				
		Date today = new Date();
		LocalDate localDateToday = today.toInstant().atZone(ZoneId.of(Utils.MY_TIME_ZONE)).toLocalDate();

		for (int i = 0; i < gameList.size(); i++) 
		{
			Game loopedGame = gameList.get(i);
			Date tempDate = loopedGame.getGameDate();
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
			Game tempGame = tempList.get(i);			
			Course tempCourse = courseDAO.getCoursesMap().get(tempGame.getCourseID());
			logger.info("LoggedDBOperation: function-inquiry; table:course; rows:1");
			tempGame.setCourse(tempCourse);
			tempGame.setCourseName(tempGame.getCourse().getCourseName());		
		}
        
		return tempList;
	}
	
	private String getGameParticipants(Game inputGame)  throws Exception 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("Current list of players for this game:");
		sb.append(NEWLINE);
		
		GameDAO gameDAO = new GameDAO(dynamoClients, new GolfMain("ignore"));		
		gameDAO.readGamesFromDB(defaultGroup);
		List<Game> gameList = gameDAO.getFullGameList();
		
		List<String> gameIDList = new ArrayList<>();
		
		for (int i = 0; i < gameList.size(); i++) 
		{
			Game game = gameDAO.getFullGameList().get(i);
			gameIDList.add(game.getGameID());
		}
		RoundDAO roundDAO = new RoundDAO(dynamoClients, new GolfMain("ignore"), inputGame);
		roundDAO.readAllRoundsFromDB(gameIDList);
		List<Round> roundList = roundDAO.getRoundsForGame(inputGame);
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone(Utils.MY_TIME_ZONE);
		signupSDF.setTimeZone(etTimeZone);
		
		List<String> roundPlayers = new ArrayList<>();
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			String signupDateTime = signupSDF.format(rd.getSignupDateTime());
			String playerName = rd.getPlayerName();
			roundPlayers.add(playerName + " (signed up: " + signupDateTime + ")");
		}
				
		for (int i = 0; i < roundPlayers.size(); i++) 
		{
			String playerName = roundPlayers.get(i);
			if (i+1 <= inputGame.getFieldSize())
			{
				sb.append(i+1 + ". " + playerName);
			}
			else
			{
				sb.append(i+1 + ". " + playerName + " (wait list)");
			}
			sb.append(NEWLINE);
		}
		
		int spotsAvailable = inputGame.getFieldSize() - roundPlayers.size();
		
		sb.append(NEWLINE);
		sb.append("Spots still available: " + spotsAvailable);
		sb.append(NEWLINE);
		
		return sb.toString();
	}
	
}

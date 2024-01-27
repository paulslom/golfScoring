package com.pas.util;

import java.sql.ResultSet;
import java.sql.SQLException;
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
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.pas.beans.Course;
import com.pas.beans.Game;
import com.pas.beans.Player;
import com.pas.beans.TeeTime;

public class DailyEmailJob implements Runnable 
{
	private static Logger log = LogManager.getLogger(DailyEmailJob.class);
	
	private static long TEN_HOURS = 36000000; //in milliseconds
	private static long SIX_DAYS = 518400000; //in milliseconds
	
	private static String NEWLINE = "<br/>";	
	
	@Override
	public void run() 
	{
		log.info("Running Daily email job");
		
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
			log.error("Exception encountered in DailyEmailJob: " + e.getMessage(), e);
		}		
   
	}

	private void sendFutureGameEmail(Game inputGame) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	
		
		String gameDateStr = sdf.format(inputGame.getGameDate());	
		log.info("I've got a game with date = " + gameDateStr + " that I will set up email for.");
		
		String subjectLine = "Golf game on " + Utils.getDayofWeekString(inputGame.getGameDate()) + " " + sdf.format(inputGame.getGameDate()) + " on " + inputGame.getCourseName();
		
		inputGame.setFutureGameEmailMessage(inputGame.getFutureGameEmailMessage().replace("~~~teeTimes~~~", getTeeTimes(inputGame)));
		inputGame.setFutureGameEmailMessage(inputGame.getFutureGameEmailMessage().replace("~~~gameDetails~~~", getGameParticipants(inputGame)));
		
		log.info("establishing email recipients");		
		ArrayList<String> emailRecipients = establishEmailRecipients();		
		log.info("email recipients successfully established");
		
		SAMailUtility.sendEmail(subjectLine, inputGame.getFutureGameEmailMessage(), emailRecipients, false); //last false parameter means do not use jsf
		log.info("email successfully sent");
	}

	private String getTeeTimes(Game inputGame) 
	{
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Utils.getDatasourceProperties());
		String sql = "select tt.idTeeTimes, tt.idgame, tt.playgroupnumber, tt.teetime, gm.gameDate, cs.courseName from teetimes tt inner join game gm on tt.idgame = gm.idgame inner join golfcourse cs on gm.idgolfcourse = cs.idgolfcourse where tt.idgame = :idgame"; 
		SqlParameterSource param = new MapSqlParameterSource("idgame", inputGame.getGameID());
		List<TeeTime> teeTimeList = null; 
		
		StringBuffer sb = new StringBuffer();
		
		for (int i = 0; i < teeTimeList.size(); i++) 
		{
			TeeTime teeTime = teeTimeList.get(i);
			sb.append(teeTime.getTeeTimeString() + " ");
		}
		
		sb.append(NEWLINE);
		
		return sb.toString();
	}

	private ArrayList<String> establishEmailRecipients() 
	{		
		JdbcTemplate jdbcTemplate = new JdbcTemplate(Utils.getDatasourceProperties());		
		String sql = "select * from player order by lastName, firstName";		 
		List<Player> playerList = null; 
		log.info("LoggedDBOperation: function-inquiry; table:player; rows:" + playerList.size());
		ArrayList<String> emailRecips = Utils.setEmailFullRecipientList(playerList);		
		
		/*
		ArrayList<String> emailRecips = new ArrayList<>();
		emailRecips.add("paulslomkowski@yahoo.com");
		*/
		
		return emailRecips;
	}
	
	private List<Game> getFutureGames()
	{
		JdbcTemplate jdbcTemplate = new JdbcTemplate(Utils.getDatasourceProperties());
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Utils.getDatasourceProperties());
		
		List<Game> tempList = new ArrayList<>();
		
		String sql = "select * from game order by gameDate";		 
		List<Game> gameList = null; 
		log.info("LoggedDBOperation: function-inquiry; table:game; rows:" + gameList.size());
		
		Date today = new Date();
		LocalDate localDateToday = today.toInstant().atZone(ZoneId.of("America/New_York")).toLocalDate();

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
		
	    for (int i = 0; i < tempList.size(); i++) 
		{
			Game tempGame = tempList.get(i);
			
			String coursesql = "select * from golfcourse where idgolfCourse = :courseID";			 
			SqlParameterSource param = new MapSqlParameterSource("courseID", tempGame.getCourseID());			 
			Course tempCourse = null; 
			log.info("LoggedDBOperation: function-inquiry; table:course; rows:1");
			tempGame.setCourse(tempCourse);
			tempGame.setCourseName(tempGame.getCourse().getCourseName());		
		}
        
		return tempList;
	}
	private String getGameParticipants(Game inputGame) 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("Current list of players for this game:");
		sb.append(NEWLINE);
		
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Utils.getDatasourceProperties());
		String sql = "SELECT concat(p.firstName, ' ', p.lastName) as playerName, r.dSignUpdatetime from round r inner join player p "
				+ "where r.idplayer = p.idplayer and r.idgame = :idgame order by r.dSignupDateTime";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", inputGame.getGameID());
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
		signupSDF.setTimeZone(etTimeZone);
		
		List<String> roundPlayers = namedParameterJdbcTemplate.query(sql, param, new ResultSetExtractor<List<String>>() 
		{	   
			@Override
		    public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<String> participantsList = new ArrayList<>();
				
				while (rs.next()) 
				{
					if (rs.getTimestamp("dSignUpdatetime") == null)
					{
						
					}
					else
					{
						String signupDateTime = signupSDF.format(rs.getTimestamp("dSignUpdatetime"));
						participantsList.add(rs.getString("playerName") + " (signed up: " + signupDateTime + ")");
					}
				}
				
				log.info("LoggedDBOperation: function-inquiry; table:game Participants; rows:" + participantsList.size());
				
				return participantsList;
		    }
		});	
		
		
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

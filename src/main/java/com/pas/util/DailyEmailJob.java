package com.pas.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import com.pas.beans.Course;
import com.pas.beans.Game;
import com.pas.beans.Player;
import com.pas.dao.CourseRowMapper;
import com.pas.dao.GameRowMapper;
import com.pas.dao.PlayerRowMapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class DailyEmailJob implements Runnable 
{
	private static Logger log = LogManager.getLogger(DailyEmailJob.class);
	
	private static long TEN_HOURS = 36000000; //in milliseconds
	private static long SIX_DAYS = 518400000; //in milliseconds
	
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
			log.error("Exception encountered in DailyEmailJob: " + e.getMessage());
			e.printStackTrace();
		}		
   
	}

	private void sendFutureGameEmail(Game inputGame) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");	
		
		String gameDateStr = sdf.format(inputGame.getGameDate());	
		log.info("I've got a game with date = " + gameDateStr + " that I will set up email for.");
		
		String subjectLine = "TMG game on " + Utils.getDayofWeekString(inputGame.getGameDate()) + " " + sdf.format(inputGame.getGameDate()) + " on " + inputGame.getCourseName();
		log.info("establishing email recipients");
		
		List<String> adminUsers = inputGame.getAdminUsers();
		//ArrayList<String> emailRecipients = establishAdminOnlyEmailRecipients(adminUsers); //just admins when testing		
		ArrayList<String> emailRecipients = establishEmailRecipients();
		
		log.info("email recipients successfully established");
		SAMailUtility.sendEmail(subjectLine, inputGame.getFutureGameEmailMessage(), emailRecipients, false); //last false parameter means do not use jsf
		log.info("email successfully sent");
	}

	private ArrayList<String> establishEmailRecipients() 
	{
		JdbcTemplate jdbcTemplate = new JdbcTemplate(Utils.getDatasourceProperties());		
		String sql = "select * from player order by lastName, firstName";		 
		List<Player> playerList = jdbcTemplate.query(sql, new PlayerRowMapper()); 	
		ArrayList<String> emailRecips = Utils.setEmailFullRecipientList(playerList);
		return emailRecips;
	}
	
	private ArrayList<String> establishAdminOnlyEmailRecipients(List<String> adminUsers) 
	{
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Utils.getDatasourceProperties());	
		
		ArrayList<String> emailRecips = new ArrayList<>();
		
		for (int i = 0; i < adminUsers.size(); i++) 
		{
			String playerUserName = adminUsers.get(i);
			String sql = "select * from player where username = :username";		 
			SqlParameterSource param = new MapSqlParameterSource("username", playerUserName);		 
			Player player = namedParameterJdbcTemplate.queryForObject(sql, param, new PlayerRowMapper());	
			String emailAddr = player.getEmailAddress();
			emailRecips.add(emailAddr);
		}
	
		return emailRecips;
	}
	
	private List<Game> getFutureGames()
	{
		JdbcTemplate jdbcTemplate = new JdbcTemplate(Utils.getDatasourceProperties());
		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(Utils.getDatasourceProperties());
		
		List<Game> tempList = new ArrayList<>();
		
		String sql = "select * from game order by gameDate";		 
		List<Game> gameList = jdbcTemplate.query(sql, new GameRowMapper()); 
		
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
			Course tempCourse = namedParameterJdbcTemplate.queryForObject(coursesql, param, new CourseRowMapper()); 	    
			tempGame.setCourse(tempCourse);
			tempGame.setCourseName(tempGame.getCourse().getCourseName());		
		}
        
		return tempList;
	}
	
}

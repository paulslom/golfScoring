package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.model.SelectItem;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.pas.beans.Course;
import com.pas.beans.CourseTee;
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.PlayerTeePreference;
import com.pas.beans.Round;
import com.pas.util.BeanUtilJSF;
import com.pas.util.Utils;

@Repository
public class GameDAO extends JdbcDaoSupport implements Serializable 
{	
	private static final long serialVersionUID = 1L;
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	private static Logger log = LogManager.getLogger(GameDAO.class);
	
	private List<Game> fullGameList = new ArrayList<Game>();
		
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       setDataSource(dataSource);	      
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing DAO: {}" +  ex.getStackTrace());
	   }
	}

	@Autowired
    public GameDAO(DataSource dataSource) 
	{
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.dataSource = dataSource;	
    }	
	
	public int addGame(Game game)
	{
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				String insertStr = " INSERT INTO game (idgolfcourse, gameDate, betAmount, teamBallValue, teamBalls, individualLowGrossPrize, ";
				insertStr = insertStr + "individualLowNetPrize, purseAmount, skinsPot, teamPot, fieldSize, totalPlayers, totalTeams, gameNoteForEmail, playTheBallMethod) ";
				insertStr = insertStr + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			
				PreparedStatement psmt = connection.prepareStatement(insertStr, new String[] { "idgame" });
				psmt.setInt(1, game.getCourseID());
				
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String gameDateTime = format.format(game.getGameDate());
				psmt.setString(2, gameDateTime);
				
				psmt.setBigDecimal(3, game.getBetAmount());
				psmt.setBigDecimal(4, game.getEachBallWorth());
				psmt.setInt(5, game.getHowManyBalls());
				psmt.setBigDecimal(6, game.getIndividualGrossPrize());
				psmt.setBigDecimal(7, game.getIndividualNetPrize());
				psmt.setBigDecimal(8, game.getPurseAmount());
				psmt.setBigDecimal(9, game.getSkinsPot());
				psmt.setBigDecimal(10, game.getTeamPot());
				psmt.setInt(11, game.getFieldSize());
				psmt.setInt(12, game.getTotalPlayers());
				psmt.setInt(13, game.getTotalTeams());
				psmt.setString(14, game.getGameNoteForEmail());
				psmt.setString(15, game.getPlayTheBallMethod());
				return psmt;
			}
		}, keyHolder);
 
		log.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		game.setGameID(keyHolder.getKey().intValue());
		refreshGameList("add", game.getGameID(), game);
		
		return keyHolder.getKey().intValue();	
	}
	
	public void updateGame(Game game)
	{
		String updateStr = " UPDATE game SET idgolfcourse = ?, gameDate = ?, betAmount = ?," ;
			updateStr = updateStr + " teamBallValue = ?, teamBalls =  ?, individualLowGrossPrize = ?," ;
			updateStr = updateStr + " individualLowNetPrize = ?, purseAmount = ?, skinsPot = ?," ;
			updateStr = updateStr + " teamPot = ?, fieldSize = ?, totalPlayers = ?, totalTeams = ?, gameNoteForEmail = ?, ";
			updateStr = updateStr + "playTheBallMethod = ?, closedForSignups = ? WHERE idgame = ?";	
		
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String gameDateTime = format.format(game.getGameDate());
		
		jdbcTemplate.update(updateStr, game.getCourseID(), gameDateTime, game.getBetAmount(), game.getEachBallWorth(), game.getHowManyBalls(), 
				game.getIndividualGrossPrize(), game.getIndividualNetPrize(), game.getPurseAmount(), game.getSkinsPot(), game.getTeamPot(),
			    game.getFieldSize(), game.getTotalPlayers(), game.getTotalTeams(), game.getGameNoteForEmail(),
			    game.getPlayTheBallMethod(), game.isGameClosedForSignups(), game.getGameID());
		
		log.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		refreshGameList("update", game.getGameID(), game);	
       		
		log.debug(getTempUserName() + " update game table complete");		
	}
	
	public void deleteGame(int gameID) 
	{		
		String deleteStr = "delete from game where idgame = ?";
		jdbcTemplate.update(deleteStr,gameID);	
		
		log.info("LoggedDBOperation: function-update; table:game; rows:1");
		
		refreshGameList("delete", gameID, null);		
		
		log.info(getTempUserName() + " deleteGame complete");	
	}
	
	public void readGamesFromDB() 
    {
		String sql = "select * from game  where gameDate > :gameDate order by gameDate desc";	
		SqlParameterSource param = new MapSqlParameterSource("gameDate", Utils.getOneMonthAgoDate());
		this.setFullGameList(namedParameterJdbcTemplate.query(sql, param, new GameRowMapper())); 
		
		log.info("LoggedDBOperation: function-inquiry; table:game; rows:" + this.getFullGameList().size());
	}
	
	public List<Game> getAvailableGames(int playerID) 
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
    	
    	assignCourseToGameList(gameList);
    	
    	return gameList;
	}
	
	public Integer getTeePreference(int playerID, int courseID) 
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
		
		assignCourseToGameList(gameList);
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
		
		assignCourseToGameList(gameList);
    	return gameList;
	}
	
	public Game getGameByGameID(int gameID) 
    {
		Map<Integer, Game> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
		Game game = fullGameMap.get(gameID);
		assignCourseToGame(game);
    	return game;		
	}

	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}
	
	private void assignCourseToGameList(List<Game> gameList)
	{
		for (int i = 0; i < gameList.size(); i++) 
		{
			Game tempGame = gameList.get(i);
			assignCourseToGame(tempGame);
		}
	}
	
	private void assignCourseToGame(Game inGame)
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		CourseTee courseTee = BeanUtilJSF.getBean("pc_CourseTee");	
		
		inGame.setCourse(golfmain.getCoursesMap().get(inGame.getCourseID()));
		inGame.setCourseName(inGame.getCourse().getCourseName());
				
		Map<Integer, List<SelectItem>> teeSelectionsMap = courseTee.getTeeSelectionsMap();
		List<SelectItem> courseTeeSelections = teeSelectionsMap.get(inGame.getCourseID());	
		inGame.setTeeSelections(courseTeeSelections);
	}
	
	private void refreshGameList(String function, int gameID, Game inputgame)
	{		
		if (function.equalsIgnoreCase("add"))
		{
			Game gm = new Game();
			
			gm.setGameID(inputgame.getGameID());
			gm.setCourseName(inputgame.getCourseName());
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
			
			this.getFullGameList().add(gm);
		}
		else
		{
			Map<Integer, Game> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
			
			if (function.equalsIgnoreCase("delete"))
			{
				fullGameMap.remove(gameID);			
			}
			else if (function.equalsIgnoreCase("update"))
			{
				fullGameMap.remove(inputgame.getGameID());
				fullGameMap.put(inputgame.getGameID(), inputgame);
			}
			
			this.getFullGameList().clear();
			Collection<Game> values = fullGameMap.values();
			this.setFullGameList(new ArrayList<>(values));
		}		
		
		Collections.sort(this.getFullGameList(), new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o2.getGameDate().compareTo(o1.getGameDate());
		   }
		});
		
		/* for debugging purposes */
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			log.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		
		
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

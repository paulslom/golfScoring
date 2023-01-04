package com.pas.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	private RoundDAO roundDAO;
	private PlayerTeePreferenceDAO playerTeePreferenceDAO;
	HashMap<Integer,Game> gamesMap = new HashMap<Integer,Game>();
		
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
    public GameDAO(DataSource dataSource, RoundDAO roundDAO, PlayerTeePreferenceDAO playerTeePreferenceDAO) 
	{
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	
	    this.roundDAO = roundDAO;
	    this.playerTeePreferenceDAO = playerTeePreferenceDAO;
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
			
		log.debug(getTempUserName() + " update game table complete");		
	}
	
	public void deleteGame(int gameID) 
	{		
		String deleteStr = "delete from game where idgame = ?";
		jdbcTemplate.update(deleteStr,gameID);	
		log.info(getTempUserName() + " deleteGame complete");	
	}
	
	public List<Game> readGamesFromDB() 
    {
		String sql = "select * from game order by gameDate desc";		 
		List<Game> gameList = jdbcTemplate.query(sql, new GameRowMapper()); 
		
		for (int i = 0; i < gameList.size(); i++) 
		{
			Game tempGame = gameList.get(i);
			gamesMap.put(i, tempGame);				
		}
		    	
    	return gameList;
	}
	
	public List<Game> readAvailableGamesFromDB(int playerID) 
    {
		String sql = "select * from game where gameDate >= CURDATE() order by gameDate";
		 
		List<Game> gameList = jdbcTemplate.query(sql, new GameRowMapper()); 
	
    	for (int i = 0; i < gameList.size(); i++) 
    	{
			Game gm = gameList.get(i);
			Round rd = roundDAO.readRoundFromDBByGameandPlayer(gm.getGameID(), playerID);
			
			Integer spotsTaken = roundDAO.countRoundsForGameFromDB(gm);
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
		PlayerTeePreference ptp = playerTeePreferenceDAO.readPlayerTeePreferenceTeeFromDB(playerID, courseID);
		if (ptp != null)
		{
			return ptp.getCourseTeeID();
		}
		return null;
	}

	public List<Game> readFutureGamesFromDB() 
    {
		String sql = "select * from game where gameDate >= CURDATE() order by gameDate";		 
		List<Game> gameList = jdbcTemplate.query(sql, new GameRowMapper()); 
		assignCourseToGameList(gameList);
    	return gameList;
	}
	
	public List<Game> readAvailableGamesByPlayerID(int playerID) 
    {
		String sql = "select * from game where idgame in (select g.idgame from game g inner join round r on r.idgame = g.idgame" + 
				" where g.gameDate >= CURDATE() and r.idplayer = :idplayer) order by gameDate";			
		SqlParameterSource param = new MapSqlParameterSource("idplayer",playerID);		 
		List<Game> gameList = namedParameterJdbcTemplate.query(sql, param, new GameRowMapper()); 
		assignCourseToGameList(gameList);
    	return gameList;
	}
	
	public Game readGameFromDB(int gameID) 
    {
		String sql = "select * from game where idgame = :idgame";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", gameID);		 
		Game game = namedParameterJdbcTemplate.queryForObject(sql, param, new GameRowMapper()); 
		assignCourseToGame(game);
    	return game;		
	}

	public HashMap<Integer, Game> getGamesMap() {
		return gamesMap;
	}

	public void setGamesMap(HashMap<Integer, Game> gamesMap) {
		this.gamesMap = gamesMap;
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
	
}

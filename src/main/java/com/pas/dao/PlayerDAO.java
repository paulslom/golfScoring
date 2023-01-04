package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.pas.beans.Player;

@Repository
public class PlayerDAO extends JdbcDaoSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	private static Logger log = LogManager.getLogger(GameDAO.class);
	private HashMap<Integer,Player> playersMap = new HashMap<Integer, Player>(); 
	
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
    public PlayerDAO(DataSource dataSource) 
	{
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
	
	public void addPlayer(Player player)
	{
		String insertStr = " INSERT INTO player (firstName, lastName, currentHandicapIndex, emailAddress, username) values(?,?,?,?,?)";			
		jdbcTemplate.update(insertStr, new Object[] {player.getFirstName(), player.getLastName(), player.getHandicap(), player.getEmailAddress(), player.getUsername()});	
		log.info("addPlayer complete");		
	}
	
	public void updatePlayerHandicap(int playerID, BigDecimal handicap) 
	{
		String updateStr = " UPDATE player SET ";				
		updateStr = updateStr + " currentHandicapIndex = ?";			
		updateStr = updateStr + " WHERE idplayer = ?";
	
		jdbcTemplate.update(updateStr, handicap, playerID);
		
		log.debug("update player handicap for playerID: " + playerID + " to: " + handicap + " on player table complete");		
	}
	
	public void updatePlayer(Player player)
	{
		String updateStr = " UPDATE player SET ";				
		updateStr = updateStr + " firstName = ?," ;
		updateStr = updateStr + " lastName = ?," ;
		updateStr = updateStr + " currentHandicapIndex = ?," ;
		updateStr = updateStr + " username = ?," ;
		updateStr = updateStr + " emailAddress = ?";			
		updateStr = updateStr + " WHERE idplayer = ?";
	
		jdbcTemplate.update(updateStr, player.getFirstName(), player.getLastName(), player.getHandicap(), player.getUsername(), player.getEmailAddress(), player.getPlayerID());
		
		log.debug("update player table complete");		
	}
	
	public List<Player> readPlayersFromDB() 
    {
		String sql = "select * from player order by lastName, firstName";		 
		List<Player> playerList = jdbcTemplate.query(sql, new PlayerRowMapper()); 
				
		//we need this for the PlayerConverter class		
		for (int i = 0; i < playerList.size(); i++) 
		{
			playersMap.put(i, playerList.get(i));
		}
		    	
    	return playerList;
	}
	
	public Player readPlayerFromDB(int playerID) 
    {		
		String sql = "select * from player where idplayer = :playerID";		 
		SqlParameterSource param = new MapSqlParameterSource("playerID", playerID);		 
		Player player = namedParameterJdbcTemplate.queryForObject(sql, param, new PlayerRowMapper());		
    	
    	return player;		
	}
	
	public Player readPlayerFromDB(String username) 
    {
		String sql = "select * from player where username = :username";		 
		SqlParameterSource param = new MapSqlParameterSource("username", username);		 
		Player player = namedParameterJdbcTemplate.queryForObject(sql, param, new PlayerRowMapper());		
    	
    	return player;
	}

	public HashMap<Integer, Player> getPlayersMap() {
		return playersMap;
	}

	public void setPlayersMap(HashMap<Integer, Player> playersMap) {
		this.playersMap = playersMap;
	}
	

}

package com.pas.dao;

import java.io.Serializable;
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

import com.pas.beans.Game;
import com.pas.beans.Player;
import com.pas.beans.PlayerMoney;

@Repository
public class PlayerMoneyDAO extends JdbcDaoSupport implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(PlayerMoneyDAO.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
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
    public PlayerMoneyDAO(DataSource dataSource) 
	{
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
		
	public List<PlayerMoney> readPlayerMoneyFromDB(Player player)
    {
		String sql = "select * from playermoney where idplayer = :idplayer";		 
		SqlParameterSource param = new MapSqlParameterSource("idplayer", player.getPlayerID());		 
		List<PlayerMoney> playerMoneyList = namedParameterJdbcTemplate.query(sql, param, new PlayerMoneyRowMapper()); 	
    	
    	return playerMoneyList;
    }
	
	public List<PlayerMoney> readPlayerMoneyFromDB(Game game)
    {
		String sql = "select * from playermoney where idgame = :idgame";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", game.getGameID());		 
		List<PlayerMoney> playerMoneyList = namedParameterJdbcTemplate.query(sql, param, new PlayerMoneyRowMapper()); 	
    	
    	return playerMoneyList;
    }
	
	public List<PlayerMoney> readPlayerMoneyFromDB()
    {
		String sql = "select * from playermoney";		 
		List<PlayerMoney> playerMoneyList = jdbcTemplate.query(sql, new PlayerMoneyRowMapper());     	
    	return playerMoneyList;
    }
	
	public List<PlayerMoney> readPlayerMoneyFromDB(Integer gameID, Integer playerID)
    {
		String sql = "select * from playermoney where idgame = :idgame  and idplayer = :idPlayer";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", gameID).addValue("idPlayer", playerID);		
		List<PlayerMoney> playerMoneyList = namedParameterJdbcTemplate.query(sql, param, new PlayerMoneyRowMapper()); 	
    
    	return playerMoneyList;
    }

	public void addPlayerMoney(PlayerMoney playerMoney)
	{
		String insertStr = "INSERT INTO playermoney (idgame, idplayer, description, amount) values(?,?,?,?)";
		jdbcTemplate.update(insertStr, new Object[] {playerMoney.getGameID(), playerMoney.getPlayerID(), playerMoney.getDescription(), playerMoney.getAmount()});	
		log.info("addPlayerMoney complete");	
	}
	
	public void updatePlayerMoney(PlayerMoney playerMoney)
	{
		String updateStr = " UPDATE playermoney SET idgame = ?, idplayer = ?, description = ?, amount = ? WHERE idplayerMoney = ?";	
		jdbcTemplate.update(updateStr, new Object[] {playerMoney.getGameID(), playerMoney.getPlayerID(), playerMoney.getDescription(), playerMoney.getAmount(), playerMoney.getPlayerMoneyID()});	 			
		log.info("updatePlayerMoney complete");		
	}
	
	//deletes all player money rows from the db for this game
	public void deletePlayerMoneyFromDB(Integer gameID)
    {
		String deleteStr = "delete from playermoney where idgame = ?";
		jdbcTemplate.update(deleteStr, gameID);	
		log.info("deletePlayerMoneyFromDB complete");    	
    }	

}

package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import com.pas.util.Utils;

@Repository
public class PlayerMoneyDAO extends JdbcDaoSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(PlayerMoneyDAO.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
	private List<PlayerMoney> playerMoneyList = new ArrayList<>();
	private Map<Integer,PlayerMoney> playerMoneyMap = new HashMap<Integer,PlayerMoney>();
	
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
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
		
	public List<PlayerMoney> getPlayerMoneyByPlayer(Player player)
    {
		List<PlayerMoney> playerMoneyListByPlayer = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getPlayerID() == player.getPlayerID())
			{
				playerMoneyListByPlayer.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByPlayer;
    }
	
	public List<PlayerMoney> getPlayerMoneyByGame(Game game)
    {
		List<PlayerMoney> playerMoneyListByGame = new ArrayList<>();
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getGameID() == game.getGameID())
			{
				playerMoneyListByGame.add(playerMoney);
			}
		}		
	    	
    	return playerMoneyListByGame;
    }
	
	public void readPlayerMoneyFromDB()
    {
		String sql = "select pm.*, game.gameDate from playermoney pm inner join game on pm.idgame = game.idgame where game.gameDate > :gameDate order by game.gameDate";
		SqlParameterSource param = new MapSqlParameterSource("gameDate", Utils.getLastYearsLastDayDate());
		this.setPlayerMoneyList(namedParameterJdbcTemplate.query(sql, param, new PlayerMoneyRowMapper()));  
		
		this.setPlayerMoneyMap(this.getPlayerMoneyList().stream().collect(Collectors.toMap(PlayerMoney::getPlayerMoneyID, gm -> gm)));		
		log.info("LoggedDBOperation: function-inquiry; table:playermoney; rows:" + playerMoneyList.size());		
    }
	
	public void addPlayerMoney(PlayerMoney playerMoney)
	{
		String insertStr = "INSERT INTO playermoney (idgame, idplayer, description, amount) values(?,?,?,?)";
		jdbcTemplate.update(insertStr, new Object[] {playerMoney.getGameID(), playerMoney.getPlayerID(), playerMoney.getDescription(), playerMoney.getAmount()});
		log.info("LoggedDBOperation: function-add; table:playermoney; rows:1");
		
		this.getPlayerMoneyList().add(playerMoney);
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		
		log.info("addPlayerMoney complete");	
	}
	
	public void updatePlayerMoney(PlayerMoney playerMoney)
	{
		String updateStr = " UPDATE playermoney SET idgame = ?, idplayer = ?, description = ?, amount = ? WHERE idplayerMoney = ?";	
		jdbcTemplate.update(updateStr, new Object[] {playerMoney.getGameID(), playerMoney.getPlayerID(), playerMoney.getDescription(), playerMoney.getAmount(), playerMoney.getPlayerMoneyID()});	
		log.info("LoggedDBOperation: function-update; table:playermoney; rows:1");
			
		this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
		this.getPlayerMoneyMap().put(playerMoney.getPlayerMoneyID(), playerMoney);
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		log.info("updatePlayerMoney complete");		
	}
	
	//deletes all player money rows from the db for this game
	public void deletePlayerMoneyFromDB(Integer gameID)
    {
		String deleteStr = "delete from playermoney where idgame = ?";
		jdbcTemplate.update(deleteStr, gameID);	
		log.info("LoggedDBOperation: function-delete; table:playermoney; rows:1");
		
		for (int i = 0; i < playerMoneyList.size(); i++)
		{
			PlayerMoney playerMoney = playerMoneyList.get(i);
			
			if (playerMoney.getGameID() == gameID)
			{
				this.getPlayerMoneyMap().remove(playerMoney.getPlayerMoneyID());
			}
		}		
		
		this.getPlayerMoneyList().clear();
		Collection<PlayerMoney> values = this.getPlayerMoneyMap().values();
		this.setPlayerMoneyList(new ArrayList<>(values));
		
		log.info("deletePlayerMoneyFromDB complete");    	
    }

	public List<PlayerMoney> getPlayerMoneyList() 
	{
		return playerMoneyList;
	}

	public void setPlayerMoneyList(List<PlayerMoney> playerMoneyList) 
	{
		this.playerMoneyList = playerMoneyList;
	}

	public Map<Integer, PlayerMoney> getPlayerMoneyMap() {
		return playerMoneyMap;
	}

	public void setPlayerMoneyMap(Map<Integer, PlayerMoney> playerMoneyMap) {
		this.playerMoneyMap = playerMoneyMap;
	}	

}

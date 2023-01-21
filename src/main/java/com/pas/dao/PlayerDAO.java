package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.pas.beans.Player;

@Repository
public class PlayerDAO extends JdbcDaoSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	private static Logger log = LogManager.getLogger(PlayerDAO.class);
	
	private Map<Integer,Player> fullPlayersMapByPlayerID = new HashMap<Integer, Player>(); 
	private Map<String,Player> fullPlayersMapByUserName = new HashMap<String, Player>(); 
	private List<Player> fullPlayerList = new ArrayList<Player>();	
	
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
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
	
	public int addPlayer(Player player)
	{
		String insertStr = " INSERT INTO player (firstName, lastName, currentHandicapIndex, emailAddress, username) values(?,?,?,?,?)";	
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
			
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				PreparedStatement psmt = connection.prepareStatement(insertStr, new String[] { "idplayer" });
				psmt.setString(1, player.getFirstName());
				psmt.setString(2, player.getLastName());
				psmt.setBigDecimal(3, player.getHandicap());
				psmt.setString(4, player.getEmailAddress());
				psmt.setString(5, player.getUsername());
				return psmt;
			}
		}, keyHolder);
 
		log.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
		
		player.setPlayerID(keyHolder.getKey().intValue());
		
		refreshListsAndMaps("add", player);	
				
		log.info("addPlayer complete");		
		
		return player.getPlayerID(); //this is the key that was just added
	}
	
	public void updatePlayerHandicap(int playerID, BigDecimal handicap) 
	{
		String updateStr = " UPDATE player SET ";				
		updateStr = updateStr + " currentHandicapIndex = ?";			
		updateStr = updateStr + " WHERE idplayer = ?";
	
		jdbcTemplate.update(updateStr, handicap, playerID);
		log.info("LoggedDBOperation: function-update; table:player; rows:1");
		
		Player player = this.getFullPlayersMapByPlayerID().get(playerID);
		player.setHandicap(handicap);
		
		
				
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
		
		log.info("LoggedDBOperation: function-update; table:player; rows:1");
		
		refreshListsAndMaps("update", player);	
		
		log.debug("update player table complete");		
	}
	
	public void readPlayersFromDB() 
    {
		String sql = "select * from player order by lastName, firstName";		 
		this.setFullPlayerList(jdbcTemplate.query(sql, new PlayerRowMapper())); 
		
		log.info("LoggedDBOperation: function-inquiry; table:player; rows:" + this.getFullPlayerList().size());
		
		this.setFullPlayersMapByPlayerID(this.getFullPlayerList().stream().collect(Collectors.toMap(Player::getPlayerID, ply -> ply)));
		this.setFullPlayersMapByUserName(this.getFullPlayerList().stream().collect(Collectors.toMap(Player::getUsername, ply -> ply)));	
		
		Collections.sort(this.getFullPlayerList(), new Comparator<Player>() 
		{
		   public int compare(Player o1, Player o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
	}
	
	private void refreshListsAndMaps(String function, Player player)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getFullPlayersMapByPlayerID().remove(player.getPlayerID());	
			this.getFullPlayersMapByUserName().remove(player.getUsername());		
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getFullPlayersMapByPlayerID().put(player.getPlayerID(), player);	
			this.getFullPlayersMapByUserName().put(player.getUsername(), player);			
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullPlayersMapByPlayerID().remove(player.getPlayerID());	
			this.getFullPlayersMapByUserName().remove(player.getUsername());		
			this.getFullPlayersMapByPlayerID().put(player.getPlayerID(), player);	
			this.getFullPlayersMapByUserName().put(player.getUsername(), player);		
		}
		
		this.getFullPlayerList().clear();
		Collection<Player> values = this.getFullPlayersMapByUserName().values();
		this.setFullPlayerList(new ArrayList<>(values));
		
		Collections.sort(this.getFullPlayerList(), new Comparator<Player>() 
		{
		   public int compare(Player o1, Player o2) 
		   {
		      return o1.getLastName().compareTo(o2.getLastName());
		   }
		});
		
	}
	
	public List<Player> getFullPlayerList() 
	{
		return fullPlayerList;
	}

	public void setFullPlayerList(List<Player> fullPlayerList) 
	{
		this.fullPlayerList = fullPlayerList;
	}

	public Map<Integer, Player> getFullPlayersMapByPlayerID() 
	{
		return fullPlayersMapByPlayerID;
	}

	public void setFullPlayersMapByPlayerID(Map<Integer, Player> fullPlayersMapByPlayerID) 
	{
		this.fullPlayersMapByPlayerID = fullPlayersMapByPlayerID;
	}

	public Map<String, Player> getFullPlayersMapByUserName() 
	{
		return fullPlayersMapByUserName;
	}

	public void setFullPlayersMapByUserName(Map<String, Player> fullPlayersMapByUserName) 
	{
		this.fullPlayersMapByUserName = fullPlayersMapByUserName;
	}
	

}

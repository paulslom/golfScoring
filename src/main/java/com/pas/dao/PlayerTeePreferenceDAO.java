package com.pas.dao;

import java.io.Serializable;
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
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.pas.beans.Group;
import com.pas.beans.PlayerTeePreference;
 
@Repository
public class PlayerTeePreferenceDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(PlayerTeePreferenceDAO.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
	private Map<Integer,PlayerTeePreference> playerTeePreferencesMap = new HashMap<Integer,PlayerTeePreference>();
	private List<PlayerTeePreference> playerTeePreferencesList = new ArrayList<>();
	
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
    public PlayerTeePreferenceDAO(DataSource dataSource) 
	{
		 this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
		 this.jdbcTemplate = new JdbcTemplate(dataSource);
	     this.dataSource = dataSource;
    }

	public List<PlayerTeePreference> readPlayerTeePreferencesFromDB(Group grp)
    {
		String sql = "select pt.* from playertees pt inner join golfcourse gc on gc.idgolfcourse = pt.idgolfCourse where gc.idgroup = :groupID";		 
		SqlParameterSource param = new MapSqlParameterSource("groupID", grp.getGroupID());		 
		this.setPlayerTeePreferencesList(namedParameterJdbcTemplate.query(sql, param, new PlayerTeePreferenceRowMapper())); 
		
		log.info("LoggedDBOperation: function-inquiry; table:playerteepreference; rows:" + playerTeePreferencesList.size());
		
		playerTeePreferencesMap = playerTeePreferencesList.stream().collect(Collectors.toMap(PlayerTeePreference::getPlayerTeePreferenceID, PlayerTeePreference -> PlayerTeePreference));
			
		return playerTeePreferencesList;
    }
	
	public PlayerTeePreference getPlayerTeePreference(Integer playerID, Integer courseID)
    {
		PlayerTeePreference playerTeePreference = new PlayerTeePreference();
		for (int i = 0; i < this.getPlayerTeePreferencesList().size(); i++) 
		{
			playerTeePreference = this.getPlayerTeePreferencesList().get(i);
			
			//log.info("ptp id: " + playerTeePreference.getPlayerTeePreferenceID() + " player id: " + playerTeePreference.getPlayerID() + " course tee id: " + playerTeePreference.getCourseTeeID());
			
			if (playerTeePreference.getPlayerID() == playerID && playerTeePreference.getCourseID() == courseID)
			{
				break; //this is the one we want
			}
		}
	
		return playerTeePreference;
    }
	
	public PlayerTeePreference getPlayerTeePreference(Integer playerTeePreferenceID)
    {
		PlayerTeePreference playerTeePreference = this.getPlayerTeePreferencesMap().get(playerTeePreferenceID);				
    	return playerTeePreference;
    }

	public void addPlayerTeePreference(PlayerTeePreference playerTeePreference)
	{
		String insertStr = "INSERT INTO playertees (idplayer, idgolfcourse, idgolfcoursetee) values(?,?,?)";			
		jdbcTemplate.update(insertStr, new Object[] {playerTeePreference.getPlayerID(), playerTeePreference.getCourseID(), playerTeePreference.getCourseTeeID()});	
		log.info("LoggedDBOperation: function-update; table:playerteepreference; rows:1");
		
		refreshListsAndMaps("add",playerTeePreference);
		
		log.info("addPlayerTeePreference complete");	
	}
	
	public void updatePlayerTeePreference(PlayerTeePreference playerTeePreference)
	{
		String updateStr = " UPDATE playertees SET ";				
		updateStr = updateStr + " idplayer = ?," ;
		updateStr = updateStr + " idgolfcourse = ?," ;
		updateStr = updateStr + " idgolfcoursetee = ?"; 		
		updateStr = updateStr + " WHERE idplayertees = ?";
	
		jdbcTemplate.update(updateStr, playerTeePreference.getPlayerID(), playerTeePreference.getCourseID(), playerTeePreference.getCourseTeeID(), playerTeePreference.getPlayerTeePreferenceID());
		log.info("LoggedDBOperation: function-update; table:playerteepreference; rows:1");
		
		refreshListsAndMaps("update", playerTeePreference);
			
		log.debug("update player tee preference table complete");		
	}
	
	private void refreshListsAndMaps(String function, PlayerTeePreference ptp)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getPlayerTeePreferencesMap().remove(ptp.getPlayerTeePreferenceID());			
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getPlayerTeePreferencesMap().put(ptp.getPlayerTeePreferenceID(), ptp);		
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getPlayerTeePreferencesMap().remove(ptp.getPlayerTeePreferenceID());		
			this.getPlayerTeePreferencesMap().put(ptp.getPlayerTeePreferenceID(), ptp);		
		}
		
		this.getPlayerTeePreferencesList().clear();
		Collection<PlayerTeePreference> values = this.getPlayerTeePreferencesMap().values();
		this.setPlayerTeePreferencesList(new ArrayList<>(values));
		
		Collections.sort(this.getPlayerTeePreferencesList(), new Comparator<PlayerTeePreference>() 
		{
		   public int compare(PlayerTeePreference o1, PlayerTeePreference o2) 
		   {
		      return o1.getPlayerFullName().compareTo(o2.getPlayerFullName());
		   }
		});
		
	}
	
	public Map<Integer, PlayerTeePreference> getPlayerTeePreferencesMap() {
		return playerTeePreferencesMap;
	}

	public void setPlayerTeePreferencesMap(Map<Integer, PlayerTeePreference> playerTeePreferencesMap) {
		this.playerTeePreferencesMap = playerTeePreferencesMap;
	}

	public List<PlayerTeePreference> getPlayerTeePreferencesList() {
		return playerTeePreferencesList;
	}

	public void setPlayerTeePreferencesList(List<PlayerTeePreference> playerTeePreferencesList) {
		this.playerTeePreferencesList = playerTeePreferencesList;
	}


	
}

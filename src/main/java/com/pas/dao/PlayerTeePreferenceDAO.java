package com.pas.dao;

import java.io.Serializable;
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
		 
		SqlParameterSource param = new MapSqlParameterSource("groupID", grp.getSelectedGroup().getGroupID());
		 
		List<PlayerTeePreference> playerTeePreferencesList = namedParameterJdbcTemplate.query(sql, param, new PlayerTeePreferenceRowMapper()); 
		
		playerTeePreferencesMap = playerTeePreferencesList.stream().collect(Collectors.toMap(PlayerTeePreference::getPlayerTeePreferenceID, PlayerTeePreference -> PlayerTeePreference));
			
		return playerTeePreferencesList;
    }
	
	public PlayerTeePreference readPlayerTeePreferenceTeeFromDB(Integer playerID, Integer courseID)
    {
		String sql = "select * from playertees where idplayer = :idplayer  and idgolfcourse = :idgolfcourse";		 
		SqlParameterSource param = new MapSqlParameterSource("idplayer", playerID).addValue("idgolfcourse", courseID);	 
		 
		PlayerTeePreference playerTeePreference = namedParameterJdbcTemplate.queryForObject(sql, param, new PlayerTeePreferenceRowMapper()); 	
    	
    	return playerTeePreference;
    }
	
	public PlayerTeePreference readPlayerTeePreferenceTeeFromDB(Integer playerTeePreferenceID)
    {
		String sql = "select * from playertees where idplayertees = :PlayerTeePreferenceID";
		 
		SqlParameterSource param = new MapSqlParameterSource("PlayerTeePreferenceID", playerTeePreferenceID);
		 
		PlayerTeePreference playerTeePreference = namedParameterJdbcTemplate.queryForObject(sql, param, new PlayerTeePreferenceRowMapper()); 	
    	
    	return playerTeePreference;
    }

	public void addPlayerTeePreference(PlayerTeePreference playerTeePreference)
	{
		String insertStr = "INSERT INTO playertees (idplayer, idgolfcourse, idgolfcoursetee) values(?,?,?)";			
		jdbcTemplate.update(insertStr, new Object[] {playerTeePreference.getPlayerID(), playerTeePreference.getCourseID(), playerTeePreference.getCourseTeeID()});	
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
		
		log.debug("update player tee preference table complete");		
	}
	
	public Map<Integer, PlayerTeePreference> getPlayerTeePreferencesMap() {
		return playerTeePreferencesMap;
	}

	public void setPlayerTeePreferencesMap(Map<Integer, PlayerTeePreference> playerTeePreferencesMap) {
		this.playerTeePreferencesMap = playerTeePreferencesMap;
	}


	
}

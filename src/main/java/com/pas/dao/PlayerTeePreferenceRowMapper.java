package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.PlayerTeePreference;

public class PlayerTeePreferenceRowMapper implements RowMapper<PlayerTeePreference>, Serializable
{
    private static final long serialVersionUID = 1L;
    
    //private static Logger log = LogManager.getLogger(PlayerTeePreferenceRowMapper.class);
  
	@Override
    public PlayerTeePreference mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
        PlayerTeePreference playerTeePreference = new PlayerTeePreference();
        
        playerTeePreference.setPlayerTeePreferenceID(rs.getInt("idplayertees"));
        playerTeePreference.setPlayerID(rs.getInt("idplayer"));
        playerTeePreference.setCourseID(rs.getInt("idgolfcourse"));
        playerTeePreference.setCourseTeeID(rs.getInt("idgolfcoursetee"));        
		
        return playerTeePreference;
    }   	
  	
}

package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.model.SelectItem;

import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.Course;
import com.pas.beans.CourseTee;
import com.pas.beans.GolfMain;
import com.pas.beans.Player;
import com.pas.beans.PlayerTeePreference;
import com.pas.util.BeanUtilJSF;

public class PlayerTeePreferenceRowMapper implements RowMapper<PlayerTeePreference>, Serializable
{
    private static final long serialVersionUID = 1L;
    
    private Map<Integer, List<SelectItem>> teeSelectionsMap = new HashMap<>();

	@Override
    public PlayerTeePreference mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
        PlayerTeePreference playerTeePreference = new PlayerTeePreference();
        
        playerTeePreference.setPlayerTeePreferenceID(rs.getInt("idplayertees"));
        playerTeePreference.setPlayerID(rs.getInt("idplayer"));
        playerTeePreference.setCourseID(rs.getInt("idgolfcourse"));
        playerTeePreference.setCourseTeeID(rs.getInt("idgolfcoursetee"));
        
        CourseTee ct = getCourseTee(playerTeePreference.getCourseTeeID());
        playerTeePreference.setTeeColor(ct.getTeeColor());
        
        if (teeSelectionsMap == null || teeSelectionsMap.size() == 0)
		{
        	teeSelectionsMap = ct.getTeeSelectionsMap();
		}
        playerTeePreference.setTeeSelections(teeSelectionsMap.get(playerTeePreference.getCourseID()));
        
        Course cs = getCourse(playerTeePreference.getCourseID());
        playerTeePreference.setCourseName(cs.getCourseName());
        
        Player player = getPlayer(playerTeePreference.getPlayerID());
		playerTeePreference.setPlayerUserName(player.getUsername());
		playerTeePreference.setPlayerFullName(player.getFullName());
		
		
		
        return playerTeePreference;
    }
	
	private CourseTee getCourseTee(int courseTeeID) 
  	{
      	GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
      	CourseTee courseTee = golfmain.getCourseTeesMap().get(courseTeeID);
  		return courseTee;
  	}
	
	private Course getCourse(int courseID) 
  	{
      	GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
      	Course course = golfmain.getCoursesMap().get(courseID);
  		return course;
  	}
	
	private Player getPlayer(int playerID) 
  	{
  		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
  		Player player = golfmain.getFullPlayerMap().get(playerID);
  		return player;
  	}
}

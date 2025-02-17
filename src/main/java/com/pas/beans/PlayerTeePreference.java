package com.pas.beans;

import java.io.Serializable;
import java.util.Objects;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

public class PlayerTeePreference implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
	//private static Logger logger = LogManager.getLogger(PlayerTeePreference.class);
	
	private String playerTeePreferenceID;
	private int oldPlayerTeePreferenceID;
	private String playerID;
	private int oldPlayerID;
	private String playerUserName;
	private String playerFullName;
	private String courseID;
	private int oldCourseID;
	private String courseName;
	private String courseTeeID;
	private int oldCourseTeeID;
	private String teeColor;	
		
	@Override
    public boolean equals(final Object o) 
	{
        if (this == o) 
        {
            return true;
        }
        if (!(o instanceof String)) 
        {
            return false;
        }
        
        final String that = (String) o;
        return Objects.equals(playerTeePreferenceID, that);
    }

	public String getTeeColor() {
		return teeColor;
	}

	public void setTeeColor(String teeColor) {
		this.teeColor = teeColor;
	}

	public String getPlayerUserName() {
		return playerUserName;
	}

	public void setPlayerUserName(String playerUserName) {
		this.playerUserName = playerUserName;
	}

	public String getPlayerFullName() {
		return playerFullName;
	}

	public void setPlayerFullName(String playerFullName) {
		this.playerFullName = playerFullName;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getPlayerTeePreferenceID() {
		return playerTeePreferenceID;
	}

	public void setPlayerTeePreferenceID(String playerTeePreferenceID) {
		this.playerTeePreferenceID = playerTeePreferenceID;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public String getCourseTeeID() {
		return courseTeeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public int getOldPlayerTeePreferenceID() {
		return oldPlayerTeePreferenceID;
	}

	public void setOldPlayerTeePreferenceID(int oldPlayerTeePreferenceID) {
		this.oldPlayerTeePreferenceID = oldPlayerTeePreferenceID;
	}

	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}

	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}
	
	
}

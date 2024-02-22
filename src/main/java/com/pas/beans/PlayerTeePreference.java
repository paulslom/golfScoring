package com.pas.beans;

import java.io.Serializable;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;

import com.pas.util.BeanUtilJSF;

@Named("pc_PlayerTeePreference")
@SessionScoped
public class PlayerTeePreference implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
	private static Logger logger = LogManager.getLogger(PlayerTeePreference.class);
	
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
	
	private boolean disableDialogButton = true;
	
	private PlayerTeePreference selectedPlayerTeePreference;
			
	public String selectRowAjax(SelectEvent<PlayerTeePreference> event)
	{
		logger.info("User clicked on a row in Player Tee Preference list");
		
		PlayerTeePreference item = event.getObject();
		
		this.setSelectedPlayerTeePreference(item);
		this.setDisableDialogButton(false);
				
		return "";
	}	
	
	public String setUpForUpdate()
	{
		this.setPlayerTeePreferenceID(this.getSelectedPlayerTeePreference().getPlayerTeePreferenceID());
		this.setPlayerID(this.getSelectedPlayerTeePreference().getPlayerID());
		this.setCourseTeeID(this.getSelectedPlayerTeePreference().getCourseTeeID());
		this.setCourseID(this.getSelectedPlayerTeePreference().getCourseID());
		this.setPlayerFullName(this.getSelectedPlayerTeePreference().getPlayerFullName());
		this.setCourseName(this.getSelectedPlayerTeePreference().getCourseName());
		this.setTeeColor(this.getSelectedPlayerTeePreference().getTeeColor());	
		
		return "";
	}
	
	public String updatePrefs() throws Exception
	{
		logger.info("entering updatePrefs");
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		
		for (int i = 0; i < gm.getCourseTeesList().size(); i++) 
		{
			CourseTee courseTee = gm.getCourseTeesList().get(i);
			
			if (courseTee.getCourseID().equalsIgnoreCase(this.getCourseID()))
			{
				if (this.getTeeColor().equalsIgnoreCase(courseTee.getTeeColor()))
				{
					this.setCourseTeeID(courseTee.getCourseTeeID());
					break;
				}
			}
		}
		
		gm.updatePlayerTeePreference(this);
		return "";
	}
		
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

	public PlayerTeePreference getSelectedPlayerTeePreference() {
		return selectedPlayerTeePreference;
	}

	public void setSelectedPlayerTeePreference(PlayerTeePreference selectedPlayerTeePreference) {
		this.selectedPlayerTeePreference = selectedPlayerTeePreference;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public boolean isDisableDialogButton() {
		return disableDialogButton;
	}

	public void setDisableDialogButton(boolean disableDialogButton) {
		this.disableDialogButton = disableDialogButton;
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

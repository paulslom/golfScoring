package com.pas.beans;

import java.io.Serializable;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;

@Named("pc_PlayerTeePreference")
@SessionScoped
public class PlayerTeePreference extends SpringBeanAutowiringSupport implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
	private static Logger log = LogManager.getLogger(PlayerTeePreference.class);
	
	private Integer playerTeePreferenceID;
	private Integer playerID;
	private String playerUserName;
	private String playerFullName;
	private Integer courseID;
	private String courseName;
	private Integer courseTeeID;
	private String teeColor;
	
	private boolean disableDialogButton = true;
	
	private PlayerTeePreference selectedPlayerTeePreference;
			
	public String selectRowAjax(SelectEvent<PlayerTeePreference> event)
	{
		log.info("User clicked on a row in Player Tee Preference list");
		
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
	
	public String updatePrefs()
	{
		log.info("entering updatePrefs");
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		
		for (int i = 0; i < gm.getCourseTeesList().size(); i++) 
		{
			CourseTee courseTee = gm.getCourseTeesList().get(i);
			
			if (courseTee.getCourseID() == this.getCourseID())
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
        if (!(o instanceof Integer)) 
        {
            return false;
        }
        
        final Integer that = (Integer) o;
        return Objects.equals(playerTeePreferenceID, that);
    }

	public Integer getPlayerTeePreferenceID() {
		return playerTeePreferenceID;
	}

	public void setPlayerTeePreferenceID(Integer playerTeePreferenceID) {
		this.playerTeePreferenceID = playerTeePreferenceID;
	}

	public Integer getPlayerID() {
		return playerID;
	}

	public void setPlayerID(Integer playerID) {
		this.playerID = playerID;
	}

	public Integer getCourseID() {
		return courseID;
	}

	public void setCourseID(Integer courseID) {
		this.courseID = courseID;
	}

	public Integer getCourseTeeID() {
		return courseTeeID;
	}

	public void setCourseTeeID(Integer courseTeeID) {
		this.courseTeeID = courseTeeID;
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
	
	
}

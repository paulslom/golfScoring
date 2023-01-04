package com.pas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.enterprise.context.RequestScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.dao.PlayerTeePreferenceDAO;
import com.pas.util.BeanUtilJSF;

@Named("pc_PlayerTeePreference")
@RequestScoped
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
	private List<SelectItem> teeSelections = new ArrayList<>();
	
	private PlayerTeePreference selectedPlayerTeePreference;
	
	private List<PlayerTeePreference> fullPlayerTeePreferencesList = new ArrayList<>();
	
	@Autowired PlayerTeePreferenceDAO playerTeePreferenceDAO;
	
	public String selectRowAjax(SelectEvent<PlayerTeePreference> event)
	{
		log.info("User clicked on a row in Player Tee Preference list");
		
		PlayerTeePreference item = event.getObject();
		
		this.setSelectedPlayerTeePreference(item);
		
		return "";
	}	
	
	public String updatePrefs()
	{
		log.info("entering updatePrefs");	
	
		try
		{
			for (int i = 0; i <  getFullPlayerTeePreferencesList().size(); i++) 
			{
				PlayerTeePreference ptp = getFullPlayerTeePreferencesList().get(i);			
				playerTeePreferenceDAO.updatePlayerTeePreference(ptp);			
			}
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Update all tee prefs successful",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (Exception e)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Update all tee prefs unsuccessful: " + e.getMessage(),null);
			FacesContext.getCurrentInstance().addMessage(null, msg);	
		}
		return "";
	}
	
	public String selectGoldAll()
	{
		log.info("entering selectGoldAll");
		
		try
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
			CourseTee courseTee = BeanUtilJSF.getBean("pc_CourseTee");	
			Map<Integer, List<SelectItem>> teeSelectionsMap = courseTee.getTeeSelectionsMap();
			
			for (int i = 0; i <  golfmain.getFullPlayerTeePreferencesList().size(); i++) 
			{
				PlayerTeePreference ptp = golfmain.getFullPlayerTeePreferencesList().get(i);
				List<SelectItem> courseTeeSelections = teeSelectionsMap.get(ptp.getCourseID());
				for (int j = 0; j < courseTeeSelections.size(); j++) 
				{
					SelectItem selItem = courseTeeSelections.get(j);
					if (selItem.getLabel().equalsIgnoreCase("Gold"))
					{
						ptp.setTeeColor("Gold");
						ptp.setCourseTeeID((Integer)selItem.getValue());
						break;
					}
				}
				
			}
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Change to all golds successful",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (Exception e)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Change to all golds unsuccessful: " + e.getMessage(),null);
			FacesContext.getCurrentInstance().addMessage(null, msg);	
		}
	
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

	public List<SelectItem> getTeeSelections() {
		return teeSelections;
	}

	public void setTeeSelections(List<SelectItem> teeSelections) {
		this.teeSelections = teeSelections;
	}

	public List<PlayerTeePreference> getFullPlayerTeePreferencesList() 
	{
		if (fullPlayerTeePreferencesList == null || fullPlayerTeePreferencesList.size() == 0)
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
			fullPlayerTeePreferencesList = golfmain.getFullPlayerTeePreferencesList();
		}
		return fullPlayerTeePreferencesList;
	}

	public void setFullPlayerTeePreferencesList(List<PlayerTeePreference> fullPlayerTeePreferencesList) {
		this.fullPlayerTeePreferencesList = fullPlayerTeePreferencesList;
	}
	
	
}

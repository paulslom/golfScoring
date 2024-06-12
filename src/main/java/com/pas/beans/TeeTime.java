package com.pas.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Named;

@Named("pc_TeeTime")
@Component
@SessionScoped
public class TeeTime implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(TeeTime.class);

	private ArrayList<String> emailRecipients = new ArrayList<String>();
	
	private static String NEWLINE = "<br/>";
	
	private String teeTimeID;
	private int oldTeeTimeID;
	private String gameID;
	private int oldGameID;
	private int playGroupNumber;
	private String teeTimeString;
	
	private Date gameDate;
	
	private String courseName;
	
	private TeeTime selectedTeeTime;
	
	private boolean disableDeleteTeeTime = true;
	
	private String operation = "";
	
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	
	@Autowired private final GolfMain golfmain;
	
	public TeeTime(String teeTimeID, String gameID, int playGroupNumber, String teeTimeString, Date gameDate, String courseName, GolfMain golfmain) 
	{	
		this.golfmain = golfmain;
		this.setTeeTimeID(teeTimeID);
		this.setGameID(gameID);
		this.setPlayGroupNumber(playGroupNumber);
		this.setTeeTimeString(teeTimeString);
		this.setGameDate(gameDate);
		this.setCourseName(courseName);
	}

	@Autowired
	public TeeTime(GolfMain golfmain) 
	{
		this.golfmain = golfmain;
	}
	
	public String selectRowAjax(SelectEvent<TeeTime> event)
	{
		logger.info(getTempUserName() + " User clicked on a row in Tee Time list");
		
		TeeTime item = event.getObject();
		
		this.setSelectedTeeTime(item);
		this.setDisableDeleteTeeTime(false); //if they've picked one, then they can delete it
				
		return "";
	}
	
	public void valueChangeGame(AjaxBehaviorEvent event) 
	{
		logger.info(getTempUserName() + " picked a game on tee times form");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		String selectedOption = (String)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			Game game = new Game(golfmain);
			game.setGameID(selectedOption);
			this.getTeeTimeList().clear();
			this.setTeeTimeList(golfmain.getGameSpecificTeeTimes(game));
		}
						
	}
	
	public String updateTeeTime()
	{
		operation = "Update";
		
		this.setTeeTimeID(this.getSelectedTeeTime().getTeeTimeID());
		this.setGameID(this.getSelectedTeeTime().getGameID());
		this.setPlayGroupNumber(this.getSelectedTeeTime().getPlayGroupNumber());
		this.setTeeTimeString(this.getSelectedTeeTime().getTeeTimeString());
		
		return "";
	}
	
	public String addTeeTime()
	{
		operation = "Add";
		
		this.setTeeTimeString("");
				
		return "";
	}	
	 
	public String deleteSelectedTeeTime()
	{
		logger.info(getTempUserName() + " is deleting a tee time");
		
		try
		{
			TeeTime tt = this.getSelectedTeeTime();
			golfmain.deleteTeeTimeFromDB(this.getSelectedTeeTime().getTeeTimeID());
			
			Game updatedGame = golfmain.getGameByGameID(tt.getGameID());
			
			updatedGame.setFieldSize(updatedGame.getFieldSize() - 4);
			updatedGame.setTotalPlayers(updatedGame.getFieldSize());
			updatedGame.selectTotalPlayers(updatedGame.getTotalPlayers());
			
			golfmain.updateGame(updatedGame);
			
			this.getTeeTimeList().clear();
			this.setTeeTimeList(golfmain.getGameSpecificTeeTimes(updatedGame));
			
			emailAdminsAboutTeeTimeRemoval(updatedGame,tt);
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"tee time successfully removed",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);  
	        
	        logger.info(getTempUserName() + " successfully deleted tee time from game");
		}
		catch (Exception e)
		{
			logger.error("Exception when deleting tee time: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when deleting tee time: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
			
		return "";
	}
	
	public String saveTeeTime()
	{
		logger.info(getTempUserName() + " user clicked Save Tee Time from maintain tee time dialog");	
		
		try
		{
			Game game = golfmain.getGameByGameID(this.getGameID());
			
			if (this.getOperation().equalsIgnoreCase("Add"))
			{
				this.setCourseName(game.getCourseName());
				this.setGameDate(game.getGameDate());
				golfmain.addTeeTime(this);
				game.setFieldSize(game.getFieldSize() + 4);
			}
			
			if (this.getOperation().equalsIgnoreCase("Update"))
			{
				this.setCourseName(this.getSelectedTeeTime().getCourseName());
				this.setGameDate(this.getSelectedTeeTime().getGameDate());
				golfmain.updateTeeTime(this);
			}	
			
			game.setTotalPlayers(game.getFieldSize());
			game.selectTotalPlayers(game.getTotalPlayers());
			
			golfmain.updateGame(game);
			
			this.getTeeTimeList().clear();
			this.setTeeTimeList(golfmain.getGameSpecificTeeTimes(game));
			
			for (int i = 0; i < this.getTeeTimeList().size(); i++) 
			{
				TeeTime tt = this.getTeeTimeList().get(i);
				logger.info("tee time id: " + tt.getTeeTimeID() + ", play group number: " + tt.getPlayGroupNumber() + ", tee time: " + tt.getTeeTimeString());
			}
		}
		catch (Exception e)
		{
			logger.error("Exception when saving tee time: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when saving tee time: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		logger.info(getTempUserName() + " exiting saveTeeTime");
		
		return "";
			
	}
	
	private void emailAdminsAboutTeeTimeRemoval(Game game1, TeeTime teeTime1) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Tee time has been removed";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Tee time removal</H3>");
		
		sb.append(NEWLINE);
		
		String teeTimeStr = teeTime1.getTeeTimeString();
		
		sb.append("<H3>" + teeTimeStr + " tee time removed from Game on " + Utils.getDayofWeekString(game1.getGameDate()) + " " +sdf.format(game1.getGameDate()) + "</H3>");
		
		String messageContent = sb.toString();		
	
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		List<String> adminUsers = golfmain.getAdminUserList();		
		
		//anyone with admin role
		for (int i = 0; i < adminUsers.size(); i++) 
		{
			Player tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(adminUsers.get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
			
		logger.info(getTempUserName() + " emailing tee time removal to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, messageContent, emailRecipients, true); //last param means use jsf		
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
        return Objects.equals(teeTimeID, that);
    }
	
	public int getPlayGroupNumber() {
		return playGroupNumber;
	}
	public void setPlayGroupNumber(int playGroupNumber) {
		this.playGroupNumber = playGroupNumber;
	}
	
	public TeeTime getSelectedTeeTime() {
		return selectedTeeTime;
	}
	public void setSelectedTeeTime(TeeTime selectedTeeTime) {
		this.selectedTeeTime = selectedTeeTime;
	}

	public boolean isDisableDeleteTeeTime() {
		return disableDeleteTeeTime;
	}

	public void setDisableDeleteTeeTime(boolean disableDeleteTeeTime) {
		this.disableDeleteTeeTime = disableDeleteTeeTime;
	}

	public String getTeeTimeString() {
		return teeTimeString;
	}

	public void setTeeTimeString(String teeTimeString) {
		this.teeTimeString = teeTimeString;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}

	public List<TeeTime> getTeeTimeList() {
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) {
		this.teeTimeList = teeTimeList;
	}
	
	public Date getGameDate() 
	{
		return gameDate;
	}

	public void setGameDate(Date gameDate) 
	{
		this.gameDate = gameDate;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getTeeTimeID() {
		return teeTimeID;
	}

	public void setTeeTimeID(String teeTimeID) {
		this.teeTimeID = teeTimeID;
	}

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}
	
}

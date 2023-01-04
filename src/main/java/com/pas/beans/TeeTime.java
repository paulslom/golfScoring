package com.pas.beans;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.dao.GameDAO;
import com.pas.dao.TeeTimeDAO;
import com.pas.dao.UsersAndAuthoritiesDAO;
import com.pas.util.BeanUtilJSF;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

@Named("pc_TeeTime")
@SessionScoped
public class TeeTime extends SpringBeanAutowiringSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(TeeTime.class);

	private ArrayList<String> emailRecipients = new ArrayList<String>();
	
	private static String NEWLINE = "<br/>";
	
	private int teeTimeID;
	private int gameID;
	private int playGroupNumber;
	private String teeTimeString;
	private Game teeTimeGame;
	
	private TeeTime selectedTeeTime;
	
	private boolean disableDeleteTeeTime = true;
	
	private String operation = "";
	
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	
	@Autowired private TeeTimeDAO teeTimeDAO;
	@Autowired private UsersAndAuthoritiesDAO usersAndAuthoritiesDAO;	
	@Autowired private GameDAO gameDAO;
	
	public TeeTime(int teeTimeID, int gameID, int playGroupNumber, String teeTimeString, Game teeTimeGame) 
	{	
		this.setTeeTimeID(teeTimeID);
		this.setGameID(gameID);
		this.setPlayGroupNumber(playGroupNumber);
		this.setTeeTimeString(teeTimeString);
		this.setTeeTimeGame(teeTimeGame);
	}

	public TeeTime() 
	{
	}

	public String selectRowAjax(SelectEvent<TeeTime> event)
	{
		log.info(getTempUserName() + " User clicked on a row in Tee Time list");
		
		TeeTime item = event.getObject();
		
		this.setSelectedTeeTime(item);
		this.setDisableDeleteTeeTime(false); //if they've picked one, then they can delete it
				
		return "";
	}
	
	public void valueChangeGame(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " picked a game on tee times form");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
			this.setTeeTimeGame(selectedOption);
			this.setTeeTimeList(golfmain.getGameSpecificTeeTimes(selectedOption));
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
		log.info(getTempUserName() + " is deleting a tee time");
		
		TeeTime tt = this.getSelectedTeeTime();
		teeTimeDAO.deleteTeeTimeFromDB(this.getSelectedTeeTime().getTeeTimeID());
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		golfmain.removeTeeTimeFromMainList(tt);
		this.setTeeTimeList(golfmain.getGameSpecificTeeTimes(this.getTeeTimeGame()));
		
		Game gm = BeanUtilJSF.getBean("pc_Game");		
		
		gm = gameDAO.readGameFromDB(tt.getGameID());
		
		gm.setFieldSize(gm.getFieldSize() - 4);
		gm.setTotalPlayers(gm.getFieldSize());
		gm.selectTotalPlayers(gm.getTotalPlayers());
		
		gameDAO.updateGame(gm);
		
		this.setTeeTimeGame(gm);
		
		golfmain.refreshFullGameList();
		
		emailAdminsAboutTeeTimeRemoval(gm,tt);
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"tee time successfully removed",null);
        FacesContext.getCurrentInstance().addMessage(null, msg);    
		
		log.info(getTempUserName() + " successfully deleted tee time from game");
		
		return "";
	}
	
	public String saveTeeTime()
	{
		log.info(getTempUserName() + " user clicked Save Tee Time from maintain tee time dialog");	
		
		if (this.getOperation().equalsIgnoreCase("Add"))
		{
			teeTimeDAO.addTeeTime(this);
		}
		
		if (this.getOperation().equalsIgnoreCase("Update"))
		{
			teeTimeDAO.updateTeeTime(this);
		}
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		gm.refreshTeeTimeList();
		this.setTeeTimeList(gm.getGameSpecificTeeTimes(this.getTeeTimeGame()));
		
		Game game = BeanUtilJSF.getBean("pc_Game");		
		
		game = gameDAO.readGameFromDB(this.getTeeTimeGame().getGameID());
		
		game.setFieldSize(game.getFieldSize() + 4);
		game.setTotalPlayers(game.getFieldSize());
		game.selectTotalPlayers(game.getTotalPlayers());
		
		gameDAO.updateGame(game);
		
		this.setTeeTimeGame(game);
		
		gm.refreshFullGameList();		
		
		log.info(getTempUserName() + " exiting saveTeeTime");
		
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
		
		List<String> adminUsers = usersAndAuthoritiesDAO.readAdminUsers();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		//anyone with admin role
		for (int i = 0; i < adminUsers.size(); i++) 
		{
			Player tempPlayer2 = golfmain.getFullPlayerMapByUserName().get(adminUsers.get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
			
		log.info(getTempUserName() + " emailing tee time removal to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, messageContent, emailRecipients, true); //last param means use jsf		
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
        return Objects.equals(teeTimeID, that);
    }
	
	public int getTeeTimeID() {
		return teeTimeID;
	}
	public void setTeeTimeID(int teeTimeID) {
		this.teeTimeID = teeTimeID;
	}
	public int getGameID() {
		return gameID;
	}
	public void setGameID(int gameID) {
		this.gameID = gameID;
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

	public Game getTeeTimeGame() {
		return teeTimeGame;
	}

	public void setTeeTimeGame(Game teeTimeGame) {
		this.teeTimeGame = teeTimeGame;
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
	
}

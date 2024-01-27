package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.TreeMap;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.primefaces.util.ComponentUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

@Named("pc_Game")
@SessionScoped
public class Game extends SpringBeanAutowiringSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(Game.class);	
	
	private static ResourceBundle genericProps = ResourceBundle.getBundle("ApplicationProperties");
	
	private static String NEWLINE = "<br/>";	
	
	private boolean courseSelected = false;
	private boolean disableShowScores = true;
	private boolean showPlayerScores = false;
	private boolean showPlayerSelectionPanel = false;
	private boolean showPregameEmail = false;
	private boolean showPostgameEmail = false;
	
	private String gameID;
	private int oldGameID;
	private String courseID;
	private int oldCourseID;
	private String courseName;
	private BigDecimal betAmount = new BigDecimal(20.00);
	private Integer totalPlayers;
	private BigDecimal purseAmount;
	private Integer fieldSize;
	private Integer spotsAvailable;
	private Integer totalTeams;
	private Integer howManyBalls;
	private String courseTeeID;
	private String courseTeeColor;
	private BigDecimal eachBallWorth;
	private BigDecimal individualGrossPrize = new BigDecimal(0.00);
	private BigDecimal individualNetPrize = new BigDecimal(0.00);
	private BigDecimal skinsPot;
	private BigDecimal suggestedSkinsPot;
	private BigDecimal teamPot;
	private String teeTimesString;
	private boolean renderSignUp = false;
	private boolean renderWithdraw = false;
	private boolean gameClosedForSignups = false;
	private String playTheBallMethod; //up everywhere; down everywhere; up in fairway, down in rough
	private String gameNoteForEmail;
	
	private String futureGameEmailMessage;
	private String preGameEmailMessage;
	private String testEmailMessage;
	private String postGameEmailMessage;
	private boolean disableEmailStuff = true;
	private ArrayList<String> emailRecipients = new ArrayList<String>();
	
	private String whoIsSignedUpMessage = "";
	private ArrayList<String> playersSignedUpList = new ArrayList<String>();
	
	private Course course;
	private Date gameDate = new Date();
	private String gameDateDisplay;

	private Group group;
	
	private Game selectedGame;
	private boolean disableGameDialogButton = true;
	
	private BigDecimal totalWon; //used for subtotaling individual winnings
	
	private List<Integer> teamNumberSelections = new ArrayList<Integer>();
	private List<Player> playersList = new ArrayList<Player>();
	private List<Round> playerScores = new ArrayList<Round>();
	private List<SkinWinnings> skinWinningsList = new ArrayList<SkinWinnings>();
	private List<Round> teamResultsList = new ArrayList<Round>();
	private List<Game> availableGameList = new ArrayList<Game>();
	private List<Game> futureGamesList = new ArrayList<>();	
	private List<PlayerMoney> playerMoneyForSelectedGameList = new ArrayList<PlayerMoney>();
	private List<SelectItem> teeSelections = new ArrayList<>();
	
	private String operation = "";
	
	public void onLoadGameList() 
	{
		log.info(getTempUserName() + " In onLoadGameList Game.java");
	}
	
	public String addGame()
	{
		operation = "Add";		
		saveGame();
		emailAdminsAboutGameAddition(this);
		return "success";
	}
	
	public String addGameFromGameList()
	{
		operation = "Add";
		
		try
		{
			this.setCourse(null);		
			this.setCourseID("0");
			this.setGameDate(null);
			this.setBetAmount(new BigDecimal(20.0));
			this.setEachBallWorth(new BigDecimal(0.0));
			this.setHowManyBalls(2);
			this.setIndividualGrossPrize(new BigDecimal(10.0));
			this.setIndividualNetPrize(new BigDecimal(10.0));
			this.setPurseAmount(new BigDecimal(0.0));
			this.setSkinsPot(new BigDecimal(0.0));
			this.setTeamPot(new BigDecimal(0.0));
			this.setFieldSize(16);
			this.setTotalPlayers(16);
			this.setTotalTeams(4);
		}
		catch (Exception e)
		{
			log.error("Exception in addGameFromGameList: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in addGameFromGameList: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		return "addGame";
	}
	
	public String updateGame()
	{
		try
		{
			this.setCourse(this.getSelectedGame().getCourse());	
			this.setGameID(this.getSelectedGame().getGameID());
			this.setCourseID(this.getSelectedGame().getCourseID());
			this.setGameDate(this.getSelectedGame().getGameDate());
			this.setBetAmount(this.getSelectedGame().getBetAmount());
			this.setEachBallWorth(this.getSelectedGame().getEachBallWorth());
			this.setHowManyBalls(this.getSelectedGame().getHowManyBalls());
			this.setIndividualGrossPrize(this.getSelectedGame().getIndividualGrossPrize());
			this.setIndividualNetPrize(this.getSelectedGame().getIndividualNetPrize());
			this.setPurseAmount(this.getSelectedGame().getPurseAmount());
			this.setSkinsPot(this.getSelectedGame().getSkinsPot());
			this.setTeamPot(this.getSelectedGame().getTeamPot());
			this.setFieldSize(this.getSelectedGame().getFieldSize());
			this.setTotalPlayers(this.getSelectedGame().getTotalPlayers());
			this.setPlayTheBallMethod(this.getSelectedGame().getPlayTheBallMethod());
			this.setGameClosedForSignups(this.getSelectedGame().isGameClosedForSignups());
		}
		catch (Exception e)
		{
			log.error("Exception in updateGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in updateGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		return "";
	}
	
	public String saveGame()
	{
		log.info(getTempUserName() + " user clicked Save Player from maintain player dialog");	
		
		try
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
			
			if (operation.equalsIgnoreCase("Add"))
			{
				log.info(getTempUserName() + " clicked Save Game from maintain game dialog, from an add");	
				Course course = golfmain.getCoursesMap().get(this.getCourseID());
				this.setCourse(course);
				this.setCourseName(course.getCourseName());
				String newGameID = golfmain.addGame(this);
				golfmain.addTeeTimes(newGameID, teeTimesString, this.getGameDate(), this.getCourseName());
				log.info(getTempUserName() + " after add Game");
			}
			else if (operation.equalsIgnoreCase("Update"))
			{
				log.info(getTempUserName() + " user clicked Save Game from maintain game dialog; from an update");			
				golfmain.updateGame(this);
				log.info(getTempUserName() + " after update Game");
			}
			else
			{
				log.info(getTempUserName() + " neither add nor update from maintain player dialog - doing nothing");
			}
			
			this.setSelectedGame(this);
			
		}
		catch (Exception e)
		{
			log.error("Exception in saveGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in saveGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		return "success";
			
	}
	
	public String onLoadEmailFuture()
	{
		log.info(getTempUserName() + " in onLoadEmailFuture");	
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		this.setFutureGamesList(golfmain.getFutureGames());
		return "";
	}
	
	public String onLoadGameSignUp()
	{
		log.info(getTempUserName() + " in onLoadGameSignUp");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(getTempUserName());	
		
		this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
		log.info(getTempUserName() + " At end of onLoadGameSignUp method in Game.java - size of available game list is: " + this.getAvailableGameList().size());		

		if (this.getAvailableGameList().size() == 0)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"No games available yet for signup.  Admin needs to add new game(s)",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else //take the last one
		{
			this.setSelectedGame(this.getAvailableGameList().get(0));
			resetSignedUpMessage(this.getAvailableGameList().get(0));
		}	
   
		return "";
	}
	
	private void resetSignedUpMessage(Game item)
	{
		this.setSelectedGame(item);
		
		this.getPlayersSignedUpList().clear();
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
		signupSDF.setTimeZone(etTimeZone);
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		List<Round> roundList = golfmain.getRoundsForGame(item);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			if (rd.getSignupDateTime() == null)
			{
				this.getPlayersSignedUpList().add(rd.getPlayerName());
			}
			else
			{
				this.getPlayersSignedUpList().add(rd.getPlayerName() + " (signed up: " + signupSDF.format(rd.getSignupDateTime()) + ")");
			}
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String displayedGameDate = sdf.format(this.getSelectedGame().getGameDate());		
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Signups for game on " + displayedGameDate + "</H3>");
		
		sb.append(NEWLINE);
		
		for (int i = 0; i < this.getPlayersSignedUpList().size(); i++) 
		{
			sb.append(this.getPlayersSignedUpList().get(i) + NEWLINE);
		}		
	
		this.setWhoIsSignedUpMessage(sb.toString());	
	}
	
	public String proceedToPlayerPicklist() 
	{
		log.info(getTempUserName() + " clicked player picklist from game add/select screen; sending them to player picklist screen");
		
		Player playerBean = BeanUtilJSF.getBean("pc_Player");
		
		playerBean.onLoadPlayerPickList();
		return "success";
	}
	
	public String selectRowSignup(SelectEvent<Game> event)
	{
		log.info(getTempUserName() + " clicked on a row in Game list on game signup screen");
		
		Game item = event.getObject();
		
		resetSignedUpMessage(item);
		
		return "";
	}	
	
	public String selectRowAjax(SelectEvent<Game> event)
	{
		log.info(getTempUserName() + " clicked on a row in Game list");
		
		Game item = event.getObject();
		
		this.setSelectedGame(item);
		this.setDisableGameDialogButton(false); //if they've picked one, then they can update it
		this.setShowPlayerSelectionPanel(true);
		this.setShowPregameEmail(true);
		this.setShowPostgameEmail(true);
		this.setDisableEmailStuff(false);
		this.getEmailRecipients().clear();
		this.setPreGameEmailMessage("");
		this.setPostGameEmailMessage("");
			
		setOperation("Update");
		
		return "";
	}	
	
	public void valueChgTotalPlayersAdd(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " changed total players on update game dialog");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{
				this.setOperation("Add");
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			log.error("Exception in valueChgTotalPlayersAdd: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgTotalPlayersAdd: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void valueChgFieldSize(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " changed field size on update game dialog");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{
				this.setOperation("Update");
				this.setTotalPlayers(selectedOption);
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			log.error("Exception in valueChgFieldSize: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgFieldSize: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void valueChgTotalPlayersUpdate(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " changed total players on update game dialog");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{
				this.setOperation("Update");
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			log.error("Exception in valueChgTotalPlayersUpdate: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgTotalPlayersUpdate: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void selectTotalPlayers(Integer totalPlayers) throws Exception 
	{		
		log.debug(getTempUserName() + " i've got this many players selected now: " + totalPlayers);
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		
		gm.setRecommendations(totalPlayers);
		
		this.setPurseAmount(gm.getRecommendedPurseAmount());
		this.setTotalTeams(gm.getRecommendedTotalTeams());
		this.setHowManyBalls(gm.getRecommendedHowManyBalls());
		this.setEachBallWorth(gm.getRecommendedEachBallWorth());
		this.setIndividualGrossPrize(gm.getRecommendedIndividualGrossPrize());
		this.setIndividualNetPrize(gm.getRecommendedIndividualNetPrize());
		this.setSkinsPot(gm.getRecommendedSkinsPot());
		this.setTeamPot(gm.getRecommendedTeamPot());
		this.setTeeTimesString(gm.getRecommendedTeeTimesString());
		this.setPlayTheBallMethod(GolfMain.getRecommendedPlayTheBallMethod());
		this.setGameNoteForEmail(GolfMain.getRecommendedGameNote());
						
	}
	
	public void valueChangeGame(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " picked a game on select players for game form");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			this.setSelectedGame(selectedOption);
			this.setShowPlayerSelectionPanel(true);
		}
						
	}
	
	public void valueChangeCourse(AjaxBehaviorEvent event) 
	{
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Course selectedOption = (Course)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			this.setCourseSelected(true);
		}
		else
		{
			this.setCourseSelected(false);
		}
				
	}
	
	public String signUp(Game game1)
	{
		log.info(getTempUserName() + " clicked signup button");
		
		Round newRound = new Round();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(getTempUserName());
		
		newRound.setGameID(game1.getGameID());
		newRound.setPlayerID(tempPlayer.getPlayerID());
		newRound.setPlayer(tempPlayer);
		newRound.setPlayerName(tempPlayer.getFirstName() + " " + tempPlayer.getLastName());
		newRound.setTeamNumber(0); //set to skins only for now until admin sets teams up.
		if (tempPlayer.getTeeTime() != null)
		{
			newRound.setTeeTimeID(tempPlayer.getTeeTime().getTeeTimeID());
			newRound.setTeeTime(tempPlayer.getTeeTime());
			//comment 1
		}
		newRound.setCourseTeeID(game1.getCourseTeeID());
		
		newRound.setRoundHandicap(tempPlayer.getHandicap()); //set this to their usga ghin handicap index when they sign up.  We'll tweak this later when entering them on the set game handicaps page
		
		if (game1.getCourseTeeID() == null || game1.getCourseTeeID().length() == 0)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"No tee selected - please select tees to play from",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			golfmain.addRound(newRound);
			
			this.getAvailableGameList().clear();
			this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
			
			resetSignedUpMessage(game1);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String displayedGameDate = sdf.format(game1.getGameDate());		
		
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player " + newRound.getPlayerName() + " successfully signed up for game on " + displayedGameDate,null);
			
			log.info("Player " + newRound.getPlayerName() + " successfully signed up for game on " + displayedGameDate);
			
	        FacesContext.getCurrentInstance().addMessage(null, msg);

		}
		        
		return "";
	}
	
	public String withdraw(Game game1)
	{
		log.info(getTempUserName() + " clicked withdraw button");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(getTempUserName());
	
		Round theRound = golfmain.getRoundByGameandPlayer(game1.gameID, tempPlayer.getPlayerID());
		
		golfmain.deleteRoundFromDB(theRound.getRoundID());
		
		this.getAvailableGameList().clear();
		this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
	
		resetSignedUpMessage(game1);
		
		//If we have a withdrawal AFTER the game has been closed for signups, any admin role needs to know about that.  Email them.
		//if (game1.isGameClosedForSignups())
		//kind of want to always know about this so commented out the if block 2020-07-04
		//{
			emailAdminsAboutWithdrawal(game1, tempPlayer);
		//}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String displayedGameDate = sdf.format(game1.getGameDate());		
	
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player " + theRound.getPlayerName() + " successfully withdrew from game on " + displayedGameDate,null);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    
		return "";
	}
	
	private void emailAdminsAboutWithdrawal(Game game1, Player tempPlayer) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Player Withdrawal";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Player Withdrawal</H3>");
		
		sb.append(NEWLINE);
		
		String wdPlayer = tempPlayer.getFullName();
		
		sb.append("<H3>" + wdPlayer + " withdrew from Game on " + Utils.getDayofWeekString(game1.getGameDate()) + " " + sdf.format(game1.getGameDate()) + "</H3>");
		
		String withdrawalMessageContent = sb.toString();		
		
		this.setTestEmailMessage(sb.toString());
		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
				
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");			
		List<String> adminUsers = golfmain.getAdminUserList();
		
		//anyone with admin role
		for (int i = 0; i < adminUsers.size(); i++) 
		{
			Player tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(adminUsers.get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
			
		log.info(getTempUserName() + " emailing withdrawal to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, withdrawalMessageContent, emailRecipients, true);	//last param means use jsf	
	}

	public void calculatePMTotal(Object o) 
	{	    
	   //log.info("inside calculateTotal.  Object = " + o);
	   String objectString = (String)o; //Player name comes back here... i guess because we're sorting by that?
	   this.setTotalWon(new BigDecimal(0.0));
	   
	   for (int i = 0; i < this.getPlayerMoneyForSelectedGameList().size(); i++) 
	   {
		   PlayerMoney pm = this.getPlayerMoneyForSelectedGameList().get(i);
		   if (pm.getPlayer().getFullName().equalsIgnoreCase(objectString))
		   {
			   this.setTotalWon(this.getTotalWon().add(pm.getAmount()));
		   }
	   }
	}
	
	/*	
	 * This was for the original non-database way
	public String setUpWeeklyGame()
	{
		log.info("entering setUpWeeklyGame method");
		
		boolean validInput = validateInput();
		
		if (validInput)
		{
			calculateSkins();
			calculateTeams();	
			calculateIndividualGrossAndNet();
		}
		return "";
	}
	*/
	public String runSelectedGame()
	{
		log.info(getTempUserName() + " entering runSelectedGame method");
		
		try
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
			
			//first assign selected game into current game
			this.setBetAmount(this.getSelectedGame().getBetAmount());
			this.setCourse(this.getSelectedGame().getCourse());
			this.setEachBallWorth(this.getSelectedGame().getEachBallWorth());
			this.setGameDate(this.getSelectedGame().getGameDate());
			this.setGameID(this.getSelectedGame().getGameID());
			this.setHowManyBalls(this.getSelectedGame().getHowManyBalls());
			this.setIndividualGrossPrize(this.getSelectedGame().getIndividualGrossPrize());
			this.setIndividualNetPrize(this.getSelectedGame().getIndividualNetPrize());
			this.setPurseAmount(this.getSelectedGame().getPurseAmount());
			this.setSkinsPot(this.getSelectedGame().getSkinsPot());
			this.setTeamPot(this.getSelectedGame().getTeamPot());
			this.setTotalPlayers(this.getSelectedGame().getTotalPlayers());
			this.setTotalTeams(this.getSelectedGame().getTotalTeams());		
			
			this.setPlayerScores(golfmain.getRoundsForGame(this));		
			
			//clear out first for this - in case it has been run before
			golfmain.deletePlayerMoneyFromDB(this.getGameID());
			this.getTeamResultsList().clear();
			
			addEntryFees();
			
			calculateSkins();
			calculateTeams();	
			calculateIndividualGrossAndNet();
			
			this.setPlayerMoneyForSelectedGameList(golfmain.getPlayerMoneyByGame(this.getSelectedGame()));
		}
		catch (Exception e)
		{
			log.error("Exception in runSelectedGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in runSelectedGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		return "";
	}
		
	private void addEntryFees() 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		for (int i = 0; i < this.getPlayerScores().size(); i++) 
		{
			Round rd = this.getPlayerScores().get(i);
			
			PlayerMoney pm = new PlayerMoney();
			pm.setGameID(this.getSelectedGame().getGameID());
			pm.setPlayerID(rd.getPlayerID());
			
			BigDecimal entryFeeAmount = new BigDecimal(this.getSelectedGame().getBetAmount().doubleValue());
			
			if (rd.getTeamNumber() < 1) //this means skins only
			{
				entryFeeAmount = this.getSelectedGame().getBetAmount().multiply(new BigDecimal("0.4")); //since skins is usually 40% of total pot
			}
			
			entryFeeAmount = entryFeeAmount.multiply(new BigDecimal("-1"));
			
			pm.setAmount(entryFeeAmount);
			pm.setDescription("Entry Fee: " + entryFeeAmount);
			
			golfmain.addPlayerMoney(pm);	
		}		
	}

	private void calculateIndividualGrossAndNet() 
	{	
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		//First get all the scores
		List<Integer> grossScores = new ArrayList<Integer>();
		List<BigDecimal> netScores = new ArrayList<BigDecimal>();
		List<Round> netRounds = new ArrayList<Round>();
		List<Round> grossRounds = new ArrayList<Round>();
		
		for (int i = 0; i < playerScores.size(); i++) 
		{
			Round round = playerScores.get(i);
			if (round.getTeamNumber() > 0) //skins only players can't win individual net/gross
			{
				netRounds.add(round);
				grossRounds.add(round);
				int grossScore = round.getTotalScore();
				grossScores.add(grossScore);
				BigDecimal netScore = (new BigDecimal(grossScore)).subtract(round.getRoundHandicap());
				netScores.add(netScore);
			}
		}
		
		Collections.sort(grossScores);
		Collections.sort(netScores);
		
		//First thing we have to do is see if the same player has either won both or split one and won the other.  Can't allow it...
		
		Map<String,Player> winnersMap = new HashMap<>(); 
		
		int lowestGrossScore = grossScores.get(0);
		BigDecimal lowestNetScore = netScores.get(0);
		int totalGrossWinners = 0;
		int totalNetWinners = 0;
		Player multipleWinningPlayer = null;
		
		for (int i = 0; i < playerScores.size(); i++) 
		{
			Round round = playerScores.get(i);
			int grossScore = round.getTotalScore();
			if (grossScore == lowestGrossScore)
			{	
				totalGrossWinners++;
				if (winnersMap.containsKey(round.getPlayerID()))
				{
					multipleWinningPlayer = round.getPlayer();
				}
				else
				{
					winnersMap.put(round.getPlayerID(), round.getPlayer());
				}
			}
			BigDecimal netScore = (new BigDecimal(grossScore)).subtract(round.getRoundHandicap());
			
			if (netScore.compareTo(lowestNetScore) == 0)
			{
				totalNetWinners++;
				if (winnersMap.containsKey(round.getPlayerID()))
				{
					multipleWinningPlayer = round.getPlayer();
				}
				else
				{
					winnersMap.put(round.getPlayerID(), round.getPlayer());
				}
			}
		}
		
		if (multipleWinningPlayer != null)
		{
			//need to remove them from one of the lists.  If they're solo winner in one of them, keep them there and remove from the other.
			
			if (totalNetWinners > 1 && totalGrossWinners == 1) //remove this guy from net
			{
				for (int i = 0; i < netRounds.size(); i++) 
				{
					Round round = netRounds.get(i);
					if (round.getPlayerID() == multipleWinningPlayer.getPlayerID())
					{
						netRounds.remove(i);
						break;
					}
				}
			}
			if (totalGrossWinners > 1 && totalNetWinners == 1) //remove this guy from gross
			{
				for (int i = 0; i < grossRounds.size(); i++) 
				{
					Round round = grossRounds.get(i);
					if (round.getPlayerID() == multipleWinningPlayer.getPlayerID())
					{
						grossRounds.remove(i);
						break;
					}
				}
			}
			if (totalGrossWinners == 1 && totalNetWinners == 1) //he won both.  Give him gross
			{
				for (int i = 0; i < netRounds.size(); i++) 
				{
					Round round = grossRounds.get(i);
					if (round.getPlayerID() == multipleWinningPlayer.getPlayerID())
					{
						lowestNetScore = netScores.get(1);
						netRounds.remove(i);
						break;
					}
				}
			}
			
		}
		
		if (individualGrossPrize.compareTo(new BigDecimal(0.0)) > 0)
		{
			totalGrossWinners = 0;
			//loop player scores looking for the low score - could be more than 1
			for (int i = 0; i < grossRounds.size(); i++) 
			{
				Round round = grossRounds.get(i);
				int grossScore = round.getTotalScore();
				if (grossScore == lowestGrossScore)
				{
					totalGrossWinners++;
				}
			}
			
			//now that we know how many gross Winners we have...
			BigDecimal grossPrize = individualGrossPrize.divide(new BigDecimal(totalGrossWinners), 2, RoundingMode.HALF_UP);
			
			int totalFound = 0;
			for (int i = 0; i < grossRounds.size(); i++) 
			{
				Round round = grossRounds.get(i);
				int grossScore = round.getTotalScore();
				if (grossScore == lowestGrossScore)
				{
					PlayerMoney pm = new PlayerMoney();
					pm.setGameID(this.getSelectedGame().getGameID());
					pm.setPlayerID(round.getPlayerID());
					pm.setAmount(grossPrize);
					pm.setDescription("Low Individual Gross: " + lowestGrossScore);
					
					golfmain.addPlayerMoney(pm);		
					
					totalFound++;
					if (totalFound == totalGrossWinners)
					{
						break;
					}
				}
			}
		}
		
		if (individualNetPrize.compareTo(new BigDecimal(0.0)) > 0)
		{
			totalNetWinners = 0;
			
			//loop player scores looking for the low score - could be more than 1
			for (int i = 0; i < netRounds.size(); i++) 
			{
				Round round = netRounds.get(i);
				BigDecimal netScore = (new BigDecimal(round.getTotalScore())).subtract(round.getRoundHandicap());
				
				if (netScore.compareTo(lowestNetScore) == 0)
				{
					totalNetWinners++;
				}
			}
			
			//now that we know how many net Winners we have...
			BigDecimal netPrize = individualNetPrize.divide(new BigDecimal(totalNetWinners), 2, RoundingMode.HALF_UP);
			
			int totalFound = 0;
			for (int i = 0; i < netRounds.size(); i++) 
			{
				Round round = netRounds.get(i);
				
				BigDecimal netScore = (new BigDecimal(round.getTotalScore())).subtract(round.getRoundHandicap());
				
				if (netScore.compareTo(lowestNetScore) == 0)
				{
							
					PlayerMoney pm = new PlayerMoney();
					pm.setGameID(this.getSelectedGame().getGameID());
					pm.setPlayerID(round.getPlayerID());
					pm.setAmount(netPrize);
					pm.setDescription("Low Individual Net: " + lowestNetScore);
						
					golfmain.addPlayerMoney(pm);
									
					totalFound++;
					if (totalFound == totalNetWinners)
					{
						break;
					}
				}
			}
		}
				
	}

	private void calculateTeams() 
	{
		log.info(getTempUserName() + " entering calculateTeams");
			
		int totalMembersPerTeam = this.getTotalPlayers() / this.getTotalTeams();
		
		//note that if someone is on team zero, they will not be on a team, only skins...and if doing it, individual gross or net.
		for (int teamNumber = 1; teamNumber <= this.getTotalTeams(); teamNumber++) 
		{
			String teamName = "";
			//get the team member scores only
			List<Round> teamRoundsList = new ArrayList<Round>();
			for (int i = 0; i < playerScores.size(); i++) 
			{
				Round round = playerScores.get(i);
				Player player = round.getPlayer();
				if (round.getTeamNumber() != teamNumber)
				{
					continue; // just re-loop if this player is on another team
				}
				
				teamName = teamName + player.getFirstName() + " ";
				teamRoundsList.add(round);
				
				if (teamRoundsList.size() == totalMembersPerTeam)
				{
					break; // no need to continue if we have everyone's card...
				}
			}
			
			//ok now we have everyone's score card on this team.
			for (int i = 1; i <= this.getHowManyBalls(); i++)
			{
				log.info(getTempUserName() + " working on team: " + teamNumber + " ball: " + i);
				
				Round tempRound = new Round();
				Player tempPlayer = new Player();
				tempPlayer.setTeamNumber(teamNumber);
				tempPlayer.setLastName("Ball " + i);
				tempPlayer.setFirstName(teamName);
				tempRound.setPlayer(tempPlayer);				
				
				for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
				{
					log.info(getTempUserName() + "      working on hole: " + holeNumber);					
					int lowestScore = Utils.getTeamScoreOnHole(teamRoundsList, holeNumber, i);  //i represents ball number					
					tempRound = Utils.setDisplayScore(holeNumber, lowestScore, course, tempRound);
				}
				
				tempRound.setFront9Total(Utils.front9Score(tempRound));
				tempRound.setBack9Total(Utils.back9Score(tempRound));
				tempRound.setTotalScore(tempRound.getBack9Total() + tempRound.getFront9Total());
				
				int totalToParInt = tempRound.getTotalScore() - (course.getFront9Par() + course.getBack9Par());
				String totalToPar = String.valueOf(totalToParInt);
				if (totalToParInt > 0)
				{
					totalToPar = "+" + totalToPar;					
				}
				tempRound.setTotalToPar(totalToPar);
				
				if (totalToParInt < 0)
				{
					tempRound.setTotalToParClass(Utils.BIRDIE_OR_BETTER_STYLECLASS);
				}
				else
				{
					tempRound.setTotalToParClass(Utils.PAR_OR_WORSE_STYLECLASS);
				}
				tempRound = Utils.setDisplayScore(Utils.FRONT9_STYLE_HOLENUM, tempRound.getFront9Total(), course, tempRound);
				tempRound = Utils.setDisplayScore(Utils.BACK9_STYLE_HOLENUM, tempRound.getBack9Total(), course, tempRound);
				tempRound = Utils.setDisplayScore(Utils.TOTAL_STYLE_HOLENUM, tempRound.getFront9Total() + tempRound.getBack9Total(), course, tempRound);
				
				this.getTeamResultsList().add(tempRound);
				
			}			
			
		}
		
		calcTeamIndividualWinnings();
				
		log.info(getTempUserName() + " leaving calculateTeams");		
	}

	private void calcTeamIndividualWinnings() 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		//First, which team(s) won each ball
		
		Integer playersPerTeamInt = totalPlayers / totalTeams;
		BigDecimal playersPerTeam = new BigDecimal(playersPerTeamInt);
		
		for (int i = 1; i <= this.getHowManyBalls(); i++)
		{
			List<Round> ballRoundsList = new ArrayList<Round>();
			for (int j = 0; j < this.getTeamResultsList().size(); j++)
			{
				Round round = this.getTeamResultsList().get(j);
				String ballNumber = round.getPlayer().getLastName();
				
				if (!ballNumber.contains(String.valueOf(i)))
				{
					continue; // just re-loop if this ball is not the one we're working on
				}
								
				ballRoundsList.add(round);
				
				if (ballRoundsList.size() == this.getTotalTeams())
				{
					break; // no need to continue if we have all the team ball scores.
				}
			}
			
			// now we have all the team ball scores.  Have to figure out who won.
			List<Round> winningBallList = new ArrayList<Round>();
			for (int j = 0; j < ballRoundsList.size(); j++)
			{
				if (winningBallList.size() == 0) //first one; just add it
				{
					winningBallList.add(ballRoundsList.get(j));
					continue;
				}
				
				Round newRound = ballRoundsList.get(j);
				Integer newBallScore = newRound.getTotalScore();
				
				Round existingRound = winningBallList.get(0);
				Integer winningBallScore = existingRound.getTotalScore();
				
				if (newBallScore < winningBallScore)
				{
					winningBallList.clear();
					winningBallList.add(newRound);
				}
				else if (newBallScore == winningBallScore) //could be a split
				{					
					winningBallList.add(newRound);
				}
			}
			
			//at the end of that loop, we should have the winning ball(s).
			log.info(getTempUserName() + " Ball " + i + " has " + winningBallList.size() + " winner(s)");
		
			BigDecimal individualBallPrize = eachBallWorth.divide(new BigDecimal(winningBallList.size()).multiply(playersPerTeam), 2, RoundingMode.HALF_UP);
			log.info(getTempUserName() + " Ball " + i + " individualBallPrize = " + individualBallPrize);
			
			//for each player on these teams, they get the individual ball prize.
			for (int j = 0; j < winningBallList.size(); j++)
			{
				Round winningBallRound = winningBallList.get(j);
				int winningTeamNumber = winningBallRound.getPlayer().getTeamNumber();
				
				for (int k = 0; k < this.getPlayerScores().size(); k++) 
				{
					Round playerRound = this.getPlayerScores().get(k);
					if (playerRound.getPlayer().getTeamNumber() == winningTeamNumber  //original method
					||  playerRound.getTeamNumber() == winningTeamNumber) //new DB method
					{						
						PlayerMoney pm = new PlayerMoney();
						pm.setGameID(this.getSelectedGame().getGameID());
						pm.setPlayerID(playerRound.getPlayerID());
						pm.setAmount(individualBallPrize);
						pm.setDescription("Ball " + i);
						
						golfmain.addPlayerMoney(pm);							
					}
				}
				
			}
		}
		
	}

	private void calculateSkins() 
	{
		log.info(getTempUserName() + " entering calculateSkins");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		int totalSkins = 0;
		List<SkinWinnings> tempSkinsList = new ArrayList<SkinWinnings>();
		
		for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
		{	
			Map<Integer,Player> holeScoreMap = new HashMap<Integer, Player>(); //we'll end up with only unique scores here when done with all players
			List<Integer> holeScoreList = new ArrayList<Integer>();
			for (int i = 0; i < playerScores.size(); i++) 
			{
				Round round = playerScores.get(i);
				Score score = round.getRoundbyHoleScores().get(holeNumber-1); //since index is zero-based use holeNumber - 1
				holeScoreMap.put(score.getScore(), round.getPlayer());
				holeScoreList.add(score.getScore());
			}
			
			TreeMap<Integer, Player> holeScoreTreeMap = new TreeMap<Integer, Player>();
			holeScoreTreeMap.putAll(holeScoreMap); //now we're sorted
			
			int lowestScore = 0;
			Player lowestPlayer = new Player();
			
			for (Map.Entry<Integer, Player> entry : holeScoreTreeMap.entrySet()) 
			{
				lowestScore = entry.getKey();
				lowestPlayer = entry.getValue();
				break; //once we get the first one, we're outta here
			}
			
			//now we know the lowest score and who has it; now it's just a matter of does anyone else have it..
			int scoreCount = 0;
			for (int i = 0; i < holeScoreList.size(); i++) 
			{
				int scoreInt = holeScoreList.get(i);
				if (scoreInt == lowestScore)
				{
					scoreCount++;
				}
				else
				{
					continue;
				}
				
				if (scoreCount > 1) //more than one means no winner for this hole.
				{
					break;
				}
			}
			
			if (scoreCount == 1) //we have a winner for this hole.
			{
				totalSkins++;
				SkinWinnings skinWinnings = new SkinWinnings();
				skinWinnings.setPlayerID(lowestPlayer.getPlayerID());
				skinWinnings.setPlayerName(lowestPlayer.getFullName());
				skinWinnings.setWinDescription(lowestScore + " on hole " + holeNumber);
				tempSkinsList.add(skinWinnings);				
			}
           
		}
		
		if (totalSkins > 0)
		{
			BigDecimal skinValue = skinsPot.divide(new BigDecimal(totalSkins), new MathContext(100));
			
			log.info(getTempUserName() + " Skins won: " + totalSkins + " at " + skinValue + " each");
			
			for (int i = 0; i < tempSkinsList.size(); i++) 
			{
				SkinWinnings skinWinnings = tempSkinsList.get(i);
				skinWinnings.setAmountWon(skinValue);
				this.skinWinningsList.add(skinWinnings);
				
				PlayerMoney pm = new PlayerMoney();
				pm.setGameID(this.getSelectedGame().getGameID());
				pm.setPlayerID(skinWinnings.getPlayerID());
				pm.setAmount(skinValue);
				pm.setDescription("Skin: " + skinWinnings.getWinDescription());
				
				golfmain.addPlayerMoney(pm);				
			}
		}		
		
		log.info(getTempUserName() + " leaving calculateSkins");
	}
	
	/*
	private boolean validateInput()
	{
		boolean isInputValid = true;
		
		BigDecimal temp = skinsPot.add(teamPot);
		BigDecimal temp1 = temp.add(individualGrossPrize);
		BigDecimal totalPrizes = temp1.add(individualNetPrize);
		
		if (!(totalPrizes.compareTo(purseAmount) == 0))
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Total Prizes does not add up to Purse Amount",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
	        isInputValid = false;
		}
		
		return isInputValid;
	}
	*/
	
	public String saveNewPlayers()
	{
		log.info(getTempUserName() + " clicked save new players");
		
		return "";
	}
	
	public BigDecimal getBetAmount() {
		return betAmount;
	}
	public void setBetAmount(BigDecimal betAmount) 
	{
		this.betAmount = betAmount;		
	}
	
	public Integer getTotalPlayers() {
		return totalPlayers;
	}
	public void setTotalPlayers(Integer totalPlayers) throws Exception 
	{
		this.totalPlayers = totalPlayers;		
		this.setTotalTeams(Utils.setRecommendedTeams(totalPlayers));	
	}	
	
	public String exportPlayerName(UIColumn column) 
	{
	    String value = "";
	    
	    for(UIComponent child: column.getChildren()) 
	    {
	        if(child instanceof ValueHolder) 
	        {
	            value = ComponentUtils.getValueToRender(FacesContext.getCurrentInstance(), child);
	        }
	    }
	    if (value!=null && value.trim().length()>=6)
	    {
	    	value = value.substring(0, 6);
	    }
	    return value;
	}
	public String showScores()
	{
		log.info(getTempUserName() + " clicked show scores button");
		this.setShowPlayerScores(true);
		return "";
	}

	public String composeTestEmail()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Golf Test Email sent to admin</H3>");
		
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		sb.append("<a href='http://golfscoring.us-east-1.elasticbeanstalk.com/login.xhtml'>Golf Scoring</a>");
		
		this.setTestEmailMessage(sb.toString());
		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		String emailAddress = "paulslomkowski@yahoo.com";
			
		emailRecipients.add(emailAddress);
		
		log.info(getTempUserName() + " emailing to: " + emailRecipients);
		
		return "";
	}
	
	public String composePostGameEmail()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Golf Results</H3>");
		
		StringBuffer sbGameDetails = getEmailGameDetails();
		sb.append(sbGameDetails);
		
		StringBuffer sbScores = getEmailScores();
		sb.append(sbScores);
		
		StringBuffer sbTeamResults = getEmailTeamResults();
		sb.append(sbTeamResults);
		
		StringBuffer sbSkinResultDetails = getEmailSkinResults();
		sb.append(sbSkinResultDetails);		
		
		StringBuffer sbIndividualWinnings = getEmailIndividualWinnings();
		sb.append(sbIndividualWinnings);
		
		this.setPostGameEmailMessage(sb.toString());
		
		establishEmailRecipients();
		
		return "";
	}
	
	private StringBuffer getEmailIndividualWinnings() 
	{
		StringBuffer sb = new StringBuffer();
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Individual Winnings</H3>");
		
		sb.append(NEWLINE);		
	
		Collections.sort(playerMoneyForSelectedGameList, new PlayerMoney.PlayerMoneyComparatorByLastNameFirstName());
		
		for (int i = 0; i < this.getPlayerMoneyForSelectedGameList().size(); i++) 
		{
			PlayerMoney pm = this.getPlayerMoneyForSelectedGameList().get(i);
			sb.append(pm.getPlayer().getFullName() + " " + pm.getDescription() + " " + currencyFmt.format(pm.getAmount()));
			sb.append(NEWLINE);		
		}
		
		sb.append("<H3>Venmo Totals</H3>");
		
		sb.append(NEWLINE);		
		
		String currentPlayer = "";
		BigDecimal zero = BigDecimal.ZERO;
		BigDecimal pmTotal = new BigDecimal(0.0);
	    if (pmTotal.compareTo(zero) > 0)
	    {
	    	pmTotal = zero;
	    }
		
		if (this.getPlayerMoneyForSelectedGameList().size() > 0)
		{
			currentPlayer = this.getPlayerMoneyForSelectedGameList().get(0).getPlayer().getFullName();
		}
		
		for (int i = 0; i < this.getPlayerMoneyForSelectedGameList().size(); i++) 
		{
			PlayerMoney pm = this.getPlayerMoneyForSelectedGameList().get(i);
			if (!pm.getPlayer().getFullName().equalsIgnoreCase(currentPlayer))
			{
				if (pmTotal.compareTo(zero) > 0)
				{
				}
				else
				{
					sb.append(currentPlayer + ": owes " + currencyFmt.format(pmTotal.multiply(new BigDecimal(-1.0))));
					sb.append(NEWLINE);	
				}
				
				pmTotal = zero;
				currentPlayer = pm.getPlayer().getFullName();
			}			
			pmTotal = pmTotal.add(pm.getAmount());				
		}
		
		if (pmTotal.compareTo(zero) > 0)
		{
		}
		else
		{
			sb.append(currentPlayer + ": owes " + currencyFmt.format(pmTotal.multiply(new BigDecimal(-1.0))));
		}
		sb.append(NEWLINE);	
		sb.append(NEWLINE);	
		
		for (int i = 0; i < this.getPlayerMoneyForSelectedGameList().size(); i++) 
		{
			PlayerMoney pm = this.getPlayerMoneyForSelectedGameList().get(i);
			if (!pm.getPlayer().getFullName().equalsIgnoreCase(currentPlayer))
			{
				if (pmTotal.compareTo(zero) > 0)
				{
					sb.append(currentPlayer + ": gets paid " + currencyFmt.format(pmTotal));
					sb.append(NEWLINE);	
				}
				else
				{
				}
				
				pmTotal = zero;
				currentPlayer = pm.getPlayer().getFullName();
			}			
			pmTotal = pmTotal.add(pm.getAmount());				
		}
		
		if (pmTotal.compareTo(zero) > 0)
		{
			sb.append(currentPlayer + ": gets paid " + currencyFmt.format(pmTotal));
		}
		else
		{
		}
		sb.append(NEWLINE);	
		sb.append(NEWLINE);	
		
		return sb;
	}

	private StringBuffer getEmailSkinResults() 
	{
		StringBuffer sb = new StringBuffer();
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Skins</H3>");
		
		sb.append(NEWLINE);		
	
		for (int i = 0; i < this.getSkinWinningsList().size(); i++) 
		{
			SkinWinnings skinWinnings = this.getSkinWinningsList().get(i);
			sb.append(skinWinnings.getPlayerName() + " " + skinWinnings.getWinDescription() + " " + currencyFmt.format(skinWinnings.getAmountWon()));
			sb.append(NEWLINE);		
		}	
		
		return sb;
	}

	private StringBuffer getEmailTeamResults() 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Team Results</H3>");
		
		sb.append(NEWLINE);		
		
		sb.append("<html><body>");
		sb.append("<table border=");
		sb.append("\"");
		sb.append("1");
		sb.append("\"");
		sb.append(" border-collapse=");
		sb.append("\"");
		sb.append("collapse");
		sb.append("\"");
		sb.append(">");		
		
		//header stuff
		sb.append("<tr>");
		
        sb.append("<td>");
        sb.append("Team");
        sb.append("</td>");

        sb.append("<td>");
        sb.append("Ball");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("1");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("2");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("3");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("4");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("5");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("6");
        sb.append("</td>");  
        
        sb.append("<td>");
        sb.append("7");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("8");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("9");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("Fr");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("10");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("11");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("12");
        sb.append("</td>");      
        
        sb.append("<td>");
        sb.append("13");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("14");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("15");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("16");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("17");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("18");
        sb.append("</td>");         

        sb.append("<td>");
        sb.append("Bk");
        sb.append("</td>"); 

        sb.append("<td>");
        sb.append("Total");
        sb.append("</td>"); 

        sb.append("<td>");
        sb.append("+/-");
        sb.append("</td>"); 
        
        sb.append("</tr>");
        
		for (int i = 0; i < this.getTeamResultsList().size(); i++) 
		{
			Round rd = this.getTeamResultsList().get(i);
			
			sb.append("<tr>");
			
	        sb.append("<td>");
	        sb.append(rd.getPlayer().getFirstName());
	        sb.append("</td>");

	        sb.append("<td>");
	        sb.append(rd.getPlayer().getLastName());
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole1StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole1Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole1Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole2StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole2Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole2Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole3StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole3Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole3Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole4StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole4Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole4Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole5StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole5Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole5Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole6StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole6Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole6Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole7StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole7Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole7Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole8StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole8Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole8Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole9Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole9Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getFront9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getFront9Total() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getFront9Total());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole10StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole10Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole10Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole11StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole11Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole11Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole12StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole12Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole12Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole13StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole13Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole13Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole14StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole14Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole14Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole15StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole15Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole15Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole16StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole16Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole16Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole17StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole17Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole17Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole18StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole18Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole18Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getBack9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getBack9Total() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getBack9Total());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getTotalStyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getTotalScore() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getTotalScore());
	        }
	        
	        sb.append("</td>");	
	        
	        sb.append("<td>");
	        
	        if (rd.getTotalToParClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getTotalToPar() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getTotalToPar());
	        }
	        
	        sb.append("</td>");	
	        
	        sb.append("</tr>");
		}	
		
		sb.append("</table></body></html>");
		
		log.info(getTempUserName() + " team results html");
		log.info(getTempUserName() + sb.toString());
		
		return sb;
	
	}

	private StringBuffer getEmailScores() 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Scores</H3>");
		
		sb.append(NEWLINE);		
		
		Collections.sort(playerScores, new RoundComparatorByTeamNumber());
		
		sb.append("<html><body>");
		sb.append("<table border=");
		sb.append("\"");
		sb.append("1");
		sb.append("\"");
		sb.append(" border-collapse=");
		sb.append("\"");
		sb.append("collapse");
		sb.append("\"");
		sb.append(">");		
		
		//header stuff
		sb.append("<tr>");
		
        sb.append("<td>");
        sb.append("Player");
        sb.append("</td>");

        sb.append("<td>");
        sb.append("Team");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("1");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("2");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("3");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("4");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("5");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("6");
        sb.append("</td>");  
        
        sb.append("<td>");
        sb.append("7");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("8");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("9");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("Fr");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("10");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("11");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("12");
        sb.append("</td>");      
        
        sb.append("<td>");
        sb.append("13");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("14");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("15");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("16");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("17");
        sb.append("</td>");
        
        sb.append("<td>");
        sb.append("18");
        sb.append("</td>");         

        sb.append("<td>");
        sb.append("Bk");
        sb.append("</td>"); 

        sb.append("<td>");
        sb.append("Grs");
        sb.append("</td>"); 

        sb.append("<td>");
        sb.append("Hcp");
        sb.append("</td>"); 

        sb.append("<td>");
        sb.append("Net");
        sb.append("</td>"); 
        
        sb.append("</tr>");
        
		for (int i = 0; i < this.getPlayerScores().size(); i++) 
		{
			Round rd = this.getPlayerScores().get(i);
			
			sb.append("<tr>");
			
	        sb.append("<td>");
	        sb.append(rd.getPlayer().getFullName());
	        sb.append("</td>");

	        sb.append("<td>");
	        sb.append(rd.getTeamNumberDisplay());
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole1StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole1Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole1Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole2StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole2Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole2Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole3StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole3Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole3Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole4StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole4Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole4Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole5StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole5Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole5Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole6StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole6Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole6Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole7StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole7Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole7Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole8StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole8Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole8Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole9Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole9Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getFront9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getFront9Total() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getFront9Total());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole10StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole10Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole10Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole11StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole11Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole11Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole12StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole12Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole12Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole13StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole13Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole13Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole14StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole14Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole14Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole15StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole15Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole15Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        if (rd.getHole16StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole16Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole16Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole17StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole17Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole17Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getHole18StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getHole18Score() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getHole18Score());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getBack9StyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getBack9Total() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getBack9Total());
	        }
	        
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getTotalStyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getTotalScore() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getTotalScore());
	        }
	        
	        sb.append("</td>");	
	        
	        sb.append("<td>");
	        sb.append(rd.getRoundHandicap());
	        sb.append("</td>");
	        
	        sb.append("<td>");
	        
	        if (rd.getNetStyleClass().equalsIgnoreCase("textRed"))
	        {
	        	sb.append("<font color=red>" + rd.getNetScore() + "</font>");
	        }
	        else
	        {
	        	sb.append(rd.getNetScore());
	        }
	        
	        sb.append("</td>");	
	        
	        sb.append("</tr>");
		}	
		
		sb.append("</table></body></html>");
		
		log.info(getTempUserName() + " scores html");
		log.info(getTempUserName() + sb.toString());
		
		return sb;
	}

	public String navigateToEmail()
	{
		return "success";
	}	

	public String onLoadPreGameEmail() 
	{
		return "success";		
	}
	
	public String composeFutureGameEmail(boolean useJSFBean)
	{
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		
		String subjectLine = "<H3>Golf game on " + Utils.getDayofWeekString(this.getSelectedGame().getGameDate()) + " " + sdf.format(this.getSelectedGame().getGameDate()) + " on " + this.getSelectedGame().getCourseName() + "</H3>";
		sb.append(subjectLine);
		
		sb.append(NEWLINE);
		
		StringBuffer sbFutureGameDetails = getFutureEmailGameDetails(useJSFBean);
		sb.append(sbFutureGameDetails);	
		sb.append(NEWLINE);
				
		if (useJSFBean)
		{
			StringBuffer whosIn = getGameParticipants();
			sb.append(whosIn);		
			sb.append(NEWLINE);		
		}
		else
		{
			sb.append("~~~gameDetails~~~");
			sb.append(NEWLINE);	
		}			
				
		sb.append("To sign up to play (or withdraw if you've already signed up but can no longer play), go to this site:");
		sb.append(NEWLINE);
		sb.append("<a href='http://golfscoring.us-east-1.elasticbeanstalk.com/login.xhtml'>Golf Scoring</a>");	
		sb.append(NEWLINE);
		sb.append("Please do not send email requests to sign up to play - the way to sign up or withdraw now is the website above.  The gmail box is not monitored regularly and you may be left out of a game if you request to get in that way.");
		sb.append(NEWLINE);
	
		sb.append(NEWLINE);
		
		sb.append("Log in with your credentials.  If first time logging in, those are your first name initial plus your last name for both userid and password.");
		sb.append(NEWLINE);
		sb.append("For example for John Doe, his credentials would be userid jdoe and password jdoe.");
		sb.append(NEWLINE);		
		sb.append("You can change your password from the profile menu and you are encouraged to do so.  Your user id is not changeable.");
		sb.append(NEWLINE);
		sb.append("To sign up or withdraw from a game hover on the Game menu and click Sign up for Game.");
		sb.append(NEWLINE);
		sb.append("select * from that screen click the appropriate button for the game you are interested in.");			
		
		this.setFutureGameEmailMessage(sb.toString());
		
		if (useJSFBean) //will be true if called from UI; false if from dailyemailjob
		{
			establishEmailRecipientsForFutureGame();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"This email will go to:" + emailRecipients,null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}

		return sb.toString();
	}
	
	public String deleteGame()
	{
		log.info(getTempUserName() + " entering Delete Game.  About to delete: " + this.getSelectedGame().getGameDate());
		
		try
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
			
			golfmain.deleteRoundsFromDB(this.getSelectedGame().getGameID());		
			golfmain.deleteTeeTimesForGameFromDB(this.getSelectedGame().getGameID());
			golfmain.deletePlayerMoneyFromDB(this.getSelectedGame().getGameID());		
			golfmain.deleteGame(this.getSelectedGame().getGameID());
			
			log.info(getTempUserName() + " " + this.getSelectedGame().getGameDate() + " successfully deleted");
			this.setSelectedGame(golfmain.getFullGameList().get(0));
			
		}
		catch (Exception e)
		{
			log.error("Exception in deleteGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in deleteGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		return "";
	}
	
	private StringBuffer getGameParticipants() 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("Current list of players for this game:");
		sb.append(NEWLINE);
	
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		List<String> roundPlayers = golfmain.getGameParticipantsFromDB(this.getSelectedGame());
		
		for (int i = 0; i < roundPlayers.size(); i++) 
		{
			String playerName = roundPlayers.get(i);
			if (i+1 <= this.getSelectedGame().getFieldSize())
			{
				sb.append(i+1 + ". " + playerName);
			}
			else
			{
				sb.append(i+1 + ". " + playerName + " (wait list)");
			}
			sb.append(NEWLINE);
		}
		
		int spotsAvailable = this.getSelectedGame().getFieldSize() - roundPlayers.size();
		
		sb.append(NEWLINE);
		sb.append("Spots still available: " + spotsAvailable);
		sb.append(NEWLINE);
		
		return sb;
	}

	public String sendFutureGameEmail()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf game on " + Utils.getDayofWeekString(this.getSelectedGame().getGameDate()) + " " + sdf.format(this.getSelectedGame().getGameDate()) + " on " + this.getSelectedGame().getCourseName();		
		if (emailRecipients.size() >= 100)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"100 or more recipients on Email list - google will not send it, preventing before trying",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			SAMailUtility.sendEmail(subjectLine, futureGameEmailMessage, emailRecipients, true); //last param means use jsf
		}
		
		return "";
	}
	
	private void establishEmailRecipientsForFutureGame()
	{		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		List<Player> fullPlayerList = golfmain.getFullPlayerList();
		
		emailRecipients = Utils.setEmailFullRecipientList(fullPlayerList);
				
		log.info(getTempUserName() + " future game composing email: will email to these recipients if sendemail clicked: " + emailRecipients);	
	}

	public String composePreGameEmail()
	{
		try
		{
			StringBuffer sb = new StringBuffer();
			sb.append("<H3>Weekly Game</H3>");
			
			StringBuffer sbGameDetails = getEmailGameDetails();
			sb.append(sbGameDetails);
			
			StringBuffer sbPlayGroupDetails = getEmailPlayGroupDetails();
			sb.append(sbPlayGroupDetails);
			
			//StringBuffer doYourOwnScores = doYourOwnScoresBlurb();
			//sb.append(doYourOwnScores);
			
			StringBuffer sbMoneyTeamDetails = getEmailMoneyTeamDetails();
			sb.append(sbMoneyTeamDetails);		
			
			this.setPreGameEmailMessage(sb.toString());
			
			establishEmailRecipients();
		}
		catch (Exception e)
		{
			log.error("Exception when composing pregame email: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when composing pregame email: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		return "";
	}
	
	/*
	private StringBuffer doYourOwnScoresBlurb() 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		sb.append("When done with your round if one person in the group could login to the website <a href='http://golfscoring.us-east-1.elasticbeanstalk.com/login.xhtml'>Golf Scoring</a> ");
		sb.append(NEWLINE);
		sb.append("and post all the scores for your play group it would be greatly appreciated.  Click on Scores menu, and then choose the Enter scores for game option");
		sb.append(NEWLINE);
		sb.append("If not please text or email a picture of your scorecard to the admin of the site as we have been doing.");	
		sb.append(NEWLINE);
		return sb;
	}
	*/
	
	private void establishEmailRecipients() 
	{
		String mailTo = genericProps.getString("mailTo");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		List<Round> roundList = golfmain.getRoundsForGame(this.getSelectedGame());
			
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			String emailAddress = rd.getPlayer().getEmailAddress();
			
			if (emailAddress == null 
			|| emailAddress.trim().length() == 0 
			|| emailAddress.equalsIgnoreCase("unknown")
			|| emailAddress.equalsIgnoreCase(mailTo))
			{
				continue;
			}
			
			emailRecipients.add(emailAddress);
		}
		
		boolean containsSearchStr = emailRecipients.stream().anyMatch("cleclerc@bryanpark.com"::equalsIgnoreCase);
		if (!containsSearchStr) //always add Chris LeClerc if not already there
		{
			emailRecipients.add("cleclerc@bryanpark.com");
		}	
				
		log.info(getTempUserName() + " emailing to: " + emailRecipients);
		
	}

	private StringBuffer getEmailMoneyTeamDetails() 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		StringBuffer sb = new StringBuffer();
		
		StringBuffer sbSkinsOnlyPlayers = new StringBuffer();
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Money Teams - auto-generated</H3>");
		
		sb.append(NEWLINE);
		
		List<Round> roundList = golfmain.getRoundsForGame(this.getSelectedGame());
		Collections.sort(roundList, new RoundComparatorByTeamNumberAndHandicap());
		
		//ferret out any skins only players first.
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			int moneyTeamNumber = rd.getTeamNumber();
			if (moneyTeamNumber == -1)
			{
				sbSkinsOnlyPlayers.append(rd.getPlayer().getFullName());
				roundList.remove(i);
			}
			else
			{
				break;
			}
		}	
		
		int priorTeamNumber = 1;
		StringBuffer playerString = new StringBuffer();
		BigDecimal totalTeamHandicap = new BigDecimal(0.0);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			int moneyTeamNumber = rd.getTeamNumber();			
			
			if (moneyTeamNumber != priorTeamNumber)
			{
				if (playerString.length() > 0)
				{
					playerString.replace(playerString.lastIndexOf(", "), playerString.length(), "");
				}
				sb.append("Team " + priorTeamNumber + ": " + playerString + " Total Team Handicap = " + totalTeamHandicap);
				sb.append(NEWLINE);
				if (playerString.length() > 0)
				{
					playerString.delete(0, playerString.length());
				}
				priorTeamNumber = moneyTeamNumber;
				totalTeamHandicap = new BigDecimal(0.0);
			}
			
			playerString.append(rd.getPlayer().getFullName() + " (crs Hcp: " + rd.getRoundHandicap().toBigInteger().toString() + "), ");
			totalTeamHandicap = totalTeamHandicap.add(rd.getRoundHandicap());
		}
		
		//for the last money team
		playerString.replace(playerString.lastIndexOf(", "), playerString.length(), "");
		sb.append("Team " + priorTeamNumber + ": " + playerString + " Total Team Handicap = " + totalTeamHandicap);
		sb.append(NEWLINE);	
		
		if (sbSkinsOnlyPlayers.length() > 0)
		{
			sb.append(NEWLINE);	
			sb.append("Skins Only players: " + sbSkinsOnlyPlayers.toString());
			sb.append(NEWLINE);	
		}
		
		return sb;
	
	}

	private StringBuffer getEmailPlayGroupDetails() throws Exception
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		
		sb.append("<H3>Play Groups</H3>");
		
		sb.append(NEWLINE);
		
		List<Round> roundList = golfmain.getRoundsForGame(this.getSelectedGame());
		Collections.sort(roundList, new RoundComparatorByPlayGroup());
		
		int priorPlayGroupNumber = 1;
		if (roundList.size() > 0)
		{
			Round temprd = roundList.get(0);
			priorPlayGroupNumber = temprd.getTeeTime().getPlayGroupNumber();
		}
		
		String tempTeeTimeString = "";
		StringBuffer playerString = new StringBuffer();
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			int playGroupNumber = rd.getTeeTime().getPlayGroupNumber();
			
			if (i==0) //very first one - populate currentTeeTime
			{
				tempTeeTimeString = rd.getTeeTime().getTeeTimeString();
			}
			
			if (playGroupNumber != priorPlayGroupNumber)
			{
				playerString.replace(playerString.lastIndexOf(", "), playerString.length(), "");
				sb.append(tempTeeTimeString + " " + playerString);
				sb.append(NEWLINE);
				playerString.delete(0, playerString.length());	
				tempTeeTimeString = rd.getTeeTime().getTeeTimeString();
				priorPlayGroupNumber = playGroupNumber;
			}
			
			playerString.append(rd.getPlayer().getFullName() + ", ");						
		}
		
		//for the last group
		playerString.replace(playerString.lastIndexOf(", "), playerString.length(), "");
		sb.append(tempTeeTimeString + " " + playerString);
		sb.append(NEWLINE);
		
		return sb;
	}

	private StringBuffer getFutureEmailGameDetails(boolean useJSFBean) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		StringBuffer sb = new StringBuffer();
	
		Game gm = this.getSelectedGame();
		sb.append("Date: " + sdf.format(gm.getGameDate()) + NEWLINE);
		sb.append("Course: " + gm.getCourseName() + NEWLINE);
		sb.append("Bet Amt: " + currencyFmt.format(gm.getBetAmount()) + NEWLINE);
		sb.append("Field Size: " + gm.getFieldSize() + NEWLINE);
		sb.append("Tee Times: ");
		
		if (useJSFBean)
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
			
			List<TeeTime> teeTimeList = golfmain.getTeeTimesByGame(gm);
			for (int i = 0; i < teeTimeList.size(); i++) 
			{
				TeeTime teeTime = teeTimeList.get(i);
				sb.append(teeTime.getTeeTimeString() + " ");
			}
			
			sb.append(NEWLINE);
		}
		else
		{
			sb.append("~~~teeTimes~~~");
			sb.append(NEWLINE);	
		}			
		
		return sb;
	}

	private void emailAdminsAboutGameAddition(Game game1) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Game has been added";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Game addition</H3>");
		
		sb.append(NEWLINE);
		
		sb.append("<H3> Game added: " + Utils.getDayofWeekString(game1.getGameDate()) + " " +sdf.format(game1.getGameDate()) + "</H3>");
		
		String messageContent = sb.toString();		
	
		emailRecipients = getEmailAdminsRecipientList();
			
		log.info(getTempUserName() + " emailing game add to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, messageContent, emailRecipients, true); //last param means use jsf		
	}
	
	private ArrayList<String> getEmailAdminsRecipientList() 
	{
		ArrayList<String> emailRecipients = new ArrayList<String>();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		//anyone with admin role
		for (int i = 0; i < golfmain.getAdminUserList().size(); i++) 
		{
			Player tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(golfmain.getAdminUserList().get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
		
		return emailRecipients;
	}
		
	private StringBuffer getEmailGameDetails() 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		
		Game gm = this.getSelectedGame();
		sb.append("Day: " + Utils.getDayofWeekString(gm.getGameDate()) + NEWLINE);
		sb.append("Date: " + sdf.format(gm.getGameDate()) + NEWLINE);
		sb.append("Course: " + gm.getCourseName() + NEWLINE);
		sb.append("Bet Amt: " + currencyFmt.format(gm.getBetAmount()) + NEWLINE);
		sb.append("Total Players: " + gm.getTotalPlayers() + NEWLINE);
		sb.append("Purse: " + currencyFmt.format(gm.getPurseAmount()) + NEWLINE);
		sb.append("Total Teams: " + gm.getTotalTeams() + NEWLINE);
		sb.append("Team Balls: " + gm.getHowManyBalls() + NEWLINE);
		sb.append("Team Ball Value: " + currencyFmt.format(gm.getEachBallWorth()) + NEWLINE);
		sb.append("Team Pot: " + currencyFmt.format(gm.getTeamPot()) + NEWLINE);
		sb.append("Skins Pot: " + currencyFmt.format(gm.getSkinsPot()) + NEWLINE);
		sb.append("Indiv Gross: " + currencyFmt.format(gm.getIndividualGrossPrize()) + NEWLINE);
		sb.append("Indiv Net: " + currencyFmt.format(gm.getIndividualNetPrize()) + NEWLINE);
		
		sb.append(NEWLINE);
		
		sb.append("Play the ball: " + gm.getPlayTheBallMethod() + NEWLINE);
		
		if (gm.getGameNoteForEmail() != null && gm.getGameNoteForEmail().trim().length()>0)
		{
			sb.append(NEWLINE);
			sb.append("Game notes: " + gm.getGameNoteForEmail() + NEWLINE);
		}
		
		sb.append(NEWLINE);
		
		sb.append("Gold tee players move back a tee box if necessary to stay within 1 tee box of whites. " + NEWLINE);
		sb.append("All scores and settling of bets must happen manually in the Pro Shop grill after the round. " + NEWLINE);
		sb.append("If you are unable to stay after the round for whatever reason, please arrange with someone to settle up your entry fee and how you want any winnings to be paid. " + NEWLINE);
		//sb.append("In a pinch we could still use the website and venmo but that MUST BE PRE-ORGANIZED WITH A SITE ADMIN like Paul Slomkowski");
		//sb.append("or Kenton Robertson or whomever is temporarily assigned an admin for a given game." + NEWLINE);		
		
		return sb;
	}

	public static class GameComparatorByDate implements Comparator<Game> 
	{
		public int compare(Game game1, Game game2)
		{
			if (game1.getGameDate().after(game2.getGameDate()))
			{
				return 1;
			}
			else
			{
				return 0;
			}
		}		
	}	
	
	public static class RoundComparatorByPlayGroup implements Comparator<Round> 
	{
		public int compare(Round round1, Round round2)
		{
			return round1.getTeeTime().getPlayGroupNumber() - round2.getTeeTime().getPlayGroupNumber();
		}		
	}
	
	public static class RoundComparatorByTeamNumber implements Comparator<Round> 
	{
		public int compare(Round round1, Round round2)
		{
			return round1.getTeamNumber() - round2.getTeamNumber();
		}		
	}
	
	public static class RoundComparatorByTeamNumberAndHandicap implements Comparator<Round> 
	{
		public int compare(Round round1, Round round2)
		{
			Round rd1 = round1;
			Round rd2 = round2;
			
			int team1 = rd1.getTeamNumber();
			int team2 = rd2.getTeamNumber();
			
	        int teamComparison = team1 - team2;

	        if (teamComparison != 0) 
	        {
	        	return teamComparison;
	        } 

	        BigDecimal handicap1 = rd1.getRoundHandicap();
	        BigDecimal handicap2 = rd2.getRoundHandicap();
	        
	        return handicap1.compareTo(handicap2);
		}		
	}
	
	public String sendPreGameEmail()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf game setup for " + sdf.format(this.getSelectedGame().getGameDate());
		SAMailUtility.sendEmail(subjectLine, preGameEmailMessage, emailRecipients, true); //last param means use jsf
		return "";
	}
	
	public String sendPostGameEmail()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf results for " + sdf.format(this.getSelectedGame().getGameDate());
		SAMailUtility.sendEmail(subjectLine, postGameEmailMessage, emailRecipients, true); //last param means use jsf
		return "";
	}
	
	public String sendTestEmail()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf Test email sent to admin on " + sdf.format(new Date());
		SAMailUtility.sendEmail(subjectLine, testEmailMessage, emailRecipients, true); //last param means use jsf
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
        return Objects.equals(gameID, that);
    }
	
	public BigDecimal getPurseAmount() {
		return purseAmount;
	}
	public void setPurseAmount(BigDecimal purseAmount) 
	{
		this.purseAmount = purseAmount;
		
		if (purseAmount != null && purseAmount.compareTo(new BigDecimal(0.0)) == 1)
		{
			BigDecimal suggestedSkins = purseAmount.multiply(new BigDecimal(0.375)); // 37.5% for skins
			
			int roundedSkins = Utils.roundToNearestMultipleOfTen(suggestedSkins.intValue());  //Round to nearest 10 dollars
			this.setSuggestedSkinsPot(new BigDecimal(roundedSkins));
		}

	}
	public Integer getTotalTeams() {
		return totalTeams;
	}
	public void setTotalTeams(Integer totalTeams) throws Exception
	{
		this.totalTeams = totalTeams;
		this.getTeamNumberSelections().clear();
		
		for (int i = 1; i <= totalTeams; i++) 
		{
			this.getTeamNumberSelections().add(i);
		}
	}
	public Integer getHowManyBalls() {
		return howManyBalls;
	}
	public void setHowManyBalls(Integer howManyBalls) 
	{
		this.howManyBalls = howManyBalls;
		if (eachBallWorth != null && howManyBalls != null)
		{
			setTeamPot(eachBallWorth.multiply(new BigDecimal(howManyBalls)));
		}
	}
	public BigDecimal getSkinsPot() {
		return skinsPot;
	}
	public void setSkinsPot(BigDecimal skinsPot) {
		this.skinsPot = skinsPot;
	}

	public Course getCourse() {
		return course;
	}

	public void setCourse(Course course) {
		this.course = course;
	}

	public Date getGameDate() {
		return gameDate;
	}

	public void setGameDate(Date gameDate) {
		this.gameDate = gameDate;
	}

	
	public BigDecimal getEachBallWorth() {
		return eachBallWorth;
	}

	public void setEachBallWorth(BigDecimal eachBallWorth) 
	{
		this.eachBallWorth = eachBallWorth;
		if (howManyBalls != null)
		{
			setTeamPot(eachBallWorth.multiply(new BigDecimal(howManyBalls)));
		}
	}
	
	public BigDecimal getTeamPot() {
		return teamPot;
	}

	public void setTeamPot(BigDecimal teamPot) {
		this.teamPot = teamPot;
	}

	public boolean isDisableShowScores() {
		return disableShowScores;
	}

	public void setDisableShowScores(boolean disableShowScores) {
		this.disableShowScores = disableShowScores;
	}

	public List<Round> getPlayerScores() 
	{
		return playerScores;
	}

	public void setPlayerScores(List<Round> playerScores) {
		this.playerScores = playerScores;
	}

	public boolean isShowPlayerScores() {
		return showPlayerScores;
	}

	public void setShowPlayerScores(boolean showPlayerScores) {
		this.showPlayerScores = showPlayerScores;
	}

	public boolean isCourseSelected() {
		return courseSelected;
	}

	public void setCourseSelected(boolean courseSelected) {
		this.courseSelected = courseSelected;
	}

	public List<SkinWinnings> getSkinWinningsList() {
		return skinWinningsList;
	}

	public void setSkinWinningsList(List<SkinWinnings> skinWinningsList) {
		this.skinWinningsList = skinWinningsList;
	}

	public BigDecimal getTotalWon() {
		return totalWon;
	}

	public void setTotalWon(BigDecimal totalWon) {
		this.totalWon = totalWon;
	}

	public List<Round> getTeamResultsList() {
		return teamResultsList;
	}

	public void setTeamResultsList(List<Round> teamResultsList) {
		this.teamResultsList = teamResultsList;
	}

	

	public List<Player> getPlayersList() {
		return playersList;
	}

	public void setPlayersList(List<Player> playersList) {
		this.playersList = playersList;
	}

	public List<Integer> getTeamNumberSelections() {
		return teamNumberSelections;
	}

	public void setTeamNumberSelections(List<Integer> teamNumberSelections) {
		this.teamNumberSelections = teamNumberSelections;
	}
	
	public Group getGroup() {
		return group;
	}

	public void setGroup(Group group) {
		this.group = group;
	}

	

	public Game getSelectedGame() {
		return selectedGame;
	}

	public void setSelectedGame(Game selectedGame) {
		this.selectedGame = selectedGame;
	}

	public boolean isDisableGameDialogButton() {
		return disableGameDialogButton;
	}

	public void setDisableGameDialogButton(boolean disableGameDialogButton) {
		this.disableGameDialogButton = disableGameDialogButton;
	}
	
	
	public BigDecimal getIndividualGrossPrize() {
		return individualGrossPrize;
	}

	public void setIndividualGrossPrize(BigDecimal individualGrossPrize) {
		this.individualGrossPrize = individualGrossPrize;
	}

	public BigDecimal getIndividualNetPrize() {
		return individualNetPrize;
	}

	public void setIndividualNetPrize(BigDecimal individualNetPrize) {
		this.individualNetPrize = individualNetPrize;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public BigDecimal getSuggestedSkinsPot() {
		return suggestedSkinsPot;
	}
	public void setSuggestedSkinsPot(BigDecimal suggestedSkinsPot) {
		this.suggestedSkinsPot = suggestedSkinsPot;
	}

	public boolean isShowPlayerSelectionPanel() {
		return showPlayerSelectionPanel;
	}

	public void setShowPlayerSelectionPanel(boolean showPlayerSelectionPanel) {
		this.showPlayerSelectionPanel = showPlayerSelectionPanel;
	}

	public List<PlayerMoney> getPlayerMoneyForSelectedGameList() {
		return playerMoneyForSelectedGameList;
	}

	public void setPlayerMoneyForSelectedGameList(List<PlayerMoney> playerMoneyForSelectedGameList) {
		this.playerMoneyForSelectedGameList = playerMoneyForSelectedGameList;
	}

	public String getTeeTimesString() {
		return teeTimesString;
	}

	public void setTeeTimesString(String teeTimesString) {
		this.teeTimesString = teeTimesString;
	}

	public String getPlayTheBallMethod() {
		return playTheBallMethod;
	}

	public void setPlayTheBallMethod(String playTheBallMethod) {
		this.playTheBallMethod = playTheBallMethod;
	}

	public String getGameNoteForEmail() {
		return gameNoteForEmail;
	}

	public void setGameNoteForEmail(String gameNoteForEmail) {
		this.gameNoteForEmail = gameNoteForEmail;
	}

	public boolean isShowPregameEmail() {
		return showPregameEmail;
	}

	public void setShowPregameEmail(boolean showPregameEmail) {
		this.showPregameEmail = showPregameEmail;
	}

	public String getPreGameEmailMessage() {
		return preGameEmailMessage;
	}

	public void setPreGameEmailMessage(String preGameEmailMessage) {
		this.preGameEmailMessage = preGameEmailMessage;
	}

	public boolean isDisableEmailStuff() {
		return disableEmailStuff;
	}

	public void setDisableEmailStuff(boolean disableEmailStuff) {
		this.disableEmailStuff = disableEmailStuff;
	}

	public ArrayList<String> getEmailRecipients() {
		return emailRecipients;
	}

	public void setEmailRecipients(ArrayList<String> emailRecipients) {
		this.emailRecipients = emailRecipients;
	}

	public String getPostGameEmailMessage() {
		return postGameEmailMessage;
	}

	public void setPostGameEmailMessage(String postGameEmailMessage) {
		this.postGameEmailMessage = postGameEmailMessage;
	}

	public boolean isShowPostgameEmail() {
		return showPostgameEmail;
	}

	public void setShowPostgameEmail(boolean showPostgameEmail) {
		this.showPostgameEmail = showPostgameEmail;
	}

	public String getTestEmailMessage() {
		return testEmailMessage;
	}

	public void setTestEmailMessage(String testEmailMessage) {
		this.testEmailMessage = testEmailMessage;
	}

	public List<Game> getAvailableGameList() {
		return availableGameList;
	}

	public void setAvailableGameList(List<Game> availableGameList) {
		this.availableGameList = availableGameList;
	}

	public boolean isRenderSignUp() {
		return renderSignUp;
	}

	public void setRenderSignUp(boolean renderSignUp) {
		this.renderSignUp = renderSignUp;
	}

	public boolean isRenderWithdraw() {
		return renderWithdraw;
	}

	public void setRenderWithdraw(boolean renderWithdraw) {
		this.renderWithdraw = renderWithdraw;
	}

	public String getWhoIsSignedUpMessage() {
		return whoIsSignedUpMessage;
	}

	public void setWhoIsSignedUpMessage(String whoIsSignedUpMessage) {
		this.whoIsSignedUpMessage = whoIsSignedUpMessage;
	}

	public ArrayList<String> getPlayersSignedUpList() {
		return playersSignedUpList;
	}

	public void setPlayersSignedUpList(ArrayList<String> playersSignedUpList) {
		this.playersSignedUpList = playersSignedUpList;
	}

	public Integer getFieldSize() {
		return fieldSize;
	}

	public void setFieldSize(Integer fieldSize) {
		this.fieldSize = fieldSize;
	}

	public Integer getSpotsAvailable() {
		return spotsAvailable;
	}

	public void setSpotsAvailable(Integer spotsAvailable) {
		this.spotsAvailable = spotsAvailable;
	}

	public String getFutureGameEmailMessage() {
		return futureGameEmailMessage;
	}

	public void setFutureGameEmailMessage(String futureGameEmailMessage) {
		this.futureGameEmailMessage = futureGameEmailMessage;
	}

	public List<Game> getFutureGamesList() {
		return futureGamesList;
	}

	public void setFutureGamesList(List<Game> futureGamesList) {
		this.futureGamesList = futureGamesList;
	}

	public boolean isGameClosedForSignups() {
		return gameClosedForSignups;
	}

	public void setGameClosedForSignups(boolean gameClosedForSignups) {
		this.gameClosedForSignups = gameClosedForSignups;
	}

	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}

	public List<SelectItem> getTeeSelections() {
		return teeSelections;
	}

	public void setTeeSelections(List<SelectItem> teeSelections) {
		this.teeSelections = teeSelections;
	}

	public String getCourseTeeColor() 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (courseTeeID != null && !courseTeeID.equalsIgnoreCase("0") && (courseTeeColor == null || courseTeeColor.trim().length() == 0))
		{
			String tempColor = "";
			Map<String,CourseTee> ctMap = golfmain.getCourseTeesMap();
			CourseTee ct = ctMap.get(courseTeeID);
			tempColor = ct.getTeeColor();
			setCourseTeeColor(tempColor);
		}
		return courseTeeColor;
	}

	public void setCourseTeeColor(String courseTeeColor) {
		this.courseTeeColor = courseTeeColor;
	}
	
	public String getGameDateDisplay()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		gameDateDisplay = sdf.format(this.getGameDate());
		return gameDateDisplay;
	}

	public void setGameDateDisplay(String gameDateDisplay) 
	{
		this.gameDateDisplay = gameDateDisplay;
	}

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public String getCourseTeeID() {
		return courseTeeID;
	}

	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}

	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}
	
}

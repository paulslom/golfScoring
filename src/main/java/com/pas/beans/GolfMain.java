package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import com.pas.beans.Player.PlayerComparatorByLastName;
import com.pas.dao.CourseDAO;
import com.pas.dao.CourseTeeDAO;
import com.pas.dao.GameDAO;
import com.pas.dao.GolfUsersDAO;
import com.pas.dao.GroupDAO;
import com.pas.dao.PlayerDAO;
import com.pas.dao.PlayerMoneyDAO;
import com.pas.dao.PlayerTeePreferenceDAO;
import com.pas.dao.RoundDAO;
import com.pas.dao.TeeTimeDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoPlayerTeePreference;
import com.pas.dynamodb.DynamoUtil;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

@Named("pc_GolfMain")
@SessionScoped
public class GolfMain implements Serializable
{
	static
	{
		Calendar cal = Calendar.getInstance();
		String currentMonth = new SimpleDateFormat("MMM").format(cal.getTime());
		
		if (currentMonth.equalsIgnoreCase("Nov")
		||  currentMonth.equalsIgnoreCase("Dec")
		||	currentMonth.equalsIgnoreCase("Jan")
		||  currentMonth.equalsIgnoreCase("Feb")
		||  currentMonth.equalsIgnoreCase("Mar"))
		{
			recommendedPlayTheBallMethod = "Up in fairway and up in bunkers";
		}
		else
		{
			recommendedPlayTheBallMethod = "Down everywhere but up in bunkers";
		}
		
		recommendedGameNote = "Play well and have fun!";

	}
	
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(GolfMain.class);	
		
	private List<SelectItem> totalPlayersSelections = new ArrayList<SelectItem>();
	private List<SelectItem> totalTeamsSelections = new ArrayList<SelectItem>();
	private List<SelectItem> howManyBallsSelections = new ArrayList<SelectItem>();
	private List<SelectItem> playGroupSelections = new ArrayList<SelectItem>();
	private List<SelectItem> holeSelections = new ArrayList<>();
	private List<SelectItem> scoreSelections = new ArrayList<>();
	
	private boolean disableProceedToSelectGame = true;
	private boolean disableProceedToSelectPlayers = true;
	private boolean disableDeleteSelectedPlayerRound = true;
	
	private String groupEmailMessage = "";
	private String groupEmailDisclaimer = "";  
	private String groupEmailSender;

	private Group selectedGroup;
	
	private String newPassword;
	
	private TeeTime selectedTeeTime;
	private boolean disableDeleteTeeTime = true;
	private String teeTimeOperation = "";
	private boolean teeTimesRenderInquiry = true;
	private boolean teeTimesRenderAddUpdateDelete = false;
	private List<TeeTime> gameSpecificTeeTimesList = new ArrayList<>();
	
	private Course selectedCourse;
	private boolean disableDeleteCourse = true;
	private String courseOperation;
	private boolean courseRenderInputFields = true;
	private boolean courseRenderInquiry = true;
	private boolean courseRenderAddUpdate = false;
	private List<CourseTee> newCourseTeesList = new ArrayList<>();
	private List<DynamoCourseTee> courseSpecificCourseTeesList = new ArrayList<>();	
	private Integer courseTeeBoxes;
	
	private PlayerTeePreference selectedPlayerTeePreference;
	private boolean disablePlayerTeePrefDialogButton = true;	
	private List<DynamoPlayerTeePreference> playerSpecificTeePreferencesList = new ArrayList<>();
	
	private String loggedInPlayerName;
	private String loggedInPlayerEmail;

	private DynamoGroup defaultGroup = null;
	
	private ArrayList<String> emailRecipients = new ArrayList<String>();
	
	private BigDecimal recommendedPurseAmount;
	private Integer recommendedTotalTeams;
	private Integer recommendedHowManyBalls;
	private BigDecimal recommendedEachBallWorth;
	private BigDecimal recommendedIndividualGrossPrize = new BigDecimal(0.00);
	private BigDecimal recommendedIndividualNetPrize = new BigDecimal(0.00);
	private BigDecimal recommendedSkinsPot;
	private BigDecimal recommendedSuggestedSkinsPot;
	private BigDecimal recommendedTeamPot;
	private BigDecimal recommendedGameFee;
	private String recommendedTeeTimesString;
	private static String recommendedPlayTheBallMethod; //up everywhere; Down everywhere but up in bunkers; up in fairway, down in rough
	private static String recommendedGameNote; 
	
	private final double id = Math.random();
	
	private static String NEWLINE = "<br/>";	
	
	private GameDAO gameDAO;
	private GolfUsersDAO golfUsersDAO;
	private RoundDAO roundDAO;
	private TeeTimeDAO teeTimeDAO;
	private CourseDAO courseDAO;
	private CourseTeeDAO courseTeeDAO;
	private PlayerDAO playerDAO;
	private PlayerMoneyDAO playerMoneyDAO;
	private PlayerTeePreferenceDAO playerTeePreferencesDAO;
	private GroupDAO groupDAO;
	
	@PostConstruct
	public void init()
	{
		logger.info("Entering GolfMain constructor.  Should only be here ONE time with Spring singleton pattern implemented");	
		logger.info("GolfMain id is: " + this.getId());
		
		final int MIN_PLAYERS = 4;
		final int MIN_BALLS = 1;
		final int MIN_TEAMS = 1;
		
		final int MAX_PLAYERS = 28;
		final int MAX_BALLS = 5;
		final int MAX_TEAMS = 7;
		
		totalPlayersSelections = new ArrayList<SelectItem>();
		totalTeamsSelections = new ArrayList<SelectItem>();
		howManyBallsSelections = new ArrayList<SelectItem>();
		playGroupSelections = new ArrayList<>();
		
		for (int i = 1; i <= 18; i++) 
		{
			SelectItem selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			holeSelections.add(selItem);
		}
		
		for (int i = 1; i <= 7; i++) 
		{
			SelectItem selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			scoreSelections.add(selItem);
		}
		for (int i = MIN_PLAYERS; i <= MAX_PLAYERS; i++) 
		{
			SelectItem selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			totalPlayersSelections.add(selItem);
		}
		
		for (int i = MIN_BALLS; i <= MAX_BALLS; i++) 
		{
			SelectItem selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			howManyBallsSelections.add(selItem);
		}
		
		SelectItem selItem = new SelectItem();
		selItem.setLabel("0");
		selItem.setValue("0");
		totalTeamsSelections.add(selItem);
		
		for (int i = MIN_TEAMS; i <= MAX_TEAMS; i++) 
		{
			SelectItem selItem2 = new SelectItem();
			selItem2.setLabel(String.valueOf(i));
			selItem2.setValue(String.valueOf(i));
			totalTeamsSelections.add(selItem2);
		}
		
		for (int i = 1; i <= MAX_TEAMS; i++) 
		{
			SelectItem selItem3 = new SelectItem();
			selItem3.setLabel(String.valueOf(i));
			selItem3.setValue(String.valueOf(i));
			playGroupSelections.add(selItem3);
		}
		
		try 
		{
			//this gets populated at app startup, no need to do it again when someone logs in.
			if (golfUsersDAO == null || golfUsersDAO.getFullUserMap().isEmpty())
			{
				DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
				golfUsersDAO = new GolfUsersDAO(dynamoClients);
				groupDAO = new GroupDAO(dynamoClients);
				groupDAO.readGroupsFromDB();
				DynamoGroup defaultGroup = this.getGroupsList().get(0);
				this.setDefaultGroup(defaultGroup);
				
				loadCourses(dynamoClients);
				loadCourseTees(dynamoClients);
				loadFullGameList(dynamoClients, defaultGroup);
				loadTeeTimeList(dynamoClients, defaultGroup);
				loadFullPlayerList(dynamoClients);
				loadFullPlayerTeePreferencesList(dynamoClients);
				loadPlayerMoneyList(dynamoClients);
				loadRoundList(dynamoClients);
			}	
		} 
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
		}		
	}

	private void loadRoundList(DynamoClients dynamoClients) throws Exception
	{
		List<String> gameIDList = new ArrayList<>();
		
		for (int i = 0; i < gameDAO.getFullGameList().size(); i++) 
		{
			DynamoGame game = gameDAO.getFullGameList().get(i);
			gameIDList.add(game.getGameID());
		}
		roundDAO = new RoundDAO(dynamoClients);
		roundDAO.readAllRoundsFromDB(gameIDList);
		logger.info("Rounds read in. List size = " + this.getFullRoundsList().size());	
		
		Map<String,Round> tempMap = new HashMap<>();
		
		Map<String, DynamoGame> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(DynamoGame::getGameID, game -> game));
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round round = this.getFullRoundsList().get(i);
				
			DynamoCourseTee ct = getCourseTeesMap().get(round.getCourseTeeID());
			if (ct != null)
		    {
				round.setCourseTeeColor(ct.getTeeColor());	    
		    }	    
			
			DynamoGame dynamoGame = fullGameMap.get(round.getGameID());
			
			if (dynamoGame == null) //should not happen but safeguard
			{
				continue;
			}
			
			Course course = courseDAO.getCoursesMap().get(dynamoGame.getCourseID());
			
			DynamoPlayer player = this.getFullPlayersMapByPlayerID().get(round.getPlayerID());
			
			TeeTime teeTime = this.getFullTeeTimesMap().get(round.getTeeTimeID());
			
			round.setPlayer(player);
			
			round.setPlayerName(player.getFirstName() + " " + player.getLastName());		
			
			if (round.getRoundHandicap() == null)
			{
				round.setRoundHandicapDifferential(new BigDecimal(0.0));
			}
			else
			{
				BigDecimal hcpIndex = player.getHandicap();
				BigDecimal hcpDifferential = hcpIndex.subtract(round.getRoundHandicap());
				round.setRoundHandicapDifferential(hcpDifferential);
			}
			
			round.setTeeTime(teeTime);
			
			round.setHole1StyleClass(Utils.getStyleForHole(1, round.getHole1Score(), course));			
			round.setHole2StyleClass(Utils.getStyleForHole(2, round.getHole2Score(), course));		
			round.setHole3StyleClass(Utils.getStyleForHole(3, round.getHole3Score(), course));		
			round.setHole4StyleClass(Utils.getStyleForHole(4, round.getHole4Score(), course));		
			round.setHole5StyleClass(Utils.getStyleForHole(5, round.getHole5Score(), course));	
			round.setHole6StyleClass(Utils.getStyleForHole(6, round.getHole6Score(), course));
			round.setHole7StyleClass(Utils.getStyleForHole(7, round.getHole7Score(), course));			
			round.setHole8StyleClass(Utils.getStyleForHole(8, round.getHole8Score(), course));	
			round.setHole9StyleClass(Utils.getStyleForHole(9, round.getHole9Score(), course));	
			round.setHole10StyleClass(Utils.getStyleForHole(10, round.getHole10Score(), course));	
			round.setHole11StyleClass(Utils.getStyleForHole(11, round.getHole11Score(), course));						
			round.setHole12StyleClass(Utils.getStyleForHole(12, round.getHole12Score(), course));					
			round.setHole13StyleClass(Utils.getStyleForHole(13, round.getHole13Score(), course));					
			round.setHole14StyleClass(Utils.getStyleForHole(14, round.getHole14Score(), course));	
			round.setHole15StyleClass(Utils.getStyleForHole(15, round.getHole15Score(), course));	
			round.setHole16StyleClass(Utils.getStyleForHole(16, round.getHole16Score(), course));	
			round.setHole17StyleClass(Utils.getStyleForHole(17, round.getHole17Score(), course));	
			round.setHole18StyleClass(Utils.getStyleForHole(18, round.getHole18Score(), course));	
			
			round.setFront9StyleClass(Utils.getFront9StyleClass(round.getFront9Total(), course));	
			round.setBack9StyleClass(Utils.getBack9StyleClass(round.getBack9Total(), course));		
			round.setTotalStyleClass(Utils.getTotalStyleClass(round.getTotalScore(), course));		
			round.setNetStyleClass(Utils.getNetStyleClass(round.getNetScore(), course));
			round.setTotalToParClass(Utils.getTotalStyleClass(round.getTotalScore(), course));		
			
			tempMap.put(round.getRoundID(), round);
		}
		
		roundDAO.getFullRoundsMap().clear();
		roundDAO.setFullRoundsMap(tempMap);
		
		Collection<Round> values = roundDAO.getFullRoundsMap().values();
		roundDAO.setFullRoundsList(new ArrayList<>(values));
		
		Collections.sort(this.getFullRoundsList(), new Comparator<Round>() 
		{
		   public int compare(Round o1, Round o2) 
		   {
		      return o1.getSignupDateTime().compareTo(o2.getSignupDateTime());
		   }
		});
		
	}

	public void loadCourses(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadCourses");
		courseDAO = new CourseDAO(dynamoClients);
		courseDAO.readCoursesFromDB(this.getDefaultGroup()); //pick the first group by default - Bryan Park.
		logger.info("Courses read in. List size = " + this.getCoursesList().size());		
    }
	
	public void loadCourseTees(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadCourseTees");
		
		courseTeeDAO = new CourseTeeDAO(dynamoClients);
		courseTeeDAO.readCourseTeesFromDB(this.getDefaultGroup());					
		logger.info("Course Tees read in. List size = " + this.getCourseTees().size());		
    }
	
	public void loadFullGameList(DynamoClients dynamoClients, DynamoGroup defaultGroup) throws Exception
	{
		logger.info("entering loadFullGameList");
		
		gameDAO = new GameDAO(dynamoClients);
		gameDAO.readGamesFromDB(defaultGroup, courseDAO.getCoursesMap());			
		logger.info("Full Game list read in. List size = " + this.getFullGameList().size());	
		
		Map<String,DynamoGame> tempMap = new HashMap<>();
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			DynamoGame game = this.getFullGameList().get(i);
			assignCourseToGame(game);
			tempMap.put(game.getGameID(), game);
		}
			
		Collection<DynamoGame> values = tempMap.values();
		gameDAO.setFullGameList(new ArrayList<>(values));
		
		Collections.sort(this.getFullGameList(), new Comparator<DynamoGame>() 
		{
		   public int compare(DynamoGame o1, DynamoGame o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
	}
	
	public String returnToGameList()
	{		
		return "/auth/admin/gameList.xhtml";
	}
	
	public String updateCourseSetup()
	{
		setCourseOperation("Update");
		setCourseRenderInquiry(false);
    	setCourseRenderAddUpdate(true);
    	
    	this.getCourseSpecificCourseTeesList().clear();
    	this.setCourseSpecificCourseTeesList(courseTeeDAO.getCourseSpecificCourseTeesListByCourseID(this.getSelectedCourse().getCourseID()));
		return "";
	}
	
	public String addCourseSetup()
	{
		setCourseOperation("Add");
		setCourseRenderInquiry(false);
    	setCourseRenderAddUpdate(true);
    	
    	Course course = new Course();
    	setSelectedCourse(course);
    	
		return "";
	}		
	
	public String updateCourse()
	{
		setCourseOperation("Update");
		saveCourse();
		setCourseRenderInquiry(true);
    	setCourseRenderAddUpdate(false);
		return "";
	}
	
	public String addCourse()
	{
		setCourseOperation("Add");
		saveCourse();
		setCourseRenderInquiry(true);
    	setCourseRenderAddUpdate(false);
		return "";
	}		 
	
	public String cancelAddUpdateCourse()
	{
		this.setCourseRenderInquiry(true);
    	this.setCourseRenderAddUpdate(false);
    	
		return "";
	}
	
	public String deleteCourse()
	{
		logger.info(getTempUserName() + " is deleting a course");
		
		try
		{
			courseDAO.deleteCourse(this.getSelectedCourse().getCourseID());
			this.deleteCourseTeesFromDB(this.getSelectedCourse().getCourseID());
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"course and course tees successfully removed",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);  
	        
	        logger.info(getTempUserName() + " course and course tees successfully deleted");
		}
		catch (Exception e)
		{
			logger.error("Exception when deleting a course: " + e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when deleting a course: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
			
		return "";
	}
	
	public String selectPlayerTeePrefRowAjax(SelectEvent<PlayerTeePreference> event)
	{
		logger.info("User clicked on a row in Player Tee Preference list");
		
		PlayerTeePreference item = event.getObject();
		
		this.setSelectedPlayerTeePreference(item);
		this.setDisablePlayerTeePrefDialogButton(false);
				
		return "";
	}	
	
	public String setUpForPlayerTeePrefUpdate()
	{
		return "";
	}
	
	public String updatePrefs() throws Exception
	{
		logger.info("entering updatePrefs");
		
		for (int i = 0; i < this.getCourseTeesList().size(); i++) 
		{
			DynamoCourseTee courseTee = this.getCourseTeesList().get(i);
			
			if (courseTee.getCourseID().equalsIgnoreCase(this.getSelectedPlayerTeePreference().getCourseID()))
			{
				if (this.getSelectedPlayerTeePreference().getTeeColor().equalsIgnoreCase(courseTee.getTeeColor()))
				{
					this.getSelectedPlayerTeePreference().setCourseTeeID(courseTee.getCourseTeeID());
					break;
				}
			}
		}
		
		this.updatePlayerTeePreference(this.getSelectedPlayerTeePreference());
		
		return "";
	}
	
	public String generateTeeBoxRows()
	{
		this.getNewCourseTeesList().clear();
		
		for (int i = 0; i < this.getCourseTeeBoxes(); i++) 
		{
			CourseTee courseTee = new CourseTee();
			courseTee.setCourseID(this.getSelectedCourse().getCourseID());
			this.getNewCourseTeesList().add(courseTee);			
		}
		return "";
	}
	
	private void saveCourse() 
	{
		logger.info(getTempUserName() + " inside saveCourse()");	
		
		try
		{
			if (this.getCourseOperation().equalsIgnoreCase("Add"))
			{
				this.getCourseDAO().addCourse(this.getSelectedCourse());
				for (int i = 0; i < this.getNewCourseTeesList().size(); i++) 
				{
					CourseTee ct = this.getNewCourseTeesList().get(i);
					ct.setCourseID(this.getSelectedCourse().getCourseID());
					ct.setCoursePar(this.getSelectedCourse().getCoursePar());
					this.getCourseTeeDAO().addCourseTee(ct);
				}
			}
			
			if (this.getCourseOperation().equalsIgnoreCase("Update"))
			{
				this.getCourseDAO().updateCourse(this.getSelectedCourse());
			}	
			
		}
		catch (Exception e)
		{
			logger.error("Exception when saving course: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when saving course: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		logger.info(getTempUserName() + " exiting saveCourse");
	}

	public Course getCourseByCourseID(String id2) 
	{
		return courseDAO.getCourseByCourseID(id2);
	}

	public String selectCourseAjax(SelectEvent<Course> event)
	{
		logger.info(getTempUserName() + " clicked on a row in Course list");
		
		Course item = event.getObject();
		this.setSelectedCourse(item);
		this.setDisableDeleteCourse(false);		
		
		return "";
	}	
		
	public String selectTeeTimeRowAjax(SelectEvent<TeeTime> event)
	{
		logger.info(getTempUserName() + " User clicked on a row in Tee Time list");
		
		TeeTime item = event.getObject();
		
		setSelectedTeeTime(item);
		setDisableDeleteTeeTime(false); //if they've picked one, then they can delete it
				
		return "";
	}	
	
	public void loadTeeTimeList(DynamoClients dynamoClients, DynamoGroup defaultGroup) throws Exception
	{
		logger.info("entering loadTeeTimeList");
		teeTimeDAO = new TeeTimeDAO(dynamoClients);
		teeTimeDAO.readTeeTimesFromDB(defaultGroup, gameDAO.getFullGamesMap());			
		logger.info("Tee Times read in. List size = " + this.getFullTeeTimesList().size());			
	}
	
	public void loadPlayerMoneyList(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadPlayerMoneyList");
		playerMoneyDAO = new PlayerMoneyDAO(dynamoClients);
		playerMoneyDAO.readPlayerMoneyFromDB();	
		
		Map<String,PlayerMoney> tempMap = new HashMap<>();
		
		for (int i = 0; i < playerMoneyDAO.getPlayerMoneyList().size(); i++) 
		{
			PlayerMoney pm = playerMoneyDAO.getPlayerMoneyList().get(i);			
			DynamoGame game = gameDAO.getGameByGameID(pm.getGameID());
			DynamoPlayer player = getFullPlayersMapByPlayerID().get(pm.getPlayerID());	
			pm.setGame(game);
			pm.setPlayer(player);
			tempMap.put(pm.getPlayerMoneyID(), pm);
		}
		
		playerMoneyDAO.getPlayerMoneyMap().clear();
		playerMoneyDAO.setPlayerMoneyMap(tempMap);
		
		Collection<PlayerMoney> values = playerMoneyDAO.getPlayerMoneyMap().values();
		playerMoneyDAO.setPlayerMoneyList(new ArrayList<>(values));		
		
		logger.info("Player Money read in. List size = " + this.getPlayerMoneyList().size());			
	}
	
	public void loadFullPlayerList(DynamoClients dynamoClients) throws Exception 
	{
		logger.info("entering loadFullPlayerList");
		playerDAO = new PlayerDAO(dynamoClients);
		playerDAO.readPlayersFromDB();			
		golfUsersDAO.readAllUsersFromDB();
						
		Map<String, GolfUser> golfUsersMap = golfUsersDAO.getFullUserMap();
		
		for (int i = 0; i < this.getFullPlayerList().size(); i++) 
		{
			DynamoPlayer tempPlayer = this.getFullPlayerList().get(i);			
			
			//get the role for this player
			GolfUser gu = golfUsersMap.get(tempPlayer.getUsername());
			
			if (gu == null)
			{
				logger.error("golfuser is null for player: " + tempPlayer.getUsername());
			}
			else
			{
				String userRole = gu.getUserRole();
				tempPlayer.setRole(userRole);
			}			
			
		}
			
		logger.info("Players read in. List size = " + this.getFullPlayerList().size());
	}

	public void loadFullPlayerTeePreferencesList(DynamoClients dynamoClients) throws Exception 
	{
		logger.info("entering loadFullPlayerTeePreferencesList");
		playerTeePreferencesDAO = new PlayerTeePreferenceDAO(dynamoClients, this);
		playerTeePreferencesDAO.readPlayerTeePreferencesFromDB(this.getDefaultGroup());
		
		Map<String,PlayerTeePreference> tempMap = new HashMap<>();
		
		for (int i = 0; i < playerTeePreferencesDAO.getPlayerTeePreferencesList().size(); i++) 
		{
			PlayerTeePreference ptp = playerTeePreferencesDAO.getPlayerTeePreferencesList().get(i);
			DynamoCourseTee ct = getCourseTeesMap().get(ptp.getCourseTeeID());
	       	Course cs = getCoursesMap().get(ptp.getCourseID());
	   		DynamoPlayer player = getFullPlayersMapByPlayerID().get(ptp.getPlayerID());
	   		
	   		if (player == null)
	   		{
	   			logger.info("player is null when loading player tee preferences.  ptp player id = " + ptp.getPlayerID() 
	   			  + " and full name is " + ptp.getPlayerFullName()); 
	   		}
	   		ptp.setTeeColor(ct.getTeeColor());       
	        ptp.setCourseName(cs.getCourseName());
	        ptp.setPlayerUserName(player.getUsername());
			ptp.setPlayerFullName(player.getFullName());	
			tempMap.put(ptp.getPlayerTeePreferenceID(), ptp);
		}
		
		playerTeePreferencesDAO.getPlayerTeePreferencesMap().clear();
		playerTeePreferencesDAO.setPlayerTeePreferencesMap(tempMap);
		
		Collection<PlayerTeePreference> values = playerTeePreferencesDAO.getPlayerTeePreferencesMap().values();
		playerTeePreferencesDAO.setPlayerTeePreferencesList(new ArrayList<>(values));
		
		Collections.sort(playerTeePreferencesDAO.getPlayerTeePreferencesList(), new Comparator<PlayerTeePreference>() 
		{
		   public int compare(PlayerTeePreference o1, PlayerTeePreference o2) 
		   {
		      return o1.getPlayerFullName().compareTo(o2.getPlayerFullName());
		   }
		});        	
		
		logger.info("Player Tee Preferences read in. List size = " + this.getFullPlayerTeePreferencesList().size());		
	}

	public void assignCourseToGame(DynamoGame dynamoGame)
	{
		Course course = courseDAO.getCoursesMap().get(dynamoGame.getCourseID());
		dynamoGame.setCourseName(course.getCourseName());
		
		List<SelectItem> courseTeeSelections = new ArrayList<>();
		
		List<DynamoCourseTee> sortedCourseTees = new ArrayList<>(getCourseTees());
		Collections.sort(sortedCourseTees, new CourseTeeComparator());
		
		for (int i = 0; i < sortedCourseTees.size(); i++) 
		{
			DynamoCourseTee courseTee = sortedCourseTees.get(i);
			if (courseTee.getCourseID().equalsIgnoreCase(dynamoGame.getCourseID()))
			{
				SelectItem selItem = new SelectItem();
				selItem.setLabel(courseTee.getTeeColor() + " (" + courseTee.getTotalYardage() + " yds)");
				selItem.setValue(courseTee.getCourseTeeID());
				courseTeeSelections.add(selItem);
			}
		}
		
		dynamoGame.setTeeSelections(courseTeeSelections);
	}
	
	public static class CourseTeeComparator implements Comparator<DynamoCourseTee>
	{
		public int compare(DynamoCourseTee courseTee1, DynamoCourseTee courseTee2)
		{
			return courseTee1.getCourseRating().compareTo(courseTee2.getCourseRating());
		}		
	}
	
	
	
	
	public void onLoadEmailGroup() 
	{
		logger.info(getTempUserName() + " In onLoadEmailGroup GolfMain.java");
		
		groupEmailDisclaimer = "The note you compose here will go to the entire group so please use wisely!  Thank you";
		groupEmailSender = getTempUserName();
	}		
	
	public void setGameSpecificTeeTimeList(DynamoGame game)
	{
		List<TeeTime> gameTeeTimes = this.getFullTeeTimesList().stream()
			.filter(p -> p.getGameID().equalsIgnoreCase(game.getGameID()))
			.collect(Collectors.mapping(p -> p, Collectors.toList()));
		
		Collections.sort(gameTeeTimes, new Comparator<TeeTime>() 
		{
		   public int compare(TeeTime o1, TeeTime o2) 
		   {
			   Integer o1Int = o1.getPlayGroupNumber();
			   Integer o2Int = o2.getPlayGroupNumber();
		      return o1Int.compareTo(o2Int);
		   }
		});		
		
		this.setGameSpecificTeeTimesList(gameTeeTimes);
	}
		
	public void setRecommendations(Integer inputPlayers)
	{		
		switch (inputPlayers) 
		{
			case 4:
				
				recommendedPurseAmount = new BigDecimal(80);
				recommendedTotalTeams = 1;
				recommendedHowManyBalls = 1;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(40);
				recommendedTeamPot = new BigDecimal(40);
				recommendedGameFee = new BigDecimal(0.00);
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00";
				}
				else
				{
					recommendedTeeTimesString = "9:30";
				}
				
				break;	
		
			case 5:
			
				recommendedPurseAmount = new BigDecimal(100);
				recommendedTotalTeams = 1;
				recommendedHowManyBalls = 1;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(50);
				recommendedTeamPot = new BigDecimal(50);;
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40";
				}
					
				break;	
		
			case 6:
				
				recommendedPurseAmount = new BigDecimal(120);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(30);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(60);
				recommendedTeamPot = new BigDecimal(60);;
				recommendedGameFee = new BigDecimal(0.00);
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40";
				}
				
				break;	
			
			case 7: //skins only 7th player
				
				recommendedPurseAmount = new BigDecimal(128);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(30);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(58);
				recommendedTeamPot = new BigDecimal(60);;
				recommendedGameFee = new BigDecimal(0.00);
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40";
				}
				
				break;	
				
			case 8:
				
				recommendedPurseAmount = new BigDecimal(160);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(70);
				recommendedTeamPot = new BigDecimal(80);
				recommendedGameFee = new BigDecimal(10.00);
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40";
				}
				break;
				
			case 9:
				recommendedPurseAmount = new BigDecimal(180);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(45);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(78);
				recommendedTeamPot = new BigDecimal(90);
				recommendedGameFee = new BigDecimal(12.00);
				
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50";
				}
				break;
				
			case 10:
				recommendedPurseAmount = new BigDecimal(200);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(35);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal("0.00");
				recommendedSkinsPot = new BigDecimal(80);
				recommendedTeamPot = new BigDecimal(105);
				recommendedGameFee = new BigDecimal("15.00");
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50";
				}
				break;
				
			case 11:  //skins only 11th player
				
				recommendedPurseAmount = new BigDecimal(210);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(35);
				recommendedIndividualGrossPrize = new BigDecimal("0.00");
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(90);
				recommendedTeamPot = new BigDecimal(105);
				recommendedGameFee = new BigDecimal(15.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50";
				}
				break;
				
			case 12:
				
				recommendedPurseAmount = new BigDecimal(240);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(56);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(112);
				recommendedTeamPot = new BigDecimal(112);
				recommendedGameFee = new BigDecimal(16.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50";
				}
				break;
				
			case 13: //skins only 13th player
				
				recommendedPurseAmount = new BigDecimal(250);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(64);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(106);
				recommendedTeamPot = new BigDecimal(128);
				recommendedGameFee = new BigDecimal(16.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				}
				break;
				
			case 14:
				
				recommendedPurseAmount = new BigDecimal(280);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(35);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(124);
				recommendedTeamPot = new BigDecimal(140);
				recommendedGameFee = new BigDecimal(16.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				}
				break;
				
			case 15:
				
				recommendedPurseAmount = new BigDecimal(300);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(55);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(107);
				recommendedTeamPot = new BigDecimal(165);
				recommendedGameFee = new BigDecimal(18.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				}
				break;
				
			case 16:
				
				recommendedPurseAmount = new BigDecimal(320);
				recommendedTotalTeams = 4;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(50);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(150);
				recommendedTeamPot = new BigDecimal(150);
				recommendedGameFee = new BigDecimal(20.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				}
				break;	

			case 17: //skins only 17th player
				
				recommendedPurseAmount = new BigDecimal(330);
				recommendedTotalTeams = 4;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(55);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(145);
				recommendedTeamPot = new BigDecimal(165);
				recommendedGameFee = new BigDecimal(20.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30 10:40";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				}
				break;
				
			case 18:
				
				recommendedPurseAmount = new BigDecimal(360);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(105);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(150);
				recommendedTeamPot = new BigDecimal(210);
				recommendedGameFee = new BigDecimal(22.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30 10:40";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				}
				break;
				
			case 19: //skins only 19th player
				
				recommendedPurseAmount = new BigDecimal(368);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(105);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(158);
				recommendedTeamPot = new BigDecimal(210);
				recommendedGameFee = new BigDecimal(22.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30 10:40";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				}
				break;
				
			case 20:
				
				recommendedPurseAmount = new BigDecimal(400);
				recommendedTotalTeams = 5;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(75);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(150);
				recommendedTeamPot = new BigDecimal(225);
				recommendedGameFee = new BigDecimal(25.00);
				if (Utils.isWinterMonth())
				{
					recommendedTeeTimesString = "10:00 10:10 10:20 10:30 10:40";
				}
				else
				{
					recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				}
				break;
			
			case 21:
				
				recommendedPurseAmount = new BigDecimal(420);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(58);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(163);
				recommendedTeamPot = new BigDecimal(232);
				recommendedGameFee = new BigDecimal(25.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 22: //skins only 22nd player
				recommendedPurseAmount = new BigDecimal(428);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(58);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(171);
				recommendedTeamPot = new BigDecimal(232);
				recommendedGameFee = new BigDecimal(25.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 23: //skins only 22nd and 23rd players
				
				recommendedPurseAmount = new BigDecimal(436);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(58);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(174);
				recommendedTeamPot = new BigDecimal(232);
				recommendedGameFee = new BigDecimal(30.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 24:
				recommendedPurseAmount = new BigDecimal(480);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(90);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(180);
				recommendedTeamPot = new BigDecimal(270);
				recommendedGameFee = new BigDecimal(30.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 25:
				recommendedPurseAmount = new BigDecimal(490);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(90);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(190);
				recommendedTeamPot = new BigDecimal(270);
				recommendedGameFee = new BigDecimal(30.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;
				
			case 26:
				recommendedPurseAmount = new BigDecimal(500);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(90);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(200);
				recommendedTeamPot = new BigDecimal(270);
				recommendedGameFee = new BigDecimal(30.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;	
	
			case 27:
				recommendedPurseAmount = new BigDecimal(540);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 5;
				recommendedEachBallWorth = new BigDecimal(72);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(180);
				recommendedTeamPot = new BigDecimal(360);
				recommendedGameFee = new BigDecimal(32.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;				
					
			case 28:
				recommendedPurseAmount = new BigDecimal(560);
				recommendedTotalTeams = 7;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(70);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(248);
				recommendedTeamPot = new BigDecimal(280);
				recommendedGameFee = new BigDecimal(32.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;	
			
			case 29:
				recommendedPurseAmount = new BigDecimal(570);
				recommendedTotalTeams = 7;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(75);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(235);
				recommendedTeamPot = new BigDecimal(300);
				recommendedGameFee = new BigDecimal(35.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;				
			
			case 30:
				recommendedPurseAmount = new BigDecimal(600);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(110);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(235);
				recommendedTeamPot = new BigDecimal(330);
				recommendedGameFee = new BigDecimal(35.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			case 31:
				recommendedPurseAmount = new BigDecimal(610);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(110);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(245);
				recommendedTeamPot = new BigDecimal(330);
				recommendedGameFee = new BigDecimal(35.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			case 32:
				recommendedPurseAmount = new BigDecimal(640);
				recommendedTotalTeams = 8;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(120);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(245);
				recommendedTeamPot = new BigDecimal(360);
				recommendedGameFee = new BigDecimal(35.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			default:				
				break;
		}
		
	}
	
	public String sendGroupEmail()
	{
		logger.info("User clicked sendGroupEmail");
		
		String subjectLine = "Group Email";
		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		List<DynamoPlayer> fullPlayerList = this.getFullPlayerList();
			
		String senderName = getLoggedInPlayerName();
		String senderReplyEmail = getLoggedInPlayerEmail();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<H3>Email message from: " + senderName + "</H3>");	
		sb.append(NEWLINE);
		
		sb.append("<H3>Reply directly to sender at: " + senderReplyEmail + "</H3>");	
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		
		sb.append("<H3>" + groupEmailMessage + "</H3>");	
		
		logger.info("Group email message about to be sent: " + sb.toString());		
		
		emailRecipients = Utils.setEmailFullRecipientList(fullPlayerList);
		if (emailRecipients.size() >= 100)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"100 or more recipients on Email list - google will not send it, preventing before trying",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			//emailRecipients.add("paulslomkowski@yahoo.com"); //to just me for testing
			SAMailUtility.sendEmail(subjectLine, sb.toString(), emailRecipients, true); //last param means use jsf
		}		
		
		logger.info("User sent email to entire group successfully");
		
		return "";
	}
	
 	public String proceedToSelectGame() 
	{
		logger.info("User clicked proceed from Main screen; sending them to game list/add screen");
		
		return "success";
	} 	
  	
	public List<SelectItem> getTotalPlayersSelections() {
		return totalPlayersSelections;
	}

	public void setTotalPlayersSelections(List<SelectItem> totalPlayersSelections) {
		this.totalPlayersSelections = totalPlayersSelections;
	}

	public List<SelectItem> getTotalTeamsSelections() {
		return totalTeamsSelections;
	}

	public void setTotalTeamsSelections(List<SelectItem> totalTeamsSelections) {
		this.totalTeamsSelections = totalTeamsSelections;
	}

	public List<SelectItem> getHowManyBallsSelections() {
		return howManyBallsSelections;
	}

	public void setHowManyBallsSelections(List<SelectItem> howManyBallsSelections) {
		this.howManyBallsSelections = howManyBallsSelections;
	}

	public boolean isDisableProceedToSelectGame() {
		return disableProceedToSelectGame;
	}

	public void setDisableProceedToSelectGame(boolean disableProceedToSelectGame) {
		this.disableProceedToSelectGame = disableProceedToSelectGame;
	}

	public boolean isDisableProceedToSelectPlayers() {
		return disableProceedToSelectPlayers;
	}

	public void setDisableProceedToSelectPlayers(boolean disableProceedToSelectPlayers) {
		this.disableProceedToSelectPlayers = disableProceedToSelectPlayers;
	}

	public boolean isDisableDeleteSelectedPlayerRound() {
		return disableDeleteSelectedPlayerRound;
	}

	public void setDisableDeleteSelectedPlayerRound(boolean disableDeleteSelectedPlayerRound) {
		this.disableDeleteSelectedPlayerRound = disableDeleteSelectedPlayerRound;
	}

	public BigDecimal getRecommendedPurseAmount() {
		return recommendedPurseAmount;
	}

	public void setRecommendedPurseAmount(BigDecimal recommendedPurseAmount) {
		this.recommendedPurseAmount = recommendedPurseAmount;
	}

	public Integer getRecommendedTotalTeams() {
		return recommendedTotalTeams;
	}

	public void setRecommendedTotalTeams(Integer recommendedTotalTeams) {
		this.recommendedTotalTeams = recommendedTotalTeams;
	}

	public Integer getRecommendedHowManyBalls() {
		return recommendedHowManyBalls;
	}

	public void setRecommendedHowManyBalls(Integer recommendedHowManyBalls) {
		this.recommendedHowManyBalls = recommendedHowManyBalls;
	}

	public BigDecimal getRecommendedEachBallWorth() {
		return recommendedEachBallWorth;
	}

	public void setRecommendedEachBallWorth(BigDecimal recommendedEachBallWorth) {
		this.recommendedEachBallWorth = recommendedEachBallWorth;
	}

	public BigDecimal getRecommendedIndividualGrossPrize() {
		return recommendedIndividualGrossPrize;
	}

	public void setRecommendedIndividualGrossPrize(BigDecimal recommendedIndividualGrossPrize) {
		this.recommendedIndividualGrossPrize = recommendedIndividualGrossPrize;
	}

	public BigDecimal getRecommendedIndividualNetPrize() {
		return recommendedIndividualNetPrize;
	}

	public void setRecommendedIndividualNetPrize(BigDecimal recommendedIndividualNetPrize) {
		this.recommendedIndividualNetPrize = recommendedIndividualNetPrize;
	}

	public BigDecimal getRecommendedSkinsPot() {
		return recommendedSkinsPot;
	}

	public void setRecommendedSkinsPot(BigDecimal recommendedSkinsPot) {
		this.recommendedSkinsPot = recommendedSkinsPot;
	}

	public BigDecimal getRecommendedSuggestedSkinsPot() {
		return recommendedSuggestedSkinsPot;
	}

	public void setRecommendedSuggestedSkinsPot(BigDecimal recommendedSuggestedSkinsPot) {
		this.recommendedSuggestedSkinsPot = recommendedSuggestedSkinsPot;
	}

	public BigDecimal getRecommendedTeamPot() {
		return recommendedTeamPot;
	}

	public void setRecommendedTeamPot(BigDecimal recommendedTeamPot) {
		this.recommendedTeamPot = recommendedTeamPot;
	}

	public String getRecommendedTeeTimesString() {
		return recommendedTeeTimesString;
	}

	public void setRecommendedTeeTimesString(String recommendedTeeTimesString) {
		this.recommendedTeeTimesString = recommendedTeeTimesString;
	}

	public static String getRecommendedPlayTheBallMethod() {
		return recommendedPlayTheBallMethod;
	}

	public void setRecommendedPlayTheBallMethod(String recommendedPlayTheBallMethod) {
		GolfMain.recommendedPlayTheBallMethod = recommendedPlayTheBallMethod;
	}

	public List<SelectItem> getHoleSelections() {
		return holeSelections;
	}

	public void setHoleSelections(List<SelectItem> holeSelections) {
		this.holeSelections = holeSelections;
	}

	public String getSignedOnUserName() 
	{
		String username = "";
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) 
		{
		   username = ((UserDetails)principal).getUsername();
		} 
		else 
		{
		   username = principal.toString();
		}
		
		if (username != null)
		{
			username = username.toLowerCase();
		}
		return username;
	}

	public List<SelectItem> getPlayGroupSelections() 
	{
		return playGroupSelections;
	}

	public void setPlayGroupSelections(List<SelectItem> playGroupSelections) 
	{
		this.playGroupSelections = playGroupSelections;
	}

	public List<TeeTime> getFullTeeTimesList()
	{
		return teeTimeDAO.getFullTeeTimesList();
	}

	public Map<String, DynamoGame> getFullGamesMap()
	{
		return gameDAO.getFullGamesMap();
	}

	public List<DynamoGame> getFullGameList()
	{
		return gameDAO.getFullGameList();
	}
	
	public List<Course> getCoursesList() 
	{
		return courseDAO.getCoursesList();
	}

	public List<DynamoPlayer> getFullPlayerList() 
	{
		return playerDAO.getFullPlayerList();
	}
	
	public List<DynamoPlayer> getActivePlayerList() 
	{
		List<DynamoPlayer> sortedList = new ArrayList<>(playerDAO.getActivePlayerList());
	    sortedList.sort(new PlayerComparatorByLastName());
	    
	    return sortedList;
	}

	public Map<String, DynamoPlayer> getFullPlayersMapByPlayerID() 
	{
		return playerDAO.getFullPlayersMapByPlayerID();
	}

	public Map<String, DynamoPlayer> getFullPlayersMapByUserName() 
	{
		return playerDAO.getFullPlayersMapByUserName();
	}
	
	public Map<String, Course> getCoursesMap() 
	{
		return courseDAO.getCoursesMap();
	}

	public Map<String, TeeTime> getFullTeeTimesMap()
	{
		return teeTimeDAO.getFullTeeTimesMap();
	}
	
	public Map<String, DynamoCourseTee> getCourseTeesMap()
	{
		return courseTeeDAO.getCourseTeesMap();
	}

	public List<DynamoCourseTee> getCourseTees()
	{
		return courseTeeDAO.getCourseTeesList();
	}

	public List<PlayerTeePreference> getFullPlayerTeePreferencesList() 
	{
		return playerTeePreferencesDAO.getPlayerTeePreferencesList();
	}
	
	public List<Round> getFullRoundsList() 
	{
		return roundDAO.getFullRoundsList();
	}

	public Map<String, PlayerTeePreference> getFullPlayerTeePreferencesMap() 
	{
		return playerTeePreferencesDAO.getPlayerTeePreferencesMap();
	}
	
	public List<SelectItem> getScoreSelections() {
		return scoreSelections;
	}

	public void setScoreSelections(List<SelectItem> scoreSelections) {
		this.scoreSelections = scoreSelections;
	}

	public String getGroupEmailMessage() {
		return groupEmailMessage;
	}

	public void setGroupEmailMessage(String groupEmailMessage) {
		this.groupEmailMessage = groupEmailMessage;
	}

	public String getGroupEmailSender() {
		return groupEmailSender;
	}

	public void setGroupEmailSender(String groupEmailSender) {
		this.groupEmailSender = groupEmailSender;
	}

	public String getGroupEmailDisclaimer() {
		return groupEmailDisclaimer;
	}

	public void setGroupEmailDisclaimer(String groupEmailDisclaimer) {
		this.groupEmailDisclaimer = groupEmailDisclaimer;
	}
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}


	public static String getRecommendedGameNote() {
		return recommendedGameNote;
	}

	public static void setRecommendedGameNote(String recommendedGameNote) {
		GolfMain.recommendedGameNote = recommendedGameNote;
	}

	public String addGame(DynamoGame dynamoGame) throws Exception 
	{
		return gameDAO.addGame(dynamoGame);
	}

	public void deleteGame(String gameID) throws Exception 
	{
		gameDAO.deleteGame(gameID);		
	}

	public void updateGame(DynamoGame game) throws Exception 
	{
		gameDAO.updateGame(game);		
	}

	public List<DynamoGame> getFutureGames() 
	{
		return gameDAO.getFutureGames();
	}

	public List<DynamoGame> getAvailableGamesByPlayerID(String playerID) 
	{
		List<DynamoGame> gameList = gameDAO.getAvailableGames(playerID);
		List<DynamoGame> tempList = new ArrayList<>();
		
		for (int i = 0; i < gameList.size(); i++) 
    	{
			DynamoGame dynamoGame = gameList.get(i);
			Round rd = this.getRoundByGameandPlayer(dynamoGame.getGameID(), playerID);
			
			Integer spotsTaken = this.countRoundsForGameFromDB(dynamoGame);
			Integer spotsAvailable = dynamoGame.getFieldSize() - spotsTaken;
			dynamoGame.setSpotsAvailable(spotsAvailable);
			
			if (rd == null)
			{
				dynamoGame.setRenderSignUp(true);
				dynamoGame.setRenderWithdraw(false);
				dynamoGame.setSelectedCourseTeeID(getTeePreference(playerID, dynamoGame.getCourseID()));
				this.assignCourseToGame(dynamoGame);
			}
			else
			{
				dynamoGame.setRenderSignUp(false);
				dynamoGame.setRenderWithdraw(true);
				dynamoGame.setSelectedCourseTeeID(rd.getCourseTeeID());
			}
	
			tempList.add(dynamoGame);
		} 
    	
    	Collections.sort(tempList, new Comparator<DynamoGame>() 
		{
		   public int compare(DynamoGame o1, DynamoGame o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
		
		return tempList;
	}

	public String getTeePreference(String playerID, String courseID)
	{
		PlayerTeePreference ptp = playerTeePreferencesDAO.getPlayerTeePreference(playerID, courseID);
		return ptp.getPlayerTeePreferenceID();
	}

	public DynamoGame getGameByGameID(String gameID) 
	{
		return gameDAO.getGameByGameID(gameID);
	}
	
	public DynamoPlayer getPlayerByPlayerID(String playerID)
	{
		return playerDAO.getFullPlayersMapByPlayerID().get(playerID);
	}
	
	public DynamoPlayer getPlayerByUserName(String username)
	{
		return playerDAO.getFullPlayersMapByUserName().get(username);
	}
		
	public String addPlayer(DynamoPlayer dynamoPlayer)  throws Exception  
	{
		return playerDAO.addPlayer(dynamoPlayer);
	}
	
	public void updatePlayer(DynamoPlayer player)  throws Exception 
	{
		playerDAO.updatePlayer(player);
	}
	
	public List<Round> getRoundsForGame(DynamoGame dynamoGame) 
	{
		return roundDAO.getRoundsForGame(dynamoGame);
	}
	
	public String addRound(Round round) throws Exception 
	{
		return roundDAO.addRound(round);
	}
	
	public void updateRound(Round round) throws Exception 
	{
		roundDAO.updateRound(round);
	}
	
	public void deleteRoundFromDB(String roundID)  throws Exception 
	{
		roundDAO.deleteRoundFromDB(roundID);
	}

	public void deleteRoundsFromDB(String gameID) 
	{
		roundDAO.deleteRoundsFromDB(gameID);		
	}

	public Round getRoundByGameandPlayer(String gameID, String playerID) 
	{
		return roundDAO.getRoundByGameandPlayer(gameID, playerID);
	}

	public Integer countRoundsForGameFromDB(DynamoGame gm) 
	{
		return roundDAO.countRoundsForGameFromDB(gm);
	}

	public void updateRoundHandicap(DynamoGame dynamoGame, String playerID, BigDecimal newRoundHandicap) throws Exception 
	{
		roundDAO.updateRoundHandicap(dynamoGame, playerID, newRoundHandicap);		
	}

	public void updateRoundTeamNumber(DynamoGame dynamoGame, String playerID, int teamNumber) throws Exception 
	{
		roundDAO.updateRoundTeamNumber(dynamoGame, playerID, teamNumber);		
	}

	public void addPlayerTeePreference(PlayerTeePreference ptp) throws Exception 
	{
		playerTeePreferencesDAO.addPlayerTeePreference(ptp);		
	}

	public void updatePlayerTeePreference(PlayerTeePreference ptp) throws Exception 
	{
		playerTeePreferencesDAO.updatePlayerTeePreference(ptp);				
	}

	public PlayerTeePreference getPlayerTeePreference(String playerID, String courseID) 
	{
		return playerTeePreferencesDAO.getPlayerTeePreference(playerID, courseID);
	}

	public List<DynamoCourseTee> getCourseTeesList()
	{
		return courseTeeDAO.getCourseTeesList();
	}

	public void deleteCourseTeesFromDB(String courseID) 
	{
		courseTeeDAO.deleteCourseTeesForCourseFromDB(courseID);		
	}
	
	public List<TeeTime> getTeeTimesByGame(DynamoGame selectedGame) 
	{
		return teeTimeDAO.getTeeTimesByGame(selectedGame);
	}

	public void deleteTeeTimeFromDB(String string) 
	{
		teeTimeDAO.deleteTeeTimeFromDB(string);		
	}

	public void addTeeTimes(String newGameID, String teeTimesString, Date gameDate, String courseName) throws Exception 
	{
		teeTimeDAO.addTeeTimes(newGameID, teeTimesString, gameDate, courseName);		
	}

	public void deleteTeeTimesForGameFromDB(String gameID) 
	{
		teeTimeDAO.deleteTeeTimesForGameFromDB(gameID);		
	}

	public void deletePlayerMoneyFromDB(String gameID) throws Exception
	{
		playerMoneyDAO.deletePlayerMoneyFromDB(gameID);		
	}

	public List<PlayerMoney> getPlayerMoneyByGame(DynamoGame dynamoGame) 
	{
		return playerMoneyDAO.getPlayerMoneyByGame(dynamoGame);
	}

	public void addPlayerMoney(PlayerMoney pm) throws Exception 
	{
		playerMoneyDAO.addPlayerMoney(pm);		
	}

	public List<PlayerMoney> getPlayerMoneyByPlayer(DynamoPlayer dynamoPlayer) 
	{
		return playerMoneyDAO.getPlayerMoneyByPlayer(dynamoPlayer);
	}
	
	public List<PlayerMoney> getPlayerMoneyList()
	{
		return playerMoneyDAO.getPlayerMoneyList();
	}
	
	public List<DynamoGroup> getGroupsList()
	{
		return groupDAO.getGroupsList();
	}

	public DynamoGroup getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(DynamoGroup defaultGroup) {
		this.defaultGroup = defaultGroup;
	}

	public List<String> getAdminUserList() 
	{
		return golfUsersDAO.getAdminUserList();
	}

	public GolfUser getGolfUser(String whoIsThis) 
	{
		return golfUsersDAO.getGolfUser(whoIsThis);
	}

	public String changePassword()
	{
		String errorMsg = "";
		
		try
		{
			String whoIsThis = Utils.getLoggedInUserName();
		
			this.updateUser(whoIsThis, this.getNewPassword(), "USER");
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Password successfully changed",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
			
		}
		catch (Exception e)
		{
			errorMsg = "Unable to change password. " + e.getMessage();
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorMsg,null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		return "";
	}
	
	public void updateUser(String whoIsThis, String newPassword, String userrole) throws Exception 
	{
		GolfUser gu = new GolfUser();
		gu.setPassword(newPassword);
		gu.setUserName(whoIsThis);
		gu.setUserRole(userrole);
		golfUsersDAO.updateUser(gu);		
	}

	public void addUser(String username, String password, String userrole) throws Exception 
	{
		GolfUser gu = new GolfUser();
		gu.setPassword(password);
		gu.setUserName(username);
		gu.setUserRole(userrole);
		golfUsersDAO.addUser(gu, username); //default their password to their username		
	}

	public void updateRole(GolfUser gu) throws Exception 
	{
		golfUsersDAO.updateRole(gu); 		
	}

	public String getLoggedInPlayerName() 
	{
		//assign who the logged in player is using their login username
		logger.info("entering getLoggedInPlayerName()");
	
		String tempUserName = getTempUserName();
		GolfUser gu = getGolfUser(tempUserName);
		
		if (gu != null && gu.getUserName() != null)
		{
			DynamoPlayer tempPlayer = getFullPlayersMapByUserName().get(gu.getUserName());			
			if (tempPlayer != null)
			{
				this.setLoggedInPlayerName(tempPlayer.getFullName());
			}
			else
			{
				logger.error("unable to determine who logged in player is - this could be a problem!");
			}
		}
		else
		{
			logger.error("unable to determine who logged in player is - this could be a problem!");				
		}
		
		logger.info("currently logged in user is: " + loggedInPlayerName);		
		
		return loggedInPlayerName;
	}

	public void setLoggedInPlayerName(String loggedInPlayerName) 
	{
		this.loggedInPlayerName = loggedInPlayerName;
	}

	public String getLoggedInPlayerEmail() 
	{
		//assign who the logged in player is using their login username
		String tempUserName = getTempUserName();
		GolfUser gu = getGolfUser(tempUserName);
			
		if (gu != null && gu.getUserName() != null)
		{
			DynamoPlayer tempPlayer = getFullPlayersMapByUserName().get(gu.getUserName());			
			if (tempPlayer != null)
			{
				this.setLoggedInPlayerEmail(tempPlayer.getEmailAddress());
			}
			else
			{
				logger.error("unable to determine who logged in player is - this could be a problem!");
			}
		}
		else
		{
			logger.error("unable to determine who logged in player is - this could be a problem!");				
		}
				
		logger.info("currently logged in user's email is: " + loggedInPlayerEmail);		
		
		return loggedInPlayerEmail;
	}
	
	public void setLoggedInPlayerEmail(String loggedInPlayerEmail) 
	{
		this.loggedInPlayerEmail = loggedInPlayerEmail;
	}

	public ArrayList<String> getEmailRecipients() {
		return emailRecipients;
	}

	public void setEmailRecipients(ArrayList<String> emailRecipients) {
		this.emailRecipients = emailRecipients;
	}

	public GameDAO getGameDAO() {
		return gameDAO;
	}

	public void setGameDAO(GameDAO gameDAO) {
		this.gameDAO = gameDAO;
	}

	public GolfUsersDAO getGolfUsersDAO() {
		return golfUsersDAO;
	}

	public void setGolfUsersDAO(GolfUsersDAO golfUsersDAO) {
		this.golfUsersDAO = golfUsersDAO;
	}

	public RoundDAO getRoundDAO() {
		return roundDAO;
	}

	public void setRoundDAO(RoundDAO roundDAO) {
		this.roundDAO = roundDAO;
	}

	public TeeTimeDAO getTeeTimeDAO() {
		return teeTimeDAO;
	}

	public void setTeeTimeDAO(TeeTimeDAO teeTimeDAO) {
		this.teeTimeDAO = teeTimeDAO;
	}

	public CourseDAO getCourseDAO() {
		return courseDAO;
	}

	public void setCourseDAO(CourseDAO courseDAO) {
		this.courseDAO = courseDAO;
	}

	public CourseTeeDAO getCourseTeeDAO() {
		return courseTeeDAO;
	}

	public void setCourseTeeDAO(CourseTeeDAO courseTeeDAO) {
		this.courseTeeDAO = courseTeeDAO;
	}

	public PlayerDAO getPlayerDAO() {
		return playerDAO;
	}

	public void setPlayerDAO(PlayerDAO playerDAO) {
		this.playerDAO = playerDAO;
	}

	public PlayerMoneyDAO getPlayerMoneyDAO() {
		return playerMoneyDAO;
	}

	public void setPlayerMoneyDAO(PlayerMoneyDAO playerMoneyDAO) {
		this.playerMoneyDAO = playerMoneyDAO;
	}

	public PlayerTeePreferenceDAO getPlayerTeePreferencesDAO() {
		return playerTeePreferencesDAO;
	}

	public void setPlayerTeePreferencesDAO(PlayerTeePreferenceDAO playerTeePreferencesDAO) {
		this.playerTeePreferencesDAO = playerTeePreferencesDAO;
	}

	public GroupDAO getGroupDAO() {
		return groupDAO;
	}

	public void setGroupDAO(GroupDAO groupDAO) {
		this.groupDAO = groupDAO;
	}

	public double getId() {
		return id;
	}

	public BigDecimal getRecommendedGameFee() {
		return recommendedGameFee;
	}

	public void setRecommendedGameFee(BigDecimal recommendedGameFee) {
		this.recommendedGameFee = recommendedGameFee;
	}

	public Group getSelectedGroup() {
		return selectedGroup;
	}

	public void setSelectedGroup(Group selectedGroup) {
		this.selectedGroup = selectedGroup;
	}

	public Course getSelectedCourse() {
		return selectedCourse;
	}

	public void setSelectedCourse(Course selectedCourse) {
		this.selectedCourse = selectedCourse;
	}

	public String getCourseOperation() {
		return courseOperation;
	}

	public void setCourseOperation(String courseOperation) {
		this.courseOperation = courseOperation;
	}

	public String getPlayersCourseCourseID() 
	{
		return courseDAO.getPlayersCourseCourseID();
	}

	public boolean isCourseRenderInputFields() {
		return courseRenderInputFields;
	}

	public void setCourseRenderInputFields(boolean courseRenderInputFields) {
		this.courseRenderInputFields = courseRenderInputFields;
	}

	public boolean isCourseRenderInquiry() {
		return courseRenderInquiry;
	}

	public void setCourseRenderInquiry(boolean courseRenderInquiry) {
		this.courseRenderInquiry = courseRenderInquiry;
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

	public String getTeeTimeOperation() {
		return teeTimeOperation;
	}

	public void setTeeTimeOperation(String teeTimeOperation) {
		this.teeTimeOperation = teeTimeOperation;
	}

	public List<TeeTime> getGameSpecificTeeTimesList() {
		return gameSpecificTeeTimesList;
	}

	public void setGameSpecificTeeTimesList(List<TeeTime> gameSpecificTeeTimesList) {
		this.gameSpecificTeeTimesList = gameSpecificTeeTimesList;
	}

	public boolean isTeeTimesRenderInquiry() {
		return teeTimesRenderInquiry;
	}

	public void setTeeTimesRenderInquiry(boolean teeTimesRenderInquiry) {
		this.teeTimesRenderInquiry = teeTimesRenderInquiry;
	}

	public boolean isTeeTimesRenderAddUpdateDelete() {
		return teeTimesRenderAddUpdateDelete;
	}

	public void setTeeTimesRenderAddUpdateDelete(boolean teeTimesRenderAddUpdateDelete) {
		this.teeTimesRenderAddUpdateDelete = teeTimesRenderAddUpdateDelete;
	}

	public boolean isDisableDeleteCourse() {
		return disableDeleteCourse;
	}

	public void setDisableDeleteCourse(boolean disableDeleteCourse) {
		this.disableDeleteCourse = disableDeleteCourse;
	}

	public boolean isCourseRenderAddUpdate() {
		return courseRenderAddUpdate;
	}

	public void setCourseRenderAddUpdate(boolean courseRenderAddUpdate) {
		this.courseRenderAddUpdate = courseRenderAddUpdate;
	}

	public Integer getCourseTeeBoxes() {
		return courseTeeBoxes;
	}

	public void setCourseTeeBoxes(Integer courseTeeBoxes) {
		this.courseTeeBoxes = courseTeeBoxes;
	}

	public List<CourseTee> getNewCourseTeesList() {
		return newCourseTeesList;
	}

	public void setNewCourseTeesList(List<CourseTee> newCourseTeesList) {
		this.newCourseTeesList = newCourseTeesList;
	}

	public List<DynamoCourseTee> getCourseSpecificCourseTeesList() {
		return courseSpecificCourseTeesList;
	}

	public void setCourseSpecificCourseTeesList(List<DynamoCourseTee> courseSpecificCourseTeesList) {
		this.courseSpecificCourseTeesList = courseSpecificCourseTeesList;
	}

	public void deletePlayer(DynamoPlayer dynamoPlayer) 
	{
		try 
		{
			playerDAO.deletePlayer(dynamoPlayer);
		} 
		catch (Exception e) 
		{
			logger.error("Error calling playerDAO.deletePlayer " +e.getMessage(), e);
		}		
	}

	public void deletePlayerTeePreferences(DynamoPlayer selectedPlayer) 
	{
		playerTeePreferencesDAO.deletePlayerTeePreferences(selectedPlayer);		
	}

	public void deletePlayerMoneyFromDB(DynamoPlayer selectedPlayer) 
	{
		playerMoneyDAO.deletePlayerMoneyFromDB(selectedPlayer);		
	}

	public void deleteGolfUser(String username)
	{
		try
		{
			golfUsersDAO.deleteUser(username);
		} 
		catch (Exception e) 
		{
			logger.error("Error calling playerDAO.deletePlayer " +e.getMessage(), e);
		}	
	}
	
	public PlayerTeePreference getSelectedPlayerTeePreference() {
		return selectedPlayerTeePreference;
	}

	public void setSelectedPlayerTeePreference(PlayerTeePreference selectedPlayerTeePreference) {
		this.selectedPlayerTeePreference = selectedPlayerTeePreference;
	}

	public boolean isDisablePlayerTeePrefDialogButton() {
		return disablePlayerTeePrefDialogButton;
	}

	public void setDisablePlayerTeePrefDialogButton(boolean disablePlayerTeePrefDialogButton) {
		this.disablePlayerTeePrefDialogButton = disablePlayerTeePrefDialogButton;
	}

	public List<DynamoPlayerTeePreference> getPlayerSpecificTeePreferencesList() 
	{
		return playerSpecificTeePreferencesList;
	}

	public void setPlayerSpecificTeePreferencesList(DynamoPlayer dynamoPlayer) 
	{
		List<DynamoPlayerTeePreference> tempList = playerTeePreferencesDAO.getPlayerSpecificTeePreferencesList(dynamoPlayer);
		
		for (int i = 0; i < tempList.size(); i++) 
		{
			DynamoPlayerTeePreference dpt = tempList.get(i);
			Course course = courseDAO.getCourseByCourseID(dpt.getCourseID());
			List<DynamoCourseTee> tempCourseTeesList = courseTeeDAO.getCourseSpecificCourseTeesListByCourseID(course.getCourseID());
			List<SelectItem> siList = new ArrayList<>();
			for (int j = 0; j < tempCourseTeesList.size(); j++) 
			{
				DynamoCourseTee dct = tempCourseTeesList.get(j);
				SelectItem si = new SelectItem();
				si.setLabel(dct.getTeeColor());
				si.setValue(dct.getCourseTeeID());
				siList.add(si);
			}
			dpt.setTeeSelections(siList);
		}
		
		this.playerSpecificTeePreferencesList = tempList;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

}

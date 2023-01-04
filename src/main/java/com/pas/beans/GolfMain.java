package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.faces.model.SelectItem;
import javax.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.dao.CourseDAO;
import com.pas.dao.CourseTeeDAO;
import com.pas.dao.GameDAO;
import com.pas.dao.PlayerDAO;
import com.pas.dao.PlayerTeePreferenceDAO;
import com.pas.dao.TeeTimeDAO;
import com.pas.dao.UsersAndAuthoritiesDAO;
import com.pas.util.BeanUtilJSF;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

@Named("pc_GolfMain")
@SessionScoped
public class GolfMain extends SpringBeanAutowiringSupport implements Serializable
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
			recommendedGameNote = "White tee players stay within 25 yds of Golds";
		}
		else
		{
			recommendedPlayTheBallMethod = "Down everywhere but up in bunkers";
		}
	}
	
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(GolfMain.class);		
	
	private List<SelectItem> totalPlayersSelections = new ArrayList<SelectItem>();
	private List<SelectItem> totalTeamsSelections = new ArrayList<SelectItem>();
	private List<SelectItem> howManyBallsSelections = new ArrayList<SelectItem>();
	private List<SelectItem> playGroupSelections = new ArrayList<SelectItem>();
	private List<SelectItem> holeSelections = new ArrayList<>();
	private List<SelectItem> scoreSelections = new ArrayList<>();
	
	private Map<Integer,Course> coursesMap = new HashMap<Integer,Course>();	
	private Map<Integer,CourseTee> courseTeesMap = new HashMap<Integer,CourseTee>();	
	private Map<Integer,Game> gamesMap = new HashMap<Integer,Game>();	
	private Map<Integer,TeeTime> teeTimesMap = new HashMap<Integer,TeeTime>();	
	private Map<Integer,Player> fullPlayerMap = new HashMap<>();
	private Map<String,Player> fullPlayerMapByUserName = new HashMap<>();
	private Map<Integer,PlayerTeePreference> fullPlayerTeePreferencesMap = new HashMap<>();

	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	private List<Game> fullGameList = new ArrayList<Game>();
	private List<Course> courseSelections = new ArrayList<Course>();
	private List<CourseTee> courseTees = new ArrayList<CourseTee>();
	private List<Player> fullPlayerList = new ArrayList<Player>();	
	private List<PlayerTeePreference> fullPlayerTeePreferencesList = new ArrayList<>();
	
	private boolean disableProceedToSelectGame = true;
	private boolean disableProceedToSelectPlayers = true;
	private boolean disableProceedToEnterScores = true;
	private boolean disableDeleteSelectedPlayerRound = true;
	
	private String groupEmailMessage = "";
	private String groupEmailDisclaimer = "";  
	private String groupEmailSender;
	
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
	private String recommendedTeeTimesString;
	private static String recommendedPlayTheBallMethod; //up everywhere; Down everywhere but up in bunkers; up in fairway, down in rough
	private static String recommendedGameNote; 
	
	private static String NEWLINE = "<br/>";	
	
	@Autowired private TeeTimeDAO teeTimeDAO;
	@Autowired private UsersAndAuthoritiesDAO usersAndAuthoritiesDAO;
	@Autowired private GameDAO gameDAO;
	@Autowired private CourseDAO courseDAO;
	@Autowired private CourseTeeDAO courseTeeDAO;
	@Autowired private PlayerDAO playerDAO;
	@Autowired private PlayerTeePreferenceDAO playerTeePreferencesDAO;
	@Autowired private Group group;
	
	@PostConstruct
	public void init()
	{			
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
		
		refreshCourseSelections();
		refreshCourseTees();
		refreshFullGameList();
		refreshTeeTimeList();
		refreshFullPlayerList();
		
	}

	public void refreshFullPlayerTeePreferencesList() 
	{
		this.setFullPlayerTeePreferencesList(playerTeePreferencesDAO.readPlayerTeePreferencesFromDB(group));	
		
		fullPlayerTeePreferencesMap.clear();		
		fullPlayerTeePreferencesMap = fullPlayerTeePreferencesList.stream().collect(Collectors.toMap(PlayerTeePreference::getPlayerTeePreferenceID, plyrt -> plyrt));
		
		log.info("Player Tee Preferences read in. List size = " + this.getFullPlayerTeePreferencesList().size());		
	}

	public void onLoadEmailGroup() 
	{
		log.info(getTempUserName() + " In onLoadEmailGroup GolfMain.java");
		
		groupEmailDisclaimer = "The note you compose here will go to the entire group so please use wisely!  Thank you";
		groupEmailSender = getTempUserName();
	}
		
	public void refreshFullPlayerList() 
	{
		this.setFullPlayerList(playerDAO.readPlayersFromDB());	
		
		for (int i = 0; i < this.getFullPlayerList().size(); i++) 
		{
			Player tempPlayer = this.getFullPlayerList().get(i);			
			
			//get the role for this player on the authorities table
			GolfUser gu = usersAndAuthoritiesDAO.readUserFromDB(tempPlayer.getUsername());
			
			if (gu == null)
			{
				log.error("golfuser read from usersAndAuthorities is null for player: " + tempPlayer.getUsername());
			}
			else
			{
				String userRole = gu.getUserRoles()[0];
				tempPlayer.setRole(userRole);
			}			
			
		}
		
		fullPlayerMap.clear();
		fullPlayerMapByUserName.clear();
		
		fullPlayerMap = fullPlayerList.stream().collect(Collectors.toMap(Player::getPlayerID, ply -> ply));
		fullPlayerMapByUserName = fullPlayerList.stream().collect(Collectors.toMap(Player::getUsername, ply -> ply));
		
		log.info("Players read in. List size = " + this.getFullPlayerList().size());
	}

	public void refreshCourseSelections()
	{
		this.setCourseSelections(courseDAO.readCoursesFromDB(group));	
		
		coursesMap.clear();		
		coursesMap = courseSelections.stream().collect(Collectors.toMap(Course::getCourseID, crs -> crs));
		
		log.info("Courses read in. List size = " + this.getCourseSelections().size());		
    }
	
	public void refreshCourseTees()
	{
		this.setCourseTees(courseTeeDAO.readCourseTeesFromDB(group));	
		
		courseTeesMap.clear();		
		courseTeesMap = courseTees.stream().collect(Collectors.toMap(CourseTee::getCourseTeeID, crsTee -> crsTee));
		
		log.info("Course Tees read in. List size = " + this.getCourseTees().size());		
    }
	
	public void refreshTeeTimeList()
	{
		this.setTeeTimeList(teeTimeDAO.readTeeTimesFromDB());
		
		for (int i = 0; i < this.getTeeTimeList().size(); i++) 
		{
			TeeTime tempTeeTime = this.getTeeTimeList().get(i);
			tempTeeTime.setTeeTimeGame(gamesMap.get(tempTeeTime.getGameID()));
		}
		
		teeTimesMap.clear();
		teeTimesMap = this.getTeeTimeList().stream().collect(Collectors.toMap(TeeTime::getTeeTimeID, ttm -> ttm));	
		
		log.info("Tee Times read in. List size = " + this.getTeeTimeList().size());	
		
	}
	
	public void refreshFullGameList()
	{
		this.setFullGameList(gameDAO.readGamesFromDB());
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			Game tempGame = this.getFullGameList().get(i);
			tempGame.setCourse(coursesMap.get(tempGame.getCourseID()));
			tempGame.setCourseName(tempGame.getCourse().getCourseName());		
		}
		
		gamesMap.clear();
		gamesMap = fullGameList.stream().collect(Collectors.toMap(Game::getGameID, gm -> gm));		
		
		log.info("Full Game list read in. List size = " + this.getFullGameList().size());		
		
	}
	
	public List<TeeTime> getGameSpecificTeeTimes(Game game)
	{
		List<TeeTime> gameTeeTimes = this.getTeeTimeList().stream()
			.filter(p -> p.getGameID() == game.getGameID())
			.collect(Collectors.mapping(
				      p -> new TeeTime(p.getTeeTimeID(), p.getGameID(), p.getPlayGroupNumber(), p.getTeeTimeString(), p.getTeeTimeGame()),
				      Collectors.toList()));
		
		return gameTeeTimes;	
	}
	
	public void removeTeeTimeFromMainList(TeeTime tt) 
	{	
		for (int i = 0; i < this.getTeeTimeList().size(); i++) 
		{
			TeeTime teeTime = teeTimeList.get(i);
			if (teeTime.getTeeTimeID() == tt.getTeeTimeID())
			{
				teeTimeList.remove(i);
				break;
			}
		}
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
				recommendedTeamPot = new BigDecimal(40);;
				recommendedTeeTimesString = "9:30";
				
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
				recommendedTeeTimesString = "9:30 9:40";
					
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
				recommendedTeeTimesString = "9:30 9:40";
				
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
				recommendedTeeTimesString = "9:30 9:40";
				
				break;	
				
			case 8:
				
				recommendedPurseAmount = new BigDecimal(160);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(80);
				recommendedTeamPot = new BigDecimal(80);;
				recommendedTeeTimesString = "9:30 9:40";
				break;
				
			case 9:
				recommendedPurseAmount = new BigDecimal(180);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(50);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(80);
				recommendedTeamPot = new BigDecimal(100);;
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 10:
				recommendedPurseAmount = new BigDecimal(200);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(80);
				recommendedTeamPot = new BigDecimal(120);;
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 11:  //skins only 11th player
				
				recommendedPurseAmount = new BigDecimal(208);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(40);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(88);
				recommendedTeamPot = new BigDecimal(120);;
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 12:
				
				recommendedPurseAmount = new BigDecimal(240);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(64);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(112);
				recommendedTeamPot = new BigDecimal(128);;
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 13: //skins only 13th player
				
				recommendedPurseAmount = new BigDecimal(248);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(64);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(120);
				recommendedTeamPot = new BigDecimal(128);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				break;
				
			case 14:
				
				recommendedPurseAmount = new BigDecimal(280);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(42);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(112);
				recommendedTeamPot = new BigDecimal(168);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				break;
				
			case 15:
				
				recommendedPurseAmount = new BigDecimal(300);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(60);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(120);
				recommendedTeamPot = new BigDecimal(180);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				break;
				
			case 16:
				
				recommendedPurseAmount = new BigDecimal(320);
				recommendedTotalTeams = 4;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(60);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(140);
				recommendedTeamPot = new BigDecimal(180);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
				break;	

			case 17: //skins only 17th player
				
				recommendedPurseAmount = new BigDecimal(328);
				recommendedTotalTeams = 4;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(60);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(148);
				recommendedTeamPot = new BigDecimal(180);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 18:
				
				recommendedPurseAmount = new BigDecimal(360);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(105);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(150);
				recommendedTeamPot = new BigDecimal(210);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 19: //skins only 19th player
				
				recommendedPurseAmount = new BigDecimal(368);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 2;
				recommendedEachBallWorth = new BigDecimal(105);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(158);
				recommendedTeamPot = new BigDecimal(210);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 20:
				
				recommendedPurseAmount = new BigDecimal(400);
				recommendedTotalTeams = 5;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(80);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(160);
				recommendedTeamPot = new BigDecimal(240);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
			
			case 21:
				
				recommendedPurseAmount = new BigDecimal(420);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(63);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(168);
				recommendedTeamPot = new BigDecimal(252);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 22: //skins only 22nd player
				recommendedPurseAmount = new BigDecimal(428);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(63);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(176);
				recommendedTeamPot = new BigDecimal(252);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 23: //skins only 22nd and 23rd players
				
				recommendedPurseAmount = new BigDecimal(436);
				recommendedTotalTeams = 3;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(63);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(184);
				recommendedTeamPot = new BigDecimal(252);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 24:
				recommendedPurseAmount = new BigDecimal(480);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(100);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(180);
				recommendedTeamPot = new BigDecimal(300);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
				break;
				
			case 25:
				recommendedPurseAmount = new BigDecimal(490);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(100);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(190);
				recommendedTeamPot = new BigDecimal(300);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;
				
			case 26:
				recommendedPurseAmount = new BigDecimal(500);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(100);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(200);
				recommendedTeamPot = new BigDecimal(300);;
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
				recommendedTeamPot = new BigDecimal(360);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;				
					
			case 28:
				recommendedPurseAmount = new BigDecimal(560);
				recommendedTotalTeams = 7;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(80);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(240);
				recommendedTeamPot = new BigDecimal(320);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20";
				break;	
			
			case 29:
				recommendedPurseAmount = new BigDecimal(570);
				recommendedTotalTeams = 7;
				recommendedHowManyBalls = 4;
				recommendedEachBallWorth = new BigDecimal(80);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(250);
				recommendedTeamPot = new BigDecimal(320);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;				
			
			case 30:
				recommendedPurseAmount = new BigDecimal(600);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(120);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(240);
				recommendedTeamPot = new BigDecimal(360);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			case 31:
				recommendedPurseAmount = new BigDecimal(610);
				recommendedTotalTeams = 6;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(120);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(250);
				recommendedTeamPot = new BigDecimal(360);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			case 32:
				recommendedPurseAmount = new BigDecimal(640);
				recommendedTotalTeams = 8;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(130);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(250);
				recommendedTeamPot = new BigDecimal(390);;
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10 10:20 10:30";
				break;
				
			default:				
				break;
		}
		
	}
	
	public String sendGroupEmail()
	{
		log.info("User clicked sendGroupEmail");
		
		String subjectLine = "TMG Group email";
		
		if (emailRecipients == null)
		{
			emailRecipients = new ArrayList<String>();
		}
		else
		{
			emailRecipients.clear();
		}
		
		List<Player> fullPlayerList = this.getFullPlayerList();
		
		Player player = BeanUtilJSF.getBean("pc_Player");
		
		String senderName = player.getLoggedInPlayerName();
		String senderReplyEmail = player.getLoggedInPlayerEmail();
		
		StringBuffer sb = new StringBuffer();
		
		sb.append("<H3>Email message from: " + senderName + "</H3>");	
		sb.append(NEWLINE);
		
		sb.append("<H3>Reply directly to sender at: " + senderReplyEmail + "</H3>");	
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		
		sb.append("<H3>" + groupEmailMessage + "</H3>");	
		
		log.info("Group email message about to be sent: " + sb.toString());		
		
		emailRecipients = Utils.setEmailFullRecipientList(fullPlayerList);
		//emailRecipients.add("paulslomkowski@yahoo.com"); //to just me for testing
		SAMailUtility.sendEmail(subjectLine, sb.toString(), emailRecipients, true); //last param means use jsf
		
		log.info("User sent email to entire group successfully");
		
		return "";
	}
 	public String proceedToSelectGame() 
	{
		log.info("User clicked proceed from Main screen; sending them to game list/add screen");
		
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

	public boolean isDisableProceedToEnterScores() {
		return disableProceedToEnterScores;
	}

	public void setDisableProceedToEnterScores(boolean disableProceedToEnterScores) {
		this.disableProceedToEnterScores = disableProceedToEnterScores;
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

	public List<SelectItem> getPlayGroupSelections() {
		return playGroupSelections;
	}

	public void setPlayGroupSelections(List<SelectItem> playGroupSelections) {
		this.playGroupSelections = playGroupSelections;
	}

	public List<TeeTime> getTeeTimeList() {
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) {
		this.teeTimeList = teeTimeList;
	}

	public List<Game> getFullGameList() {
		return fullGameList;
	}

	public void setFullGameList(List<Game> fullGameList) {
		this.fullGameList = fullGameList;
	}

	public List<Course> getCourseSelections() {
		return courseSelections;
	}

	public void setCourseSelections(List<Course> courseSelections) {
		this.courseSelections = courseSelections;
	}

	public Map<Integer, Player> getFullPlayerMap() {
		return fullPlayerMap;
	}

	public void setFullPlayerMap(Map<Integer, Player> fullPlayerMap) {
		this.fullPlayerMap = fullPlayerMap;
	}

	public Map<String, Player> getFullPlayerMapByUserName() {
		return fullPlayerMapByUserName;
	}

	public void setFullPlayerMapByUserName(Map<String, Player> fullPlayerMapByUserName) {
		this.fullPlayerMapByUserName = fullPlayerMapByUserName;
	}

	public List<Player> getFullPlayerList() {
		return fullPlayerList;
	}

	public void setFullPlayerList(List<Player> fullPlayerList) {
		this.fullPlayerList = fullPlayerList;
	}

	public Map<Integer, Course> getCoursesMap() {
		return coursesMap;
	}

	public void setCoursesMap(Map<Integer, Course> coursesMap) {
		this.coursesMap = coursesMap;
	}

	public Map<Integer, Game> getGamesMap() {
		return gamesMap;
	}

	public void setGamesMap(Map<Integer, Game> gamesMap) {
		this.gamesMap = gamesMap;
	}

	public Map<Integer, TeeTime> getTeeTimesMap() {
		return teeTimesMap;
	}

	public void setTeeTimesMap(Map<Integer, TeeTime> teeTimesMap) {
		this.teeTimesMap = teeTimesMap;
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

	public Map<Integer, CourseTee> getCourseTeesMap() {
		return courseTeesMap;
	}

	public void setCourseTeesMap(Map<Integer, CourseTee> courseTeesMap) {
		this.courseTeesMap = courseTeesMap;
	}

	public List<CourseTee> getCourseTees() {
		return courseTees;
	}

	public void setCourseTees(List<CourseTee> courseTees) {
		this.courseTees = courseTees;
	}

	public List<PlayerTeePreference> getFullPlayerTeePreferencesList() {
		return fullPlayerTeePreferencesList;
	}

	public void setFullPlayerTeePreferencesList(List<PlayerTeePreference> fullPlayerTeePreferencesList) {
		this.fullPlayerTeePreferencesList = fullPlayerTeePreferencesList;
	}

	public Map<Integer, PlayerTeePreference> getFullPlayerTeePreferencesMap() {
		return fullPlayerTeePreferencesMap;
	}

	public void setFullPlayerTeePreferencesMap(Map<Integer, PlayerTeePreference> fullPlayerTeePreferencesMap) {
		this.fullPlayerTeePreferencesMap = fullPlayerTeePreferencesMap;
	}

	public static String getRecommendedGameNote() {
		return recommendedGameNote;
	}

	public static void setRecommendedGameNote(String recommendedGameNote) {
		GolfMain.recommendedGameNote = recommendedGameNote;
	}
		
}

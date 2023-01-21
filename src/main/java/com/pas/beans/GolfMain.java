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
import com.pas.dao.GroupDAO;
import com.pas.dao.PlayerDAO;
import com.pas.dao.PlayerMoneyDAO;
import com.pas.dao.PlayerTeePreferenceDAO;
import com.pas.dao.RoundDAO;
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
	
	private boolean disableProceedToSelectGame = true;
	private boolean disableProceedToSelectPlayers = true;
	private boolean disableProceedToEnterScores = true;
	private boolean disableDeleteSelectedPlayerRound = true;
	
	private String groupEmailMessage = "";
	private String groupEmailDisclaimer = "";  
	private String groupEmailSender;
	
	private Group defaultGroup = null;
	
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
	@Autowired private RoundDAO roundDAO;
	@Autowired private CourseDAO courseDAO;
	@Autowired private CourseTeeDAO courseTeeDAO;
	@Autowired private PlayerDAO playerDAO;
	@Autowired private PlayerMoneyDAO playerMoneyDAO;
	@Autowired private PlayerTeePreferenceDAO playerTeePreferencesDAO;
	@Autowired private GroupDAO groupDAO;
	
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
		
		//this gets populated at app startup, no need to do it again when someone logs in.
		if (usersAndAuthoritiesDAO.getFullUserMap().isEmpty())
		{
			groupDAO.readGroupsFromDB();
			Group defaultGroup = this.getGroupsList().get(0);
			this.setDefaultGroup(defaultGroup);
			
			refreshCourseSelections();
			refreshCourseTees();
			refreshFullGameList();
			refreshTeeTimeList();
			refreshFullPlayerList();
			refreshFullPlayerTeePreferencesList();
			refreshPlayerMoneyList();
		}	
				
	}

	public void refreshCourseSelections()
	{
		courseDAO.readCoursesFromDB(this.getDefaultGroup()); //pick the first group by default - Bryan Park.
		log.info("Courses read in. List size = " + this.getCourseSelections().size());		
    }
	
	public void refreshCourseTees()
	{
		courseTeeDAO.readCourseTeesFromDB(this.getDefaultGroup());					
		log.info("Course Tees read in. List size = " + this.getCourseTees().size());		
    }
	
	public void refreshFullGameList()
	{
		gameDAO.readGamesFromDB();			
		log.info("Full Game list read in. List size = " + this.getFullGameList().size());			
	}
	
	public void refreshTeeTimeList()
	{
		teeTimeDAO.readTeeTimesFromDB();			
		log.info("Tee Times read in. List size = " + this.getTeeTimeList().size());			
	}
	
	public void refreshPlayerMoneyList()
	{
		playerMoneyDAO.readPlayerMoneyFromDB();	
		
		Map<Integer,PlayerMoney> tempMap = new HashMap<Integer,PlayerMoney>();
		
		for (int i = 0; i < playerMoneyDAO.getPlayerMoneyList().size(); i++) 
		{
			PlayerMoney pm = playerMoneyDAO.getPlayerMoneyList().get(i);			
			Game game = getGamesMap().get(pm.getGameID());
			Player player = getFullPlayersMapByPlayerID().get(pm.getPlayerID());	
			pm.setGame(game);
			pm.setPlayer(player);
			tempMap.put(pm.getPlayerMoneyID(), pm);
		}
		
		playerMoneyDAO.getPlayerMoneyMap().clear();
		playerMoneyDAO.setPlayerMoneyMap(tempMap);
		
		Collection<PlayerMoney> values = playerMoneyDAO.getPlayerMoneyMap().values();
		playerMoneyDAO.setPlayerMoneyList(new ArrayList<>(values));		
		
		log.info("Player Money read in. List size = " + this.getPlayerMoneyList().size());			
	}
	
	public void refreshFullPlayerList() 
	{
		playerDAO.readPlayersFromDB();			
		usersAndAuthoritiesDAO.readAllUsersFromDB();
						
		Map<String, GolfUser> golfUsersMap = usersAndAuthoritiesDAO.getFullUserMap();
		
		for (int i = 0; i < this.getFullPlayerList().size(); i++) 
		{
			Player tempPlayer = this.getFullPlayerList().get(i);			
			
			//get the role for this player
			GolfUser gu = golfUsersMap.get(tempPlayer.getUsername());
			
			if (gu == null)
			{
				log.error("golfuser is null for player: " + tempPlayer.getUsername());
			}
			else
			{
				String userRole = gu.getUserRoles()[0];
				tempPlayer.setRole(userRole);
			}			
			
		}
			
		log.info("Players read in. List size = " + this.getFullPlayerList().size());
	}

	public void refreshFullPlayerTeePreferencesList() 
	{
		playerTeePreferencesDAO.readPlayerTeePreferencesFromDB(this.getDefaultGroup());
		
		Map<Integer,PlayerTeePreference> tempMap = new HashMap<Integer,PlayerTeePreference>();
		
		for (int i = 0; i < playerTeePreferencesDAO.getPlayerTeePreferencesList().size(); i++) 
		{
			PlayerTeePreference ptp = playerTeePreferencesDAO.getPlayerTeePreferencesList().get(i);
			CourseTee ct = getCourseTeesMap().get(ptp.getCourseTeeID());
	       	Course cs = getCoursesMap().get(ptp.getCourseID());
	   		Player player = getFullPlayersMapByPlayerID().get(ptp.getPlayerID());
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
		
		log.info("Player Tee Preferences read in. List size = " + this.getFullPlayerTeePreferencesList().size());		
	}

	public void onLoadEmailGroup() 
	{
		log.info(getTempUserName() + " In onLoadEmailGroup GolfMain.java");
		
		groupEmailDisclaimer = "The note you compose here will go to the entire group so please use wisely!  Thank you";
		groupEmailSender = getTempUserName();
	}		
	
	public List<TeeTime> getGameSpecificTeeTimes(Game game)
	{
		List<TeeTime> gameTeeTimes = this.getTeeTimeList().stream()
			.filter(p -> p.getGameID() == game.getGameID())
			.collect(Collectors.mapping(
				      p -> new TeeTime(p.getTeeTimeID(), p.getGameID(), p.getPlayGroupNumber(), p.getTeeTimeString(), p.getGameDate(), p.getCourseName()),
				      Collectors.toList()));
		
		Collections.sort(gameTeeTimes, new Comparator<TeeTime>() 
		{
		   public int compare(TeeTime o1, TeeTime o2) 
		   {
			   Integer o1Int = o1.getPlayGroupNumber();
			   Integer o2Int = o2.getPlayGroupNumber();
		      return o1Int.compareTo(o2Int);
		   }
		});		
		
		return gameTeeTimes;	
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
		
		String subjectLine = "Group Email";
		
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

	public List<SelectItem> getPlayGroupSelections() 
	{
		return playGroupSelections;
	}

	public void setPlayGroupSelections(List<SelectItem> playGroupSelections) 
	{
		this.playGroupSelections = playGroupSelections;
	}

	public List<TeeTime> getTeeTimeList()
	{
		return teeTimeDAO.getTeeTimeList();
	}

	public List<Game> getFullGameList() 
	{
		return gameDAO.getFullGameList();
	}

	public List<Course> getCourseSelections() 
	{
		return courseDAO.getCourseSelections();
	}

	public List<Player> getFullPlayerList() 
	{
		return playerDAO.getFullPlayerList();
	}

	public Map<Integer, Player> getFullPlayersMapByPlayerID() 
	{
		return playerDAO.getFullPlayersMapByPlayerID();
	}

	public Map<String, Player> getFullPlayersMapByUserName() 
	{
		return playerDAO.getFullPlayersMapByUserName();
	}
	
	public Map<Integer, Course> getCoursesMap() 
	{
		return courseDAO.getCoursesMap();
	}

	public Map<Integer, Game> getGamesMap() 
	{
		return gameDAO.getFullGameMap();
	}

	public Map<Integer, TeeTime> getTeeTimesMap()
	{
		return teeTimeDAO.getTeeTimesMap();
	}
	
	public Map<Integer, CourseTee> getCourseTeesMap() 
	{
		return courseTeeDAO.getCourseTeesMap();
	}

	public List<CourseTee> getCourseTees() 
	{
		return courseTeeDAO.getCourseTeesList();
	}

	public List<PlayerTeePreference> getFullPlayerTeePreferencesList() 
	{
		return playerTeePreferencesDAO.getPlayerTeePreferencesList();
	}

	public Map<Integer, PlayerTeePreference> getFullPlayerTeePreferencesMap() 
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

	public int addGame(Game game) 
	{
		return gameDAO.addGame(game);
	}

	public void deleteGame(int gameID) 
	{
		gameDAO.deleteGame(gameID);		
	}

	public void updateGame(Game game) 
	{
		gameDAO.updateGame(game);		
	}

	public List<Game> getFutureGames() 
	{
		return gameDAO.getFutureGames();
	}

	public List<Game> getAvailableGamesByPlayerID(int playerID) 
	{
		return gameDAO.getAvailableGames(playerID);
	}

	public Integer getTeePreference(int playerID, int courseID)
	{
		return gameDAO.getTeePreference(playerID, courseID);
	}

	public Game getGameByGameID(int gameID) 
	{
		return gameDAO.getGameByGameID(gameID);
	}
	
	public Player getPlayerByPlayerID(int playerID)
	{
		return playerDAO.getFullPlayersMapByPlayerID().get(playerID);
	}
	
	public void updatePlayerHandicap(int playerID, BigDecimal handicap)
	{
		playerDAO.updatePlayerHandicap(playerID, handicap);
	}
	
	public int addPlayer(Player player) 
	{
		return playerDAO.addPlayer(player);
	}
	
	public void updatePlayer(Player player) 
	{
		playerDAO.updatePlayer(player);
	}
	
	public List<Round> getRoundsForGame(Game game) 
	{
		return roundDAO.getRoundsForGame(game);
	}
	
	public int addRound(Round round) 
	{
		return roundDAO.addRound(round);
	}
	
	public void updateRound(Round round) 
	{
		roundDAO.updateRound(round);
	}
	
	public void deleteRoundFromDB(int roundID) 
	{
		roundDAO.deleteRoundFromDB(roundID);
	}

	public void deleteRoundsFromDB(int gameID) 
	{
		roundDAO.deleteRoundsFromDB(gameID);		
	}

	public Round getRoundByGameandPlayer(int gameID, int playerID) 
	{
		return roundDAO.getRoundByGameandPlayer(gameID, playerID);
	}

	public List<String> getGameParticipantsFromDB(Game selectedGame) 
	{
		return roundDAO.getGameParticipantsFromDB(selectedGame);
	}

	public Integer countRoundsForGameFromDB(Game gm) 
	{
		return roundDAO.countRoundsForGameFromDB(gm);
	}

	public void updateRoundHandicap(Game selectedGame, int playerID, BigDecimal newRoundHandicap) 
	{
		roundDAO.updateRoundHandicap(selectedGame, playerID, newRoundHandicap);		
	}

	public void updateRoundTeamNumber(Game selectedGame, int playerID, int teamNumber) 
	{
		roundDAO.updateRoundTeamNumber(selectedGame, playerID, teamNumber);		
	}

	public void addPlayerTeePreference(PlayerTeePreference ptp) 
	{
		playerTeePreferencesDAO.addPlayerTeePreference(ptp);		
	}

	public void updatePlayerTeePreference(PlayerTeePreference ptp) 
	{
		playerTeePreferencesDAO.updatePlayerTeePreference(ptp);				
	}

	public PlayerTeePreference getPlayerTeePreference(int playerID, int courseID) 
	{
		return playerTeePreferencesDAO.getPlayerTeePreference(playerID, courseID);
	}

	public List<CourseTee> getCourseTeesList()
	{
		return courseTeeDAO.getCourseTeesList();
	}

	public List<TeeTime> getTeeTimesByGame(Game selectedGame) 
	{
		return teeTimeDAO.getTeeTimesByGame(selectedGame);
	}

	public void deleteTeeTimeFromDB(int teeTimeID) 
	{
		teeTimeDAO.deleteTeeTimeFromDB(teeTimeID);		
	}

	public void addTeeTime(TeeTime teeTime) 
	{
		teeTimeDAO.addTeeTime(teeTime);		
	}

	public void updateTeeTime(TeeTime teeTime) 
	{
		teeTimeDAO.updateTeeTime(teeTime);		
	}

	public void addTeeTimes(int newGameID, String teeTimesString, Date gameDate, String courseName) 
	{
		teeTimeDAO.addTeeTimes(newGameID, teeTimesString, gameDate, courseName);		
	}

	public void deleteTeeTimesForGameFromDB(int gameID) 
	{
		teeTimeDAO.deleteTeeTimesForGameFromDB(gameID);		
	}

	public void deletePlayerMoneyFromDB(int gameID)
	{
		playerMoneyDAO.deletePlayerMoneyFromDB(gameID);		
	}

	public List<PlayerMoney> getPlayerMoneyByGame(Game selectedGame) 
	{
		return playerMoneyDAO.getPlayerMoneyByGame(selectedGame);
	}

	public void addPlayerMoney(PlayerMoney pm) 
	{
		playerMoneyDAO.addPlayerMoney(pm);		
	}

	public List<PlayerMoney> getPlayerMoneyByPlayer(Player player) 
	{
		return playerMoneyDAO.getPlayerMoneyByPlayer(player);
	}
	
	public List<PlayerMoney> getPlayerMoneyList()
	{
		return playerMoneyDAO.getPlayerMoneyList();
	}
	
	public List<Group> getGroupsList()
	{
		return groupDAO.getGroupsList();
	}

	public Group getDefaultGroup() {
		return defaultGroup;
	}

	public void setDefaultGroup(Group defaultGroup) {
		this.defaultGroup = defaultGroup;
	}
		
}

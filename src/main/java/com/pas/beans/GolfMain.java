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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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
import com.pas.dynamodb.DynamoUtil;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

@Named("pc_GolfMain")
@Component
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
	
	private String loggedInPlayerName;
	private String loggedInPlayerEmail;

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
	
	public GolfMain() 
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
				golfUsersDAO = new GolfUsersDAO(dynamoClients, this);
				groupDAO = new GroupDAO(dynamoClients);
				groupDAO.readGroupsFromDB();
				Group defaultGroup = this.getGroupsList().get(0);
				this.setDefaultGroup(defaultGroup);
				
				loadCourseSelections(dynamoClients);
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

	//use this from DailyEmailJob so as to not run anything like at app startup
	public GolfMain(String tempString)
	{		
	}
	
	private void loadRoundList(DynamoClients dynamoClients) throws Exception
	{
		List<String> gameIDList = new ArrayList<>();
		
		for (int i = 0; i < gameDAO.getFullGameList().size(); i++) 
		{
			Game game = gameDAO.getFullGameList().get(i);
			gameIDList.add(game.getGameID());
		}
		roundDAO = new RoundDAO(dynamoClients, this, null);
		roundDAO.readAllRoundsFromDB(gameIDList);
		logger.info("Rounds read in. List size = " + this.getFullRoundsList().size());	
		
		Map<String,Round> tempMap = new HashMap<>();
		
		Map<String, Game> fullGameMap = this.getFullGameList().stream().collect(Collectors.toMap(Game::getGameID, game -> game));
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round round = this.getFullRoundsList().get(i);
				
			CourseTee ct = getCourseTeesMap().get(round.getCourseTeeID());
			if (ct != null)
		    {
				round.setCourseTeeColor(ct.getTeeColor());	    
		    }	    
			
			Game game = fullGameMap.get(round.getGameID());
			
			if (game == null) //should not happen but safeguard
			{
				continue;
			}
			
			Course course = game.getCourse();
			
			Player player = this.getFullPlayersMapByPlayerID().get(round.getPlayerID());
			
			TeeTime teeTime = this.getTeeTimesMap().get(round.getTeeTimeID());
			
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

	public void loadCourseSelections(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadCourseSelections");
		courseDAO = new CourseDAO(dynamoClients);
		courseDAO.readCoursesFromDB(this.getDefaultGroup()); //pick the first group by default - Bryan Park.
		logger.info("Courses read in. List size = " + this.getCourseSelections().size());		
    }
	
	public void loadCourseTees(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadCourseTees");
		
		courseTeeDAO = new CourseTeeDAO(dynamoClients, this);
		courseTeeDAO.readCourseTeesFromDB(this.getDefaultGroup());					
		logger.info("Course Tees read in. List size = " + this.getCourseTees().size());		
    }
	
	public void loadFullGameList(DynamoClients dynamoClients, Group defaultGroup) throws Exception 
	{
		logger.info("entering loadFullGameList");
		
		gameDAO = new GameDAO(dynamoClients, this);
		gameDAO.readGamesFromDB(defaultGroup);			
		logger.info("Full Game list read in. List size = " + this.getFullGameList().size());	
		
		Map<String,Game> tempMap = new HashMap<>();
		
		for (int i = 0; i < this.getFullGameList().size(); i++) 
		{
			Game game = this.getFullGameList().get(i);
			assignCourseToGame(game);
			tempMap.put(game.getGameID(), game);
		}
			
		Collection<Game> values = tempMap.values();
		gameDAO.setFullGameList(new ArrayList<>(values));
		
		Collections.sort(this.getFullGameList(), new Comparator<Game>() 
		{
		   public int compare(Game o1, Game o2) 
		   {
		      return o1.getGameDate().compareTo(o2.getGameDate());
		   }
		});
	}
	
	public void loadTeeTimeList(DynamoClients dynamoClients, Group defaultGroup) throws Exception
	{
		logger.info("entering loadTeeTimeList");
		teeTimeDAO = new TeeTimeDAO(dynamoClients);
		teeTimeDAO.readTeeTimesFromDB(defaultGroup);			
		logger.info("Tee Times read in. List size = " + this.getTeeTimeList().size());			
	}
	
	public void loadPlayerMoneyList(DynamoClients dynamoClients)  throws Exception
	{
		logger.info("entering loadPlayerMoneyList");
		playerMoneyDAO = new PlayerMoneyDAO(dynamoClients, this);
		playerMoneyDAO.readPlayerMoneyFromDB();	
		
		Map<String,PlayerMoney> tempMap = new HashMap<>();
		
		for (int i = 0; i < playerMoneyDAO.getPlayerMoneyList().size(); i++) 
		{
			PlayerMoney pm = playerMoneyDAO.getPlayerMoneyList().get(i);			
			Game game = gameDAO.getGameByGameID(pm.getGameID());
			Player player = getFullPlayersMapByPlayerID().get(pm.getPlayerID());	
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
			Player tempPlayer = this.getFullPlayerList().get(i);			
			
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
			CourseTee ct = getCourseTeesMap().get(ptp.getCourseTeeID());
	       	Course cs = getCoursesMap().get(ptp.getCourseID());
	   		Player player = getFullPlayersMapByPlayerID().get(ptp.getPlayerID());
	   		
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

	public void assignCourseToGame(Game inGame)
	{
		inGame.setCourse(getCoursesMap().get(inGame.getCourseID()));
		inGame.setCourseName(inGame.getCourse().getCourseName());
		
		List<SelectItem> courseTeeSelections = new ArrayList<>();
		
		List<CourseTee> sortedCourseTees = new ArrayList<>(getCourseTees());
		Collections.sort(sortedCourseTees, new CourseTeeComparator());
		
		for (int i = 0; i < sortedCourseTees.size(); i++) 
		{
			CourseTee courseTee = sortedCourseTees.get(i);
			if (courseTee.getCourseID().equalsIgnoreCase(inGame.getCourseID()))
			{
				SelectItem selItem = new SelectItem();
				selItem.setLabel(courseTee.getTeeColor() + " (" + courseTee.getTotalYardage() + " yds)");
				selItem.setValue(courseTee.getCourseTeeID());
				courseTeeSelections.add(selItem);
			}
		}
		
		inGame.setTeeSelections(courseTeeSelections);
	}
	
	public static class CourseTeeComparator implements Comparator<CourseTee> 
	{
		public int compare(CourseTee courseTee1, CourseTee courseTee2)
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
	
	public List<TeeTime> getGameSpecificTeeTimes(Game game)
	{
		List<TeeTime> gameTeeTimes = this.getTeeTimeList().stream()
			.filter(p -> p.getGameID().equalsIgnoreCase(game.getGameID()))
			.collect(Collectors.mapping(
				      p -> new TeeTime(p.getTeeTimeID(), p.getGameID(), p.getPlayGroupNumber(), p.getTeeTimeString(), p.getGameDate(), p.getCourseName(), this),
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
				recommendedTeamPot = new BigDecimal(40);
				recommendedGameFee = new BigDecimal(0.00);
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
				recommendedGameFee = new BigDecimal(0.00);
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
				recommendedGameFee = new BigDecimal(0.00);
				recommendedTeeTimesString = "9:30 9:40";
				
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
				recommendedTeeTimesString = "9:30 9:40";
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
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 10:
				recommendedPurseAmount = new BigDecimal(200);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(35);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(80);
				recommendedTeamPot = new BigDecimal(105);
				recommendedGameFee = new BigDecimal(15.00);
				recommendedTeeTimesString = "9:30 9:40 9:50";
				break;
				
			case 11:  //skins only 11th player
				
				recommendedPurseAmount = new BigDecimal(210);
				recommendedTotalTeams = 2;
				recommendedHowManyBalls = 3;
				recommendedEachBallWorth = new BigDecimal(35);
				recommendedIndividualGrossPrize = new BigDecimal(0.00);
				recommendedIndividualNetPrize = new BigDecimal(0.00);
				recommendedSkinsPot = new BigDecimal(90);
				recommendedTeamPot = new BigDecimal(105);
				recommendedGameFee = new BigDecimal(15.00);
				recommendedTeeTimesString = "9:30 9:40 9:50";
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
				recommendedTeeTimesString = "9:30 9:40 9:50";
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
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
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
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
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
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
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
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00";
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
				recommendedTeamPot = new BigDecimal(210);
				recommendedGameFee = new BigDecimal(22.00);
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
				recommendedTeamPot = new BigDecimal(210);
				recommendedGameFee = new BigDecimal(22.00);
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
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
				recommendedTeeTimesString = "9:30 9:40 9:50 10:00 10:10";
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
		
		List<Player> fullPlayerList = this.getFullPlayerList();
			
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
	
	public List<Player> getActivePlayerList() 
	{
		List<Player> sortedList = new ArrayList<>(playerDAO.getActivePlayerList());
	    sortedList.sort(new PlayerComparatorByLastName());
	    
	    return sortedList;
	}

	public Map<String, Player> getFullPlayersMapByPlayerID() 
	{
		return playerDAO.getFullPlayersMapByPlayerID();
	}

	public Map<String, Player> getFullPlayersMapByUserName() 
	{
		return playerDAO.getFullPlayersMapByUserName();
	}
	
	public Map<String, Course> getCoursesMap() 
	{
		return courseDAO.getCoursesMap();
	}

	public Map<String, TeeTime> getTeeTimesMap()
	{
		return teeTimeDAO.getTeeTimesMap();
	}
	
	public Map<String, CourseTee> getCourseTeesMap() 
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

	public String addGame(Game game, String teeTimesString) throws Exception 
	{
		return gameDAO.addGame(game, teeTimesString);
	}

	public void deleteGame(String gameID) throws Exception 
	{
		gameDAO.deleteGame(gameID);		
	}

	public void updateGame(Game game) throws Exception 
	{
		gameDAO.updateGame(game);		
	}

	public List<Game> getFutureGames() 
	{
		return gameDAO.getFutureGames();
	}

	public List<Game> getAvailableGamesByPlayerID(String playerID) 
	{
		return gameDAO.getAvailableGames(playerID);
	}

	public String getTeePreference(String playerID, String courseID)
	{
		return gameDAO.getTeePreference(playerID, courseID);
	}

	public Game getGameByGameID(String gameID) 
	{
		return gameDAO.getGameByGameID(gameID);
	}
	
	public Player getPlayerByPlayerID(String playerID)
	{
		return playerDAO.getFullPlayersMapByPlayerID().get(playerID);
	}
	
	public Player getPlayerByUserName(String username)
	{
		return playerDAO.getFullPlayersMapByUserName().get(username);
	}
		
	public String addPlayer(Player player)  throws Exception  
	{
		return playerDAO.addPlayer(player);
	}
	
	public void updatePlayer(Player player)  throws Exception 
	{
		playerDAO.updatePlayer(player);
	}
	
	public List<Round> getRoundsForGame(Game game) 
	{
		return roundDAO.getRoundsForGame(game);
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

	public List<String> getGameParticipantsFromDB(Game selectedGame) 
	{
		return roundDAO.getGameParticipantsFromDB(selectedGame);
	}

	public Integer countRoundsForGameFromDB(Game gm) 
	{
		return roundDAO.countRoundsForGameFromDB(gm);
	}

	public void updateRoundHandicap(Game selectedGame, String playerID, BigDecimal newRoundHandicap) throws Exception 
	{
		roundDAO.updateRoundHandicap(selectedGame, playerID, newRoundHandicap);		
	}

	public void updateRoundTeamNumber(Game selectedGame, String playerID, int teamNumber) throws Exception 
	{
		roundDAO.updateRoundTeamNumber(selectedGame, playerID, teamNumber);		
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

	public List<CourseTee> getCourseTeesList()
	{
		return courseTeeDAO.getCourseTeesList();
	}

	public List<TeeTime> getTeeTimesByGame(Game selectedGame) 
	{
		return teeTimeDAO.getTeeTimesByGame(selectedGame);
	}

	public void deleteTeeTimeFromDB(String string) 
	{
		teeTimeDAO.deleteTeeTimeFromDB(string);		
	}

	public void addTeeTime(TeeTime teeTime) throws Exception 
	{
		teeTimeDAO.addTeeTime(teeTime);		
	}

	public void updateTeeTime(TeeTime teeTime) throws Exception 
	{
		teeTimeDAO.updateTeeTime(teeTime);		
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

	public List<PlayerMoney> getPlayerMoneyByGame(Game selectedGame) 
	{
		return playerMoneyDAO.getPlayerMoneyByGame(selectedGame);
	}

	public void addPlayerMoney(PlayerMoney pm) throws Exception 
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

	public List<String> getAdminUserList() 
	{
		return golfUsersDAO.getAdminUserList();
	}

	public GolfUser getGolfUser(String whoIsThis) 
	{
		return golfUsersDAO.getGolfUser(whoIsThis);
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
			Player tempPlayer = getFullPlayersMapByUserName().get(gu.getUserName());			
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
			Player tempPlayer = getFullPlayersMapByUserName().get(gu.getUserName());			
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
		
}

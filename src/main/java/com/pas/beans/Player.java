package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DualListModel;

import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;
import com.pas.util.Utils;

@Named("pc_Player")
@SessionScoped
public class Player extends SpringBeanAutowiringSupport implements Serializable
{	
	private static final long serialVersionUID = 4089402354585236177L;
	private static Logger log = LogManager.getLogger(Game.class);
	
	private int playerID;
	private String username;
	private String oldUsername = "";
	private String role;
	private String oldRole = "";
	private String firstName;
	private String lastName;
	private String fullName;
	private int teamNumber;
	private BigDecimal handicap;
	private int grossScore;
	private BigDecimal netScore;
	private String emailAddress;
	private boolean resetPassword;
	
	private Player selectedPlayer;
	private String loggedInPlayerName;
	private String loggedInPlayerEmail;
	
	private boolean disablePlayersDialogButton = true;
	private List<Player> selectedPlayers = new ArrayList<Player>();
	private List<Round> roundsForGame = new ArrayList<Round>();	
		
	private DualListModel<Player> playersPickList = new DualListModel<Player>();
	private List<Player> playersPickListSource = new ArrayList<Player>();
	private List<Player> playersPickListTarget = new ArrayList<Player>();
	
	private DualListModel<Player> gameTeeTimeList1 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList2 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList3 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList4 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList5 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList6 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList7 = new DualListModel<Player>();
	private DualListModel<Player> gameTeeTimeList8 = new DualListModel<Player>();
	
	private boolean showGameTeeTimeList3 = false;
	private boolean showGameTeeTimeList4 = false;
	private boolean showGameTeeTimeList5 = false;
	private boolean showGameTeeTimeList6 = false;
	private boolean showGameTeeTimeList7 = false;
	private boolean showGameTeeTimeList8 = false;
	
	private String gameTeeTimeListCaption1 = "";
	private String gameTeeTimeListCaption2 = "";
	private String gameTeeTimeListCaption3 = "";
	private String gameTeeTimeListCaption4 = "";
	private String gameTeeTimeListCaption5 = "";
	private String gameTeeTimeListCaption6 = "";
	private String gameTeeTimeListCaption7 = "";
	private String gameTeeTimeListCaption8 = "";
	
	private String teePreference = "Gold";
	
	private boolean disablePickTeams = true;
	
	private TeeTime teeTime;
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	
	private String operation = "";
	
	public void onLoadPlayerPickList() 
	{			
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (game == null || game.getSelectedGame() == null)
		{
			game.setSelectedGame(golfmain.getFullGameList().get(0));
		}
		
		loadSelectedPlayers(game.getSelectedGame());		
		
		setPlayerPickLists(game.getSelectedGame());			
	}
	
	public void onLoadTeeTimePickList() 
	{
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (game == null || game.getSelectedGame() == null)
		{
			game.setSelectedGame(golfmain.getFullGameList().get(0));
			loadSelectedPlayers(game.getSelectedGame());	
		}
		else if (CollectionUtils.isEmpty(this.getSelectedPlayers()))
		{
			loadSelectedPlayers(game.getSelectedGame());
		}	
		
		this.setTeeTimeList(golfmain.getTeeTimesByGame(game.getSelectedGame()));		
		
		showTeeTimePicklist();
	}
	
	public void setPlayerPickLists(Game game) 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (playersPickListSource != null)
		{
			playersPickListSource.clear();
		}
		
		if (playersPickListTarget != null)
		{
			playersPickListTarget.clear();
		}
		Map<Integer, Player> selectedMap = new HashMap<Integer, Player>();
		
		for (int i = 0; i < this.getSelectedPlayers().size(); i++) 
		{
			playersPickListTarget.add(this.getSelectedPlayers().get(i));
			selectedMap.put(this.getSelectedPlayers().get(i).getPlayerID(), this.getSelectedPlayers().get(i));
		}
		
		for (int i = 0; i < golfmain.getFullPlayerList().size(); i++) 
		{
			Player tempPlayer = golfmain.getFullPlayerList().get(i);
			if (!selectedMap.containsKey(tempPlayer.getPlayerID()))
			{
				playersPickListSource.add(golfmain.getFullPlayerList().get(i));
			}
		}
		
		Collections.sort(playersPickListSource, new PlayerComparatorByLastName());
		Collections.sort(playersPickListTarget, new PlayerComparatorByLastName());
		
		this.setPlayersPickList(new DualListModel<Player>(playersPickListSource, playersPickListTarget));	
	}

	
	public String proceedToGameHandicaps()
	{
		log.info("User is done with tee times for game, proceed to enter player handicaps specific to this game");
		
		saveAndStayTeeTimesPickList();
		
		return "success";
	}
	
	public String proceedToTeeTimes()
	{
		log.info("User is done selecting players for game, proceed to tee times");
		
		saveAndStayPickList();
		
		onLoadTeeTimePickList();
		
		return "success";
	}	
	
	public void valueChangeGame(AjaxBehaviorEvent event) 
	{
		log.info("User picked a game on select players for game form");
			
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			loadSelectedPlayers(selectedOption);
			
			setPlayerPickLists(selectedOption);
			
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
			
			this.setTeeTimeList(golfmain.getTeeTimesByGame(selectedOption));		
			
			this.setRoundsForGame(golfmain.getRoundsForGame(selectedOption));
			
			showTeeTimePicklist();
		}
						
	}
	private String loadSelectedPlayers(Game game) 
	{
		log.info("load of gameSelectPlayers; loading those already selected");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		this.getSelectedPlayers().clear();
			
		roundsForGame = golfmain.getRoundsForGame(game);
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round round = roundsForGame.get(i);
			Player player = golfmain.getPlayerByPlayerID(round.getPlayerID());
			round.setPlayer(player);
			player.setTeamNumber(round.getTeamNumber());
			player.setTeeTime(round.getTeeTime());
			if (round.getRoundHandicap() != null)
			{
				player.setHandicap(round.getRoundHandicap());
			}
			this.getSelectedPlayers().add(player);
			
			for (int j = 0; j < golfmain.getFullPlayerList().size(); j++) 
			{
				Player fullPlayer = golfmain.getFullPlayerList().get(j);
				if (fullPlayer.getPlayerID() == round.getPlayerID())
				{
					fullPlayer.setTeamNumber(round.getTeamNumber());
					fullPlayer.setTeeTime(round.getTeeTime());
					if (round.getRoundHandicap() != null)
					{
						fullPlayer.setHandicap(round.getRoundHandicap());
					}
					golfmain.getFullPlayerList().set(j, fullPlayer);
					break;
				}
			}
		}
		
		if (roundsForGame.size() > 0)
		{
			GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
			gm.setDisableProceedToEnterScores(false);					
		}
		
		return "";
	}
		
	public String addPlayer()
	{
		operation = "Add";
		this.setFirstName("");
		this.setLastName("");
		this.setEmailAddress("");
		this.setUsername("");
		this.setHandicap(null);
		return "";
	}
	
	public String updatePlayer()
	{
		this.setPlayerID(this.getSelectedPlayer().getPlayerID());
		this.setEmailAddress(this.getSelectedPlayer().getEmailAddress());
		this.setFirstName(this.getSelectedPlayer().getFirstName());
		this.setLastName(this.getSelectedPlayer().getLastName());
		this.setOldUsername(this.getSelectedPlayer().getUsername());
		this.setUsername(this.getSelectedPlayer().getUsername());
		this.setHandicap(this.getSelectedPlayer().getHandicap());
		return "";
	}
	
	public String savePlayer()
	{
		log.info("user clicked Save Player from maintain player dialog");	
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		if (operation.equalsIgnoreCase("Add"))
		{
			log.info("user clicked Save Player from maintain player dialog, from an add");	
			
			//first need to make sure the chosen userid does not already exist in the system.
			Player existingPlayer = golfmain.getPlayerByUserName(this.getUsername());
			
			if (existingPlayer == null)
			{
				int newPlayerID = golfmain.addPlayer(this);
				golfmain.addUserAndAuthority(this.getUsername(), this.getUsername(), "USER"); //default their password to their username
				
				//need to save off the initial tee preferences here
				addInitialTeePrefs(newPlayerID);
				
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player successfully added",null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				//throw an error that this username already exists
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"this username already exists on the Database.  Pick another",null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				FacesContext.getCurrentInstance().validationFailed();
			}
				
			log.info("after add Player");
		}
		else if (operation.equalsIgnoreCase("Update"))
		{
			log.info("user clicked Save Player from maintain player dialog; from an update");			
			golfmain.updatePlayer(this);
			
			if (!this.getOldUsername().equalsIgnoreCase(this.getUsername()))
			{
				golfmain.addUserAndAuthority(this.getOldUsername(), this.getUsername(), "USER"); //default their password to their username
			}
			if (this.isResetPassword())
			{
				GolfUser gu = golfmain.getGolfUser(this.getUsername());
				golfmain.resetPassword(gu); //default their password to their username
			}
			if (!this.getOldRole().equalsIgnoreCase(this.getRole()))
			{
				GolfUser gu = golfmain.getGolfUser(this.getUsername());
				String[] arr = new String[1];
				arr[0] = this.getRole();
				gu.setUserRoles(arr);
				golfmain.updateRole(gu); 
			}
						
			log.info("after update Player");
		}
		else
		{
			log.info("neither add nor update from maintain player dialog - doing nothing");
		}
		
		return "";
			
	}
	
	private void addInitialTeePrefs(int newPlayerID) 
	{
		String teePreference = this.getTeePreference();
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		
		List<CourseTee> courseTees = gm.getCourseTeesList(); 			
		List<Course> courses = gm.getCourseSelections();
		
		for (int j = 0; j < courses.size(); j++) 
		{
			Course course = courses.get(j);
			PlayerTeePreference ptp = new PlayerTeePreference();
			ptp.setPlayerID(newPlayerID);
			ptp.setCourseID(course.getCourseID());
			ptp.setPlayerFullName(this.getFirstName() + " " +this.getLastName());
			for (int k = 0; k < courseTees.size(); k++) 
			{
				CourseTee courseTee = courseTees.get(k);
				
				if (courseTee.getCourseID() == course.getCourseID()
				&&  courseTee.getTeeColor().equalsIgnoreCase(teePreference))
				{
					ptp.setCourseTeeID(courseTee.getCourseTeeID());
					gm.addPlayerTeePreference(ptp);	
					break;
				}
			}
		}		
		
	}

	private void processSel()
	{
		Game game = BeanUtilJSF.getBean("pc_Game");
		
		if (game != null && game.getSelectedGame() != null)
		{
			GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");		
			gm.setDisableProceedToEnterScores(false);
		}	
		
		if (this.getSelectedPlayers().size() == game.getSelectedGame().getTotalPlayers())
		{
			this.setDisablePickTeams(false);
		}
		else
		{
			this.setDisablePickTeams(true);
		}
	}
	
	private void saveRounds(Map<Integer, Date> roundSignupDateTimesMap, Map<Integer, Integer> roundTeeSelectionsMap)
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		Game game = BeanUtilJSF.getBean("pc_Game");
		Round roundBean = BeanUtilJSF.getBean("pc_Round");
		
		if (game != null && game.getSelectedGame() != null)
		{
			roundBean.getSyncGameRoundList().clear();
			
			for (int i = 0; i < this.getSelectedPlayers().size(); i++) 
			{
				Player tempPlayer = this.getSelectedPlayers().get(i);
				
				Round newRound = new Round();
				newRound.setGameID(game.getSelectedGame().getGameID());
				newRound.setPlayerID(tempPlayer.getPlayerID());
				newRound.setPlayer(tempPlayer);
				newRound.setPlayerName(tempPlayer.getFirstName() + " " + tempPlayer.getLastName());
				newRound.setTeamNumber(tempPlayer.getTeamNumber());
				if (tempPlayer.getTeeTime() != null)
				{
					newRound.setTeeTimeID(tempPlayer.getTeeTime().getTeeTimeID());
					newRound.setTeeTime(tempPlayer.getTeeTime());
				}
				newRound.setRoundHandicap(tempPlayer.getHandicap());
				
				if (roundSignupDateTimesMap != null)
				{
					Date tempDate = roundSignupDateTimesMap.get(newRound.getPlayerID());
					
					if (tempDate != null)
					{
						newRound.setSignupDateTime(tempDate);
					}
				}
				
				if (roundTeeSelectionsMap != null)
				{
					Integer courseTeeID = roundTeeSelectionsMap.get(newRound.getPlayerID());
					
					if (courseTeeID == null)
					{
						newRound.setCourseTeeID(golfmain.getTeePreference(newRound.getPlayerID(), game.getSelectedGame().getCourseID()));
					}
					else
					{
						newRound.setCourseTeeID(courseTeeID);
					}
				}		
				
				golfmain.addRound(newRound);					
				
				roundBean.getSyncGameRoundList().add(newRound);				
			} 
		}
	}
	
	public String saveAndStayTeeTimesPickList()
	{
		log.info("saving player round records with tee times");
		
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (game != null && game.getSelectedGame() != null)
		{
			for (int i = 0; i < this.getTeeTimeList().size(); i++) 
			{
				TeeTime teeTime = this.getTeeTimeList().get(i);
				
				List<Player> tempPlayerList = new ArrayList<Player>();
				
				switch (i) 
				{
					case 0:
						
						tempPlayerList = this.getGameTeeTimeList1().getTarget();
						break;
						
					case 1:
						
						tempPlayerList = this.getGameTeeTimeList2().getTarget();
						break;	
						
					case 2:
						
						tempPlayerList = this.getGameTeeTimeList3().getTarget();
						break;	
						
					case 3:
						
						tempPlayerList = this.getGameTeeTimeList4().getTarget();
						break;	
						
					case 4:
		
						tempPlayerList = this.getGameTeeTimeList5().getTarget();
						break;
						
					case 5:
						
						tempPlayerList = this.getGameTeeTimeList6().getTarget();
						break;	
						
					case 6:
						
						tempPlayerList = this.getGameTeeTimeList7().getTarget();
						break;	
						
					case 7:
						
						tempPlayerList = this.getGameTeeTimeList8().getTarget();
						break;	
		
					default:
						break;
				}
				
				for (int j = 0; j < tempPlayerList.size(); j++) 
				{
					int playerID = tempPlayerList.get(j).getPlayerID();
					int gameID = game.getSelectedGame().getGameID();
					Round rd = golfmain.getRoundByGameandPlayer(gameID, playerID);
					rd.setTeeTimeID(teeTime.getTeeTimeID());
					rd.setTeeTime(teeTime);
					golfmain.updateRound(rd);
				}
				
			}
		}
		
		FacesContext context = FacesContext.getCurrentInstance();
	    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Tee Times Saved", "Tee Times Saved and staying on this page"));
	    
		return "";
	}
	
	public String saveAndStayPickList()
	{
		log.info("saving info from player picklist screen");
		
		//clear out first for this
		Game game = BeanUtilJSF.getBean("pc_Game");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		//we want to preserve the signup dates/times here
		Map<Integer,Date> roundSignupDateTimesMap = null;
		
		//we want to preserve the tee selections here
		Map<Integer,Integer> roundTeeSelectionsMap = null;
		
		if (game != null && game.getSelectedGame() != null)
		{
			roundSignupDateTimesMap = preserveSignupDateTimes(game.getSelectedGame());
			roundTeeSelectionsMap = preserveTeeSelections(game.getSelectedGame());
			golfmain.deleteRoundsFromDB(game.getSelectedGame().getGameID());
		}
		
		this.getSelectedPlayers().clear();
		
		for (int i = 0; i < this.getPlayersPickList().getTarget().size(); i++) 
		{
			this.getSelectedPlayers().add(this.getPlayersPickList().getTarget().get(i));
		}		
		
		saveRounds(roundSignupDateTimesMap, roundTeeSelectionsMap);
		
		loadSelectedPlayers(game); //this resets roundsforgame.  If they deleted a player then we need this list reset
		
		return "";
	}
	
	private Map<Integer, Date> preserveSignupDateTimes(Game selectedGame) 
	{
		Map<Integer,Date> roundSignupDateTimesMap = new HashMap<>();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		//get all the rounds for this game first.		
		List<Round> roundsForGame = golfmain.getRoundsForGame(selectedGame);
		
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round temprd = roundsForGame.get(i);
			roundSignupDateTimesMap.put(temprd.getPlayer().getPlayerID(), temprd.getSignupDateTime());
		}
		
		return roundSignupDateTimesMap;
	}
	
	private Map<Integer, Integer> preserveTeeSelections(Game selectedGame) 
	{
		Map<Integer,Integer> roundTeeSelectionsMap = new HashMap<>();
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		//get all the rounds for this game first.		
		List<Round> roundsForGame = golfmain.getRoundsForGame(selectedGame);
		
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round temprd = roundsForGame.get(i);
			roundTeeSelectionsMap.put(temprd.getPlayer().getPlayerID(), temprd.getCourseTeeID());
		}
		
		return roundTeeSelectionsMap;
	}

	public static class PlayerComparatorByHandicap implements Comparator<Player> 
	{
		public int compare(Player player1, Player player2)
		{
			return player1.getHandicap().compareTo(player2.getHandicap());
		}		
	}	
	
	public static class PlayerComparatorByLastName implements Comparator<Player> 
	{
		public int compare(Player player1, Player player2)
		{
			return player1.getLastName().compareTo(player2.getLastName());
		}		
	}	
	
	public String showTeeTimePicklist()
	{
		log.info("setting up tee times for selected game");
		
		setShowGameTeeTimeList3(false);
		setShowGameTeeTimeList4(false);
		setShowGameTeeTimeList5(false);
		setShowGameTeeTimeList6(false);
		setShowGameTeeTimeList7(false);
		setShowGameTeeTimeList8(false);
		
		Map<Integer,Player> sourcePlayerMap = new HashMap<Integer,Player>();
		for (int j = 0; j < this.getRoundsForGame().size(); j++) 
		{
			Round rd = this.getRoundsForGame().get(j);
			sourcePlayerMap.put(rd.getPlayer().getPlayerID(), rd.getPlayer());			
		}
		
		for (int i = 0; i < this.getTeeTimeList().size(); i++) 
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
			
			List<Player> sourcePlayerList = new ArrayList<Player>();
			List<Player> targetPlayerList = new ArrayList<Player>();
			
			for (int j = 0; j < this.getRoundsForGame().size(); j++) 
			{
				Round rd = this.getRoundsForGame().get(j);
				
				if (rd.getTeeTimeID() == teeTime.getTeeTimeID())
				{
					targetPlayerList.add(rd.getPlayer());
					if (sourcePlayerMap.containsKey(rd.getPlayer().getPlayerID()))
					{
						sourcePlayerMap.remove(rd.getPlayer().getPlayerID());
					}
				}
				
			}
			
			Collections.sort(sourcePlayerList, new PlayerComparatorByLastName());
			Collections.sort(targetPlayerList, new PlayerComparatorByLastName());
			
			switch (i) 
			{
				case 0:
					
					this.setGameTeeTimeList1(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption1(teeTime.getTeeTimeString());
					break;
					
				case 1:
					
					this.setGameTeeTimeList2(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption2(teeTime.getTeeTimeString());
					break;	
					
				case 2:
					
					this.setGameTeeTimeList3(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption3(teeTime.getTeeTimeString());
					setShowGameTeeTimeList3(true);
					break;	
					
				case 3:
					
					this.setGameTeeTimeList4(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption4(teeTime.getTeeTimeString());
					setShowGameTeeTimeList4(true);
					break;	
					
				case 4:
	
					this.setGameTeeTimeList5(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption5(teeTime.getTeeTimeString());
					setShowGameTeeTimeList5(true);
					break;
					
				case 5:
					
					this.setGameTeeTimeList6(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption6(teeTime.getTeeTimeString());
					setShowGameTeeTimeList6(true);
					break;	
					
				case 6:
					
					this.setGameTeeTimeList7(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption7(teeTime.getTeeTimeString());
					setShowGameTeeTimeList7(true);
					break;	
					
				case 7:
					
					this.setGameTeeTimeList8(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption8(teeTime.getTeeTimeString());
					setShowGameTeeTimeList8(true);
					break;	
	
				default:
					break;
			}
			
		} 
		
		//Once we're done with this loop, anything in sourcePlayerMap is not assigned a tee time yet.  Put them in any list where there's not 4 players.
		for (Entry<Integer, Player> entry : sourcePlayerMap.entrySet()) 
		{
			List<Player> sourcePlayerList = new ArrayList<Player>();
			List<Player> targetPlayerList = new ArrayList<Player>();
		
	        Player ply = entry.getValue();
	        for (int i = 0; i < this.getTeeTimeList().size(); i++) 
			{
	        	switch (i) 
				{
					case 0:
						
						sourcePlayerList = this.getGameTeeTimeList1().getSource();
						targetPlayerList = this.getGameTeeTimeList1().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList1(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						
						break;
						
					case 1:
						
						sourcePlayerList = this.getGameTeeTimeList2().getSource();
						targetPlayerList = this.getGameTeeTimeList2().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList2(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}				
						break;	
						
					case 2:
						
						sourcePlayerList = this.getGameTeeTimeList3().getSource();
						targetPlayerList = this.getGameTeeTimeList3().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList3(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 3:
						
						sourcePlayerList = this.getGameTeeTimeList4().getSource();
						targetPlayerList = this.getGameTeeTimeList4().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList4(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}			
						break;	
						
					case 4:
		
						sourcePlayerList = this.getGameTeeTimeList5().getSource();
						targetPlayerList = this.getGameTeeTimeList5().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList5(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						break;
						
					case 5:
						
						sourcePlayerList = this.getGameTeeTimeList6().getSource();
						targetPlayerList = this.getGameTeeTimeList6().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList6(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 6:
						
						sourcePlayerList = this.getGameTeeTimeList7().getSource();
						targetPlayerList = this.getGameTeeTimeList7().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList7(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 7:
						
						sourcePlayerList = this.getGameTeeTimeList8().getSource();
						targetPlayerList = this.getGameTeeTimeList8().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList8(new DualListModel<Player>(sourcePlayerList, targetPlayerList));
						}
						break;	
		
					default:
						break;
				}
			}
	        
	    }
			
		return "success";
	}
	
	public String proceedToEnterScores() 
	{
		log.info("User clicked proceed from player selection screen; saving new player round records and sending them to enter scores");
		
		//clear out first for this
		Game game = BeanUtilJSF.getBean("pc_Game");//we want to preserve the signup dates/times here
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Map<Integer,Date> roundSignupDateTimesMap = null;
		
		//we want to preserve the tee selections here
		Map<Integer,Integer> roundTeeSelectionsMap = null;
				
		
		if (game != null && game.getSelectedGame() != null)
		{
			roundSignupDateTimesMap = preserveSignupDateTimes(game.getSelectedGame());
			roundTeeSelectionsMap = preserveTeeSelections(game.getSelectedGame());
			golfmain.deleteRoundsFromDB(game.getSelectedGame().getGameID());
		}
				
		saveRounds(roundSignupDateTimesMap, roundTeeSelectionsMap);
		
		return "success";
	}

	public String selectMultiRowAjax(UnselectEvent<Player> event)
	{
		log.info("User unchecked a checkbox in Player selection list");
		
		processSel();
				
		return "";
	}
	
	public String selectMultiRowAjax(SelectEvent<Player> event)
	{
		log.info("User clicked a checkbox in Player selection list");
		
		processSel();
				
		return "";
	}
	
	public String selectRowAjax(SelectEvent<Player> event)
	{
		log.info("User clicked on a row in Player list");
		
		Player item = event.getObject();
		
		this.setSelectedPlayer(item);
		this.setDisablePlayersDialogButton(false); //if they've picked one, then they can update it
		
		//get the role for this player on the authorities table
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		GolfUser gu = golfmain.getGolfUser(item.getUsername());
		
		String userRole = gu.getUserRoles()[0];
		this.setRole(userRole);
		this.setOldRole(userRole);
		
		setOperation("Update");
		
		return "";
	}		
		
	public void onTransferTeeTime1(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
        if (!playersMoved.isEmpty())
        {
        	if (event.isAdd())
        	{
        		removePlayersFromTeeTimePickLists(playersMoved,0);       	
        	}
        	else
        	{
        		addPlayersToTeeTimePickLists(playersMoved,0); 
        	}
        }           
    } 

	public void onTransferTeeTime2(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,1);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,1); 
    	}       
    }  
	
	public void onTransferTeeTime3(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,2);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,2); 
    	}        
    } 
	
	public void onTransferTeeTime4(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,3);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,3); 
    	}       
    }  
	
	public void onTransferTeeTime5(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,4);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,4); 
    	}          
    } 
	
	public void onTransferTeeTime6(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,5);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,5); 
    	}          
    }  
	
	public void onTransferTeeTime7(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,6);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,6); 
    	}        
    } 
	
	public void onTransferTeeTime8(TransferEvent event) 
	{
		List<Player> playersMoved = new ArrayList<Player>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((Player) item);
        }
        
        //need to remove these from the other lists now
		if (event.isAdd())
    	{
    		removePlayersFromTeeTimePickLists(playersMoved,7);       	
    	}
    	else
    	{
    		addPlayersToTeeTimePickLists(playersMoved,7); 
    	}          
    }  
	
	private void addPlayersToTeeTimePickLists(List<Player> playersMoved, int leaveThisListAlone) 
	{
		for (int k = 0; k < playersMoved.size(); k++)
		{
			Player playerMoved = playersMoved.get(k);
			
			for (int i = 0; i < this.getTeeTimeList().size(); i++) 
			{
				if (leaveThisListAlone == i)
				{
					continue;
				}
				else
				{	
		        	switch (i) 
					{
						case 0:
							
							List<Player> sourceList0 = this.getGameTeeTimeList1().getSource();
							
							if (this.getGameTeeTimeList1().getTarget() != null 
							&&  this.getGameTeeTimeList1().getTarget().size() < 4)
							{
								sourceList0.add(playerMoved);										
							}
							
							break; 
							
						case 1:
							
							List<Player> sourceList1 = this.getGameTeeTimeList2().getSource();
							if (this.getGameTeeTimeList2().getTarget() != null 
							&&  this.getGameTeeTimeList2().getTarget().size() < 4)
							{
								sourceList1.add(playerMoved);										
							}
											
							break;
							
						case 2:
							
							List<Player> sourceList2 = this.getGameTeeTimeList3().getSource();
							if (this.getGameTeeTimeList3().getTarget() != null 
							&&  this.getGameTeeTimeList3().getTarget().size() < 4)
							{
								sourceList2.add(playerMoved);										
							}
														
							break;	
							
						case 3:
							
							List<Player> sourceList3 = this.getGameTeeTimeList4().getSource();
							if (this.getGameTeeTimeList4().getTarget() != null 
							&&  this.getGameTeeTimeList4().getTarget().size() < 4)
							{
								sourceList3.add(playerMoved);										
							}
								
							break;	
							
						case 4:
			
							List<Player> sourceList4 = this.getGameTeeTimeList5().getSource();
							if (this.getGameTeeTimeList5().getTarget() != null 
							&&  this.getGameTeeTimeList5().getTarget().size() < 4)
							{
								sourceList4.add(playerMoved);										
							}
								
							break;
							
						case 5:
							
							List<Player> sourceList5 = this.getGameTeeTimeList6().getSource();
							if (this.getGameTeeTimeList6().getTarget() != null 
							&&  this.getGameTeeTimeList6().getTarget().size() < 4)
							{
								sourceList5.add(playerMoved);										
							}
							
							break;	
							
						case 6:
							
							List<Player> sourceList6 = this.getGameTeeTimeList7().getSource();
							if (this.getGameTeeTimeList7().getTarget() != null 
							&&  this.getGameTeeTimeList7().getTarget().size() < 4)
							{
								sourceList6.add(playerMoved);										
							}
								
							break;	
							
						case 7:
							
							List<Player> sourceList7 = this.getGameTeeTimeList8().getSource();
							if (this.getGameTeeTimeList8().getTarget() != null 
							&&  this.getGameTeeTimeList8().getTarget().size() < 4)
							{
								sourceList7.add(playerMoved);										
							}
							
							break;	
			
						default:
							break;
					}
				}
						
			}		
		}
		
		
	}

	private void removePlayersFromTeeTimePickLists(List<Player> playersMoved, int leaveThisListAlone) 
	{
		for (int k = 0; k < playersMoved.size(); k++)
		{
			Player playerMoved = playersMoved.get(k);
			
			for (int i = 0; i < this.getTeeTimeList().size(); i++) 
			{
				if (leaveThisListAlone == i)
				{
					continue;
				}
				else
				{	
		        	switch (i) 
					{
						case 0:
							
							List<Player> sourceList0 = this.getGameTeeTimeList1().getSource();
							for (int j = 0; j < sourceList0.size(); j++) 
							{
								Player tempPlayer = sourceList0.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList0.remove(j);
									break;
								}
							}
							List<Player> targetList0 = this.getGameTeeTimeList1().getTarget();
							for (int j = 0; j < targetList0.size(); j++) 
							{
								Player tempPlayer = targetList0.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList0.remove(j);
									break;
								}
							}
							
							break; 
							
						case 1:
							
							List<Player> sourceList1 = this.getGameTeeTimeList2().getSource();
							for (int j = 0; j < sourceList1.size(); j++) 
							{
								Player tempPlayer = sourceList1.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList1.remove(j);
									break;
								}
							}
							List<Player> targetList1 = this.getGameTeeTimeList2().getTarget();
							for (int j = 0; j < targetList1.size(); j++) 
							{
								Player tempPlayer = targetList1.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList1.remove(j);
									break;
								}
							}
							break;
							
						case 2:
							
							List<Player> sourceList2 = this.getGameTeeTimeList3().getSource();
							for (int j = 0; j < sourceList2.size(); j++) 
							{
								Player tempPlayer = sourceList2.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList2.remove(j);
									break;
								}
							}
							List<Player> targetList2 = this.getGameTeeTimeList3().getTarget();
							for (int j = 0; j < targetList2.size(); j++) 
							{
								Player tempPlayer = targetList2.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList2.remove(j);
									break;
								}
							}
							break;	
							
						case 3:
							
							List<Player> sourceList3 = this.getGameTeeTimeList4().getSource();
							for (int j = 0; j < sourceList3.size(); j++) 
							{
								Player tempPlayer = sourceList3.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList3.remove(j);
									break;
								}
							}
							List<Player> targetList3 = this.getGameTeeTimeList4().getTarget();
							for (int j = 0; j < targetList3.size(); j++) 
							{
								Player tempPlayer = targetList3.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList3.remove(j);
									break;
								}
							}
							break;	
							
						case 4:
			
							List<Player> sourceList4 = this.getGameTeeTimeList5().getSource();
							for (int j = 0; j < sourceList4.size(); j++) 
							{
								Player tempPlayer = sourceList4.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList4.remove(j);
									break;
								}
							}
							List<Player> targetList4 = this.getGameTeeTimeList5().getTarget();
							for (int j = 0; j < targetList4.size(); j++) 
							{
								Player tempPlayer = targetList4.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList4.remove(j);
									break;
								}
							}
							break;
							
						case 5:
							
							List<Player> sourceList5 = this.getGameTeeTimeList6().getSource();
							for (int j = 0; j < sourceList5.size(); j++) 
							{
								Player tempPlayer = sourceList5.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList5.remove(j);
									break;
								}
							}
							List<Player> targetList5 = this.getGameTeeTimeList6().getTarget();
							for (int j = 0; j < targetList5.size(); j++) 
							{
								Player tempPlayer = targetList5.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList5.remove(j);
									break;
								}
							}
							break;	
							
						case 6:
							
							List<Player> sourceList6 = this.getGameTeeTimeList7().getSource();
							for (int j = 0; j < sourceList6.size(); j++) 
							{
								Player tempPlayer = sourceList6.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList6.remove(j);
									break;
								}
							}
							List<Player> targetList6 = this.getGameTeeTimeList7().getTarget();
							for (int j = 0; j < targetList6.size(); j++) 
							{
								Player tempPlayer = targetList6.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList6.remove(j);
									break;
								}
							}
							break;	
							
						case 7:
							
							List<Player> sourceList7 = this.getGameTeeTimeList8().getSource();
							for (int j = 0; j < sourceList7.size(); j++) 
							{
								Player tempPlayer = sourceList7.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									sourceList7.remove(j);
									break;
								}
							}
							List<Player> targetList7 = this.getGameTeeTimeList8().getTarget();
							for (int j = 0; j < targetList7.size(); j++) 
							{
								Player tempPlayer = targetList7.get(j);
								if (tempPlayer.getPlayerID() == playerMoved.getPlayerID())
								{
									targetList7.remove(j);
									break;
								}
							}
							break;	
			
						default:
							break;
					}
				}
						
			}		
		}
		
		
	}

	public void onTransfer(TransferEvent event) 
	{
        StringBuilder builder = new StringBuilder();
        for(Object item : event.getItems()) 
        {
            builder.append(((Player) item).getFullName()).append("<br />");
        }
         
        FacesMessage msg = new FacesMessage();
        msg.setSeverity(FacesMessage.SEVERITY_INFO);
        msg.setSummary("Items Transferred");
        msg.setDetail(builder.toString());
         
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }  
     
    public void onSelect(SelectEvent<Player> event) 
    {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Selected", event.getObject().getFullName()));
    }
     
    public void onUnselect(UnselectEvent<Player> event) {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Item Unselected", event.getObject().getFullName()));
    }
     
    public void onReorder() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "List Reordered", null));
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
        return Objects.equals(playerID, that);
    }
	
	public int getTeamNumber() {
		return teamNumber;
	}
	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}
	public BigDecimal getHandicap() {
		return handicap;
	}
	public void setHandicap(BigDecimal handicap) {
		this.handicap = handicap;
	}
	public int getGrossScore() {
		return grossScore;
	}
	public void setGrossScore(int grossScore) {
		this.grossScore = grossScore;
	}
	public BigDecimal getNetScore() {
		return netScore;
	}
	public void setNetScore(BigDecimal netScore) {
		this.netScore = netScore;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) 
	{
		this.firstName = firstName;
		this.setFullName(firstName + " " + lastName);
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) 
	{
		this.lastName = lastName;
		this.setFullName(firstName + " " + lastName);
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public int getPlayerID() {
		return playerID;
	}
	public void setPlayerID(int playerID) {
		this.playerID = playerID;
	}
	
	public String toString()
	{
		return "Player name: " + fullName + " handicap: " + handicap;
	}
	
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	
	public Player getSelectedPlayer() {
		return selectedPlayer;
	}

	public void setSelectedPlayer(Player selectedPlayer) {
		this.selectedPlayer = selectedPlayer;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public boolean isDisablePlayersDialogButton() {
		return disablePlayersDialogButton;
	}

	public void setDisablePlayersDialogButton(boolean disablePlayersDialogButton) {
		this.disablePlayersDialogButton = disablePlayersDialogButton;
	}

	public List<Player> getSelectedPlayers() {
		return selectedPlayers;
	}

	public void setSelectedPlayers(List<Player> selectedPlayers) {
		this.selectedPlayers = selectedPlayers;
	}

	public List<Round> getRoundsForGame() {
		return roundsForGame;
	}

	public void setRoundsForGame(List<Round> roundsForGame) {
		this.roundsForGame = roundsForGame;
	}

	public int getTotalSelectedPlayers() 
	{
		int tempInt = 0;
		
		if (this.getSelectedPlayers() != null && this.getSelectedPlayers().size() > 0)
		{
			tempInt = this.getSelectedPlayers().size();
		}
		return tempInt;
	}
	
	public int getPickListTargetPlayersSelected() 
	{
		int tempInt = 0;
		
		if (this.getPlayersPickList() != null 
		&& this.getPlayersPickList().getTarget() != null 		
		&& this.getPlayersPickList().getTarget().size() > 0)
		{
			tempInt = this.getPlayersPickList().getTarget().size();
		}
	
		return tempInt;
	}

	public boolean isDisablePickTeams() {
		return disablePickTeams;
	}

	public void setDisablePickTeams(boolean disablePickTeams) {
		this.disablePickTeams = disablePickTeams;
	}

	public TeeTime getTeeTime() {
		return teeTime;
	}

	public void setTeeTime(TeeTime teeTime) {
		this.teeTime = teeTime;
	}

	public List<TeeTime> getTeeTimeList() {
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) {
		this.teeTimeList = teeTimeList;
	}

	public DualListModel<Player> getPlayersPickList() {
		return playersPickList;
	}

	public void setPlayersPickList(DualListModel<Player> playersPickList) {
		this.playersPickList = playersPickList;
	}

	public List<Player> getPlayersPickListSource() {
		return playersPickListSource;
	}

	public void setPlayersPickListSource(List<Player> playersPickListSource) {
		this.playersPickListSource = playersPickListSource;
	}

	public List<Player> getPlayersPickListTarget() {
		return playersPickListTarget;
	}

	public void setPlayersPickListTarget(List<Player> playersPickListTarget) {
		this.playersPickListTarget = playersPickListTarget;
	}
	
	public boolean isShowGameTeeTimeList3() {
		return showGameTeeTimeList3;
	}

	public void setShowGameTeeTimeList3(boolean showGameTeeTimeList3) {
		this.showGameTeeTimeList3 = showGameTeeTimeList3;
	}

	public boolean isShowGameTeeTimeList4() {
		return showGameTeeTimeList4;
	}

	public void setShowGameTeeTimeList4(boolean showGameTeeTimeList4) {
		this.showGameTeeTimeList4 = showGameTeeTimeList4;
	}

	public boolean isShowGameTeeTimeList5() {
		return showGameTeeTimeList5;
	}

	public void setShowGameTeeTimeList5(boolean showGameTeeTimeList5) {
		this.showGameTeeTimeList5 = showGameTeeTimeList5;
	}

	public boolean isShowGameTeeTimeList6() {
		return showGameTeeTimeList6;
	}

	public void setShowGameTeeTimeList6(boolean showGameTeeTimeList6) {
		this.showGameTeeTimeList6 = showGameTeeTimeList6;
	}

	public boolean isShowGameTeeTimeList7() {
		return showGameTeeTimeList7;
	}

	public void setShowGameTeeTimeList7(boolean showGameTeeTimeList7) {
		this.showGameTeeTimeList7 = showGameTeeTimeList7;
	}

	public boolean isShowGameTeeTimeList8() {
		return showGameTeeTimeList8;
	}

	public void setShowGameTeeTimeList8(boolean showGameTeeTimeList8) {
		this.showGameTeeTimeList8 = showGameTeeTimeList8;
	}

	public DualListModel<Player> getGameTeeTimeList1() {
		return gameTeeTimeList1;
	}

	public void setGameTeeTimeList1(DualListModel<Player> gameTeeTimeList1) {
		this.gameTeeTimeList1 = gameTeeTimeList1;
	}

	public DualListModel<Player> getGameTeeTimeList2() {
		return gameTeeTimeList2;
	}

	public void setGameTeeTimeList2(DualListModel<Player> gameTeeTimeList2) {
		this.gameTeeTimeList2 = gameTeeTimeList2;
	}

	public DualListModel<Player> getGameTeeTimeList3() {
		return gameTeeTimeList3;
	}

	public void setGameTeeTimeList3(DualListModel<Player> gameTeeTimeList3) {
		this.gameTeeTimeList3 = gameTeeTimeList3;
	}

	public DualListModel<Player> getGameTeeTimeList4() {
		return gameTeeTimeList4;
	}

	public void setGameTeeTimeList4(DualListModel<Player> gameTeeTimeList4) {
		this.gameTeeTimeList4 = gameTeeTimeList4;
	}

	public DualListModel<Player> getGameTeeTimeList5() {
		return gameTeeTimeList5;
	}

	public void setGameTeeTimeList5(DualListModel<Player> gameTeeTimeList5) {
		this.gameTeeTimeList5 = gameTeeTimeList5;
	}

	public DualListModel<Player> getGameTeeTimeList6() {
		return gameTeeTimeList6;
	}

	public void setGameTeeTimeList6(DualListModel<Player> gameTeeTimeList6) {
		this.gameTeeTimeList6 = gameTeeTimeList6;
	}

	public DualListModel<Player> getGameTeeTimeList7() {
		return gameTeeTimeList7;
	}

	public void setGameTeeTimeList7(DualListModel<Player> gameTeeTimeList7) {
		this.gameTeeTimeList7 = gameTeeTimeList7;
	}

	public DualListModel<Player> getGameTeeTimeList8() {
		return gameTeeTimeList8;
	}

	public void setGameTeeTimeList8(DualListModel<Player> gameTeeTimeList8) {
		this.gameTeeTimeList8 = gameTeeTimeList8;
	}

	public String getGameTeeTimeListCaption1() {
		return gameTeeTimeListCaption1;
	}

	public void setGameTeeTimeListCaption1(String gameTeeTimeListCaption1) {
		this.gameTeeTimeListCaption1 = gameTeeTimeListCaption1;
	}

	public String getGameTeeTimeListCaption2() {
		return gameTeeTimeListCaption2;
	}

	public void setGameTeeTimeListCaption2(String gameTeeTimeListCaption2) {
		this.gameTeeTimeListCaption2 = gameTeeTimeListCaption2;
	}

	public String getGameTeeTimeListCaption3() {
		return gameTeeTimeListCaption3;
	}

	public void setGameTeeTimeListCaption3(String gameTeeTimeListCaption3) {
		this.gameTeeTimeListCaption3 = gameTeeTimeListCaption3;
	}

	public String getGameTeeTimeListCaption4() {
		return gameTeeTimeListCaption4;
	}

	public void setGameTeeTimeListCaption4(String gameTeeTimeListCaption4) {
		this.gameTeeTimeListCaption4 = gameTeeTimeListCaption4;
	}

	public String getGameTeeTimeListCaption5() {
		return gameTeeTimeListCaption5;
	}

	public void setGameTeeTimeListCaption5(String gameTeeTimeListCaption5) {
		this.gameTeeTimeListCaption5 = gameTeeTimeListCaption5;
	}

	public String getGameTeeTimeListCaption6() {
		return gameTeeTimeListCaption6;
	}

	public void setGameTeeTimeListCaption6(String gameTeeTimeListCaption6) {
		this.gameTeeTimeListCaption6 = gameTeeTimeListCaption6;
	}

	public String getGameTeeTimeListCaption7() {
		return gameTeeTimeListCaption7;
	}

	public void setGameTeeTimeListCaption7(String gameTeeTimeListCaption7) {
		this.gameTeeTimeListCaption7 = gameTeeTimeListCaption7;
	}

	public String getGameTeeTimeListCaption8() {
		return gameTeeTimeListCaption8;
	}

	public void setGameTeeTimeListCaption8(String gameTeeTimeListCaption8) {
		this.gameTeeTimeListCaption8 = gameTeeTimeListCaption8;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getOldUsername() {
		return oldUsername;
	}

	public void setOldUsername(String oldUsername) {
		this.oldUsername = oldUsername;
	}

	public String getLoggedInPlayerName() 
	{
		//assign who the logged in player is using their login username
		
		GolfUser gu = BeanUtilJSF.getBean("pc_GolfUser");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		if (gu == null || gu.getUserName() == null) //first time here gu will have no user name.  henceforth it should..
		{
			String tempUserName = getTempUserName();
			gu = golfmain.getGolfUser(tempUserName);
			
			if (gu != null && gu.getUserName() != null)
			{
				Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(gu.getUserName());			
				if (tempPlayer != null)
				{
					this.setLoggedInPlayerName(tempPlayer.getFullName());
				}
				else
				{
					log.error("unable to determine who logged in player is - this could be a problem!");
				}
			}
			else
			{
				log.error("unable to determine who logged in player is - this could be a problem!");				
			}
		}
		else
		{
			Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(gu.getUserName());			
			if (tempPlayer != null)
			{
				this.setLoggedInPlayerName(tempPlayer.getFullName());
			}
		}		
		
		log.info("currently logged in user is: " + loggedInPlayerName);		
		
		return loggedInPlayerName;
	}

	public void setLoggedInPlayerName(String loggedInPlayerName) 
	{
		this.loggedInPlayerName = loggedInPlayerName;
	}

	public boolean isResetPassword() {
		return resetPassword;
	}

	public void setResetPassword(boolean resetPassword) {
		this.resetPassword = resetPassword;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getOldRole() {
		return oldRole;
	}

	public void setOldRole(String oldRole) {
		this.oldRole = oldRole;
	}
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}

	public String getLoggedInPlayerEmail() 
	{
		//assign who the logged in player is using their login username
		
		GolfUser gu = BeanUtilJSF.getBean("pc_GolfUser");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		if (gu == null || gu.getUserName() == null) //first time here gu will have no user name.  henceforth it should..
		{
			String tempUserName = getTempUserName();
			gu = golfmain.getGolfUser(tempUserName);
			
			if (gu != null && gu.getUserName() != null)
			{
				Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(gu.getUserName());			
				if (tempPlayer != null)
				{
					this.setLoggedInPlayerEmail(tempPlayer.getEmailAddress());
				}
				else
				{
					log.error("unable to determine who logged in player is - this could be a problem!");
				}
			}
			else
			{
				log.error("unable to determine who logged in player is - this could be a problem!");				
			}
		}
		else
		{
			Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(gu.getUserName());			
			if (tempPlayer != null)
			{
				this.setLoggedInPlayerEmail(tempPlayer.getEmailAddress());
			}
		}		
		
		log.info("currently logged in user's email is: " + loggedInPlayerEmail);		
		
		return loggedInPlayerEmail;
	}

	public void setLoggedInPlayerEmail(String loggedInPlayerEmail) {
		this.loggedInPlayerEmail = loggedInPlayerEmail;
	}

	public String getTeePreference() {
		return teePreference;
	}

	public void setTeePreference(String teePreference) {
		this.teePreference = teePreference;
	}

}
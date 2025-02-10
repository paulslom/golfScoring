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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.DualListModel;

import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.util.Utils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pc_Player")
@SessionScoped
public class Player implements Serializable
{	
	private static final long serialVersionUID = 4089402354585236177L;
	private static Logger logger = LogManager.getLogger(Game.class);
	
	private String playerID;
	private int oldPlayerID;
	private String username;
	private String oldUsername = "";
	private String role;
	private String oldRole = "";
	private String firstName;
	private String lastName;
	private String fullName;
	private BigDecimal handicap;
	private String emailAddress;
	private boolean active;
	
	private boolean resetPassword;
	
	private Player selectedPlayer;
	
	private boolean disablePlayersDialogButton = true;
	private List<DynamoPlayer> selectedPlayersList = new ArrayList<>();
		
	private DualListModel<DynamoPlayer> playersPickList = new DualListModel<>();
	private List<DynamoPlayer> playersPickListSource = new ArrayList<>();
	private List<DynamoPlayer> playersPickListTarget = new ArrayList<>();
	
	private DualListModel<DynamoPlayer> gameTeeTimeList1 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList2 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList3 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList4 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList5 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList6 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList7 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList8 = new DualListModel<>();
	
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
	
	@Inject GolfMain golfmain;
	@Inject Game game;

	public void onLoadPlayerPickList() 
	{
		try
		{
			loadSelectedPlayers(game.getSelectedGame());
			setPlayerPickLists(game.getSelectedGame());			

		}
		catch (Exception e)
		{
			logger.error("onLoadPlayerPickList failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"onLoadPlayerPickList failed: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void onLoadTeeTimePickList() 
	{
		try
		{
			loadSelectedPlayers(golfmain.getFullGameList().get(0));
			this.setTeeTimeList(golfmain.getTeeTimesByGame(golfmain.getFullGameList().get(0)));			
			showTeeTimePicklist();
		}
		catch (Exception e)
		{
			logger.error("onLoadTeeTimePickList failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"onLoadTeeTimePickList: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void setPlayerPickLists(DynamoGame dynamoGame) 
	{
		if (playersPickListSource != null)
		{
			playersPickListSource.clear();
		}
		
		if (playersPickListTarget != null)
		{
			playersPickListTarget.clear();
		}
		Map<String, DynamoPlayer> selectedMap = new HashMap<>();
		
		for (int i = 0; i < this.getSelectedPlayersList().size(); i++) 
		{
			playersPickListTarget.add(this.getSelectedPlayersList().get(i));
			selectedMap.put(this.getSelectedPlayersList().get(i).getPlayerID(), this.getSelectedPlayersList().get(i));
		}
		
		for (int i = 0; i < golfmain.getFullPlayerList().size(); i++) 
		{
			DynamoPlayer tempPlayer = golfmain.getFullPlayerList().get(i);
			if (!selectedMap.containsKey(tempPlayer.getPlayerID()))
			{
				playersPickListSource.add(golfmain.getFullPlayerList().get(i));
			}
		}
		
		Collections.sort(playersPickListSource, new PlayerComparatorByLastName());
		Collections.sort(playersPickListTarget, new PlayerComparatorByLastName());
		
		this.setPlayersPickList(new DualListModel<DynamoPlayer>(playersPickListSource, playersPickListTarget));	
	}

	public String proceedToGameHandicaps()
	{
		logger.info("User is done with tee times for game, proceed to enter player handicaps specific to this game");
		
		saveAndStayTeeTimesPickList();

		return "/auth/admin/gameHandicaps.xhtml";
	}
	
	public String proceedToTeeTimes()
	{
		logger.info("User is done selecting players for game, proceed to tee times");
		
		try
		{
			saveAndStayPickList();		
			onLoadTeeTimePickList();		
		}
		catch (Exception e)
		{
			logger.error("proceedToTeeTimes failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"proceed to tee times failed: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
			return "";
		}
		return "/auth/admin/teeTimePickList.xhtml";
	}	
	
	public void valueChangeGamePlayerPicklist(AjaxBehaviorEvent event) 
	{
		logger.info("User picked a game");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
			
			String gameID = (String)selectonemenu.getValue();
			
			if (gameID != null)
			{
				game.setSelectedGame(golfmain.getGameByGameID(gameID));
				loadSelectedPlayers(game.getSelectedGame());
				setPlayerPickLists(game.getSelectedGame());
			}
		}
		catch (Exception e)
		{
			logger.error("valueChangeGamePlayerPicklist failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"valueChangeGamePlayerPicklist failed: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
						
	}
	
	public void valueChangeGamePlayerTeeTimes(AjaxBehaviorEvent event) 
	{
		logger.info("User picked a game on select players for game form");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
		String gameID = (String)selectonemenu.getValue();
		
		if (gameID != null)
		{
			game.setSelectedGame(golfmain.getGameByGameID(gameID));			
			this.setTeeTimeList(golfmain.getTeeTimesByGame(game.getSelectedGame()));						
			showTeeTimePicklist();
		}
						
	}
		
	private String loadSelectedPlayers(DynamoGame dynamoGame) throws Exception
	{
		logger.info("load of gameSelectPlayers; loading those already selected");
		
		this.getSelectedPlayersList().clear();
		
		List<Round> roundsForGame = game.getRoundsForGame().get(dynamoGame.getGameID());
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round round = roundsForGame.get(i);
			DynamoPlayer player = golfmain.getPlayerByPlayerID(round.getPlayerID());
			this.getSelectedPlayersList().add(player);
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
		this.setActive(this.getSelectedPlayer().isActive());
		return "";
	}
	
	public String savePlayer()
	{
		logger.info("user clicked Save Player from maintain player dialog");	
		
		try
		{
			if (operation.equalsIgnoreCase("Add"))
			{
				logger.info("user clicked Save Player from maintain player dialog, from an add");	
				
				//first need to make sure the chosen userid does not already exist in the system.
				DynamoPlayer existingPlayer = golfmain.getPlayerByUserName(this.getUsername());
				
				if (existingPlayer == null)
				{
					String newPlayerID = golfmain.addPlayer(this);
					golfmain.addUser(this.getUsername(), this.getUsername(), "USER"); //default their password to their username
					
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
					
				logger.info("after add Player");
			}
			else if (operation.equalsIgnoreCase("Update"))
			{
				logger.info("user clicked Save Player from maintain player dialog; from an update");			
				golfmain.updatePlayer(Utils.convertPlayerToDynamoPlayer(this));
				
				if (!this.getOldUsername().equalsIgnoreCase(this.getUsername()))
				{
					golfmain.addUser(this.getOldUsername(), this.getUsername(), "USER"); //default their password to their username
				}
				if (this.isResetPassword())
				{
					GolfUser gu = golfmain.getGolfUser(this.getUsername());
					gu.setPassword(gu.getUserName()); //default their password to their username
					golfmain.updateUser(this.getUsername(), gu.getPassword(), role);
				}
				if (!this.getOldRole().equalsIgnoreCase(this.getRole()))
				{
					GolfUser gu = golfmain.getGolfUser(this.getUsername());
					gu.setUserRole(this.getRole());
					golfmain.updateRole(gu); 
				}
							
				logger.info("after update Player");
			}
			else
			{
				logger.info("neither add nor update from maintain player dialog - doing nothing");
			}
		}
		catch (Exception e)
		{
			logger.error("savePlayer failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when saving player: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    

		}
		return "";
			
	}
	
	private void addInitialTeePrefs(String newPlayerID) throws Exception 
	{
		String teePreference = this.getTeePreference();
		
		List<DynamoCourseTee> courseTees = golfmain.getCourseTeesList();
		List<Course> courses = golfmain.getCoursesList();
		
		for (int j = 0; j < courses.size(); j++) 
		{
			Course course = courses.get(j);
			PlayerTeePreference playerTeePreference = new PlayerTeePreference();
			playerTeePreference.setPlayerID(newPlayerID);
			playerTeePreference.setCourseID(course.getCourseID());
			playerTeePreference.setPlayerFullName(this.getFirstName() + " " +this.getLastName());
			for (int k = 0; k < courseTees.size(); k++) 
			{
				DynamoCourseTee courseTee = courseTees.get(k);
				
				if (courseTee.getCourseID().equalsIgnoreCase(course.getCourseID())
				&&  courseTee.getTeeColor().equalsIgnoreCase(teePreference))
				{
					playerTeePreference.setCourseTeeID(courseTee.getCourseTeeID());
					golfmain.addPlayerTeePreference(playerTeePreference);
					break;
				}
			}
		}		
		
	}

	private void saveRounds(Map<String, Date> roundSignupDateTimesMap, Map<String, String> roundTeeSelectionsMap, DynamoGame dynamoGame) throws Exception
	{
		if (dynamoGame != null)
		{
			try
			{
				int totalPlayersForGame = dynamoGame.getTotalPlayers();
				int totalRoundsForGame = 0;
				
				for (int i = 0; i < this.getPlayersPickList().getTarget().size(); i++) 
				{
					DynamoPlayer tempPlayer = (DynamoPlayer) this.getPlayersPickList().getTarget().get(i);
					
					totalRoundsForGame++;
					
					Round round = new Round();
					round.setGameID(dynamoGame.getGameID());
					round.setPlayerID(tempPlayer.getPlayerID());
					round.setPlayer(tempPlayer);
					round.setPlayerName(tempPlayer.getFirstName() + " " + tempPlayer.getLastName());					
					round.setRoundHandicap(tempPlayer.getHandicap());					
					round.setSignupDateTime(null); //let upsert call determine whether to use existing value or new date time stamp there
					
					if (roundSignupDateTimesMap != null)
					{
						Date tempDate = roundSignupDateTimesMap.get(round.getPlayerID());
						
						if (tempDate != null)
						{
							round.setSignupDateTime(tempDate);
						}
					}
					
					if (roundTeeSelectionsMap != null)
					{
						String courseTeeID = roundTeeSelectionsMap.get(round.getPlayerID());
						
						if (courseTeeID == null)
						{
							round.setCourseTeeID(golfmain.getTeePreference(round.getPlayerID(), dynamoGame.getCourseID()));
						}
						else
						{
							round.setCourseTeeID(courseTeeID);
						}
					}		
					
					golfmain.addRound(round);
				}				
				
				if (totalRoundsForGame > totalPlayersForGame)
				{
					String msg = "Player:saveRounds: We have more rounds than players for game, this is a big problem.  Total rounds = " + totalRoundsForGame + " and total players for this game = " + totalPlayersForGame;
					throw new Exception(msg);					
				}
			}
			catch (Exception e)
			{
				logger.error(e.getMessage(), e);
				FacesContext context = FacesContext.getCurrentInstance();
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
			}
			
		}
		
	}
	
	public String saveAndStayTeeTimesPickList()
	{
		logger.info("saving player round records with tee times");
		
		try
		{			
			if (game.getSelectedGame() == null)
			{
				throw new Exception("no game selected - selected game null");
			}
			else
			{				
				int totalPlayersForGame = game.getSelectedGame().getTotalPlayers();
				int totalRoundsForGame = 0;
			
				for (int i = 0; i < this.getTeeTimeList().size(); i++) 
				{
					TeeTime teeTime = this.getTeeTimeList().get(i);
					
					List<DynamoPlayer> tempPlayerList = new ArrayList<>();
					
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
						totalRoundsForGame++;
						String playerID = tempPlayerList.get(j).getPlayerID();
						String gameID = game.getSelectedGame().getGameID();
						Round rd = golfmain.getRoundByGameandPlayer(gameID, playerID);
						rd.setTeeTimeID(teeTime.getTeeTimeID());
						rd.setTeeTime(teeTime);
						golfmain.updateRound(rd);
					}
					
					if (totalRoundsForGame > totalPlayersForGame)
					{
						String msg = "Player:saveAndStayTeeTimesPickList: We have more rounds than players for game, this is a big problem.  Total rounds = " + totalRoundsForGame + " and total players for this game = " + totalPlayersForGame;
						throw new Exception(msg);					
					}
					
				}
			}
			
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Tee Times Saved", "Tee Times Saved and staying on this page"));
		}
		catch (Exception e)
		{
			logger.error("Exception in saveAndStayTeeTimesPickList: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in saveAndStayTeeTimesPickList: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}	
		return "";
	}
	
	public String saveAndStayPickList() throws Exception
	{
		logger.info("saving info from player picklist screen");
		
		//clear out first for this
		//we want to preserve the signup dates/times here
		Map<String,Date> roundSignupDateTimesMap = null;
		
		//we want to preserve the tee selections here
		Map<String, String> roundTeeSelectionsMap = null;
		
		if (game.getSelectedGame() != null)
		{
			roundSignupDateTimesMap = preserveSignupDateTimes(game.getSelectedGame());
			roundTeeSelectionsMap = preserveTeeSelections(game.getSelectedGame());
			golfmain.deleteRoundsFromDB(game.getSelectedGame().getGameID());
						
			saveRounds(roundSignupDateTimesMap, roundTeeSelectionsMap, game.getSelectedGame());
			
			loadSelectedPlayers(game.getSelectedGame()); //this resets roundsforgame.  If they deleted a player then we need this list reset

		}		
			
		return "";
	}
	
	private Map<String, Date> preserveSignupDateTimes(DynamoGame dynamoGame) 
	{
		Map<String,Date> roundSignupDateTimesMap = new HashMap<>();
			
		//get all the rounds for this game first.		
		List<Round> roundsForGame = golfmain.getRoundsForGame(dynamoGame);
		
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round temprd = roundsForGame.get(i);
			roundSignupDateTimesMap.put(temprd.getPlayer().getPlayerID(), temprd.getSignupDateTime());
		}
		
		return roundSignupDateTimesMap;
	}
	
	private Map<String, String> preserveTeeSelections(DynamoGame dynamoGame) 
	{
		Map<String, String> roundTeeSelectionsMap = new HashMap<>();
		
		//get all the rounds for this game first.		
		List<Round> roundsForGame = golfmain.getRoundsForGame(dynamoGame);
		
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
	
	public static class PlayerComparatorByLastName implements Comparator<DynamoPlayer> 
	{
		public int compare(DynamoPlayer player1, DynamoPlayer player2)
		{
			return player1.getLastName().compareTo(player2.getLastName());
		}		
	}	
	
	public String showTeeTimePicklist()
	{
		logger.info("setting up tee times for selected game");
		
		setShowGameTeeTimeList3(false);
		setShowGameTeeTimeList4(false);
		setShowGameTeeTimeList5(false);
		setShowGameTeeTimeList6(false);
		setShowGameTeeTimeList7(false);
		setShowGameTeeTimeList8(false);
		
		Map<String,DynamoPlayer> sourcePlayerMap = new HashMap<>();
		List<Round> roundsForGameList = game.getRoundsForGame().get(game.getSelectedGame().getGameID());
		
		for (int j = 0; j < roundsForGameList.size(); j++) 
		{
			Round rd = roundsForGameList.get(j);
			sourcePlayerMap.put(rd.getPlayer().getPlayerID(), rd.getPlayer());			
		}
		
		for (int i = 0; i < this.getTeeTimeList().size(); i++) 
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
			
			List<DynamoPlayer> sourcePlayerList = new ArrayList<>();
			List<DynamoPlayer> targetPlayerList = new ArrayList<>();
			
			for (int j = 0; j < roundsForGameList.size(); j++) 
			{
				Round rd = roundsForGameList.get(j);
				
				if (rd.getTeeTimeID() != null && rd.getTeeTimeID().equalsIgnoreCase(teeTime.getTeeTimeID()))
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
					
					this.setGameTeeTimeList1(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption1(teeTime.getTeeTimeString());
					break;
					
				case 1:
					
					this.setGameTeeTimeList2(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption2(teeTime.getTeeTimeString());
					break;	
					
				case 2:
					
					this.setGameTeeTimeList3(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption3(teeTime.getTeeTimeString());
					setShowGameTeeTimeList3(true);
					break;	
					
				case 3:
					
					this.setGameTeeTimeList4(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption4(teeTime.getTeeTimeString());
					setShowGameTeeTimeList4(true);
					break;	
					
				case 4:
	
					this.setGameTeeTimeList5(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption5(teeTime.getTeeTimeString());
					setShowGameTeeTimeList5(true);
					break;
					
				case 5:
					
					this.setGameTeeTimeList6(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption6(teeTime.getTeeTimeString());
					setShowGameTeeTimeList6(true);
					break;	
					
				case 6:
					
					this.setGameTeeTimeList7(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption7(teeTime.getTeeTimeString());
					setShowGameTeeTimeList7(true);
					break;	
					
				case 7:
					
					this.setGameTeeTimeList8(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
					setGameTeeTimeListCaption8(teeTime.getTeeTimeString());
					setShowGameTeeTimeList8(true);
					break;	
	
				default:
					break;
			}
			
		} 
		
		//Once we're done with this loop, anything in sourcePlayerMap is not assigned a tee time yet.  Put them in any list where there's not 4 players.
		for (Entry<String, DynamoPlayer> entry : sourcePlayerMap.entrySet()) 
		{
			List<DynamoPlayer> sourcePlayerList = new ArrayList<>();
			List<DynamoPlayer> targetPlayerList = new ArrayList<>();
		
			DynamoPlayer ply = entry.getValue();
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
							this.setGameTeeTimeList1(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						
						break;
						
					case 1:
						
						sourcePlayerList = this.getGameTeeTimeList2().getSource();
						targetPlayerList = this.getGameTeeTimeList2().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList2(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}				
						break;	
						
					case 2:
						
						sourcePlayerList = this.getGameTeeTimeList3().getSource();
						targetPlayerList = this.getGameTeeTimeList3().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList3(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 3:
						
						sourcePlayerList = this.getGameTeeTimeList4().getSource();
						targetPlayerList = this.getGameTeeTimeList4().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList4(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}			
						break;	
						
					case 4:
		
						sourcePlayerList = this.getGameTeeTimeList5().getSource();
						targetPlayerList = this.getGameTeeTimeList5().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList5(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						break;
						
					case 5:
						
						sourcePlayerList = this.getGameTeeTimeList6().getSource();
						targetPlayerList = this.getGameTeeTimeList6().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList6(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 6:
						
						sourcePlayerList = this.getGameTeeTimeList7().getSource();
						targetPlayerList = this.getGameTeeTimeList7().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList7(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						break;	
						
					case 7:
						
						sourcePlayerList = this.getGameTeeTimeList8().getSource();
						targetPlayerList = this.getGameTeeTimeList8().getTarget();
						
						if (targetPlayerList != null && targetPlayerList.size() < 4)
						{
							sourcePlayerList.add(ply);
							this.setGameTeeTimeList8(new DualListModel<DynamoPlayer>(sourcePlayerList, targetPlayerList));
						}
						break;	
		
					default:
						break;
				}
			}
	        
	    }
			
		return "success";
	}
	
	public String selectMultiRowAjax(UnselectEvent<Player> event)
	{
		logger.info("User unchecked a checkbox in Player selection list");				
		return "";
	}
	
	public String selectMultiRowAjax(SelectEvent<Player> event)
	{
		logger.info("User clicked a checkbox in Player selection list");				
		return "";
	}
	
	public String selectRowAjax(SelectEvent<Player> event)
	{
		logger.info("User clicked on a row in Player list");
		
		Player item = event.getObject();
		
		this.setSelectedPlayer(item);
		this.setDisablePlayersDialogButton(false); //if they've picked one, then they can update it
		
		//get the role for this player on the authorities table
		GolfUser gu = golfmain.getGolfUser(item.getUsername());
		
		String userRole = gu.getUserRole();
		this.setRole(userRole);
		this.setOldRole(userRole);
		
		setOperation("Update");
		
		return "";
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
	
	public void playerPLMovement(TransferEvent event) 
	{
		logger.info("Player selected or unselected for game");
	}
	
	public void onTransferTeeTime1(TransferEvent event) 
	{
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
		List<DynamoPlayer> playersMoved = new ArrayList<>();
		
		for(Object item : event.getItems()) 
        {
            playersMoved.add((DynamoPlayer) item);
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
	
	private void addPlayersToTeeTimePickLists(List<DynamoPlayer> playersMoved, int leaveThisListAlone) 
	{
		for (int k = 0; k < playersMoved.size(); k++)
		{
			DynamoPlayer playerMoved = playersMoved.get(k);
			
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
							
							List<DynamoPlayer> sourceList0 = this.getGameTeeTimeList1().getSource();
							
							if (this.getGameTeeTimeList1().getTarget() != null 
							&&  this.getGameTeeTimeList1().getTarget().size() < 4)
							{
								sourceList0.add(playerMoved);										
							}
							
							break; 
							
						case 1:
							
							List<DynamoPlayer> sourceList1 = this.getGameTeeTimeList2().getSource();
							if (this.getGameTeeTimeList2().getTarget() != null 
							&&  this.getGameTeeTimeList2().getTarget().size() < 4)
							{
								sourceList1.add(playerMoved);										
							}
											
							break;
							
						case 2:
							
							List<DynamoPlayer> sourceList2 = this.getGameTeeTimeList3().getSource();
							if (this.getGameTeeTimeList3().getTarget() != null 
							&&  this.getGameTeeTimeList3().getTarget().size() < 4)
							{
								sourceList2.add(playerMoved);										
							}
														
							break;	
							
						case 3:
							
							List<DynamoPlayer> sourceList3 = this.getGameTeeTimeList4().getSource();
							if (this.getGameTeeTimeList4().getTarget() != null 
							&&  this.getGameTeeTimeList4().getTarget().size() < 4)
							{
								sourceList3.add(playerMoved);										
							}
								
							break;	
							
						case 4:
			
							List<DynamoPlayer> sourceList4 = this.getGameTeeTimeList5().getSource();
							if (this.getGameTeeTimeList5().getTarget() != null 
							&&  this.getGameTeeTimeList5().getTarget().size() < 4)
							{
								sourceList4.add(playerMoved);										
							}
								
							break;
							
						case 5:
							
							List<DynamoPlayer> sourceList5 = this.getGameTeeTimeList6().getSource();
							if (this.getGameTeeTimeList6().getTarget() != null 
							&&  this.getGameTeeTimeList6().getTarget().size() < 4)
							{
								sourceList5.add(playerMoved);										
							}
							
							break;	
							
						case 6:
							
							List<DynamoPlayer> sourceList6 = this.getGameTeeTimeList7().getSource();
							if (this.getGameTeeTimeList7().getTarget() != null 
							&&  this.getGameTeeTimeList7().getTarget().size() < 4)
							{
								sourceList6.add(playerMoved);										
							}
								
							break;	
							
						case 7:
							
							List<DynamoPlayer> sourceList7 = this.getGameTeeTimeList8().getSource();
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

	private void removePlayersFromTeeTimePickLists(List<DynamoPlayer> playersMoved, int leaveThisListAlone) 
	{
		for (int k = 0; k < playersMoved.size(); k++)
		{
			DynamoPlayer playerMoved = playersMoved.get(k);
			
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
							
							List<DynamoPlayer> sourceList0 = this.getGameTeeTimeList1().getSource();
							for (int j = 0; j < sourceList0.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList0.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList0.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList0 = this.getGameTeeTimeList1().getTarget();
							for (int j = 0; j < targetList0.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList0.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList0.remove(j);
									break;
								}
							}
							
							break; 
							
						case 1:
							
							List<DynamoPlayer> sourceList1 = this.getGameTeeTimeList2().getSource();
							for (int j = 0; j < sourceList1.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList1.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList1.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList1 = this.getGameTeeTimeList2().getTarget();
							for (int j = 0; j < targetList1.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList1.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList1.remove(j);
									break;
								}
							}
							break;
							
						case 2:
							
							List<DynamoPlayer> sourceList2 = this.getGameTeeTimeList3().getSource();
							for (int j = 0; j < sourceList2.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList2.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList2.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList2 = this.getGameTeeTimeList3().getTarget();
							for (int j = 0; j < targetList2.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList2.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList2.remove(j);
									break;
								}
							}
							break;	
							
						case 3:
							
							List<DynamoPlayer> sourceList3 = this.getGameTeeTimeList4().getSource();
							for (int j = 0; j < sourceList3.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList3.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList3.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList3 = this.getGameTeeTimeList4().getTarget();
							for (int j = 0; j < targetList3.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList3.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList3.remove(j);
									break;
								}
							}
							break;	
							
						case 4:
			
							List<DynamoPlayer> sourceList4 = this.getGameTeeTimeList5().getSource();
							for (int j = 0; j < sourceList4.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList4.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList4.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList4 = this.getGameTeeTimeList5().getTarget();
							for (int j = 0; j < targetList4.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList4.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList4.remove(j);
									break;
								}
							}
							break;
							
						case 5:
							
							List<DynamoPlayer> sourceList5 = this.getGameTeeTimeList6().getSource();
							for (int j = 0; j < sourceList5.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList5.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList5.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList5 = this.getGameTeeTimeList6().getTarget();
							for (int j = 0; j < targetList5.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList5.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList5.remove(j);
									break;
								}
							}
							break;	
							
						case 6:
							
							List<DynamoPlayer> sourceList6 = this.getGameTeeTimeList7().getSource();
							for (int j = 0; j < sourceList6.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList6.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList6.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList6 = this.getGameTeeTimeList7().getTarget();
							for (int j = 0; j < targetList6.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList6.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									targetList6.remove(j);
									break;
								}
							}
							break;	
							
						case 7:
							
							List<DynamoPlayer> sourceList7 = this.getGameTeeTimeList8().getSource();
							for (int j = 0; j < sourceList7.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList7.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList7.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList7 = this.getGameTeeTimeList8().getTarget();
							for (int j = 0; j < targetList7.size(); j++) 
							{
								DynamoPlayer tempPlayer = targetList7.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
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
        if (!(o instanceof String)) 
        {
            return false;
        }
        
        final String that = (String) o;
        return Objects.equals(playerID, that);
    }
	
	public BigDecimal getHandicap() {
		return handicap;
	}
	public void setHandicap(BigDecimal handicap) {
		this.handicap = handicap;
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

	public int getTotalSelectedPlayers() 
	{
		int tempInt = 0;
		
		if (this.getSelectedPlayersList() != null && this.getSelectedPlayersList().size() > 0)
		{
			tempInt = this.getSelectedPlayersList().size();
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
	
	public String getTeePreference() 
	{
		return teePreference;
	}

	public void setTeePreference(String teePreference) 
	{
		this.teePreference = teePreference;
	}
	public boolean isActive() 
	{
		return active;
	}

	public void setActive(boolean active) 
	{
		this.active = active;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList1() {
		return gameTeeTimeList1;
	}

	public void setGameTeeTimeList1(DualListModel<DynamoPlayer> gameTeeTimeList1) {
		this.gameTeeTimeList1 = gameTeeTimeList1;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList2() {
		return gameTeeTimeList2;
	}

	public void setGameTeeTimeList2(DualListModel<DynamoPlayer> gameTeeTimeList2) {
		this.gameTeeTimeList2 = gameTeeTimeList2;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList3() {
		return gameTeeTimeList3;
	}

	public void setGameTeeTimeList3(DualListModel<DynamoPlayer> gameTeeTimeList3) {
		this.gameTeeTimeList3 = gameTeeTimeList3;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList4() {
		return gameTeeTimeList4;
	}

	public void setGameTeeTimeList4(DualListModel<DynamoPlayer> gameTeeTimeList4) {
		this.gameTeeTimeList4 = gameTeeTimeList4;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList5() {
		return gameTeeTimeList5;
	}

	public void setGameTeeTimeList5(DualListModel<DynamoPlayer> gameTeeTimeList5) {
		this.gameTeeTimeList5 = gameTeeTimeList5;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList6() {
		return gameTeeTimeList6;
	}

	public void setGameTeeTimeList6(DualListModel<DynamoPlayer> gameTeeTimeList6) {
		this.gameTeeTimeList6 = gameTeeTimeList6;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList7() {
		return gameTeeTimeList7;
	}

	public void setGameTeeTimeList7(DualListModel<DynamoPlayer> gameTeeTimeList7) {
		this.gameTeeTimeList7 = gameTeeTimeList7;
	}

	public DualListModel<DynamoPlayer> getGameTeeTimeList8() {
		return gameTeeTimeList8;
	}

	public void setGameTeeTimeList8(DualListModel<DynamoPlayer> gameTeeTimeList8) {
		this.gameTeeTimeList8 = gameTeeTimeList8;
	}

	public List<DynamoPlayer> getSelectedPlayersList() {
		return selectedPlayersList;
	}

	public void setSelectedPlayersList(List<DynamoPlayer> selectedPlayersList) {
		this.selectedPlayersList = selectedPlayersList;
	}

	public DualListModel<DynamoPlayer> getPlayersPickList() {
		return playersPickList;
	}

	public void setPlayersPickList(DualListModel<DynamoPlayer> playersPickList) {
		this.playersPickList = playersPickList;
	}

	public List<DynamoPlayer> getPlayersPickListSource() {
		return playersPickListSource;
	}

	public void setPlayersPickListSource(List<DynamoPlayer> playersPickListSource) {
		this.playersPickListSource = playersPickListSource;
	}

	public List<DynamoPlayer> getPlayersPickListTarget() {
		return playersPickListTarget;
	}

	public void setPlayersPickListTarget(List<DynamoPlayer> playersPickListTarget) {
		this.playersPickListTarget = playersPickListTarget;
	}


}
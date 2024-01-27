package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;
import com.pas.util.Utils;

@Named("pc_Round")
@SessionScoped
public class Round extends SpringBeanAutowiringSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(Round.class);

	private String roundID;
	private int oldRoundID;
	private String gameID;
	private int oldGameID;
	private String playerID;
	private int oldPlayerID;
	private int teamNumber;	
	private String teamNumberDisplay;
	private String teeTimeID;
	private int oldTeeTimeID;
	private String playerName;
	private BigDecimal roundHandicap;
	private BigDecimal playerHandicapIndex;
	private String courseTeeID;
	private int oldCourseTeeID;
	private String courseTeeColor;
	private BigDecimal roundHandicapDifferential;
	private Date signupDateTime;
	private boolean disableFixScore = true;
	private TeeTime teeTime;
	private Game game;
	private Player player;
	private List<Score> roundbyHoleScores = new ArrayList<Score>();
	
	private List<Round> syncGameRoundList = Collections.synchronizedList(new ArrayList<>());	
		
	private List<Game> availableGamesList = new ArrayList<Game>();
	private List<SelectItem> teamNumberList = new ArrayList<SelectItem>();
	
	private Round selectedRound;
	
	private Integer fixHole;
	private Integer correctedScore;
	private String roundIDForCorrectedScore;
	
	private Integer hole1Score;
	private Integer hole2Score;
	private Integer hole3Score;
	private Integer hole4Score;
	private Integer hole5Score;
	private Integer hole6Score;
	private Integer hole7Score;
	private Integer hole8Score;
	private Integer hole9Score;
	private Integer front9Total;
	private Integer hole10Score;
	private Integer hole11Score;
	private Integer hole12Score;
	private Integer hole13Score;
	private Integer hole14Score;
	private Integer hole15Score;
	private Integer hole16Score;
	private Integer hole17Score;
	private Integer hole18Score;
	private Integer back9Total;	
	private Integer totalScore;
	private String totalToPar;
	private BigDecimal netScore;	
	
	private boolean hole1ScoreEntryDisabled = false;
	private boolean hole2ScoreEntryDisabled = false;
	private boolean hole3ScoreEntryDisabled = false;
	private boolean hole4ScoreEntryDisabled = false;
	private boolean hole5ScoreEntryDisabled = false;
	private boolean hole6ScoreEntryDisabled = false;
	private boolean hole7ScoreEntryDisabled = false;
	private boolean hole8ScoreEntryDisabled = false;
	private boolean hole9ScoreEntryDisabled = false;
	private boolean hole10ScoreEntryDisabled = false;
	private boolean hole11ScoreEntryDisabled = false;
	private boolean hole12ScoreEntryDisabled = false;
	private boolean hole13ScoreEntryDisabled = false;
	private boolean hole14ScoreEntryDisabled = false;
	private boolean hole15ScoreEntryDisabled = false;
	private boolean hole16ScoreEntryDisabled = false;
	private boolean hole17ScoreEntryDisabled = false;
	private boolean hole18ScoreEntryDisabled = false;
	
	private String hole1StyleClass;
	private String hole2StyleClass;
	private String hole3StyleClass;
	private String hole4StyleClass;
	private String hole5StyleClass;
	private String hole6StyleClass;
	private String hole7StyleClass;
	private String hole8StyleClass;
	private String hole9StyleClass;
	private String hole10StyleClass;
	private String hole11StyleClass;
	private String hole12StyleClass;
	private String hole13StyleClass;
	private String hole14StyleClass;
	private String hole15StyleClass;
	private String hole16StyleClass;
	private String hole17StyleClass;
	private String hole18StyleClass;
	private String front9StyleClass;
	private String back9StyleClass;
	private String totalStyleClass;
	private String netStyleClass;
	private String totalToParClass;
	
	private boolean disableRunGameNavigate = true;
	
	private List<Round> roundsForGame = new ArrayList<Round>();	
	
	public Round() 
	{
		//log.debug("In Round constructor.  hash code is this: " + this.hashCode());
	}
	
	public String toString()
	{
		return "roundID = " + this.getRoundID() + " player: " + this.getPlayerName() + " handicap: " + this.getRoundHandicap()
					+ " team number: " + this.getTeamNumber() + " score: " + this.getTotalScore();
	}
	
	public String selectRowAjax(SelectEvent<Round> event)
	{
		log.info(getTempUserName() + " selected a row in Round selection list");
		Round rd = event.getObject();
		this.setSelectedRound(rd);
		
		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
		
		gm.setDisableDeleteSelectedPlayerRound(false);			

		this.setDisableFixScore(false);
		
		return "";
	}
	
	public void onLoadGameHandicaps() 
	{
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (game == null || game.getSelectedGame() == null)
		{
			game.setSelectedGame(golfmain.getFullGameList().get(0));
			this.setRoundsForGame(golfmain.getRoundsForGame(game.getSelectedGame()));
		}
		else if (CollectionUtils.isEmpty(this.getRoundsForGame()))
		{
			this.setRoundsForGame(golfmain.getRoundsForGame(game.getSelectedGame()));
		}				
	}

	public void onloadPickTeams() 
	{
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		if (game == null || game.getSelectedGame() == null)
		{
			game.setSelectedGame(golfmain.getFullGameList().get(0));
			this.setRoundsForGame(golfmain.getRoundsForGame(game.getSelectedGame()));
		}
		else if (CollectionUtils.isEmpty(this.getRoundsForGame()))
		{
			this.setRoundsForGame(golfmain.getRoundsForGame(game.getSelectedGame()));
		}
		
		this.getTeamNumberList().clear();
		
		//add team -1 in case this person is skins only!
		SelectItem selItem = new SelectItem();
		selItem.setLabel("Skins Only");
		selItem.setValue("-1");
		this.getTeamNumberList().add(selItem);
			
		for (int i = 1; i <= game.getSelectedGame().getTotalTeams(); i++) 
		{
			selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			this.getTeamNumberList().add(selItem);
		}				
		
	}
	
	public String resetTeams() 
	{
		log.info("User clicked reset teams from player selection screen");
		
		for (int i = 0; i < this.getRoundsForGame().size(); i++) 
		{
			Round rd = this.getRoundsForGame().get(i);
			rd.setTeamNumber(0); 
		} 
		return "";
	}	
	
	
	public String pickTeams() 
	{
		log.info("User clicked pick teams from pick teams screen");
		
		//How many players per team?  Need lists that we can snake through by handicap.
		
		List<Round> aPlayersList = new ArrayList<Round>();
		List<Round> bPlayersList = new ArrayList<Round>();
		List<Round> cPlayersList = new ArrayList<Round>();
		List<Round> dPlayersList = new ArrayList<Round>();
		List<Round> ePlayersList = new ArrayList<Round>();
		List<Round> fPlayersList = new ArrayList<Round>();
		List<Round> gPlayersList = new ArrayList<Round>();
		List<Round> hPlayersList = new ArrayList<Round>();
		
		List<Round> gamePlayersList = new ArrayList<Round>();
		
		try
		{
			for (int i = 0; i < this.getRoundsForGame().size(); i++) 
			{
				Round rd = this.getRoundsForGame().get(i);
				if (rd.getTeamNumber() < 0) //means skins only - do not include them in team pick
				{
					continue;
				}
				else
				{
					gamePlayersList.add(rd);
				}
			} 
			
			Collections.sort(gamePlayersList, new Round.RoundComparatorByHandicap());
			
			Game game = BeanUtilJSF.getBean("pc_Game");
			int playersPerTeam = game.getSelectedGame().getTotalPlayers() / game.getSelectedGame().getTotalTeams(); 
			int totalTeams = game.getSelectedGame().getTotalTeams();
			
			//A players first
			for (int j = 1; j <= totalTeams; j++) 
			{
				aPlayersList.add(gamePlayersList.get(j-1));
			}
			
			//B players
			for (int j = totalTeams+1; j <= totalTeams*2; j++) 
			{
				bPlayersList.add(gamePlayersList.get(j-1));			
			}
			
			Comparator<Round> b = Collections.reverseOrder(new Round.RoundComparatorByHandicap()); 
	        Collections.sort(bPlayersList, b);
			
			//C players
			for (int j = totalTeams*2+1; j <= totalTeams*3; j++) 
			{
				cPlayersList.add(gamePlayersList.get(j-1));			
			}		
			
			//D players
			if (playersPerTeam >= 4)
			{
				for (int j = totalTeams*3+1; j <= totalTeams*4; j++) 
				{
					dPlayersList.add(gamePlayersList.get(j-1));				
				}
				Comparator<Round> d = Collections.reverseOrder(new Round.RoundComparatorByHandicap()); 
		        Collections.sort(dPlayersList, d);
				
			}
			
			//E players
			if (playersPerTeam >= 5)
			{
				for (int j = totalTeams*4+1; j <= totalTeams*5; j++) 
				{
					ePlayersList.add(gamePlayersList.get(j-1));				
				}	
			}
			
			//F players, reverse the order
			if (playersPerTeam >= 6)
			{
				for (int j = totalTeams*5+1; j <= totalTeams*6; j++) 
				{
					fPlayersList.add(gamePlayersList.get(j-1));
				}
				Comparator<Round> f = Collections.reverseOrder(new Round.RoundComparatorByHandicap()); 
		        Collections.sort(fPlayersList, f);
			}
			
			//G players
			if (playersPerTeam >= 7)
			{
				for (int j = totalTeams*6+1; j <= totalTeams*7; j++) 
				{
					gPlayersList.add(gamePlayersList.get(j-1));
				}	
			}
			
			//H players
			if (playersPerTeam >= 8)
			{
				for (int j = totalTeams*7+1; j <= totalTeams*8; j++) 
				{
					hPlayersList.add(gamePlayersList.get(j-1));				
				}
				Comparator<Round> h = Collections.reverseOrder(new Round.RoundComparatorByHandicap()); 
		        Collections.sort(hPlayersList, h);
			}
			
			for (int i = 0; i <= totalTeams-1; i++) 
			{
				Round aPlayer = aPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGame().size(); j++) 
				{
					Round rd = this.getRoundsForGame().get(j);
					Player tempPlayer = rd.getPlayer();
					if (aPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				
				Round bPlayer = bPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGame().size(); j++) 
				{
					Round rd = this.getRoundsForGame().get(j);
					Player tempPlayer = rd.getPlayer();
					if (bPlayer.getPlayerID() == tempPlayer.getPlayerID())
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				Round cPlayer = cPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGame().size(); j++) 
				{
					Round rd = this.getRoundsForGame().get(j);
					Player tempPlayer = rd.getPlayer();
					if (cPlayer.getPlayerID() == tempPlayer.getPlayerID())
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				
				if (playersPerTeam >= 4)
				{
					Round dPlayer = dPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGame().size(); j++) 
					{
						Round rd = this.getRoundsForGame().get(j);
						Player tempPlayer = rd.getPlayer();
						if (dPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 5)
				{
					Round ePlayer = ePlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGame().size(); j++) 
					{
						Round rd = this.getRoundsForGame().get(j);
						Player tempPlayer = rd.getPlayer();
						if (ePlayer.getPlayerID() == tempPlayer.getPlayerID())
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 6)
				{
					Round fPlayer = fPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGame().size(); j++) 
					{
						Round rd = this.getRoundsForGame().get(j);
						Player tempPlayer = rd.getPlayer();
						if (fPlayer.getPlayerID() == tempPlayer.getPlayerID())
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 7)
				{
					Round gPlayer = gPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGame().size(); j++) 
					{
						Round rd = this.getRoundsForGame().get(j);
						Player tempPlayer = rd.getPlayer();
						if (gPlayer.getPlayerID() == tempPlayer.getPlayerID())
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 8)
				{
					Round hPlayer = hPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGame().size(); j++) 
					{
						Round rd = this.getRoundsForGame().get(j);
						Player tempPlayer = rd.getPlayer();
						if (hPlayer.getPlayerID() == tempPlayer.getPlayerID())
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
			}
			
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Pick Teams successful", "Successfully picked teams"));		
		} 
		catch (Exception e) 
		{
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Pick Teams error encountered", e.getMessage()));		
		}		
		
		return "";
	}
	
	public void valueChangeGamePickTeams(AjaxBehaviorEvent event) 
	{
		log.info("User picked a game on select players for game form");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			this.setRoundsForGame(golfmain.getRoundsForGame(selectedOption));
		}
		
		this.getTeamNumberList().clear();
		
		//add team -1 in case this person is skins only!
		SelectItem selItem = new SelectItem();
		selItem.setLabel("Skins Only");
		selItem.setValue("-1");
		this.getTeamNumberList().add(selItem);
			
		for (int i = 1; i <= selectedOption.getTotalTeams(); i++) 
		{
			selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			this.getTeamNumberList().add(selItem);
		}
	}
	

	public String proceedToPreGameEmail()
	{
		log.info("User is done with picking teams for game, proceed to pregame email");
		
		saveAndStayPickTeams();
		
		Game game = BeanUtilJSF.getBean("pc_Game");
		
		if (game != null && game.getSelectedGame() != null)
		{
			game.onLoadPreGameEmail();
		}
		
		return "success";
	}
	
	public String saveAndStayPickTeams()
	{
		Game game = BeanUtilJSF.getBean("pc_Game");
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		for (int i = 0; i < this.getRoundsForGame().size(); i++) 
		{
			Round rd = this.getRoundsForGame().get(i);
			golfmain.updateRoundTeamNumber(game.getSelectedGame(), rd.getPlayerID(), rd.getTeamNumber());			
		}
		
		FacesContext context = FacesContext.getCurrentInstance();
	    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Teams Saved", "Teams saved"));
	 
		return "";
	}
	public String proceedToPickTeams()
	{
		log.info("Game Handicap entry done, proceed to pick teams");
		updateGameHandicaps();
		return "success";
	}
	
	public String updateGameHandicaps()
	{
		try
		{
			Game game = BeanUtilJSF.getBean("pc_Game");		
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
			
			for (int i = 0; i < this.getRoundsForGame().size(); i++) 
			{
				Round rd = this.getRoundsForGame().get(i);
				Player player = rd.getPlayer();
				golfmain.updatePlayer(player);
				
				CourseTee ct = golfmain.getCourseTeesMap().get(rd.getCourseTeeID());			
				BigDecimal newRoundHandicap = Utils.getCourseHandicap(ct, rd.getPlayer().getHandicap());
				
				golfmain.updateRoundHandicap(game.getSelectedGame(), player.getPlayerID(), newRoundHandicap);			
			}
			
			this.setRoundsForGame(golfmain.getRoundsForGame(game.getSelectedGame()));
			
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Game Handicaps Saved", "Game handicaps saved"));
		}
		catch (Exception e) 
		{
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}		
	 
		return "";
	}
	
	public String clearFixedScore()
	{
		//updateAllRounds();
		correctedScore = null;
		fixHole = null;
		roundIDForCorrectedScore = null;
		return "";
	}
	
	public String saveFixedScore()
	{
		switch (fixHole) 
		{
			case 1:
				this.getSelectedRound().setHole1Score(correctedScore);
				break;
				
			case 2:
				this.getSelectedRound().setHole2Score(correctedScore);
				break;
				
			case 3:
				this.getSelectedRound().setHole3Score(correctedScore);
				break;	
				
			case 4:
				this.getSelectedRound().setHole4Score(correctedScore);
				break;
				
			case 5:
				this.getSelectedRound().setHole5Score(correctedScore);
				break;
				
			case 6:
				this.getSelectedRound().setHole6Score(correctedScore);
				break;	
				
			case 7:
				this.getSelectedRound().setHole7Score(correctedScore);
				break;
				
			case 8:
				this.getSelectedRound().setHole8Score(correctedScore);
				break;
				
			case 9:
				this.getSelectedRound().setHole9Score(correctedScore);
				break;	
				
			case 10:
				this.getSelectedRound().setHole10Score(correctedScore);
				break;
				
			case 11:
				this.getSelectedRound().setHole11Score(correctedScore);
				break;
				
			case 12:
				this.getSelectedRound().setHole12Score(correctedScore);
				break;
				
			case 13:
				this.getSelectedRound().setHole13Score(correctedScore);
				break;	
				
			case 14:
				this.getSelectedRound().setHole14Score(correctedScore);
				break;
				
			case 15:
				this.getSelectedRound().setHole15Score(correctedScore);
				break;
				
			case 16:
				this.getSelectedRound().setHole16Score(correctedScore);
				break;	
				
			case 17:
				this.getSelectedRound().setHole17Score(correctedScore);
				break;
				
			case 18:
				this.getSelectedRound().setHole18Score(correctedScore);
				break;
	
			default:
				break;
		}
		
		this.getSelectedRound().setFront9Total(Utils.front9Score(this.getSelectedRound()));
		this.getSelectedRound().setBack9Total(Utils.back9Score(this.getSelectedRound()));
		this.getSelectedRound().setTotalScore(Utils.totalScore(this.getSelectedRound()));	
		
		return "";
	}
	
	public String onLoadGameEnterScores()
	{
		log.info(getTempUserName() + " in onLoadGameEnterScores");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(getTempUserName());
	
		boolean adminUser = Utils.isAdminUser();
		
		if (adminUser)
		{
			availableGamesList = golfmain.getFullGameList();
		}
		else //normal user; only load games they are a part of, that are in the future.
		{
			availableGamesList = golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID());
		}				
		
		//let's take the first one
		if (availableGamesList != null && availableGamesList.size() > 0)
		{
			this.setGame(availableGamesList.get(0));			
			this.setSyncGameRoundList(golfmain.getRoundsForGame(this.getGame()));
			setUpGameEnterScores(tempPlayer);		
		}
		else //no games available to enter scores for
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"No games available to enter scores for",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);	     
		}		
	
		return "";
	}
	
	private void setUpGameEnterScores(Player tempPlayer) 
	{
		/*
		boolean adminUser = Utils.isAdminUser();		
		
		if (!adminUser)
		{
			//remove anything from the list where it's not the same play group as the logged in user
			
			int tempTeeTimeID = 0;
			
			//first let's find out what play group this player is in (might not be in any; in that case empty out the list!)
			synchronized (this.getSyncGameRoundList())
			{
				for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 			
				{
					Round rd = this.getSyncGameRoundList().get(i);
					if (rd.getPlayerID() == tempPlayer.getPlayerID())
					{
						tempTeeTimeID = rd.getTeeTimeID();
						break;
					}
				}
			}
			
			final int temp2TeeTimeID = tempTeeTimeID;
			
			if (tempTeeTimeID == 0)
			{
				this.getSyncGameRoundList().clear(); //not a part of this game
			}
			else
			{
				this.getSyncGameRoundList().removeIf(rd -> (rd.getTeeTimeID() != temp2TeeTimeID));				
			}
			
			synchronized (this.getSyncGameRoundList())
			{
				for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 
				{
					Round tempRd = this.getSyncGameRoundList().get(i);
					log.info(getTempUserName() + " showing user round for entering scores: roundID = " + tempRd.getRoundID() + " player: " + tempRd.getPlayerName());
				}
			}
		}	
		*/
	}

	public void valueChangeGameHandicaps(AjaxBehaviorEvent event) 
	{
		log.info("User picked a game on game handicaps form");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
	
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			this.setRoundsForGame(golfmain.getRoundsForGame(selectedOption));
		}
						
	}
	
	public void valueChangeGame(AjaxBehaviorEvent event) 
	{
		log.info(getTempUserName() + " picked a game on game enter scores");
		
		SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
		Game selectedOption = (Game)selectonemenu.getValue();
		
		if (selectedOption != null)
		{
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
			
			Player tempPlayer = golfmain.getFullPlayersMapByUserName().get(getTempUserName());
		
			this.getSyncGameRoundList().clear();
			this.setSyncGameRoundList(golfmain.getRoundsForGame(selectedOption));		
			setUpGameEnterScores(tempPlayer);			
		}
						
	}
	
	public String runGameNavigate()
	{
		boolean allScoresEntered = validateScores();
		
		if (allScoresEntered)
		{
			return "success";
		}
		else
		{
			return "";
		}
	}
	
	private boolean validateScores() 
	{
		boolean allScoresValid = true; //assume true till proven otherwise
		
		synchronized (this.getSyncGameRoundList())
		{
			for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 
			{
				Round tempRound = this.getSyncGameRoundList().get(i);
				
				if (tempRound.getHole1Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole2Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole3Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole4Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole5Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole6Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole7Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole8Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole9Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole10Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole11Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole12Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole13Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole14Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole15Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole16Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole17Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				if (tempRound.getHole18Score() == 0)
				{
					allScoresValid = false;
					break;
				}
				
				if (!allScoresValid)
				{
					FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"there are round(s) with unentered scores still",null);
					FacesContext.getCurrentInstance().addMessage(null, msg);				
				}	     	
				
			}	
		}
		
		return allScoresValid;
	}

	public synchronized String updateAllRounds()
	{
		log.info(getTempUserName() + " entering updateAllRounds method");
		
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		int updatedRounds = 0;
		
		this.setDisableRunGameNavigate(false);
		
		synchronized (this.getSyncGameRoundList())
		{
			for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 
			{
				Round tempRound = this.getSyncGameRoundList().get(i);
				
				if (tempRound.getHole1Score() == null)
				{
					continue;
				}
				
				log.info(getTempUserName() + " in updateAllRounds method roundID = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName());
				
				int frontScore = tempRound.getHole1Score() + tempRound.getHole2Score() + tempRound.getHole3Score();
				frontScore = frontScore + tempRound.getHole4Score() + tempRound.getHole5Score() + tempRound.getHole6Score();
				frontScore = frontScore + tempRound.getHole7Score() + tempRound.getHole8Score() + tempRound.getHole9Score();
				
				tempRound.setFront9Total(frontScore);
				
				int backScore = tempRound.getHole10Score() + tempRound.getHole11Score() + tempRound.getHole12Score();
				backScore = backScore + tempRound.getHole13Score() + tempRound.getHole14Score() + tempRound.getHole15Score();
				backScore = backScore + tempRound.getHole16Score() + tempRound.getHole17Score() + tempRound.getHole18Score();
				
				tempRound.setBack9Total(backScore);
				
				tempRound.setTotalScore(frontScore + backScore); 
				
				if (tempRound.getTotalScore() == 0)
				{
					this.setDisableRunGameNavigate(true);
				}
				
				tempRound.setNetScore((new BigDecimal(tempRound.getTotalScore())).subtract(tempRound.getRoundHandicap()));
				
				Game game = golfmain.getGameByGameID(tempRound.getGameID());
				Course course = golfmain.getCoursesMap().get(game.getCourseID());
				int coursePar = course.getCoursePar();
				int scoreToPar = tempRound.getTotalScore() - coursePar;
				String scoreToParString = "";
				if (scoreToPar < 0)
				{
					scoreToParString = String.valueOf(scoreToPar);
				}
				else if (scoreToPar == 0)
				{
					scoreToParString = "E";
				}
				else
				{
					scoreToParString = "+" + String.valueOf(scoreToPar);
				}
				
				tempRound.setTotalToPar(scoreToParString);
				
				log.info(getTempUserName() + " about to update round = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName() + " score = " + tempRound.getTotalScore());
				
				golfmain.updateRound(tempRound);
				
				log.info(getTempUserName() + " completed updating round = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName() + " score = " + tempRound.getTotalScore());
				
				updatedRounds++;
			}
		}
		
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,updatedRounds + " Rounds updated",null);
        FacesContext.getCurrentInstance().addMessage(null, msg);
        
		return "";
	}
	
	public String deleteSelectedPlayerRound()
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		
		Round rd = this.getSelectedRound();
		golfmain.deleteRoundFromDB(rd.getRoundID());
		
		int indexToRemove = -1;
		
		synchronized (this.getSyncGameRoundList())
		{
			for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 
			{
				Round tempRound = this.getSyncGameRoundList().get(i);
				if (tempRound.getRoundID() == rd.getRoundID())
				{
					indexToRemove = i;
					break;
				}
			}
		}
		
		this.getSyncGameRoundList().remove(indexToRemove);
		
		return "";
	}
	
	//changed to use javascript in golfJS.js instead - once went to auto-tabbing ajax event was not reliably fired
	public void tallyScore(Round tempRound, int rowIndex)
	{
		int frontScore = tempRound.getHole1Score() + tempRound.getHole2Score() + tempRound.getHole3Score();
		frontScore = frontScore + tempRound.getHole4Score() + tempRound.getHole5Score() + tempRound.getHole6Score();
		frontScore = frontScore + tempRound.getHole7Score() + tempRound.getHole8Score() + tempRound.getHole9Score();
		
		tempRound.setFront9Total(frontScore);
		PrimeFaces.current().ajax().update("scoresForm:roundsTableID:"+rowIndex+":front9ID"); 
		
		int backScore = tempRound.getHole10Score() + tempRound.getHole11Score() + tempRound.getHole12Score();
		backScore = backScore + tempRound.getHole13Score() + tempRound.getHole14Score() + tempRound.getHole15Score();
		backScore = backScore + tempRound.getHole16Score() + tempRound.getHole17Score() + tempRound.getHole18Score();
		
		tempRound.setBack9Total(backScore);
		PrimeFaces.current().ajax().update("scoresForm:roundsTableID:"+rowIndex+":back9ID"); 		
		
		tempRound.setTotalScore(frontScore + backScore); 
		PrimeFaces.current().ajax().update("scoresForm:roundsTableID:"+rowIndex+":totalScoreID"); 		
	}
	
	public static class RoundComparatorByHandicap implements Comparator<Round> 
	{
		public int compare(Round round1, Round round2)
		{
			return round1.getRoundHandicap().compareTo(round2.getRoundHandicap());
		}		
	}	
	
	public List<Score> getRoundbyHoleScores() {
		return roundbyHoleScores;
	}

	public void setRoundbyHoleScores(List<Score> roundbyHoleScores) {
		this.roundbyHoleScores = roundbyHoleScores;
	}

	public Player getPlayer() {
		return player;
	}

	public void setPlayer(Player player) {
		this.player = player;
	}

	

	public String getHole1StyleClass() {
		return hole1StyleClass;
	}

	public void setHole1StyleClass(String hole1StyleClass) {
		this.hole1StyleClass = hole1StyleClass;
	}

	public String getHole2StyleClass() {
		return hole2StyleClass;
	}

	public void setHole2StyleClass(String hole2StyleClass) {
		this.hole2StyleClass = hole2StyleClass;
	}

	public String getHole3StyleClass() {
		return hole3StyleClass;
	}

	public void setHole3StyleClass(String hole3StyleClass) {
		this.hole3StyleClass = hole3StyleClass;
	}

	public String getHole4StyleClass() {
		return hole4StyleClass;
	}

	public void setHole4StyleClass(String hole4StyleClass) {
		this.hole4StyleClass = hole4StyleClass;
	}

	public String getHole5StyleClass() {
		return hole5StyleClass;
	}

	public void setHole5StyleClass(String hole5StyleClass) {
		this.hole5StyleClass = hole5StyleClass;
	}

	public String getHole6StyleClass() {
		return hole6StyleClass;
	}

	public void setHole6StyleClass(String hole6StyleClass) {
		this.hole6StyleClass = hole6StyleClass;
	}

	public String getHole7StyleClass() {
		return hole7StyleClass;
	}

	public void setHole7StyleClass(String hole7StyleClass) {
		this.hole7StyleClass = hole7StyleClass;
	}

	public String getHole8StyleClass() {
		return hole8StyleClass;
	}

	public void setHole8StyleClass(String hole8StyleClass) {
		this.hole8StyleClass = hole8StyleClass;
	}

	public String getHole9StyleClass() {
		return hole9StyleClass;
	}

	public void setHole9StyleClass(String hole9StyleClass) {
		this.hole9StyleClass = hole9StyleClass;
	}

	public String getHole10StyleClass() {
		return hole10StyleClass;
	}

	public void setHole10StyleClass(String hole10StyleClass) {
		this.hole10StyleClass = hole10StyleClass;
	}

	public String getHole11StyleClass() {
		return hole11StyleClass;
	}

	public void setHole11StyleClass(String hole11StyleClass) {
		this.hole11StyleClass = hole11StyleClass;
	}

	public String getHole12StyleClass() {
		return hole12StyleClass;
	}

	public void setHole12StyleClass(String hole12StyleClass) {
		this.hole12StyleClass = hole12StyleClass;
	}

	public String getHole13StyleClass() {
		return hole13StyleClass;
	}

	public void setHole13StyleClass(String hole13StyleClass) {
		this.hole13StyleClass = hole13StyleClass;
	}

	public String getHole14StyleClass() {
		return hole14StyleClass;
	}

	public void setHole14StyleClass(String hole14StyleClass) {
		this.hole14StyleClass = hole14StyleClass;
	}

	public String getHole15StyleClass() {
		return hole15StyleClass;
	}

	public void setHole15StyleClass(String hole15StyleClass) {
		this.hole15StyleClass = hole15StyleClass;
	}

	public String getHole16StyleClass() {
		return hole16StyleClass;
	}

	public void setHole16StyleClass(String hole16StyleClass) {
		this.hole16StyleClass = hole16StyleClass;
	}

	public String getHole17StyleClass() {
		return hole17StyleClass;
	}

	public void setHole17StyleClass(String hole17StyleClass) {
		this.hole17StyleClass = hole17StyleClass;
	}

	public String getHole18StyleClass() {
		return hole18StyleClass;
	}

	public void setHole18StyleClass(String hole18StyleClass) {
		this.hole18StyleClass = hole18StyleClass;
	}

	public String getFront9StyleClass() {
		return front9StyleClass;
	}

	public void setFront9StyleClass(String front9StyleClass) {
		this.front9StyleClass = front9StyleClass;
	}

	public String getBack9StyleClass() {
		return back9StyleClass;
	}

	public void setBack9StyleClass(String back9StyleClass) {
		this.back9StyleClass = back9StyleClass;
	}

	public String getTotalStyleClass() {
		return totalStyleClass;
	}

	public void setTotalStyleClass(String totalStyleClass) {
		this.totalStyleClass = totalStyleClass;
	}

	public String getNetStyleClass() {
		return netStyleClass;
	}

	public void setNetStyleClass(String netStyleClass) {
		this.netStyleClass = netStyleClass;
	}

	public String getTotalToPar() {
		return totalToPar;
	}

	public void setTotalToPar(String totalToPar) {
		this.totalToPar = totalToPar;
	}

	public String getTotalToParClass() {
		return totalToParClass;
	}

	public void setTotalToParClass(String totalToParClass) {
		this.totalToParClass = totalToParClass;
	}

	public Game getGame() {
		return game;
	}

	public void setGame(Game game) {
		this.game = game;
	}

	public BigDecimal getNetScore() {
		return netScore;
	}

	public void setNetScore(BigDecimal netScore) {
		this.netScore = netScore;
	}

	public static Logger getLog() {
		return log;
	}

	public static void setLog(Logger log) {
		Round.log = log;
	}

	public Integer getHole1Score() {
		return hole1Score;
	}

	public void setHole1Score(Integer hole1Score) {
		this.hole1Score = hole1Score;
	}

	public Integer getHole2Score() {
		return hole2Score;
	}

	public void setHole2Score(Integer hole2Score) {
		this.hole2Score = hole2Score;
	}

	public Integer getHole3Score() {
		return hole3Score;
	}

	public void setHole3Score(Integer hole3Score) {
		this.hole3Score = hole3Score;
	}

	public Integer getHole4Score() {
		return hole4Score;
	}

	public void setHole4Score(Integer hole4Score) {
		this.hole4Score = hole4Score;
	}

	public Integer getHole5Score() {
		return hole5Score;
	}

	public void setHole5Score(Integer hole5Score) {
		this.hole5Score = hole5Score;
	}

	public Integer getHole6Score() {
		return hole6Score;
	}

	public void setHole6Score(Integer hole6Score) {
		this.hole6Score = hole6Score;
	}

	public Integer getHole7Score() {
		return hole7Score;
	}

	public void setHole7Score(Integer hole7Score) {
		this.hole7Score = hole7Score;
	}

	public Integer getHole8Score() {
		return hole8Score;
	}

	public void setHole8Score(Integer hole8Score) {
		this.hole8Score = hole8Score;
	}

	public Integer getHole9Score() {
		return hole9Score;
	}

	public void setHole9Score(Integer hole9Score) {
		this.hole9Score = hole9Score;
	}

	public Integer getFront9Total() {
		return front9Total;
	}

	public void setFront9Total(Integer front9Total) {
		this.front9Total = front9Total;
	}

	public Integer getHole10Score() {
		return hole10Score;
	}

	public void setHole10Score(Integer hole10Score) {
		this.hole10Score = hole10Score;
	}

	public Integer getHole11Score() {
		return hole11Score;
	}

	public void setHole11Score(Integer hole11Score) {
		this.hole11Score = hole11Score;
	}

	public Integer getHole12Score() {
		return hole12Score;
	}

	public void setHole12Score(Integer hole12Score) {
		this.hole12Score = hole12Score;
	}

	public Integer getHole13Score() {
		return hole13Score;
	}

	public void setHole13Score(Integer hole13Score) {
		this.hole13Score = hole13Score;
	}

	public Integer getHole14Score() {
		return hole14Score;
	}

	public void setHole14Score(Integer hole14Score) {
		this.hole14Score = hole14Score;
	}

	public Integer getHole15Score() {
		return hole15Score;
	}

	public void setHole15Score(Integer hole15Score) {
		this.hole15Score = hole15Score;
	}

	public Integer getHole16Score() {
		return hole16Score;
	}

	public void setHole16Score(Integer hole16Score) {
		this.hole16Score = hole16Score;
	}

	public Integer getHole17Score() {
		return hole17Score;
	}

	public void setHole17Score(Integer hole17Score) {
		this.hole17Score = hole17Score;
	}

	public Integer getHole18Score() {
		return hole18Score;
	}

	public void setHole18Score(Integer hole18Score) {
		this.hole18Score = hole18Score;
	}

	public Integer getBack9Total() {
		return back9Total;
	}

	public void setBack9Total(Integer back9Total) {
		this.back9Total = back9Total;
	}

	public Integer getTotalScore() {
		return totalScore;
	}

	public void setTotalScore(Integer totalScore) {
		this.totalScore = totalScore;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public Round getSelectedRound() {
		return selectedRound;
	}

	public void setSelectedRound(Round selectedRound) {
		this.selectedRound = selectedRound;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public void setTeamNumber(int teamNumber) 
	{
		this.teamNumber = teamNumber;
		if (teamNumber <= 0)
		{
			this.setTeamNumberDisplay("Skins Only");
		}
		else
		{
			this.setTeamNumberDisplay(String.valueOf(teamNumber));
		}
	}

	public BigDecimal getRoundHandicap() {
		return roundHandicap;
	}

	public void setRoundHandicap(BigDecimal roundHandicap) {
		this.roundHandicap = roundHandicap;
	}

	public TeeTime getTeeTime() {
		return teeTime;
	}

	public void setTeeTime(TeeTime teeTime) {
		this.teeTime = teeTime;
	}

	public String getTeamNumberDisplay() {
		return teamNumberDisplay;
	}

	public void setTeamNumberDisplay(String teamNumberDisplay) {
		this.teamNumberDisplay = teamNumberDisplay;
	}

	public boolean isDisableRunGameNavigate() {
		return disableRunGameNavigate;
	}

	public void setDisableRunGameNavigate(boolean disableRunGameNavigate) {
		this.disableRunGameNavigate = disableRunGameNavigate;
	}

	public List<Game> getAvailableGamesList() {
		return availableGamesList;
	}

	public void setAvailableGamesList(List<Game> availableGamesList) {
		this.availableGamesList = availableGamesList;
	}

	public Integer getFixHole() {
		return fixHole;
	}

	public void setFixHole(Integer fixHole) {
		this.fixHole = fixHole;
	}

	public Integer getCorrectedScore() {
		return correctedScore;
	}

	public void setCorrectedScore(Integer correctedScore) {
		this.correctedScore = correctedScore;
	}

	public String getRoundIDForCorrectedScore() {
		return roundIDForCorrectedScore;
	}

	public void setRoundIDForCorrectedScore(String roundIDForCorrectedScore) {
		this.roundIDForCorrectedScore = roundIDForCorrectedScore;
	}

	public boolean isDisableFixScore() {
		return disableFixScore;
	}

	public void setDisableFixScore(boolean disableFixScore) {
		this.disableFixScore = disableFixScore;
	}
	
	public List<Round> getSyncGameRoundList() {
		return syncGameRoundList;
	}

	public void setSyncGameRoundList(List<Round> syncGameRoundList) {
		this.syncGameRoundList = syncGameRoundList;
	}
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}

	public boolean isHole1ScoreEntryDisabled() {
		return hole1ScoreEntryDisabled;
	}

	public void setHole1ScoreEntryDisabled(boolean hole1ScoreEntryDisabled) {
		this.hole1ScoreEntryDisabled = hole1ScoreEntryDisabled;
	}

	public boolean isHole2ScoreEntryDisabled() {
		return hole2ScoreEntryDisabled;
	}

	public void setHole2ScoreEntryDisabled(boolean hole2ScoreEntryDisabled) {
		this.hole2ScoreEntryDisabled = hole2ScoreEntryDisabled;
	}

	public boolean isHole3ScoreEntryDisabled() {
		return hole3ScoreEntryDisabled;
	}

	public void setHole3ScoreEntryDisabled(boolean hole3ScoreEntryDisabled) {
		this.hole3ScoreEntryDisabled = hole3ScoreEntryDisabled;
	}

	public boolean isHole4ScoreEntryDisabled() {
		return hole4ScoreEntryDisabled;
	}

	public void setHole4ScoreEntryDisabled(boolean hole4ScoreEntryDisabled) {
		this.hole4ScoreEntryDisabled = hole4ScoreEntryDisabled;
	}

	public boolean isHole5ScoreEntryDisabled() {
		return hole5ScoreEntryDisabled;
	}

	public void setHole5ScoreEntryDisabled(boolean hole5ScoreEntryDisabled) {
		this.hole5ScoreEntryDisabled = hole5ScoreEntryDisabled;
	}

	public boolean isHole6ScoreEntryDisabled() {
		return hole6ScoreEntryDisabled;
	}

	public void setHole6ScoreEntryDisabled(boolean hole6ScoreEntryDisabled) {
		this.hole6ScoreEntryDisabled = hole6ScoreEntryDisabled;
	}

	public boolean isHole7ScoreEntryDisabled() {
		return hole7ScoreEntryDisabled;
	}

	public void setHole7ScoreEntryDisabled(boolean hole7ScoreEntryDisabled) {
		this.hole7ScoreEntryDisabled = hole7ScoreEntryDisabled;
	}

	public boolean isHole8ScoreEntryDisabled() {
		return hole8ScoreEntryDisabled;
	}

	public void setHole8ScoreEntryDisabled(boolean hole8ScoreEntryDisabled) {
		this.hole8ScoreEntryDisabled = hole8ScoreEntryDisabled;
	}

	public boolean isHole9ScoreEntryDisabled() {
		return hole9ScoreEntryDisabled;
	}

	public void setHole9ScoreEntryDisabled(boolean hole9ScoreEntryDisabled) {
		this.hole9ScoreEntryDisabled = hole9ScoreEntryDisabled;
	}

	public boolean isHole10ScoreEntryDisabled() {
		return hole10ScoreEntryDisabled;
	}

	public void setHole10ScoreEntryDisabled(boolean hole10ScoreEntryDisabled) {
		this.hole10ScoreEntryDisabled = hole10ScoreEntryDisabled;
	}

	public boolean isHole11ScoreEntryDisabled() {
		return hole11ScoreEntryDisabled;
	}

	public void setHole11ScoreEntryDisabled(boolean hole11ScoreEntryDisabled) {
		this.hole11ScoreEntryDisabled = hole11ScoreEntryDisabled;
	}

	public boolean isHole12ScoreEntryDisabled() {
		return hole12ScoreEntryDisabled;
	}

	public void setHole12ScoreEntryDisabled(boolean hole12ScoreEntryDisabled) {
		this.hole12ScoreEntryDisabled = hole12ScoreEntryDisabled;
	}

	public boolean isHole13ScoreEntryDisabled() {
		return hole13ScoreEntryDisabled;
	}

	public void setHole13ScoreEntryDisabled(boolean hole13ScoreEntryDisabled) {
		this.hole13ScoreEntryDisabled = hole13ScoreEntryDisabled;
	}

	public boolean isHole14ScoreEntryDisabled() {
		return hole14ScoreEntryDisabled;
	}

	public void setHole14ScoreEntryDisabled(boolean hole14ScoreEntryDisabled) {
		this.hole14ScoreEntryDisabled = hole14ScoreEntryDisabled;
	}

	public boolean isHole15ScoreEntryDisabled() {
		return hole15ScoreEntryDisabled;
	}

	public void setHole15ScoreEntryDisabled(boolean hole15ScoreEntryDisabled) {
		this.hole15ScoreEntryDisabled = hole15ScoreEntryDisabled;
	}

	public boolean isHole16ScoreEntryDisabled() {
		return hole16ScoreEntryDisabled;
	}

	public void setHole16ScoreEntryDisabled(boolean hole16ScoreEntryDisabled) {
		this.hole16ScoreEntryDisabled = hole16ScoreEntryDisabled;
	}

	public boolean isHole17ScoreEntryDisabled() {
		return hole17ScoreEntryDisabled;
	}

	public void setHole17ScoreEntryDisabled(boolean hole17ScoreEntryDisabled) {
		this.hole17ScoreEntryDisabled = hole17ScoreEntryDisabled;
	}

	public boolean isHole18ScoreEntryDisabled() {
		return hole18ScoreEntryDisabled;
	}

	public void setHole18ScoreEntryDisabled(boolean hole18ScoreEntryDisabled) {
		this.hole18ScoreEntryDisabled = hole18ScoreEntryDisabled;
	}

	public Date getSignupDateTime() {
		return signupDateTime;
	}

	public void setSignupDateTime(Date signupDateTime) {
		this.signupDateTime = signupDateTime;
	}

	public BigDecimal getPlayerHandicapIndex() {
		return playerHandicapIndex;
	}

	public void setPlayerHandicapIndex(BigDecimal playerHandicapIndex) {
		this.playerHandicapIndex = playerHandicapIndex;
	}

	public String getCourseTeeColor() {
		return courseTeeColor;
	}

	public void setCourseTeeColor(String courseTeeColor) {
		this.courseTeeColor = courseTeeColor;
	}

	public BigDecimal getRoundHandicapDifferential() {
		return roundHandicapDifferential;
	}

	public void setRoundHandicapDifferential(BigDecimal roundHandicapDifferential) {
		this.roundHandicapDifferential = roundHandicapDifferential;
	}

	public List<Round> getRoundsForGame() {
		return roundsForGame;
	}

	public void setRoundsForGame(List<Round> roundsForGame) {
		this.roundsForGame = roundsForGame;
	}

	public List<SelectItem> getTeamNumberList() {
		return teamNumberList;
	}

	public void setTeamNumberList(List<SelectItem> teamNumberList) {
		this.teamNumberList = teamNumberList;
	}

	public String getRoundID() {
		return roundID;
	}

	public void setRoundID(String roundID) {
		this.roundID = roundID;
	}

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	public void setTeeTimeID(String teeTimeID) {
		this.teeTimeID = teeTimeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public String getTeeTimeID() {
		return teeTimeID;
	}

	public String getCourseTeeID() {
		return courseTeeID;
	}

	public int getOldRoundID() {
		return oldRoundID;
	}

	public void setOldRoundID(int oldRoundID) {
		this.oldRoundID = oldRoundID;
	}

	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}

	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}
}

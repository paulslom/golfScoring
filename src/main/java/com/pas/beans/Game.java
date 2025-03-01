package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
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
import java.util.ResourceBundle;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.component.selectonemenu.SelectOneMenu;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.util.ComponentUtils;

import com.pas.beans.Player.PlayerComparatorByLastName;
import com.pas.dynamodb.DateToStringConverter;
import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.util.SAMailUtility;
import com.pas.util.Utils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIColumn;
import jakarta.faces.component.UIComponent;
import jakarta.faces.component.ValueHolder;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.event.AjaxBehaviorEvent;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pc_Game")
@SessionScoped
public class Game implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(Game.class);	
	
	private static ResourceBundle genericProps = ResourceBundle.getBundle("ApplicationProperties");
	
	private static String NEWLINE = "<br/>";	
	
	private boolean courseSelected = false;
	private boolean disableShowScores = true;
	private boolean showPlayerScores = false;
	private boolean showPlayerSelectionPanel = false;
	private boolean showPregameEmail = false;
	private boolean showPostgameEmail = false;
	
	private static boolean meetInGrillRoomAfterRound = true;
	
	private boolean gameClosedForSignups = false;
		
	private String futureGameEmailMessage;
	private String preGameEmailMessage;
	private String testEmailMessage;
	private String postGameEmailMessage;
	private boolean disableEmailStuff = true;
	private ArrayList<String> emailRecipients = new ArrayList<String>();
	
	private String whoIsSignedUpMessage = "";
	private ArrayList<String> playersSignedUpList = new ArrayList<String>();
		
	private DynamoGame selectedGame;
		
	private boolean renderInputFields = true;
	private boolean renderInquiry = true;
	private boolean renderAddUpdateDelete = false;
	
	private BigDecimal totalWon; //used for subtotal of individual winnings
	
	private List<Player> playersList = new ArrayList<Player>();
	private List<Round> playerScores = new ArrayList<Round>();
	private List<SkinWinnings> skinWinningsList = new ArrayList<SkinWinnings>();
	private List<Round> teamResultsList = new ArrayList<Round>();
	private List<String> teamSummaryList = new ArrayList<>();
	private List<DynamoGame> availableGameList = new ArrayList<>();
	private List<DynamoGame> futureGamesList = new ArrayList<>();	
	private List<PlayerMoney> playerMoneyForSelectedGameList = new ArrayList<PlayerMoney>();
	
	private List<Round> syncGameRoundList = Collections.synchronizedList(new ArrayList<>());		
	private Round selectedRound;
	private Integer fixHole;
	private boolean disableFixScore = true;
	private Integer correctedScore;
	private String roundIDForCorrectedScore;
	private Map<String,List<Round>> roundsForGame = new HashMap<>();
	private List<Round> roundsForGameList = new ArrayList<>();
	
	
	private DualListModel<DynamoPlayer> playersPickList = new DualListModel<>();
	private List<DynamoPlayer> playersPickListSource = new ArrayList<>();
	private List<DynamoPlayer> playersPickListTarget = new ArrayList<>();
	private List<DynamoPlayer> selectedPlayersList = new ArrayList<>();
	
	private boolean showGameTeeTimeList3 = false;
	private boolean showGameTeeTimeList4 = false;
	private boolean showGameTeeTimeList5 = false;
	private boolean showGameTeeTimeList6 = false;
	private boolean showGameTeeTimeList7 = false;
	private boolean showGameTeeTimeList8 = false;	
	private DualListModel<DynamoPlayer> gameTeeTimeList1 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList2 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList3 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList4 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList5 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList6 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList7 = new DualListModel<>();
	private DualListModel<DynamoPlayer> gameTeeTimeList8 = new DualListModel<>();
	private String gameTeeTimeListCaption1 = "";
	private String gameTeeTimeListCaption2 = "";
	private String gameTeeTimeListCaption3 = "";
	private String gameTeeTimeListCaption4 = "";
	private String gameTeeTimeListCaption5 = "";
	private String gameTeeTimeListCaption6 = "";
	private String gameTeeTimeListCaption7 = "";
	private String gameTeeTimeListCaption8 = "";
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();

	private List<SelectItem> teamNumberList = new ArrayList<SelectItem>();
	
	private boolean disableRunGameNavigate = true;	
	
	private String operation = "";
	
	@Inject GolfMain golfmain;

	public String toString()
	{
		return "Game Date: " + this.getSelectedGame().getGameDateDisplay() + " Game ID: " + this.getSelectedGame().getGameID();
	}

	public void onLoadGameList() 
	{
		logger.info(Utils.getLoggedInUserName() + " In onLoadGameList Game.java");
		this.setRenderInquiry(true);
	}
	
	//compose future game email - called from emailFutureGames.xhtml	
	public String fgEmail()
	{
		try
		{
			List<Round> roundsForGame = golfmain.getRoundsForGame(this.getSelectedGame());
			this.setFutureGameEmailMessage(Utils.composeFutureGameEmail(this.getSelectedGame(), golfmain.getFullPlayerList(), golfmain.getTeeTimesByGame(this.getSelectedGame()), roundsForGame));
		}
		catch (Exception e) 
        {
        	logger.error("fgEmail errored: " + e.getMessage(), e);
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
		 	FacesContext.getCurrentInstance().addMessage(null, facesMessage);		 	
        }
		return "";
	}
	
	//send future game email - called from emailFutureGames.xhtml
	public String sendFGEmail()
	{
		try
		{
			List<Round> roundsForGame = golfmain.getRoundsForGame(this.getSelectedGame());
			String emailMessage = Utils.composeFutureGameEmail(this.getSelectedGame(), golfmain.getFullPlayerList(),golfmain.getTeeTimesByGame(this.getSelectedGame()), roundsForGame);
			Utils.sendFutureGameEmail(this.getSelectedGame(),golfmain.getFullPlayerList(), emailMessage);
		}
		catch (Exception e) 
        {
        	logger.error("sendFGEmail errored: " + e.getMessage(), e);
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
		 	FacesContext.getCurrentInstance().addMessage(null, facesMessage);		 	
        }
		return "";
	}
	
	public String selectGameAcid()
	{		
		try 
        {
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		    String acid = ec.getRequestParameterMap().get("operation");
		    String id = ec.getRequestParameterMap().get("id");
		    
		    logger.info("game operation setup for add-change-inquire-delete.  Function is: " + acid);
		    
		    if (id != null)
		    {
		    	this.setSelectedGame(golfmain.getGameByGameID(id));
		    }
		    
		    if (acid.equalsIgnoreCase("Add"))
		    {
		    	this.setOperation("Add");
		    	
		    	this.setRenderInputFields(true);
		    	this.setRenderInquiry(false);
		    	this.setRenderAddUpdateDelete(true);
		    	
		    	this.setSelectedGame(new DynamoGame());
		    	
		    	this.getSelectedGame().setCourseID(golfmain.getPlayersCourseCourseID());
				this.getSelectedGame().setGameDateJava(new Date());
				this.getSelectedGame().setBetAmount(new BigDecimal(20.0));
				this.getSelectedGame().setEachBallWorth(new BigDecimal(0.0));
				this.getSelectedGame().setHowManyBalls(2);
				this.getSelectedGame().setIndividualGrossPrize(new BigDecimal(10.0));
				this.getSelectedGame().setIndividualNetPrize(new BigDecimal(10.0));
				this.getSelectedGame().setPurseAmount(new BigDecimal(0.0));
				this.getSelectedGame().setSkinsPot(new BigDecimal(0.0));
				this.getSelectedGame().setTeamPot(new BigDecimal(0.0));
				this.getSelectedGame().setFieldSize(16);
				this.getSelectedGame().setTotalPlayers(16);
				this.getSelectedGame().setTotalTeams(4);
				this.getSelectedGame().setGameFee(new BigDecimal(20.0));				
		    }
		    else if (acid.equalsIgnoreCase("Update"))
		    {
		    	this.setOperation("Update");
		    	
		    	this.setRenderInputFields(true);
		    	this.setRenderInquiry(false);
		    	this.setRenderAddUpdateDelete(true);	    			    	
		    }
		    else if (acid.equalsIgnoreCase("Delete"))
		    {
		    	this.setOperation("Delete");
		    	
		    	this.setRenderInputFields(false);
		    	this.setRenderInquiry(false);	
		    	this.setRenderAddUpdateDelete(true);
		    }
		    else if (acid.equalsIgnoreCase("View"))
		    {
		    	this.setOperation("View");
		    	
		    	this.setRenderInputFields(false);
		    	this.setRenderInquiry(false);
		    	this.setRenderAddUpdateDelete(true);
		    }
		    else if (acid.equalsIgnoreCase("TeeTimes"))
		    {
		    	golfmain.setGameSpecificTeeTimeList(this.getSelectedGame());
		    	return "/auth/admin/teeTimes.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("SelectPlayers"))
		    {
		    	loadPlayerPickList();
		    	return "/auth/admin/playerPicklist.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("PlayGroups"))
		    {
		    	loadTeeTimePickList();
		    	return "/auth/admin/teeTimePickList.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("GameHandicaps"))
		    {
		    	onLoadGameHandicaps();
		    	return "/auth/admin/gameHandicaps.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("PickTeams"))
		    {
		    	onLoadPickTeams();
		    	return "/auth/admin/pickTeams.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("PreGameEmail"))
		    {
		    	return "/auth/admin/emailPreGame.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("Scores"))
		    {
		    	onLoadGameEnterScores();
		    	return "/auth/gameEnterScores.xhtml";
		    }
		    else if (acid.equalsIgnoreCase("RunGame"))
		    {
		    	return "/auth/admin/runGame.xhtml";
		    }
					    
        } 
        catch (Exception e) 
        {
        	logger.error("selectGameAcid errored: " + e.getMessage(), e);
			FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
		 	FacesContext.getCurrentInstance().addMessage(null, facesMessage);		 	
        }
		
		return "";		
	}	
	
	public void loadPlayerPickList() 
	{
		try
		{
			loadSelectedPlayers(this.getSelectedGame());
			setPlayerPickLists(this.getSelectedGame());	
		}
		catch (Exception e)
		{
			logger.error("loadPlayerPickList failed: " + e.getMessage(), e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"onLoadPlayerPickList failed: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public String loadSelectedPlayers(DynamoGame dynamoGame) throws Exception
	{
		logger.info("load of gameSelectPlayers; loading those already selected");
		
		this.getSelectedPlayersList().clear();
		
		List<Round> roundsForGame = golfmain.getRoundsForGame(dynamoGame);
		for (int i = 0; i < roundsForGame.size(); i++) 
		{
			Round round = roundsForGame.get(i);
			DynamoPlayer player = golfmain.getPlayerByPlayerID(round.getPlayerID());
			this.getSelectedPlayersList().add(player);
		}
				
		return "";
	}
	
	public void loadTeeTimePickList() 
	{
		try
		{
			loadSelectedPlayers(this.getSelectedGame());
			this.setTeeTimeList(golfmain.getTeeTimesByGame(this.getSelectedGame()));			
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
		List<Round> roundsForGameList = golfmain.getRoundsForGame(this.getSelectedGame());
		
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
	
	public String cancelViewAddUpdateDeleteGame()
	{
		this.setRenderInquiry(true);
    	this.setRenderAddUpdateDelete(false);
    	
		return "";
	}
	
	public String addGame()
	{
		operation = "Add";		
		saveGame();
		//emailAdminsAboutGameAddition(this);
		this.setRenderInquiry(true);
    	this.setRenderAddUpdateDelete(false);
    	
		return "";
	}
	
	public String updateGame()
	{
		logger.info("entering updateGameActionListener");
		operation = "Update";		
		saveGame();
		this.setRenderInquiry(true);
    	this.setRenderAddUpdateDelete(false);
    	
		return "";
	}
	
	public String deleteGame()
	{
		logger.info(Utils.getLoggedInUserName() + " entering Delete Game.  About to delete: " + this.getSelectedGame().getGameDate());
		
		try
		{
			golfmain.deleteRoundsFromDB(this.getSelectedGame().getGameID());		
			golfmain.deleteTeeTimesForGameFromDB(this.getSelectedGame().getGameID());
			golfmain.deletePlayerMoneyFromDB(this.getSelectedGame().getGameID());		
			golfmain.deleteGame(this.getSelectedGame().getGameID());
			
			logger.info(Utils.getLoggedInUserName() + " " + this.getSelectedGame().getGameDate() + " successfully deleted");
			this.setSelectedGame(golfmain.getFullGameList().get(0));
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Game successfully deleted",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
			
		}
		catch (Exception e)
		{
			logger.error("Exception in deleteGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in deleteGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		this.setRenderInquiry(true);
    	this.setRenderAddUpdateDelete(false);
    	
		return "";
	}
	
	public String saveGame()
	{
		logger.info("entering saveGame");
		
		try
		{
			if (operation.equalsIgnoreCase("Add"))
			{
				logger.info(Utils.getLoggedInUserName() + " clicked Add game");
				this.getSelectedGame().setGameID(null); //should not have a game id on an add
				this.getSelectedGame().setGameDate(DateToStringConverter.convertDateToDynamoStringFormat(this.getSelectedGame().getGameDateJava()));
				Course course = golfmain.getCoursesMap().get(this.getSelectedGame().getCourseID());
				this.getSelectedGame().setCourseName(course.getCourseName());
				String newGameID = golfmain.addGame(this.getSelectedGame());
				golfmain.addTeeTimes(newGameID, this.getSelectedGame().getTeeTimesString(), this.getSelectedGame().getGameDateJava(), this.getSelectedGame().getCourseName());
				logger.info(Utils.getLoggedInUserName() + " after add Game");
			}
			else if (operation.equalsIgnoreCase("Update"))
			{
				logger.info(Utils.getLoggedInUserName() + " clicked Update game");
				this.getSelectedGame().setGameDate(DateToStringConverter.convertDateToDynamoStringFormat(this.getSelectedGame().getGameDateJava()));
				golfmain.updateGame(this.getSelectedGame());
				logger.info(Utils.getLoggedInUserName() + " after update Game");
			}
			else
			{
				logger.info(Utils.getLoggedInUserName() + " neither add nor update from maintain player dialog - doing nothing");
			}
						
		}
		catch (Exception e)
		{
			logger.error("Exception in saveGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in saveGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		return "";
			
	}
	
	public String updateTeeTimeSetup()
	{
		golfmain.setTeeTimeOperation("Update");
		golfmain.setTeeTimesRenderInquiry(false);
    	golfmain.setTeeTimesRenderAddUpdateDelete(true);
		return "";
	}
	
	public String addTeeTimeSetup()
	{
		golfmain.setTeeTimeOperation("Add");
		golfmain.setTeeTimesRenderInquiry(false);
    	golfmain.setTeeTimesRenderAddUpdateDelete(true);
    	
    	TeeTime tt = new TeeTime();
    	tt.setGameID(this.getSelectedGame().getGameID());
    	tt.setCourseName(this.getSelectedGame().getCourseName());
    	tt.setGameDate(this.getSelectedGame().getGameDateJava());
    	tt.setPlayGroupNumber(4);
    	tt.setTeeTimeString("10:30");
    	golfmain.setSelectedTeeTime(tt);
    	
		return "";
	}		
	
	public String updateTeeTime()
	{
		golfmain.setTeeTimeOperation("Update");
		saveTeeTime();
		golfmain.setTeeTimesRenderInquiry(true);
    	golfmain.setTeeTimesRenderAddUpdateDelete(false);
    	golfmain.setGameSpecificTeeTimeList(this.getSelectedGame());
		return "";
	}
	
	public String addTeeTime()
	{
		golfmain.setTeeTimeOperation("Add");
		saveTeeTime();
		golfmain.setTeeTimesRenderInquiry(true);
    	golfmain.setTeeTimesRenderAddUpdateDelete(false);
    	golfmain.setGameSpecificTeeTimeList(this.getSelectedGame());
		return "";
	}		 
	
	public String deleteTeeTime()
	{
		logger.info(Utils.getLoggedInUserName() + " is deleting a tee time");
		
		try
		{
			TeeTime tt = golfmain.getSelectedTeeTime();
			golfmain.deleteTeeTimeFromDB(golfmain.getSelectedTeeTime().getTeeTimeID());
			
			DynamoGame updatedGame = golfmain.getGameByGameID(tt.getGameID());
			
			updatedGame.setFieldSize(updatedGame.getFieldSize() - 4);
			updatedGame.setTotalPlayers(updatedGame.getFieldSize());
			this.selectTotalPlayers(updatedGame.getTotalPlayers());
			
			golfmain.updateGame(updatedGame);
			
			golfmain.setTeeTimesRenderInquiry(true);
	    	golfmain.setTeeTimesRenderAddUpdateDelete(false);
	    	
	    	golfmain.setGameSpecificTeeTimeList(this.getSelectedGame());
	    	
			emailAdminsAboutTeeTimeRemoval(tt);
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"tee time successfully removed",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);  
	        
	        logger.info(Utils.getLoggedInUserName() + " successfully deleted tee time from game");
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
		logger.info(Utils.getLoggedInUserName() + " inside saveTeeTime");	
		
		try
		{
			if (golfmain.getTeeTimeOperation().equalsIgnoreCase("Add"))
			{
				golfmain.getTeeTimeDAO().addTeeTime(golfmain.getSelectedTeeTime());
				this.getSelectedGame().setFieldSize(this.getSelectedGame().getFieldSize() + 4);
			}
			
			if (golfmain.getTeeTimeOperation().equalsIgnoreCase("Update"))
			{
				golfmain.getTeeTimeDAO().updateTeeTime(golfmain.getSelectedTeeTime());
			}	
			
			this.getSelectedGame().setTotalPlayers(this.getSelectedGame().getFieldSize());
			
			updateGame();			
		}
		catch (Exception e)
		{
			logger.error("Exception when saving tee time: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when saving tee time: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		logger.info(Utils.getLoggedInUserName() + " exiting saveTeeTime");
		
		return "";
			
	}
	
	public String cancelAddUpdateTeeTime()
	{
		golfmain.setTeeTimeOperation("");
		golfmain.setTeeTimesRenderInquiry(true);
    	golfmain.setTeeTimesRenderAddUpdateDelete(false);
		return "";
	}
	
	private void emailAdminsAboutTeeTimeRemoval(TeeTime teeTime1) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Tee time has been removed";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Tee time removal</H3>");
		
		sb.append(NEWLINE);
		
		String teeTimeStr = teeTime1.getTeeTimeString();
		
		sb.append("<H3>" + teeTimeStr + " tee time removed from Game on " + Utils.getDayofWeekString(this.getSelectedGame().getGameDateJava()) + " " + sdf.format(this.getSelectedGame().getGameDateJava()) + "</H3>");
		
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
			DynamoPlayer tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(adminUsers.get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
			
		logger.info(Utils.getLoggedInUserName() + " emailing tee time removal to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, messageContent, emailRecipients); 	
	}
	

	public String selectRoundRowAjax(SelectEvent<Round> event)
	{
		logger.info(Utils.getLoggedInUserName() + " selected a row in Round selection list");
		Round rd = event.getObject();
		this.setSelectedRound(rd);
			
		golfmain.setDisableDeleteSelectedPlayerRound(false);			

		this.setDisableFixScore(false);
		
		return "";
	}	
	
	public String resetTeams() 
	{
		logger.info("User clicked reset teams from player selection screen");
		
		for (int i = 0; i < this.getRoundsForGameList().size(); i++) 
		{
			Round rd = this.getRoundsForGameList().get(i);
			rd.setTeamNumber(0); 
		} 
		return "";
	}	
	
	
	public String pickTeams() 
	{
		logger.info("User clicked pick teams from pick teams screen");
		
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
			for (int i = 0; i < this.getRoundsForGameList().size(); i++) 
			{
				Round rd = this.getRoundsForGameList().get(i);
				if (rd.getTeamNumber() < 0) //means skins only - do not include them in team pick
				{
					continue;
				}
				else
				{
					gamePlayersList.add(rd);
				}
			} 
			
			Collections.sort(gamePlayersList, new RoundComparatorByHandicap());
			
			int playersPerTeam = this.getSelectedGame().getTotalPlayers() / this.getSelectedGame().getTotalTeams(); 
			int totalTeams = this.getSelectedGame().getTotalTeams();
			
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
			
			Comparator<Round> b = Collections.reverseOrder(new RoundComparatorByHandicap()); 
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
				Comparator<Round> d = Collections.reverseOrder(new RoundComparatorByHandicap()); 
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
				Comparator<Round> f = Collections.reverseOrder(new RoundComparatorByHandicap()); 
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
				Comparator<Round> h = Collections.reverseOrder(new RoundComparatorByHandicap()); 
		        Collections.sort(hPlayersList, h);
			}
			
			for (int i = 0; i <= totalTeams-1; i++) 
			{
				Round aPlayer = aPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
				{
					Round rd = this.getRoundsForGameList().get(j);
					DynamoPlayer tempPlayer = rd.getPlayer();
					if (aPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				
				Round bPlayer = bPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
				{
					Round rd = this.getRoundsForGameList().get(j);
					DynamoPlayer tempPlayer = rd.getPlayer();
					if (bPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				Round cPlayer = cPlayersList.get(i);
				
				for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
				{
					Round rd = this.getRoundsForGameList().get(j);
					DynamoPlayer tempPlayer = rd.getPlayer();
					if (cPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
					{
						rd.setTeamNumber(i+1);
						break;
					}
				} 
				
				if (playersPerTeam >= 4)
				{
					Round dPlayer = dPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
					{
						Round rd = this.getRoundsForGameList().get(j);
						DynamoPlayer tempPlayer = rd.getPlayer();
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
					
					for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
					{
						Round rd = this.getRoundsForGameList().get(j);
						DynamoPlayer tempPlayer = rd.getPlayer();
						if (ePlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 6)
				{
					Round fPlayer = fPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
					{
						Round rd = this.getRoundsForGameList().get(j);
						DynamoPlayer tempPlayer = rd.getPlayer();
						if (fPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 7)
				{
					Round gPlayer = gPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
					{
						Round rd = this.getRoundsForGameList().get(j);
						DynamoPlayer tempPlayer = rd.getPlayer();
						if (gPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
						{
							rd.setTeamNumber(i+1);
							break;
						}
					} 
				}
				
				if (playersPerTeam >= 8)
				{
					Round hPlayer = hPlayersList.get(i);
					
					for (int j = 0; j < this.getRoundsForGameList().size(); j++) 
					{
						Round rd = this.getRoundsForGameList().get(j);
						DynamoPlayer tempPlayer = rd.getPlayer();
						if (hPlayer.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
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
		
	public String savePickTeams()
	{
		try
		{
			int totalPlayersForGame = this.getSelectedGame().getTotalPlayers();
			int totalRoundsForGame = 0;
			
			for (int i = 0; i < golfmain.getRoundsForGame(this.getSelectedGame()).size(); i++) 
			{
				totalRoundsForGame++;
				
				Round rd = this.getRoundsForGameList().get(i);
				golfmain.updateRoundTeamNumber(this.getSelectedGame(), rd.getPlayerID(), rd.getTeamNumber());			
			}
			
			FacesContext context = FacesContext.getCurrentInstance();		
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Teams Saved", "Teams saved"));	 
		    
			if (totalRoundsForGame > totalPlayersForGame)
			{
				String msg = "saveAndStayPickTeams: We have more rounds than players for game, this is a big problem.  Total rounds = " + totalRoundsForGame + " and total players for this game = " + totalPlayersForGame;
				throw new Exception(msg);
			}				
			
		}
		catch (Exception e) 
		{
			logger.error(e.getMessage(), e);
			FacesContext context = FacesContext.getCurrentInstance();
		    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage()));
		}	
		
		return "/auth/admin/gameList.xhtml";
	}
	
	public String updateGameHandicaps() throws Exception
	{
		int totalPlayersForGame = this.getSelectedGame().getTotalPlayers();
		int totalRoundsForGame = 0;

		for (int i = 0; i < golfmain.getRoundsForGame(this.getSelectedGame()).size(); i++)
		{
			totalRoundsForGame++;

			Round rd = this.getRoundsForGameList().get(i);
			DynamoPlayer player = rd.getPlayer();
			golfmain.updatePlayer(player);

			DynamoCourseTee ct = golfmain.getCourseTeesMap().get(rd.getCourseTeeID());
			BigDecimal newRoundHandicap = Utils.getCourseHandicap(ct, rd.getPlayer().getHandicap());

			golfmain.updateRoundHandicap(this.getSelectedGame(), player.getPlayerID(), newRoundHandicap);
		}

		this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Game Handicaps Saved", "Game handicaps saved"));

		if (totalRoundsForGame > totalPlayersForGame)
		{
			String msg = "updateGameHandicaps: We have more rounds than players for game, this is a big problem.  Total rounds = " + totalRoundsForGame + " and total players for this game = " + totalPlayersForGame;
			throw new Exception(msg);
		}

		return "/auth/admin/gameList.xhtml";
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
		logger.info(Utils.getLoggedInUserName() + " in onLoadGameEnterScores");
		
		DynamoPlayer tempPlayer = golfmain.getFullPlayersMapByUserName().get(Utils.getLoggedInUserName());
	
		this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		setUpGameEnterScores(tempPlayer);		
			
		return "";
	}
	
	private void setUpGameEnterScores(DynamoPlayer tempPlayer) 
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
					if (rd.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
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
					logger.info(Utils.getLoggedInUserName() + " showing user round for entering scores: roundID = " + tempRd.getRoundID() + " player: " + tempRd.getPlayerName());
				}
			}
		}	
		*/
	}

	public String runGameNavigate()
	{
		boolean allScoresEntered = validateScores();
			
		if (allScoresEntered)
		{
			return "/auth/admin/runGame.xhtml";
		}
		else
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Not all scores entered",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
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
		logger.info(Utils.getLoggedInUserName() + " entering updateAllRounds method");
		
		try
		{
			int updatedRounds = 0;
			
			synchronized (this.getRoundsForGameList())
			{
				for (int i = 0; i < this.getRoundsForGameList().size(); i++) 
				{
					Round tempRound = this.getRoundsForGameList().get(i);
					
					if (tempRound.getHole1Score() == null)
					{
						continue;
					}
					
					logger.info(Utils.getLoggedInUserName() + " in updateAllRounds method roundID = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName());
					
					int frontScore = tempRound.getHole1Score() + tempRound.getHole2Score() + tempRound.getHole3Score();
					frontScore = frontScore + tempRound.getHole4Score() + tempRound.getHole5Score() + tempRound.getHole6Score();
					frontScore = frontScore + tempRound.getHole7Score() + tempRound.getHole8Score() + tempRound.getHole9Score();
					
					tempRound.setFront9Total(frontScore);
					
					int backScore = tempRound.getHole10Score() + tempRound.getHole11Score() + tempRound.getHole12Score();
					backScore = backScore + tempRound.getHole13Score() + tempRound.getHole14Score() + tempRound.getHole15Score();
					backScore = backScore + tempRound.getHole16Score() + tempRound.getHole17Score() + tempRound.getHole18Score();
					
					tempRound.setBack9Total(backScore);
					
					tempRound.setTotalScore(frontScore + backScore); 
										
					tempRound.setNetScore((new BigDecimal(tempRound.getTotalScore())).subtract(tempRound.getRoundHandicap()));
					
					DynamoGame game = golfmain.getGameByGameID(tempRound.getGameID());
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
					
					logger.info(Utils.getLoggedInUserName() + " about to update round = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName() + " score = " + tempRound.getTotalScore());
					
					golfmain.updateRound(tempRound);
					
					logger.info(Utils.getLoggedInUserName() + " completed updating round = " + tempRound.getRoundID() + " player: " + tempRound.getPlayerName() + " score = " + tempRound.getTotalScore());
					
					updatedRounds++;
				}
			}
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,updatedRounds + " Rounds updated",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (Exception e)
		{
			logger.error("Exception in updateAllRounds: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in updateAllRounds: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}		
        
		return "";
	}
	
	public String deleteSelectedPlayerRound()
	{
		try
		{
			Round rd = this.getSelectedRound();
			golfmain.deleteRoundFromDB(rd.getRoundID());
			
			int indexToRemove = -1;
			
			synchronized (this.getSyncGameRoundList())
			{
				for (int i = 0; i < this.getSyncGameRoundList().size(); i++) 
				{
					Round tempRound = this.getSyncGameRoundList().get(i);
					if (tempRound.getRoundID().equalsIgnoreCase(rd.getRoundID()))
					{
						indexToRemove = i;
						break;
					}
				}
			}
			
			this.getSyncGameRoundList().remove(indexToRemove);		
		}
		catch (Exception e)
		{
			logger.error("Exception in deleteSelectedPlayerRound: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in deleteSelectedPlayerRound: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}	
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
	public void createTeamNumberList(DynamoGame game) 
	{
		this.getTeamNumberList().clear();
		
		SelectItem selItem = new SelectItem();
		selItem.setLabel("--select--");
		selItem.setValue("0");
		this.getTeamNumberList().add(selItem);
		
		//add team -1 in case this person is skins only!
		selItem = new SelectItem();
		selItem.setLabel("Skins Only");
		selItem.setValue("-1");
		this.getTeamNumberList().add(selItem);
			
		for (int i = 1; i <= game.getTotalTeams(); i++) 
		{
			selItem = new SelectItem();
			selItem.setLabel(String.valueOf(i));
			selItem.setValue(String.valueOf(i));
			this.getTeamNumberList().add(selItem);
		}			
		
	}
	
	public String onLoadEmailFuture()
	{
		logger.info(Utils.getLoggedInUserName() + " in onLoadEmailFuture");	
		this.setFutureGamesList(golfmain.getFutureGames());
		return "";
	}
	
	public String onLoadGameSignUp()
	{
		logger.info(Utils.getLoggedInUserName() + " in onLoadGameSignUp");
		
		DynamoPlayer tempPlayer = golfmain.getFullPlayersMapByUserName().get(Utils.getLoggedInUserName());	
		
		this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
		logger.info(Utils.getLoggedInUserName() + " At end of onLoadGameSignUp method in Game.java - size of available game list is: " + this.getAvailableGameList().size());		

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
	
	private void resetSignedUpMessage(DynamoGame item)
	{
		this.setSelectedGame(item);
		
		this.getPlayersSignedUpList().clear();		
			
		List<Round> roundList = golfmain.getRoundsForGame(item);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);			
			String signupLine = Utils.getSignupLine(rd);			
			this.getPlayersSignedUpList().add(signupLine);
		}
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String displayedGameDate = sdf.format(this.getSelectedGame().getGameDateJava());		
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Signups for game on " + displayedGameDate + "</H3>");
		
		sb.append(NEWLINE);
		
		for (int i = 0; i < this.getPlayersSignedUpList().size(); i++) 
		{
			sb.append(this.getPlayersSignedUpList().get(i) + NEWLINE);
		}		
	
		this.setWhoIsSignedUpMessage(sb.toString());	
	}	

	public String selectRowSignup(SelectEvent<DynamoGame> event)
	{
		logger.info(Utils.getLoggedInUserName() + " clicked on a row in Game list on game signup screen");
		
		DynamoGame item = event.getObject();
		
		resetSignedUpMessage(item);
		
		return "";
	}	
	
	public String selectRowAjax(SelectEvent<DynamoGame> event)
	{
		logger.info(Utils.getLoggedInUserName() + " clicked on a row in Game list");
		
		DynamoGame item = event.getObject();
		this.setSelectedGame(item);
		this.setShowPlayerSelectionPanel(true);
		this.setShowPregameEmail(true);
		this.setShowPostgameEmail(true);
		this.setDisableEmailStuff(false);
		this.getEmailRecipients().clear();
		this.setPreGameEmailMessage("");
		this.setPostGameEmailMessage("");
				
		return "";
	}	
	
	public void valueChgTotalPlayersAdd(AjaxBehaviorEvent event) 
	{
		logger.info(Utils.getLoggedInUserName() + " changed total players");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{				
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in valueChgTotalPlayersAdd: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgTotalPlayersAdd: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void onLoadGameHandicaps()
	{
		if (this.getSelectedGame() == null)
		{
			this.setSelectedGame(golfmain.getFullGameList().get(0));
			this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		}
		else
		{
			this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		}
	}

	public void onLoadPickTeams()
	{
		if (this.getSelectedGame() == null)
		{
			this.setSelectedGame(golfmain.getFullGameList().get(0));
			this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		}
		else
		{
			this.setRoundsForGameList(golfmain.getRoundsForGame(this.getSelectedGame()));
		}

		createTeamNumberList(this.getSelectedGame());
	}
	
	public void valueChgFieldSize(AjaxBehaviorEvent event) 
	{
		logger.info(Utils.getLoggedInUserName() + " changed field size on update game dialog");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{				
				this.getSelectedGame().setTotalPlayers(selectedOption);
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in valueChgFieldSize: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgFieldSize: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void valueChgTotalPlayersUpdate(AjaxBehaviorEvent event) 
	{
		logger.info(Utils.getLoggedInUserName() + " changed total players on update game dialog");
		
		try
		{
			SelectOneMenu selectonemenu = (SelectOneMenu)event.getSource();
		
			Integer selectedOption = (Integer)selectonemenu.getValue();
			
			if (selectedOption != null)
			{
				selectTotalPlayers(selectedOption);
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in valueChgTotalPlayersUpdate: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in valueChgTotalPlayersUpdate: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	}
	
	public void selectTotalPlayers(Integer totalPlayers) throws Exception 
	{		
		logger.info(Utils.getLoggedInUserName() + " i've got this many players selected now: " + totalPlayers);
		
		golfmain.setRecommendations(totalPlayers);
		
		this.getSelectedGame().setPurseAmount(golfmain.getRecommendedPurseAmount());
		this.getSelectedGame().setTotalTeams(golfmain.getRecommendedTotalTeams());
		this.getSelectedGame().setHowManyBalls(golfmain.getRecommendedHowManyBalls());
		this.getSelectedGame().setEachBallWorth(golfmain.getRecommendedEachBallWorth());
		this.getSelectedGame().setIndividualGrossPrize(golfmain.getRecommendedIndividualGrossPrize());
		this.getSelectedGame().setIndividualNetPrize(golfmain.getRecommendedIndividualNetPrize());
		this.getSelectedGame().setSkinsPot(golfmain.getRecommendedSkinsPot());
		this.getSelectedGame().setTeamPot(golfmain.getRecommendedTeamPot());
		this.getSelectedGame().setGameFee(golfmain.getRecommendedGameFee());
		this.getSelectedGame().setTeeTimesString(golfmain.getRecommendedTeeTimesString());
		this.getSelectedGame().setPlayTheBallMethod(GolfMain.getRecommendedPlayTheBallMethod());
		this.getSelectedGame().setGameNoteForEmail(GolfMain.getRecommendedGameNote());
						
	}
	
	public String signUp(DynamoGame dynamoGame)
	{
		logger.info(Utils.getLoggedInUserName() + " clicked signup button");
		
		try
		{
			DynamoPlayer tempPlayer = golfmain.getFullPlayersMapByUserName().get(Utils.getLoggedInUserName());
			
			if (playerIsAlreadySignedUp(dynamoGame, tempPlayer)) //should not happen but for some reason has been
			{
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Player is already signed up for this game",null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
				return "";
			}
			
			Round round = new Round();
			round.setGameID(dynamoGame.getGameID());
			round.setPlayerID(tempPlayer.getPlayerID());
			round.setPlayer(tempPlayer);
			round.setPlayerName(tempPlayer.getFirstName() + " " + tempPlayer.getLastName());
			round.setTeamNumber(0); //set to skins only for now until admin sets teams up.
			
			round.setCourseTeeID(dynamoGame.getSelectedCourseTeeID());
			
			round.setRoundHandicap(tempPlayer.getHandicap()); //set this to their usga ghin handicap index when they sign up.  We'll tweak this later when entering them on the set game handicaps page
			
			if (dynamoGame.getSelectedCourseTeeID() == null || dynamoGame.getSelectedCourseTeeID().length() == 0)
			{
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"No tee selected - please select tees to play from",null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
			}
			else
			{
				round.setCourseTeeColor(getCourseTeeColor(dynamoGame.getSelectedCourseTeeID()));
				golfmain.addRound(round);
				
				this.getAvailableGameList().clear();
				this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
				
				resetSignedUpMessage(dynamoGame);
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				String displayedGameDate = sdf.format(dynamoGame.getGameDateJava());		
			
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player " + round.getPlayerName() + " successfully signed up for game on " + displayedGameDate,null);
				
				logger.info("Player " + round.getPlayerName() + " successfully signed up for game on " + displayedGameDate);
				
		        FacesContext.getCurrentInstance().addMessage(null, msg);
	
			}
		}
		catch (Exception e)
		{
			logger.error("Exception in signUp: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in signUp: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}	        
		return "";
	}
	
	public String getCourseTeeColor(String courseTeeID) 
	{
		String tempColor = "";
		if (courseTeeID != null && !courseTeeID.equalsIgnoreCase("0"))
		{			
			Map<String,DynamoCourseTee> ctMap = golfmain.getCourseTeesMap();
			DynamoCourseTee ct = ctMap.get(courseTeeID);
			tempColor = ct.getTeeColor();
		}
		return tempColor;
	}
	
	private boolean playerIsAlreadySignedUp(DynamoGame dynamoGame, DynamoPlayer tempPlayer) 
	{
		boolean playerIsSignedUpAlready = false;
		
		List<Round> roundList = golfmain.getRoundsForGame(dynamoGame);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			if (rd != null 
			&& rd.getPlayerID() != null 
			&& tempPlayer != null 
			&& tempPlayer.getPlayerID() != null
			&& rd.getPlayerID().equalsIgnoreCase(tempPlayer.getPlayerID()))
			{
				playerIsSignedUpAlready = true;
				break;
			}
		}
		return playerIsSignedUpAlready;
	}

	public String withdraw(DynamoGame dynamoGame)
	{
		logger.info(Utils.getLoggedInUserName() + " clicked withdraw button");
		
		try
		{
			DynamoPlayer tempPlayer = golfmain.getFullPlayersMapByUserName().get(Utils.getLoggedInUserName());
		
			Round theRound = golfmain.getRoundByGameandPlayer(dynamoGame.getGameID(), tempPlayer.getPlayerID());
			
			golfmain.deleteRoundFromDB(theRound.getRoundID());
			
			this.getAvailableGameList().clear();
			this.setAvailableGameList(golfmain.getAvailableGamesByPlayerID(tempPlayer.getPlayerID()));	
		
			resetSignedUpMessage(dynamoGame);
			
			//If we have a withdrawal AFTER the game has been closed for signups, any admin role needs to know about that.  Email them.
			//if (game1.isGameClosedForSignups())
			//kind of want to always know about this so commented out the if block 2020-07-04
			//{
				emailAdminsAboutWithdrawal(dynamoGame, tempPlayer);
			//}
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String displayedGameDate = sdf.format(dynamoGame.getGameDateJava());		
		
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player " + theRound.getPlayerName() + " successfully withdrew from game on " + displayedGameDate,null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		catch (Exception e)
		{
			logger.error("Exception in withdraw: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in withdraw: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		return "";
	}
	
	private void emailAdminsAboutWithdrawal(DynamoGame dynamoGame, DynamoPlayer tempPlayer) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Player Withdrawal";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Player Withdrawal</H3>");
		
		sb.append(NEWLINE);
		
		String wdPlayer = tempPlayer.getFullName();
		
		sb.append("<H3>" + wdPlayer + " withdrew from Game on " + Utils.getDayofWeekString(dynamoGame.getGameDateJava()) + " " + sdf.format(dynamoGame.getGameDateJava()) + "</H3>");
		
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
				
		List<String> adminUsers = golfmain.getAdminUserList();
		
		//anyone with admin role
		for (int i = 0; i < adminUsers.size(); i++) 
		{
			DynamoPlayer tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(adminUsers.get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
			
		logger.info(Utils.getLoggedInUserName() + " emailing withdrawal to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, withdrawalMessageContent, emailRecipients);
	}

	public void calculatePMTotal(Object o) 
	{	    
	   //logger.info("inside calculateTotal.  Object = " + o);
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
		logger.info("entering setUpWeeklyGame method");
		
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
		logger.info(Utils.getLoggedInUserName() + " entering runSelectedGame method");
		
		try
		{
			this.setPlayerScores(golfmain.getRoundsForGame(this.getSelectedGame()));		
			
			//clear out first for this - in case it has been run before
			golfmain.deletePlayerMoneyFromDB(this.getSelectedGame().getGameID());
			this.getTeamResultsList().clear();
			
			if (!meetInGrillRoomAfterRound)
			{
				addEntryFees();
			}			
			
			calculateSkins();
			calculateTeams();	
			calculateIndividualGrossAndNet();
			
			//this.setPlayerMoneyForSelectedGameList(golfmain.getPlayerMoneyByGame(this.getSelectedGame()));
		}
		catch (Exception e)
		{
			logger.error("Exception in runSelectedGame: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in runSelectedGame: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage("runGameMessageId", msg);    
		}
		return "";
	}
		
	private void addEntryFees() throws Exception 
	{
		for (int i = 0; i < this.getPlayerScores().size(); i++) 
		{
			Round rd = this.getPlayerScores().get(i);
			
			PlayerMoney playerMoney = new PlayerMoney();
			playerMoney.setGameID(this.getSelectedGame().getGameID());
			playerMoney.setPlayerID(rd.getPlayerID());
			
			BigDecimal entryFeeAmount = new BigDecimal(this.getSelectedGame().getBetAmount().doubleValue());
			
			if (rd.getTeamNumber() < 1) //this means skins only
			{
				entryFeeAmount = this.getSelectedGame().getBetAmount().multiply(new BigDecimal("0.4")); //since skins is usually 40% of total pot
			}
			
			entryFeeAmount = entryFeeAmount.multiply(new BigDecimal("-1"));
			
			playerMoney.setAmount(entryFeeAmount);
			playerMoney.setDescription("Entry Fee: " + entryFeeAmount);
			
			golfmain.addPlayerMoney(playerMoney);
		}		
	}

	private void calculateIndividualGrossAndNet() throws Exception 
	{	
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
		
		Map<String,DynamoPlayer> winnersMap = new HashMap<>(); 
		
		int lowestGrossScore = grossScores.get(0);
		BigDecimal lowestNetScore = netScores.get(0);
		int totalGrossWinners = 0;
		int totalNetWinners = 0;
		DynamoPlayer multipleWinningPlayer = null;
		
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
					if (round.getPlayerID().equalsIgnoreCase(multipleWinningPlayer.getPlayerID()))
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
					if (round.getPlayerID().equalsIgnoreCase(multipleWinningPlayer.getPlayerID()))
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
					if (round.getPlayerID().equalsIgnoreCase(multipleWinningPlayer.getPlayerID()))
					{
						lowestNetScore = netScores.get(1);
						netRounds.remove(i);
						break;
					}
				}
			}
			
		}
		
		if (this.getSelectedGame().getIndividualGrossPrize().compareTo(new BigDecimal(0.0)) > 0)
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
			BigDecimal grossPrize = this.getSelectedGame().getIndividualGrossPrize().divide(new BigDecimal(totalGrossWinners), 2, RoundingMode.HALF_UP);
			
			int totalFound = 0;
			for (int i = 0; i < grossRounds.size(); i++) 
			{
				Round round = grossRounds.get(i);
				int grossScore = round.getTotalScore();
				if (grossScore == lowestGrossScore)
				{
					PlayerMoney playerMoney = new PlayerMoney();
					playerMoney.setGameID(this.getSelectedGame().getGameID());
					playerMoney.setPlayerID(round.getPlayerID());
					playerMoney.setAmount(grossPrize);
					playerMoney.setDescription("Low Individual Gross: " + lowestGrossScore);
					
					golfmain.addPlayerMoney(playerMoney);
					
					totalFound++;
					if (totalFound == totalGrossWinners)
					{
						break;
					}
				}
			}
		}
		
		if (this.getSelectedGame().getIndividualNetPrize().compareTo(new BigDecimal(0.0)) > 0)
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
			BigDecimal netPrize = this.getSelectedGame().getIndividualNetPrize().divide(new BigDecimal(totalNetWinners), 2, RoundingMode.HALF_UP);
			
			int totalFound = 0;
			for (int i = 0; i < netRounds.size(); i++) 
			{
				Round round = netRounds.get(i);
				
				BigDecimal netScore = (new BigDecimal(round.getTotalScore())).subtract(round.getRoundHandicap());
				
				if (netScore.compareTo(lowestNetScore) == 0)
				{
					PlayerMoney playerMoney = new PlayerMoney();
					playerMoney.setGameID(this.getSelectedGame().getGameID());
					playerMoney.setPlayerID(round.getPlayerID());
					playerMoney.setAmount(netPrize);
					playerMoney.setDescription("Low Individual Net: " + lowestNetScore);
						
					golfmain.addPlayerMoney(playerMoney);
									
					totalFound++;
					if (totalFound == totalNetWinners)
					{
						break;
					}
				}
			}
		}
				
	}

	private void calculateTeams() throws Exception 
	{
		logger.info(Utils.getLoggedInUserName() + " entering calculateTeams");
			
		int totalMembersPerTeam = this.getSelectedGame().getTotalPlayers() / this.getSelectedGame().getTotalTeams();
		
		//note that if someone is on team zero, they will not be on a team, only skins...and if doing it, individual gross or net.
		for (int teamNumber = 1; teamNumber <= this.getSelectedGame().getTotalTeams(); teamNumber++) 
		{
			String teamName = "";
			//get the team member scores only
			List<Round> teamRoundsList = new ArrayList<Round>();
			for (int i = 0; i < playerScores.size(); i++) 
			{
				Round round = playerScores.get(i);
				DynamoPlayer player = round.getPlayer();
				if (round.getTeamNumber() != teamNumber)
				{
					continue; // just re-loop if this player is on another team
				}
				
				teamName = teamName + player.getFirstName() + " " + player.getLastName() + " ";
				teamRoundsList.add(round);
				
				if (teamRoundsList.size() == totalMembersPerTeam)
				{
					break; // no need to continue if we have everyone's card...
				}
			}
			
			//ok now we have everyone's score card on this team.
			for (int i = 1; i <= this.getSelectedGame().getHowManyBalls(); i++)
			{
				logger.info(Utils.getLoggedInUserName() + " working on team: " + teamNumber + " ball: " + i);
				
				Round round = new Round();
				DynamoPlayer tempPlayer = new DynamoPlayer();
				tempPlayer.setLastName("Ball " + i);
				tempPlayer.setFirstName(teamName);
				round.setPlayer(tempPlayer);				

				for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
				{
					logger.info(Utils.getLoggedInUserName() + "      working on hole: " + holeNumber);					
					int lowestScore = Utils.getTeamScoreOnHole(teamRoundsList, holeNumber, i);  //i represents ball number					
					round = Utils.setDisplayScore(holeNumber, lowestScore, this.getSelectedGame().getCourse(), round);
				}
				
				round.setFront9Total(Utils.front9Score(round));
				round.setBack9Total(Utils.back9Score(round));
				round.setTotalScore(round.getBack9Total() + round.getFront9Total());
				
				int totalToParInt = round.getTotalScore() - (this.getSelectedGame().getCourse().getFront9Par() + this.getSelectedGame().getCourse().getBack9Par());
				String totalToPar = String.valueOf(totalToParInt);
				if (totalToParInt > 0)
				{
					totalToPar = "+" + totalToPar;					
				}
				round.setTotalToPar(totalToPar);
				
				if (totalToParInt < 0)
				{
					round.setTotalToParClass(Utils.BIRDIE_OR_BETTER_STYLECLASS);
				}
				else
				{
					round.setTotalToParClass(Utils.PAR_OR_WORSE_STYLECLASS);
				}
				round = Utils.setDisplayScore(Utils.FRONT9_STYLE_HOLENUM, round.getFront9Total(), this.getSelectedGame().getCourse(), round);
				round = Utils.setDisplayScore(Utils.BACK9_STYLE_HOLENUM, round.getBack9Total(), this.getSelectedGame().getCourse(), round);
				round = Utils.setDisplayScore(Utils.TOTAL_STYLE_HOLENUM, round.getFront9Total() + round.getBack9Total(), this.getSelectedGame().getCourse(), round);
				
				this.getTeamResultsList().add(round);
			}		
			
		}
		
		Round tempRound = this.getTeamResultsList().get(0);
		String teamSummaryTeamName = tempRound.getPlayer().getFirstName();
		StringBuffer sb = new StringBuffer();
		sb.append(teamSummaryTeamName);
		sb.append(" ********* ");
		
		for (int i = 0; i < this.getTeamResultsList().size(); i++)
		{
			Round r = this.getTeamResultsList().get(i);
			DynamoPlayer tempPlayer = r.getPlayer();

			if (!teamSummaryTeamName.equalsIgnoreCase(tempPlayer.getFirstName()))
			{
				this.getTeamSummaryList().add(sb.toString());			
				sb.setLength(0);
				teamSummaryTeamName = tempPlayer.getFirstName();
				sb.append(teamSummaryTeamName);
				sb.append(" ********* ");
			}			
			
			sb.append(tempPlayer.getLastName());
			sb.append(" ");
			sb.append(r.getTotalToPar());	
			sb.append(" ********* ");
		}
		
		this.getTeamSummaryList().add(sb.toString());	
		
		calcTeamIndividualWinnings();
				
		logger.info(Utils.getLoggedInUserName() + " leaving calculateTeams");		
	}

	private void calcTeamIndividualWinnings() throws Exception 
	{
		//First, which team(s) won each ball
		
		Integer playersPerTeamInt = this.getSelectedGame().getTotalPlayers() / this.getSelectedGame().getTotalTeams();
		BigDecimal playersPerTeam = new BigDecimal(playersPerTeamInt);
		
		for (int i = 1; i <= this.getSelectedGame().getHowManyBalls(); i++)
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
				
				if (ballRoundsList.size() == this.getSelectedGame().getTotalTeams())
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
			logger.info(Utils.getLoggedInUserName() + " Ball " + i + " has " + winningBallList.size() + " winner(s)");
		
			BigDecimal individualBallPrize = this.getSelectedGame().getEachBallWorth().divide(new BigDecimal(winningBallList.size()).multiply(playersPerTeam), 2, RoundingMode.HALF_UP);
			logger.info(Utils.getLoggedInUserName() + " Ball " + i + " individualBallPrize = " + individualBallPrize);
			
			//for each player on these teams, they get the individual ball prize.
			for (int j = 0; j < winningBallList.size(); j++)
			{
				Round winningBallRound = winningBallList.get(j);
				int winningTeamNumber = winningBallRound.getTeamNumber();
				
				for (int k = 0; k < this.getPlayerScores().size(); k++) 
				{
					Round playerRound = this.getPlayerScores().get(k);
					
					if (playerRound.getTeamNumber() == winningTeamNumber) 
					{
						PlayerMoney playerMoney = new PlayerMoney();
						playerMoney.setGameID(this.getSelectedGame().getGameID());
						playerMoney.setPlayerID(playerRound.getPlayerID());
						playerMoney.setAmount(individualBallPrize);
						playerMoney.setDescription("Ball " + i);
						
						golfmain.addPlayerMoney(playerMoney);
					}
				}
				
			}
		}
		
	}

	private void calculateSkins() throws Exception 
	{
		logger.info(Utils.getLoggedInUserName() + " entering calculateSkins");
		
		int totalSkins = 0;
		List<SkinWinnings> tempSkinsList = new ArrayList<SkinWinnings>();
		
		for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
		{	
			Map<Integer,DynamoPlayer> holeScoreMap = new HashMap<>(); //we'll end up with only unique scores here when done with all players
			List<Integer> holeScoreList = new ArrayList<Integer>();
			for (int i = 0; i < playerScores.size(); i++) 
			{
				Round round = playerScores.get(i);
				Score score = new Score();
				switch (holeNumber) 
				{
					case 1:	
						round = Utils.setDisplayScore(holeNumber, round.getHole1Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole1Score());									
						break;					
					case 2:	
						round = Utils.setDisplayScore(holeNumber, round.getHole2Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole2Score());								
						break;					
					case 3:	
						round = Utils.setDisplayScore(holeNumber, round.getHole3Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole3Score());
						break;					
					case 4:	
						round = Utils.setDisplayScore(holeNumber, round.getHole4Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole4Score());
						break;					
					case 5:	
						round = Utils.setDisplayScore(holeNumber, round.getHole5Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole5Score());
						break;					
					case 6:	
						round = Utils.setDisplayScore(holeNumber, round.getHole6Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole6Score());
						break;
					case 7:	
						round = Utils.setDisplayScore(holeNumber, round.getHole7Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole7Score());
						break;					
					case 8:	
						round = Utils.setDisplayScore(holeNumber, round.getHole8Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole8Score());
						break;					
					case 9:	
						round = Utils.setDisplayScore(holeNumber, round.getHole9Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole9Score());
						break;
						
					//back 9
					case 10:
						round = Utils.setDisplayScore(holeNumber, round.getHole10Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole10Score());
						break;					
					case 11:
						round = Utils.setDisplayScore(holeNumber, round.getHole11Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole11Score());
						break;					
					case 12:
						round = Utils.setDisplayScore(holeNumber, round.getHole12Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole12Score());
						break;					
					case 13:
						round = Utils.setDisplayScore(holeNumber, round.getHole13Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole13Score());
						break;					
					case 14:
						round = Utils.setDisplayScore(holeNumber, round.getHole14Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole14Score());
						break;					
					case 15:	
						round = Utils.setDisplayScore(holeNumber, round.getHole15Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole15Score());
						break;					
					case 16:
						round = Utils.setDisplayScore(holeNumber, round.getHole16Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole16Score());
						break;					
					case 17:
						round = Utils.setDisplayScore(holeNumber, round.getHole17Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole17Score());
						break;					
					case 18:
						round = Utils.setDisplayScore(holeNumber, round.getHole18Score(), this.getSelectedGame().getCourse(), round);
						score.setScore(round.getHole18Score());
						break;
						
					default:
						break;
				}
				holeScoreMap.put(score.getScore(), round.getPlayer());
				holeScoreList.add(score.getScore());
			}
			
			TreeMap<Integer, DynamoPlayer> holeScoreTreeMap = new TreeMap<Integer, DynamoPlayer>();
			holeScoreTreeMap.putAll(holeScoreMap); //now we're sorted
			
			int lowestScore = 0;
			DynamoPlayer player = new DynamoPlayer();
			for (Map.Entry<Integer, DynamoPlayer> entry : holeScoreTreeMap.entrySet()) 
			{
				lowestScore = entry.getKey();
				player = entry.getValue();
				break; //once we get the first one, we're outta here
			}
			
			//now we know the lowest score and who has it; now it's just a matter of does anyone else have it..
			int scoreCount = 0;
			for (int j = 0; j < holeScoreList.size(); j++) 
			{
				int scoreInt = holeScoreList.get(j);
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
				skinWinnings.setPlayerID(player.getPlayerID());
				skinWinnings.setPlayerName(player.getFullName());
				skinWinnings.setWinDescription(lowestScore + " on hole " + holeNumber);
				tempSkinsList.add(skinWinnings);				
			}
           
		}
		
		if (totalSkins > 0)
		{
			BigDecimal skinValue = this.getSelectedGame().getSkinsPot().divide(new BigDecimal(totalSkins), 2, RoundingMode.HALF_UP);
			
			logger.info(Utils.getLoggedInUserName() + " Skins won: " + totalSkins + " at " + skinValue + " each");
			
			for (int i = 0; i < tempSkinsList.size(); i++) 
			{
				SkinWinnings skinWinnings = tempSkinsList.get(i);
				skinWinnings.setAmountWon(skinValue);
				this.skinWinningsList.add(skinWinnings);
				
				PlayerMoney playerMoney = new PlayerMoney();
				playerMoney.setGameID(this.getSelectedGame().getGameID());
				playerMoney.setPlayerID(skinWinnings.getPlayerID());
				playerMoney.setAmount(skinValue);
				playerMoney.setDescription("Skin: " + skinWinnings.getWinDescription());
				
				golfmain.addPlayerMoney(playerMoney);
			}
		}		
		
		logger.info(Utils.getLoggedInUserName() + " leaving calculateSkins");
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
		logger.info(Utils.getLoggedInUserName() + " clicked save new players");
		
		return "";
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
		logger.info(Utils.getLoggedInUserName() + " clicked show scores button");
		this.setShowPlayerScores(true);
		return "";
	}

	public String composeTestEmail()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Golf Test Email sent to admin</H3>");
		
		sb.append(NEWLINE);
		sb.append(NEWLINE);
		sb.append("<a href='" + Utils.WEBSITE_URL + "'>Golf Scoring</a>");
		
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
		
		logger.info(Utils.getLoggedInUserName() + " emailing to: " + emailRecipients);
		
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
		
		/*
		StringBuffer sbIndividualWinnings = getEmailIndividualWinnings();
		sb.append(sbIndividualWinnings);
		*/
		
		this.setPostGameEmailMessage(sb.toString());
		
		establishEmailRecipients();
		
		return "";
	}
	
	/*
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
		
		if (!meetInGrillRoomAfterRound)
		{
			sb.append("<H3>Venmo Totals  (Assumes no one paid in 20)</H3>");
		}
		else
		{
			sb.append("<H3>Individual Totals  (Assumes everyone paid in 20)</H3>");
		}
		
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
*/
	
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
		
		logger.info(Utils.getLoggedInUserName() + " team results html");
		logger.info(Utils.getLoggedInUserName() + sb.toString());
		
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
		
		logger.info(Utils.getLoggedInUserName() + " scores html");
		logger.info(Utils.getLoggedInUserName() + sb.toString());
		
		return sb;
	}

	public String navigateToEmail()
	{
		return "/auth/admin/emailPostGame.xhtml";
	}	

	public String onLoadPreGameEmail() 
	{
		return "success";		
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
			logger.error("Exception when composing pregame email.  Have tee times been assigned to everyone?: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception when composing pregame email.  Have tee times been assigned to everyone? " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
		
		return "";
	}
	
	/*
	private StringBuffer doYourOwnScoresBlurb() 
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		sb.append("When done with your round if one person in the group could login to the website <a href='" + WEBSITE_URL + "'>Golf Scoring</a> ");
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
				
		logger.info(Utils.getLoggedInUserName() + " emailing to: " + emailRecipients);
		
	}

	private StringBuffer getEmailMoneyTeamDetails() 
	{
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

	/*
	private void emailAdminsAboutGameAddition(Game game1) 
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Game has been added";
	
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>Game addition</H3>");
		
		sb.append(NEWLINE);
		
		sb.append("<H3> Game added: " + Utils.getDayofWeekString(game1.getSelectedGame().getGameDateJava()) + " " + sdf.format(game1.getSelectedGame().getGameDateJava()) + "</H3>");
		
		String messageContent = sb.toString();		
	
		emailRecipients = getEmailAdminsRecipientList();
			
		logger.info(Utils.getLoggedInUserName() + " emailing game add to: " + emailRecipients);
		
		SAMailUtility.sendEmail(subjectLine, messageContent, emailRecipients, true); //last param means use jsf		
	}
	
	
	private ArrayList<String> getEmailAdminsRecipientList() 
	{
		ArrayList<String> emailRecipients = new ArrayList<String>();
		
		//anyone with admin role
		for (int i = 0; i < golfmain.getAdminUserList().size(); i++) 
		{
			DynamoPlayer tempPlayer2 = golfmain.getFullPlayersMapByUserName().get(golfmain.getAdminUserList().get(i));			
			emailRecipients.add(tempPlayer2.getEmailAddress());
		}
		
		return emailRecipients;
	}
	*/	
	private StringBuffer getEmailGameDetails() 
	{
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		StringBuffer sb = new StringBuffer();
		
		sb.append(NEWLINE);
		
		DynamoGame gm = this.getSelectedGame();
		sb.append("Day: " + Utils.getDayofWeekString(gm.getGameDateJava()) + NEWLINE);
		sb.append("Date: " + gm.getGameDateDisplay() + NEWLINE);
		sb.append("Course: " + gm.getCourseName() + NEWLINE);
		sb.append("Bet Amt: " + currencyFmt.format(gm.getBetAmount()) + NEWLINE);
		sb.append("Total Players: " + gm.getTotalPlayers() + NEWLINE);
		sb.append("Purse: " + currencyFmt.format(gm.getPurseAmount()) + NEWLINE);
		sb.append("Total Teams: " + gm.getTotalTeams() + NEWLINE);
		sb.append("Team Balls: " + gm.getHowManyBalls() + NEWLINE);
		sb.append("Team Ball Value: " + currencyFmt.format(gm.getEachBallWorth()) + NEWLINE);
		sb.append("Team Pot: " + currencyFmt.format(gm.getTeamPot()) + NEWLINE);
		sb.append("Skins Pot: " + currencyFmt.format(gm.getSkinsPot()) + NEWLINE);
		sb.append("Game Fee: " + currencyFmt.format(gm.getGameFee()) + NEWLINE);
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
		
		//sb.append("Gold tee players move back a tee box if necessary to stay within 1 tee box of whites. " + NEWLINE);
		sb.append("All scores and settling of bets must happen manually in the Pro Shop grill after the round. " + NEWLINE);
		sb.append("If you are unable to stay after the round for whatever reason, please arrange with someone to settle up your entry fee and how you want any winnings to be paid. " + NEWLINE);
		//sb.append("In a pinch we could still use the website and venmo but that MUST BE PRE-ORGANIZED WITH A SITE ADMIN like Paul Slomkowski");
		//sb.append("or Kenton Robertson or whomever is temporarily assigned an admin for a given game." + NEWLINE);		
		
		return sb;
	}

	public static class GameComparatorByDate implements Comparator<DynamoGame> 
	{
		public int compare(DynamoGame game1, DynamoGame game2)
		{
			if (game1.getGameDateJava().after(game2.getGameDateJava()))
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
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
			String subjectLine = "Golf game setup for " + sdf.format(this.getSelectedGame().getGameDateJava());
			SAMailUtility.sendEmail(subjectLine, preGameEmailMessage, emailRecipients); 
		} 
        catch (Exception e) 
        {
            logger.error("exception: " + e.getMessage(), e);
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
		 	FacesContext.getCurrentInstance().addMessage(null, facesMessage);		 	
        }
		
		return "";
	}
	
	public String sendPostGameEmail()
	{
		try
		{
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
			String subjectLine = "Golf results for " + sdf.format(this.getSelectedGame().getGameDateJava());
			SAMailUtility.sendEmail(subjectLine, postGameEmailMessage, emailRecipients); 
		} 
        catch (Exception e) 
        {
            logger.error("exception: " + e.getMessage(), e);
            FacesMessage facesMessage = new FacesMessage(FacesMessage.SEVERITY_ERROR, e.getMessage(), e.getMessage());
		 	FacesContext.getCurrentInstance().addMessage(null, facesMessage);		 	
        }
		return "";
	}
	
	public String sendTestEmail()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf Test email sent to admin on " + sdf.format(new Date());
		SAMailUtility.sendEmail(subjectLine, testEmailMessage, emailRecipients); 
		return "";
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

	public DynamoGame getSelectedGame() {
		return selectedGame;
	}

	public void setSelectedGame(DynamoGame selectedGame) {
		this.selectedGame = selectedGame;
	}
	
	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
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

	public String getFutureGameEmailMessage() {
		return futureGameEmailMessage;
	}

	public void setFutureGameEmailMessage(String futureGameEmailMessage) {
		this.futureGameEmailMessage = futureGameEmailMessage;
	}

	public boolean isGameClosedForSignups() {
		return gameClosedForSignups;
	}

	public void setGameClosedForSignups(boolean gameClosedForSignups) {
		this.gameClosedForSignups = gameClosedForSignups;
	}

	public List<String> getTeamSummaryList() {
		return teamSummaryList;
	}

	public void setTeamSummaryList(List<String> teamSummaryList) {
		this.teamSummaryList = teamSummaryList;
	}

	public List<DynamoGame> getAvailableGameList() {
		return availableGameList;
	}

	public void setAvailableGameList(List<DynamoGame> availableGameList) {
		this.availableGameList = availableGameList;
	}

	public List<DynamoGame> getFutureGamesList() {
		return futureGamesList;
	}

	public void setFutureGamesList(List<DynamoGame> futureGamesList) {
		this.futureGamesList = futureGamesList;
	}

	public Map<String, List<Round>> getRoundsForGame() {
		return roundsForGame;
	}

	public void setRoundsForGame(Map<String, List<Round>> roundsForGame) {
		this.roundsForGame = roundsForGame;
	}

	public List<Round> getRoundsForGameList() {
		return roundsForGameList;
	}

	public void setRoundsForGameList(List<Round> roundsForGameList) {
		this.roundsForGameList = roundsForGameList;
	}

	public List<SelectItem> getTeamNumberList() {
		return teamNumberList;
	}

	public void setTeamNumberList(List<SelectItem> teamNumberList) {
		this.teamNumberList = teamNumberList;
	}

	public boolean isRenderInputFields() {
		return renderInputFields;
	}

	public void setRenderInputFields(boolean renderInputFields) {
		this.renderInputFields = renderInputFields;
	}

	public boolean isRenderInquiry() {
		return renderInquiry;
	}

	public void setRenderInquiry(boolean renderInquiry) {
		this.renderInquiry = renderInquiry;
	}

	public boolean isRenderAddUpdateDelete() {
		return renderAddUpdateDelete;
	}

	public void setRenderAddUpdateDelete(boolean renderAddUpdateDelete) {
		this.renderAddUpdateDelete = renderAddUpdateDelete;
	}

	public boolean isDisableRunGameNavigate() {
		return disableRunGameNavigate;
	}

	public void setDisableRunGameNavigate(boolean disableRunGameNavigate) {
		this.disableRunGameNavigate = disableRunGameNavigate;
	}

	public List<Round> getSyncGameRoundList() {
		return syncGameRoundList;
	}

	public void setSyncGameRoundList(List<Round> syncGameRoundList) {
		this.syncGameRoundList = syncGameRoundList;
	}

	public Round getSelectedRound() {
		return selectedRound;
	}

	public void setSelectedRound(Round selectedRound) {
		this.selectedRound = selectedRound;
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

	public GolfMain getGolfmain() {
		return golfmain;
	}

	public void setGolfmain(GolfMain golfmain) {
		this.golfmain = golfmain;
	}

	public List<DynamoPlayer> getSelectedPlayersList() {
		return selectedPlayersList;
	}

	public void setSelectedPlayersList(List<DynamoPlayer> selectedPlayersList) {
		this.selectedPlayersList = selectedPlayersList;
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

	public List<TeeTime> getTeeTimeList() {
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) {
		this.teeTimeList = teeTimeList;
	}
	
}

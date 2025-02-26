package com.pas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.event.UnselectEvent;

import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoPlayer;
import com.pas.dynamodb.DynamoPlayerTeePreference;
import com.pas.util.Utils;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pc_Player")
@SessionScoped
public class Player implements Serializable
{	
	private static final long serialVersionUID = 4089402354585236177L;
	private static Logger logger = LogManager.getLogger(Player.class);
		
	private boolean resetPassword;
	
	private DynamoPlayer selectedPlayer;
	private DynamoPlayerTeePreference selectedPlayerTeePreference;
	
	private boolean renderInquiry = true;
	private boolean renderAddUpdate = false;
	
	
			
	private String operation = "";
	
	@Inject GolfMain golfmain;
	@Inject Game game;

	public String selectPlayerAcid()
	{		
		try 
        {
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
		    String acid = ec.getRequestParameterMap().get("operation");
		    String id = ec.getRequestParameterMap().get("id");
		    
		    logger.info("player operation setup for add-change-delete.  Function is: " + acid);
		    
		    if (id != null)
		    {
		    	this.setSelectedPlayer(golfmain.getPlayerByPlayerID(id));
		    }
		    
		    if (acid.equalsIgnoreCase("Add"))
		    {
		    	this.setOperation("Add");
		    	this.setRenderInquiry(false);
		    	this.setRenderAddUpdate(true);
		    	
		    	this.setSelectedPlayer(new DynamoPlayer());
		    	
		    	this.getSelectedPlayer().setActive(true);		
		    }
		    else if (acid.equalsIgnoreCase("Update"))
		    {
		    	this.setOperation("Update");
		      	this.setRenderInquiry(false);
		    	this.setRenderAddUpdate(true);	    			    	
		    }
		    else if (acid.equalsIgnoreCase("TeePrefs"))
		    {
		    	golfmain.setPlayerSpecificTeePreferencesList(this.getSelectedPlayer());
		    	return "/auth/admin/playerTeePrefs.xhtml";
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
		
	public String returnToPlayerList()
	{
		return "/auth/admin/playerList.xhtml";
	}
	
	public String cancelAddUpdatePlayer()
	{
		this.setRenderInquiry(true);
    	this.setRenderAddUpdate(false);    	
		return "";
	}	
	
	public String savePlayer()
	{
		logger.info("user clicked Save Player");	
		
		try
		{
			if (operation.equalsIgnoreCase("Add"))
			{
				logger.info("user clicked Save Player from an add");	
				
				//first need to make sure the chosen userid does not already exist in the system.
				DynamoPlayer existingPlayer = golfmain.getPlayerByUserName(this.getSelectedPlayer().getUsername());
				
				if (existingPlayer == null)
				{
					String newPlayerID = golfmain.addPlayer(this.getSelectedPlayer());
					golfmain.addUser(this.getSelectedPlayer().getUsername(), this.getSelectedPlayer().getUsername(), "USER"); //default their password to their username
					
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
				logger.info("user clicked Save Player from an update");			
				golfmain.updatePlayer(this.getSelectedPlayer());
				
				if (this.isResetPassword())
				{
					GolfUser gu = golfmain.getGolfUser(this.getSelectedPlayer().getUsername());
					gu.setPassword(gu.getUserName()); //default their password to their username
					golfmain.updateUser(this.getSelectedPlayer().getUsername(), gu.getPassword(), this.getSelectedPlayer().getRole());
				}
											
				logger.info("after update Player");
			}
			else
			{
				logger.info("neither add nor update from maintain player dialog - doing nothing");
			}
			
			this.setRenderInquiry(true);
	    	this.setRenderAddUpdate(false);    	
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
		String teePreference = this.getSelectedPlayer().getTeePreference();
		
		List<DynamoCourseTee> courseTees = golfmain.getCourseTeesList();
		List<Course> courses = golfmain.getCoursesList();
		
		for (int j = 0; j < courses.size(); j++) 
		{
			Course course = courses.get(j);
			PlayerTeePreference playerTeePreference = new PlayerTeePreference();
			playerTeePreference.setPlayerID(newPlayerID);
			playerTeePreference.setCourseID(course.getCourseID());
			playerTeePreference.setPlayerFullName(this.getSelectedPlayer().getFirstName() + " " + this.getSelectedPlayer().getLastName());
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
				
				for (int i = 0; i < game.getPlayersPickList().getTarget().size(); i++) 
				{
					DynamoPlayer tempPlayer = (DynamoPlayer) game.getPlayersPickList().getTarget().get(i);
					
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
	
	public String saveTeeTimesPickList()
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
			
				for (int i = 0; i < game.getTeeTimeList().size(); i++) 
				{
					TeeTime teeTime = game.getTeeTimeList().get(i);
					
					List<DynamoPlayer> tempPlayerList = new ArrayList<>();
					
					switch (i) 
					{
						case 0:
							
							tempPlayerList = game.getGameTeeTimeList1().getTarget();
							break;
							
						case 1:
							
							tempPlayerList = game.getGameTeeTimeList2().getTarget();
							break;	
							
						case 2:
							
							tempPlayerList = game.getGameTeeTimeList3().getTarget();
							break;	
							
						case 3:
							
							tempPlayerList = game.getGameTeeTimeList4().getTarget();
							break;	
							
						case 4:
			
							tempPlayerList = game.getGameTeeTimeList5().getTarget();
							break;
							
						case 5:
							
							tempPlayerList = game.getGameTeeTimeList6().getTarget();
							break;	
							
						case 6:
							
							tempPlayerList = game.getGameTeeTimeList7().getTarget();
							break;	
							
						case 7:
							
							tempPlayerList = game.getGameTeeTimeList8().getTarget();
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
						String msg = "Player:saveTeeTimesPickList: We have more rounds than players for game, this is a big problem.  Total rounds = " + totalRoundsForGame + " and total players for this game = " + totalPlayersForGame;
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
		
		return "/auth/admin/gameList.xhtml";
		
	}
	
	public String savePlayersPickList() throws Exception
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
			
			game.loadSelectedPlayers(game.getSelectedGame()); //this resets roundsforgame.  If they deleted a player then we need this list reset

		}		
			
		return "/auth/admin/gameList.xhtml";
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

	public static class PlayerComparatorByHandicap implements Comparator<DynamoPlayer> 
	{
		public int compare(DynamoPlayer player1, DynamoPlayer player2)
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
	
	public String deletePlayer()
	{
		logger.info(Utils.getLoggedInUserName() + " entering Delete Player.  About to delete: " + this.getSelectedPlayer());
		
		try
		{
			ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
			String id = ec.getRequestParameterMap().get("id");
				    
		    if (id != null)
		    {
		    	this.setSelectedPlayer(golfmain.getPlayerByPlayerID(id));
		    }
		    
			golfmain.deletePlayerTeePreferences(this.getSelectedPlayer());
			golfmain.deletePlayerMoneyFromDB(this.getSelectedPlayer());		
			golfmain.deletePlayer(this.getSelectedPlayer());
			golfmain.deleteGolfUser(this.getSelectedPlayer().getUsername());
			
			logger.info(Utils.getLoggedInUserName() + " " + this.getSelectedPlayer() + " successfully deleted");
			
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,"Player " + this.getSelectedPlayer().getFullName() +  " successfully deleted",null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);  			
		}
		catch (Exception e)
		{
			logger.error("Exception in deletePlayer: " +e.getMessage(),e);
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Exception in deletePlayer: " + e.getMessage(),null);
	        FacesContext.getCurrentInstance().addMessage(null, msg);    
		}
	 	
		return "";
	}
	
	public String selectMultiRowAjax(UnselectEvent<Player> event)
	{
		logger.info("User unchecked a checkbox in Player selection list");				
		return "";
	}
		
	public String selectRowAjax(SelectEvent<DynamoPlayer> event)
	{
		logger.info("User clicked on a row in Player list");
		
		DynamoPlayer item = event.getObject();
		
		this.setSelectedPlayer(item);
		
		//get the role for this player on the authorities table
		GolfUser gu = golfmain.getGolfUser(item.getUsername());
		
		String userRole = gu.getUserRole();
		item.setRole(userRole);
		
		setOperation("Update");
		
		return "";
	}		
	
	public int getPickListTargetPlayersSelected() 
	{
		int tempInt = 0;
		
		if (game.getPlayersPickList() != null 
		&& game.getPlayersPickList().getTarget() != null 		
		&& game.getPlayersPickList().getTarget().size() > 0)
		{
			tempInt = game.getPlayersPickList().getTarget().size();
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
			
			for (int i = 0; i < game.getTeeTimeList().size(); i++) 
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
							
							List<DynamoPlayer> sourceList0 = game.getGameTeeTimeList1().getSource();
							
							if (game.getGameTeeTimeList1().getTarget() != null 
							&&  game.getGameTeeTimeList1().getTarget().size() < 4)
							{
								sourceList0.add(playerMoved);										
							}
							
							break; 
							
						case 1:
							
							List<DynamoPlayer> sourceList1 = game.getGameTeeTimeList2().getSource();
							if (game.getGameTeeTimeList2().getTarget() != null 
							&&  game.getGameTeeTimeList2().getTarget().size() < 4)
							{
								sourceList1.add(playerMoved);										
							}
											
							break;
							
						case 2:
							
							List<DynamoPlayer> sourceList2 = game.getGameTeeTimeList3().getSource();
							if (game.getGameTeeTimeList3().getTarget() != null 
							&&  game.getGameTeeTimeList3().getTarget().size() < 4)
							{
								sourceList2.add(playerMoved);										
							}
														
							break;	
							
						case 3:
							
							List<DynamoPlayer> sourceList3 = game.getGameTeeTimeList4().getSource();
							if (game.getGameTeeTimeList4().getTarget() != null 
							&&  game.getGameTeeTimeList4().getTarget().size() < 4)
							{
								sourceList3.add(playerMoved);										
							}
								
							break;	
							
						case 4:
			
							List<DynamoPlayer> sourceList4 = game.getGameTeeTimeList5().getSource();
							if (game.getGameTeeTimeList5().getTarget() != null 
							&&  game.getGameTeeTimeList5().getTarget().size() < 4)
							{
								sourceList4.add(playerMoved);										
							}
								
							break;
							
						case 5:
							
							List<DynamoPlayer> sourceList5 = game.getGameTeeTimeList6().getSource();
							if (game.getGameTeeTimeList6().getTarget() != null 
							&&  game.getGameTeeTimeList6().getTarget().size() < 4)
							{
								sourceList5.add(playerMoved);										
							}
							
							break;	
							
						case 6:
							
							List<DynamoPlayer> sourceList6 = game.getGameTeeTimeList7().getSource();
							if (game.getGameTeeTimeList7().getTarget() != null 
							&&  game.getGameTeeTimeList7().getTarget().size() < 4)
							{
								sourceList6.add(playerMoved);										
							}
								
							break;	
							
						case 7:
							
							List<DynamoPlayer> sourceList7 = game.getGameTeeTimeList8().getSource();
							if (game.getGameTeeTimeList8().getTarget() != null 
							&&  game.getGameTeeTimeList8().getTarget().size() < 4)
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
			
			for (int i = 0; i < game.getTeeTimeList().size(); i++) 
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
							
							List<DynamoPlayer> sourceList0 = game.getGameTeeTimeList1().getSource();
							for (int j = 0; j < sourceList0.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList0.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList0.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList0 = game.getGameTeeTimeList1().getTarget();
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
							
							List<DynamoPlayer> sourceList1 = game.getGameTeeTimeList2().getSource();
							for (int j = 0; j < sourceList1.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList1.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList1.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList1 = game.getGameTeeTimeList2().getTarget();
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
							
							List<DynamoPlayer> sourceList2 = game.getGameTeeTimeList3().getSource();
							for (int j = 0; j < sourceList2.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList2.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList2.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList2 = game.getGameTeeTimeList3().getTarget();
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
							
							List<DynamoPlayer> sourceList3 = game.getGameTeeTimeList4().getSource();
							for (int j = 0; j < sourceList3.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList3.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList3.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList3 = game.getGameTeeTimeList4().getTarget();
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
			
							List<DynamoPlayer> sourceList4 = game.getGameTeeTimeList5().getSource();
							for (int j = 0; j < sourceList4.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList4.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList4.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList4 = game.getGameTeeTimeList5().getTarget();
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
							
							List<DynamoPlayer> sourceList5 = game.getGameTeeTimeList6().getSource();
							for (int j = 0; j < sourceList5.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList5.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList5.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList5 = game.getGameTeeTimeList6().getTarget();
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
							
							List<DynamoPlayer> sourceList6 = game.getGameTeeTimeList7().getSource();
							for (int j = 0; j < sourceList6.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList6.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList6.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList6 = game.getGameTeeTimeList7().getTarget();
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
							
							List<DynamoPlayer> sourceList7 = game.getGameTeeTimeList8().getSource();
							for (int j = 0; j < sourceList7.size(); j++) 
							{
								DynamoPlayer tempPlayer = sourceList7.get(j);
								if (tempPlayer.getPlayerID().equalsIgnoreCase(playerMoved.getPlayerID()))
								{
									sourceList7.remove(j);
									break;
								}
							}
							List<DynamoPlayer> targetList7 = game.getGameTeeTimeList8().getTarget();
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
     
    public void onReorder() {
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "List Reordered", null));
    }
	
	public DynamoPlayer getSelectedPlayer() {
		return selectedPlayer;
	}

	public void setSelectedPlayer(DynamoPlayer selectedPlayer) {
		this.selectedPlayer = selectedPlayer;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public int getTotalSelectedPlayers() 
	{
		int tempInt = 0;
		
		if (game.getSelectedPlayersList() != null && game.getSelectedPlayersList().size() > 0)
		{
			tempInt = game.getSelectedPlayersList().size();
		}
		return tempInt;
	}
	
	public boolean isResetPassword() {
		return resetPassword;
	}

	public void setResetPassword(boolean resetPassword) {
		this.resetPassword = resetPassword;
	}
	
	public boolean isRenderInquiry() {
		return renderInquiry;
	}

	public void setRenderInquiry(boolean renderInquiry) {
		this.renderInquiry = renderInquiry;
	}

	public boolean isRenderAddUpdate() {
		return renderAddUpdate;
	}

	public void setRenderAddUpdate(boolean renderAddUpdate) {
		this.renderAddUpdate = renderAddUpdate;
	}

	public DynamoPlayerTeePreference getSelectedPlayerTeePreference() {
		return selectedPlayerTeePreference;
	}

	public void setSelectedPlayerTeePreference(DynamoPlayerTeePreference selectedPlayerTeePreference) {
		this.selectedPlayerTeePreference = selectedPlayerTeePreference;
	}

}
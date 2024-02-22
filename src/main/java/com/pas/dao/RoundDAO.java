package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.Game;
import com.pas.beans.Round;
import com.pas.beans.Score;
import com.pas.dynamodb.DateToStringConverter;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoRound;
import com.pas.util.Utils;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class RoundDAO implements Serializable
{
	static DateTimeFormatter etFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static ZoneId etZoneId = ZoneId.of(Utils.MY_TIME_ZONE);
  
	private static final long serialVersionUID = 1L;
	
	private static Logger logger = LogManager.getLogger(RoundDAO.class);
	
	private Map<String, Round> fullRoundsMap = new HashMap<>();	
	private List<Round> fullRoundsList = new ArrayList<Round>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoRound> roundsTable;
	private static final String AWS_TABLE_NAME = "rounds";
	
	public RoundDAO(DynamoClients dynamoClients2) 
	{
	   try 
	   {
	       dynamoClients = dynamoClients2;
	       roundsTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoRound.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing TeeTimeDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
	
	public List<String> getGameParticipantsFromDB(Game selectedGame)
    {	
		List<Round> roundList = getRoundsForGame(selectedGame);
		List<String> participantsList = new ArrayList<>();
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone(Utils.MY_TIME_ZONE);
		signupSDF.setTimeZone(etTimeZone);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			String signupDateTime = signupSDF.format(rd.getSignupDateTime());
			participantsList.add(rd.getPlayerName() + " (signed up: " + signupDateTime + ")");
		}
		
		return participantsList;
    }
	
	public void readAllRoundsFromDB()
    {
		String oneMonthAgo = Utils.getOneMonthAgoDate();
		Map<String, AttributeValue> av = Map.of(":min_value", AttributeValue.fromS(oneMonthAgo));
		
		ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .consistentRead(true)
                .filterExpression(Expression.builder()
                        .expression("signupDateTime >= :min_value")
                        .expressionValues(av)
                        .build())
                .build();
		
		Iterator<DynamoRound> results = roundsTable.scan(request).items().iterator();
	  	
		while (results.hasNext()) 
        {
			DynamoRound dynamoRound = results.next();
          	
			Round round = new Round();

			round.setRoundID(dynamoRound.getRoundID());
			round.setGameID(dynamoRound.getGameID());
			round.setPlayerID(dynamoRound.getPlayerID());
			round.setTeamNumber(dynamoRound.getTeamNumber());
			round.setTeeTimeID(dynamoRound.getTeeTimeID());
			round.setPlayerName(dynamoRound.getPlayerName());
			round.setRoundHandicap(dynamoRound.getRoundHandicap());
			round.setPlayerHandicapIndex(dynamoRound.getPlayerHandicapIndex());
			round.setCourseTeeID(dynamoRound.getCourseTeeID());
			round.setCourseTeeColor(dynamoRound.getCourseTeeColor());
			round.setRoundHandicapDifferential(dynamoRound.getRoundHandicapDifferential());
			
			String signupdatetime = dynamoRound.getSignupDateTime();
			DateToStringConverter dtsc = new DateToStringConverter();
			Date sdate = dtsc.unconvert(signupdatetime);
			round.setSignupDateTime(sdate);
			
			round.setHole1Score(dynamoRound.getHole1Score());
			round.setHole2Score(dynamoRound.getHole2Score());
			round.setHole3Score(dynamoRound.getHole3Score());
			round.setHole4Score(dynamoRound.getHole4Score());
			round.setHole5Score(dynamoRound.getHole5Score());
			round.setHole6Score(dynamoRound.getHole6Score());
			round.setHole7Score(dynamoRound.getHole7Score());
			round.setHole8Score(dynamoRound.getHole8Score());
			round.setHole9Score(dynamoRound.getHole9Score());
			round.setFront9Total(dynamoRound.getFront9Total());
			round.setHole10Score(dynamoRound.getHole10Score());
			round.setHole11Score(dynamoRound.getHole11Score());
			round.setHole12Score(dynamoRound.getHole12Score());
			round.setHole13Score(dynamoRound.getHole13Score());
			round.setHole14Score(dynamoRound.getHole14Score());
			round.setHole15Score(dynamoRound.getHole15Score());
			round.setHole16Score(dynamoRound.getHole16Score());
			round.setHole17Score(dynamoRound.getHole17Score());
			round.setHole18Score(dynamoRound.getHole18Score());
			round.setBack9Total(dynamoRound.getBack9Total());
			round.setTotalScore(dynamoRound.getTotalScore());
			round.setTotalToPar(dynamoRound.getTotalToPar());
			round.setNetScore(dynamoRound.getNetScore());
			
			round.setRoundbyHoleScores(setHoleScoresList(round));
			
            this.getFullRoundsList().add(round);			
        }	
		
		logger.info("LoggedDBOperation: function-inquiry; table:round; rows:" + this.getFullRoundsList().size());
		
		this.setFullRoundsMap(this.getFullRoundsList().stream().collect(Collectors.toMap(Round::getRoundID, rd -> rd)));		
    }
	
	public List<Round> getRoundsForGame(Game selectedGame)
    {
		List<Round> roundsByGameList = new ArrayList<>();
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(selectedGame.getGameID()))
			{
				roundsByGameList.add(rd);
			}
		}
		
		Collections.sort(roundsByGameList, new Comparator<Round>() 
		{
		   public int compare(Round o1, Round o2) 
		   {
		      return o1.getSignupDateTime().compareTo(o2.getSignupDateTime());
		   }
		});
		
	   	return roundsByGameList;
    }
	
	public List<Round> readPlayGroupRoundsFromDB(Game selectedGame, String teeTimeID)
    {
		List<Round> roundsList = new ArrayList<>();
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(selectedGame.getGameID()) && rd.getTeeTimeID().equalsIgnoreCase(teeTimeID))
			{
				roundsList.add(rd);
			}
		}
		
    	return roundsList;
    }
	
	public Integer countRoundsForGameFromDB(Game selectedGame)
    {
		int count = 0; 
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(selectedGame.getGameID()))
			{
				count++;
			}
		}
		
    	return count;
    }
	
	public Round getRoundByRoundID(String roundID)
    {
		Round round = this.getFullRoundsMap().get(roundID);
	 	return round;
    }
	
	public Round getRoundByGameandPlayer(String gameID, String playerID)
    {
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(gameID) && rd.getPlayerID().equalsIgnoreCase(playerID))
			{
				return rd;
			}
		}
		
		return null;
		    	
    }
	
	public void deleteRoundFromDB(String roundID)
    {
		Key key = Key.builder().partitionValue(roundID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		roundsTable.deleteItem(deleteItemEnhancedRequest);
	
		logger.info("LoggedDBOperation: function-delete; table:round; rows:1");
		
		Round rd = new Round();
		rd.setRoundID(roundID);
		
		refreshListsAndMaps("delete", rd);		
		
		logger.info(getTempUserName() + " delete round table complete");
    }
	
	public void deleteRoundsFromDB(String gameID)
    {
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(gameID))
			{
				deleteRoundFromDB(rd.getRoundID());
				this.getFullRoundsMap().remove(rd.getRoundID());
			}
		}
		
		refreshListsAndMaps("special", null);	
		
		logger.info(getTempUserName() + " delete rounds table complete");		
    }

	private DynamoRound dynamoUpsert(Round round) throws Exception 
	{
		DynamoRound dynamoRound = new DynamoRound();
        
		if (round.getRoundID() == null)
		{
			dynamoRound.setRoundID(UUID.randomUUID().toString());
			dynamoRound.setSignupDateTime(DateToStringConverter.convertDateToDynamoStringFormat(new Date()));
		}
		else
		{
			dynamoRound.setRoundID(round.getRoundID());
			dynamoRound.setSignupDateTime(DateToStringConverter.convertDateToDynamoStringFormat(round.getSignupDateTime()));
		}
		
		dynamoRound.setGameID(round.getGameID());
		dynamoRound.setPlayerID(round.getPlayerID());
		dynamoRound.setTeamNumber(round.getTeamNumber());
		dynamoRound.setTeeTimeID(round.getTeeTimeID());
		dynamoRound.setPlayerName(round.getPlayerName());
		dynamoRound.setRoundHandicap(round.getRoundHandicap());
		dynamoRound.setPlayerHandicapIndex(round.getPlayerHandicapIndex());
		dynamoRound.setCourseTeeID(round.getCourseTeeID());
		dynamoRound.setCourseTeeColor(round.getCourseTeeColor());
		dynamoRound.setRoundHandicapDifferential(round.getRoundHandicapDifferential());

		dynamoRound.setHole1Score(round.getHole1Score());
		dynamoRound.setHole2Score(round.getHole2Score());
		dynamoRound.setHole3Score(round.getHole3Score());
		dynamoRound.setHole4Score(round.getHole4Score());
		dynamoRound.setHole5Score(round.getHole5Score());
		dynamoRound.setHole6Score(round.getHole6Score());
		dynamoRound.setHole7Score(round.getHole7Score());
		dynamoRound.setHole8Score(round.getHole8Score());
		dynamoRound.setHole9Score(round.getHole9Score());
		dynamoRound.setFront9Total(round.getFront9Total());
		dynamoRound.setHole10Score(round.getHole10Score());
		dynamoRound.setHole11Score(round.getHole11Score());
		dynamoRound.setHole12Score(round.getHole12Score());
		dynamoRound.setHole13Score(round.getHole13Score());
		dynamoRound.setHole14Score(round.getHole14Score());
		dynamoRound.setHole15Score(round.getHole15Score());
		dynamoRound.setHole16Score(round.getHole16Score());
		dynamoRound.setHole17Score(round.getHole17Score());
		dynamoRound.setHole18Score(round.getHole18Score());
		dynamoRound.setBack9Total(round.getBack9Total());
		dynamoRound.setTotalScore(round.getTotalScore());
		dynamoRound.setTotalToPar(round.getTotalToPar());
		dynamoRound.setNetScore(round.getNetScore());
		
		
		PutItemEnhancedRequest<DynamoRound> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoRound.class).item(dynamoRound).build();
		roundsTable.putItem(putItemEnhancedRequest);
		
		return dynamoRound;
	}
	
	public String addRound(Round round) throws Exception
	{
		DynamoRound dr = dynamoUpsert(round);
		
		round.setRoundID(dr.getRoundID());
		DateToStringConverter dsc = new DateToStringConverter();
		round.setSignupDateTime(dsc.unconvert(dr.getSignupDateTime()));
		
		logger.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		refreshListsAndMaps("add", round);	
		
		logger.info(getTempUserName() + " insert round table complete");
		
		return round.getRoundID();
	}
	
	public void updateRound(Round round) throws Exception
	{
		dynamoUpsert(round);
		
		logger.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		refreshListsAndMaps("update", round);	
		
		logger.debug(getTempUserName() + " update round table complete.  Round id updated: " + round.getRoundID());
		
	}

	public void updateRoundHandicap(Game selectedGame, String playerID, BigDecimal handicap) throws Exception 
	{
		
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(selectedGame.getGameID()) && rd.getPlayerID().equalsIgnoreCase(playerID))
			{
				rd.setRoundHandicap(handicap);
				dynamoUpsert(rd);
				logger.info("LoggedDBOperation: function-update; table:round; rows:1");
				this.getFullRoundsMap().remove(rd.getRoundID());
				this.getFullRoundsMap().put(rd.getRoundID(), rd);
				this.getFullRoundsList().clear();
				Collection<Round> values = this.getFullRoundsMap().values();
				this.setFullRoundsList(new ArrayList<>(values));
				break;
			}
		}
		
		refreshListsAndMaps("special", null);
		
		logger.debug(getTempUserName() + " update player handicap for playerID: " + playerID + " to: " + handicap + " on round table complete");		
	}	
	
	public void updateRoundTeamNumber(Game selectedGame, String playerID, int teamNumber) throws Exception 
	{
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID().equalsIgnoreCase(selectedGame.getGameID()) && rd.getPlayerID().equalsIgnoreCase(playerID))
			{
				rd.setTeamNumber(teamNumber);
				dynamoUpsert(rd);
				logger.info("LoggedDBOperation: function-update; table:round; rows:1");
				this.getFullRoundsMap().remove(rd.getRoundID());
				this.getFullRoundsMap().put(rd.getRoundID(), rd);
				this.getFullRoundsList().clear();
				Collection<Round> values = this.getFullRoundsMap().values();
				this.setFullRoundsList(new ArrayList<>(values));
				break;
			}
		}
		
		refreshListsAndMaps("special", null);
		
		logger.debug(getTempUserName() + " update team number for playerID: " + playerID + " to: " + teamNumber + " on round table complete");		
	}	
	
	private List<Score> setHoleScoresList(Round rd)
	{
		List<Score> scoreList = new ArrayList<>();
		
		for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
		{
			Score score = new Score();
			score.setHoleNumber(holeNumber);
			Integer scoreInteger = null;
			switch (holeNumber) 
			{
				case 1:						
					scoreInteger = rd.getHole1Score();									
					break;					
				case 2:					
					scoreInteger = rd.getHole2Score();								
					break;					
				case 3:	
					scoreInteger = rd.getHole3Score();
					break;					
				case 4:	
					scoreInteger = rd.getHole4Score();
					break;					
				case 5:						
					scoreInteger = rd.getHole5Score();
					break;					
				case 6:						
					scoreInteger = rd.getHole6Score();
					break;
				case 7:						
					scoreInteger = rd.getHole7Score();
					break;					
				case 8:	
					scoreInteger = rd.getHole8Score();
					break;					
				case 9:	
					scoreInteger = rd.getHole9Score();
					break;
					
				//back 9
				case 10:	
					scoreInteger = rd.getHole10Score();
					break;					
				case 11:	
					scoreInteger = rd.getHole11Score();
					break;					
				case 12:	
					scoreInteger = rd.getHole12Score();
					break;					
				case 13:	
					scoreInteger = rd.getHole13Score();
					break;					
				case 14:	
					scoreInteger = rd.getHole14Score();
					break;					
				case 15:	
					scoreInteger = rd.getHole15Score();
					break;					
				case 16:	
					scoreInteger = rd.getHole16Score();
					break;					
				case 17:	
					scoreInteger = rd.getHole17Score();
					break;					
				case 18:	
					scoreInteger = rd.getHole18Score();
					break;
					
				default:
					break;
			}
			
			if (scoreInteger != null)
			{
				score.setScore(scoreInteger);
				scoreList.add(score);	
			}			
		}
		
		return scoreList;
	}
	
	private void refreshListsAndMaps(String function, Round round)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getFullRoundsMap().remove(round.getRoundID());	
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getFullRoundsMap().put(round.getRoundID(), round);	
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullRoundsMap().remove(round.getRoundID());	
			this.getFullRoundsMap().put(round.getRoundID(), round);	
		}
		
		this.getFullRoundsList().clear();
		Collection<Round> values = this.getFullRoundsMap().values();
		this.setFullRoundsList(new ArrayList<>(values));
		
		Collections.sort(this.getFullRoundsList(), new Comparator<Round>() 
		{
		   public int compare(Round o1, Round o2) 
		   {			   
		      return o2.getSignupDateTime().compareTo(o1.getSignupDateTime());
		   }
		});
		
	}
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}

	public Map<String, Round> getFullRoundsMap() {
		return fullRoundsMap;
	}

	public void setFullRoundsMap(Map<String, Round> fullRoundsMap) {
		this.fullRoundsMap = fullRoundsMap;
	}

	public List<Round> getFullRoundsList() {
		return fullRoundsList;
	}

	public void setFullRoundsList(List<Round> fullRoundsList) {
		this.fullRoundsList = fullRoundsList;
	}
}

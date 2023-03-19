package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.Round;
import com.pas.util.Utils;

@Repository
public class RoundDAO  extends JdbcDaoSupport  implements Serializable
{
	static DateTimeFormatter etFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static ZoneId etZoneId = ZoneId.of("America/New_York");
  
	private static final long serialVersionUID = 1L;
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	private static Logger log = LogManager.getLogger(RoundDAO.class);
	
	private Map<Integer,Round> fullRoundsMap = new HashMap<Integer,Round>();	
	private List<Round> fullRoundsList = new ArrayList<Round>();
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       setDataSource(dataSource);
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing DAO: {}" +  ex.getStackTrace());
	   }
	}

	@Autowired
    public RoundDAO(DataSource dataSource) 
	{
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
	
	
	public List<String> getGameParticipantsFromDB(Game selectedGame)
    {		
		String sql = "SELECT concat(p.firstName, ' ', p.lastName) as playerName, r.dSignUpdatetime from round r inner join player p "
				+ "where r.idplayer = p.idplayer and r.idgame = :idgame order by r.dSignupDateTime";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", selectedGame.getGameID());
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone("America/New_York");
		signupSDF.setTimeZone(etTimeZone);
		
		return namedParameterJdbcTemplate.query(sql, param, new ResultSetExtractor<List<String>>() 
		{	   
			@Override
		    public List<String> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<String> participantsList = new ArrayList<>();
				
				while (rs.next()) 
				{
					if (rs.getTimestamp("dSignUpdatetime") == null)
					{
						
					}
					else
					{
						String signupDateTime = signupSDF.format(rs.getTimestamp("dSignUpdatetime"));
						participantsList.add(rs.getString("playerName") + " (signed up: " + signupDateTime + ")");
					}
				}
				
				log.info("LoggedDBOperation: function-inquiry; table:game Participants; rows:" + participantsList.size());
				
				return participantsList;
		    }
		});	
		
    }
	
	public void readAllRoundsFromDB()
    {
		String sql = "select * from round where dSignUpdatetime > :dSignUpdatetime order by dSignUpdatetime desc";	
		SqlParameterSource param = new MapSqlParameterSource("dSignUpdatetime", Utils.getOneMonthAgoDate());
		this.setFullRoundsList(namedParameterJdbcTemplate.query(sql, param, new RoundRowMapper())); 
	
		log.info("LoggedDBOperation: function-inquiry; table:round; rows:" + this.getFullRoundsList().size());
		
		this.setFullRoundsMap(this.getFullRoundsList().stream().collect(Collectors.toMap(Round::getRoundID, rd -> rd)));		
    }
	
	public List<Round> getRoundsForGame(Game selectedGame)
    {
		List<Round> roundsByGameList = new ArrayList<>();
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == selectedGame.getGameID())
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
	
	public List<Round> readPlayGroupRoundsFromDB(Game selectedGame, Integer teeTimeID)
    {
		List<Round> roundsList = new ArrayList<>();
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == selectedGame.getGameID() && rd.getTeeTimeID() == teeTimeID)
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
			
			if (rd.getGameID() == selectedGame.getGameID())
			{
				count++;
			}
		}
		
    	return count;
    }
	
	public Round getRoundByRoundID(Integer roundID)
    {
		Round round = this.getFullRoundsMap().get(roundID);
	 	return round;
    }
	
	public Round getRoundByGameandPlayer(Integer gameID, Integer playerID)
    {
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == gameID && rd.getPlayerID() == playerID)
			{
				return rd;
			}
		}
		
		return null;
		    	
    }
	
	public void deleteRoundFromDB(Integer roundID)
    {
		String deleteStr = "delete from round where idround = ?";
		jdbcTemplate.update(deleteStr, roundID);	
		log.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		Round rd = new Round();
		rd.setRoundID(roundID);
		
		refreshListsAndMaps("delete", rd);		
		
		log.info(getTempUserName() + " delete round table complete");
    }
	
	public void deleteRoundsFromDB(Integer gameID)
    {
		String deleteStr = "delete from round where idgame = ?";
		int updatedRows = jdbcTemplate.update(deleteStr, gameID);	
		log.info("LoggedDBOperation: function-update; table:round; rows:" + updatedRows);
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == gameID)
			{
				this.getFullRoundsMap().remove(rd.getRoundID());
			}
		}
		
		refreshListsAndMaps("special", null);	
		
		log.info(getTempUserName() + " delete rounds table complete");		
    }

	public int addRound(Round round)
	{
		KeyHolder keyHolder = new GeneratedKeyHolder();
		
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				String insertStr = "INSERT INTO round (idgame, idplayer, hole1Score, hole2Score, hole3Score, hole4Score, hole5Score, hole6Score, ";
				insertStr = insertStr + "hole7Score, hole8Score, hole9Score, hole10Score, hole11Score, hole12Score, ";
				insertStr = insertStr + "hole13Score, hole14Score, hole15Score, hole16Score, hole17Score, hole18Score, ";
				insertStr = insertStr + "front9Score, back9Score, totalScore, totalToPar, netScore, teamNumber, roundHandicap, idTeeTimes, dSignUpdatetime, idCourseTee)";
				insertStr = insertStr + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";		
		
				PreparedStatement psmt = connection.prepareStatement(insertStr, Statement.RETURN_GENERATED_KEYS);
					
				psmt.setInt(1, round.getGameID());
				psmt.setInt(2, round.getPlayerID());
				
				if (round.getHole1Score() == null)
				{
					psmt.setNull(3, Types.INTEGER);
				}
				else
				{
					psmt.setInt(3, round.getHole1Score());
				}
				
				if (round.getHole2Score() == null)
				{
					psmt.setNull(4, Types.INTEGER);
				}
				else
				{
					psmt.setInt(4, round.getHole2Score());
				}
				
				if (round.getHole3Score() == null)
				{
					psmt.setNull(5, Types.INTEGER);
				}
				else
				{
					psmt.setInt(5, round.getHole3Score());
				}
				
				if (round.getHole4Score() == null)
				{
					psmt.setNull(6, Types.INTEGER);
				}
				else
				{
					psmt.setInt(6, round.getHole4Score());
				}
				
				if (round.getHole5Score() == null)
				{
					psmt.setNull(7, Types.INTEGER);
				}
				else
				{
					psmt.setInt(7, round.getHole5Score());
				}
				
				if (round.getHole6Score() == null)
				{
					psmt.setNull(8, Types.INTEGER);
				}
				else
				{
					psmt.setInt(8, round.getHole6Score());
				}
				
				if (round.getHole7Score() == null)
				{
					psmt.setNull(9, Types.INTEGER);
				}
				else
				{
					psmt.setInt(9, round.getHole7Score());
				}
				
				if (round.getHole8Score() == null)
				{
					psmt.setNull(10, Types.INTEGER);
				}
				else
				{
					psmt.setInt(10, round.getHole8Score());
				}
				
				if (round.getHole9Score() == null)
				{
					psmt.setNull(11, Types.INTEGER);
				}
				else
				{
					psmt.setInt(11, round.getHole9Score());
				}
				
				if (round.getHole10Score() == null)
				{
					psmt.setNull(12, Types.INTEGER);
				}
				else
				{
					psmt.setInt(12, round.getHole10Score());
				}
				
				if (round.getHole11Score() == null)
				{
					psmt.setNull(13, Types.INTEGER);
				}
				else
				{
					psmt.setInt(13, round.getHole11Score());
				}
				
				if (round.getHole12Score() == null)
				{
					psmt.setNull(14, Types.INTEGER);
				}
				else
				{
					psmt.setInt(14, round.getHole12Score());
				}
				
				if (round.getHole13Score() == null)
				{
					psmt.setNull(15, Types.INTEGER);
				}
				else
				{
					psmt.setInt(15, round.getHole13Score());
				}
				
				if (round.getHole14Score() == null)
				{
					psmt.setNull(16, Types.INTEGER);
				}
				else
				{
					psmt.setInt(16, round.getHole14Score());
				}
				
				if (round.getHole15Score() == null)
				{
					psmt.setNull(17, Types.INTEGER);
				}
				else
				{
					psmt.setInt(17, round.getHole15Score());
				}
				
				if (round.getHole16Score() == null)
				{
					psmt.setNull(18, Types.INTEGER);
				}
				else
				{
					psmt.setInt(18, round.getHole16Score());
				}
				
				if (round.getHole17Score() == null)
				{
					psmt.setNull(19, Types.INTEGER);
				}
				else
				{
					psmt.setInt(19, round.getHole17Score());
				}
				
				if (round.getHole18Score() == null)
				{
					psmt.setNull(20, Types.INTEGER);
				}
				else
				{
					psmt.setInt(20, round.getHole18Score());
				}
				
				if (round.getFront9Total() == null)
				{
					psmt.setNull(21, Types.INTEGER);
				}
				else
				{
					psmt.setInt(21, round.getFront9Total());
				}
				
				if (round.getBack9Total() == null)
				{
					psmt.setNull(22, Types.INTEGER);
				}
				else
				{
					psmt.setInt(22, round.getBack9Total());
				}
				
				if (round.getTotalScore() == null)
				{
					psmt.setNull(23, Types.INTEGER);
				}
				else
				{
					psmt.setInt(23, round.getTotalScore());
				}
					
				psmt.setString(24, round.getTotalToPar());			
				psmt.setBigDecimal(25, round.getNetScore());
				psmt.setInt(26, round.getTeamNumber());
				psmt.setBigDecimal(27, round.getRoundHandicap());
				if (round.getTeeTimeID() < 1)
				{
					psmt.setNull(28, Types.INTEGER);
				}
				else
				{
					psmt.setInt(28, round.getTeeTimeID());
				}
			
				
				if (round.getSignupDateTime() == null)
				{					
					LocalDateTime currentDateTime = LocalDateTime.now();
					Date rightNow = new Date();
					round.setSignupDateTime(rightNow);
			        ZonedDateTime currentETTime = currentDateTime.atZone(etZoneId); //ET Time
			    	String signupDateTime = etFormat.format(currentETTime);			    	
					psmt.setString(29, signupDateTime);
				}
				else
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");					
					String signupDateTime = format.format(round.getSignupDateTime());
					psmt.setString(29, signupDateTime);
				}
				
				if (round.getCourseTeeID() == null)
				{
					psmt.setNull(30, Types.INTEGER);
				}
				else
				{
					psmt.setInt(30, round.getCourseTeeID());
				}
				return psmt;
			}
		}, keyHolder);
		
		log.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		round.setRoundID(keyHolder.getKey().intValue());
		
		refreshListsAndMaps("add", round);	
		
		log.info(getTempUserName() + " insert round table complete");
		
		return round.getRoundID();
	}
	
	public void updateRound(Round round)
	{
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				String updateStr = " UPDATE round SET idgame = ?, idplayer = ?, hole1Score = ?, hole2Score = ?, hole3Score = ?, hole4Score = ?," ;
				updateStr = updateStr + " hole5Score = ?, hole6Score = ?, hole7Score = ?, hole8Score = ?, hole9Score = ?, hole10Score = ?," ;
				updateStr = updateStr + " hole11Score = ?, hole12Score = ?, hole13Score = ?, hole14Score = ?," ;
				updateStr = updateStr + " hole15Score = ?, hole16Score = ?, hole17Score = ?, hole18Score = ?," ;
				updateStr = updateStr + " front9Score = ?, back9Score = ?, totalScore = ?, totalToPar = ?, netScore = ?, teamNumber = ?, roundHandicap = ?, idTeeTimes = ?, idCourseTee = ?";
				updateStr = updateStr + " WHERE idround = ?";				
		
				PreparedStatement psmt = connection.prepareStatement(updateStr, new String[] {});
				
				psmt.setInt(1, round.getGameID());
				psmt.setInt(2, round.getPlayerID());
				
				if (round.getHole1Score() == null)
				{
					psmt.setNull(3, Types.INTEGER);
				}
				else
				{
					psmt.setInt(3, round.getHole1Score());
				}
				
				if (round.getHole2Score() == null)
				{
					psmt.setNull(4, Types.INTEGER);
				}
				else
				{
					psmt.setInt(4, round.getHole2Score());
				}
				
				if (round.getHole3Score() == null)
				{
					psmt.setNull(5, Types.INTEGER);
				}
				else
				{
					psmt.setInt(5, round.getHole3Score());
				}
				
				if (round.getHole4Score() == null)
				{
					psmt.setNull(6, Types.INTEGER);
				}
				else
				{
					psmt.setInt(6, round.getHole4Score());
				}
				
				if (round.getHole5Score() == null)
				{
					psmt.setNull(7, Types.INTEGER);
				}
				else
				{
					psmt.setInt(7, round.getHole5Score());
				}
				
				if (round.getHole6Score() == null)
				{
					psmt.setNull(8, Types.INTEGER);
				}
				else
				{
					psmt.setInt(8, round.getHole6Score());
				}
				
				if (round.getHole7Score() == null)
				{
					psmt.setNull(9, Types.INTEGER);
				}
				else
				{
					psmt.setInt(9, round.getHole7Score());
				}
				
				if (round.getHole8Score() == null)
				{
					psmt.setNull(10, Types.INTEGER);
				}
				else
				{
					psmt.setInt(10, round.getHole8Score());
				}
				
				if (round.getHole9Score() == null)
				{
					psmt.setNull(11, Types.INTEGER);
				}
				else
				{
					psmt.setInt(11, round.getHole9Score());
				}
				
				if (round.getHole10Score() == null)
				{
					psmt.setNull(12, Types.INTEGER);
				}
				else
				{
					psmt.setInt(12, round.getHole10Score());
				}
				
				if (round.getHole11Score() == null)
				{
					psmt.setNull(13, Types.INTEGER);
				}
				else
				{
					psmt.setInt(13, round.getHole11Score());
				}
				
				if (round.getHole12Score() == null)
				{
					psmt.setNull(14, Types.INTEGER);
				}
				else
				{
					psmt.setInt(14, round.getHole12Score());
				}
				
				if (round.getHole13Score() == null)
				{
					psmt.setNull(15, Types.INTEGER);
				}
				else
				{
					psmt.setInt(15, round.getHole13Score());
				}
				
				if (round.getHole14Score() == null)
				{
					psmt.setNull(16, Types.INTEGER);
				}
				else
				{
					psmt.setInt(16, round.getHole14Score());
				}
				
				if (round.getHole15Score() == null)
				{
					psmt.setNull(17, Types.INTEGER);
				}
				else
				{
					psmt.setInt(17, round.getHole15Score());
				}
				
				if (round.getHole16Score() == null)
				{
					psmt.setNull(18, Types.INTEGER);
				}
				else
				{
					psmt.setInt(18, round.getHole16Score());
				}
				
				if (round.getHole17Score() == null)
				{
					psmt.setNull(19, Types.INTEGER);
				}
				else
				{
					psmt.setInt(19, round.getHole17Score());
				}
				
				if (round.getHole18Score() == null)
				{
					psmt.setNull(20, Types.INTEGER);
				}
				else
				{
					psmt.setInt(20, round.getHole18Score());
				}
				
				if (round.getFront9Total() == null)
				{
					psmt.setNull(21, Types.INTEGER);
				}
				else
				{
					psmt.setInt(21, round.getFront9Total());
				}
				
				if (round.getBack9Total() == null)
				{
					psmt.setNull(22, Types.INTEGER);
				}
				else
				{
					psmt.setInt(22, round.getBack9Total());
				}
				
				if (round.getTotalScore() == null)
				{
					psmt.setNull(23, Types.INTEGER);
				}
				else
				{
					psmt.setInt(23, round.getTotalScore());
				}
					
				psmt.setString(24, round.getTotalToPar());			
				psmt.setBigDecimal(25, round.getNetScore());			
				psmt.setInt(26, round.getTeamNumber());
				psmt.setBigDecimal(27, round.getRoundHandicap());	
				
				if (round.getTeeTimeID() < 1)
				{
					psmt.setNull(28, Types.INTEGER);
				}
				else
				{
					psmt.setInt(28, round.getTeeTimeID());
				}
				
				if (round.getCourseTeeID() == null)
				{
					psmt.setNull(29, Types.INTEGER);
				}
				else
				{
					psmt.setInt(29, round.getCourseTeeID());
				}
				
				psmt.setInt(30, round.getRoundID());
				
				return psmt;
			}
		});
		
		log.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		refreshListsAndMaps("update", round);	
		
		log.debug(getTempUserName() + " update round table complete.  Round id updated: " + round.getRoundID());
		
	}

	public void updateRoundHandicap(Game selectedGame, int playerID, BigDecimal handicap) 
	{
		String updateStr = " UPDATE round SET roundHandicap = ? WHERE idgame = ? AND idplayer = ?";	
		jdbcTemplate.update(updateStr, handicap, selectedGame.getGameID(), playerID);	
		log.info("LoggedDBOperation: function-update; table:round; rows:1");
		
		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == selectedGame.getGameID() && rd.getPlayerID() == playerID)
			{
				rd.setRoundHandicap(handicap);
				this.getFullRoundsMap().remove(rd.getRoundID());
				this.getFullRoundsMap().put(rd.getRoundID(), rd);
				this.getFullRoundsList().clear();
				Collection<Round> values = this.getFullRoundsMap().values();
				this.setFullRoundsList(new ArrayList<>(values));
				break;
			}
		}
		
		refreshListsAndMaps("special", null);
		
		log.debug(getTempUserName() + " update player handicap for playerID: " + playerID + " to: " + handicap + " on round table complete");		
	}	
	
	public void updateRoundTeamNumber(Game selectedGame, int playerID, int teamNumber) 
	{
		String updateStr = " UPDATE round SET teamNumber = ? WHERE idgame = ? AND idplayer = ?";	
		jdbcTemplate.update(updateStr, teamNumber, selectedGame.getGameID(), playerID);		
		log.info("LoggedDBOperation: function-update; table:round; rows:1");
		

		for (int i = 0; i < this.getFullRoundsList().size(); i++) 
		{
			Round rd = this.getFullRoundsList().get(i);
			
			if (rd.getGameID() == selectedGame.getGameID() && rd.getPlayerID() == playerID)
			{
				rd.setTeamNumber(teamNumber);
				this.getFullRoundsMap().remove(rd.getRoundID());
				this.getFullRoundsMap().put(rd.getRoundID(), rd);
				this.getFullRoundsList().clear();
				Collection<Round> values = this.getFullRoundsMap().values();
				this.setFullRoundsList(new ArrayList<>(values));
				break;
			}
		}
		
		refreshListsAndMaps("special", null);
		
		log.debug(getTempUserName() + " update team number for playerID: " + playerID + " to: " + teamNumber + " on round table complete");		
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

	public Map<Integer, Round> getFullRoundsMap() {
		return fullRoundsMap;
	}

	public void setFullRoundsMap(Map<Integer, Round> fullRoundsMap) {
		this.fullRoundsMap = fullRoundsMap;
	}

	public List<Round> getFullRoundsList() {
		return fullRoundsList;
	}

	public void setFullRoundsList(List<Round> fullRoundsList) {
		this.fullRoundsList = fullRoundsList;
	}
}

package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

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
				return participantsList;
		    }
		});
		
    }
	public List<Round> readRoundsFromDB(Game selectedGame)
    {
		String sql = "select * from round where idgame = :idgame order by dSignUpdatetime";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", selectedGame.getGameID());		 
		List<Round> roundsList = namedParameterJdbcTemplate.query(sql, param, new RoundRowMapper());    
	   	return roundsList;
    }
	
	public List<Round> readPlayGroupRoundsFromDB(Game selectedGame, Integer teeTimeID)
    {
		String sql = "select * from round where idgame = :idgame and idteeTimes = :teeTimeID";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", selectedGame.getGameID()).addValue("teeTimeID", teeTimeID);		 
		List<Round> roundsList = namedParameterJdbcTemplate.query(sql, param, new RoundRowMapper()); 		
    	return roundsList;
    }
	
	public Integer countRoundsForGameFromDB(Game selectedGame)
    {
		String sql = "select count(*) from round where idgame = ?";					
		int count = jdbcTemplate.queryForObject(sql, new Object[] {selectedGame.getGameID()}, Integer.class);    	
    	return count;
    }
	
	public Round readRoundFromDB(Integer roundID)
    {
		String sql = "select * from round where idround = :idround";		 
		SqlParameterSource param = new MapSqlParameterSource("idround", roundID);		 
		Round round = namedParameterJdbcTemplate.queryForObject(sql, param, new RoundRowMapper()); 		
    	return round;
    }
	
	public Round readRoundFromDBByGameandPlayer(Integer gameID, Integer playerID)
    {
		String sql = "select * from round where idgame = :idgame  and idplayer = :idPlayer";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", gameID).addValue("idPlayer", playerID);		
		List<Round> roundList = namedParameterJdbcTemplate.query(sql, param, new RoundRowMapper()); 
		
		if (roundList == null || roundList.size() == 0)
		{
			return null;
		}
		else
		{
			return roundList.get(0);
		}
    	
    }
	
	public void deleteRoundFromDB(Integer roundID)
    {
		String deleteStr = "delete from round where idround = ?";
		jdbcTemplate.update(deleteStr, roundID);	
		log.info(getTempUserName() + " delete round table complete");
    }
	
	public void deleteRoundsFromDB(Integer gameID)
    {
		String deleteStr = "delete from round where idgame = ?";
		jdbcTemplate.update(deleteStr, gameID);	
		log.info(getTempUserName() + " delete rounds table complete");		
    }

	public void addRound(Round round)
	{
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				String insertStr = "INSERT INTO round (idgame, idplayer, hole1Score, hole2Score, hole3Score, hole4Score, hole5Score, hole6Score, ";
				insertStr = insertStr + "hole7Score, hole8Score, hole9Score, hole10Score, hole11Score, hole12Score, ";
				insertStr = insertStr + "hole13Score, hole14Score, hole15Score, hole16Score, hole17Score, hole18Score, ";
				insertStr = insertStr + "front9Score, back9Score, totalScore, totalToPar, netScore, teamNumber, roundHandicap, idTeeTimes, dSignUpdatetime, idCourseTee)";
				insertStr = insertStr + "values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";		
		
				PreparedStatement psmt = connection.prepareStatement(insertStr, new String[] {});
					
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
		});
								
		log.info(getTempUserName() + " insert round table complete");		
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
			
		log.debug(getTempUserName() + " update round table complete.  Round id updated: " + round.getRoundID());
		
	}

	public void updateRoundHandicap(Game selectedGame, int playerID, BigDecimal handicap) 
	{
		String updateStr = " UPDATE round SET roundHandicap = ? WHERE idgame = ? AND idplayer = ?";	
		jdbcTemplate.update(updateStr, handicap, selectedGame.getGameID(), playerID);		
		log.debug(getTempUserName() + " update player handicap for playerID: " + playerID + " to: " + handicap + " on round table complete");		
	}	
	
	public void updateRoundTeamNumber(Game selectedGame, int playerID, int teamNumber) 
	{
		String updateStr = " UPDATE round SET teamNumber = ? WHERE idgame = ? AND idplayer = ?";	
		jdbcTemplate.update(updateStr, teamNumber, selectedGame.getGameID(), playerID);		
		log.debug(getTempUserName() + " update team number for playerID: " + playerID + " to: " + teamNumber + " on round table complete");		
	}	
	
	private String getTempUserName() 
	{
		String username = "";		
		username = Utils.getLoggedInUserName();			
		return username;
	}
}

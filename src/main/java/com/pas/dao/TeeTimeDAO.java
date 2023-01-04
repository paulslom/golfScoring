package com.pas.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.TeeTime;

@Repository
public class TeeTimeDAO extends JdbcDaoSupport implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(TeeTimeDAO.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
	private HashMap<Integer,TeeTime> teeTimesMap = new HashMap<Integer, TeeTime>(); //we need this for the TeeTimeConverter class	
	
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
    public TeeTimeDAO(DataSource dataSource) 
	{
		this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
	
	public List<TeeTime> readTeeTimesFromDB(Game game)
    {
		String sql = "select * from teetimes where idgame = :idgame order by playGroupNumber";		 
		SqlParameterSource param = new MapSqlParameterSource("idgame", game.getGameID());		 
		List<TeeTime> teeTimeList = namedParameterJdbcTemplate.query(sql, param, new TeeTimeRowMapper()); 
    	
    	return teeTimeList;
    }
	
	public List<TeeTime> readTeeTimesFromDB()
    {
		String sql = "select * from teetimes";		 
		List<TeeTime> teeTimeList = jdbcTemplate.query(sql, new TeeTimeRowMapper()); 
    	
		for (int i = 0; i < teeTimeList.size(); i++) 
		{
			teeTimesMap.put(i, teeTimeList.get(i));
		}
    	
    	return teeTimeList;
    }
	
	public TeeTime readTeeTimeFromDB(Integer teeTimeID)
    {
		String sql = "select * from teetimes where idteeTimes = :idteeTimes";		 
		SqlParameterSource param = new MapSqlParameterSource("idteeTimes", teeTimeID);		 
		TeeTime teeTime = namedParameterJdbcTemplate.queryForObject(sql, param, new TeeTimeRowMapper());     	
		
    	return teeTime;
    }	
	
	public void addTeeTime(TeeTime teeTime)
	{
		String insertStr = "INSERT INTO teetimes (idgame, playGroupNumber, teeTime) values(?,?,?)";			
		jdbcTemplate.update(insertStr, new Object[] {teeTime.getTeeTimeGame().getGameID(), teeTime.getPlayGroupNumber(), teeTime.getTeeTimeString()});	
		log.info("addTeeTime complete");	
	}
	
	public void updateTeeTime(TeeTime teeTime)
	{
		String updateStr = "update teetimes set idgame = ?, playGroupNumber = ?, teeTime = ? where idteeTimes = ?";
		jdbcTemplate.update(updateStr, new Object[] {teeTime.getTeeTimeGame().getGameID(), teeTime.getPlayGroupNumber(), teeTime.getTeeTimeString(), teeTime.getTeeTimeID()});	 			
		log.info("updateTeeTime complete");	
	}
	
	public void addTeeTimes(Integer gameID, String teeTimesString)
	{
		String insertStr = "INSERT INTO teetimes (idgame, playGroupNumber, teeTime) values(?,?,?)";			

		StringTokenizer st = new StringTokenizer(teeTimesString, " ");
			
		int tokenCount = 0;
	     
	 	while (st.hasMoreTokens()) 
	 	{	 			
 			String teeTime = st.nextToken();
 			tokenCount++;
 			
 			jdbcTemplate.update(insertStr, new Object[] {gameID, tokenCount, teeTime});	 				
	 	}
	 	
	 	log.info("addTeeTimes complete");				
	}	
	
	//deletes all tee times for a specific game
	public void deleteTeeTimesForGameFromDB(int gameID) 
	{
		String deleteStr = "delete from teetimes where idgame = ?";
		jdbcTemplate.update(deleteStr,gameID);	
		log.info("deleteTeeTimeForGameFromDB complete");			
	}
		
	//deletes a particular tee time
	public void deleteTeeTimeFromDB(Integer teeTimeID)
    {
		String deleteStr = "delete from teetimes where idteeTimes = ?";
		jdbcTemplate.update(deleteStr, teeTimeID);	
		log.info("deleteTeeTimeFromDB complete");	
    }

	public HashMap<Integer, TeeTime> getTeeTimesMap() {
		return teeTimesMap;
	}

	public void setTeeTimesMap(HashMap<Integer, TeeTime> teeTimesMap) {
		this.teeTimesMap = teeTimesMap;
	}

	
}

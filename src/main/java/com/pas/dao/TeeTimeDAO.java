package com.pas.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.TeeTime;

@Repository
public class TeeTimeDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(TeeTimeDAO.class);
	
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	
	private Map<Integer,TeeTime> teeTimesMap = new HashMap<Integer, TeeTime>(); //we need this for the TeeTimeConverter class
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	
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
	    this.jdbcTemplate = new JdbcTemplate(dataSource);
	    this.dataSource = dataSource;	    
    }	
	
	public List<TeeTime> getTeeTimesByGame(Game game)
    {
		List<TeeTime> ttList = new ArrayList<>();
		
		for (int i = 0; i < this.getTeeTimeList().size(); i++)
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
			if (teeTime.getGameID() == game.getGameID())
			{
				ttList.add(teeTime);
			}
		}
		
		Collections.sort(ttList, new Comparator<TeeTime>() 
		{
			public int compare(TeeTime o1, TeeTime o2) 
			{
			   Integer o1Int = o1.getPlayGroupNumber();
			   Integer o2Int = o2.getPlayGroupNumber();
			   return o1Int.compareTo(o2Int);
			}
			
		});
		
    	return ttList;
    }
	
	public void readTeeTimesFromDB()
    {
		String sql = "select tt.idTeeTimes, tt.idgame, tt.playgroupnumber, tt.teetime, gm.gameDate, cs.courseName from teetimes tt inner join game gm on tt.idgame = gm.idgame inner join golfcourse cs on gm.idgolfcourse = cs.idgolfcourse";		 
		this.setTeeTimeList(jdbcTemplate.query(sql, new TeeTimeRowMapper())); 
    	
		log.info("LoggedDBOperation: function-inquiry; table:teetimes; rows:" + teeTimeList.size());
		
		this.setTeeTimesMap(this.getTeeTimeList().stream().collect(Collectors.toMap(TeeTime::getTeeTimeID, tt -> tt)));    	
    }
	
	public TeeTime getTeeTimeByTeeTimeID(Integer teeTimeID)
    {
		TeeTime teeTime = this.getTeeTimesMap().get(teeTimeID);
		return teeTime;
    }	
	
	public void addTeeTime(TeeTime teeTime)
	{
		String insertStr = "INSERT INTO teetimes (idgame, playGroupNumber, teeTime) values(?,?,?)";			
		
		KeyHolder keyHolder = new GeneratedKeyHolder();
			
		jdbcTemplate.update(new PreparedStatementCreator() 
		{
			public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
			{
				PreparedStatement psmt = connection.prepareStatement(insertStr, Statement.RETURN_GENERATED_KEYS);
				psmt.setInt(1, teeTime.getGameID());
				psmt.setInt(2, teeTime.getPlayGroupNumber());
				psmt.setString(3, teeTime.getTeeTimeString());
				return psmt;
			}
		}, keyHolder);
 
		log.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
		
		teeTime.setTeeTimeID(keyHolder.getKey().intValue());
		this.getTeeTimesMap().put(keyHolder.getKey().intValue(), teeTime);
		
		refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
				
		log.info("addTeeTime complete");	
	}
	
	public void updateTeeTime(TeeTime teeTime)
	{
		String updateStr = "update teetimes set idgame = ?, playGroupNumber = ?, teeTime = ? where idteeTimes = ?";
		jdbcTemplate.update(updateStr, new Object[] {teeTime.getGameID(), teeTime.getPlayGroupNumber(), teeTime.getTeeTimeString(), teeTime.getTeeTimeID()});	
		log.info("LoggedDBOperation: function-update; table:teetimes; rows:1");
		
		refreshListsAndMaps("update", teeTime);
		
		log.info("updateTeeTime complete");	
	}
	
	public void addTeeTimes(Integer gameID, String teeTimesString, Date gameDate, String courseName)
	{
		String insertStr = "INSERT INTO teetimes (idgame, playGroupNumber, teeTime) values(?,?,?)";			

		StringTokenizer st = new StringTokenizer(teeTimesString, " ");
			
		int tokenCount = 0;
	     
	 	while (st.hasMoreTokens()) 
	 	{	 			
 			String teeTimeStr = st.nextToken();
 			tokenCount++;
 			
 			final Integer tkcInt = new Integer(tokenCount);
 			
 			KeyHolder keyHolder = new GeneratedKeyHolder();
 			
 			jdbcTemplate.update(new PreparedStatementCreator() 
 			{
 				public PreparedStatement createPreparedStatement(Connection connection) throws SQLException 
 				{
 					PreparedStatement psmt = connection.prepareStatement(insertStr, new String[] { "idteeTimes" });
 					psmt.setInt(1, gameID);
 					psmt.setInt(2, tkcInt);
 					psmt.setString(3, teeTimeStr);
 					return psmt;
 				}
 			}, keyHolder);
 	 
 			log.info("LoggedDBOperation: function-add; table:teetimes; rows:1");
 			
 			TeeTime teeTime = new TeeTime();
 			teeTime.setTeeTimeID(keyHolder.getKey().intValue());
 			teeTime.setGameID(gameID);
 			teeTime.setPlayGroupNumber(tokenCount);
 			teeTime.setTeeTimeString(teeTimeStr);
 			teeTime.setGameDate(gameDate);
 			teeTime.setCourseName(courseName);
 			this.getTeeTimeList().add(teeTime);
 			this.getTeeTimesMap().put(keyHolder.getKey().intValue(), teeTime);
 			
 			refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
	 	}
	 	
	 	log.info("addTeeTimes complete");				
	}	
	
	//deletes all tee times for a specific game
	public void deleteTeeTimesForGameFromDB(int gameID) 
	{
		//first identify the games we're talking about, so that we can fix up the list and map after the DB interaction.
		List<Integer> teeTimeIDs = new ArrayList<>();
		for (int i = 0; i < this.getTeeTimeList().size(); i++)
		{
			TeeTime teeTime = this.getTeeTimeList().get(i);
			if (teeTime.getGameID() == gameID)
			{
				teeTimeIDs.add(teeTime.getTeeTimeID());
			}
		}
		
		String deleteStr = "delete from teetimes where idgame = ?";
		int updatedRows = jdbcTemplate.update(deleteStr,gameID);
		log.info("LoggedDBOperation: function-update; table:teetimes; rows:" + updatedRows);
		
		for (int i = 0; i < teeTimeIDs.size(); i++) 
		{
			this.getTeeTimesMap().remove(teeTimeIDs.get(i));
		}		
		
		refreshListsAndMaps("special", null); //special bypasses add/update/delete and assumes the map is good and then rebuilds the list and sorts
		
		log.info("deleteTeeTimeForGameFromDB complete");			
	}
		
	//deletes a particular tee time
	public void deleteTeeTimeFromDB(Integer teeTimeID)
    {
		String deleteStr = "delete from teetimes where idteeTimes = ?";
		jdbcTemplate.update(deleteStr, teeTimeID);	
		
		log.info("LoggedDBOperation: function-update; table:teetimes; rows:1");
		
		TeeTime teeTime = new TeeTime();
		teeTime.setTeeTimeID(teeTimeID);
		
		refreshListsAndMaps("delete", teeTime); 		
		
		log.info("deleteTeeTimeFromDB complete");	
    }

	private void refreshListsAndMaps(String function, TeeTime teeTime)
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getTeeTimesMap().remove(teeTime.getTeeTimeID());	
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getTeeTimesMap().remove(teeTime.getTeeTimeID());	
			this.getTeeTimesMap().put(teeTime.getTeeTimeID(), teeTime);	
		}
		
		this.getTeeTimeList().clear();
		Collection<TeeTime> values = this.getTeeTimesMap().values();
		this.setTeeTimeList(new ArrayList<>(values));
		
		Collections.sort(this.getTeeTimeList(), new Comparator<TeeTime>() 
		{
		   public int compare(TeeTime o1, TeeTime o2) 
		   {
		      return o1.getTeeTimeString().compareTo(o2.getTeeTimeString());
		   }
		});
		
	}	
	
	public List<TeeTime> getTeeTimeList() 
	{
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) 
	{
		this.teeTimeList = teeTimeList;
	}

	public Map<Integer, TeeTime> getTeeTimesMap() {
		return teeTimesMap;
	}

	public void setTeeTimesMap(Map<Integer, TeeTime> teeTimesMap) {
		this.teeTimesMap = teeTimesMap;
	}

	
}

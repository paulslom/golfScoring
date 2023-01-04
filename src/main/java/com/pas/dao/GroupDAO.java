package com.pas.dao;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.pas.beans.Group;

@Repository
public class GroupDAO extends JdbcDaoSupport implements Serializable 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(GroupDAO.class);
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	
	private Map<Integer,Group> groupsMap = new HashMap<Integer,Group>();
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
		   log.info("attempting to setDataSource in initialize method of GroupDAO");
	       setDataSource(dataSource);
	       log.info("successfully set setDataSource in initialize method of GroupDAO");
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing DAO: {}" +  ex.getStackTrace());
	   }
	}

	@Autowired
    public GroupDAO(DataSource dataSource) 
	{
		this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

	public List<Group> readGroupsFromDB() 
    {
		log.info("attempting to readGroupsFromDB in GroupDAO");
		String sql = "select * from golfgroup";		 
		List<Group> groupList = jdbcTemplate.query(sql, new GroupRowMapper()); 
		
		log.info("successfully read groups in readGroupsFromDB in GroupDAO");
		
		groupsMap = groupList.stream().collect(Collectors.toMap(Group::getGroupID, group -> group));	   
		
    	return groupList;
	}

	public Map<Integer, Group> getGroupsMap() {
		return groupsMap;
	}

	public void setGroupsMap(Map<Integer, Group> groupsMap) {
		this.groupsMap = groupsMap;
	}	

}

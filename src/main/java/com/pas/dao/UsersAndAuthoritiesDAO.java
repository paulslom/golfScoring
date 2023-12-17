package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.pas.beans.GolfUser;

import jakarta.annotation.PostConstruct;

@Repository
public class UsersAndAuthoritiesDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(UsersAndAuthoritiesDAO.class);
	private final transient JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	
	private Map<String,GolfUser> fullUserMap = new HashMap<>();
	private Map<String,GolfUser> adminUserMap = new HashMap<>();

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
    public UsersAndAuthoritiesDAO(DataSource dataSource) 
	{
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }
	
	public List<String> getAdminUserList()
	{
		List<String> adminUserList = new ArrayList<>();		
		this.getAdminUserMap().forEach((k, v) -> adminUserList.add(k));		
		return adminUserList;		
	}
	
	public void readAllUsersFromDB()
	{				
		String sql1 = "SELECT user_id, username, password, role FROM golfusers";
		 
		List<GolfUser> userList = jdbcTemplate.query(sql1, new ResultSetExtractor<List<GolfUser>>() 
		{	   
			@Override
		    public List<GolfUser> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<GolfUser> userList2 = new ArrayList<>();
				while (rs.next()) 
				{
			        GolfUser GolfUser2 = new GolfUser();
			        GolfUser2.setUserId(rs.getInt("user_id"));
			        GolfUser2.setUserName(rs.getString("username").toLowerCase());
			        GolfUser2.setPassword(rs.getString("password"));
			        GolfUser2.setUserRole(rs.getString("role"));		 
		            userList2.add(GolfUser2);
				}
				return userList2;
		    }
		});
    	
		log.info("LoggedDBOperation: function-inquiry; table:golfusers; rows:" + userList.size());
		
		for (int i = 0; i < userList.size(); i++) 
		{
			GolfUser gu = userList.get(i);
			
			if (this.getFullUserMap().containsKey(gu.getUserName()))
			{
				log.error("duplicate user: " + gu.getUserName());
			}
			else
			{
				this.getFullUserMap().put(gu.getUserName(), gu);
								
				if (gu.getUserRole().contains("ADMIN"))
				{
					this.getAdminUserMap().put(gu.getUserName(), gu);
				}
			}
			
		}
		
		//this loop only for debugging purposes
		/*
		for (Map.Entry<String, GolfUser> entry : this.getFullUserMap().entrySet()) 
		{
		    String key = entry.getKey();
		    GolfUser golfUser = entry.getValue();

		    log.info("Key = " + key + ", value = " + golfUser.getUserName());
		}
		*/
		
		log.info("exiting");
		
	}
	
	public GolfUser getGolfUser(String username)
    {	    	
		GolfUser gu = this.getFullUserMap().get(username);			
    	return gu;
    }	

	
	private void deleteUserAndAuthority(String username)
	{
		String deleteStrAuthorities = "DELETE from authorities where username = ?";
		jdbcTemplate.update(deleteStrAuthorities, username);	
		log.info("LoggedDBOperation: function-update; table:authorities; rows:1");
		
		String deleteStrUsers = "DELETE from users where username = ?";	
		jdbcTemplate.update(deleteStrUsers, username);	
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		GolfUser gu = new GolfUser();
		gu.setUserName(username);
		refreshListsAndMaps("delete", gu);	
	}
	
	public void addUserAndAuthority(GolfUser gu)
	{
		int ENABLED = 1;
		
		String insertStrUsers = "INSERT INTO golfusers (username, password, role, enabled) VALUES (?,?,?,?)";
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getPassword());	
		gu.setPassword(encodedPW);
		jdbcTemplate.update(insertStrUsers, new Object[] {gu.getUserName(), encodedPW, gu.getUserRole(), ENABLED});
		log.info("LoggedDBOperation: function-update; table:golfusers; rows:1");
					
		refreshListsAndMaps("add", gu);	
	}
	
	public void updateUserAndAuthority(String username, GolfUser gu)
	{
		deleteUserAndAuthority(username);
		addUserAndAuthority(gu);		
		refreshListsAndMaps("update", gu);	
	}

	private void refreshListsAndMaps(String function, GolfUser golfuser) 
	{
		if (function.equalsIgnoreCase("delete"))
		{
			this.getFullUserMap().remove(golfuser.getUserName());	
		}
		else if (function.equalsIgnoreCase("add"))
		{
			this.getFullUserMap().put(golfuser.getUserName(), golfuser);	
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getFullUserMap().remove(golfuser.getUserName());	
			this.getFullUserMap().put(golfuser.getUserName(), golfuser);		
		}
		
	}

	public void resetPassword(GolfUser gu) 
	{
		String updateStr = " UPDATE users SET password = ? WHERE username = ?";
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getUserName()); //resets to their username		
		jdbcTemplate.update(updateStr, encodedPW, gu);
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		refreshListsAndMaps("update", gu);	
		
		log.debug("successfully reset password for user " + gu);			
	}
	
	public void updateRole(GolfUser gu) 
	{
		String updateStr = " UPDATE golfusers SET role = ? WHERE username = ?";
		jdbcTemplate.update(updateStr, gu.getUserRole(), gu.getUserName());
		log.info("LoggedDBOperation: function-update; table:authorities; rows:1");
		
		refreshListsAndMaps("update", gu);	
		
		log.debug("successfully reset role for user " + gu.getUserName());			
	}	

	public Map<String, GolfUser> getFullUserMap() 
	{
		return fullUserMap;
	}

	public void setFullUserMap(Map<String, GolfUser> fullUserMap) 
	{
		this.fullUserMap = fullUserMap;
	}

	public Map<String, GolfUser> getAdminUserMap() {
		return adminUserMap;
	}

	public void setAdminUserMap(Map<String, GolfUser> adminUserMap) {
		this.adminUserMap = adminUserMap;
	}

	
	
}

package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.pas.beans.GolfUser;

@Repository
public class UsersAndAuthoritiesDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(UsersAndAuthoritiesDAO.class);
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final JdbcTemplate jdbcTemplate;
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
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.dataSource = dataSource;
    }
		
	private class TempUser
	{
		private String userName;
		private String password;
		
		public String getUserName() {
			return userName;
		}
		public void setUserName(String userName) {
			this.userName = userName;
		}
		public String getPassword() {
			return password;
		}
		public void setPassword(String password) {
			this.password = password;
		}		
	}
	
	private class TempAuthority
	{
		private String authority;
		
		public String getAuthority() {
			return authority;
		}
		public void setAuthority(String authority) {
			this.authority = authority;
		}
		
	}
	
	public List<String> getAdminUserList()
	{
		List<String> adminUserList = new ArrayList<>();		
		this.getAdminUserMap().forEach((k, v) -> adminUserList.add(k));		
		return adminUserList;		
	}
	
	public void readAllUsersFromDB()
	{				
		String sql1 = "SELECT username, password FROM users";
		 
		List<TempUser> userList = jdbcTemplate.query(sql1, new ResultSetExtractor<List<TempUser>>() 
		{	   
			@Override
		    public List<TempUser> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<TempUser> userList2 = new ArrayList<>();
				while (rs.next()) 
				{
			        TempUser tempUser2 = new TempUser();
			        tempUser2.setUserName(rs.getString("username").toLowerCase());
			        tempUser2.setPassword(rs.getString("password"));		        
		            userList2.add(tempUser2);
				}
				return userList2;
		    }
		});
    	
		log.info("LoggedDBOperation: function-inquiry; table:users; rows:" + userList.size());
		
		for (int i = 0; i < userList.size(); i++) 
		{
			GolfUser gu = new GolfUser();
			
			TempUser tempUser = userList.get(i);
			
			if (tempUser != null)
			{
				gu.setUserName(tempUser.getUserName());
				gu.setPassword(tempUser.getPassword());
			}
			
			String sql2 = "SELECT a.authority FROM authorities a where a.username = :username";
			 
			SqlParameterSource param2 = new MapSqlParameterSource("username", gu.getUserName());
			
			List<TempAuthority> authorityList = namedParameterJdbcTemplate.query(sql2, param2, new RowMapper<TempAuthority>() 
			{			 
			    @Override
			    public TempAuthority mapRow(ResultSet rs, int rowNum) throws SQLException 
			    {		         
			        TempAuthority tempAuthority = new TempAuthority();
			        tempAuthority.setAuthority(rs.getString("authority"));		       
			        return tempAuthority;
			    }
			});
			
			log.info("LoggedDBOperation: function-inquiry; table:authorities; rows:" + authorityList.size());
			
			List<String> userRoleList = new ArrayList<String>();
			
			for (int j = 0; j < authorityList.size(); j++) 
			{
				TempAuthority ta = authorityList.get(j);
				userRoleList.add(ta.getAuthority());
				
				String[] arr = new String[userRoleList.size()]; 
		        arr = userRoleList.toArray(arr); 
				gu.setUserRoles(arr);
			}
			
			if (this.getFullUserMap().containsKey(gu.getUserName()))
			{
				log.error("duplicate user: " + gu.getUserName());
			}
			else
			{
				this.getFullUserMap().put(gu.getUserName(), gu);
								
				for (int j = 0; j < gu.getUserRoles().length; j++) 
				{
					String userRole = gu.getUserRoles()[j];
					if (userRole.contains("ADMIN"))
					{
						this.getAdminUserMap().put(gu.getUserName(), gu);
					}
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
		String insertStrUsers = "INSERT INTO users (username, password, enabled) VALUES (?,?,?)";
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getPassword());	
		gu.setPassword(encodedPW);
		jdbcTemplate.update(insertStrUsers, new Object[] {gu.getUserName(), encodedPW, true});
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		String insertStrAuthorities = "INSERT INTO authorities (username, authority) VALUES (?,?)";			
		jdbcTemplate.update(insertStrAuthorities, new Object[] {gu.getUserName(), gu.getUserRoles()[0]});	
		log.info("LoggedDBOperation: function-update; table:authorities; rows:1");
				
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
		String updateStr = " UPDATE authorities SET authority = ? WHERE username = ?";
		jdbcTemplate.update(updateStr, gu.getUserRoles(), gu.getUserName());
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

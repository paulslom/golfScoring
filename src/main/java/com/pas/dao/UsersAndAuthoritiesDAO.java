package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(UsersAndAuthoritiesDAO.class);
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;

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
	
	public List<String> readAdminUsers()
	{
		String sql = "SELECT auth.username FROM authorities auth where authority = 'ADMIN'";
		List<String> userList = jdbcTemplate.query(sql, new RowMapper<String>()
				{public String mapRow(ResultSet rs, int rowNum) 
                    throws SQLException {return rs.getString(1);}});
		return userList;		
	}
	
	public GolfUser readUserFromDB(String username)
    {	    	
		GolfUser gu = new GolfUser();
		
		String sql1 = "SELECT u.username, u.password FROM users u where u.username = :username";
		 
		SqlParameterSource param1 = new MapSqlParameterSource("username", username);
		 
		TempUser tempUser = namedParameterJdbcTemplate.query(sql1, param1, new ResultSetExtractor<TempUser>() 
		{	   
			@Override
		    public TempUser extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				if (rs.next()) 
				{
			        TempUser tempUser2 = new TempUser();
			        tempUser2.setUserName(rs.getString("username"));
			        tempUser2.setPassword(rs.getString("password"));		        
		            return tempUser2;
				}
				return null;
		    }
		});
    	
		if (tempUser != null)
		{
			gu.setUserName(tempUser.getUserName());
			gu.setPassword(tempUser.getPassword());
		}
		
		String sql2 = "SELECT a.authority FROM authorities a where a.username = :username";
		 
		SqlParameterSource param2 = new MapSqlParameterSource("username", username);
		
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
		
		List<String> userRoleList = new ArrayList<String>();
		
		for (int i = 0; i < authorityList.size(); i++) 
		{
			TempAuthority ta = authorityList.get(i);
			userRoleList.add(ta.getAuthority());
			
			String[] arr = new String[userRoleList.size()]; 
	        arr = userRoleList.toArray(arr); 
			gu.setUserRoles(arr);
		}
			
    	return gu;
    }	

	private void deleteUserAndAuthority(String username)
	{
		String deleteStrAuthorities = "DELETE from authorities where username = ?";
		jdbcTemplate.update(deleteStrAuthorities, username);		
		String deleteStrUsers = "DELETE from users where username = ?";	
		jdbcTemplate.update(deleteStrUsers, username);		
	}
	
	public void addUserAndAuthority(String username, String password, String userrole)
	{
		String insertStrUsers = "INSERT INTO users (username, password, enabled) VALUES (?,?,?)";
		String encodedPW=new BCryptPasswordEncoder().encode(password);		
		jdbcTemplate.update(insertStrUsers, new Object[] {username, encodedPW, true});
		
		String insertStrAuthorities = "INSERT INTO authorities (username, authority) VALUES (?,?)";			
		jdbcTemplate.update(insertStrAuthorities, new Object[] {username, userrole});	
	}
	
	public void updateUserAndAuthority(String username, String password, String userrole)
	{
		deleteUserAndAuthority(username);
		addUserAndAuthority(username, password, userrole);
	}

	public void resetPassword(String username) 
	{
		String updateStr = " UPDATE users SET password = ? WHERE username = ?";
		String encodedPW=new BCryptPasswordEncoder().encode(username); //resets to their username		
		jdbcTemplate.update(updateStr, encodedPW, username);
		
		log.debug("successfully reset password for user " + username);			
	}
	
	public void updateRole(String username, String newRole) 
	{
		String updateStr = " UPDATE authorities SET authority = ? WHERE username = ?";
		jdbcTemplate.update(updateStr, newRole, username);
		
		log.debug("successfully reset role for user " + username);			
	}
	
	
}

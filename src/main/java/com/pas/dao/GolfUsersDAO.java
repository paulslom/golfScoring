package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;

@Repository
public class GolfUsersDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(GolfUsersDAO.class);
	private final transient JdbcTemplate jdbcTemplate;
	private final DataSource dataSource;
	
	private Map<String,GolfUser> fullUserMap = new HashMap<>();
	private Map<String,GolfUser> adminUserMap = new HashMap<>();

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<GolfUser> golfUsersTable;
	private static final String AWS_TABLE_NAME = "golfUsers";
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       setDataSource(dataSource);
	       dynamoClients = DynamoUtil.getDynamoClients();
	       golfUsersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(GolfUser.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing GolfUsersDAO. Ex = " + ex.getMessage(), ex);
	   }
	   
	}

	@Autowired
    public GolfUsersDAO(DataSource dataSource) 
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
	
	public void readAllUsersFromDB() throws Exception
	{				
		List<GolfUser> userList = new ArrayList<>();
		
		/*
		System.out.println("Listing tables in DynamoDB Local...");
        System.out.println("-------------------------------");
        ListTablesResponse listTablesResponse = dynamoClients.getDdbClient().listTables();
        System.out.println(listTablesResponse.tableNames());
        */
		
	    Iterator<GolfUser> results = golfUsersTable.scan().items().iterator();
            
        while (results.hasNext()) 
        {
            GolfUser gu = results.next();
            
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
          	
		log.info("LoggedDBOperation: function-inquiry; table:golfusers; rows:" + userList.size());
				
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
	
	public void readAllUsersFromDBOldMySQLWay() throws Exception
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
	
	private void deleteUser(String username) throws Exception
	{
		String deleteStrUsers = "DELETE from golfusers where username = ?";	
		jdbcTemplate.update(deleteStrUsers, username);	
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		GolfUser gu = new GolfUser();
		gu.setUserName(username);
		refreshListsAndMaps("delete", gu);	
	}
	
	public void addUser(GolfUser gu) throws Exception
	{
		int ENABLED = 1;
		
		String insertStrUsers = "INSERT INTO golfusers (username, password, role, enabled) VALUES (?,?,?,?)";
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getPassword());	
		gu.setPassword(encodedPW);
		jdbcTemplate.update(insertStrUsers, new Object[] {gu.getUserName(), encodedPW, gu.getUserRole(), ENABLED});
		log.info("LoggedDBOperation: function-update; table:golfusers; rows:1");
					
		refreshListsAndMaps("add", gu);	
	}
	
	public void updateUser(String username, GolfUser gu) throws Exception
	{
		deleteUser(username);
		addUser(gu);		
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

	public void resetPassword(GolfUser gu) throws Exception
	{
		String updateStr = " UPDATE golfusers SET password = ? WHERE username = ?";
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getUserName()); //resets to their username
		gu.setPassword(encodedPW);
		log.debug("encoded password for user " + gu.getUserName() + " is " + encodedPW);	
		jdbcTemplate.update(updateStr, encodedPW, gu.getUserName());
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		refreshListsAndMaps("update", gu);	
		
		log.debug("successfully reset password for user " + gu);			
	}
	
	public void updateRole(GolfUser gu)  throws Exception
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

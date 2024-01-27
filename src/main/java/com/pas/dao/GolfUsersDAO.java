package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;

import com.pas.beans.GolfUser;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedResponse;

@Repository
public class GolfUsersDAO implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(GolfUsersDAO.class);
	
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
	       dynamoClients = DynamoUtil.getDynamoClients();
	       golfUsersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(GolfUser.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing GolfUsersDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}

	public List<String> getAdminUserList()
	{
		List<String> adminUserList = new ArrayList<>();		
		this.getAdminUserMap().forEach((k, v) -> adminUserList.add(k));		
		return adminUserList;		
	}
	
	public void readAllUsersFromDB() throws Exception
	{				
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
          	
		log.info("LoggedDBOperation: function-inquiry; table:golfusers; rows:" + this.getFullUserMap().size());
				
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
		Key key = Key.builder().partitionValue(username).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		golfUsersTable.deleteItem(deleteItemEnhancedRequest);
		
		log.info("LoggedDBOperation: function-update; table:users; rows:1");
		
		GolfUser gu = new GolfUser();
		gu.setUserName(username);
		refreshListsAndMaps("delete", gu);	
	}
	
	public void addUser(GolfUser gu) throws Exception
	{
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getPassword());	
		gu.setPassword(encodedPW);
		
		PutItemEnhancedRequest<GolfUser> putItemEnhancedRequest = PutItemEnhancedRequest.builder(GolfUser.class).item(gu).build();
		PutItemEnhancedResponse<GolfUser> putItemEnhancedResponse = golfUsersTable.putItemWithResponse(putItemEnhancedRequest);
		GolfUser returnedUser = putItemEnhancedResponse.attributes();
		
		if (!returnedUser.equals(gu))
		{
			throw new Exception("something went wrong with addUser - returned item not the same as what we attempted to put");
		}
		
		log.info("LoggedDBOperation: function-update; table:golfusers; rows:1");
					
		refreshListsAndMaps("add", gu);	
	}
	
	public void updateUser(GolfUser gu) throws Exception
	{
		deleteUser(gu.getUserName());
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
		String encodedPW=new BCryptPasswordEncoder().encode(gu.getUserName()); //resets to their username
		gu.setPassword(encodedPW);
		
		//log.debug("encoded password for user " + gu.getUserName() + " is " + encodedPW);
		
		updateUser(gu);
		
		log.debug("successfully reset password for user " + gu);			
	}
	
	public void updateRole(GolfUser gu)  throws Exception
	{
		updateUser(gu);		
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

	public Map<String, GolfUser> getAdminUserMap() 
	{
		return adminUserMap;
	}

	public void setAdminUserMap(Map<String, GolfUser> adminUserMap) 
	{
		this.adminUserMap = adminUserMap;
	}
	
}

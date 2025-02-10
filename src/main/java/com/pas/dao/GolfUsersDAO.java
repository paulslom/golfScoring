package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.GolfUser;
import com.pas.dynamodb.DynamoClients;
import com.pas.util.Utils;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

public class GolfUsersDAO implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(GolfUsersDAO.class);
	
	private Map<String,GolfUser> fullUserMap = new HashMap<>();
	private Map<String,GolfUser> adminUserMap = new HashMap<>();

	private static DynamoClients dynamoClients;
	private static DynamoDbTable<GolfUser> golfUsersTable;
	private static final String AWS_TABLE_NAME = "golfUsers";
	
	public GolfUsersDAO(DynamoClients dynamoClients2)
	{
	   try 
	   {
	       dynamoClients = dynamoClients2;
	       golfUsersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(GolfUser.class));
	   } 
	   catch (final Exception ex) 
	   {
	      logger.error("Got exception while initializing GolfUsersDAO. Ex = " + ex.getMessage(), ex);
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
				logger.error("duplicate user: " + gu.getUserName());
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
          	
		logger.info("LoggedDBOperation: function-inquiry; table:golfusers; rows:" + this.getFullUserMap().size());
				
		//this loop only for debugging purposes
		/*
		for (Map.Entry<String, GolfUser> entry : this.getFullUserMap().entrySet()) 
		{
		    String key = entry.getKey();
		    GolfUser golfUser = entry.getValue();

		    logger.info("Key = " + key + ", value = " + golfUser.getUserName());
		}
		*/
		
		logger.info("exiting");
		
	}
		
	public GolfUser getGolfUser(String username)
    {	    	
		GolfUser gu = this.getFullUserMap().get(username);			
    	return gu;
    }	
	
	public GolfUser getGolfUserFromDB(String username)
    {	    	
		GolfUser inputGolfUser = new GolfUser();
		inputGolfUser.setUserName(username);
		GolfUser retrievedGolfUser = golfUsersTable.getItem(inputGolfUser);		
		return retrievedGolfUser;
    }	
	
	private void deleteUser(String username) throws Exception
	{
		Key key = Key.builder().partitionValue(username).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		golfUsersTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:users; rows:1");
		
		GolfUser gu = new GolfUser();
		gu.setUserName(username);
		refreshListsAndMaps("delete", gu);	
	}
	
	public void addUser(GolfUser gu, String pw) throws Exception
	{
		String encodedPW = "";
		
		if (pw == null || pw.trim().length() == 0)
		{	
			encodedPW = Utils.getEncryptedPassword(gu.getUserName());
		}
		else
		{
			encodedPW = Utils.getEncryptedPassword(pw);
			
		}
		
		gu.setPassword(encodedPW);		
		
		PutItemEnhancedRequest<GolfUser> putItemEnhancedRequest = PutItemEnhancedRequest.builder(GolfUser.class).item(gu).build();
		golfUsersTable.putItem(putItemEnhancedRequest);
			
		logger.info("LoggedDBOperation: function-update; table:golfusers; rows:1");
					
		refreshListsAndMaps("add", gu);	
	}
	
	public void updateUser(GolfUser gu) throws Exception
	{
		deleteUser(gu.getUserName());
		addUser(gu, gu.getPassword());		
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
	
	public void updateRole(GolfUser gu)  throws Exception
	{
		updateUser(gu);		
		logger.debug("successfully reset role for user " + gu.getUserName());			
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

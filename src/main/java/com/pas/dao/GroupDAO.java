package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.pas.beans.GolfMain;
import com.pas.beans.Group;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGroup;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class GroupDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(GroupDAO.class);
	
	private Map<String,Group> groupsMap = new HashMap<>();
	private List<Group> groupsList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGroup> groupsTable;
	private static final String AWS_TABLE_NAME = "groups";
	
	@Autowired private final GolfMain golfmain;
	
	public GroupDAO(DynamoClients dynamoClients2, GolfMain golfmain) 
	{
		this.golfmain = golfmain;
		
		dynamoClients = dynamoClients2;
		
		try 
	    {
	       groupsTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGroup.class));
	    } 
	    catch (final Exception ex) 
	    {
	       logger.error("Got exception while initializing PlayersDAO. Ex = " + ex.getMessage(), ex);
	    }	  
		
	}

	public void readGroupsFromDB() 
    {
		logger.info("attempting to readGroupsFromDB");
		
		Iterator<DynamoGroup> results = groupsTable.scan().items().iterator();
		
		while (results.hasNext()) 
        {
			DynamoGroup dynamoGroup = results.next();
            
			Group group = new Group(golfmain);
			group.setGroupID(dynamoGroup.getGroupID());
			group.setGroupName(dynamoGroup.getGroupName());
			
            this.getGroupsList().add(group);			
        }
		
		logger.info("successfully read groups in readGroupsFromDB");
		
		logger.info("LoggedDBOperation: function-inquiry; table:group; rows:" + this.getGroupsList().size());
		
		groupsMap = this.getGroupsList().stream().collect(Collectors.toMap(Group::getGroupID, group -> group));	 		
	}

	public Map<String, Group> getGroupsMap() 
	{
		return groupsMap;
	}

	public void setGroupsMap(Map<String, Group> groupsMap) 
	{
		this.groupsMap = groupsMap;
	}

	public List<Group> getGroupsList() 
	{
		return groupsList;
	}

	public void setGroupsList(List<Group> groupsList) 
	{
		this.groupsList = groupsList;
	}	

}

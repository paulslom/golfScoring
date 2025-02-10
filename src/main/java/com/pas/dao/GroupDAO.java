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

import com.pas.beans.GolfMain;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGroup;

import jakarta.inject.Inject;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

public class GroupDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(GroupDAO.class);
	
	private Map<String,DynamoGroup> groupsMap = new HashMap<>();

	private List<DynamoGroup> groupsList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGroup> groupsTable;
	private static final String AWS_TABLE_NAME = "groups";
	
	@Inject GolfMain golfmain;
	
	public GroupDAO(DynamoClients dynamoClients2) 
	{
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
            

			
            this.getGroupsList().add(dynamoGroup);
        }
		
		logger.info("successfully read groups in readGroupsFromDB");
		
		logger.info("LoggedDBOperation: function-inquiry; table:group; rows:" + this.getGroupsList().size());
		
		groupsMap = this.getGroupsList().stream().collect(Collectors.toMap(DynamoGroup::getGroupID, group -> group));
	}

	public List<DynamoGroup> getGroupsList() {
		return groupsList;
	}

	public void setGroupsList(List<DynamoGroup> groupsList) {
		this.groupsList = groupsList;
	}

	public Map<String, DynamoGroup> getGroupsMap() {
		return groupsMap;
	}

	public void setGroupsMap(Map<String, DynamoGroup> groupsMap) {
		this.groupsMap = groupsMap;
	}


}

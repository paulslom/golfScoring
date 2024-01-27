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
import org.springframework.stereotype.Repository;

import com.pas.beans.Group;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoGroup;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

@Repository
public class GroupDAO implements Serializable 
{
	private static final long serialVersionUID = 1L;
	private static Logger log = LogManager.getLogger(GroupDAO.class);
	
	private Map<String,Group> groupsMap = new HashMap<>();
	private List<Group> groupsList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoGroup> groupsTable;
	private static final String AWS_TABLE_NAME = "groups";
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       groupsTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGroup.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing PlayersDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}

	public void readGroupsFromDB() 
    {
		log.info("attempting to readGroupsFromDB");
		
		Iterator<DynamoGroup> results = groupsTable.scan().items().iterator();
		
		while (results.hasNext()) 
        {
			DynamoGroup dynamoGroup = results.next();
            
			Group group = new Group();
			group.setGroupID(dynamoGroup.getGroupID());
			group.setGroupName(dynamoGroup.getGroupName());
			
            this.getGroupsList().add(group);			
        }
		
		log.info("successfully read groups in readGroupsFromDB");
		
		log.info("LoggedDBOperation: function-inquiry; table:group; rows:" + this.getGroupsList().size());
		
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

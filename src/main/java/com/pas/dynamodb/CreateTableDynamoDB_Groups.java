package com.pas.dynamodb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pas.beans.Group;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDB_Groups
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Groups.class);
	private static String AWS_TABLE_NAME = "groups";
	
	public void loadTable(DynamoClients dynamoClients, InputStream inputStream) throws Exception 
	{
		//Delete the table in DynamoDB Local if it exists.  If not, just catch the exception and move on
        try
        {
        	deleteTable(dynamoClients.getDynamoDbEnhancedClient());
        }
        catch (Exception e)
        {
        	logger.info(e.getMessage());
        }
        
        // Create a table in DynamoDB Local
        DynamoDbTable<DynamoGroup> groupsTable = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        loadTableData(groupsTable, inputStream);    
		
	}
        
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoGroup> groupsTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGroup.class));
       	groupsTable.deleteTable();
	}

    private static void loadTableData(DynamoDbTable<DynamoGroup> groupsTable, InputStream inputStream) throws Exception
    {   
    	// Insert data into the table
    	logger.info("Inserting data into the table:" + AWS_TABLE_NAME);  
        
        List<Group> groupsList = readFromFileAndConvert(inputStream);
        
        if (groupsList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
        }
        else
        {
        	for (int i = 0; i < groupsList.size(); i++) 
    		{
            	Group obj = groupsList.get(i);
            	
            	DynamoGroup dg = new DynamoGroup();
            	dg.setGroupID(UUID.randomUUID().toString());
            	dg.setOldGroupID(obj.getOldGroupID());
            	dg.setGroupName(obj.getGroupName());
            	
            	groupsTable.putItem(dg);                
    		}             
        }        
	}
    
    private static List<Group> readFromFileAndConvert(InputStream inputStream) throws Exception 
    {
    	Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	Group[] groupArray = new Gson().fromJson(reader, Group[].class);
        List<Group> groupList = Arrays.asList(groupArray);
        return groupList;       
    }
    
    private static DynamoDbTable<DynamoGroup> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoGroup> groupsTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGroup.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
	        groupsTable.createTable(builder -> builder
	                .provisionedThroughput(b -> b
	                        .readCapacityUnits(Long.valueOf(5))
	                        .writeCapacityUnits(Long.valueOf(5))
	                        .build())
	        );
	        
        }
        catch (ResourceInUseException riue)
        {
        	System.out.println("Table already exists! " + riue.getMessage());
        	throw riue;
        }
        // The 'dynamoDbClient' instance that's passed to the builder for the DynamoDbWaiter is the same instance
        // that was passed to the builder of the DynamoDbEnhancedClient instance used to create the 'customerDynamoDbTable'.
        // This means that the same Region that was configured on the standard 'dynamoDbClient' instance is used for all service clients.
        
        try (DynamoDbWaiter waiter = DynamoDbWaiter.builder().client(ddbClient).build()) // DynamoDbWaiter is Autocloseable
        { 
            ResponseOrException<DescribeTableResponse> response = waiter
                    .waitUntilTableExists(builder -> builder.tableName(AWS_TABLE_NAME).build())
                    .matched();
            
            response.response().orElseThrow(
                    () -> new RuntimeException(AWS_TABLE_NAME + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(AWS_TABLE_NAME + " table was created.");
        }        
        
        return groupsTable;
    }

	
   
}
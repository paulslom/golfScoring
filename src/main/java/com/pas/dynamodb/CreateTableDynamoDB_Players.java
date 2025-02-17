package com.pas.dynamodb;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.pas.beans.Player;

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDB_Players
{
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Players.class);
	private static String AWS_TABLE_NAME = "players";
	
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
        DynamoDbTable<DynamoPlayer> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        loadTableData(table, inputStream);	
	}
     
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoPlayer> playersTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayer.class));
        
        try
        {
        	playersTable.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}
   
    private static void loadTableData(DynamoDbTable<DynamoPlayer> playerTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
         
        List<Player> playerList = readFromFileAndConvert(inputStream);
        
        if (playerList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
        }
        else
        {
        	for (int i = 0; i < playerList.size(); i++) 
    		{
            	Player obj = playerList.get(i);
            	
            	DynamoPlayer dynamoObj = new DynamoPlayer();
            	
            	dynamoObj.setPlayerID(UUID.randomUUID().toString());
            	            
            	playerTable.putItem(dynamoObj);                
    		}             
        }
        
	}
    
    private static List<Player> readFromFileAndConvert(InputStream inputStream) throws Exception
    {
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	Player[] playerArray = new Gson().fromJson(reader, Player[].class);
       	List<Player> playerList = Arrays.asList(playerArray);
       	return playerList;        
    }
    
    private static DynamoDbTable<DynamoPlayer> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoPlayer> playersTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayer.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	ArrayList<EnhancedGlobalSecondaryIndex> gsindices = new ArrayList<>();
        	
        	EnhancedGlobalSecondaryIndex gameIDGSI = EnhancedGlobalSecondaryIndex.builder()
        			.indexName("gsi_Username")
        			.projection(p -> p.projectionType(ProjectionType.ALL))
        			.build();
        	gsindices.add(gameIDGSI);
        	
        	EnhancedGlobalSecondaryIndex oldTeeTimeIDGSI = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("gsi_OldPlayerID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .build();
        	gsindices.add(oldTeeTimeIDGSI);
             	
        	playersTable.createTable(r -> r.globalSecondaryIndices(gsindices).build());	    
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
        
        return playersTable;
    }

	
   
}
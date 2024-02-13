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

import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbIndex;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.enhanced.dynamodb.model.Page;
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDB_TeeTimes
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Games.class);
	private static String AWS_TABLE_NAME = "teetimes";
	
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME_GAMES = "games";
	
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
        DynamoDbTable<DynamoTeeTime> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        //need the games table to look up game ids
        gamesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_GAMES, TableSchema.fromBean(DynamoGame.class));
        
        loadTableData(table, inputStream);	
		
	}
       
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoTeeTime> teetimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoTeeTime.class));
        
        try
        {
        	teetimesTable.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}
   
    private static void loadTableData(DynamoDbTable<DynamoTeeTime> teetimeTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
        
        List<DynamoTeeTime> teetimeList = readFromFileAndConvert(inputStream);
        
        DynamoDbIndex<DynamoGame> gamesGSI = gamesTable.index("gsi_OldGameID");
        
        if (teetimeList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
        }
        else
        {
        	for (int i = 0; i < teetimeList.size(); i++) 
    		{
            	DynamoTeeTime dtt = teetimeList.get(i);
              	
            	Key key = Key.builder().partitionValue(dtt.getOldGameID()).build();
            	QueryConditional qc = QueryConditional.keyEqualTo(key);
            	
            	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoGame>> gamesByOldGameID = gamesGSI.query(qer);
            	     
            	PageIterable<DynamoGame> pages = PageIterable.create(gamesByOldGameID);
            	
            	List<DynamoGame> dtList = pages.items().stream().toList();
            	String gameID = "";
            	if (dtList != null && dtList.size() > 0)
            	{
            		DynamoGame dt = dtList.get(0);
            		gameID = dt.getGameID();
            	}
            
             	dtt.setGameID(gameID);
             	dtt.setTeeTimeID(UUID.randomUUID().toString());
               	
                teetimeTable.putItem(dtt);                
    		}             
        }
        
	}
    
    private static List<DynamoTeeTime> readFromFileAndConvert(InputStream inputStream) throws Exception 
    {
       Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       DynamoTeeTime[] teetimeArray = new Gson().fromJson(reader, DynamoTeeTime[].class);
       List<DynamoTeeTime> teetimeList = Arrays.asList(teetimeArray);
       return teetimeList;       
    }
    
    private static DynamoDbTable<DynamoTeeTime> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoTeeTime> teetimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoTeeTime.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	ArrayList<EnhancedGlobalSecondaryIndex> gsindices = new ArrayList<>();
        	
        	EnhancedGlobalSecondaryIndex gameIDGSI = EnhancedGlobalSecondaryIndex.builder()
        			.indexName("gsi_GameID")
        			.projection(p -> p.projectionType(ProjectionType.ALL))
        			.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
        			.build();
        	gsindices.add(gameIDGSI);
        	
        	EnhancedGlobalSecondaryIndex oldTeeTimeIDGSI = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("gsi_oldTeeTimeID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .build();
        	gsindices.add(oldTeeTimeIDGSI);
        	
        	EnhancedGlobalSecondaryIndex oldGameIDGSI = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("gsi_oldGameID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .build();
        	gsindices.add(oldGameIDGSI);
        	
        	teetimesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .globalSecondaryIndices(gsindices).build());	        
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
        
        return teetimesTable;
    }

	
   
}
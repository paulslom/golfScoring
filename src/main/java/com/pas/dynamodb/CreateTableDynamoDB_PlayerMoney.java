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

public class CreateTableDynamoDB_PlayerMoney
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_PlayerMoney.class);
	private static String AWS_TABLE_NAME = "playermoney";
	
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME_PLAYERS = "players";
	
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
        DynamoDbTable<DynamoPlayerMoney> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        //need the these tables to look up ids
        playersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_PLAYERS, TableSchema.fromBean(DynamoPlayer.class));
        gamesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_GAMES, TableSchema.fromBean(DynamoGame.class));
        
        loadTableData(table, inputStream);			
	}
	    
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoPlayerMoney> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerMoney.class));
        
        try
        {
        	table.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}
   
    private static void loadTableData(DynamoDbTable<DynamoPlayerMoney> playerMoneyTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
        
        List<DynamoPlayerMoney> playerMoneyList = readFromFileAndConvert(inputStream);
        
        DynamoDbIndex<DynamoPlayer> playersGSI = playersTable.index("gsi_OldPlayerID");
        DynamoDbIndex<DynamoGame> gamesGSI = gamesTable.index("gsi_OldGameID");
        
        if (playerMoneyList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
        }
        else
        {
        	for (int i = 0; i < playerMoneyList.size(); i++) 
    		{
            	DynamoPlayerMoney dtt = playerMoneyList.get(i);
              	
            	dtt.setPlayerMoneyID(UUID.randomUUID().toString());
            	
            	Key key = Key.builder().partitionValue(dtt.getOldPlayerID()).build();
            	QueryConditional qc = QueryConditional.keyEqualTo(key);
            	
            	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoPlayer>> playersByOldPlayerID = playersGSI.query(qer);
            	     
            	PageIterable<DynamoPlayer> pages = PageIterable.create(playersByOldPlayerID);
            	
            	List<DynamoPlayer> dtList = pages.items().stream().toList();
            	
            	if (dtList != null && dtList.size() > 0)
            	{
            		DynamoPlayer dt = dtList.get(0);
            		dtt.setPlayerID(dt.getPlayerID());
            	} 
            	else
            	{
            		System.err.println("Player ID will be null on this one! OldPlayerID = " + dtt.getOldPlayerID());
            	}
             	
             	key = Key.builder().partitionValue(dtt.getOldGameID()).build();
            	qc = QueryConditional.keyEqualTo(key);
            	
            	qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoGame>> gamesByCourseID = gamesGSI.query(qer);
            	     
            	PageIterable<DynamoGame> pagesGame = PageIterable.create(gamesByCourseID);
            	
            	List<DynamoGame> gamesList = pagesGame.items().stream().toList();
            	
            	if (gamesList != null && gamesList.size() > 0)
            	{
            		DynamoGame dc = gamesList.get(0);
            		dtt.setGameID(dc.getGameID());
            	}
            	
                playerMoneyTable.putItem(dtt);
                
    		}             
        }
        
	}
    
    private static List<DynamoPlayerMoney> readFromFileAndConvert(InputStream inputStream) 
    {
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	DynamoPlayerMoney[] dynamoPlayerTeePreferenceArray = new Gson().fromJson(reader, DynamoPlayerMoney[].class);
       	List<DynamoPlayerMoney> tempList = Arrays.asList(dynamoPlayerTeePreferenceArray);
       	return tempList;        
    }
    
    private static DynamoDbTable<DynamoPlayerMoney> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoPlayerMoney> playerMoneyTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerMoney.class));
        
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
                    .indexName("gsi_PlayerID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .build();
        	gsindices.add(oldTeeTimeIDGSI);
               	
        	playerMoneyTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
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
        
        return playerMoneyTable;
    }

	
   
}
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

public class CreateTableDynamoDB_Rounds
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Rounds.class);
	private static String AWS_TABLE_NAME = "rounds";
	
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME_PLAYERS = "players";
	
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME_GAMES = "games";
	
	private static DynamoDbTable<DynamoTeeTime> teeTimesTable;
	private static final String AWS_TABLE_NAME_TEETIMES = "teetimes";
	
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME_COURSETEES = "coursetees";
	
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
        DynamoDbTable<DynamoRound> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        //need the these tables to look up ids
        playersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_PLAYERS, TableSchema.fromBean(DynamoPlayer.class));
        gamesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_GAMES, TableSchema.fromBean(DynamoGame.class));
        teeTimesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_TEETIMES, TableSchema.fromBean(DynamoTeeTime.class));
        courseTeesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_COURSETEES, TableSchema.fromBean(DynamoCourseTee.class));
        
        loadTableData(table, inputStream);		
		
	}
   
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient) throws Exception
    {
    	DynamoDbTable<DynamoRound> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoRound.class));
       	table.deleteTable();
	}
   
    private static void loadTableData(DynamoDbTable<DynamoRound> roundsTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
          
        List<DynamoRound> roundsList = readFromFileAndConvert(inputStream);
        
        DynamoDbIndex<DynamoPlayer> playersGSI = playersTable.index("gsi_OldPlayerID");
        DynamoDbIndex<DynamoGame> gamesGSI = gamesTable.index("gsi_OldGameID");
        DynamoDbIndex<DynamoTeeTime> teeTimesGSI = teeTimesTable.index("gsi_oldTeeTimeID");
        DynamoDbIndex<DynamoCourseTee> courseTeesGSI = courseTeesTable.index("gsi_OldCourseTeeID");
        
        if (roundsList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
        }
        else
        {
        	for (int i = 0; i < roundsList.size(); i++) 
    		{
            	DynamoRound dr = roundsList.get(i);
              	
            	dr.setRoundID(UUID.randomUUID().toString());
            	
            	//PlayerID
            	
            	Key key = Key.builder().partitionValue(dr.getOldPlayerID()).build();
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
            		dr.setPlayerID(dt.getPlayerID());
            		dr.setPlayerName(dt.getFirstName() + " " + dt.getLastName());
            		dr.setPlayerHandicapIndex(dt.getHandicap());
            	} 
            	else
            	{
            		logger.error("Player ID will be null on this one! OldPlayerID = " + dr.getOldPlayerID());
            	}
             	
            	//GameID
            	
             	key = Key.builder().partitionValue(dr.getOldGameID()).build();
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
            		dr.setGameID(dc.getGameID());
            	}
            	else
            	{
            		logger.error("Game ID will be null on this one! OldGameID = " + dr.getOldGameID());
            	}
            	
            	//CourseTeeID
            	key = Key.builder().partitionValue(dr.getOldCourseTeeID()).build();
            	qc = QueryConditional.keyEqualTo(key);
            	
            	qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoCourseTee>> ctid = courseTeesGSI.query(qer);
            	     
            	PageIterable<DynamoCourseTee> pagesct = PageIterable.create(ctid);
            	
            	List<DynamoCourseTee> courseTeesList = pagesct.items().stream().toList();
            	
            	if (courseTeesList != null && courseTeesList.size() > 0)
            	{
            		DynamoCourseTee dc = courseTeesList.get(0);
            		dr.setCourseTeeID(dc.getCourseTeeID());
            		dr.setCourseTeeColor(dc.getTeeColor());
            	}
            	
            	//TeeTimeID
            	key = Key.builder().partitionValue(dr.getOldTeeTimeID()).build();
            	qc = QueryConditional.keyEqualTo(key);
            	
            	qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoTeeTime>> dtID = teeTimesGSI.query(qer);
            	     
            	PageIterable<DynamoTeeTime> pagesTeeTime = PageIterable.create(dtID);
            	
            	List<DynamoTeeTime> teeTimesList = pagesTeeTime.items().stream().toList();
            	
            	if (teeTimesList != null && teeTimesList.size() > 0)
            	{
            		DynamoTeeTime dt = teeTimesList.get(0);
            		dr.setTeeTimeID(dt.getTeeTimeID());
            	}
            	else
            	{
            		logger.error("TeeTime ID will be null on this one! OldTeeTimeID = " + dr.getOldTeeTimeID());
            	}
            
            	roundsTable.putItem(dr);               
    		}             
        }
        
	}
    
    private static List<DynamoRound> readFromFileAndConvert(InputStream inputStream) 
    {
    	Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	DynamoRound[] dynamoPlayerTeePreferenceArray = new Gson().fromJson(reader, DynamoRound[].class);
        List<DynamoRound> tempList = Arrays.asList(dynamoPlayerTeePreferenceArray);
        return tempList;      
    }
    
    private static DynamoDbTable<DynamoRound> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoRound> teetimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoRound.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	teetimesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                                    .indexName("gsi_GameID")
                                                    .projection(p -> p.projectionType(ProjectionType.ALL))
                                                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                                                    .build()));
	        
        }
        catch (ResourceInUseException riue)
        {
        	logger.error("Table already exists! " + riue.getMessage());
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
            logger.info(AWS_TABLE_NAME + " table was created.");
        }        
        
        return teetimesTable;
    }
   
}
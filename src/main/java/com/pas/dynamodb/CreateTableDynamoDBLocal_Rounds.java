package com.pas.dynamodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.google.gson.Gson;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
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
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDBLocal_Rounds
{	 
	private static String AWS_JSON_FILE_NAME = "RoundsData.json";
	private static String AWS_TABLE_NAME = "rounds";
	
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME_PLAYERS = "players";
	
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME_GAMES = "games";
	
	private static DynamoDbTable<DynamoTeeTime> teeTimesTable;
	private static final String AWS_TABLE_NAME_TEETIMES = "teetimes";
	
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME_COURSETEES = "coursetees";
	
    public static void main(String[] args) 
    {
        try 
        {
        	String AWS_REGION = args[0];
        	String uri = args[1];
            
            DynamoDbClient ddbClient =  DynamoDbClient.builder()
            		.endpointOverride(URI.create(uri))
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    .build();
            
            //  Create a client and connect to DynamoDB Local, using an instance of the standard client.
            DynamoDbEnhancedClient ddbEnhancedClient = DynamoDbEnhancedClient.builder()
                    .dynamoDbClient(ddbClient)                           
                    .build();
            
            //Delete the table in DynamoDB Local if it exists
            deleteTable(ddbEnhancedClient);
            
            // Create a table in DynamoDB Local
            DynamoDbTable<DynamoRound> teetimeTable = createTable(ddbEnhancedClient, ddbClient);

            //need the these tables to look up ids
            playersTable = ddbEnhancedClient.table(AWS_TABLE_NAME_PLAYERS, TableSchema.fromBean(DynamoPlayer.class));
            gamesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_GAMES, TableSchema.fromBean(DynamoGame.class));
            teeTimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_TEETIMES, TableSchema.fromBean(DynamoTeeTime.class));
            courseTeesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_COURSETEES, TableSchema.fromBean(DynamoCourseTee.class));
            
            loadTableData(teetimeTable);
            
            scan(teetimeTable);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        //System.exit(1);
    }
    
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoRound> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoRound.class));
        
        try
        {
        	table.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}

	private static void scan(DynamoDbTable<DynamoRound> roundsTable) 
    {
        try 
        {
            Iterator<DynamoRound> results = roundsTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoRound rec = results.next();
                System.out.println("ID = " + rec.getRoundID() + " .. playerID = " + rec.getPlayerID());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
    private static void loadTableData(DynamoDbTable<DynamoRound> roundsTable) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();        
        
        List<DynamoRound> roundsList = readFromFileAndConvert();
        
        DynamoDbIndex<DynamoPlayer> playersGSI = playersTable.index("gsi_OldPlayerID");
        DynamoDbIndex<DynamoGame> gamesGSI = gamesTable.index("gsi_OldGameID");
        DynamoDbIndex<DynamoTeeTime> teeTimesGSI = teeTimesTable.index("gsi_oldTeeTimeID");
        DynamoDbIndex<DynamoCourseTee> courseTeesGSI = courseTeesTable.index("gsi_OldCourseTeeID");
        
        if (roundsList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
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
            		System.err.println("Player ID will be null on this one! OldPlayerID = " + dr.getOldPlayerID());
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
            		System.err.println("Game ID will be null on this one! OldGameID = " + dr.getOldGameID());
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
            		System.err.println("TeeTime ID will be null on this one! OldTeeTimeID = " + dr.getOldTeeTimeID());
            	}
            	
                try 
                {
                	roundsTable.putItem(dr);
                } 
                catch (ResourceNotFoundException e) 
                {
                    System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", AWS_TABLE_NAME);
                    System.err.println("Be sure that it exists and that you've typed its name correctly!");
                    System.exit(1);
                } 
                catch (DynamoDbException e) 
                {
                    System.err.println(e.getMessage());
                    System.exit(1);
                }
    		}             
        }
        
	}
    
    private static List<DynamoRound> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	DynamoRound[] dynamoPlayerTeePreferenceArray = new Gson().fromJson(reader, DynamoRound[].class);
        	List<DynamoRound> tempList = Arrays.asList(dynamoPlayerTeePreferenceArray);
        	return tempList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file " + AWS_JSON_FILE_NAME + exception.getMessage());
        }
        return null;
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
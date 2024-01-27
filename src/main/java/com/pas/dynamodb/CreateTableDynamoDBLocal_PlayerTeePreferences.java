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

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
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

public class CreateTableDynamoDBLocal_PlayerTeePreferences
{	 
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	private static String AWS_REGION = "us-east-1";
	private static String AWS_JSON_FILE_NAME = "PlayerTeesData.json";
	private static String AWS_TABLE_NAME = "playerteepreferences";
	private static String AWS_DYNAMODB_LOCAL_DB_LOCATION = "C:/Paul/DynamoDB";
	
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME_PLAYERS = "players";
	
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME_COURSES = "courses";
	
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME_COURSETEES = "coursetees";
	
    public static void main(String[] args) 
    {
        try 
        {
            System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-port", AWS_DYNAMODB_LOCAL_PORT, "-sharedDb", "-dbPath", AWS_DYNAMODB_LOCAL_DB_LOCATION};
            System.out.println("Starting DynamoDB Local...");
            DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            
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
            DynamoDbTable<DynamoPlayerTeePreference> teetimeTable = createTable(ddbEnhancedClient, ddbClient);

            //need the these tables to look up ids
            playersTable = ddbEnhancedClient.table(AWS_TABLE_NAME_PLAYERS, TableSchema.fromBean(DynamoPlayer.class));
            coursesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_COURSES, TableSchema.fromBean(DynamoCourse.class));
            courseTeesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_COURSETEES, TableSchema.fromBean(DynamoCourseTee.class));
            
            loadTableData(teetimeTable);
            
            scan(teetimeTable);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        System.exit(1);
    }
    
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<DynamoPlayerTeePreference> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerTeePreference.class));
        
        try
        {
        	table.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}

	private static void scan(DynamoDbTable<DynamoPlayerTeePreference> teetimeTable) 
    {
        try 
        {
            Iterator<DynamoPlayerTeePreference> results = teetimeTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoPlayerTeePreference rec = results.next();
                System.out.println("ID = " + rec.getPlayerTeePreferenceID() + " .. playerID = " + rec.getPlayerID());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
    private static void loadTableData(DynamoDbTable<DynamoPlayerTeePreference> playerTeePreferenceTable) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();        
        
        List<DynamoPlayerTeePreference> playerTeePreferenceList = readFromFileAndConvert();
        
        DynamoDbIndex<DynamoPlayer> playersGSI = playersTable.index("gsi_OldPlayerID");
        DynamoDbIndex<DynamoCourse> coursesGSI = coursesTable.index("gsi_OldCourseID");
        DynamoDbIndex<DynamoCourseTee> courseTeesGSI = courseTeesTable.index("gsi_OldCourseTeeID");
        
        if (playerTeePreferenceList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
        }
        else
        {
        	for (int i = 0; i < playerTeePreferenceList.size(); i++) 
    		{
            	DynamoPlayerTeePreference dtt = playerTeePreferenceList.get(i);
              	
            	dtt.setPlayerTeePreferenceID(UUID.randomUUID().toString());
            	
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
            		dtt.setPlayerUserName(dt.getUsername());
            		dtt.setPlayerFullName(dt.getFirstName() + " " + dt.getLastName());
            	} 
            	else
            	{
            		System.err.println("Player ID will be null on this one! OldPlayerID = " + dtt.getOldPlayerID());
            	}
             	
             	key = Key.builder().partitionValue(dtt.getOldCourseID()).build();
            	qc = QueryConditional.keyEqualTo(key);
            	
            	qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoCourse>> coursesByCourseID = coursesGSI.query(qer);
            	     
            	PageIterable<DynamoCourse> pagesCourse = PageIterable.create(coursesByCourseID);
            	
            	List<DynamoCourse> courseList = pagesCourse.items().stream().toList();
            	
            	if (courseList != null && courseList.size() > 0)
            	{
            		DynamoCourse dc = courseList.get(0);
            		dtt.setCourseID(dc.getCourseID());
            		dtt.setCourseName(dc.getCourseName());
            	}
            	
            	key = Key.builder().partitionValue(dtt.getOldCourseTeeID()).build();
            	qc = QueryConditional.keyEqualTo(key);
            	
            	qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoCourseTee>> courseTeesByCourseTeeID = courseTeesGSI.query(qer);
            	     
            	PageIterable<DynamoCourseTee> pagesCourseTee = PageIterable.create(courseTeesByCourseTeeID);
            	
            	List<DynamoCourseTee> courseTeesList = pagesCourseTee.items().stream().toList();
            	
            	if (courseTeesList != null && courseTeesList.size() > 0)
            	{
            		DynamoCourseTee dct = courseTeesList.get(0);
            		dtt.setCourseTeeID(dct.getCourseTeeID());
            		dtt.setTeeColor(dct.getTeeColor());
            	}
            	
                try 
                {
                	playerTeePreferenceTable.putItem(dtt);
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
    
    private static List<DynamoPlayerTeePreference> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	DynamoPlayerTeePreference[] dynamoPlayerTeePreferenceArray = new Gson().fromJson(reader, DynamoPlayerTeePreference[].class);
        	List<DynamoPlayerTeePreference> tempList = Arrays.asList(dynamoPlayerTeePreferenceArray);
        	return tempList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file " + AWS_JSON_FILE_NAME + exception.getMessage());
        }
        return null;
    }
    
    private static DynamoDbTable<DynamoPlayerTeePreference> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoPlayerTeePreference> teetimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerTeePreference.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	teetimesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                                    .indexName("gsi_OldPlayerID")
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
            
            DescribeTableResponse tableDescription = response.response().orElseThrow(
                    () -> new RuntimeException(AWS_TABLE_NAME + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(AWS_TABLE_NAME + " table was created.");
        }        
        
        return teetimesTable;
    }
   
}
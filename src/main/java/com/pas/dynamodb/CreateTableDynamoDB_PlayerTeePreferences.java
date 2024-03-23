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

public class CreateTableDynamoDB_PlayerTeePreferences
{	 
	private static Logger logger = LogManager.getLogger(CreateTableDynamoDB_Players.class);
	private static String AWS_TABLE_NAME = "playerteepreferences";
		
	private static DynamoDbTable<DynamoPlayer> playersTable;
	private static final String AWS_TABLE_NAME_PLAYERS = "players";
	
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME_COURSES = "courses";
	
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
        DynamoDbTable<DynamoPlayerTeePreference> table = createTable(dynamoClients.getDynamoDbEnhancedClient(), dynamoClients.getDdbClient());           

        //need the these tables to look up ids
        playersTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_PLAYERS, TableSchema.fromBean(DynamoPlayer.class));
        coursesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_COURSES, TableSchema.fromBean(DynamoCourse.class));
        courseTeesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME_COURSETEES, TableSchema.fromBean(DynamoCourseTee.class));
        
        loadTableData(table, inputStream);			
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
   
    private static void loadTableData(DynamoDbTable<DynamoPlayerTeePreference> playerTeePreferenceTable, InputStream inputStream) throws Exception
    {   
        // Insert data into the table
        logger.info("Inserting data into the table:" + AWS_TABLE_NAME);
         
        List<DynamoPlayerTeePreference> playerTeePreferenceList = readFromFileAndConvert(inputStream);
        
        DynamoDbIndex<DynamoPlayer> playersGSI = playersTable.index("gsi_OldPlayerID");
        DynamoDbIndex<DynamoCourse> coursesGSI = coursesTable.index("gsi_OldCourseID");
        DynamoDbIndex<DynamoCourseTee> courseTeesGSI = courseTeesTable.index("gsi_OldCourseTeeID");
        
        if (playerTeePreferenceList == null)
        {
        	logger.error("list from json file is Empty - can't do anything more so exiting");
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
            		logger.error("Player ID will be null on this one! OldPlayerID = " + dtt.getOldPlayerID());
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
            	
                playerTeePreferenceTable.putItem(dtt);
                
    		}             
        }
        
	}
    
    private static List<DynamoPlayerTeePreference> readFromFileAndConvert(InputStream inputStream) 
    {
    	Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
       	DynamoPlayerTeePreference[] dynamoPlayerTeePreferenceArray = new Gson().fromJson(reader, DynamoPlayerTeePreference[].class);
       	List<DynamoPlayerTeePreference> tempList = Arrays.asList(dynamoPlayerTeePreferenceArray);
       	return tempList;       
    }
    
    private static DynamoDbTable<DynamoPlayerTeePreference> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoPlayerTeePreference> teetimesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerTeePreference.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	teetimesTable.createTable(r -> r.globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                                    .indexName("gsi_OldPlayerID")
                                                    .projection(p -> p.projectionType(ProjectionType.ALL))
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
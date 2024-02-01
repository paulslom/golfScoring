package com.pas.dynamodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
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

public class CreateTableDynamoDBLocal_Games
{	 
	private static String AWS_JSON_FILE_NAME = "GamesData.json";
	private static String AWS_TABLE_NAME = "games";
		
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME_COURSES = "courses";
	
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
            DynamoDbTable<DynamoGame> table = createTable(ddbEnhancedClient, ddbClient);

            //need the courses table to look up course ids
            coursesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_COURSES, TableSchema.fromBean(DynamoCourse.class));
        
            loadTableData(table);            
              
            scan(table);            
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
    	DynamoDbTable<DynamoGame> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGame.class));
        
        try
        {
        	table.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
	}

	private static void scan(DynamoDbTable<DynamoGame> table) 
    {
        try 
        {
            Iterator<DynamoGame> results = table.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoGame rec = results.next();
                System.out.println("game ID = " + rec.getGameID() + "  course id = " + rec.getCourseID()
                	+ " .. game date = " + rec.getGameDate());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
    private static void loadTableData(DynamoDbTable<DynamoGame> table) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();         
       
        List<DynamoGame> dynamoGameList = readFromFileAndConvert();
       	
    	//look up the new course id in the courses table, using the oldCourseID int field obj.getOldCourseID()
    	
    	DynamoDbIndex<DynamoCourse> coursesGSI = coursesTable.index("gsi_OldCourseID");

        if (dynamoGameList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
        }
        else
        {
        	for (int i = 0; i < dynamoGameList.size(); i++) 
    		{
            	DynamoGame dygm = dynamoGameList.get(i);            	
                    
            	Key key = Key.builder().partitionValue(dygm.getOldCourseID()).build();
            	QueryConditional qc = QueryConditional.keyEqualTo(key);
            	
            	QueryEnhancedRequest qer = QueryEnhancedRequest.builder()
                        .queryConditional(qc)
                        .build();
            	SdkIterable<Page<DynamoCourse>> coursesByOldCourseID = coursesGSI.query(qer);
            	     
            	PageIterable<DynamoCourse> pages = PageIterable.create(coursesByOldCourseID);
            	
            	List<DynamoCourse> dcList = pages.items().stream().toList();
            	String courseID = "";
            	if (dcList != null && dcList.size() > 0)
            	{
            		DynamoCourse dc = dcList.get(0);
            		courseID = dc.getCourseID();
            	}
            	
            	dygm.setCourseID(courseID);
              	dygm.setGameID(UUID.randomUUID().toString());
                        	
                try 
                {
                    table.putItem(dygm);
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
    
    private static List<DynamoGame> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	DynamoGame[] gameArray = new Gson().fromJson(reader, DynamoGame[].class);
        	List<DynamoGame> gameList = Arrays.asList(gameArray);
        	return gameList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file " + AWS_JSON_FILE_NAME + exception.getMessage());
        }
        return null;
    }
    
    private static DynamoDbTable<DynamoGame> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient) 
    {
        DynamoDbTable<DynamoGame> gamesTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGame.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
        	ArrayList<EnhancedGlobalSecondaryIndex> gsindices = new ArrayList<>();
        	
        	EnhancedGlobalSecondaryIndex gsi1 = EnhancedGlobalSecondaryIndex.builder()
        			.indexName("gsi_GameDate")
        			.projection(p -> p.projectionType(ProjectionType.ALL))
        			.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
        			.build();
        	gsindices.add(gsi1);
        	
        	EnhancedGlobalSecondaryIndex gsi2 = EnhancedGlobalSecondaryIndex.builder()
                    .indexName("gsi_OldGameID")
                    .projection(p -> p.projectionType(ProjectionType.ALL))
                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .build();
        	gsindices.add(gsi2);       	
        	  	
        	gamesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
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
        
        return gamesTable;
    }
   
}
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

public class CreateTableDynamoDBLocal_TeeTimes
{	 
	private static String AWS_JSON_FILE_NAME = "TeeTimesData.json";
	private static String AWS_TABLE_NAME = "teetimes";
	
	private static DynamoDbTable<DynamoGame> gamesTable;
	private static final String AWS_TABLE_NAME_GAMES = "games";
	
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
            DynamoDbTable<DynamoTeeTime> teetimeTable = createTable(ddbEnhancedClient, ddbClient);

            //need the games table to look up game ids
            gamesTable = ddbEnhancedClient.table(AWS_TABLE_NAME_GAMES, TableSchema.fromBean(DynamoGame.class));
            
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

	private static void scan(DynamoDbTable<DynamoTeeTime> teetimeTable) 
    {
        try 
        {
            Iterator<DynamoTeeTime> results = teetimeTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoTeeTime rec = results.next();
                System.out.println("ID = " + rec.getTeeTimeID() + " .. tee time string = " + rec.getTeeTimeString());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
    private static void loadTableData(DynamoDbTable<DynamoTeeTime> teetimeTable) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();        
        
        List<DynamoTeeTime> teetimeList = readFromFileAndConvert();
        
        DynamoDbIndex<DynamoGame> gamesGSI = gamesTable.index("gsi_OldGameID");
        
        if (teetimeList == null)
        {
        	System.err.println("list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
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
               	
                try 
                {
                    teetimeTable.putItem(dtt);
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
    
    private static List<DynamoTeeTime> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	DynamoTeeTime[] teetimeArray = new Gson().fromJson(reader, DynamoTeeTime[].class);
        	List<DynamoTeeTime> teetimeList = Arrays.asList(teetimeArray);
        	return teetimeList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file " + AWS_JSON_FILE_NAME + exception.getMessage());
        }
        return null;
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
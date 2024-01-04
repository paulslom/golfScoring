package com.pas.dynamodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.google.gson.Gson;
import com.pas.beans.GolfUser;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.GetItemEnhancedRequest;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDBLocal
{	 
	private static String AWS_DYNAMODB_LOCAL_PORT;
	private static String AWS_REGION;
	private static String AWS_JSON_FILE_NAME;
	private static String AWS_TABLE_NAME;
	private static String AWS_KEY_NAME;
   
	private static String TABLE_PROPERTIES = "golfUsersDynamoDB.properties";

    public static void main(String[] args) 
    {
        try 
        {
        	getProperties();
        	
            System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an in-memory and in-process instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-port", AWS_DYNAMODB_LOCAL_PORT, "-sharedDb", "-dbPath", "C:/Paul/DynamoDB"};
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
            
            // Create a table in DynamoDB Local with table name Music and partition key Artist
            // Understanding core components of DynamoDB: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html
            DynamoDbTable<GolfUser> golfUserTable = createTable(ddbEnhancedClient, ddbClient, AWS_TABLE_NAME, AWS_KEY_NAME);

            //  List all the tables in DynamoDB Local

            System.out.println("Listing tables in DynamoDB Local...");
            System.out.println("-------------------------------");
            ListTablesResponse listTablesResponse = ddbClient.listTables();
            System.out.println(listTablesResponse.tableNames());

            loadTableData(golfUserTable);
            
            scan(golfUserTable);
            
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
        
        System.exit(1);
    }
    
    private static void scan(DynamoDbTable<GolfUser> golfUserTable) 
    {
        try 
        {
            Iterator<GolfUser> results = golfUserTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                GolfUser rec = results.next();
                System.out.println("ID = " + rec.getUserId() + " .. user name = " + rec.getUserName() + " .. role = " + rec.getUserRole());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
    private static void getProperties()
	{
		try 
	    {	
			Properties prop = new Properties();
			
	    	InputStream stream = new FileInputStream(new File("C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\dynamoDb.properties")); 
	    	prop.load(stream);   		
		 	
		    AWS_REGION = prop.getProperty("region");
		    AWS_DYNAMODB_LOCAL_PORT = prop.getProperty("local_port");		   
			
	    	InputStream stream2 = new FileInputStream(new File("C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\" + TABLE_PROPERTIES)); 
	    	prop.load(stream2);   		
		 	
	    	AWS_JSON_FILE_NAME = prop.getProperty("jsonFileName");
	    	AWS_TABLE_NAME = prop.getProperty("tableName");
	        AWS_KEY_NAME = prop.getProperty("keyName");	        
		 }
		 catch (Exception e) 
	     { 
		    System.out.println(e.toString());
		 }     		
	   	
	}

    private static void loadTableData(DynamoDbTable<GolfUser> golfUserTable) throws Exception
    {   
        // Insert data into the table
        System.out.println();
        System.out.println("Inserting data into the table:" + AWS_TABLE_NAME);
        System.out.println();        
        
        List<GolfUser> golfUserList = readFromFileAndConvert();
        
        if (golfUserList == null)
        {
        	System.err.println("golf user list from json file is Empty - can't do anything more so exiting");
            System.exit(1);
        }
        else
        {
        	for (int i = 0; i < golfUserList.size(); i++) 
    		{
            	GolfUser gu = golfUserList.get(i);
            	
                try 
                {
                    golfUserTable.putItem(gu);
                    
                    // Get data from the table
                    System.out.println("Getting Item from the table for key after putItem: " + AWS_KEY_NAME);
                    System.out.println("-------------------------------");
                                        
                    Key key = Key.builder().partitionValue(gu.getUserName()).sortValue(gu.getUserRole()).build();
                    
                    GetItemEnhancedRequest getItemEnhancedRequest = GetItemEnhancedRequest.builder()
                    		.key(key)
                            .consistentRead(true)
                            .build();
                    
                    GolfUser golfUserInserted = golfUserTable.getItem(getItemEnhancedRequest);
                    System.out.println("Successfully retrieved Item from the table for key: " + golfUserInserted.getUserName() 
                    		+ " the id for this is = " + golfUserInserted.getUserId());
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

	
    
    private static List<GolfUser> readFromFileAndConvert() 
    {
    	String jsonFile = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + AWS_JSON_FILE_NAME;
    	
        try (InputStream inputStream = new FileInputStream(new File(jsonFile));
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) 
        {
        	GolfUser[] golfUserArray = new Gson().fromJson(reader, GolfUser[].class);
        	List<GolfUser> golfUserList = Arrays.asList(golfUserArray);
        	return golfUserList;
        } 
        catch (final Exception exception) 
        {
        	System.out.println("Got an exception while reading the json file /data/golfusersdata.json" + exception.getMessage());
        }
        return null;
    }
    
    private static DynamoDbTable<GolfUser> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient, String tableName, String key) 
    {
        DynamoDbTable<GolfUser> golfUsersTable = ddbEnhancedClient.table(tableName, TableSchema.fromBean(GolfUser.class));
        
        // Create the DynamoDB table.  If it exists, it'll throw an exception
        
        try
        {
	        golfUsersTable.createTable(builder -> builder
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
                    .waitUntilTableExists(builder -> builder.tableName(tableName).build())
                    .matched();
            
            DescribeTableResponse tableDescription = response.response().orElseThrow(
                    () -> new RuntimeException(tableName + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(tableName + " table was created.");
        }        
        
        return golfUsersTable;
    }
    
    public static void getDynamoDBItem(DynamoDbClient ddb, String tableName, String key, String keyVal) 
    {
        HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();

        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal).build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(tableName)
                .consistentRead(true)
                .build();

        try 
        {
            Map<String, AttributeValue> returnedItem = ddb.getItem(request).item();

            if (returnedItem.size() != 0) 
            {
                Set<String> keys = returnedItem.keySet();
                for (String key1 : keys) 
                {
                    System.out.format("%s: %s\n", key1, returnedItem.get(key1).s());
                }
            } 
            else 
            {
                System.out.format("No item found with the key: %s!\n", keyToGet.get(key).s());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    public static void deleteDynamoDBItem(DynamoDbClient ddb, String tableName, String key, String keyVal) 
    {
        HashMap<String, AttributeValue> keyToGet =
                new HashMap<String, AttributeValue>();

        keyToGet.put(key, AttributeValue.builder()
                .s(keyVal)
                .build());

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(tableName)
                .key(keyToGet)
                .build();

        try 
        {
            ddb.deleteItem(deleteReq);
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
    }

    
}
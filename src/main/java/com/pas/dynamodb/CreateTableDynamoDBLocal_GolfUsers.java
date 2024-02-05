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

import com.google.gson.Gson;
import com.pas.beans.GolfUser;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.internal.waiters.ResponseOrException;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.ResourceInUseException;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDBLocal_GolfUsers
{	 
	private static String AWS_JSON_FILE_NAME = "GolfUsersData.json";
	private static String AWS_TABLE_NAME = "golfUsers";
	
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
            DynamoDbTable<GolfUser> golfUserTable = createTable(ddbEnhancedClient, ddbClient, AWS_TABLE_NAME);           

            loadTableData(golfUserTable);
            
            scan(golfUserTable);
            
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
        
        //System.exit(1);
    }
    
    private static void deleteTable(DynamoDbEnhancedClient ddbEnhancedClient)
    {
    	DynamoDbTable<GolfUser> golfUsersTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(GolfUser.class));
        
        try
        {
        	golfUsersTable.deleteTable();
        }
        catch (Exception e)
        {
        	System.out.println("Table did not already exist, so no delete table executed! " + e.getMessage());
        }
		
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
    
    private static DynamoDbTable<GolfUser> createTable(DynamoDbEnhancedClient ddbEnhancedClient, DynamoDbClient ddbClient, String tableName) 
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
            
            response.response().orElseThrow(
                    () -> new RuntimeException(tableName + " was not created."));
            
            // The actual error can be inspected in response.exception()
            System.out.println(tableName + " table was created.");
        }        
        
        return golfUsersTable;
    }
   
}
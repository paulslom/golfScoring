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
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.fasterxml.jackson.databind.introspect.TypeResolutionContext.Empty;
import com.google.gson.Gson;
import com.pas.beans.GolfUser;
import com.pas.util.FileDataLoader;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest;
import software.amazon.awssdk.services.dynamodb.model.CreateTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DeleteItemRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableResponse;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement;
import software.amazon.awssdk.services.dynamodb.model.KeyType;
import software.amazon.awssdk.services.dynamodb.model.ListTablesResponse;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;
import software.amazon.awssdk.services.dynamodb.model.ResourceNotFoundException;
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

public class CreateTableDynamoDBLocal
{
    private static DynamoDBProxyServer server;
    
    private static String AWS_REGION;
    private static String AWS_DYNAMODB_LOCAL_PORT;
     
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
            final String[] localArgs = {"-inMemory", "-port", AWS_DYNAMODB_LOCAL_PORT};
            System.out.println("Starting DynamoDB Local...");
            server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            
            //  Create a client and connect to DynamoDB Local
            DynamoDbClient ddbClient = DynamoDbClient.builder()
                    .endpointOverride(URI.create(uri))
                    .httpClient(UrlConnectionHttpClient.builder().build())
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    //.credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET)))
                    .build();

            // Create a table in DynamoDB Local with table name Music and partition key Artist
            // Understanding core components of DynamoDB: https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/HowItWorks.CoreComponents.html
            createTable(ddbClient, AWS_TABLE_NAME, AWS_KEY_NAME);

            //  List all the tables in DynamoDB Local

            System.out.println("Listing tables in DynamoDB Local...");
            System.out.println("-------------------------------");
            ListTablesResponse listTablesResponse = ddbClient.listTables();
            System.out.println(listTablesResponse.tableNames());

            loadTableData(ddbClient);
            
        } 
        catch (Exception e) 
        {
            throw new RuntimeException(e);
        }
    }
    
    private static void loadTableData(DynamoDbClient ddbClient) throws Exception
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
            	
            	HashMap<String, AttributeValue> itemValues = new HashMap<String, AttributeValue>();

                // Add all content to the table
                itemValues.put("userName", AttributeValue.builder().s(gu.getUserName()).build());
                itemValues.put("password", AttributeValue.builder().s(gu.getPassword()).build());
                itemValues.put("userRole", AttributeValue.builder().s(gu.getUserRole()).build());
                itemValues.put("userId", AttributeValue.builder().s(String.valueOf(gu.getUserId())).build());

                PutItemRequest request = PutItemRequest.builder()
                        .tableName(AWS_TABLE_NAME)
                        .item(itemValues)
                        .build();

                try 
                {
                    ddbClient.putItem(request);
                    
                    // Get data from the table
                    System.out.println("Getting Item from the table for key: " + AWS_KEY_NAME);
                    System.out.println("-------------------------------");
                    getDynamoDBItem(ddbClient, AWS_TABLE_NAME, AWS_KEY_NAME, gu.getUserName());
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
    
    private static String createTable(DynamoDbClient ddb, String tableName, String key) 
    {
        DynamoDbWaiter dbWaiter = ddb.waiter();
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName(key)
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName(key)
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(Long.valueOf(5))
                        .writeCapacityUnits(Long.valueOf(5))
                        .build())
                .tableName(tableName)
                .build();

        String newTable = "";
        
        try 
        {
            CreateTableResponse response = ddb.createTable(request);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName(tableName)
                    .build();

            // Wait until the Amazon DynamoDB table is created
            WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);

            newTable = response.tableDescription().tableName();
            return newTable;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        return "";
    }

    public static void getDynamoDBItem(DynamoDbClient ddb, String tableName, String key, String keyVal) {

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
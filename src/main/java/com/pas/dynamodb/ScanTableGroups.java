package com.pas.dynamodb;

import java.net.URI;
import java.util.Iterator;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class ScanTableGroups
{	 
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	private static String AWS_REGION = "us-east-1";
	private static String AWS_JSON_FILE_NAME = "GolfGroupsData.json";
	private static String AWS_TABLE_NAME = "groups";
	private static String AWS_DYNAMODB_LOCAL_DB_LOCATION = "C:/Paul/DynamoDB";
	
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
            
            DynamoDbTable<DynamoGroup> groupsTable = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGroup.class));

            //  List all the tables in DynamoDB Local          
            scan(groupsTable);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        System.exit(1);
    }
    
	private static void scan(DynamoDbTable<DynamoGroup> groupsTable) 
    {
		System.out.println("These are the contents of the GROUPS table in dynamoDB");
		
        try 
        {
            Iterator<DynamoGroup> results = groupsTable.scan().items().iterator();
            
            while (results.hasNext()) 
            {
                DynamoGroup rec = results.next();
                System.out.println("ID = " + rec.getGroupID() + " .. group name = " + rec.getGroupName());
            }
        } 
        catch (DynamoDbException e) 
        {
            System.err.println(e.getMessage());
            System.exit(1);
        }
        System.out.println("Done with dynamo scan");
    }
   
}
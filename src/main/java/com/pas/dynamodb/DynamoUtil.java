package com.pas.dynamodb;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.pas.util.Utils;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

public class DynamoUtil 
{
	private static Logger logger = LogManager.getLogger(DynamoUtil.class);
	
	private static String AWS_REGION = "us-east-1";
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	
	private static DynamoDBProxyServer server;
	
	private static DynamoClients dynamoClients = null;	

	public static final Long READ_CAPACITY = 10L;
	public static final Long WRITE_CAPACITY = 5L;
	public static final ProvisionedThroughput DEFAULT_PROVISIONED_THROUGHPUT =
	            ProvisionedThroughput.builder().readCapacityUnits(READ_CAPACITY).writeCapacityUnits(WRITE_CAPACITY).build();
	    
	public static DynamoClients getDynamoClients() throws Exception
	{
		if (dynamoClients != null)
		{
			return dynamoClients;
		}
		
		DynamoDbEnhancedClient dynamoDbEnhancedClient;
		DynamoDbClient ddbClient;

		if (Utils.isLocalEnv())
        {
        	logger.info("We are operating in LOCAL env - connecting to DynamoDBLocal");
        	
        	System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-port", AWS_DYNAMODB_LOCAL_PORT, "-sharedDb", "-dbPath", "C:/Paul/DynamoDB"};
            logger.info("Starting DynamoDB Local...");
            
            server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
            
            //  Create a client that will connect to DynamoDB Local
            ddbClient =  DynamoDbClient.builder()
            		.endpointOverride(URI.create(uri))
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    .build();
        }
        else
        {
        	logger.info("We are operating in AWS env - connecting to DynamoDB on AWS");
        	
        	ddbClient =  DynamoDbClient.builder()
                    .region(Region.of(AWS_REGION))
                    .credentialsProvider(ProfileCredentialsProvider.create("default"))
                    .build();
        } 
		
	    //Create a client and connect to DynamoDB, using an instance of the standard client.
        dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder()
                .dynamoDbClient(ddbClient)                           
                .build();
        
        dynamoClients = new DynamoClients();
        dynamoClients.setDdbClient(ddbClient);
        dynamoClients.setDynamoDbEnhancedClient(dynamoDbEnhancedClient);
        
        return dynamoClients;
	}
	
	public static void stopDynamoServer()
	{
		if (Utils.isLocalEnv())
        {
        	logger.info("We are operating in LOCAL env - STOPPING dynamoDB local server");
        	try 
        	{
				server.stop();
			} 
        	catch (Exception e) 
        	{
				logger.error("Unable to stop local dynamo server: " + e.getMessage(), e);
			}
        }
	}
	
}

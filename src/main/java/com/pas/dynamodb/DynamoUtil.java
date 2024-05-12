package com.pas.dynamodb;

import java.net.URI;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
//import com.pas.util.Utils;

import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
//import software.amazon.awssdk.enhanced.dynamodb.model.EnhancedGlobalSecondaryIndex;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
//import software.amazon.awssdk.services.dynamodb.model.ProjectionType;
//import software.amazon.awssdk.services.dynamodb.model.ProvisionedThroughput;

public class DynamoUtil 
{
	private static Logger logger = LogManager.getLogger(DynamoUtil.class);
	
	private static String AWS_REGION = "us-east-1";
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	
	private static DynamoDBProxyServer server;
	
	private static DynamoClients dynamoClients = null;	
	
	//private static final boolean runningOnAWS = false;
    private static final boolean runningOnAWS = true;

	/*
	The maximum number of strongly consistent reads and writes consumed per second before DynamoDB returns a ThrottlingException.
	* For more information, see https://docs.aws.amazon.com/amazondynamodb/latest/developerguide/ProvisionedThroughput.html
    *  If read/write capacity mode is PAY_PER_REQUEST the value is set to 0.
    *  
    *  If you don't specify provisionedThroughput, you get defaults, and per AWS documentation that will use on-demand.  So for 
    *  on-demand let's skip defining provisioned throughput entirely.
    *  
    *  When default settings are used, values for provisioned throughput are not set. 
    *  Instead, the billing mode for the table is set to on-demand.
    *  
    *  Example of a create table with provisioned throughput:
    *  
    *  coursesTable.createTable(r -> r.provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                    .globalSecondaryIndices(
                        EnhancedGlobalSecondaryIndex.builder()
                                                    .indexName("gsi_OldCourseID")
                                                    .projection(p -> p.projectionType(ProjectionType.ALL))
                                                    .provisionedThroughput(DynamoUtil.DEFAULT_PROVISIONED_THROUGHPUT)
                                                    .build()));
    */
	
	//public static final Long READ_CAPACITY = 3L;
	//public static final Long WRITE_CAPACITY = 3L;
	//public static final ProvisionedThroughput DEFAULT_PROVISIONED_THROUGHPUT =
	//            ProvisionedThroughput.builder().readCapacityUnits(READ_CAPACITY).writeCapacityUnits(WRITE_CAPACITY).build();
	    
	public static DynamoClients getDynamoClients() throws Exception
	{
		if (dynamoClients != null)
		{
			return dynamoClients;
		}
		
		DynamoDbEnhancedClient dynamoDbEnhancedClient;
		DynamoDbClient ddbClient;

		if (!runningOnAWS)
        {
        	logger.info("We are operating in LOCAL env - connecting to DynamoDBLocal");
        	
        	//System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
        	System.setProperty("aws.region", "us-east-1");
        	
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
            
            // Create an instance of DynamoDB Local that runs over HTTP
            final String[] localArgs = {"-inMemory", "-port", AWS_DYNAMODB_LOCAL_PORT};
            logger.info("Starting DynamoDB Local...");
            
            server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
               
            //  Create a client that will connect to DynamoDB Local
            ddbClient =  DynamoDbClient.builder()
            		.endpointOverride(URI.create(uri))
            		.credentialsProvider(EnvironmentVariableCredentialsProvider.create())
            		.region(Region.of(AWS_REGION))
                    .build();
        }
        else
        {
        	logger.info("We are operating in AWS env - connecting to DynamoDB on AWS");
        	
        	ddbClient =  DynamoDbClient.builder()
                    .region(Region.of(AWS_REGION))
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
		if (!runningOnAWS)
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

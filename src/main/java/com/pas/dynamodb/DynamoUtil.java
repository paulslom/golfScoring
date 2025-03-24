package com.pas.dynamodb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static DynamoClients dynamoClients = null;	
	
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

		logger.info("connecting to DynamoDB on AWS");
        	
        ddbClient =  DynamoDbClient.builder().region(Region.of(AWS_REGION)).build();
      		
	    //Create a client and connect to DynamoDB, using an instance of the standard client.
        dynamoDbEnhancedClient = DynamoDbEnhancedClient.builder().dynamoDbClient(ddbClient).build();
        
        dynamoClients = new DynamoClients();
        dynamoClients.setDdbClient(ddbClient);
        dynamoClients.setDynamoDbEnhancedClient(dynamoDbEnhancedClient);
        
        return dynamoClients;
	}	
	
}

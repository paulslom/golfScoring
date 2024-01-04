package com.pas.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

public class DynamoClients 
{	
	private DynamoDbEnhancedClient dynamoDbEnhancedClient;
	private DynamoDbClient ddbClient;
	
	public DynamoDbEnhancedClient getDynamoDbEnhancedClient() {
		return dynamoDbEnhancedClient;
	}
	public void setDynamoDbEnhancedClient(DynamoDbEnhancedClient dynamoDbEnhancedClient) {
		this.dynamoDbEnhancedClient = dynamoDbEnhancedClient;
	}
	public DynamoDbClient getDdbClient() {
		return ddbClient;
	}
	public void setDdbClient(DynamoDbClient ddbClient) {
		this.ddbClient = ddbClient;
	}

}

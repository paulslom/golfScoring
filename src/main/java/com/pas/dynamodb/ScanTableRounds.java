package com.pas.dynamodb;

import java.net.URI;
import java.util.Iterator;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class ScanTableRounds
{	 
	private static String AWS_TABLE_NAME = "rounds";
	
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
            
            DynamoDbTable<DynamoRound> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoRound.class));

            showIndexes(AWS_REGION, uri);
            
            scan(table);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        //System.exit(1);
    }
    
    private static void showIndexes(String AWS_REGION, String uri) 
	{
	    //Have to use dynamo V1 to use describe table to see indexes
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder
        			.standard()
        			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(uri, AWS_REGION))
        			.build(); 
        
        DynamoDB dynamoDB = new DynamoDB(client);

        Table tableDescTable = dynamoDB.getTable(AWS_TABLE_NAME);
        com.amazonaws.services.dynamodbv2.model.TableDescription tableDesc = tableDescTable.describe();
        
        Iterator<GlobalSecondaryIndexDescription> gsiIter = tableDesc.getGlobalSecondaryIndexes().iterator();
        
        while (gsiIter.hasNext()) 
        {
            GlobalSecondaryIndexDescription gsiDesc = gsiIter.next();
            System.out.println("Info for index " + gsiDesc.getIndexName() + ":");

            Iterator<KeySchemaElement> kseIter = gsiDesc.getKeySchema().iterator();
            while (kseIter.hasNext()) 
            {
                KeySchemaElement kse = kseIter.next();
                System.out.printf("\t%s: %s\n", kse.getAttributeName(), kse.getKeyType());
            }
        }        
		
	}
    
	private static void scan(DynamoDbTable<DynamoRound> table) 
    {
		System.out.println("These are the contents of the teetimes table in dynamoDB");
			
        try 
        {
            Iterator<DynamoRound> results = table.scan().items().iterator();
          	
            while (results.hasNext()) 
            {
                DynamoRound rec = results.next();
                System.out.println("roundID = " + rec.getRoundID() + " .. gameID = " + rec.getGameID()
                		+ "  teeTimeID = " + rec.getTeeTimeID() + " .. playerID = " + rec.getPlayerID()
                		+ "  courseTeeID = " + rec.getCourseTeeID() + " .. teamNumber = " + rec.getTeamNumber()
                		+ "  front9Total = " + rec.getFront9Total() + " .. back9Total = " + rec.getBack9Total()
                		+ "  totalScore = " + rec.getTotalScore() + " .. totalToPar = " + rec.getTotalToPar()
                		+ "  netScore = " + rec.getNetScore() + " .. signupDateTime = " + rec.getSignupDateTime()
                		+ "  playerName = " + rec.getPlayerName() + " .. roundHandicap = " + rec.getRoundHandicap()
                		+ "  playerHandicapIndex = " + rec.getPlayerHandicapIndex() + " .. courseTeeColor = " + rec.getCourseTeeColor()
                		+ "  roundHandicapDifferential = " + rec.getRoundHandicapDifferential()
                		+ "  hole1Score = " + rec.getHole1Score() + " .. hole2Score = " + rec.getHole2Score()
                		+ "  hole3Score = " + rec.getHole3Score() + " .. hole4Score = " + rec.getHole4Score()
                		+ "  hole5Score = " + rec.getHole5Score() + " .. hole6Score = " + rec.getHole6Score()
                		+ "  hole7Score = " + rec.getHole7Score() + " .. hole8Score = " + rec.getHole8Score()
                		+ "  hole9Score = " + rec.getHole9Score() + " .. hole10Score = " + rec.getHole10Score()
                		+ "  hole11Score = " + rec.getHole11Score() + " .. hole12Score = " + rec.getHole12Score()
                		+ "  hole13Score = " + rec.getHole13Score() + " .. hole14Score = " + rec.getHole14Score()
                		+ "  hole15Score = " + rec.getHole15Score() + " .. hole16Score = " + rec.getHole16Score()
                		+ "  hole17Score = " + rec.getHole17Score() + " .. hole18Score = " + rec.getHole18Score());
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
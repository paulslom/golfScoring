package com.pas.dynamodb;

import java.net.URI;
import java.util.Iterator;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;
import com.amazonaws.services.dynamodbv2.model.GlobalSecondaryIndexDescription;
import com.amazonaws.services.dynamodbv2.model.KeySchemaElement;

import software.amazon.awssdk.auth.credentials.ProfileCredentialsProvider;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.DynamoDbException;

public class ScanTableGames
{	 
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	private static String AWS_REGION = "us-east-1";
	private static String AWS_TABLE_NAME = "games";
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
            
            DynamoDbTable<DynamoGame> table = ddbEnhancedClient.table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoGame.class));

            showIndexes();
           
            //  List all the tables in DynamoDB Local          
            scan(table);
            
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        System.exit(1);
    }
    
	private static void showIndexes() 
	{
		String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
		
		 //Have to use dynamo V1 to use describe table to see indexes
        AmazonDynamoDB client = AmazonDynamoDBClientBuilder
        			.standard()
        			.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(uri, AWS_REGION))
        			.build(); 
        
        DynamoDB dynamoDB = new DynamoDB(client);

        Table tableDescTable = dynamoDB.getTable("games");
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

	private static void scan(DynamoDbTable<DynamoGame> table) 
    {
		System.out.println("These are the contents of the games table in dynamoDB");
				
        try 
        {
            Iterator<DynamoGame> results = table.scan().items().iterator();
          	
            while (results.hasNext()) 
            {
                DynamoGame rec = results.next();
                System.out.println("gameID = " + rec.getGameID() + " .. oldGameID = " + rec.getOldGameID()
                		+ "  courseID = " + rec.getCourseID() + " .. oldCourseID = " + rec.getOldCourseID()
                		+ "  gameDate = " + rec.getGameDate() + " .. fieldSize = " + rec.getFieldSize()
                		+ "  totalPlayers = " + rec.getTotalPlayers() + " .. totalTeams = " + rec.getTotalTeams()
                		+ "  skinsPot = " + rec.getSkinsPot() + " .. teamPot = " + rec.getTeamPot()
                		+ "  betAmount = " + rec.getBetAmount() + " .. howManyBalls = " + rec.getHowManyBalls()
                		+ "  purseAmount = " + rec.getPurseAmount() + " .. eachBallWorth = " + rec.getEachBallWorth()
                		+ "  individualNetPrize = " + rec.getIndividualNetPrize() + " .. playTheBallMethod = " + rec.getPlayTheBallMethod()
                		+ "  individialGrossPrize = " + rec.getIndividualGrossPrize()
                		+ "  gameClosedForSignups = " + rec.isGameClosedForSignups() + " .. gameNoteForEmail = " + rec.getGameNoteForEmail());
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
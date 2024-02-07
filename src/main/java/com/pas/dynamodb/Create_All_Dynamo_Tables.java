package com.pas.dynamodb;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.amazonaws.services.dynamodbv2.local.main.ServerRunner;
import com.amazonaws.services.dynamodbv2.local.server.DynamoDBProxyServer;

public class Create_All_Dynamo_Tables
{
	private static Logger log = LogManager.getLogger(Create_All_Dynamo_Tables.class); //log4j for Logging	 
     
	private static String AWS_DYNAMODB_LOCAL_PORT = "8000";
	private static String AWS_REGION = "us-east-1";
	private static String AWS_DYNAMODB_LOCAL_DB_LOCATION = "C:/Paul/DynamoDB";

    public static void main(String[] args) throws Exception
    { 
    	log.debug("**********  START of program ***********");   	
    	
    	 try 
         {
            System.setProperty("sqlite4java.library.path", "C:\\Paul\\DynamoDB\\DynamoDBLocal_lib");
            String uri = "http://localhost:" + AWS_DYNAMODB_LOCAL_PORT;
             
            final String[] localArgs = {"-port", AWS_DYNAMODB_LOCAL_PORT, "-sharedDb", "-dbPath", AWS_DYNAMODB_LOCAL_DB_LOCATION};
            System.out.println("Starting DynamoDB Local...");
            DynamoDBProxyServer server = ServerRunner.createServerFromCommandLineArgs(localArgs);
            server.start();
    
            String[] callingArgs = {AWS_REGION, uri};
            
            /*  */       
            CreateTableDynamoDBLocal_GolfUsers.main(callingArgs);
	    	CreateTableDynamoDBLocal_Groups.main(callingArgs);
	    	CreateTableDynamoDBLocal_Courses.main(callingArgs);
	    	CreateTableDynamoDBLocal_CourseTees.main(callingArgs);
	    	CreateTableDynamoDBLocal_Games.main(callingArgs);
	    	CreateTableDynamoDBLocal_TeeTimes.main(callingArgs);
	    	CreateTableDynamoDBLocal_Players.main(callingArgs);	
	    	CreateTableDynamoDBLocal_PlayerTeePreferences.main(callingArgs);	    	
	    	CreateTableDynamoDBLocal_PlayerMoney.main(callingArgs);	    	    
	    	CreateTableDynamoDBLocal_Rounds.main(callingArgs);
	    	
	    	server.stop();
	    	
			log.debug("**********  END of program ***********");
         }
    	 catch (Exception e)
    	 {
    		 log.error("Exception in Create_All_Dynamo_Tables " + e.getMessage(), e);
    	 }
		System.exit(1);
	}
		
}
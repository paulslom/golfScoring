package com.pas.dynamodb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Create_All_Dynamo_Tables
{
	private static Logger log = LogManager.getLogger(Create_All_Dynamo_Tables.class); //log4j for Logging 
	
	private static String GOLFUSERS_JSONFILE = "GolfUsersData.json";
	private static String COURSES_JSONFILE = "CoursesData.json";
	private static String GAMES_JSONFILE = "GamesData.json";
	private static String GOLFCOURSETEES_JSONFILE = "GolfCourseTeesData.json";
	private static String PLAYERMONEY_JSONFILE = "PlayerMoneyData.json";
	private static String PLAYERS_JSONFILE = "PlayersData.json";
	private static String PLAYERTEES_JSONFILE = "PlayerTeesData.json";
	private static String ROUNDS_JSONFILE = "RoundsData.json";
	private static String TEETIMES_JSONFILE = "TeeTimesData.json";
	private static String GOLFGROUPS_JSONFILE = "GolfGroupsData.json";
	
    public static void main(String[] args) throws Exception
    { 
    	log.debug("**********  START of program ***********");   	
    	
    	 try 
         {
    		 DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
    
    		 doTable(dynamoClients, GOLFUSERS_JSONFILE);
    		 doTable(dynamoClients, GOLFGROUPS_JSONFILE);
    		 doTable(dynamoClients, COURSES_JSONFILE);
    		 doTable(dynamoClients, GOLFCOURSETEES_JSONFILE);
    		 doTable(dynamoClients, GAMES_JSONFILE);
    		 doTable(dynamoClients, TEETIMES_JSONFILE);
    		 doTable(dynamoClients, PLAYERS_JSONFILE);
    		 doTable(dynamoClients, PLAYERTEES_JSONFILE);
    		 doTable(dynamoClients, PLAYERMONEY_JSONFILE);
    		 doTable(dynamoClients, ROUNDS_JSONFILE);
	       	
	    	 DynamoUtil.stopDynamoServer();
	    	
			 log.debug("**********  END of program ***********");
         }
    	 catch (Exception e)
    	 {
    		 log.error("Exception in Create_All_Dynamo_Tables " + e.getMessage(), e);
    	 }
		System.exit(1);
	}

    private static void doTable(DynamoClients dynamoClients, String jsonFileName) throws Exception 
	{
		String jsonFilePath = "C:\\Paul\\GitHub\\golfScoring\\src\\main\\resources\\data\\" + jsonFileName;
        File jsonFile = new File(jsonFilePath);                    
        InputStream inputStream = new FileInputStream(jsonFile);
    		 
        if (jsonFileName.equalsIgnoreCase(GOLFUSERS_JSONFILE))
        {
        	CreateTableDynamoDB_GolfUsers ct = new CreateTableDynamoDB_GolfUsers();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(GOLFGROUPS_JSONFILE))
        {
        	CreateTableDynamoDB_Groups ct = new CreateTableDynamoDB_Groups();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(COURSES_JSONFILE))
        {
        	CreateTableDynamoDB_Courses ct = new CreateTableDynamoDB_Courses();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(GOLFCOURSETEES_JSONFILE))
        {
        	CreateTableDynamoDB_CourseTees ct = new CreateTableDynamoDB_CourseTees();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(GAMES_JSONFILE))
        {
        	CreateTableDynamoDB_Games ct = new CreateTableDynamoDB_Games();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(TEETIMES_JSONFILE))
        {
        	CreateTableDynamoDB_TeeTimes ct = new CreateTableDynamoDB_TeeTimes();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(PLAYERS_JSONFILE))
        {
        	CreateTableDynamoDB_Players ct = new CreateTableDynamoDB_Players();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(PLAYERTEES_JSONFILE))
        {
        	CreateTableDynamoDB_PlayerTeePreferences ct = new CreateTableDynamoDB_PlayerTeePreferences();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(PLAYERMONEY_JSONFILE))
        {
        	CreateTableDynamoDB_PlayerMoney ct = new CreateTableDynamoDB_PlayerMoney();
        	ct.loadTable(dynamoClients, inputStream);		
        }
        else if (jsonFileName.equalsIgnoreCase(ROUNDS_JSONFILE))
        {
        	CreateTableDynamoDB_Rounds ct = new CreateTableDynamoDB_Rounds();
        	ct.loadTable(dynamoClients, inputStream);		
        }		
	}
		
}
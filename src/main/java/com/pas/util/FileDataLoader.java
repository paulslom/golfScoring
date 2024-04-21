package com.pas.util;

import java.io.InputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.dynamodb.CreateTableDynamoDB_CourseTees;
import com.pas.dynamodb.CreateTableDynamoDB_Courses;
import com.pas.dynamodb.CreateTableDynamoDB_Games;
import com.pas.dynamodb.CreateTableDynamoDB_GolfUsers;
import com.pas.dynamodb.CreateTableDynamoDB_Groups;
import com.pas.dynamodb.CreateTableDynamoDB_PlayerMoney;
import com.pas.dynamodb.CreateTableDynamoDB_PlayerTeePreferences;
import com.pas.dynamodb.CreateTableDynamoDB_Players;
import com.pas.dynamodb.CreateTableDynamoDB_Rounds;
import com.pas.dynamodb.CreateTableDynamoDB_TeeTimes;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

public class FileDataLoader 
{
	private static Logger logger = LogManager.getLogger(FileDataLoader.class);
    //private static final boolean loadData = false;
    private static final boolean loadData = true;
    
    private static String GOLFUSERS_JSONFILE = "/data/GolfUsersData.json";
	private static String COURSES_JSONFILE = "/data/CoursesData.json";
	private static String GAMES_JSONFILE = "/data/GamesData.json";
	private static String GOLFCOURSETEES_JSONFILE = "/data/GolfCourseTeesData.json";
	private static String PLAYERMONEY_JSONFILE = "/data/PlayerMoneyData.json";
	private static String PLAYERS_JSONFILE = "/data/PlayersData.json";
	private static String PLAYERTEES_JSONFILE = "/data/PlayerTeesData.json";
	private static String ROUNDS_JSONFILE = "/data/RoundsData.json";
	private static String TEETIMES_JSONFILE = "/data/TeeTimesData.json";
	private static String GOLFGROUPS_JSONFILE = "/data/GolfGroupsData.json";
	
    public boolean load() throws Exception 
    {
        if (!loadData) 
        {
            logger.info("Load data property is set to false.  Not loading dynamo tables from json files");
            return false;
        }
        
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
		/**/       
                
        return true;
    }
    
    private static void doTable(DynamoClients dynamoClients, String jsonFileName) throws Exception 
	{
    	InputStream inputStream = FileDataLoader.class.getResourceAsStream(jsonFileName);
		 		 
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


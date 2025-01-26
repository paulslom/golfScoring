package com.pas.dynamodb;

import java.util.UUID;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;

public class InsertPlayerTeePreferenceRow
{	 
	private static String AWS_TABLE_NAME = "playerteepreferences";
	
    public static void main(String[] args) 
    {
        try 
        {
        	DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
            
            System.out.println("Successfully established dynamoenhancedclient");
            
            DynamoDbTable<DynamoPlayerTeePreference> playerTeePreferencesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoPlayerTeePreference.class));

            System.out.println("Successfully got the table");
            
            DynamoPlayerTeePreference dtt = setUpPTP();
            
            PutItemEnhancedRequest<DynamoPlayerTeePreference> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoPlayerTeePreference.class).item(dtt).build();
            
            System.out.println("Successfully established putItemRequest");
            
    		playerTeePreferencesTable.putItem(putItemEnhancedRequest);  
    		
    		System.out.println("Successfully added item to table");
        } 
        catch (Exception e) 
        {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
        
        //System.exit(1);
    }
    
	private static DynamoPlayerTeePreference setUpPTP()
	{
		DynamoPlayerTeePreference dtt = new DynamoPlayerTeePreference();
		
		dtt.setPlayerTeePreferenceID(UUID.randomUUID().toString());
    	dtt.setPlayerID("d10533e5-824b-4408-a0c9-ed7b70e38286");
    	dtt.setCourseID("457b9b6e-baa7-4cdc-a1d2-514d102fd77c");  //champs
    	//dtt.setCourseID("ed383d5d-5b04-474d-9245-85ed9d91e524");  //players
    	dtt.setCourseTeeID("ff2635b6-edde-4496-ba88-f048b4cc7c4c"); //champs
    	//dtt.setCourseTeeID("ae65ac0b-0185-4bda-8398-9b971cee4ba7"); //players
    	dtt.setPlayerUserName("jwolfe");
    	dtt.setPlayerFullName("John Wolfe");
    	dtt.setCourseName("Bryan Park Champions");  
    	dtt.setTeeColor("Gold");
    		
		return dtt;		
	}
   
}
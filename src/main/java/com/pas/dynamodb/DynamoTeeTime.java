package com.pas.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoTeeTime 
{
	private String teeTimeID;
	private String gameID;
	private int oldTeeTimeID;
	private int oldGameID;
	private int playGroupNumber;
	private String teeTimeString;
	
	@DynamoDbPartitionKey //primary key
	public String getTeeTimeID() 
	{
		return teeTimeID;
	}
	
	public void setTeeTimeID(String teeTimeID) 
	{
		this.teeTimeID = teeTimeID;
	}
	
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameID")
	public String getGameID() 
	{
		return gameID;
	}
	
	public void setGameID(String gameID) 
	{
		this.gameID = gameID;
	}
	
	public int getPlayGroupNumber() 
	{
		return playGroupNumber;
	}
	
	public void setPlayGroupNumber(int playGroupNumber) 
	{
		this.playGroupNumber = playGroupNumber;
	}
	
	public String getTeeTimeString() 
	{
		return teeTimeString;
	}
	
	public void setTeeTimeString(String teeTimeString) 
	{
		this.teeTimeString = teeTimeString;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_oldTeeTimeID")
	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_oldGameID")
	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}
}

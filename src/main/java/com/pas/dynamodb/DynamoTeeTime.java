package com.pas.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

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
	
	@DynamoDBAttribute(attributeName = "TeeTimeID")
	@DynamoDbPartitionKey //primary key
	public String getTeeTimeID() 
	{
		return teeTimeID;
	}
	
	@DynamoDBAttribute(attributeName = "TeeTimeID")
	public void setTeeTimeID(String teeTimeID) 
	{
		this.teeTimeID = teeTimeID;
	}
	
	@DynamoDBAttribute(attributeName = "GameID")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameID")
	public String getGameID() 
	{
		return gameID;
	}
	
	@DynamoDBAttribute(attributeName = "GameID")
	public void setGameID(String gameID) 
	{
		this.gameID = gameID;
	}
	
	@DynamoDBAttribute(attributeName = "PlayGroupNumber")
	public int getPlayGroupNumber() 
	{
		return playGroupNumber;
	}
	
	@DynamoDBAttribute(attributeName = "PlayGroupNumber")
	public void setPlayGroupNumber(int playGroupNumber) 
	{
		this.playGroupNumber = playGroupNumber;
	}
	
	@DynamoDBAttribute(attributeName = "TeeTimeString")
	public String getTeeTimeString() 
	{
		return teeTimeString;
	}
	
	@DynamoDBAttribute(attributeName = "TeeTimeString")
	public void setTeeTimeString(String teeTimeString) 
	{
		this.teeTimeString = teeTimeString;
	}

	@DynamoDBAttribute(attributeName = "OldTeeTimeID")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_oldTeeTimeID")
	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	@DynamoDBAttribute(attributeName = "OldGameID")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_oldGameID")
	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}
}

package com.pas.dynamodb;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoPlayerMoney 
{
	private String playerMoneyID;
	private int oldPlayerMoneyID;
	private String gameID;
	private int oldGameID;
	private String playerID;
	private int oldPlayerID;
	private String description;
	private BigDecimal amount;

	@DynamoDBAttribute(attributeName = "PlayerID")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_PlayerID")
	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	@DynamoDBAttribute(attributeName = "oldPlayerID")
	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	@DynamoDBAttribute(attributeName = "playerMoneyID")
	@DynamoDbPartitionKey //primary key
	public String getPlayerMoneyID() {
		return playerMoneyID;
	}

	public void setPlayerMoneyID(String playerMoneyID) {
		this.playerMoneyID = playerMoneyID;
	}

	@DynamoDBAttribute(attributeName = "oldPlayerMoneyID")
	public int getOldPlayerMoneyID() {
		return oldPlayerMoneyID;
	}

	public void setOldPlayerMoneyID(int oldPlayerMoneyID) {
		this.oldPlayerMoneyID = oldPlayerMoneyID;
	}

	@DynamoDBAttribute(attributeName = "gameID")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameID")
	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	@DynamoDBAttribute(attributeName = "oldGameID")
	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}

	@DynamoDBAttribute(attributeName = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@DynamoDBAttribute(attributeName = "amount")
	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
}

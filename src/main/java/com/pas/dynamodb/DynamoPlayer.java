package com.pas.dynamodb;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@DynamoDbBean
public class DynamoPlayer 
{
	private String playerID;
	private int oldPlayerID;
	private String username;
	private String firstName;
	private String lastName;
	private BigDecimal handicap;
	private String emailAddress;
	private boolean active;

	@DynamoDBAttribute(attributeName = "PlayerID")
	@DynamoDbPartitionKey //primary key
	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	@DynamoDBAttribute(attributeName = "Username")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_Username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@DynamoDBAttribute(attributeName = "FirstName")
	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	@DynamoDBAttribute(attributeName = "LastName")
	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	@DynamoDBAttribute(attributeName = "Handicap")
	public BigDecimal getHandicap() {
		return handicap;
	}

	public void setHandicap(BigDecimal handicap) {
		this.handicap = handicap;
	}

	@DynamoDBAttribute(attributeName = "emailAddress")
	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	@DynamoDBAttribute(attributeName = "Active")
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_OldPlayerID")
	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}
}

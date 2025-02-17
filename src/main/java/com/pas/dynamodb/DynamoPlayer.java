package com.pas.dynamodb;

import java.math.BigDecimal;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

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
	private String fullName;

	private String role;
	
	public String toString()
	{
		return "playerID: " + playerID + " username: " + username + " FirstName: " + firstName + " Last Name: " + lastName;
	}
	
	@DynamoDbPartitionKey //primary key
	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_Username")
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) 
	{
		this.firstName = firstName;
		this.setFullName(firstName + " " + lastName);
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) 
	{
		this.lastName = lastName;
		this.setFullName(firstName + " " + lastName);
	}

	public BigDecimal getHandicap() {
		return handicap;
	}

	public void setHandicap(BigDecimal handicap) {
		this.handicap = handicap;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

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

	@DynamoDbIgnore
	public String getRole() {
		return role;
	}

	@DynamoDbIgnore
	public void setRole(String role) {
		this.role = role;
	}

	@DynamoDbIgnore
	public String getFullName() {
		return fullName;
	}

	@DynamoDbIgnore
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
}

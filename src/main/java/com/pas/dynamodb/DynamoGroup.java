package com.pas.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class DynamoGroup 
{
	private String groupID;
	private Integer oldGroupID;
	private String groupName;
	
	@DynamoDBAttribute(attributeName = "GroupID")
	@DynamoDbPartitionKey //primary key
	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	@DynamoDBAttribute(attributeName = "OldGroupID")
	public Integer getOldGroupID() {
		return oldGroupID;
	}

	public void setOldGroupID(Integer oldGroupID) {
		this.oldGroupID = oldGroupID;
	}

	@DynamoDBAttribute(attributeName = "GroupName")
	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}

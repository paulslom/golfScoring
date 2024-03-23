package com.pas.dynamodb;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

@DynamoDbBean
public class DynamoGroup 
{
	private String groupID;
	private Integer oldGroupID;
	private String groupName;
	
	@DynamoDbPartitionKey //primary key
	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public Integer getOldGroupID() {
		return oldGroupID;
	}

	public void setOldGroupID(Integer oldGroupID) {
		this.oldGroupID = oldGroupID;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

}

package com.pas.beans;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey;

@Named("pc_GolfUser")
@RequestScoped
@DynamoDbBean
public class GolfUser implements Serializable
{
	private static final long serialVersionUID = 131158039169073163L;
	
	private Integer userId;
	private String userName;
	private String password;
	private String userRole;
	
	/*
	 * @DynamoDbPartitionKey is the primary key
	 * @DynamoDbSortKey sorts on something
	 * @DynamoDbSecondaryPartitionKey is a GSI (Global Secondary Index.  Example:
	 *     @DynamoDbSecondaryPartitionKey(indexNames = "customers_by_name")
	 * @DynamoDbSecondarySortKey
	 *     // Defines an LSI (customers_by_date) with a sort key of 'createdDate' and also declares the 
           // same attribute as a sort key for the GSI named 'customers_by_name'
           @DynamoDbSecondarySortKey(indexNames = {"customers_by_date", "customers_by_name"})
	 */
	
	@DynamoDbPartitionKey
	public String getUserName() 
	{
		return userName;
	}
	
	public void setUserName(String userName) 
	{
		this.userName = userName;
	}
	
	public String getPassword() 
	{
		return password;
	}
	
	public void setPassword(String password) 
	{
		this.password = password;
	}
	
	public Integer getUserId() 
	{
		return userId;
	}
	
	public void setUserId(Integer userId) 
	{
		this.userId = userId;
	}
	
	@DynamoDbSortKey
	public String getUserRole() 
	{
		return userRole;
	}
	
	public void setUserRole(String userRole) 
	{
		this.userRole = userRole;
	}
		
	
}

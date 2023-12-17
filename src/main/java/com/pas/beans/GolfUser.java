package com.pas.beans;

import java.io.Serializable;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

@Named("pc_GolfUser")
@RequestScoped
public class GolfUser implements Serializable
{
	private static final long serialVersionUID = 131158039169073163L;
	
	private Integer userId;
	private String userName;
	private String password;
	private String userRole;
	
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getUserRole() {
		return userRole;
	}
	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}
		
	
}

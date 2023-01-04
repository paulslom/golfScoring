package com.pas.beans;

import java.io.Serializable;

import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("pc_GolfUser")
@RequestScoped
public class GolfUser implements Serializable
{
	private static final long serialVersionUID = 131158039169073163L;
	private String userName;
	private String password;
	private String[] userRoles;
	
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
	public String[] getUserRoles() {
		return userRoles;
	}
	public void setUserRoles(String[] userRoles) {
		this.userRoles = userRoles;
	}
		
	
}

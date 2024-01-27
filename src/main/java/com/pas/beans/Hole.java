package com.pas.beans;

import java.io.Serializable;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;

@DynamoDbBean
public class Hole implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String courseID;
	private int holeNumber;
	private int par;
	
	
	public int getHoleNumber() {
		return holeNumber;
	}
	public void setHoleNumber(int holeNumber) {
		this.holeNumber = holeNumber;
	}
	public int getPar() {
		return par;
	}
	public void setPar(int par) {
		this.par = par;
	}
	public String getCourseID() {
		return courseID;
	}
	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}
	
	
}

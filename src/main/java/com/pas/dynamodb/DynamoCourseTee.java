package com.pas.dynamodb;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoCourseTee 
{
	private String courseTeeID;
	private int oldCourseTeeID;
	private String courseID;
	private int oldCourseID;
	private String teeColor;
	private BigDecimal courseRating;
	private int coursePar;
	private int slopeRating;
	private int totalYardage;

	@DynamoDBAttribute(attributeName = "CourseTeeID")
	@DynamoDbPartitionKey //primary key
	public String getCourseTeeID() {
		return courseTeeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}
	                                             
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_OldCourseTeeID")
	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}

	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}

	public String getTeeColor() {
		return teeColor;
	}

	public void setTeeColor(String teeColor) {
		this.teeColor = teeColor;
	}

	public BigDecimal getCourseRating() {
		return courseRating;
	}

	public void setCourseRating(BigDecimal courseRating) {
		this.courseRating = courseRating;
	}

	public int getCoursePar() {
		return coursePar;
	}

	public void setCoursePar(int coursePar) {
		this.coursePar = coursePar;
	}

	public int getSlopeRating() {
		return slopeRating;
	}

	public void setSlopeRating(int slopeRating) {
		this.slopeRating = slopeRating;
	}

	public int getTotalYardage() {
		return totalYardage;
	}

	public void setTotalYardage(int totalYardage) {
		this.totalYardage = totalYardage;
	}
}

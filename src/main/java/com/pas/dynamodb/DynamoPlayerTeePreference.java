package com.pas.dynamodb;

import java.util.ArrayList;
import java.util.List;

import jakarta.faces.model.SelectItem;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoPlayerTeePreference 
{
	private String playerTeePreferenceID;
	private int oldPlayerTeePreferenceID;
	private String playerID;
	private int oldPlayerID;
	private String courseID;
	private int oldCourseID;
	private String courseTeeID;
	private int oldCourseTeeID;
	private String playerUserName;
	private String playerFullName;
	private String courseName;
	private String teeColor;

	private List<SelectItem> teeSelections = new ArrayList<>();
	
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_PlayerID")
	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_OldPlayerID")
	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	@DynamoDbPartitionKey //primary key
	public String getPlayerTeePreferenceID() {
		return playerTeePreferenceID;
	}

	public void setPlayerTeePreferenceID(String playerTeePreferenceID) {
		this.playerTeePreferenceID = playerTeePreferenceID;
	}

	public int getOldPlayerTeePreferenceID() {
		return oldPlayerTeePreferenceID;
	}

	public void setOldPlayerTeePreferenceID(int oldPlayerTeePreferenceID) {
		this.oldPlayerTeePreferenceID = oldPlayerTeePreferenceID;
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

	public String getCourseTeeID() {
		return courseTeeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}

	public String getPlayerUserName() {
		return playerUserName;
	}

	public void setPlayerUserName(String playerUserName) {
		this.playerUserName = playerUserName;
	}

	public String getPlayerFullName() {
		return playerFullName;
	}

	public void setPlayerFullName(String playerFullName) {
		this.playerFullName = playerFullName;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getTeeColor() {
		return teeColor;
	}

	public void setTeeColor(String teeColor) {
		this.teeColor = teeColor;
	}

	@DynamoDbIgnore
	public List<SelectItem> getTeeSelections() {
		return teeSelections;
	}

	@DynamoDbIgnore
	public void setTeeSelections(List<SelectItem> teeSelections) {
		this.teeSelections = teeSelections;
	}
}

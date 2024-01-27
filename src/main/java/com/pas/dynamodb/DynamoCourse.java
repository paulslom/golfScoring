package com.pas.dynamodb;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoCourse 
{
	private String courseID;
	private int oldCourseID;
	private String courseName;
	private int front9Par;
	private int back9Par;
	private int coursePar;
	private String groupID;
	private int hole1Par;
	private int hole2Par;
	private int hole3Par;
	private int hole4Par;
	private int hole5Par;
	private int hole6Par;
	private int hole7Par;
	private int hole8Par;
	private int hole9Par;
	private int hole10Par;
	private int hole11Par;
	private int hole12Par;
	private int hole13Par;
	private int hole14Par;
	private int hole15Par;
	private int hole16Par;
	private int hole17Par;
	private int hole18Par;

	@DynamoDBAttribute(attributeName = "CourseID")
	@DynamoDbPartitionKey //primary key
	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_OldCourseID")
	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public int getFront9Par() {
		return front9Par;
	}

	public void setFront9Par(int front9Par) {
		this.front9Par = front9Par;
	}

	public int getBack9Par() {
		return back9Par;
	}

	public void setBack9Par(int back9Par) {
		this.back9Par = back9Par;
	}

	public int getCoursePar() {
		return coursePar;
	}

	public void setCoursePar(int coursePar) {
		this.coursePar = coursePar;
	}

	public String getGroupID() {
		return groupID;
	}

	public void setGroupID(String groupID) {
		this.groupID = groupID;
	}

	public int getHole1Par() {
		return hole1Par;
	}

	public void setHole1Par(int hole1Par) {
		this.hole1Par = hole1Par;
	}

	public int getHole2Par() {
		return hole2Par;
	}

	public void setHole2Par(int hole2Par) {
		this.hole2Par = hole2Par;
	}

	public int getHole3Par() {
		return hole3Par;
	}

	public void setHole3Par(int hole3Par) {
		this.hole3Par = hole3Par;
	}

	public int getHole4Par() {
		return hole4Par;
	}

	public void setHole4Par(int hole4Par) {
		this.hole4Par = hole4Par;
	}

	public int getHole5Par() {
		return hole5Par;
	}

	public void setHole5Par(int hole5Par) {
		this.hole5Par = hole5Par;
	}

	public int getHole6Par() {
		return hole6Par;
	}

	public void setHole6Par(int hole6Par) {
		this.hole6Par = hole6Par;
	}

	public int getHole7Par() {
		return hole7Par;
	}

	public void setHole7Par(int hole7Par) {
		this.hole7Par = hole7Par;
	}

	public int getHole8Par() {
		return hole8Par;
	}

	public void setHole8Par(int hole8Par) {
		this.hole8Par = hole8Par;
	}

	public int getHole9Par() {
		return hole9Par;
	}

	public void setHole9Par(int hole9Par) {
		this.hole9Par = hole9Par;
	}

	public int getHole10Par() {
		return hole10Par;
	}

	public void setHole10Par(int hole10Par) {
		this.hole10Par = hole10Par;
	}

	public int getHole11Par() {
		return hole11Par;
	}

	public void setHole11Par(int hole11Par) {
		this.hole11Par = hole11Par;
	}

	public int getHole12Par() {
		return hole12Par;
	}

	public void setHole12Par(int hole12Par) {
		this.hole12Par = hole12Par;
	}

	public int getHole13Par() {
		return hole13Par;
	}

	public void setHole13Par(int hole13Par) {
		this.hole13Par = hole13Par;
	}

	public int getHole14Par() {
		return hole14Par;
	}

	public void setHole14Par(int hole14Par) {
		this.hole14Par = hole14Par;
	}

	public int getHole15Par() {
		return hole15Par;
	}

	public void setHole15Par(int hole15Par) {
		this.hole15Par = hole15Par;
	}

	public int getHole16Par() {
		return hole16Par;
	}

	public void setHole16Par(int hole16Par) {
		this.hole16Par = hole16Par;
	}

	public int getHole17Par() {
		return hole17Par;
	}

	public void setHole17Par(int hole17Par) {
		this.hole17Par = hole17Par;
	}

	public int getHole18Par() {
		return hole18Par;
	}

	public void setHole18Par(int hole18Par) {
		this.hole18Par = hole18Par;
	}
}

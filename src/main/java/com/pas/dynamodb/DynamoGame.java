package com.pas.dynamodb;

import java.math.BigDecimal;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBAttribute;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoGame 
{
	private String gameID;
	private int oldGameID;
	private String gameDate;
	private String courseID;
	private int oldCourseID;
	private Integer fieldSize;
	private Integer totalPlayers;
	private Integer totalTeams;
	private BigDecimal skinsPot;
	private BigDecimal teamPot;
	private BigDecimal betAmount = new BigDecimal(20.00);
	private Integer howManyBalls;
	private BigDecimal purseAmount;
	private BigDecimal eachBallWorth;
	private BigDecimal individualGrossPrize = new BigDecimal(0.00);
	private BigDecimal individualNetPrize = new BigDecimal(0.00);
	private String playTheBallMethod; //up everywhere; down everywhere; up in fairway, down in rough	
	private boolean gameClosedForSignups = false;	
	private String gameNoteForEmail;
	
	@DynamoDBAttribute(attributeName = "GameID")
	@DynamoDbPartitionKey //primary key
	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	@DynamoDBAttribute(attributeName = "GameDate")
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameDate")
	public String getGameDate() {
		return gameDate;
	}

	public void setGameDate(String gameDate) {
		this.gameDate = gameDate;
	}

	@DynamoDBAttribute(attributeName = "CourseID")
	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	@DynamoDBAttribute(attributeName = "FieldSize")
	public Integer getFieldSize() {
		return fieldSize;
	}

	public void setFieldSize(Integer fieldSize) {
		this.fieldSize = fieldSize;
	}

	@DynamoDBAttribute(attributeName = "TotalPlayers")
	public Integer getTotalPlayers() {
		return totalPlayers;
	}

	public void setTotalPlayers(Integer totalPlayers) {
		this.totalPlayers = totalPlayers;
	}

	@DynamoDBAttribute(attributeName = "TotalTeams")
	public Integer getTotalTeams() {
		return totalTeams;
	}

	public void setTotalTeams(Integer totalTeams) {
		this.totalTeams = totalTeams;
	}

	@DynamoDBAttribute(attributeName = "SkinsPot")
	public BigDecimal getSkinsPot() {
		return skinsPot;
	}

	public void setSkinsPot(BigDecimal skinsPot) {
		this.skinsPot = skinsPot;
	}

	@DynamoDBAttribute(attributeName = "TeamPot")
	public BigDecimal getTeamPot() {
		return teamPot;
	}

	@DynamoDBAttribute(attributeName = "")
	public void setTeamPot(BigDecimal teamPot) {
		this.teamPot = teamPot;
	}

	@DynamoDBAttribute(attributeName = "BetAmount")
	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	@DynamoDBAttribute(attributeName = "HowManyBalls")
	public Integer getHowManyBalls() {
		return howManyBalls;
	}

	public void setHowManyBalls(Integer howManyBalls) {
		this.howManyBalls = howManyBalls;
	}

	@DynamoDBAttribute(attributeName = "PurseAmount")
	public BigDecimal getPurseAmount() {
		return purseAmount;
	}
	
	public void setPurseAmount(BigDecimal purseAmount) {
		this.purseAmount = purseAmount;
	}

	@DynamoDBAttribute(attributeName = "EachBallWorth")
	public BigDecimal getEachBallWorth() {
		return eachBallWorth;
	}
	
	public void setEachBallWorth(BigDecimal eachBallWorth) {
		this.eachBallWorth = eachBallWorth;
	}

	@DynamoDBAttribute(attributeName = "IndividualGrossPrize")
	public BigDecimal getIndividualGrossPrize() {
		return individualGrossPrize;
	}

	public void setIndividualGrossPrize(BigDecimal individualGrossPrize) {
		this.individualGrossPrize = individualGrossPrize;
	}

	@DynamoDBAttribute(attributeName = "IndividualNetPrize")
	public BigDecimal getIndividualNetPrize() {
		return individualNetPrize;
	}

	public void setIndividualNetPrize(BigDecimal individualNetPrize) {
		this.individualNetPrize = individualNetPrize;
	}

	@DynamoDBAttribute(attributeName = "PlayTheBallMethod")
	public String getPlayTheBallMethod() {
		return playTheBallMethod;
	}

	public void setPlayTheBallMethod(String playTheBallMethod) {
		this.playTheBallMethod = playTheBallMethod;
	}

	@DynamoDBAttribute(attributeName = "GameClosedForSignups")
	public boolean isGameClosedForSignups() {
		return gameClosedForSignups;
	}

	public void setGameClosedForSignups(boolean gameClosedForSignups) {
		this.gameClosedForSignups = gameClosedForSignups;
	}

	@DynamoDBAttribute(attributeName = "GameNoteForEmail")
	public String getGameNoteForEmail() {
		return gameNoteForEmail;
	}

	public void setGameNoteForEmail(String gameNoteForEmail) {
		this.gameNoteForEmail = gameNoteForEmail;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_OldGameID")
	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}

	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}
}

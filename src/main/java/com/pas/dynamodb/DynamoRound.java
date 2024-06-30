package com.pas.dynamodb;

import java.math.BigDecimal;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class DynamoRound 
{
	private String roundID;
	private int oldRoundID;
	private String gameID;
	private int oldGameID;
	private String playerID;
	private int oldPlayerID;
	private int teamNumber;	
	private String teeTimeID;
	private int oldTeeTimeID;
	private String playerName;
	private BigDecimal roundHandicap;
	private BigDecimal playerHandicapIndex;
	private String courseTeeID;
	private int oldCourseTeeID;
	private String courseTeeColor;
	private BigDecimal roundHandicapDifferential;
	private String signupDateTime;	
	private String roundCreatedDateTime;
	private Integer hole1Score;
	private Integer hole2Score;
	private Integer hole3Score;
	private Integer hole4Score;
	private Integer hole5Score;
	private Integer hole6Score;
	private Integer hole7Score;
	private Integer hole8Score;
	private Integer hole9Score;	
	private Integer hole10Score;
	private Integer hole11Score;
	private Integer hole12Score;
	private Integer hole13Score;
	private Integer hole14Score;
	private Integer hole15Score;
	private Integer hole16Score;
	private Integer hole17Score;
	private Integer hole18Score;	
	private Integer front9Total;
	private Integer back9Total;	
	private Integer totalScore;
	private String totalToPar;
	private BigDecimal netScore;
	
	@DynamoDbPartitionKey //primary key
	public String getRoundID() {
		return roundID;
	}
	public void setRoundID(String roundID) {
		this.roundID = roundID;
	}
	public int getOldRoundID() {
		return oldRoundID;
	}
	public void setOldRoundID(int oldRoundID) {
		this.oldRoundID = oldRoundID;
	}
	
	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameID")
	public String getGameID() {
		return gameID;
	}
	public void setGameID(String gameID) {
		this.gameID = gameID;
	}
	public int getOldGameID() {
		return oldGameID;
	}
	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}
	public String getPlayerID() {
		return playerID;
	}
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}
	public int getOldPlayerID() {
		return oldPlayerID;
	}
	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}
	public int getTeamNumber() {
		return teamNumber;
	}
	public void setTeamNumber(int teamNumber) {
		this.teamNumber = teamNumber;
	}
	public String getTeeTimeID() {
		return teeTimeID;
	}
	public void setTeeTimeID(String teeTimeID) {
		this.teeTimeID = teeTimeID;
	}
	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}
	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public BigDecimal getRoundHandicap() {
		return roundHandicap;
	}
	public void setRoundHandicap(BigDecimal roundHandicap) {
		this.roundHandicap = roundHandicap;
	}
	public BigDecimal getPlayerHandicapIndex() {
		return playerHandicapIndex;
	}
	public void setPlayerHandicapIndex(BigDecimal playerHandicapIndex) {
		this.playerHandicapIndex = playerHandicapIndex;
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
	public String getCourseTeeColor() {
		return courseTeeColor;
	}
	public void setCourseTeeColor(String courseTeeColor) {
		this.courseTeeColor = courseTeeColor;
	}
	public BigDecimal getRoundHandicapDifferential() {
		return roundHandicapDifferential;
	}
	public void setRoundHandicapDifferential(BigDecimal roundHandicapDifferential) {
		this.roundHandicapDifferential = roundHandicapDifferential;
	}
	public String getSignupDateTime() {
		return signupDateTime;
	}
	public void setSignupDateTime(String signupDateTime) {
		this.signupDateTime = signupDateTime;
	}
	public Integer getHole1Score() {
		return hole1Score;
	}
	public void setHole1Score(Integer hole1Score) {
		this.hole1Score = hole1Score;
	}
	public Integer getHole2Score() {
		return hole2Score;
	}
	public void setHole2Score(Integer hole2Score) {
		this.hole2Score = hole2Score;
	}
	public Integer getHole3Score() {
		return hole3Score;
	}
	public void setHole3Score(Integer hole3Score) {
		this.hole3Score = hole3Score;
	}
	public Integer getHole4Score() {
		return hole4Score;
	}
	public void setHole4Score(Integer hole4Score) {
		this.hole4Score = hole4Score;
	}
	public Integer getHole5Score() {
		return hole5Score;
	}
	public void setHole5Score(Integer hole5Score) {
		this.hole5Score = hole5Score;
	}
	public Integer getHole6Score() {
		return hole6Score;
	}
	public void setHole6Score(Integer hole6Score) {
		this.hole6Score = hole6Score;
	}
	public Integer getHole7Score() {
		return hole7Score;
	}
	public void setHole7Score(Integer hole7Score) {
		this.hole7Score = hole7Score;
	}
	public Integer getHole8Score() {
		return hole8Score;
	}
	public void setHole8Score(Integer hole8Score) {
		this.hole8Score = hole8Score;
	}
	public Integer getHole9Score() {
		return hole9Score;
	}
	public void setHole9Score(Integer hole9Score) {
		this.hole9Score = hole9Score;
	}
	public Integer getHole10Score() {
		return hole10Score;
	}
	public void setHole10Score(Integer hole10Score) {
		this.hole10Score = hole10Score;
	}
	public Integer getHole11Score() {
		return hole11Score;
	}
	public void setHole11Score(Integer hole11Score) {
		this.hole11Score = hole11Score;
	}
	public Integer getHole12Score() {
		return hole12Score;
	}
	public void setHole12Score(Integer hole12Score) {
		this.hole12Score = hole12Score;
	}
	public Integer getHole13Score() {
		return hole13Score;
	}
	public void setHole13Score(Integer hole13Score) {
		this.hole13Score = hole13Score;
	}
	public Integer getHole14Score() {
		return hole14Score;
	}
	public void setHole14Score(Integer hole14Score) {
		this.hole14Score = hole14Score;
	}
	public Integer getHole15Score() {
		return hole15Score;
	}
	public void setHole15Score(Integer hole15Score) {
		this.hole15Score = hole15Score;
	}
	public Integer getHole16Score() {
		return hole16Score;
	}
	public void setHole16Score(Integer hole16Score) {
		this.hole16Score = hole16Score;
	}
	public Integer getHole17Score() {
		return hole17Score;
	}
	public void setHole17Score(Integer hole17Score) {
		this.hole17Score = hole17Score;
	}
	public Integer getHole18Score() {
		return hole18Score;
	}
	public void setHole18Score(Integer hole18Score) {
		this.hole18Score = hole18Score;
	}
	public Integer getFront9Total() {
		return front9Total;
	}
	public void setFront9Total(Integer front9Total) {
		this.front9Total = front9Total;
	}
	public Integer getBack9Total() {
		return back9Total;
	}
	public void setBack9Total(Integer back9Total) {
		this.back9Total = back9Total;
	}
	public Integer getTotalScore() {
		return totalScore;
	}
	public void setTotalScore(Integer totalScore) {
		this.totalScore = totalScore;
	}
	public String getTotalToPar() {
		return totalToPar;
	}
	public void setTotalToPar(String totalToPar) {
		this.totalToPar = totalToPar;
	}
	public BigDecimal getNetScore() {
		return netScore;
	}
	public void setNetScore(BigDecimal netScore) {
		this.netScore = netScore;
	}
	public String getRoundCreatedDateTime() {
		return roundCreatedDateTime;
	}
	public void setRoundCreatedDateTime(String roundCreatedDateTime) {
		this.roundCreatedDateTime = roundCreatedDateTime;
	}	

	
}

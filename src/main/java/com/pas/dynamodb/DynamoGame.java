package com.pas.dynamodb;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.pas.beans.Course;
import com.pas.util.Utils;

import jakarta.faces.model.SelectItem;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore;
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
	private BigDecimal gameFee = new BigDecimal(0.00);
	private String playTheBallMethod; //up everywhere; down everywhere; up in fairway, down in rough	
	private boolean gameClosedForSignups = false;	
	private String gameNoteForEmail;

	private boolean renderSignUp = true;
	private boolean renderWithdraw = false;
	private List<SelectItem> teeSelections = new ArrayList<>();
	private Course course;
	private String courseName;
	private Date gameDateJava;
	private Integer spotsAvailable;
	private String selectedCourseTeeID;
	private String gameDateDisplay;
	private BigDecimal suggestedSkinsPot;
	private String teeTimesString;

	@DynamoDbPartitionKey //primary key
	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	@DynamoDbSecondaryPartitionKey(indexNames = "gsi_GameDate")
	public String getGameDate() {
		return gameDate;
	}

	public void setGameDate(String gameDate) {
		this.gameDate = gameDate;
	}

	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public Integer getFieldSize() {
		return fieldSize;
	}

	public void setFieldSize(Integer fieldSize) {
		this.fieldSize = fieldSize;
	}

	public Integer getTotalPlayers() {
		return totalPlayers;
	}

	public void setTotalPlayers(Integer totalPlayers) 
	{
		this.totalPlayers = totalPlayers;
		this.setTotalTeams(Utils.setRecommendedTeams(totalPlayers));	
	}

	public Integer getTotalTeams() {
		return totalTeams;
	}

	public void setTotalTeams(Integer totalTeams) 
	{
		this.totalTeams = totalTeams;
	}

	public BigDecimal getSkinsPot() {
		return skinsPot;
	}

	public void setSkinsPot(BigDecimal skinsPot) {
		this.skinsPot = skinsPot;
	}

	public BigDecimal getTeamPot() {
		return teamPot;
	}

	public void setTeamPot(BigDecimal teamPot) {
		this.teamPot = teamPot;
	}

	public BigDecimal getBetAmount() {
		return betAmount;
	}

	public void setBetAmount(BigDecimal betAmount) {
		this.betAmount = betAmount;
	}

	public Integer getHowManyBalls() {
		return howManyBalls;
	}

	public void setHowManyBalls(Integer howManyBalls)
	{
		this.howManyBalls = howManyBalls;
		
		if (eachBallWorth != null && howManyBalls != null)
		{
			setTeamPot(eachBallWorth.multiply(new BigDecimal(howManyBalls)));
		}
	}

	public BigDecimal getPurseAmount() {
		return purseAmount;
	}
	
	public void setPurseAmount(BigDecimal purseAmount) 
	{		
		this.purseAmount = purseAmount;
		
		if (purseAmount != null && purseAmount.compareTo(new BigDecimal(0.0)) == 1)
		{
			BigDecimal suggestedSkins = purseAmount.multiply(new BigDecimal(0.375)); // 37.5% for skins
			
			int roundedSkins = Utils.roundToNearestMultipleOfTen(suggestedSkins.intValue());  //Round to nearest 10 dollars
			this.setSuggestedSkinsPot(new BigDecimal(roundedSkins));
		}
	}

	public BigDecimal getEachBallWorth() {
		return eachBallWorth;
	}
	
	public void setEachBallWorth(BigDecimal eachBallWorth) 
	{
		this.eachBallWorth = eachBallWorth;
		
		if (howManyBalls != null)
		{
			setTeamPot(eachBallWorth.multiply(new BigDecimal(howManyBalls)));
		}
	}

	public BigDecimal getIndividualGrossPrize() {
		return individualGrossPrize;
	}

	public void setIndividualGrossPrize(BigDecimal individualGrossPrize) {
		this.individualGrossPrize = individualGrossPrize;
	}

	public BigDecimal getIndividualNetPrize() {
		return individualNetPrize;
	}

	public void setIndividualNetPrize(BigDecimal individualNetPrize) {
		this.individualNetPrize = individualNetPrize;
	}

	public String getPlayTheBallMethod() {
		return playTheBallMethod;
	}

	public void setPlayTheBallMethod(String playTheBallMethod) {
		this.playTheBallMethod = playTheBallMethod;
	}

	public boolean isGameClosedForSignups() {
		return gameClosedForSignups;
	}

	public void setGameClosedForSignups(boolean gameClosedForSignups) {
		this.gameClosedForSignups = gameClosedForSignups;
	}

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
	

	public BigDecimal getGameFee() {
		return gameFee;
	}

	public void setGameFee(BigDecimal gameFee) {
		this.gameFee = gameFee;
	}

	@DynamoDbIgnore
	public Date getGameDateJava()
	{
		return gameDateJava;
	}

	@DynamoDbIgnore
	public void setGameDateJava(Date gameDateJava)
	{
		this.gameDateJava = gameDateJava;
	}

	@DynamoDbIgnore
	public String getCourseName() {
		return courseName;
	}

	@DynamoDbIgnore
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	@DynamoDbIgnore
	public List<SelectItem> getTeeSelections() {
		return teeSelections;
	}

	@DynamoDbIgnore
	public void setTeeSelections(List<SelectItem> teeSelections) {
		this.teeSelections = teeSelections;
	}

	@DynamoDbIgnore
	public Integer getSpotsAvailable() {
		return spotsAvailable;
	}

	@DynamoDbIgnore
	public void setSpotsAvailable(Integer spotsAvailable) {
		this.spotsAvailable = spotsAvailable;
	}

	@DynamoDbIgnore
	public boolean isRenderSignUp() {
		return renderSignUp;
	}

	@DynamoDbIgnore
	public void setRenderSignUp(boolean renderSignUp) {
		this.renderSignUp = renderSignUp;
	}

	@DynamoDbIgnore
	public boolean isRenderWithdraw() {
		return renderWithdraw;
	}

	@DynamoDbIgnore
	public void setRenderWithdraw(boolean renderWithdraw) {
		this.renderWithdraw = renderWithdraw;
	}

	@DynamoDbIgnore
	public String getSelectedCourseTeeID() {
		return selectedCourseTeeID;
	}

	@DynamoDbIgnore	
	public void setSelectedCourseTeeID(String selectedCourseTeeID) {
		this.selectedCourseTeeID = selectedCourseTeeID;
	}

	@DynamoDbIgnore
	public String getGameDateDisplay()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		gameDateDisplay = sdf.format(this.getGameDateJava());
		return gameDateDisplay;
	}

	@DynamoDbIgnore
	public void setGameDateDisplay(String gameDateDisplay) 
	{
		this.gameDateDisplay = gameDateDisplay;
	}

	@DynamoDbIgnore
	public BigDecimal getSuggestedSkinsPot() {
		return suggestedSkinsPot;
	}

	@DynamoDbIgnore
	public void setSuggestedSkinsPot(BigDecimal suggestedSkinsPot) {
		this.suggestedSkinsPot = suggestedSkinsPot;
	}
	
	@DynamoDbIgnore
	public String getTeeTimesString() {
		return teeTimesString;
	}
	
	@DynamoDbIgnore
	public void setTeeTimesString(String teeTimesString) {
		this.teeTimesString = teeTimesString;
	}

	@DynamoDbIgnore
	public Course getCourse() {
		return course;
	}

	@DynamoDbIgnore
	public void setCourse(Course course) {
		this.course = course;
	}
}

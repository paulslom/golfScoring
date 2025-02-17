package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import com.pas.dynamodb.DynamoPlayer;

public class Round implements Serializable
{
	private static final long serialVersionUID = 1L;
	//private static Logger logger = LogManager.getLogger(Round.class);

	private String roundID;
	private int oldRoundID;
	private String gameID;
	private int oldGameID;
	private String playerID;
	private int oldPlayerID;
	private int teamNumber;	
	private String teamNumberDisplay;
	private String teeTimeID;
	private int oldTeeTimeID;
	private String playerName;
	private BigDecimal roundHandicap;
	private BigDecimal playerHandicapIndex;
	private String courseTeeID;
	private int oldCourseTeeID;
	private String courseTeeColor;
	private BigDecimal roundHandicapDifferential;
	private Date signupDateTime;
	
	private TeeTime teeTime;
	private DynamoPlayer player;
	private List<Score> roundbyHoleScores = new ArrayList<Score>();		
	
	private Integer hole1Score;
	private Integer hole2Score;
	private Integer hole3Score;
	private Integer hole4Score;
	private Integer hole5Score;
	private Integer hole6Score;
	private Integer hole7Score;
	private Integer hole8Score;
	private Integer hole9Score;
	private Integer front9Total;
	private Integer hole10Score;
	private Integer hole11Score;
	private Integer hole12Score;
	private Integer hole13Score;
	private Integer hole14Score;
	private Integer hole15Score;
	private Integer hole16Score;
	private Integer hole17Score;
	private Integer hole18Score;
	private Integer back9Total;	
	private Integer totalScore;
	private String totalToPar;
	private BigDecimal netScore;	
	
	private boolean hole1ScoreEntryDisabled = false;
	private boolean hole2ScoreEntryDisabled = false;
	private boolean hole3ScoreEntryDisabled = false;
	private boolean hole4ScoreEntryDisabled = false;
	private boolean hole5ScoreEntryDisabled = false;
	private boolean hole6ScoreEntryDisabled = false;
	private boolean hole7ScoreEntryDisabled = false;
	private boolean hole8ScoreEntryDisabled = false;
	private boolean hole9ScoreEntryDisabled = false;
	private boolean hole10ScoreEntryDisabled = false;
	private boolean hole11ScoreEntryDisabled = false;
	private boolean hole12ScoreEntryDisabled = false;
	private boolean hole13ScoreEntryDisabled = false;
	private boolean hole14ScoreEntryDisabled = false;
	private boolean hole15ScoreEntryDisabled = false;
	private boolean hole16ScoreEntryDisabled = false;
	private boolean hole17ScoreEntryDisabled = false;
	private boolean hole18ScoreEntryDisabled = false;
	
	private String hole1StyleClass;
	private String hole2StyleClass;
	private String hole3StyleClass;
	private String hole4StyleClass;
	private String hole5StyleClass;
	private String hole6StyleClass;
	private String hole7StyleClass;
	private String hole8StyleClass;
	private String hole9StyleClass;
	private String hole10StyleClass;
	private String hole11StyleClass;
	private String hole12StyleClass;
	private String hole13StyleClass;
	private String hole14StyleClass;
	private String hole15StyleClass;
	private String hole16StyleClass;
	private String hole17StyleClass;
	private String hole18StyleClass;
	private String front9StyleClass;
	private String back9StyleClass;
	private String totalStyleClass;
	private String netStyleClass;
	private String totalToParClass;	
	
	public String toString()
	{
		return "roundID = " + this.getRoundID() + " player: " + this.getPlayerName() + " handicap: " + this.getRoundHandicap()
					+ " team number: " + this.getTeamNumber() + " score: " + this.getTotalScore();
	}
	
	@Override
    public boolean equals(final Object o) 
	{
        if (this == o) 
        {
            return true;
        }
        if (!(o instanceof String)) 
        {
            return false;
        }
        
        final String that = (String) o;
        return Objects.equals(roundID, that);
    }
	
	
	
	public List<Score> getRoundbyHoleScores() {
		return roundbyHoleScores;
	}

	public void setRoundbyHoleScores(List<Score> roundbyHoleScores) {
		this.roundbyHoleScores = roundbyHoleScores;
	}

	public DynamoPlayer getPlayer() {
		return player;
	}

	public void setPlayer(DynamoPlayer player) {
		this.player = player;
	}
	public String getHole1StyleClass() {
		return hole1StyleClass;
	}

	public void setHole1StyleClass(String hole1StyleClass) {
		this.hole1StyleClass = hole1StyleClass;
	}

	public String getHole2StyleClass() {
		return hole2StyleClass;
	}

	public void setHole2StyleClass(String hole2StyleClass) {
		this.hole2StyleClass = hole2StyleClass;
	}

	public String getHole3StyleClass() {
		return hole3StyleClass;
	}

	public void setHole3StyleClass(String hole3StyleClass) {
		this.hole3StyleClass = hole3StyleClass;
	}

	public String getHole4StyleClass() {
		return hole4StyleClass;
	}

	public void setHole4StyleClass(String hole4StyleClass) {
		this.hole4StyleClass = hole4StyleClass;
	}

	public String getHole5StyleClass() {
		return hole5StyleClass;
	}

	public void setHole5StyleClass(String hole5StyleClass) {
		this.hole5StyleClass = hole5StyleClass;
	}

	public String getHole6StyleClass() {
		return hole6StyleClass;
	}

	public void setHole6StyleClass(String hole6StyleClass) {
		this.hole6StyleClass = hole6StyleClass;
	}

	public String getHole7StyleClass() {
		return hole7StyleClass;
	}

	public void setHole7StyleClass(String hole7StyleClass) {
		this.hole7StyleClass = hole7StyleClass;
	}

	public String getHole8StyleClass() {
		return hole8StyleClass;
	}

	public void setHole8StyleClass(String hole8StyleClass) {
		this.hole8StyleClass = hole8StyleClass;
	}

	public String getHole9StyleClass() {
		return hole9StyleClass;
	}

	public void setHole9StyleClass(String hole9StyleClass) {
		this.hole9StyleClass = hole9StyleClass;
	}

	public String getHole10StyleClass() {
		return hole10StyleClass;
	}

	public void setHole10StyleClass(String hole10StyleClass) {
		this.hole10StyleClass = hole10StyleClass;
	}

	public String getHole11StyleClass() {
		return hole11StyleClass;
	}

	public void setHole11StyleClass(String hole11StyleClass) {
		this.hole11StyleClass = hole11StyleClass;
	}

	public String getHole12StyleClass() {
		return hole12StyleClass;
	}

	public void setHole12StyleClass(String hole12StyleClass) {
		this.hole12StyleClass = hole12StyleClass;
	}

	public String getHole13StyleClass() {
		return hole13StyleClass;
	}

	public void setHole13StyleClass(String hole13StyleClass) {
		this.hole13StyleClass = hole13StyleClass;
	}

	public String getHole14StyleClass() {
		return hole14StyleClass;
	}

	public void setHole14StyleClass(String hole14StyleClass) {
		this.hole14StyleClass = hole14StyleClass;
	}

	public String getHole15StyleClass() {
		return hole15StyleClass;
	}

	public void setHole15StyleClass(String hole15StyleClass) {
		this.hole15StyleClass = hole15StyleClass;
	}

	public String getHole16StyleClass() {
		return hole16StyleClass;
	}

	public void setHole16StyleClass(String hole16StyleClass) {
		this.hole16StyleClass = hole16StyleClass;
	}

	public String getHole17StyleClass() {
		return hole17StyleClass;
	}

	public void setHole17StyleClass(String hole17StyleClass) {
		this.hole17StyleClass = hole17StyleClass;
	}

	public String getHole18StyleClass() {
		return hole18StyleClass;
	}

	public void setHole18StyleClass(String hole18StyleClass) {
		this.hole18StyleClass = hole18StyleClass;
	}

	public String getFront9StyleClass() {
		return front9StyleClass;
	}

	public void setFront9StyleClass(String front9StyleClass) {
		this.front9StyleClass = front9StyleClass;
	}

	public String getBack9StyleClass() {
		return back9StyleClass;
	}

	public void setBack9StyleClass(String back9StyleClass) {
		this.back9StyleClass = back9StyleClass;
	}

	public String getTotalStyleClass() {
		return totalStyleClass;
	}

	public void setTotalStyleClass(String totalStyleClass) {
		this.totalStyleClass = totalStyleClass;
	}

	public String getNetStyleClass() {
		return netStyleClass;
	}

	public void setNetStyleClass(String netStyleClass) {
		this.netStyleClass = netStyleClass;
	}

	public String getTotalToPar() {
		return totalToPar;
	}

	public void setTotalToPar(String totalToPar) {
		this.totalToPar = totalToPar;
	}

	public String getTotalToParClass() {
		return totalToParClass;
	}

	public void setTotalToParClass(String totalToParClass) {
		this.totalToParClass = totalToParClass;
	}

	public BigDecimal getNetScore() {
		return netScore;
	}

	public void setNetScore(BigDecimal netScore) {
		this.netScore = netScore;
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

	public Integer getFront9Total() {
		return front9Total;
	}

	public void setFront9Total(Integer front9Total) {
		this.front9Total = front9Total;
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

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public int getTeamNumber() {
		return teamNumber;
	}

	public void setTeamNumber(int teamNumber) 
	{
		this.teamNumber = teamNumber;
		if (teamNumber <= 0)
		{
			this.setTeamNumberDisplay("Skins Only");
		}
		else
		{
			this.setTeamNumberDisplay(String.valueOf(teamNumber));
		}
	}

	public BigDecimal getRoundHandicap() {
		return roundHandicap;
	}

	public void setRoundHandicap(BigDecimal roundHandicap) {
		this.roundHandicap = roundHandicap;
	}

	public TeeTime getTeeTime() {
		return teeTime;
	}

	public void setTeeTime(TeeTime teeTime) {
		this.teeTime = teeTime;
	}

	public String getTeamNumberDisplay() {
		return teamNumberDisplay;
	}

	public void setTeamNumberDisplay(String teamNumberDisplay) {
		this.teamNumberDisplay = teamNumberDisplay;
	}
	
	public boolean isHole1ScoreEntryDisabled() {
		return hole1ScoreEntryDisabled;
	}

	public void setHole1ScoreEntryDisabled(boolean hole1ScoreEntryDisabled) {
		this.hole1ScoreEntryDisabled = hole1ScoreEntryDisabled;
	}

	public boolean isHole2ScoreEntryDisabled() {
		return hole2ScoreEntryDisabled;
	}

	public void setHole2ScoreEntryDisabled(boolean hole2ScoreEntryDisabled) {
		this.hole2ScoreEntryDisabled = hole2ScoreEntryDisabled;
	}

	public boolean isHole3ScoreEntryDisabled() {
		return hole3ScoreEntryDisabled;
	}

	public void setHole3ScoreEntryDisabled(boolean hole3ScoreEntryDisabled) {
		this.hole3ScoreEntryDisabled = hole3ScoreEntryDisabled;
	}

	public boolean isHole4ScoreEntryDisabled() {
		return hole4ScoreEntryDisabled;
	}

	public void setHole4ScoreEntryDisabled(boolean hole4ScoreEntryDisabled) {
		this.hole4ScoreEntryDisabled = hole4ScoreEntryDisabled;
	}

	public boolean isHole5ScoreEntryDisabled() {
		return hole5ScoreEntryDisabled;
	}

	public void setHole5ScoreEntryDisabled(boolean hole5ScoreEntryDisabled) {
		this.hole5ScoreEntryDisabled = hole5ScoreEntryDisabled;
	}

	public boolean isHole6ScoreEntryDisabled() {
		return hole6ScoreEntryDisabled;
	}

	public void setHole6ScoreEntryDisabled(boolean hole6ScoreEntryDisabled) {
		this.hole6ScoreEntryDisabled = hole6ScoreEntryDisabled;
	}

	public boolean isHole7ScoreEntryDisabled() {
		return hole7ScoreEntryDisabled;
	}

	public void setHole7ScoreEntryDisabled(boolean hole7ScoreEntryDisabled) {
		this.hole7ScoreEntryDisabled = hole7ScoreEntryDisabled;
	}

	public boolean isHole8ScoreEntryDisabled() {
		return hole8ScoreEntryDisabled;
	}

	public void setHole8ScoreEntryDisabled(boolean hole8ScoreEntryDisabled) {
		this.hole8ScoreEntryDisabled = hole8ScoreEntryDisabled;
	}

	public boolean isHole9ScoreEntryDisabled() {
		return hole9ScoreEntryDisabled;
	}

	public void setHole9ScoreEntryDisabled(boolean hole9ScoreEntryDisabled) {
		this.hole9ScoreEntryDisabled = hole9ScoreEntryDisabled;
	}

	public boolean isHole10ScoreEntryDisabled() {
		return hole10ScoreEntryDisabled;
	}

	public void setHole10ScoreEntryDisabled(boolean hole10ScoreEntryDisabled) {
		this.hole10ScoreEntryDisabled = hole10ScoreEntryDisabled;
	}

	public boolean isHole11ScoreEntryDisabled() {
		return hole11ScoreEntryDisabled;
	}

	public void setHole11ScoreEntryDisabled(boolean hole11ScoreEntryDisabled) {
		this.hole11ScoreEntryDisabled = hole11ScoreEntryDisabled;
	}

	public boolean isHole12ScoreEntryDisabled() {
		return hole12ScoreEntryDisabled;
	}

	public void setHole12ScoreEntryDisabled(boolean hole12ScoreEntryDisabled) {
		this.hole12ScoreEntryDisabled = hole12ScoreEntryDisabled;
	}

	public boolean isHole13ScoreEntryDisabled() {
		return hole13ScoreEntryDisabled;
	}

	public void setHole13ScoreEntryDisabled(boolean hole13ScoreEntryDisabled) {
		this.hole13ScoreEntryDisabled = hole13ScoreEntryDisabled;
	}

	public boolean isHole14ScoreEntryDisabled() {
		return hole14ScoreEntryDisabled;
	}

	public void setHole14ScoreEntryDisabled(boolean hole14ScoreEntryDisabled) {
		this.hole14ScoreEntryDisabled = hole14ScoreEntryDisabled;
	}

	public boolean isHole15ScoreEntryDisabled() {
		return hole15ScoreEntryDisabled;
	}

	public void setHole15ScoreEntryDisabled(boolean hole15ScoreEntryDisabled) {
		this.hole15ScoreEntryDisabled = hole15ScoreEntryDisabled;
	}

	public boolean isHole16ScoreEntryDisabled() {
		return hole16ScoreEntryDisabled;
	}

	public void setHole16ScoreEntryDisabled(boolean hole16ScoreEntryDisabled) {
		this.hole16ScoreEntryDisabled = hole16ScoreEntryDisabled;
	}

	public boolean isHole17ScoreEntryDisabled() {
		return hole17ScoreEntryDisabled;
	}

	public void setHole17ScoreEntryDisabled(boolean hole17ScoreEntryDisabled) {
		this.hole17ScoreEntryDisabled = hole17ScoreEntryDisabled;
	}

	public boolean isHole18ScoreEntryDisabled() {
		return hole18ScoreEntryDisabled;
	}

	public void setHole18ScoreEntryDisabled(boolean hole18ScoreEntryDisabled) {
		this.hole18ScoreEntryDisabled = hole18ScoreEntryDisabled;
	}

	public Date getSignupDateTime() {
		return signupDateTime;
	}

	public void setSignupDateTime(Date signupDateTime) {
		this.signupDateTime = signupDateTime;
	}

	public BigDecimal getPlayerHandicapIndex() {
		return playerHandicapIndex;
	}

	public void setPlayerHandicapIndex(BigDecimal playerHandicapIndex) {
		this.playerHandicapIndex = playerHandicapIndex;
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

	public String getRoundID() {
		return roundID;
	}

	public void setRoundID(String roundID) {
		this.roundID = roundID;
	}

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public String getPlayerID() {
		return playerID;
	}

	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}

	public void setTeeTimeID(String teeTimeID) {
		this.teeTimeID = teeTimeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public String getTeeTimeID() {
		return teeTimeID;
	}

	public String getCourseTeeID() {
		return courseTeeID;
	}

	public int getOldRoundID() {
		return oldRoundID;
	}

	public void setOldRoundID(int oldRoundID) {
		this.oldRoundID = oldRoundID;
	}

	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}

	public int getOldPlayerID() {
		return oldPlayerID;
	}

	public void setOldPlayerID(int oldPlayerID) {
		this.oldPlayerID = oldPlayerID;
	}

	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}
}

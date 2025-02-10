package com.pas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class TeeTime implements Serializable
{
	private static final long serialVersionUID = 1L;

	//private static Logger logger = LogManager.getLogger(TeeTime.class);
	
	private String teeTimeID;
	private int oldTeeTimeID;
	private String gameID;
	private int oldGameID;
	private int playGroupNumber;
	private String teeTimeString;
	
	private Date gameDate;
	
	private String courseName;
		
	private List<TeeTime> teeTimeList = new ArrayList<TeeTime>();
	
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
        return Objects.equals(teeTimeID, that);
    }
	
	public int getPlayGroupNumber() {
		return playGroupNumber;
	}
	public void setPlayGroupNumber(int playGroupNumber) {
		this.playGroupNumber = playGroupNumber;
	}
	
	public String getTeeTimeString() {
		return teeTimeString;
	}

	public void setTeeTimeString(String teeTimeString) {
		this.teeTimeString = teeTimeString;
	}
	
	public List<TeeTime> getTeeTimeList() {
		return teeTimeList;
	}

	public void setTeeTimeList(List<TeeTime> teeTimeList) {
		this.teeTimeList = teeTimeList;
	}
	
	public Date getGameDate() 
	{
		return gameDate;
	}

	public void setGameDate(Date gameDate) 
	{
		this.gameDate = gameDate;
	}

	public String getCourseName() {
		return courseName;
	}

	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public String getTeeTimeID() {
		return teeTimeID;
	}

	public void setTeeTimeID(String teeTimeID) {
		this.teeTimeID = teeTimeID;
	}

	public String getGameID() {
		return gameID;
	}

	public void setGameID(String gameID) {
		this.gameID = gameID;
	}

	public int getOldTeeTimeID() {
		return oldTeeTimeID;
	}

	public void setOldTeeTimeID(int oldTeeTimeID) {
		this.oldTeeTimeID = oldTeeTimeID;
	}

	public int getOldGameID() {
		return oldGameID;
	}

	public void setOldGameID(int oldGameID) {
		this.oldGameID = oldGameID;
	}
	
}

package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;

public class SkinWinnings implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	private String playerID;
	private String playerName;
	private String winDescription; //should say something like <score> on hole <hole number>
	private BigDecimal amountWon;
	
	public String getPlayerName() {
		return playerName;
	}
	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}
	public String getWinDescription() {
		return winDescription;
	}
	public void setWinDescription(String winDescription) {
		this.winDescription = winDescription;
	}
	public BigDecimal getAmountWon() {
		return amountWon;
	}
	public void setAmountWon(BigDecimal amountWon) {
		this.amountWon = amountWon;
	}
	public String getPlayerID() {
		return playerID;
	}
	public void setPlayerID(String playerID) {
		this.playerID = playerID;
	}
	
	
}

package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;

@Named("pc_PlayerMoney")
@SessionScoped
public class PlayerMoney extends SpringBeanAutowiringSupport implements Serializable 
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(PlayerMoney.class);	
	
	private String playerMoneyID;
	private int oldPlayerMoneyID;
	private String gameID;
	private int oldGameID;
	private String playerID;
	private int oldPlayerID;
	private String description;
	private BigDecimal amount;
	
	private Game game;
	private Player player;
	
	private List<PlayerMoney> individualPlayerMoneyList = new ArrayList<PlayerMoney>();
	private List<PlayerMoney> fullPlayerMoneyList = new ArrayList<PlayerMoney>();
	private List<PlayerMoney> totaledPlayerMoneyList = new ArrayList<PlayerMoney>();
	
	private PlayerMoney selectedPlayerMoney;
	
	private BigDecimal totalAmount;
	
	public void onLoadPlayerMoney() 
	{
		logger.info("about to run Player Money");
		runTheMoney();		
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
        return Objects.equals(playerMoneyID, that);
    }
	
	public String runTheMoney()
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");
		
		this.setFullPlayerMoneyList(golfmain.getPlayerMoneyList());
		
		if (this.getFullPlayerMoneyList().size() > 0)
		{
			PlayerMoney pm1 = this.getFullPlayerMoneyList().get(0);
			Player priorPlayer = pm1.getPlayer();
			String priorPlayerID = priorPlayer.getPlayerID();
			
			BigDecimal playerTotal = new BigDecimal(0.0);
			
			for (int i = 0; i < this.getFullPlayerMoneyList().size(); i++) 
			{
				PlayerMoney pm = this.getFullPlayerMoneyList().get(i);
				
				if (pm.getPlayerID().equalsIgnoreCase(priorPlayerID))
				{
					playerTotal = playerTotal.add(pm.getAmount());
				}
				else
				{
					PlayerMoney totalPM = new PlayerMoney();
					totalPM.setPlayer(priorPlayer);
					totalPM.setAmount(playerTotal);
					this.getTotaledPlayerMoneyList().add(totalPM);
					
					priorPlayer = pm.getPlayer();
					priorPlayerID = priorPlayer.getPlayerID();
					playerTotal = new BigDecimal(0);
					playerTotal = playerTotal.add(pm.getAmount());
				}
			}
			
			PlayerMoney totalPM = new PlayerMoney();
			PlayerMoney lastPM = this.getFullPlayerMoneyList().get(this.getFullPlayerMoneyList().size()-1);
			totalPM.setPlayer(lastPM.getPlayer());
			totalPM.setAmount(playerTotal);
			this.getTotaledPlayerMoneyList().add(totalPM);
		}
		return "";
	}
	
	public String getPlayerMoneyDetail(PlayerMoney pm)
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
		
		this.setSelectedPlayerMoney(pm);
		this.setTotalAmount(this.getSelectedPlayerMoney().getAmount());
		this.setIndividualPlayerMoneyList(golfmain.getPlayerMoneyByPlayer(pm.getPlayer()));
		
		return "";
	}
		
	public static class PlayerMoneyComparatorByLastNameFirstName implements Comparator<PlayerMoney> 
	{
		public int compare(PlayerMoney playerMoney1, PlayerMoney playerMoney2)
		{
			String lastName1 = playerMoney1.getPlayer().getLastName();
			String lastName2 = playerMoney2.getPlayer().getLastName();
			
			int lastNameComparison = lastName1.compareTo(lastName2);
			
			if (lastNameComparison != 0) 
		    {
		      	return lastNameComparison;
		    }
			
			//if we get this far we need to compare first names too
			return playerMoney1.getPlayer().getFirstName().compareTo(playerMoney2.getPlayer().getFirstName());
		}		
	}
	
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	public Game getGame() {
		return game;
	}
	public void setGame(Game game) {
		this.game = game;
	}
	public Player getPlayer() {
		return player;
	}
	public void setPlayer(Player player) {
		this.player = player;
	}
	public List<PlayerMoney> getFullPlayerMoneyList() {
		return fullPlayerMoneyList;
	}
	public void setFullPlayerMoneyList(List<PlayerMoney> fullPlayerMoneyList) {
		this.fullPlayerMoneyList = fullPlayerMoneyList;
	}
	public List<PlayerMoney> getTotaledPlayerMoneyList() {
		return totaledPlayerMoneyList;
	}
	public void setTotaledPlayerMoneyList(List<PlayerMoney> totaledPlayerMoneyList) {
		this.totaledPlayerMoneyList = totaledPlayerMoneyList;
	}
	public PlayerMoney getSelectedPlayerMoney() {
		return selectedPlayerMoney;
	}
	public void setSelectedPlayerMoney(PlayerMoney selectedPlayerMoney) {
		this.selectedPlayerMoney = selectedPlayerMoney;
	}

	public List<PlayerMoney> getIndividualPlayerMoneyList() {
		return individualPlayerMoneyList;
	}

	public void setIndividualPlayerMoneyList(List<PlayerMoney> individualPlayerMoneyList) {
		this.individualPlayerMoneyList = individualPlayerMoneyList;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public void setTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}

	public String getPlayerMoneyID() {
		return playerMoneyID;
	}

	public void setPlayerMoneyID(String playerMoneyID) {
		this.playerMoneyID = playerMoneyID;
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

	public int getOldPlayerMoneyID() {
		return oldPlayerMoneyID;
	}

	public void setOldPlayerMoneyID(int oldPlayerMoneyID) {
		this.oldPlayerMoneyID = oldPlayerMoneyID;
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
	
}

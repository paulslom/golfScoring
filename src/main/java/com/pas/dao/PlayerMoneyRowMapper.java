package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.Player;
import com.pas.beans.PlayerMoney;
import com.pas.util.BeanUtilJSF;

@Repository
public class PlayerMoneyRowMapper implements RowMapper<PlayerMoney>, Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public PlayerMoney mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
    	PlayerMoney playerMoney = new PlayerMoney();
    	
    	playerMoney.setPlayerMoneyID(rs.getInt("idplayerMoney"));
		playerMoney.setGameID(rs.getInt("idgame"));
		playerMoney.setPlayerID(rs.getInt("idplayer"));
		playerMoney.setDescription(rs.getString("description"));	
		playerMoney.setAmount(rs.getBigDecimal("amount"));
		
		Game game = getGame(playerMoney.getGameID());
		Player player = getPlayer(playerMoney.getPlayerID());
		
		playerMoney.setPlayer(player);
		playerMoney.setGame(game);
								 		
 		return playerMoney;     	
    }
    
    private Game getGame(int gameID) 
  	{
      	GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
      	Game game = golfmain.getGamesMap().get(gameID);
  		return game;
  	}
  	
  	private Player getPlayer(int playerID) 
  	{
  		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
  		Player player = golfmain.getFullPlayerMap().get(playerID);
  		return player;
  	}
    
}

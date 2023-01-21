package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.PlayerMoney;

@Repository
public class PlayerMoneyRowMapper implements RowMapper<PlayerMoney>, Serializable
{
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
								 		
 		return playerMoney;     	
    }
       
}

package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.Player;

@Repository
public class PlayerRowMapper implements RowMapper<Player>, Serializable 
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Player mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
    	Player player = new Player();
    	
		player.setPlayerID(rs.getInt("idplayer"));
		player.setFirstName(rs.getString("firstName"));
		player.setLastName(rs.getString("lastName"));
		player.setFullName(player.getFirstName() + " " + player.getLastName());
		player.setHandicap(rs.getBigDecimal("currentHandicapIndex"));
		player.setUsername(rs.getString("username"));
		player.setEmailAddress(rs.getString("emailAddress"));
		
		Integer active = rs.getInt("bactive");
		if (active == 1)
		{
			player.setActive(true);
		}
		else
		{
			player.setActive(false);
		}
 		return player; 	
    	
    }
}

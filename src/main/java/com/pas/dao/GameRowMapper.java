package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.Game;

public class GameRowMapper implements RowMapper<Game> , Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Game mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
    	Game game = new Game();    	
     	
        game.setGameID(rs.getInt("idgame"));
		game.setCourseID(rs.getInt("idgolfcourse"));		
		game.setGameDate(rs.getDate("gameDate"));
		game.setBetAmount(rs.getBigDecimal("betAmount"));
		game.setEachBallWorth(rs.getBigDecimal("teamBallValue"));
		game.setHowManyBalls(rs.getInt("teamBalls"));
		game.setIndividualGrossPrize(rs.getBigDecimal("individualLowGrossPrize"));
		game.setIndividualNetPrize(rs.getBigDecimal("individualLowNetPrize"));
		game.setPurseAmount(rs.getBigDecimal("purseAmount"));
		game.setSkinsPot(rs.getBigDecimal("skinsPot"));
		game.setTeamPot(rs.getBigDecimal("teamPot"));
		game.setFieldSize(rs.getInt("fieldSize"));
		
		try 
		{
			game.setTotalPlayers(rs.getInt("totalPlayers"));
			game.setTotalTeams(rs.getInt("totalTeams"));
		} 
		catch (Exception e) 
		{			
			throw new SQLException(e.getMessage());
		}
		
		game.setGameNoteForEmail(rs.getString("gameNoteForEmail"));
		game.setPlayTheBallMethod(rs.getString("playTheBallMethod"));
		game.setGameClosedForSignups(rs.getBoolean("closedForSignups"));
        
        return game;

    }
}

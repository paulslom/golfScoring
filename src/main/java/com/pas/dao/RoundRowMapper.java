package com.pas.dao;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.Round;
import com.pas.beans.Score;

@Repository
public class RoundRowMapper implements RowMapper<Round>, Serializable 
{    
	private static final long serialVersionUID = 1L;
	
	static ZoneId etZoneId = ZoneId.of("America/New_York");

	@Override
    public Round mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
    	Round round = new Round();
    	
    	round.setRoundID(rs.getInt("idround"));
		round.setGameID(rs.getInt("idgame"));
		round.setPlayerID(rs.getInt("idplayer"));
		round.setTeamNumber(rs.getInt("teamNumber"));
		round.setTeeTimeID(rs.getInt("idTeeTimes"));
		round.setRoundHandicap(rs.getBigDecimal("roundHandicap"));
		round.setSignupDateTime(rs.getTimestamp("dSignUpdatetime"));
		round.setCourseTeeID(rs.getInt("idCourseTee"));
		
		for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
		{
			Score score = new Score();
			score.setHoleNumber(holeNumber);
		
			switch (holeNumber) 
			{
				case 1:	
					
					Integer tempInt = rs.getInt("hole1Score");
					if (tempInt == 0)
					{
						round.setHole1Score(null);
					}
					else
					{
						round.setHole1Score(rs.getInt("hole1Score"));
						score.setScore(round.getHole1Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				case 2:	
					
					tempInt = rs.getInt("hole2Score");
					if (tempInt == 0)
					{
						round.setHole2Score(null);
					}
					else
					{
						round.setHole2Score(rs.getInt("hole2Score"));
						score.setScore(round.getHole2Score());
						round.getRoundbyHoleScores().add(score);
					}
								
					break;	
					
				case 3:	
					
					tempInt = rs.getInt("hole3Score");
					if (tempInt == 0)
					{
						round.setHole3Score(null);
					}
					else
					{
						round.setHole3Score(rs.getInt("hole3Score"));
						score.setScore(round.getHole3Score());
						round.getRoundbyHoleScores().add(score);
					}
								
					break;
					
				case 4:	
					
					tempInt = rs.getInt("hole4Score");
					if (tempInt == 0)
					{
						round.setHole4Score(null);
					}
					else
					{
						round.setHole4Score(rs.getInt("hole4Score"));
						score.setScore(round.getHole4Score());
						round.getRoundbyHoleScores().add(score);
					}
								
					break;
					
				case 5:	
					
					tempInt = rs.getInt("hole5Score");
					if (tempInt == 0)
					{
						round.setHole5Score(null);
					}
					else
					{
						round.setHole5Score(rs.getInt("hole5Score"));
						score.setScore(round.getHole5Score());
						round.getRoundbyHoleScores().add(score);
					}
									
					break;
					
				case 6:	
					
					tempInt = rs.getInt("hole6Score");
					if (tempInt == 0)
					{
						round.setHole6Score(null);
					}
					else
					{
						round.setHole6Score(rs.getInt("hole6Score"));
						score.setScore(round.getHole6Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				case 7:	
					
					tempInt = rs.getInt("hole7Score");
					if (tempInt == 0)
					{
						round.setHole7Score(null);
					}
					else
					{
						round.setHole7Score(rs.getInt("hole7Score"));
						score.setScore(round.getHole7Score());
						round.getRoundbyHoleScores().add(score);
					}
								
					break;		
					
				case 8:	
					
					tempInt = rs.getInt("hole8Score");
					if (tempInt == 0)
					{
						round.setHole8Score(null);
					}
					else
					{
						round.setHole8Score(rs.getInt("hole8Score"));
						score.setScore(round.getHole8Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				case 9:	
					
					tempInt = rs.getInt("hole9Score");
					if (tempInt == 0)
					{
						round.setHole9Score(null);
					}
					else
					{
						round.setHole9Score(rs.getInt("hole9Score"));
						score.setScore(round.getHole9Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				//back 9
				case 10:	
					
					tempInt = rs.getInt("hole10Score");
					if (tempInt == 0)
					{
						round.setHole10Score(null);
					}
					else
					{
						round.setHole10Score(rs.getInt("hole10Score"));
						score.setScore(round.getHole10Score());
						round.getRoundbyHoleScores().add(score);
					}
					break;
					
				case 11:	
					
					tempInt = rs.getInt("hole11Score");
					if (tempInt == 0)
					{
						round.setHole11Score(null);
					}
					else
					{
						round.setHole11Score(rs.getInt("hole11Score"));
						score.setScore(round.getHole11Score());
						round.getRoundbyHoleScores().add(score);
					}
					break;
					
				case 12:	
					
					tempInt = rs.getInt("hole12Score");
					if (tempInt == 0)
					{
						round.setHole12Score(null);
					}
					else
					{
						round.setHole12Score(rs.getInt("hole12Score"));
						score.setScore(round.getHole12Score());
						round.getRoundbyHoleScores().add(score);
					}
					break;
					
				case 13:	
					
					tempInt = rs.getInt("hole13Score");
					if (tempInt == 0)
					{
						round.setHole13Score(null);
					}
					else
					{
						round.setHole13Score(rs.getInt("hole13Score"));
						score.setScore(round.getHole13Score());
						round.getRoundbyHoleScores().add(score);
					}
					break;
					
				case 14:	
					
					tempInt = rs.getInt("hole14Score");
					if (tempInt == 0)
					{
						round.setHole14Score(null);
					}
					else
					{
						round.setHole14Score(rs.getInt("hole14Score"));
						score.setScore(round.getHole14Score());
						round.getRoundbyHoleScores().add(score);
					}
				
					break;
					
				case 15:	
					
					tempInt = rs.getInt("hole15Score");
					if (tempInt == 0)
					{
						round.setHole15Score(null);
					}
					else
					{
						round.setHole15Score(rs.getInt("hole15Score"));
						score.setScore(round.getHole15Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				case 16:	
					
					tempInt = rs.getInt("hole16Score");
					if (tempInt == 0)
					{
						round.setHole16Score(null);
					}
					else
					{
						round.setHole16Score(rs.getInt("hole16Score"));
						score.setScore(round.getHole16Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;	
					
				case 17:	
					
					tempInt = rs.getInt("hole17Score");
					if (tempInt == 0)
					{
						round.setHole17Score(null);
					}
					else
					{
						round.setHole17Score(rs.getInt("hole17Score"));
						score.setScore(round.getHole17Score());
						round.getRoundbyHoleScores().add(score);
					}
				
					break;
					
				case 18:	
					
					tempInt = rs.getInt("hole18Score");
					if (tempInt == 0)
					{
						round.setHole18Score(null);
					}
					else
					{
						round.setHole18Score(rs.getInt("hole18Score"));
						score.setScore(round.getHole18Score());
						round.getRoundbyHoleScores().add(score);
					}
					
					break;
					
				default:
					break;
			}
			
		}
		
		round.setFront9Total(rs.getInt("front9Score"));
		round.setBack9Total(rs.getInt("back9Score"));
		round.setTotalScore(rs.getInt("totalScore"));
		round.setNetScore(rs.getBigDecimal("netScore"));
		round.setTotalToPar(rs.getString("totalToPar"));
			
		return round;
    }	
}

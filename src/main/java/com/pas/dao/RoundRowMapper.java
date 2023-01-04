package com.pas.dao;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import com.pas.beans.Course;
import com.pas.beans.CourseTee;
import com.pas.beans.Game;
import com.pas.beans.GolfMain;
import com.pas.beans.Player;
import com.pas.beans.Round;
import com.pas.beans.Score;
import com.pas.beans.TeeTime;
import com.pas.util.BeanUtilJSF;
import com.pas.util.Utils;

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
		
		CourseTee ct = getCourseTee(round.getCourseTeeID());
		if (ct != null)
	    {
			round.setCourseTeeColor(ct.getTeeColor());	    
	    }
	        
		Course course = getCourse(round.getGameID());
		Player player = getPlayer(round.getPlayerID());
		TeeTime teeTime = getTeeTime(round.getTeeTimeID());
		
		round.setPlayer(player);
		
		round.setPlayerName(player.getFirstName() + " " + player.getLastName());		
		
		if (round.getRoundHandicap() == null)
		{
			round.setRoundHandicapDifferential(new BigDecimal(0.0));
		}
		else
		{
			BigDecimal hcpIndex = player.getHandicap();
			BigDecimal hcpDifferential = hcpIndex.subtract(round.getRoundHandicap());
			round.setRoundHandicapDifferential(hcpDifferential);
		}
		
		round.setTeeTime(teeTime);
		
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
						round.setHole1StyleClass(Utils.getStyleForHole(1, round.getHole1Score(), course));			
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
						round.setHole2StyleClass(Utils.getStyleForHole(2, round.getHole2Score(), course));		
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
						round.setHole3StyleClass(Utils.getStyleForHole(3, round.getHole3Score(), course));		
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
						round.setHole4StyleClass(Utils.getStyleForHole(4, round.getHole4Score(), course));		
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
						round.setHole5StyleClass(Utils.getStyleForHole(5, round.getHole5Score(), course));	
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
						round.setHole6StyleClass(Utils.getStyleForHole(6, round.getHole6Score(), course));
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
						round.setHole7StyleClass(Utils.getStyleForHole(7, round.getHole7Score(), course));			
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
						round.setHole8StyleClass(Utils.getStyleForHole(8, round.getHole8Score(), course));	
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
						round.setHole9StyleClass(Utils.getStyleForHole(9, round.getHole9Score(), course));	
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
						round.setHole10StyleClass(Utils.getStyleForHole(10, round.getHole10Score(), course));	
					
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
						round.setHole11StyleClass(Utils.getStyleForHole(11, round.getHole11Score(), course));						
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
						round.setHole12StyleClass(Utils.getStyleForHole(12, round.getHole12Score(), course));					
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
						round.setHole13StyleClass(Utils.getStyleForHole(13, round.getHole13Score(), course));					
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
						round.setHole14StyleClass(Utils.getStyleForHole(14, round.getHole14Score(), course));	
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
						round.setHole15StyleClass(Utils.getStyleForHole(15, round.getHole15Score(), course));	
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
						round.setHole16StyleClass(Utils.getStyleForHole(16, round.getHole16Score(), course));	
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
						round.setHole17StyleClass(Utils.getStyleForHole(17, round.getHole17Score(), course));	
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
						round.setHole18StyleClass(Utils.getStyleForHole(18, round.getHole18Score(), course));	
					}
					
					break;
					
				default:
					break;
			}
			
		}
		
		round.setFront9Total(rs.getInt("front9Score"));
		round.setFront9StyleClass(Utils.getFront9StyleClass(round.getFront9Total(), course));	
		
		round.setBack9Total(rs.getInt("back9Score"));
		round.setBack9StyleClass(Utils.getBack9StyleClass(round.getBack9Total(), course));		
		
		round.setTotalScore(rs.getInt("totalScore"));
		round.setTotalStyleClass(Utils.getTotalStyleClass(round.getTotalScore(), course));		
		
		round.setNetScore(rs.getBigDecimal("netScore"));
		round.setNetStyleClass(Utils.getNetStyleClass(round.getNetScore(), course));
		
		round.setTotalToPar(rs.getString("totalToPar"));
		round.setTotalToParClass(Utils.getTotalStyleClass(round.getTotalScore(), course));		
				 		
 		return round;  	    	
    }
    
    private TeeTime getTeeTime(int teeTimeID) 
	{
    	GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
    	TeeTime teeTime = golfmain.getTeeTimesMap().get(teeTimeID);
		return teeTime;
	}
	
	private Player getPlayer(int playerID) 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
		Player player = golfmain.getFullPlayerMap().get(playerID);
		return player;
	}

	private Course getCourse(int i) 
	{
		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");			
		Game game = golfmain.getGamesMap().get(i);
		return game.getCourse();
	}
	
	private CourseTee getCourseTee(int courseTeeID) 
  	{
      	GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
      	CourseTee courseTee = golfmain.getCourseTeesMap().get(courseTeeID);
  		return courseTee;
  	}
}

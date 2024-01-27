package com.pas.dynamodb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.pas.beans.Course;
import com.pas.beans.CourseTee;
import com.pas.beans.Game;
import com.pas.beans.GolfUser;
import com.pas.beans.Group;
import com.pas.beans.Hole;
import com.pas.beans.Player;
import com.pas.beans.PlayerMoney;
import com.pas.beans.PlayerTeePreference;
import com.pas.beans.Round;
import com.pas.beans.Score;
import com.pas.beans.TeeTime;

public class CreateJSONForDynamoFromMySQL
{
	private static Logger log = LogManager.getLogger(CreateJSONForDynamoFromMySQL.class); //log4j for Logging
	 
    private static String jsonOutputFileGolfUsers = "C:/Paul/GitHub/golfScoring/src/main/resources/data/GolfUsersData.json"; 
    private static String jsonOutputFileGolfGroups = "C:/Paul/GitHub/golfScoring/src/main/resources/data/GolfGroupsData.json"; 	
    private static String jsonOutputFileCourses = "C:/Paul/GitHub/golfScoring/src/main/resources/data/CoursesData.json";     
    private static String jsonOutputFileGolfCourseTees = "C:/Paul/GitHub/golfScoring/src/main/resources/data/GolfCourseTeesData.json"; 	
    private static String jsonOutputFileGames = "C:/Paul/GitHub/golfScoring/src/main/resources/data/GamesData.json"; 
    private static String jsonOutputFileTeeTimes = "C:/Paul/GitHub/golfScoring/src/main/resources/data/TeeTimesData.json"; 	    
    private static String jsonOutputFilePlayers = "C:/Paul/GitHub/golfScoring/src/main/resources/data/PlayersData.json"; 	
    private static String jsonOutputFilePlayerTees = "C:/Paul/GitHub/golfScoring/src/main/resources/data/PlayerTeesData.json"; 	
    private static String jsonOutputFilePlayerMoney = "C:/Paul/GitHub/golfScoring/src/main/resources/data/PlayerMoneyData.json"; 
    private static String jsonOutputFileRounds = "C:/Paul/GitHub/golfScoring/src/main/resources/data/RoundsData.json"; 	
  
    public static void main(String[] args) throws Exception
    { 
    	log.debug("**********  START of program ***********");   	
    	
    	//List<GolfUser> golfUserList = getGolfUsersFromMySQLDB();  //1	
    	//List<Group> groupList = getGroupsFromMySQLDB();	//2
    	//List<Course> courseList = getCoursesFromMySQLDB();	//3
    	//List<CourseTee> courseTeeList = getCourseTeesFromMySQLDB();	//4 
    	//List<Game> gameList = getGamesFromMySQLDB();	//5
    	//List<TeeTime> teeTimesList = getTeeTimesFromMySQLDB();	//6
    	//List<Player> playerList = getPlayersFromMySQLDB();	//7
    	List<PlayerTeePreference> playerTeePreferenceList = getPlayerTeePreferenceFromMySQLDB();	//8
    	//List<PlayerMoney> playerMoneyList = getPlayerMoneyFromMySQLDB();	//9    	
    	//List<Round> roundList = getRoundsFromMySQLDB();	//10
     	
    	log.debug("********** starting write of JSON file ***********");   	
	   	//writeGolfUsersJSONFile(golfUserList);  //1
    	//writeGroupsJSONFile(groupList); //2
	   	//writeCoursesJSONFile(courseList); //3
	    //writeCourseTeeJSONFile(courseTeeList); //4
    	//writeGamesJSONFile(gameList); //5
	    //writeTeeTimesJSONFile(teeTimesList); //6
    	//writePlayersJSONFile(playerList); //7
	    writePlayerTeePreferenceJSONFile(playerTeePreferenceList); //8
    	//writePlayerMoneyJSONFile(playerMoneyList); //9    	
    	//writeRoundsJSONFile(roundList); //10	 	
    	
	   	log.debug("********** finished write of JSON file ***********");   	
	   	
		log.debug("**********  END of program ***********");		
	}

	private static List<PlayerTeePreference> getPlayerTeePreferenceFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from playertees order by idplayer";		 
    	List<PlayerTeePreference> playerTeePreferenceList = jdbcTemplate.query(sql, new ResultSetExtractor<List<PlayerTeePreference>>() 
		{	   
			@Override
		    public List<PlayerTeePreference> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<PlayerTeePreference> playerTeePreferenceList2 = new ArrayList<>();
				while (rs.next()) 
				{
					PlayerTeePreference playerTeePreference = new PlayerTeePreference();
			        
			        playerTeePreference.setOldPlayerTeePreferenceID(rs.getInt("idplayertees"));
			        playerTeePreference.setOldPlayerID(rs.getInt("idplayer"));
			        playerTeePreference.setOldCourseID(rs.getInt("idgolfcourse"));
			        playerTeePreference.setOldCourseTeeID(rs.getInt("idgolfcoursetee"));        
					
					playerTeePreferenceList2.add(playerTeePreference);
				}
				return playerTeePreferenceList2;
		    }
		});
    	
    	log.debug("successfully read in " + playerTeePreferenceList.size() + " playerTeePreference rows from DB");
		return playerTeePreferenceList;
	}

	private static List<CourseTee> getCourseTeesFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from golfcoursetees order by idgolfcourse, teecolor";		 
    	List<CourseTee> courseTeeList = jdbcTemplate.query(sql, new ResultSetExtractor<List<CourseTee>>() 
		{	   
			@Override
		    public List<CourseTee> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<CourseTee> courseTeeList2 = new ArrayList<>();
				while (rs.next()) 
				{
					CourseTee courseTee = new CourseTee();
			        
			        courseTee.setOldCourseTeeID(rs.getInt("idGolfCourseTees"));
			        courseTee.setOldCourseID(rs.getInt("idGolfCourse"));
					courseTee.setTeeColor(rs.getString("teeColor"));
					courseTee.setCourseRating(rs.getBigDecimal("courseRating"));
					courseTee.setSlopeRating(rs.getInt("courseSlope"));
					courseTee.setCoursePar(rs.getInt("coursePar"));	
					courseTee.setTotalYardage(rs.getInt("totalYardage"));
					
					courseTeeList2.add(courseTee);
				}
				return courseTeeList2;
		    }
		});
    	
    	log.debug("successfully read in " + courseTeeList.size() + " courseTee rows from DB");
		return courseTeeList;
	}

	private static List<PlayerMoney> getPlayerMoneyFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from playermoney order by idplayer";		 
    	List<PlayerMoney> playerMoneyList = jdbcTemplate.query(sql, new ResultSetExtractor<List<PlayerMoney>>() 
		{	   
			@Override
		    public List<PlayerMoney> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<PlayerMoney> playerMoneyList2 = new ArrayList<>();
				while (rs.next()) 
				{
					PlayerMoney playerMoney = new PlayerMoney();
			    	
			    	playerMoney.setOldPlayerMoneyID(rs.getInt("idplayerMoney"));
					playerMoney.setOldGameID(rs.getInt("idgame"));
					playerMoney.setOldPlayerID(rs.getInt("idplayer"));
					playerMoney.setDescription(rs.getString("description"));	
					playerMoney.setAmount(rs.getBigDecimal("amount"));	
					
		            playerMoneyList2.add(playerMoney);
				}
				return playerMoneyList2;
		    }
		});
    	
    	log.debug("successfully read in " + playerMoneyList.size() + " playerMoney rows from DB");
		return playerMoneyList;
	}

	private static List<TeeTime> getTeeTimesFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from teetimes order by idteeTimes";		 
    	List<TeeTime> teeTimesList = jdbcTemplate.query(sql, new ResultSetExtractor<List<TeeTime>>() 
		{	   
			@Override
		    public List<TeeTime> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<TeeTime> teeTimesList2 = new ArrayList<>();
				while (rs.next()) 
				{
					TeeTime teeTime = new TeeTime();
		    		
					teeTime.setOldTeeTimeID(rs.getInt("idteeTimes"));
					teeTime.setOldGameID(rs.getInt("idgame"));
					teeTime.setPlayGroupNumber(rs.getInt("playGroupNumber"));
					teeTime.setTeeTimeString(rs.getString("teeTime"));
					
		            teeTimesList2.add(teeTime);
				}
				return teeTimesList2;
		    }
		});
    	
    	log.debug("successfully read in " + teeTimesList.size() + " tee times from DB");
		return teeTimesList;
	}

	private static List<Group> getGroupsFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from golfgroup";		 
    	List<Group> groupList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Group>>() 
		{	   
			@Override
		    public List<Group> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<Group> groupList2 = new ArrayList<>();
				while (rs.next()) 
				{
					Group group = new Group();
			        
			        group.setOldGroupID(rs.getInt("idgroup"));
					group.setGroupName(rs.getString("groupName"));
					
		            groupList2.add(group);
				}
				return groupList2;
		    }
		});
    	
    	log.debug("successfully read in " + groupList.size() + " groups from DB");
		return groupList;
	}

	private static List<Round> getRoundsFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from round order by idgame, idround";		 
    	List<Round> roundList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Round>>() 
		{	   
			@Override
		    public List<Round> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<Round> roundList2 = new ArrayList<>();
				while (rs.next()) 
				{
					Round round = new Round();
			    	
			    	round.setOldRoundID(rs.getInt("idround"));
					round.setOldGameID(rs.getInt("idgame"));
					round.setOldPlayerID(rs.getInt("idplayer"));
					round.setTeamNumber(rs.getInt("teamNumber"));
					round.setOldTeeTimeID(rs.getInt("idTeeTimes"));
					round.setRoundHandicap(rs.getBigDecimal("roundHandicap"));
					round.setSignupDateTime(rs.getTimestamp("dSignUpdatetime"));
					round.setOldCourseTeeID(rs.getInt("idCourseTee"));
					
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
					
		            roundList2.add(round);
				}
				return roundList2;
		    }
		});
    	
    	log.debug("successfully read in " + roundList.size() + " rounds from DB");
		return roundList;
	}

	private static List<Game> getGamesFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from game order by idgame";		 
    	List<Game> gameList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Game>>() 
		{	   
			@Override
		    public List<Game> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<Game> gameList2 = new ArrayList<>();
				while (rs.next()) 
				{
					Game game = new Game();    	
			     	
			        game.setOldGameID(rs.getInt("idgame"));
					game.setOldCourseID(rs.getInt("idgolfcourse"));		
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
					
		            gameList2.add(game);
				}
				return gameList2;
		    }
		});
    	
    	log.debug("successfully read in " + gameList.size() + " games from DB");
		return gameList;
	}

	private static List<Player> getPlayersFromMySQLDB() 
	{
		MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from player order by username";		 
    	List<Player> playerList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Player>>() 
		{	   
			@Override
		    public List<Player> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<Player> playerList2 = new ArrayList<>();
				while (rs.next()) 
				{
			        Player player = new Player();
			        player.setOldPlayerID(rs.getInt("idplayer"));
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
		            playerList2.add(player);
				}
				return playerList2;
		    }
		});
    	
    	log.debug("successfully read in " + playerList.size() + " players from DB");
		return playerList;
	}

	private static List<GolfUser> getGolfUsersFromMySQLDB()
    {
    	MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from golfUsers order by username";		 
    	List<GolfUser> userList = jdbcTemplate.query(sql, new ResultSetExtractor<List<GolfUser>>() 
		{	   
			@Override
		    public List<GolfUser> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<GolfUser> userList2 = new ArrayList<>();
				while (rs.next()) 
				{
			        GolfUser GolfUser2 = new GolfUser();
			        GolfUser2.setUserId(rs.getInt("user_id"));
			        GolfUser2.setUserName(rs.getString("username").toLowerCase());
			        GolfUser2.setPassword(rs.getString("password"));
			        GolfUser2.setUserRole(rs.getString("role"));		 
		            userList2.add(GolfUser2);
				}
				return userList2;
		    }
		});
    	
    	log.debug("successfully read in " + userList.size() + " users from DB");
		return userList;
	}
    
    private static List<Course> getCoursesFromMySQLDB()
    {
    	MysqlDataSource ds = getMySQLDatasource();
    	JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);    
    	String sql = "select * from golfcourse";		 
    	List<Course> courseList = jdbcTemplate.query(sql, new ResultSetExtractor<List<Course>>() 
		{	   
			@Override
		    public List<Course> extractData(ResultSet rs) throws SQLException, DataAccessException 
		    {
				List<Course> courseList2 = new ArrayList<>();
				while (rs.next()) 
				{
			        Course course = new Course();
			        course.setOldCourseID(rs.getInt("idGolfCourse"));
					course.setCourseName(rs.getString("courseName"));
					course.setFront9Par(rs.getInt("front9Par"));
					course.setBack9Par(rs.getInt("back9Par"));
					course.setCoursePar(rs.getInt("coursePar"));
									
					for (int holeNumber = 1; holeNumber <= 18; holeNumber++) 
					{
						Hole hole = new Hole();
						hole.setCourseID(course.getCourseID());
						hole.setHoleNumber(holeNumber);
						
						switch (holeNumber) 
						{
							case 1:	
								
								hole.setPar(rs.getInt("hole1Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 2:	
								
								hole.setPar(rs.getInt("hole2Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;	
								
							case 3:	
								
								hole.setPar(rs.getInt("hole3Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 4:	
								
								hole.setPar(rs.getInt("hole4Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;			
								
							case 5:	
								
								hole.setPar(rs.getInt("hole5Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 6:	
								
								hole.setPar(rs.getInt("hole6Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;	
								
							case 7:	
								
								hole.setPar(rs.getInt("hole7Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;			
								
							case 8:	
								
								hole.setPar(rs.getInt("hole8Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 9:	
								
								hole.setPar(rs.getInt("hole9Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;	
							
							//back 9
							case 10:	
								
								hole.setPar(rs.getInt("hole10Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 11:	
								
								hole.setPar(rs.getInt("hole11Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;	
								
							case 12:	
								
								hole.setPar(rs.getInt("hole12Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 13:	
								
								hole.setPar(rs.getInt("hole13Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;			
								
							case 14:	
								
								hole.setPar(rs.getInt("hole14Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 15:	
								
								hole.setPar(rs.getInt("hole15Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;	
								
							case 16:	
								
								hole.setPar(rs.getInt("hole16Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;			
								
							case 17:	
								
								hole.setPar(rs.getInt("hole17Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;
								
							case 18:	
								
								hole.setPar(rs.getInt("hole18Par"));
								course.getHolesList().add(hole);
								course.getHolesMap().put(holeNumber, hole);
								break;		
								
							default:
								break;
						}
						
					}				
		            courseList2.add(course);
				}
				return courseList2;
		    }
		});
    	
    	log.debug("successfully read in " + courseList.size() + " courses from DB");
		return courseList;
	}
    
   
	/*
	 * example output json
	 * {
            "userId": "1",
            "userName": "paulslom",
            "password": "xxxfff",
            "userRole": "ADMIN"
            
              GolfUser2.setUserId(rs.getInt("user_id"));
			        GolfUser2.setUserName(rs.getString("username").toLowerCase());
			        GolfUser2.setPassword(rs.getString("password"));
			        GolfUser2.setUserRole(rs.getString("role"));		 
		        
        },
	 */
	private static void writeGolfUsersJSONFile(List<GolfUser> golfUserList) 
	{
		BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileGolfUsers, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
			
			for (int i = 0; i < golfUserList.size(); i++) 
			{
				GolfUser gu = golfUserList.get(i);
				
				bw.write("{");
				
				bw.newLine();				
				bw.write("\t\"userId\": \"" + gu.getUserId() + "\"," );
				bw.newLine();	
				bw.write("\t\"userName\": \"" + gu.getUserName() + "\"," );
				bw.newLine();	
				bw.write("\t\"password\": \"" + gu.getPassword() + "\"," );
				bw.newLine();
				bw.write("\t\"userRole\": \"" + gu.getUserRole() + "\"" );
				bw.newLine();
				
				if (i == golfUserList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}	

    private static void writeCoursesJSONFile(List<Course> courseList) 
    {
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileCourses, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
			
			for (int i = 0; i < courseList.size(); i++) 
			{
				Course course = courseList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"courseID\":" + course.getOldCourseID() + "," );
				bw.newLine();	
				bw.write("\t\"idGroup\": 1," );
				bw.newLine();	
				bw.write("\t\"courseName\": \"" + course.getCourseName() + "\"," );
				bw.newLine();
				bw.write("\t\"front9Par\":" + course.getFront9Par() + "," );
				bw.newLine();				
				bw.write("\t\"back9Par\":" + course.getBack9Par() + "," );
				bw.newLine();	
				bw.write("\t\"coursePar\":" + course.getCoursePar() + "," );
				bw.newLine();
				bw.write("\t\"holesList\":");
				bw.newLine();
				bw.write("\t[");
				bw.newLine();
				
				for (int j = 0; j < course.getHolesList().size(); j++) 
				{					
					bw.write("\t\t{");
					bw.newLine();
					bw.write("\t\t\t\"holeNumber\":" + course.getHolesList().get(j).getHoleNumber() + "," );
					bw.newLine();	
					bw.write("\t\t\t\"par\":" + course.getHolesList().get(j).getPar() );
					bw.newLine();
					
					if (j < 17)
					{
						bw.write("\t\t},");
					}
					else
					{
						bw.write("\t\t}");
						bw.newLine();
						bw.write("\t]");
						
					}
					
					bw.newLine();
				}
					
				if (i == courseList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}    

    private static void writeTeeTimesJSONFile(List<TeeTime> teeTimesList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileTeeTimes, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < teeTimesList.size(); i++) 
			{
				TeeTime teeTime = teeTimesList.get(i);
				
				bw.write("{");
			
				bw.newLine();				
				bw.write("\t\"oldTeeTimeID\":" + teeTime.getOldTeeTimeID() + "," );
				bw.newLine();
				bw.write("\t\"oldGameID\":" + teeTime.getOldGameID() + "," );
				bw.newLine();	
				bw.write("\t\"playGroupNumber\":" + teeTime.getPlayGroupNumber() + "," );
				bw.newLine();	
				bw.write("\t\"teeTimeString\": \"" + teeTime.getTeeTimeString()  + "\"");
				bw.newLine();
								
				if (i == teeTimesList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}
    
    private static void writePlayerMoneyJSONFile(List<PlayerMoney> playerMoneyList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFilePlayerMoney, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < playerMoneyList.size(); i++) 
			{
				PlayerMoney playerMoney = playerMoneyList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"idplayerMoney\":" + playerMoney.getPlayerMoneyID() + "," );
				bw.newLine();
				bw.write("\t\"idgame\":" + playerMoney.getGameID() + "," );
				bw.newLine();	
				bw.write("\t\"idplayer\":" + playerMoney.getPlayerID() + "," );
				bw.newLine();	
				bw.write("\t\"amount\":" + playerMoney.getAmount() + "," );
				bw.newLine();	
				bw.write("\t\"description\": \"" + playerMoney.getDescription()  + "\"");
				bw.newLine();
								
				if (i == playerMoneyList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}

    private static void writeCourseTeeJSONFile(List<CourseTee> courseTeeList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileGolfCourseTees, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < courseTeeList.size(); i++) 
			{
				CourseTee courseTee = courseTeeList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"oldCourseTeeID\":" + courseTee.getOldCourseTeeID() + "," );
				bw.newLine();
				bw.write("\t\"oldCourseID\":" + courseTee.getOldCourseID() + "," );
				bw.newLine();	
				bw.write("\t\"courseRating\":" + courseTee.getCourseRating() + "," );
				bw.newLine();	
				bw.write("\t\"courseSlope\":" + courseTee.getSlopeRating() + "," );
				bw.newLine();	
				bw.write("\t\"teeColor\": \"" + courseTee.getTeeColor() + "\"," );
				bw.newLine();	
				bw.write("\t\"coursePar\":" + courseTee.getCoursePar() + "," );
				bw.newLine();	
				bw.write("\t\"idgroup\":" + "1" + "," );
				bw.newLine();	
				bw.write("\t\"totalYardage\": \"" + courseTee.getTotalYardage()  + "\"");
				bw.newLine();
								
				if (i == courseTeeList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}
    
    private static void writePlayerTeePreferenceJSONFile(List<PlayerTeePreference> playerTeePreferenceList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFilePlayerTees, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < playerTeePreferenceList.size(); i++) 
			{
				PlayerTeePreference playerTeePreference = playerTeePreferenceList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"oldPlayerTeePreferenceID\":" + playerTeePreference.getOldPlayerTeePreferenceID() + "," );
				bw.newLine();	
				bw.write("\t\"oldPlayerID\":" + playerTeePreference.getOldPlayerID() + "," );
				bw.newLine();	
				bw.write("\t\"oldCourseID\":" + playerTeePreference.getOldCourseID() + "," );
				bw.newLine();	
				bw.write("\t\"oldCourseTeeID\":" + playerTeePreference.getOldCourseTeeID());
				bw.newLine();	
								
				if (i == playerTeePreferenceList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
	}
    
    private static void writeGroupsJSONFile(List<Group> groupList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileGolfGroups, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < groupList.size(); i++) 
			{
				Group group = groupList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"oldGroupID\":" + group.getOldGroupID() + "," );
				bw.newLine();	
				bw.write("\t\"groupName\": \"" + group.getGroupName()  + "\"");
				bw.newLine();
								
				if (i == groupList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
		
	}
    
    private static void writePlayersJSONFile(List<Player> playerList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFilePlayers, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < playerList.size(); i++) 
			{
				Player player = playerList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"oldPlayerID\":" + player.getOldPlayerID() + "," );
				bw.newLine();	
				bw.write("\t\"firstName\": \"" + player.getFirstName() + "\"," );
				bw.newLine();
				bw.write("\t\"lastName\": \"" + player.getLastName() + "\"," );
				bw.newLine();				
				bw.write("\t\"handicap\":" + player.getHandicap() + "," );
				bw.newLine();
				bw.write("\t\"username\": \"" + player.getUsername() + "\"," );
				bw.newLine();				
				bw.write("\t\"emailAddress\": \"" + player.getEmailAddress() + "\"," );
				bw.newLine();	
				bw.write("\t\"active\":" + player.isActive());
				bw.newLine();	
				
				if (i == playerList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}
	}
    
    private static void writeGamesJSONFile(List<Game> gameList) 
	{
    	BufferedWriter bw = null;
    	
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileGames, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
						
			for (int i = 0; i < gameList.size(); i++) 
			{
				Game game = gameList.get(i);
				
				bw.write("{");
				
				bw.newLine();				
				bw.write("\t\"oldGameID\":" + game.getOldGameID() + "," );
				bw.newLine();	
				bw.write("\t\"oldCourseID\":" + game.getOldCourseID() + "," );
				bw.newLine();				
				
				sdf.setTimeZone(TimeZone.getTimeZone("EST"));
				String gameDateText = sdf.format(game.getGameDate());
						
				bw.write("\t\"gameDate\": \""  + gameDateText + "\"," );
				bw.newLine();
				bw.write("\t\"betAmount\":" + game.getBetAmount() + "," );
				bw.newLine();
				bw.write("\t\"eachBallWorth\":" + game.getEachBallWorth() + "," );
				bw.newLine();
				bw.write("\t\"howManyBalls\":" + game.getHowManyBalls() + "," );
				bw.newLine();
				bw.write("\t\"individualGrossPrize\":" + game.getIndividualGrossPrize() + "," );
				bw.newLine();
				bw.write("\t\"individualNetPrize\":" + game.getIndividualNetPrize() + "," );
				bw.newLine();
				bw.write("\t\"purseAmount\":" + game.getPurseAmount() + "," );
				bw.newLine();
				bw.write("\t\"skinsPot\":" + game.getSkinsPot() + "," );
				bw.newLine();
				bw.write("\t\"teamPot\":" + game.getTeamPot() + "," );
				bw.newLine();
				bw.write("\t\"fieldSize\":" + game.getFieldSize() + "," );
				bw.newLine();
				bw.write("\t\"totalPlayers\":" + game.getTotalPlayers() + "," );
				bw.newLine();
				bw.write("\t\"totalTeams\":" + game.getTotalTeams() + "," );
				bw.newLine();
				bw.write("\t\"gameNoteForEmail\": \"" + game.getGameNoteForEmail() + "\"," );
				bw.newLine();				
				bw.write("\t\"playTheBallMethod\": \"" + game.getPlayTheBallMethod() + "\"," );
				bw.newLine();	
				bw.write("\t\"closedForSignups\":" + game.isGameClosedForSignups());
				bw.newLine();	
				
				if (i == gameList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}		
	}
    
    private static void writeRoundsJSONFile(List<Round> roundList) 
	{
    	BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFileRounds, false);
			bw = new BufferedWriter(fw);
			
			bw.write("[");
			bw.newLine();	
			
			for (int i = 0; i < roundList.size(); i++) 
			{
				Round round = roundList.get(i);
				
				bw.write("{");
					
				bw.newLine();				
				bw.write("\t\"idround\":" + round.getRoundID() + "," );
				bw.newLine();	
				bw.write("\t\"idgame\":" + round.getGameID() + "," );
				bw.newLine();
				bw.write("\t\"idplayer\":" + round.getPlayerID() + "," );
				bw.newLine();
				bw.write("\t\"idteetimes\":" + round.getTeeTimeID() + "," );
				bw.newLine();
				bw.write("\t\"teamnumber\":" + round.getTeamNumber() + "," );
				bw.newLine();
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm a z");
				sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
					
				bw.write("\t\"dSignupDateTime\": \""  + sdf.format(round.getSignupDateTime()) + "\"," );
				bw.newLine();
				
				bw.write("\t\"netscore\":" + round.getNetScore() + "," );
				bw.newLine();
				bw.write("\t\"roundhandicap\":" + round.getRoundHandicap() + "," );
				bw.newLine();
				bw.write("\t\"idcoursetee\":" + round.getCourseTeeID() + "," );
				bw.newLine();
				bw.write("\t\"totaltopar\": \"" + round.getTotalToPar() + "\"," );
				bw.newLine();	
				
				bw.write("\t\"front9Score\":" + round.getFront9Total() + "," );
				bw.newLine();				
				bw.write("\t\"back9Score\":" + round.getBack9Total() + "," );
				bw.newLine();	
				bw.write("\t\"courseScore\":" + round.getTotalScore() + "," );
				bw.newLine();	
				bw.write("\t\"hole1Score\":" + round.getHole1Score() + "," );
				bw.newLine();
				bw.write("\t\"hole2Score\":" + round.getHole2Score() + "," );
				bw.newLine();				
				bw.write("\t\"hole3Score\":" + round.getHole3Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole4Score\":" + round.getHole4Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole5Score\":" + round.getHole5Score() + "," );
				bw.newLine();
				bw.write("\t\"hole6Score\":" + round.getHole6Score() + "," );
				bw.newLine();				
				bw.write("\t\"hole7Score\":" + round.getHole7Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole8Score\":" + round.getHole8Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole9Score\":" + round.getHole9Score() + "," );
				bw.newLine();
				bw.write("\t\"hole10Score\":" + round.getHole10Score() + "," );
				bw.newLine();				
				bw.write("\t\"hole11Score\":" + round.getHole11Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole12Score\":" + round.getHole12Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole13Score\":" + round.getHole13Score() + "," );
				bw.newLine();
				bw.write("\t\"hole14Score\":" + round.getHole14Score() + "," );
				bw.newLine();				
				bw.write("\t\"hole15Score\":" + round.getHole15Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole16Score\":" + round.getHole16Score() + "," );
				bw.newLine();	
				bw.write("\t\"hole17Score\":" + round.getHole17Score() + "," );
				bw.newLine();
				bw.write("\t\"hole18Score\":" + round.getHole18Score());
				bw.newLine();
				
				if (i == roundList.size() - 1)
				{
					bw.write("}");
				}
				else
				{
					bw.write("},");
				}
				bw.newLine();				
			}
			
			bw.write("]");
			bw.newLine();	
	
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		finally
		{
			try 
			{
				bw.close();
			} 
			catch (IOException e)
			{				
				e.printStackTrace();
			}
		}		
	}

	
    private static MysqlDataSource getMySQLDatasource()
	{
		MysqlDataSource ds = null;
		
		Properties prop = new Properties();
		
	    try 
	    {
	    	
	    	InputStream stream = new FileInputStream(new File("C:\\EclipseProjects\\GolfScoringWS\\Servers\\Tomcat v10.1 Server at localhost-config/catalina.properties")); 
	    	prop.load(stream);   		
		
	    	ds = new MysqlDataSource();
	    	
		    String dbName = prop.getProperty("RDS_DB_NAME");
		    String userName = prop.getProperty("RDS_USERNAME");
		    String password = prop.getProperty("RDS_PASSWORD");
		    String hostname = prop.getProperty("RDS_HOSTNAME");
		    String port = prop.getProperty("RDS_PORT");
		    String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
		    
		    //log.info("jdbcUrl for datasource: " + jdbcUrl);
		    
		    ds.setURL(jdbcUrl);
		    ds.setPassword(password);
		    ds.setUser(userName);
		    
		 }
		 catch (Exception e) 
	     { 
		    log.error(e.toString());
		 }     		
       	
       	return ds;
	}
		
}
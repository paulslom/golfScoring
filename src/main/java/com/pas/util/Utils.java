package com.pas.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//import com.mysql.cj.jdbc.MysqlDataSource;
import com.pas.beans.Course;
import com.pas.beans.Round;
import com.pas.beans.Score;
import com.pas.beans.TeeTime;
import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGame;
import com.pas.dynamodb.DynamoPlayer;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.servlet.http.HttpSession;

public class Utils 
{
	private static Logger logger = LogManager.getLogger(Utils.class);	
	
	private static ResourceBundle genericProps = ResourceBundle.getBundle("ApplicationProperties");
	
	public static String PAR_OR_WORSE_STYLECLASS = "textBlack";
	public static String BIRDIE_OR_BETTER_STYLECLASS = "textRed";
	
	public static int FRONT9_STYLE_HOLENUM = 19;
	public static int BACK9_STYLE_HOLENUM = 20;
	public static int TOTAL_STYLE_HOLENUM = 21;
	public static int NET_STYLE_HOLENUM = 22;
	
	public static String MY_TIME_ZONE = "America/New_York";
	
	public static String NEWLINE = "<br/>";	
	
	//private static String WEBSITE_URL = "http://golfscoring-2.us-east-1.elasticbeanstalk.com";  //original RDS DB one
	public static String WEBSITE_URL = "http://golfscoring-springboot-env.eba-bir4zg4h.us-east-1.elasticbeanstalk.com"; //SpringBoot-DynamoDB-JDK17
		
	public static String getLastYearsLastDayDate() 
	{
	    Calendar prevYear = Calendar.getInstance();
	    prevYear.add(Calendar.YEAR, -1);
	    String returnDate = prevYear.get(Calendar.YEAR) + "-12-31";
	    return returnDate;
	}
	
	public static String getOneMonthAgoDate() 
	{
	    Calendar calOneMonthAgo = Calendar.getInstance();
	    calOneMonthAgo.add(Calendar.MONTH, -1);
	    Date dateOneMonthAgo = calOneMonthAgo.getTime();
	    Locale locale = Locale.getDefault();
	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
	    String returnDate = formatter.format(dateOneMonthAgo);
	    return returnDate;
	}
	
	public static String getThreeMonthsAgoDate() 
	{
	    Calendar calThreeMonthsAgo = Calendar.getInstance();
	    calThreeMonthsAgo.add(Calendar.MONTH, -3);
	    Date dateOneMonthAgo = calThreeMonthsAgo.getTime();
	    Locale locale = Locale.getDefault();
	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
	    String returnDate = formatter.format(dateOneMonthAgo);
	    return returnDate;
	}
	
	public static String getOneYearAgoDate() 
	{
	    Calendar calOneYearAgo = Calendar.getInstance();
	    calOneYearAgo.add(Calendar.YEAR, -1);
	    Date dateOneYearAgo = calOneYearAgo.getTime();
	    Locale locale = Locale.getDefault();
	    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", locale);
	    String returnDate = formatter.format(dateOneYearAgo);
	    return returnDate;
	}
	
	// function to round the number 
	public static int roundToNearestMultipleOfTen(int n) 
	{ 
	    // Smaller multiple 
	    int a = (n / 10) * 10; 
	      
	    // Larger multiple 
	    int b = a + 10; 
	  
	    // Return of closest of two 
	    return (n - a > b - n)? b : a; 
	}
	
	public static Integer setRecommendedTeams(Integer inputPlayers) 
	{
		Integer recommendedTeams = 2;
		
		switch (inputPlayers) 
		{
			case 7:
			case 11:
			case 13:
			case 17:
			case 19:
			case 22:
			case 23:
			case 26:
			case 29:
				recommendedTeams = 0;
				break;	

			case 6:
			case 8:
			case 10:
			case 14:
				recommendedTeams = 2;
				break;
			
			case 9:
			case 12:
			case 15:
			case 18:
			case 21:
			case 27:
				recommendedTeams = 3;
				break;	
	
			case 16:
			case 20:			
			case 28:
				recommendedTeams = 4;
				break;	
			
			case 25:			
				recommendedTeams = 5;
				break;
				
			case 24:
			case 30:
				recommendedTeams = 6;
				break;
			default:
				recommendedTeams = 0;
				break;
		}
		return recommendedTeams;
	}
	
	public static int getTeamScoreOnHole(List<Round> roundsList, int holeNumber, int ballNumber)
	{
		//get a list of scores for this hole
		List<Integer> holeScoreList = new ArrayList<Integer>();
		for (int i = 0; i < roundsList.size(); i++) 
		{
			Round round = roundsList.get(i);
			
			Score score = new Score();
			
			if (holeNumber == 1 && round.getHole1Score() != null)
			{
				score.setScore(round.getHole1Score());
			}
			else if (holeNumber == 2 && round.getHole2Score() != null)
			{
				score.setScore(round.getHole2Score());
			}
			else if (holeNumber == 3 && round.getHole3Score() != null)
			{
				score.setScore(round.getHole3Score());
			}
			else if (holeNumber == 4 && round.getHole4Score() != null)
			{
				score.setScore(round.getHole4Score());
			}
			else if (holeNumber == 5 && round.getHole5Score() != null)
			{
				score.setScore(round.getHole5Score());
			}
			else if (holeNumber == 6 && round.getHole6Score() != null)
			{
				score.setScore(round.getHole6Score());
			}
			else if (holeNumber == 7 && round.getHole7Score() != null)
			{
				score.setScore(round.getHole7Score());
			}
			else if (holeNumber == 8 && round.getHole8Score() != null)
			{
				score.setScore(round.getHole8Score());
			}
			else if (holeNumber == 9 && round.getHole9Score() != null)
			{
				score.setScore(round.getHole9Score());
			}
			else if (holeNumber == 10 && round.getHole10Score() != null)
			{
				score.setScore(round.getHole10Score());
			}
			else if (holeNumber == 11 && round.getHole11Score() != null)
			{
				score.setScore(round.getHole11Score());
			}
			else if (holeNumber == 12 && round.getHole12Score() != null)
			{
				score.setScore(round.getHole12Score());
			}
			else if (holeNumber == 13 && round.getHole13Score() != null)
			{
				score.setScore(round.getHole13Score());
			}
			else if (holeNumber == 14 && round.getHole14Score() != null)
			{
				score.setScore(round.getHole14Score());
			}
			else if (holeNumber == 15 && round.getHole15Score() != null)
			{
				score.setScore(round.getHole15Score());
			}
			else if (holeNumber == 16 && round.getHole16Score() != null)
			{
				score.setScore(round.getHole16Score());
			}
			else if (holeNumber == 17 && round.getHole17Score() != null)
			{
				score.setScore(round.getHole17Score());
			}
			else if (holeNumber == 18 && round.getHole18Score() != null)
			{
				score.setScore(round.getHole18Score());
			}
			
			holeScoreList.add(score.getScore());
		}
		
		//holeScoreList has the scores for this team for this hole
		Collections.sort(holeScoreList);
		
		int returnScore = holeScoreList.get(ballNumber-1); //since it's sorted, we should just be able to take the index of the ball in question
				
		return returnScore;
	}
	
	public static Date getGameDateTimeUsingTeeTimeString(Date gameDate, String teeTimesString) 
	{
		if (teeTimesString == null)
		{
			return gameDate;
		}
		
		StringTokenizer st = new StringTokenizer(teeTimesString, " ");
		String teeTimeStr = "";	     
	 	while (st.hasMoreTokens()) 
	 	{	 			
	 		teeTimeStr = st.nextToken();
	 		break;
	 	}
		Date returnDate = new Date();
		
		if (teeTimeStr != null && teeTimeStr.trim().length() > 0)
		{
			Calendar calendar = Calendar.getInstance(); 
	        calendar.setTime(gameDate); 
	   
	        try
	        {
	        	String hour = "";
		        String minute = "";
		        
		        StringTokenizer stTime = new StringTokenizer(teeTimeStr, ":");
		       	hour = stTime.nextToken();
			 	minute = stTime.nextToken();
	        	calendar.add(Calendar.HOUR_OF_DAY, Integer.parseInt(hour)); 
	        	calendar.add(Calendar.MINUTE, Integer.parseInt(minute)); 
	        }
	        catch (Exception e) //ignore if we can't set hour and minute
	        {	        	
	        }
	        
	        returnDate = calendar.getTime(); 
		}
		return returnDate;
	}
	public static int front9Score(Round round)
	{
		int front9 = 0;
		
		front9 = round.getHole1Score() + round.getHole2Score() + round.getHole3Score();
		front9 = front9 + round.getHole4Score() + round.getHole5Score() + round.getHole6Score();
		front9 = front9 + round.getHole7Score() + round.getHole8Score() + round.getHole9Score();
		
		return front9;
	}
	
	public static int back9Score(Round round)
	{
		int back9 = 0;
		
		back9 = round.getHole10Score() + round.getHole11Score() + round.getHole12Score();
		back9 = back9 + round.getHole13Score() + round.getHole14Score() + round.getHole15Score();
		back9 = back9 + round.getHole16Score() + round.getHole17Score() + round.getHole18Score();
		
		return back9;
	}
	
	public static int totalScore(Round round)
	{
		int totalScore = front9Score(round) + back9Score(round);
		
		return totalScore;
	}
	
	public static String getFront9StyleClass(Integer score, Course course)
	{
		if (score == null)
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		else if (course.getFront9Par() > score)
		{
			return BIRDIE_OR_BETTER_STYLECLASS;
		}
		else
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		
	}
	
	public static String getBack9StyleClass(Integer score, Course course)
	{
		if (score == null)
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		else if (course.getBack9Par() > score)
		{
			return BIRDIE_OR_BETTER_STYLECLASS;
		}
		else
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		
	}
	
	public static String getTotalStyleClass(Integer score, Course course)
	{
		if (score == null)
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		else if (course.getCoursePar() > score)
		{
			return BIRDIE_OR_BETTER_STYLECLASS;
		}
		else
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
	}
	
	public static String getNetStyleClass(BigDecimal netScore, Course course) 
	{
		BigDecimal courseParBD = BigDecimal.valueOf(course.getCoursePar());
		if (netScore != null && netScore.compareTo(courseParBD) == -1)
		{
			return BIRDIE_OR_BETTER_STYLECLASS;
		}
		else
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
	}
	
	public static String getStyleForHole(int holeNumber, Integer score, Course course)
	{
		if (score == null)
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
		else if (course.getHolesMap().get(holeNumber).getPar() > score)
		{
			return BIRDIE_OR_BETTER_STYLECLASS;
		}
		else
		{
			return PAR_OR_WORSE_STYLECLASS;
		}
				
	}
	
	public static Round setDisplayScore(int holeNumber, int score, Course course, Round existingRound)
	{
		Round tempRound = existingRound;
		
		switch (holeNumber) 
		{
		case 1:
			
			tempRound.setHole1Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole1StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole1StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 2:
			
			tempRound.setHole2Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole2StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole2StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 3:
			
			tempRound.setHole3Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole3StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole3StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 4:
			
			tempRound.setHole4Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole4StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole4StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 5:
			
			tempRound.setHole5Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole5StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole5StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 6:
			
			tempRound.setHole6Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole6StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole6StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 7:
			
			tempRound.setHole7Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole7StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole7StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 8:
			
			tempRound.setHole8Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole8StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole8StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 9:
			
			tempRound.setHole9Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole9StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole9StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			
			break;
			
		case 10:
			
			tempRound.setHole10Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole10StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole10StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 11:
			
			tempRound.setHole11Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole11StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole11StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 12:
			
			tempRound.setHole12Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole12StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole12StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 13:
			
			tempRound.setHole13Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole13StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole13StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			
			break;
			
		case 14:
			
			tempRound.setHole14Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole14StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole14StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 15:
			
			tempRound.setHole15Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole15StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole15StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 16:
			
			tempRound.setHole16Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole16StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole16StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 17:
			
			tempRound.setHole17Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole17StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole17StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 18:
			
			tempRound.setHole18Score(score);
			if (course.getHolesMap().get(holeNumber).getPar() > score)
			{
				tempRound.setHole18StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setHole18StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 19: //front 9
			
			if (course.getFront9Par() > score)
			{
				tempRound.setFront9StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setFront9StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 20: //back 9
			
			if (course.getBack9Par() > score)
			{
				tempRound.setBack9StyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setBack9StyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 21: //total
			
			if (course.getFront9Par() + course.getBack9Par() > score)
			{
				tempRound.setTotalStyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setTotalStyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;
			
		case 22: //net
			
			if (course.getCoursePar() > score)
			{
				tempRound.setNetStyleClass(BIRDIE_OR_BETTER_STYLECLASS);
			}
			else
			{
				tempRound.setNetStyleClass(PAR_OR_WORSE_STYLECLASS);
			}
			break;	
	
		default:
			break;
		}
		
		return tempRound;
		
	}
	
	@SuppressWarnings("unchecked")
	public static boolean isAdminUser()
	{
		boolean adminUser = false;
	
		Collection<SimpleGrantedAuthority> authorities = (Collection<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		
		for (Iterator<SimpleGrantedAuthority> iterator = authorities.iterator(); iterator.hasNext();) 
		{
			SimpleGrantedAuthority simpleGrantedAuthority = (SimpleGrantedAuthority) iterator.next();
			if (simpleGrantedAuthority.getAuthority().contains("ADMIN"))
			{
				adminUser = true; //admin user can see all the rounds
				break;
			}			
		}
		
		return adminUser;
		
	}
	
	public static String getLoggedInUserName()
	{
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		String currentGolfUser = (String) session.getAttribute("currentGolfUser");
		
		currentGolfUser = SecurityContextHolder.getContext().getAuthentication().getName();
		
	    return currentGolfUser == null ? null : currentGolfUser.toLowerCase().trim();
	}
	
	/*
	public static MysqlDataSource getDatasourceProperties()
	{
		MysqlDataSource ds = null;
		
       	if (System.getProperty("RDS_HOSTNAME") != null) 
   		{
   		    try 
   		    {
   		    	ds = new MysqlDataSource();
   		    	
   			    String dbName = System.getProperty("RDS_DB_NAME");
   			    String userName = System.getProperty("RDS_USERNAME");
   			    String password = System.getProperty("RDS_PASSWORD");
   			    String hostname = System.getProperty("RDS_HOSTNAME");
   			    String port = System.getProperty("RDS_PORT");
   			    String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;
   			    
   			    //logger.info("jdbcUrl for datasource: " + jdbcUrl);
   			    
   			    ds.setURL(jdbcUrl);
   			    ds.setPassword(password);
   			    ds.setUser(userName);
   			    
   			  }
   			  catch (Exception e) 
   		      { 
   				  logger.error(e.toString());
   			  }   			  
   		}
       	
       	return ds;
	}
	*/
	
	public static ArrayList<String> setEmailFullRecipientList(List<DynamoPlayer> list) 
	{
		ArrayList<String> emailRecipients = new ArrayList<String>();
		
		String mailTo = genericProps.getString("mailTo");
		
		for (int i = 0; i < list.size(); i++) 
		{
			DynamoPlayer player = list.get(i);
			String emailAddress = player.getEmailAddress();
			
			if (emailAddress == null 
			|| emailAddress.trim().length() == 0 
			|| emailAddress.equalsIgnoreCase("unknown")
			|| !player.isActive()
			|| emailAddress.equalsIgnoreCase(mailTo))
			{
				continue;
			}
			
			emailRecipients.add(emailAddress);
		}
		
		boolean containsSearchStr = emailRecipients.stream().anyMatch("cleclerc@bryanpark.com"::equalsIgnoreCase);
		if (!containsSearchStr) //always add Chris LeClerc if not already there
		{
			emailRecipients.add("cleclerc@bryanpark.com");
		}	
		
		return emailRecipients;
	}	
		
	public static String getDayofWeekString(Date date) 
	{
		Locale locale = Locale.getDefault();
	    DateFormat formatter = new SimpleDateFormat("EEEE", locale);
	    return formatter.format(date);
	}
	
	public static BigDecimal getCourseHandicap(DynamoCourseTee courseTee, BigDecimal handicapIndex) throws Exception
	{
		//From here: http://www.scga.org/pdfs/Course%20Handicap%20Calculation.pdf
		//Course Handicap = Handicap Index × (Slope Rating ÷ 113) + (Course Rating – Par)
		
		BigDecimal courseRatingMinusPar = courseTee.getCourseRating().subtract(new BigDecimal(courseTee.getCoursePar()));
		
		BigDecimal bdSlope = new BigDecimal(courseTee.getSlopeRating());
		BigDecimal slopeFactor = new BigDecimal(113);
		BigDecimal slopeRatingDiv113 = bdSlope.divide(slopeFactor,6, RoundingMode.HALF_EVEN); 	
		
		BigDecimal courseHandicap = handicapIndex.multiply(slopeRatingDiv113).add(courseRatingMinusPar);
		
		//Lets finalize this by rounding to a whole number.
		courseHandicap = courseHandicap.setScale(0, RoundingMode.HALF_EVEN); 
		
		return courseHandicap;
	}
	
	public static Boolean isRunningInsideDocker() 
	{
        try (Stream < String > stream = Files.lines(Paths.get("/proc/1/cgroup"))) 
        {
        	boolean inDocker = stream.anyMatch(line -> line.contains("/docker")); 
        	boolean inAWSECS = stream.anyMatch(line -> line.contains("/ecs")); 
        	
        	logger.info("Am I in docker container? " + inDocker);
        	logger.info("Am I in Amazon AWS ECS container? " + inAWSECS);
        	
            return inDocker || inAWSECS;
        } 
        catch (IOException e) 
        {
        	logger.info("Am I in docker container? NO");
            return false;
        }
    }
	
	public static boolean isLocalEnv()
	{
		boolean isLocal = true; //just set to true when running locally
		/*
		try 
		{
			Path dir = (Path)Paths.get("/Paul", "GitHub");
			isLocal = Files.isDirectory(dir);
		} 
		catch (Exception e) 
		{			
		}
		*/		
		return isLocal;
	}
	
	public static String composeFutureGameEmail(DynamoGame dynamoGame, List<DynamoPlayer> fullPlayerList, List<TeeTime> teeTimeList, List<Round> roundsForGame)
	{
		StringBuffer sb = new StringBuffer();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		
		String subjectLine = "<H3>Golf game on " + Utils.getDayofWeekString(dynamoGame.getGameDateJava()) + " " + sdf.format(dynamoGame.getGameDateJava()) + " on " + dynamoGame.getCourseName() + "</H3>";
		sb.append(subjectLine);
		
		sb.append(NEWLINE);
		
		StringBuffer sbFutureGameDetails = getFutureEmailGameDetails(dynamoGame, teeTimeList);
		sb.append(sbFutureGameDetails);	
		sb.append(NEWLINE);
		
		StringBuffer whosIn = getGameParticipants(dynamoGame, roundsForGame);
		sb.append(whosIn);		
		sb.append(NEWLINE);		
						
		sb.append("To sign up to play (or withdraw if you've already signed up but can no longer play), go to this site:");
		sb.append(NEWLINE);
		sb.append("<a href='" + WEBSITE_URL + "'>Golf Scoring</a>");
		sb.append(NEWLINE);
		sb.append("Please do not send email requests to sign up to play - the way to sign up or withdraw now is the website above.  The gmail box is not monitored regularly and you may be left out of a game if you request to get in that way.");
		sb.append(NEWLINE);
	
		sb.append(NEWLINE);
		
		sb.append("Log in with your credentials.  If first time logging in, those are your first name initial plus your last name for both userid and password.");
		sb.append(NEWLINE);
		sb.append("For example for John Doe, his credentials would be userid jdoe and password jdoe.");
		sb.append(NEWLINE);		
		sb.append("You can change your password from the profile menu and you are encouraged to do so.  Your user id is not changeable.");
		sb.append(NEWLINE);
		sb.append("To sign up or withdraw from a game hover on the Game menu and click Sign up for Game.");
		sb.append(NEWLINE);
		sb.append("select * from that screen click the appropriate button for the game you are interested in.");			
		
		ArrayList<String> emailRecipients = establishEmailRecipientsForFutureGame(fullPlayerList);
		String logMessage = "This future game email will go to " + emailRecipients.size() + " recipients: " + emailRecipients;
		logger.info(logMessage);
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO,logMessage,null);
		FacesContext.getCurrentInstance().addMessage(null, msg);
	
		return sb.toString();
	}	
	
	public static List<String> getGameParticipantsFromDB(List<Round> roundList)
    {		
		List<String> participantsList = new ArrayList<>();
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone(Utils.MY_TIME_ZONE);
		signupSDF.setTimeZone(etTimeZone);
		
		for (int i = 0; i < roundList.size(); i++) 
		{
			Round rd = roundList.get(i);
			String signupLine = Utils.getSignupLine(rd);			
			participantsList.add(signupLine);
		}
		
		return participantsList;
    }
	
	private static StringBuffer getGameParticipants(DynamoGame dynamoGame, List<Round> roundsForGame)
	{
		StringBuffer sb = new StringBuffer();
		
		sb.append("Current list of players for this game:");
		sb.append(NEWLINE);
		
		List<String> roundPlayers = getGameParticipantsFromDB(roundsForGame);
		
		for (int i = 0; i < roundPlayers.size(); i++) 
		{
			String playerName = roundPlayers.get(i);
			if (i+1 <= dynamoGame.getFieldSize())
			{
				sb.append(i+1 + ". " + playerName);
			}
			else
			{
				sb.append(i+1 + ". " + playerName + " (wait list)");
			}
			sb.append(NEWLINE);
		}
		
		int spotsAvailable = dynamoGame.getFieldSize() - roundPlayers.size();
		
		sb.append(NEWLINE);
		sb.append("Spots still available: " + spotsAvailable);
		sb.append(NEWLINE);
		
		return sb;
	}
	
	public static ArrayList<String> establishEmailRecipientsForFutureGame(List<DynamoPlayer> fullPlayerList)
	{		
		ArrayList<String> emailRecipients = new ArrayList<>();	 
		
		emailRecipients = Utils.setEmailFullRecipientList(fullPlayerList);
				
		logger.info(" future game composing email: will email to these recipients if sendemail clicked: " + emailRecipients);
		
		return emailRecipients;
	}
	
	public static StringBuffer getFutureEmailGameDetails(DynamoGame gm, List<TeeTime> teeTimeList)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		DecimalFormat currencyFmt = new DecimalFormat("$0.00");
		
		StringBuffer sb = new StringBuffer();	
		
		sb.append("Date: " + sdf.format(gm.getGameDateJava()) + NEWLINE);
		sb.append("Course: " + gm.getCourseName() + NEWLINE);
		sb.append("Bet Amt: " + currencyFmt.format(gm.getBetAmount()) + NEWLINE);
		sb.append("Field Size: " + gm.getFieldSize() + NEWLINE);
		sb.append("Tee Times: ");		
		
		for (int i = 0; i < teeTimeList.size(); i++)
		{
			TeeTime teeTime = teeTimeList.get(i);
			sb.append(teeTime.getTeeTimeString() + " ");
		}

		sb.append(NEWLINE);

		return sb;
	}
	
	public static String sendFutureGameEmail(DynamoGame dynamoGame, List<DynamoPlayer> fullPlayerList, String futureGameEmailMessage)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "Golf game on " + Utils.getDayofWeekString(dynamoGame.getGameDateJava()) + " " + sdf.format(dynamoGame.getGameDateJava()) + " on " + dynamoGame.getCourseName();		
		ArrayList<String> emailRecipients = establishEmailRecipientsForFutureGame(fullPlayerList);
		
		if (emailRecipients.size() >= 100)
		{
			FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"100 or more recipients on Email list - google will not send it, preventing before trying",null);
			FacesContext.getCurrentInstance().addMessage(null, msg);
		}
		else
		{
			SAMailUtility.sendEmail(subjectLine, futureGameEmailMessage, emailRecipients, true); //last param means use jsf
		}
		
		return "";
	}	
	
	public static long getDailyEmailTime() 
    {
    	int hour = 8;
    	int minute = 0;
    	int second = 0;
    	
    	//int hour = 22;
    	//int minute = 28;
    	//int second = 0;
    	
    	ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Utils.MY_TIME_ZONE));
    	ZonedDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(second);
    	
    	if(now.compareTo(nextRun) > 0)
    	{
    		nextRun = nextRun.plusDays(1);
    	}

    	Duration duration = Duration.between(now, nextRun);
    	long initialDelay = duration.getSeconds();
		return initialDelay;
	}
	
	public static String getEncryptedPassword(String unencryptedPassword)
	{
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encryptedPW = passwordEncoder.encode(unencryptedPassword);
		return encryptedPW;
	}
	
	public static String getSignupLine(Round rd) 
	{
		StringBuffer signupString = new StringBuffer();
		
		SimpleDateFormat signupSDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss aa");
		TimeZone etTimeZone = TimeZone.getTimeZone(Utils.MY_TIME_ZONE);
		signupSDF.setTimeZone(etTimeZone);

		signupString.append(rd.getPlayerName());
		
		String teeColorSelection = rd.getCourseTeeColor();
		
		if (teeColorSelection != null && teeColorSelection.trim().length() > 0)
		{
			signupString.append(" (" + teeColorSelection + " tees) ");
		}
		
		if (rd.getSignupDateTime() != null)
		{
			signupString.append(" (signed up: " + signupSDF.format(rd.getSignupDateTime()) + ")");
		}
		
		return signupString.toString();
	}

	public static boolean isWinterMonth() 
	{
		boolean isWinterMonth = false;
		
		Calendar calendarToday = Calendar.getInstance();
		
		int monthNumber  = calendarToday.get(Calendar.MONTH);
		monthNumber++; //to account for 0 being January
		if (monthNumber == 1 || monthNumber == 2 || monthNumber == 3 || monthNumber == 11 || monthNumber == 12)
		{
			isWinterMonth = true;
		}
		
		return isWinterMonth;
	}

}

package com.pas.dynamodb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.pas.beans.GolfUser;

public class CreateJSONForDynamoFromMySQL
{
	private static Logger log = LogManager.getLogger(CreateJSONForDynamoFromMySQL.class); //log4j for Logging
	 
    private static String jsonOutputFile = "C:/Paul/GitHub/golfScoring/src/main/resources/data/GolfUsersData.json"; 			

    public static void main(String[] args) throws Exception
    { 
    	log.debug("**********  START of program ***********");   	
    	
    	List<GolfUser> golfUserList = getGolfUsersFromMySQLDB();	
    	
    	log.debug("********** starting write of JSON file ***********");   	
	   	writeJSONFile(golfUserList);
	   	log.debug("********** finished write of JSON file ***********");   	
	   	
		log.debug("**********  END of program ***********");
		
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
	private static void writeJSONFile(List<GolfUser> golfUserList) 
	{
		BufferedWriter bw = null;
		
		try
		{
			FileWriter fw = new FileWriter(jsonOutputFile, false);
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
		
}
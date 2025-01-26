package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.pas.beans.CourseTee;
import com.pas.beans.GolfMain;
import com.pas.beans.Group;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourseTee;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
 
public class CourseTeeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(CourseTeeDAO.class);
		
	private Map<String,CourseTee> courseTeesMap = new HashMap<>();
	private List<CourseTee> courseTeesList = new ArrayList<CourseTee>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME = "coursetees";

	@Autowired private final GolfMain golfmain;
	
	public CourseTeeDAO(DynamoClients dynamoClients2, GolfMain golfmain) 
	{
		this.golfmain = golfmain;
		
		try 
		{
		    dynamoClients = dynamoClients2;
		    courseTeesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourseTee.class));
		} 
		catch (final Exception ex) 
		{
		   logger.error("Got exception while initializing CourseTeeDAO. Ex = " + ex.getMessage(), ex);
		}	   
	}
	
	public List<CourseTee> readCourseTeesFromDB(Group grp)
    {
		Iterator<DynamoCourseTee> results = courseTeesTable.scan().items().iterator();
		  	
		while (results.hasNext()) 
        {
			DynamoCourseTee dynamoCourseTee = results.next();
          	
			CourseTee courseTee = new CourseTee(golfmain);
			courseTee.setCourseTeeID(dynamoCourseTee.getCourseTeeID());
			courseTee.setCourseID(dynamoCourseTee.getCourseID());
			courseTee.setTeeColor(dynamoCourseTee.getTeeColor());
			courseTee.setCourseRating(dynamoCourseTee.getCourseRating());
			courseTee.setCoursePar(dynamoCourseTee.getCoursePar());
			courseTee.setSlopeRating(dynamoCourseTee.getSlopeRating());
			courseTee.setTotalYardage(dynamoCourseTee.getTotalYardage());			
			
            this.getCourseTeesList().add(courseTee);			
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:courseTee; rows:" + this.getCourseTeesList().size());
		
		courseTeesMap = this.getCourseTeesList().stream().collect(Collectors.toMap(CourseTee::getCourseTeeID, CourseTee -> CourseTee));
			
		return this.getCourseTeesList();
    }
	
	public void addCourseTee(CourseTee courseTee) throws Exception 
	{
		dynamoUpsert(courseTee);	
		
		logger.info("Added a new courseTee");
	}
	
	private DynamoCourseTee dynamoUpsert(CourseTee courseTee) throws Exception 
	{
		DynamoCourseTee dynamoCourseTee = new DynamoCourseTee();
        
		if (courseTee.getCourseTeeID() == null)
		{
			dynamoCourseTee.setCourseTeeID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoCourseTee.setCourseTeeID(courseTee.getCourseTeeID());
		}
		
		dynamoCourseTee.setCourseID(courseTee.getCourseID());
		dynamoCourseTee.setTeeColor(courseTee.getTeeColor());
		dynamoCourseTee.setCourseRating(courseTee.getCourseRating());
		dynamoCourseTee.setCoursePar(courseTee.getCoursePar());
		dynamoCourseTee.setSlopeRating(courseTee.getSlopeRating());
		dynamoCourseTee.setTotalYardage(courseTee.getTotalYardage());		
		
		PutItemEnhancedRequest<DynamoCourseTee> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoCourseTee.class).item(dynamoCourseTee).build();
		courseTeesTable.putItem(putItemEnhancedRequest);
				
		return dynamoCourseTee;
	}
	
	public Map<String, CourseTee> getCourseTeesMap() 
	{
		return courseTeesMap;
	}

	public void setCourseTeesMap(Map<String, CourseTee> courseTeesMap) 
	{
		this.courseTeesMap = courseTeesMap;
	}

	public List<CourseTee> getCourseTeesList() {
		return courseTeesList;
	}

	public void setCourseTeesList(List<CourseTee> courseTeesList) {
		this.courseTeesList = courseTeesList;
	}

	

	
}

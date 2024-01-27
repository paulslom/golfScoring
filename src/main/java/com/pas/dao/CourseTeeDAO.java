package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Repository;

import com.pas.beans.CourseTee;
import com.pas.beans.Group;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
 
@Repository
public class CourseTeeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(CourseTeeDAO.class);
		
	private Map<String,CourseTee> courseTeesMap = new HashMap<>();
	private List<CourseTee> courseTeesList = new ArrayList<CourseTee>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME = "coursetees";

	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       courseTeesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourseTee.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing CourseTeeDAO. Ex = " + ex.getMessage(), ex);
	   }	   
	}
	
	public List<CourseTee> readCourseTeesFromDB(Group grp)
    {
		Iterator<DynamoCourseTee> results = courseTeesTable.scan().items().iterator();
		  	
		while (results.hasNext()) 
        {
			DynamoCourseTee dynamoCourseTee = results.next();
          	
			CourseTee courseTee = new CourseTee();
			courseTee.setCourseTeeID(dynamoCourseTee.getCourseTeeID());
			courseTee.setCourseID(dynamoCourseTee.getCourseID());
			courseTee.setTeeColor(dynamoCourseTee.getTeeColor());
			courseTee.setCourseRating(dynamoCourseTee.getCourseRating());
			courseTee.setCoursePar(dynamoCourseTee.getCoursePar());
			courseTee.setSlopeRating(dynamoCourseTee.getSlopeRating());
			courseTee.setTotalYardage(dynamoCourseTee.getTotalYardage());			
			
            this.getCourseTeesList().add(courseTee);			
        }
		
		log.info("LoggedDBOperation: function-inquiry; table:courseTee; rows:" + this.getCourseTeesList().size());
		
		courseTeesMap = this.getCourseTeesList().stream().collect(Collectors.toMap(CourseTee::getCourseTeeID, CourseTee -> CourseTee));
			
		return this.getCourseTeesList();
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

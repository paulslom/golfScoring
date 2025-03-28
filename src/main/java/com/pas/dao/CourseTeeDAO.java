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

import com.pas.beans.CourseTee;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourseTee;
import com.pas.dynamodb.DynamoGroup;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
 
public class CourseTeeDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(CourseTeeDAO.class);
		
	private Map<String,DynamoCourseTee> courseTeesMap = new HashMap<>();
	private Map<String,List<DynamoCourseTee>> courseTeesMapListByCourseID = new HashMap<>();
	private List<DynamoCourseTee> courseTeesList = new ArrayList<>();
	
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoCourseTee> courseTeesTable;
	private static final String AWS_TABLE_NAME = "coursetees";

	public CourseTeeDAO(DynamoClients dynamoClients2)
	{
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
	
	public List<DynamoCourseTee> readCourseTeesFromDB(DynamoGroup grp)
    {
		Iterator<DynamoCourseTee> results = courseTeesTable.scan().items().iterator();
		  	
		while (results.hasNext()) 
        {
			DynamoCourseTee dynamoCourseTee = results.next();
            this.getCourseTeesList().add(dynamoCourseTee);
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:courseTee; rows:" + this.getCourseTeesList().size());
		
		courseTeesMap = this.getCourseTeesList().stream().collect(Collectors.toMap(DynamoCourseTee::getCourseTeeID, DynamoCourseTee -> DynamoCourseTee));
		
		for (int i = 0; i < this.getCourseTeesList().size(); i++) 
		{
			DynamoCourseTee dct = this.getCourseTeesList().get(i);
			
			if (this.getCourseTeesMapListByCourseID().containsKey(dct.getCourseID()))
			{
				List<DynamoCourseTee> ctList = this.getCourseTeesMapListByCourseID().get(dct.getCourseID());
				ctList.add(dct);
			}
			else
			{
				List<DynamoCourseTee> ctList = new ArrayList<>();
				ctList.add(dct);
				this.getCourseTeesMapListByCourseID().put(dct.getCourseID(), ctList);
			}
		}
		
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

	public List<DynamoCourseTee> getCourseSpecificCourseTeesListByCourseID(String courseid) 
	{
		return this.getCourseTeesMapListByCourseID().get(courseid);
	}
	
	public List<DynamoCourseTee> getCourseTeesList() {
		return courseTeesList;
	}

	public void setCourseTeesList(List<DynamoCourseTee> courseTeesList) {
		this.courseTeesList = courseTeesList;
	}

	public Map<String, DynamoCourseTee> getCourseTeesMap() {
		return courseTeesMap;
	}

	public void setCourseTeesMap(Map<String, DynamoCourseTee> courseTeesMap) {
		this.courseTeesMap = courseTeesMap;
	}

	public Map<String, List<DynamoCourseTee>> getCourseTeesMapListByCourseID() {
		return courseTeesMapListByCourseID;
	}

	public void setCourseTeesMapListByCourseID(Map<String, List<DynamoCourseTee>> courseTeesMapListByCourseID) {
		this.courseTeesMapListByCourseID = courseTeesMapListByCourseID;
	}

	//deletes a particular course tee
	public void deleteCourseTeeFromDB(String courseTeeID)
    {
		Key key = Key.builder().partitionValue(courseTeeID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		courseTeesTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:courseTees; rows:1");
    }
		
	//deletes all tees for a specific course
	public void deleteCourseTeesForCourseFromDB(String courseID) 
	{
		List<DynamoCourseTee> tempList = this.getCourseTeesMapListByCourseID().get(courseID);
		for (int i = 0; i < tempList.size(); i++)
		{
			DynamoCourseTee dct = tempList.get(i);
			deleteCourseTeeFromDB(dct.getCourseTeeID());
			this.getCourseTeesMap().remove(dct.getCourseTeeID());
		}
		
		this.getCourseTeesMapListByCourseID().remove(courseID);			
		logger.info("deleteCourseTeesForCourseFromDB");			
	}
}

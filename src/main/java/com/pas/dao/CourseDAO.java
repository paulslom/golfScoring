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

import com.pas.beans.Course;
import com.pas.beans.Group;
import com.pas.beans.Hole;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourse;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
 
public class CourseDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(CourseDAO.class);
	
	private Map<String,Course> coursesMap = new HashMap<>();
	private List<Course> courseSelections = new ArrayList<Course>();
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME = "courses";
	
	public CourseDAO(DynamoClients dynamoClients2) 
	{
		try 
		   {
		       dynamoClients = dynamoClients2;
		       coursesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourse.class));
		   } 
		   catch (final Exception ex) 
		   {
		      logger.error("Got exception while initializing CourseDAO. Ex = " + ex.getMessage(), ex);
		   }	   
	}
	
	public void readCoursesFromDB(Group grp)
    {
		Iterator<DynamoCourse> results = coursesTable.scan().items().iterator();
		
		while (results.hasNext()) 
        {
			DynamoCourse dynamoCourse = results.next();
          	
			Course course = new Course();
			course.setCourseID(dynamoCourse.getCourseID());
			course.setOldCourseID(dynamoCourse.getOldCourseID());
			course.setCourseName(dynamoCourse.getCourseName());
			course.setFront9Par(dynamoCourse.getFront9Par());
			course.setBack9Par(dynamoCourse.getBack9Par());
			course.setCoursePar(dynamoCourse.getCoursePar());
			
			Hole hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(1);
			hole.setPar(dynamoCourse.getHole1Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(2);
			hole.setPar(dynamoCourse.getHole2Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(3);
			hole.setPar(dynamoCourse.getHole3Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(4);
			hole.setPar(dynamoCourse.getHole4Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(5);
			hole.setPar(dynamoCourse.getHole5Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(6);
			hole.setPar(dynamoCourse.getHole6Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(7);
			hole.setPar(dynamoCourse.getHole7Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(8);
			hole.setPar(dynamoCourse.getHole8Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(9);
			hole.setPar(dynamoCourse.getHole9Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(10);
			hole.setPar(dynamoCourse.getHole10Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(11);
			hole.setPar(dynamoCourse.getHole11Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(12);
			hole.setPar(dynamoCourse.getHole12Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(13);
			hole.setPar(dynamoCourse.getHole13Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(14);
			hole.setPar(dynamoCourse.getHole14Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(15);
			hole.setPar(dynamoCourse.getHole15Par());
			course.getHolesList().add(hole);
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(16);
			hole.setPar(dynamoCourse.getHole16Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(17);
			hole.setPar(dynamoCourse.getHole17Par());
			course.getHolesList().add(hole);			
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(18);
			hole.setPar(dynamoCourse.getHole18Par());
			course.getHolesList().add(hole);
			
			course.setHolesMap(course.getHolesList().stream().collect(Collectors.toMap(listHole->listHole.getHoleNumber(),listHole-> listHole)));
			
            this.getCourseSelections().add(course);			
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:course; rows:" + this.getCourseSelections().size());
		
		coursesMap = this.getCourseSelections().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
    }
	
	public String addCourse(Course course) throws Exception
	{
		DynamoCourse dynamoCourse = dynamoUpsert(course);	
		course.setCourseID(dynamoCourse.getCourseID());
		
		logger.info("Added a new course");
		
		return course.getCourseID();
	}
	
	private DynamoCourse dynamoUpsert(Course course) throws Exception 
	{
		DynamoCourse dynamoCourse = new DynamoCourse();
        
		if (course.getCourseID() == null)
		{
			dynamoCourse.setCourseID(UUID.randomUUID().toString());
		}
		else
		{
			dynamoCourse.setCourseID(course.getCourseID());
		}
		
		dynamoCourse.setCourseName(course.getCourseName());
		dynamoCourse.setFront9Par(course.getFront9Par());
		dynamoCourse.setBack9Par(course.getBack9Par());
		dynamoCourse.setCoursePar(course.getCoursePar());
		dynamoCourse.setGroupID("e5cfe1cc-d16a-4ca3-9ea5-9ff2fe4b675f"); //hard coded bryan park group
		dynamoCourse.setHole1Par(course.getHolesList().get(0).getPar());
		dynamoCourse.setHole2Par(course.getHolesList().get(1).getPar());
		dynamoCourse.setHole3Par(course.getHolesList().get(2).getPar());
		dynamoCourse.setHole4Par(course.getHolesList().get(3).getPar());
		dynamoCourse.setHole5Par(course.getHolesList().get(4).getPar());
		dynamoCourse.setHole6Par(course.getHolesList().get(5).getPar());
		dynamoCourse.setHole7Par(course.getHolesList().get(6).getPar());
		dynamoCourse.setHole8Par(course.getHolesList().get(7).getPar());
		dynamoCourse.setHole9Par(course.getHolesList().get(8).getPar());
		dynamoCourse.setHole10Par(course.getHolesList().get(9).getPar());
		dynamoCourse.setHole11Par(course.getHolesList().get(10).getPar());
		dynamoCourse.setHole12Par(course.getHolesList().get(11).getPar());
		dynamoCourse.setHole13Par(course.getHolesList().get(12).getPar());
		dynamoCourse.setHole14Par(course.getHolesList().get(13).getPar());
		dynamoCourse.setHole15Par(course.getHolesList().get(14).getPar());
		dynamoCourse.setHole16Par(course.getHolesList().get(15).getPar());
		dynamoCourse.setHole17Par(course.getHolesList().get(16).getPar());
		dynamoCourse.setHole18Par(course.getHolesList().get(17).getPar());
		
		PutItemEnhancedRequest<DynamoCourse> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoCourse.class).item(dynamoCourse).build();
		coursesTable.putItem(putItemEnhancedRequest);
				
		return dynamoCourse;
	}
	
	public Map<String, Course> getCoursesMap() 
	{
		return coursesMap;
	}

	public void setCoursesMap(Map<String, Course> coursesMap) 
	{
		this.coursesMap = coursesMap;
	}

	public List<Course> getCourseSelections() {
		return courseSelections;
	}

	public void setCourseSelections(List<Course> courseSelections) {
		this.courseSelections = courseSelections;
	}
}

package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.Course;
import com.pas.beans.Hole;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourse;
import com.pas.dynamodb.DynamoGroup;

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
import software.amazon.awssdk.enhanced.dynamodb.model.DeleteItemEnhancedRequest;
import software.amazon.awssdk.enhanced.dynamodb.model.PutItemEnhancedRequest;
 
public class CourseDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(CourseDAO.class);
	
	private Map<String,Course> coursesMap = new HashMap<>();
	private List<Course> coursesList = new ArrayList<>();
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
	
	public void readCoursesFromDB(DynamoGroup grp)
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
			course.setHole1Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(2);
			hole.setPar(dynamoCourse.getHole2Par());
			course.getHolesList().add(hole);
			course.setHole2Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(3);
			hole.setPar(dynamoCourse.getHole3Par());
			course.getHolesList().add(hole);			
			course.setHole3Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(4);
			hole.setPar(dynamoCourse.getHole4Par());
			course.getHolesList().add(hole);
			course.setHole4Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(5);
			hole.setPar(dynamoCourse.getHole5Par());
			course.getHolesList().add(hole);			
			course.setHole5Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(6);
			hole.setPar(dynamoCourse.getHole6Par());
			course.getHolesList().add(hole);
			course.setHole6Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(7);
			hole.setPar(dynamoCourse.getHole7Par());
			course.getHolesList().add(hole);			
			course.setHole7Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(8);
			hole.setPar(dynamoCourse.getHole8Par());
			course.getHolesList().add(hole);			
			course.setHole8Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(9);
			hole.setPar(dynamoCourse.getHole9Par());
			course.getHolesList().add(hole);
			course.setHole9Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(10);
			hole.setPar(dynamoCourse.getHole10Par());
			course.getHolesList().add(hole);			
			course.setHole10Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(11);
			hole.setPar(dynamoCourse.getHole11Par());
			course.getHolesList().add(hole);
			course.setHole11Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(12);
			hole.setPar(dynamoCourse.getHole12Par());
			course.getHolesList().add(hole);			
			course.setHole12Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(13);
			hole.setPar(dynamoCourse.getHole13Par());
			course.getHolesList().add(hole);
			course.setHole13Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(14);
			hole.setPar(dynamoCourse.getHole14Par());
			course.getHolesList().add(hole);
			course.setHole14Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(15);
			hole.setPar(dynamoCourse.getHole15Par());
			course.getHolesList().add(hole);
			course.setHole15Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(16);
			hole.setPar(dynamoCourse.getHole16Par());
			course.getHolesList().add(hole);
			course.setHole16Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(17);
			hole.setPar(dynamoCourse.getHole17Par());
			course.getHolesList().add(hole);
			course.setHole17Par(hole.getPar());
			
			hole = new Hole();
			hole.setCourseID(course.getCourseID());
			hole.setHoleNumber(18);
			hole.setPar(dynamoCourse.getHole18Par());
			course.getHolesList().add(hole);
			course.setHole18Par(hole.getPar());
			
			course.setHolesMap(course.getHolesList().stream().collect(Collectors.toMap(listHole->listHole.getHoleNumber(),listHole-> listHole)));
			
            this.getCoursesList().add(course);			
        }
		
		logger.info("LoggedDBOperation: function-inquiry; table:course; rows:" + this.getCoursesList().size());
		
		coursesMap = this.getCoursesList().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
		
		Collections.sort(this.getCoursesList(), new Comparator<Course>()
		{
		   public int compare(Course o1, Course o2) 
		   {
		      return o1.getCourseName().compareTo(o2.getCourseName());
		   }
		});
    }
	
	public String addCourse(Course course) throws Exception
	{
		DynamoCourse dynamoCourse = dynamoUpsert(course);	
		course.setCourseID(dynamoCourse.getCourseID());
		
		logger.info("LoggedDBOperation: function-add; table:course; rows:1");
		
		refreshCoursesList("Add", course.getCourseID(), course);		
		
		return course.getCourseID();
	}
	
	public String updateCourse(Course course) throws Exception
	{
		dynamoUpsert(course);
		
		logger.info("LoggedDBOperation: function-update; table:course; rows:1");
		
		refreshCoursesList("Update", course.getCourseID(), null);		
		
		return course.getCourseID();
	}
	
	public void deleteCourse(String courseID) throws Exception
	{
		Key key = Key.builder().partitionValue(courseID).build();
		DeleteItemEnhancedRequest deleteItemEnhancedRequest = DeleteItemEnhancedRequest.builder().key(key).build();
		coursesTable.deleteItem(deleteItemEnhancedRequest);
		
		logger.info("LoggedDBOperation: function-delete; table:course; rows:1");
		
		refreshCoursesList("Delete", courseID, null);		
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
		dynamoCourse.setHole1Par(course.getHole1Par());
		dynamoCourse.setHole2Par(course.getHole2Par());
		dynamoCourse.setHole3Par(course.getHole3Par());
		dynamoCourse.setHole4Par(course.getHole4Par());
		dynamoCourse.setHole5Par(course.getHole5Par());
		dynamoCourse.setHole6Par(course.getHole6Par());
		dynamoCourse.setHole7Par(course.getHole7Par());
		dynamoCourse.setHole8Par(course.getHole8Par());
		dynamoCourse.setHole9Par(course.getHole9Par());
		dynamoCourse.setHole10Par(course.getHole10Par());
		dynamoCourse.setHole11Par(course.getHole11Par());
		dynamoCourse.setHole12Par(course.getHole12Par());
		dynamoCourse.setHole13Par(course.getHole13Par());
		dynamoCourse.setHole14Par(course.getHole14Par());
		dynamoCourse.setHole15Par(course.getHole15Par());
		dynamoCourse.setHole16Par(course.getHole16Par());
		dynamoCourse.setHole17Par(course.getHole17Par());
		dynamoCourse.setHole18Par(course.getHole18Par());
		
		PutItemEnhancedRequest<DynamoCourse> putItemEnhancedRequest = PutItemEnhancedRequest.builder(DynamoCourse.class).item(dynamoCourse).build();
		coursesTable.putItem(putItemEnhancedRequest);
				
		return dynamoCourse;
	}
			
	private void refreshCoursesList(String function, String courseID, Course course) throws Exception
	{	
		if (function.equalsIgnoreCase("add"))
		{			
			this.getCoursesMap().put(courseID,course);
		}
		else if (function.equalsIgnoreCase("delete"))
		{
			this.getCoursesMap().remove(courseID);
		}
		else if (function.equalsIgnoreCase("update"))
		{
			this.getCoursesMap().replace(courseID, course);
		}
			
		this.getCoursesList().clear();
		Collection<Course> values = this.getCoursesMap().values();
		this.setCoursesList(new ArrayList<>(values));

		Collections.sort(this.getCoursesList(), new Comparator<Course>()
		{
		   public int compare(Course o1, Course o2) 
		   {
		      return o1.getCourseName().compareTo(o2.getCourseName());
		   }
		});
		
		/* for debugging purposes 
		for (int i = 0; i < fullGameList.size(); i++) 
		{
			Game gm = fullGameList.get(i);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			logger.info("gameID: " + gm.getGameID() + ", game date: " + sdf.format(gm.getGameDate()));
		}
		*/
		
	}

	public Map<String, Course> getCoursesMap() 
	{
		return coursesMap;
	}

	public void setCoursesMap(Map<String, Course> coursesMap) 
	{
		this.coursesMap = coursesMap;
	}

	public List<Course> getCoursesList() {
		return coursesList;
	}

	public void setCoursesList(List<Course> coursesList) {
		this.coursesList = coursesList;
	}

	public String getPlayersCourseCourseID() 
	{
		String id = "";
		
		for (int i = 0; i < this.getCoursesList().size(); i++) 
		{
			Course course = this.getCoursesList().get(i);
			if (course.getCourseName().equalsIgnoreCase("Bryan Park Players"))
			{
				id = course.getCourseID();
				break;
			}
		}
		return id;
	}
	
	public Course getCourseByCourseID(String courseID) 
	{
		return this.getCoursesMap().get(courseID);
	}

}

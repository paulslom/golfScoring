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

import com.pas.beans.Course;
import com.pas.beans.Group;
import com.pas.beans.Hole;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoCourse;
import com.pas.dynamodb.DynamoUtil;

import jakarta.annotation.PostConstruct;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;
 
@Repository
public class CourseDAO implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(CourseDAO.class);
	
	private Map<String,Course> coursesMap = new HashMap<>();
	private List<Course> courseSelections = new ArrayList<Course>();
	private static DynamoClients dynamoClients;
	private static DynamoDbTable<DynamoCourse> coursesTable;
	private static final String AWS_TABLE_NAME = "courses";
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       dynamoClients = DynamoUtil.getDynamoClients();
	       coursesTable = dynamoClients.getDynamoDbEnhancedClient().table(AWS_TABLE_NAME, TableSchema.fromBean(DynamoCourse.class));
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing PlayersDAO. Ex = " + ex.getMessage(), ex);
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
			
			new Hole();
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
			
            this.getCourseSelections().add(course);			
        }
		
		log.info("LoggedDBOperation: function-inquiry; table:course; rows:" + this.getCourseSelections().size());
		
		coursesMap = this.getCourseSelections().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
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

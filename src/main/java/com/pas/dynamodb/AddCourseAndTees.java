package com.pas.dynamodb;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.pas.beans.Course;
import com.pas.beans.CourseTee;
import com.pas.beans.Hole;
import com.pas.dao.CourseDAO;
import com.pas.dao.CourseTeeDAO;
import com.pas.dao.PlayerTeePreferenceDAO;

@Component
public class AddCourseAndTees
{	 
	private static CourseDAO courseDAO;
	private static CourseTeeDAO courseTeeDAO;
	private static PlayerTeePreferenceDAO playerTeePreferencesDAO;
	
	/* How to use this:
	 * 
	 * 1) code up the course specifics in the add the course method, comment out the other 2, uncoment addTheCourse.
	 * 2) run this class with just add the course.  Go into NoSQL Workbench, see that the new course is added, and copy the new course ID.
	 * 3) comment out addtheCourse(), and uncomment addTheCourseTees()
	 * 4) copy the new courseID into the addthecourseTees method where it has the courseTee.setCourseID
	 */
    public static void main(String[] args) throws Exception 
    {    	
    	DynamoClients dynamoClients = DynamoUtil.getDynamoClients();
    	
    	//addTheCourse(dynamoClients);
    	addTheCourseTees(dynamoClients);
		
        System.exit(1);
    }

	private static void addTheCourseTees(DynamoClients dynamoClients) throws Exception 
	{
		courseTeeDAO = new CourseTeeDAO(dynamoClients, null);
		
		CourseTee courseTee = new CourseTee(null);
		
		courseTee.setCourseID("7b563b65-cb2e-48f9-91ba-076d98b0f76d");		
		courseTee.setTeeColor("Black");
		courseTee.setCourseRating(new BigDecimal("73.5"));
		courseTee.setCoursePar(72);
		courseTee.setSlopeRating(139);
		courseTee.setTotalYardage(7033);		

		courseTeeDAO.addCourseTee(courseTee);
		
		courseTee = new CourseTee(null);
		
		courseTee.setCourseID("7b563b65-cb2e-48f9-91ba-076d98b0f76d");	
		courseTee.setTeeColor("Blue");
		courseTee.setCourseRating(new BigDecimal("71.5"));
		courseTee.setCoursePar(72);
		courseTee.setSlopeRating(133);
		courseTee.setTotalYardage(6682);		

		courseTeeDAO.addCourseTee(courseTee);
		
		courseTee = new CourseTee(null);
		
		courseTee.setCourseID("7b563b65-cb2e-48f9-91ba-076d98b0f76d");	
		courseTee.setTeeColor("White");
		courseTee.setCourseRating(new BigDecimal("68.7"));
		courseTee.setCoursePar(72);
		courseTee.setSlopeRating(122);
		courseTee.setTotalYardage(6036);		

		courseTeeDAO.addCourseTee(courseTee);
		
		courseTee = new CourseTee(null);
		
		courseTee.setCourseID("7b563b65-cb2e-48f9-91ba-076d98b0f76d");	
		courseTee.setTeeColor("Gold");
		courseTee.setCourseRating(new BigDecimal("67.0"));
		courseTee.setCoursePar(72);
		courseTee.setSlopeRating(116);
		courseTee.setTotalYardage(5672);		

		courseTeeDAO.addCourseTee(courseTee);	
	}

	private static void addTheCourse(DynamoClients dynamoClients) throws Exception 
	{
		courseDAO = new CourseDAO(dynamoClients);
		
		Course course = new Course();
		
		course.setCourseName("Oak Valley");
		course.setFront9Par(36);
		course.setBack9Par(36);
		course.setCoursePar(72);
		
		Hole hole = new Hole();
		hole.setHoleNumber(1);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(2);
		hole.setPar(5);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(3);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(4);
		hole.setPar(4);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(5);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(6);
		hole.setPar(3);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(7);
		hole.setPar(5);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(8);
		hole.setPar(3);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(9);
		hole.setPar(4);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(10);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(11);
		hole.setPar(3);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(12);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(13);
		hole.setPar(4);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(14);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(15);
		hole.setPar(5);
		course.getHolesList().add(hole);
		
		hole = new Hole();
		hole.setHoleNumber(16);
		hole.setPar(3);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(17);
		hole.setPar(4);
		course.getHolesList().add(hole);			
		
		hole = new Hole();
		hole.setHoleNumber(18);
		hole.setPar(5);
		course.getHolesList().add(hole);
		
		courseDAO.addCourse(course);		
	}
    
}
package com.pas.dao;

import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.Course;
import com.pas.beans.Hole;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseRowMapper implements RowMapper<Course>, Serializable
{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
    public Course mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
        Course course = new Course();
        
        course.setCourseID(rs.getInt("idGolfCourse"));
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

        return course;

    }
}

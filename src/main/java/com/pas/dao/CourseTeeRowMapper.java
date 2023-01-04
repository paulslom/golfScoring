package com.pas.dao;

import org.springframework.jdbc.core.RowMapper;

import com.pas.beans.CourseTee;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CourseTeeRowMapper implements RowMapper<CourseTee>, Serializable
{
    private static final long serialVersionUID = 1L;

	@Override
    public CourseTee mapRow(ResultSet rs, int rowNum) throws SQLException 
    {
        CourseTee courseTee = new CourseTee();
        
        courseTee.setCourseTeeID(rs.getInt("idGolfCourseTees"));
        courseTee.setCourseID(rs.getInt("idGolfCourse"));
		courseTee.setTeeColor(rs.getString("teeColor"));
		courseTee.setCourseRating(rs.getBigDecimal("courseRating"));
		courseTee.setSlopeRating(rs.getInt("courseSlope"));
		courseTee.setCoursePar(rs.getInt("coursePar"));	
		courseTee.setTotalYardage(rs.getInt("totalYardage"));
		
        return courseTee;
    }
}

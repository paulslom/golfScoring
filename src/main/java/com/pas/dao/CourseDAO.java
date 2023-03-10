package com.pas.dao;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.stereotype.Repository;

import com.pas.beans.Course;
import com.pas.beans.Group;
 
@Repository
public class CourseDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(CourseDAO.class);
	
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
	private Map<Integer,Course> coursesMap = new HashMap<Integer,Course>();
	private List<Course> courseSelections = new ArrayList<Course>();
	
	@PostConstruct
	private void initialize() 
	{
	   try 
	   {
	       setDataSource(dataSource);
	   } 
	   catch (final Exception ex) 
	   {
	      log.error("Got exception while initializing DAO: {}" +  ex.getStackTrace());
	   }
	}

	@Autowired
    public CourseDAO(DataSource dataSource) 
	{
		 this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	     this.dataSource = dataSource;
    }

	public void readCoursesFromDB(Group grp)
    {
		String sql = "select * from golfcourse where idgroup = :groupID";		 
		SqlParameterSource param = new MapSqlParameterSource("groupID", grp.getGroupID());
		this.setCourseSelections(namedParameterJdbcTemplate.query(sql, param, new CourseRowMapper())); 
		
		log.info("LoggedDBOperation: function-inquiry; table:course; rows:" + this.getCourseSelections().size());
		
		coursesMap = this.getCourseSelections().stream().collect(Collectors.toMap(Course::getCourseID, course -> course));
    }
	
	public Map<Integer, Course> getCoursesMap() 
	{
		return coursesMap;
	}

	public void setCoursesMap(Map<Integer, Course> coursesMap) 
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

package com.pas.dao;

import java.io.Serializable;
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

import com.pas.beans.CourseTee;
import com.pas.beans.Group;
 
@Repository
public class CourseTeeDAO extends JdbcDaoSupport implements Serializable
{
	private static final long serialVersionUID = 1L;

	private static Logger log = LogManager.getLogger(CourseTeeDAO.class);
	
	private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
	private final DataSource dataSource;
	
	private Map<Integer,CourseTee> CourseTeesMap = new HashMap<Integer,CourseTee>();
	
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
    public CourseTeeDAO(DataSource dataSource) 
	{
		 this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
	     this.dataSource = dataSource;
    }

	public List<CourseTee> readCourseTeesFromDB(Group grp)
    {
		String sql = "select gcTee.* from golfcoursetees gcTee inner join golfcourse gc on gc.idgolfcourse = gcTee.idgolfCourse where gc.idgroup = :groupID";
		 
		SqlParameterSource param = new MapSqlParameterSource("groupID", grp.getSelectedGroup().getGroupID());
		 
		List<CourseTee> CourseTeesList = namedParameterJdbcTemplate.query(sql, param, new CourseTeeRowMapper()); 
		
		CourseTeesMap = CourseTeesList.stream().collect(Collectors.toMap(CourseTee::getCourseTeeID, CourseTee -> CourseTee));
			
		return CourseTeesList;
    }
	
	public CourseTee readCourseTeeTeeFromDB(Integer CourseTeeID)
    {
		String sql = "select * from golfCourseTee where idgolfCourseTee = :CourseTeeID";
		 
		SqlParameterSource param = new MapSqlParameterSource("CourseTeeID", CourseTeeID);
		 
		CourseTee CourseTee = namedParameterJdbcTemplate.queryForObject(sql, param, new CourseTeeRowMapper()); 	
    	
    	return CourseTee;
    }

	public Map<Integer, CourseTee> getCourseTeesMap() 
	{
		return CourseTeesMap;
	}

	public void setCourseTeesMap(Map<Integer, CourseTee> CourseTeesMap) 
	{
		this.CourseTeesMap = CourseTeesMap;
	}

	
}

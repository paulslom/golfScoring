package com.pas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

@Named("pc_Course")
@RequestScoped
public class Course extends SpringBeanAutowiringSupport implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
	
	private Integer courseID;
	private String courseName;
	private int front9Par;
	private int back9Par;
	private int coursePar;
	private List<Hole> holesList = new ArrayList<Hole>();
	private Map<Integer,Hole> holesMap = new HashMap<Integer,Hole>();	
	 
	public int getCourseID() {
		return courseID;
	}
	public void setCourseID(int courseID) {
		this.courseID = courseID;
	}
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public int getFront9Par() {
		return front9Par;
	}
	public void setFront9Par(int front9Par) {
		this.front9Par = front9Par;
	}
	public int getBack9Par() {
		return back9Par;
	}
	public void setBack9Par(int back9Par) {
		this.back9Par = back9Par;
	}
	public List<Hole> getHolesList() {
		return holesList;
	}
	public void setHolesList(List<Hole> holesList) {
		this.holesList = holesList;
	}
	
	@Override
    public boolean equals(final Object o) 
	{
        if (this == o) 
        {
            return true;
        }
        if (!(o instanceof Integer)) 
        {
            return false;
        }
        
        final Integer that = (Integer) o;
        return Objects.equals(courseID, that);
    }
	public Map<Integer, Hole> getHolesMap() {
		return holesMap;
	}
	public void setHolesMap(Map<Integer, Hole> holesMap) {
		this.holesMap = holesMap;
	}
	public int getCoursePar() {
		return coursePar;
	}
	public void setCoursePar(int coursePar) {
		this.coursePar = coursePar;
	}
	public void setCourseID(Integer courseID) {
		this.courseID = courseID;
	}
	
}

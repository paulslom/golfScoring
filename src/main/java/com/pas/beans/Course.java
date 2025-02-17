package com.pas.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Course implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
	
	private String courseID;
	private int oldCourseID;
	private String courseName;
	private int front9Par;
	private int back9Par;
	private int coursePar;
	
	private Integer hole1Par;
	private Integer hole2Par;
	private Integer hole3Par;
	private Integer hole4Par;
	private Integer hole5Par;
	private Integer hole6Par;
	private Integer hole7Par;
	private Integer hole8Par;
	private Integer hole9Par;
	private Integer hole10Par;
	private Integer hole11Par;
	private Integer hole12Par;
	private Integer hole13Par;
	private Integer hole14Par;
	private Integer hole15Par;
	private Integer hole16Par;
	private Integer hole17Par;
	private Integer hole18Par;
	
	private List<Hole> holesList = new ArrayList<Hole>();
	private Map<Integer,Hole> holesMap = new HashMap<Integer,Hole>();	
	 
	
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
        if (!(o instanceof String)) 
        {
            return false;
        }
        
        final String that = (String) o;
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
	
	public String getCourseID() {
		return courseID;
	}
	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}
	public int getOldCourseID() {
		return oldCourseID;
	}
	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}
	public Integer getHole1Par() {
		return hole1Par;
	}
	public void setHole1Par(Integer hole1Par) {
		this.hole1Par = hole1Par;
	}
	public Integer getHole2Par() {
		return hole2Par;
	}
	public void setHole2Par(Integer hole2Par) {
		this.hole2Par = hole2Par;
	}
	public Integer getHole3Par() {
		return hole3Par;
	}
	public void setHole3Par(Integer hole3Par) {
		this.hole3Par = hole3Par;
	}
	public Integer getHole4Par() {
		return hole4Par;
	}
	public void setHole4Par(Integer hole4Par) {
		this.hole4Par = hole4Par;
	}
	public Integer getHole5Par() {
		return hole5Par;
	}
	public void setHole5Par(Integer hole5Par) {
		this.hole5Par = hole5Par;
	}
	public Integer getHole6Par() {
		return hole6Par;
	}
	public void setHole6Par(Integer hole6Par) {
		this.hole6Par = hole6Par;
	}
	public Integer getHole7Par() {
		return hole7Par;
	}
	public void setHole7Par(Integer hole7Par) {
		this.hole7Par = hole7Par;
	}
	public Integer getHole8Par() {
		return hole8Par;
	}
	public void setHole8Par(Integer hole8Par) {
		this.hole8Par = hole8Par;
	}
	public Integer getHole9Par() {
		return hole9Par;
	}
	public void setHole9Par(Integer hole9Par) {
		this.hole9Par = hole9Par;
	}
	public Integer getHole10Par() {
		return hole10Par;
	}
	public void setHole10Par(Integer hole10Par) {
		this.hole10Par = hole10Par;
	}
	public Integer getHole11Par() {
		return hole11Par;
	}
	public void setHole11Par(Integer hole11Par) {
		this.hole11Par = hole11Par;
	}
	public Integer getHole12Par() {
		return hole12Par;
	}
	public void setHole12Par(Integer hole12Par) {
		this.hole12Par = hole12Par;
	}
	public Integer getHole13Par() {
		return hole13Par;
	}
	public void setHole13Par(Integer hole13Par) {
		this.hole13Par = hole13Par;
	}
	public Integer getHole14Par() {
		return hole14Par;
	}
	public void setHole14Par(Integer hole14Par) {
		this.hole14Par = hole14Par;
	}
	public Integer getHole15Par() {
		return hole15Par;
	}
	public void setHole15Par(Integer hole15Par) {
		this.hole15Par = hole15Par;
	}
	public Integer getHole16Par() {
		return hole16Par;
	}
	public void setHole16Par(Integer hole16Par) {
		this.hole16Par = hole16Par;
	}
	public Integer getHole17Par() {
		return hole17Par;
	}
	public void setHole17Par(Integer hole17Par) {
		this.hole17Par = hole17Par;
	}
	public Integer getHole18Par() {
		return hole18Par;
	}
	public void setHole18Par(Integer hole18Par) {
		this.hole18Par = hole18Par;
	}
	
	
}

package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.model.SelectItem;
import jakarta.inject.Named;

import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.pas.util.BeanUtilJSF;

@Named("pc_CourseTee")
@SessionScoped
public class CourseTee extends SpringBeanAutowiringSupport implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
		
	private Integer courseTeeID;
	private Integer courseID;
	private String teeColor;
	private BigDecimal courseRating;
	private int coursePar;
	private int slopeRating;
	private int totalYardage;
	
	private Map<Integer, List<SelectItem>> teeSelectionsMap = new HashMap<>();
	
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
        return Objects.equals(courseTeeID, that);
    }


	public Integer getCourseTeeID() {
		return courseTeeID;
	}


	public void setCourseTeeID(Integer courseTeeID) {
		this.courseTeeID = courseTeeID;
	}


	public Integer getCourseID() {
		return courseID;
	}


	public void setCourseID(Integer courseID) {
		this.courseID = courseID;
	}


	public String getTeeColor() {
		return teeColor;
	}


	public void setTeeColor(String teeColor) {
		this.teeColor = teeColor;
	}


	public BigDecimal getCourseRating() {
		return courseRating;
	}


	public void setCourseRating(BigDecimal courseRating) {
		this.courseRating = courseRating;
	}


	public int getCoursePar() {
		return coursePar;
	}


	public void setCoursePar(int coursePar) {
		this.coursePar = coursePar;
	}


	public int getSlopeRating() {
		return slopeRating;
	}


	public void setSlopeRating(int slopeRating) {
		this.slopeRating = slopeRating;
	}


	public int getTotalYardage() {
		return totalYardage;
	}


	public void setTotalYardage(int totalYardage) {
		this.totalYardage = totalYardage;
	}


	public Map<Integer, List<SelectItem>> getTeeSelectionsMap()
	{		
		if (teeSelectionsMap == null || teeSelectionsMap.size() == 0)
		{
			Integer lastCourseID = 0;
			List<SelectItem> ctList = new ArrayList<>();
		
			GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");	
			if (golfmain != null && golfmain.getCourseTees() != null && golfmain.getCourseTees().size() > 0)
	      	{
				for (int i = 0; i < golfmain.getCourseTees().size(); i++) 
				{					
					CourseTee ct = golfmain.getCourseTees().get(i);
					if (ct.getCourseID() != lastCourseID && lastCourseID != 0)
					{	
						List<SelectItem> tempList = new ArrayList<>();
						tempList.addAll(ctList);
						teeSelectionsMap.put(lastCourseID, tempList);
						ctList.clear();						
					}
					SelectItem selItem = new SelectItem();
					selItem.setLabel(ct.getTeeColor());
					selItem.setValue(ct.getCourseTeeID());
					ctList.add(selItem);
					lastCourseID = ct.getCourseID();
				}
				
				if (ctList != null && ctList.size() > 0)
				{
					teeSelectionsMap.put(lastCourseID, ctList);
				}
	      	}
		}
		
		//let's log the teeSelections map here
		for (Map.Entry<Integer, List<SelectItem>> entry : teeSelectionsMap.entrySet())
		{
			//log.info("Course ID = " + entry.getKey());
			List<SelectItem> loggedList = entry.getValue();
			for (int i = 0; i < loggedList.size(); i++) 
			{
				//SelectItem selItem = loggedList.get(i);
				//log.info("Tee ID = " + selItem.getValue() + " Tee Color = " + selItem.getLabel());
			}
			//log.info("---------");
		}
                             
		return teeSelectionsMap;
	}


	public void setTeeSelectionsMap(Map<Integer, List<SelectItem>> teeSelectionsMap) 
	{
		this.teeSelectionsMap = teeSelectionsMap;
	}


	public List<SelectItem> getTeeSelections(Integer courseID)
	{
		if (getTeeSelectionsMap() != null)
		{
			return getTeeSelectionsMap().get(courseID);
		}
		return null;
	}
	
	
}

package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.pas.dynamodb.DynamoCourseTee;

import jakarta.faces.model.SelectItem;
import jakarta.inject.Inject;

public class CourseTee implements Serializable
{
	private static final long serialVersionUID = 3523975134478530653L;
		
	private String courseTeeID;
	private int oldCourseTeeID;
	private String courseID;
	private int oldCourseID;
	private String teeColor;
	private BigDecimal courseRating;
	private Integer coursePar;
	private Integer slopeRating;
	private Integer totalYardage;
	
	@Inject GolfMain golfmain;
	
	private Map<String, List<SelectItem>> teeSelectionsMap = new HashMap<>();
	
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
        return Objects.equals(courseTeeID, that);
    }
	
	public Map<String, List<SelectItem>> getTeeSelectionsMap()
	{		
		if (teeSelectionsMap == null || teeSelectionsMap.size() == 0)
		{
			String lastCourseID = "0";
			List<SelectItem> ctList = new ArrayList<>();
		
			if (golfmain != null && golfmain.getCourseTees() != null && golfmain.getCourseTees().size() > 0)
	      	{
				for (int i = 0; i < golfmain.getCourseTees().size(); i++) 
				{					
					DynamoCourseTee ct = golfmain.getCourseTees().get(i);
					if (!ct.getCourseID().equalsIgnoreCase(lastCourseID) && !lastCourseID.equalsIgnoreCase("0"))
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
		for (Map.Entry<String, List<SelectItem>> entry : teeSelectionsMap.entrySet())
		{
			//logger.info("Course ID = " + entry.getKey());
			List<SelectItem> loggedList = entry.getValue();
			for (int i = 0; i < loggedList.size(); i++) 
			{
				//SelectItem selItem = loggedList.get(i);
				//logger.info("Tee ID = " + selItem.getValue() + " Tee Color = " + selItem.getLabel());
			}
			//logger.info("---------");
		}
                             
		return teeSelectionsMap;
	}


	public void setTeeSelectionsMap(Map<String, List<SelectItem>> teeSelectionsMap) 
	{
		this.teeSelectionsMap = teeSelectionsMap;
	}

	public List<SelectItem> getTeeSelections(String courseID)
	{
		if (getTeeSelectionsMap() != null)
		{
			return getTeeSelectionsMap().get(courseID);
		}
		return null;
	}
	
	public static class CourseTeeComparator implements Comparator<CourseTee> 
	{
		public int compare(CourseTee courseTee1, CourseTee courseTee2)
		{
			return courseTee1.getCourseRating().compareTo(courseTee2.getCourseRating());
		}		
	}
	
	public String getCourseTeeID() {
		return courseTeeID;
	}

	public void setCourseTeeID(String courseTeeID) {
		this.courseTeeID = courseTeeID;
	}

	public String getCourseID() {
		return courseID;
	}

	public void setCourseID(String courseID) {
		this.courseID = courseID;
	}

	public int getOldCourseTeeID() {
		return oldCourseTeeID;
	}

	public void setOldCourseTeeID(int oldCourseTeeID) {
		this.oldCourseTeeID = oldCourseTeeID;
	}

	public int getOldCourseID() {
		return oldCourseID;
	}

	public void setOldCourseID(int oldCourseID) {
		this.oldCourseID = oldCourseID;
	}

	public Integer getCoursePar() {
		return coursePar;
	}

	public void setCoursePar(Integer coursePar) {
		this.coursePar = coursePar;
	}

	public Integer getSlopeRating() {
		return slopeRating;
	}

	public void setSlopeRating(Integer slopeRating) {
		this.slopeRating = slopeRating;
	}

	public Integer getTotalYardage() {
		return totalYardage;
	}

	public void setTotalYardage(Integer totalYardage) {
		this.totalYardage = totalYardage;
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

	
}

package com.pas.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

import com.pas.beans.Course;
import com.pas.beans.GolfMain;

@FacesConverter(forClass = Course.class)
public class CourseConverter implements Converter<Object>
{
	Map<Integer,Course> coursesMap = new HashMap<Integer,Course>();
	
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object modelValue) 
	{
	    if (modelValue == null) 
	    {
	        return "";
	    }

	    if (modelValue instanceof Integer) 
	    {
	        return String.valueOf(((Integer) modelValue));
	    } 
	    else 
	    {
	        throw new ConverterException(new FacesMessage(modelValue + " is not a valid Course"));
	    }
	}
	
	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String submittedValue) 
	{
	    if (submittedValue == null || submittedValue.isEmpty()) 
	    {
	        return null;
	    }

	    try 
	    {
	    	if (coursesMap.isEmpty())
	    	{
	    		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
	        	List<Course> courseSelections = gm.getCourseSelections();
	        	coursesMap = courseSelections.stream().collect(Collectors.toMap(Course::getCourseID, crs -> crs));	   
	    	}
			int courseID = Integer.valueOf(submittedValue);
	        return coursesMap.get(courseID);
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid course ID"), e);
	    }
	}

}

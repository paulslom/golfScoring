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

import com.pas.beans.GolfMain;
import com.pas.beans.Group;

@FacesConverter(forClass = Group.class)
public class GroupConverter implements Converter<Object>
{
	Map<String,Group> groupsMap = new HashMap<>();	
	
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
	    else if (modelValue instanceof Group) 
	    {
	    	Group tempGroup = (Group)modelValue;
	        return String.valueOf(tempGroup.getGroupID());
	    } 
	    else 
	    {
	        throw new ConverterException(new FacesMessage(modelValue + " is not a valid Group"));
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
	    	if (groupsMap.isEmpty())
	    	{
	    		GolfMain gm = BeanUtilJSF.getBean("pc_GolfMain");
	        	List<Group> fullGroupList = gm.getGroupsList();
	        	groupsMap = fullGroupList.stream().collect(Collectors.toMap(Group::getGroupID, grp -> grp));	   
	    	}
			
			Group returnGroup = groupsMap.get(submittedValue);
	        return returnGroup;
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid group ID"), e);
	    }
	}

}

package com.pas.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;

import com.pas.beans.GolfMain;
import com.pas.beans.Group;

@FacesConverter(forClass = Group.class)
public class GroupConverter implements Converter<Object>
{
	Map<Integer,Group> groupsMap = new HashMap<Integer,Group>();	
	
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
			
			int groupID = Integer.valueOf(submittedValue);
			Group returnGroup = groupsMap.get(groupID);
	        return returnGroup;
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid group ID"), e);
	    }
	}

}

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

import com.pas.beans.TeeTime;

@FacesConverter(forClass = TeeTime.class)
public class TeeTimeConverter implements Converter<Object>
{
	Map<Integer,TeeTime> teeTimesMap = new HashMap<Integer, TeeTime>();
	
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
	    else if (modelValue instanceof TeeTime) 
	    {
	    	TeeTime tempTeeTime = (TeeTime)modelValue;
	        return String.valueOf(tempTeeTime.getTeeTimeID());
	    } 
	    else 
	    {
	        throw new ConverterException(new FacesMessage(modelValue + " is not a valid TeeTime"));
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
	    	if (teeTimesMap.isEmpty())
	    	{
	    		TeeTime teeTime = BeanUtilJSF.getBean("pc_TeeTime");
	        	List<TeeTime> fullTeeTimeList = teeTime.getTeeTimeList();
	        	teeTimesMap = fullTeeTimeList.stream().collect(Collectors.toMap(TeeTime::getTeeTimeID, tt -> tt));	   
	    	}
			
			int teeTimeID = Integer.valueOf(submittedValue);
			TeeTime returnTeeTime = teeTimesMap.get(teeTimeID);
	        return returnTeeTime;
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid tee Time ID"), e);
	    }
	}

}

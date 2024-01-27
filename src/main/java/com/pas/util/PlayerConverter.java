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
import com.pas.beans.Player;

@FacesConverter(forClass = Player.class, value = "playerConverter")
public class PlayerConverter implements Converter<Object>
{
	Map<String,Player> playersMap = new HashMap<>();
	
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
	    else if (modelValue instanceof Player) 
	    {
	    	Player tempPlayer = (Player)modelValue;
	        return String.valueOf(tempPlayer.getPlayerID());
	    } 
	    else 
	    {
	        throw new ConverterException(new FacesMessage(modelValue + " is not a valid Player"));
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
	    	if (playersMap.isEmpty())
	    	{
	    		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		    		
	        	List<Player> fullPlayerList = golfmain.getFullPlayerList();
	        	playersMap = fullPlayerList.stream().collect(Collectors.toMap(Player::getPlayerID, ply -> ply));	   
	    	}
			
			Player returnPlayer = playersMap.get(submittedValue);
	        return returnPlayer;
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid player ID"), e);
	    }
	}

}

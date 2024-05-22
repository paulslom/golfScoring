package com.pas.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;

import com.pas.beans.GolfMain;
import com.pas.beans.Player;

public class PlayerConverter implements Converter<Object>
{
	Map<String,Player> playersMap = new HashMap<>();
	
	@Autowired private final GolfMain golfmain;
	
	public PlayerConverter() 
	{
		this.golfmain = new GolfMain("ignore");		
	}
	
	public PlayerConverter(GolfMain golfmain) 
	{
		this.golfmain = golfmain;
	}
	
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object modelValue) 
	{
	    if (modelValue == null) 
	    {
	        return "";
	    }

	    if (modelValue instanceof String) 
	    {
	        return (String)modelValue;
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

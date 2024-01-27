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

import com.pas.beans.Game;
import com.pas.beans.GolfMain;

@FacesConverter(forClass = Game.class)
public class GameConverter implements Converter<Object>
{
	Map<String,Game> gamesMap = new HashMap<String,Game>();	
	
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
	    else if (modelValue instanceof Game)
	    {
	    	Game thisGame = (Game)modelValue;
	    	return String.valueOf(thisGame.getGameID());
	    }
	    else 
	    {
	        throw new ConverterException(new FacesMessage(modelValue + " is not a valid Game"));
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
	    	if (gamesMap.isEmpty())
	    	{
	    		GolfMain golfmain = BeanUtilJSF.getBean("pc_GolfMain");		
	        	List<Game> fullGameList = golfmain.getFullGameList();
	        	gamesMap = fullGameList.stream().collect(Collectors.toMap(Game::getGameID, gm -> gm));	   
	    	}
			
			return gamesMap.get(submittedValue);
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid game ID"), e);
	    }
	}

}

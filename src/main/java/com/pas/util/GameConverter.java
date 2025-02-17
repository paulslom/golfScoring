package com.pas.util;

import java.io.Serializable;

import com.pas.beans.GolfMain;
import com.pas.dynamodb.DynamoGame;

import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named("pc_GameConverter")
@SessionScoped
public class GameConverter implements Serializable, Converter<Object>
{
	private static final long serialVersionUID = 1L;
	
	@Inject GolfMain golfmain;
	
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
	    else if (modelValue instanceof DynamoGame) 
	    {
	    	DynamoGame tempGame = (DynamoGame)modelValue;
	        return String.valueOf(tempGame.getGameID());
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
	    	return golfmain.getFullGamesMap().get(submittedValue);
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid game ID"), e);
	    }
	}

}

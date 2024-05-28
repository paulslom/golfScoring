package com.pas.util;

import java.util.HashMap;
import java.util.Map;

import com.pas.beans.GolfMain;
import com.pas.beans.Player;
import com.pas.dao.PlayerDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.ConverterException;
import jakarta.faces.convert.FacesConverter;

public class PlayerConverter implements Converter<Object>
{
	Map<String,Player> playersMap = new HashMap<>();
		
	public PlayerConverter() 
	{
		DynamoClients dynamoClients;
		try 
		{
			dynamoClients = DynamoUtil.getDynamoClients();
			PlayerDAO playerDAO = new PlayerDAO(dynamoClients, new GolfMain("ignore"));
			playerDAO.readPlayersFromDB();
			playersMap = playerDAO.getFullPlayersMapByPlayerID();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}		
		
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
	    	Player returnPlayer = playersMap.get(submittedValue);
	        return returnPlayer;
	    } 
	    catch (NumberFormatException e) 
	    {
	        throw new ConverterException(new FacesMessage(submittedValue + " is not a valid player ID"), e);
	    }
	}

}

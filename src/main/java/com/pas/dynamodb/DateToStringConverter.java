package com.pas.dynamodb;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateToStringConverter
{
    public Date unconvert(String s) 
    {
    	//Example of what we are unconverting: 2020-03-21T00:00:00.000-04:00
    	
    	DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);
    	LocalDateTime ldt = LocalDateTime.parse(s, inputFormatter);
    	Date returnDate = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
        return returnDate;
    }
    
    public static String convertDateToDynamoStringFormat(Date inputDate)
    {
    	String returnString = "";
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    	sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"));
		returnString = sdf.format(inputDate);
    	return returnString;
    }
}

package com.pas.dynamodb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;

public class DateToStringConverter implements DynamoDBTypeConverter<String, Date> 
{
	@Override
    public String convert(Date inputDate) 
    {
    	LocalDate localDate = LocalDate.ofInstant(inputDate.toInstant(), ZoneId.systemDefault());
        return localDate.toString();
    }

    @Override
    public Date unconvert(String s) 
    {
    	//Example of what we are unconverting: 2020-03-21T00:00:00.000-04:00
    	
    	DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ENGLISH);
    	LocalDate ldt = LocalDate.parse(s, inputFormatter);
    	Date returnDate = Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return returnDate;
    }	
	
}

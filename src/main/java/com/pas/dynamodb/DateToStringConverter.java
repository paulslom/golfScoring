package com.pas.dynamodb;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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
    	LocalDate ldt = LocalDate.parse(s);
    	Date returnDate = Date.from(ldt.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return returnDate;
    }	
	
}

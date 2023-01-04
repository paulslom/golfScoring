/*
 * Created on May 24, 2006
 *
 */
package com.pas.util;

/**
 * @author paslom
 *
 */
public class RetrieveSystemProperty
{
	public static void main(String[] args) throws Exception
	{
		String rdsDbName = System.getProperty("RDS_DB_NAME");
		System.out.println("Rds db name = " + rdsDbName);
		
		String rdsUserName = System.getProperty("RDS_USERNAME");
		System.out.println("Rds user name = " + rdsUserName);
		
		String rdsPassword = System.getProperty("RDS_PASSWORD");
		System.out.println("Rds password = " + rdsPassword);
		
		String rdsHostName = System.getProperty("RDS_HOSTNAME");
		System.out.println("Rds hostname = " + rdsHostName);
		
		String rdsPort = System.getProperty("RDS_PORT");
		System.out.println("Rds port = " + rdsPort);
	}	
	
}

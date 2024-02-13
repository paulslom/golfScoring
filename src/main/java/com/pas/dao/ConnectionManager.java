package com.pas.dao;

import java.io.Serializable;

import javax.sql.DataSource;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mysql.cj.jdbc.MysqlDataSource;
import com.pas.util.Utils;

@Configuration
public class ConnectionManager implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ConnectionManager.class);	

	@Bean
    public DataSource getDataSource() 
	{
		logger.info("entering getDataSource method in golfScoring application");		
		MysqlDataSource ds = Utils.getDatasourceProperties();		
       	return ds;   		
    }
    
}

package com.pas.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import com.pas.util.FileDataLoader;

import jakarta.servlet.ServletContext;

@Configuration
public class MyWebAppInitializer implements ServletContextInitializer 
{
	private static Logger logger = LogManager.getLogger(MyWebAppInitializer.class);	
	
	@Override
    public void onStartup(ServletContext sc) 
	{                
        try 
        {
        	logger.info("entering MyWebAppInitializer");
            logger.info("Calling file data Loader which might reload data files to table depending on setting...");
            FileDataLoader fileDataLoader = new FileDataLoader();
            boolean success = fileDataLoader.load();
            if (success) 
            {
                logger.info("Successfully updated dynamo db database.");
            }
            else 
            {
                logger.error("Failed to update dynamo db database.");
            }
        } 
        catch (final Exception ex) 
        {
            logger.error("Unable to perform start-up " + ex.getMessage(), ex);
        }
       
    }
}
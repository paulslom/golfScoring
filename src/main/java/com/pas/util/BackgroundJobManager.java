package com.pas.util;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.pas.beans.GolfMain;
import com.pas.dynamodb.DynamoUtil;

@WebListener
public class BackgroundJobManager implements ServletContextListener 
{
	private static Logger logger = LogManager.getLogger(BackgroundJobManager.class);

    private ScheduledExecutorService scheduler;
   
    @Autowired private final GolfMain golfmain;
    
    public BackgroundJobManager(GolfMain golfmain) 
    {
		this.golfmain = golfmain;
	}
    
    @Override
    public void contextInitialized(ServletContextEvent event) 
    {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        long scDelay = get8AMinEast();
        try 
        {
			scheduler.scheduleAtFixedRate(new DailyEmailJob(golfmain), scDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
			logger.info("Daily email job set to run every day at 8 am ET");
		}
        catch (Exception e) 
        {
			logger.error("unable to create scheduler for DailyEmailJob " + e.getMessage(), e);
		}       
		
    }

    private long get8AMinEast() 
    {
    	int hour = 8;
    	int minute = 0;
    	int second = 0;
    	
    	//int hour = 19;
    	//int minute = 15;
    	//int second = 0;
    	
    	ZonedDateTime now = ZonedDateTime.now(ZoneId.of(Utils.MY_TIME_ZONE));
    	ZonedDateTime nextRun = now.withHour(hour).withMinute(minute).withSecond(second);
    	
    	if(now.compareTo(nextRun) > 0)
    	{
    		nextRun = nextRun.plusDays(1);
    	}

    	Duration duration = Duration.between(now, nextRun);
    	long initialDelay = duration.getSeconds();
		return initialDelay;
	}

	@Override
    public void contextDestroyed(ServletContextEvent event) 
    {
		logger.info("Destroying servlet and app is shutting down");
		DynamoUtil.stopDynamoServer();
        scheduler.shutdownNow();
    }

}
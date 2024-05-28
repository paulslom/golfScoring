package com.pas.spring;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import com.pas.util.DailyEmailJob;
import com.pas.util.FileDataLoader;
import com.pas.util.Utils;
import com.sun.faces.config.FacesInitializer;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

@Configuration
public class MyWebAppInitializer implements ServletContextInitializer 
{
	private static Logger logger = LogManager.getLogger(MyWebAppInitializer.class);	
	
	private ScheduledExecutorService scheduler;
	 
	@Override
    public void onStartup(ServletContext sc) 
	{                
        try 
        {
        	logger.info("entering MyWebAppInitializer");
        	
        	logger.info("initializing Jsf items");
        	EnhancedListener cdiInitializer = new EnhancedListener();
            cdiInitializer.onStartup(null, sc);

            ServletContainerInitializer facesInitializer = new FacesInitializer();
            /* 
            FilterRegistration myFilter = sc.addFilter("myFilter", new MyFilter());
            myFilter.setInitParameter("oauthConnectionClass", "net.oauth.client.httpclient4.HttpClient4");
            myFilter.setInitParameter("redirect", "true");
        */
            sc.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString()); 
            sc.setInitParameter("jakarta.faces.DEFAULT_SUFFIX", ".xhtml");
            sc.setInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
            sc.setInitParameter("jakarta.faces.PROJECT_STAGE", "Production");
            sc.setInitParameter("jakarta.faces.VALIDATE_EMPTY_FIELDS", "false");
            sc.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", "true");
            sc.setInitParameter("jakarta.faces.FACELETS_VIEW_MAPPINGS", "*.xhtml");
            sc.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/main-faces-config.xml");
            sc.setInitParameter("jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
            sc.setInitParameter("primefaces.UPLOADER", "commons");
            
            ServletRegistration.Dynamic elResolverInitializer = sc.addServlet("elResolverInit", new ELResolverInitializerServlet());
            elResolverInitializer.setLoadOnStartup(2);
            
            //only one class is necessary here, not sure why they are not ALL needed but they are not.
            Set<Class<?>> jsfAnnotatedClasses = new HashSet<>();
            jsfAnnotatedClasses.add(com.pas.beans.GolfUser.class);
            
            logger.info("total jsf annotated classes: " + jsfAnnotatedClasses.size()); 
            
            Iterator<Class<?>> itr = jsfAnnotatedClasses.iterator(); 
            while(itr.hasNext())
            { 
            	logger.info(itr.next()); 
            }
            facesInitializer.onStartup(jsfAnnotatedClasses, sc);
            
            logger.info("completed initialization of Jsf items");
            
            logger.info("Calling file data Loader which might reload data files to table depending on setting...");
            FileDataLoader fileDataLoader = new FileDataLoader();
            boolean success = fileDataLoader.load();
            if (success) 
            {
                logger.info("Successfully loaded dynamo db database.");
            }
            
            logger.info("Setting up Daily Email Job to run at 8 am in the east");
            
            scheduler = Executors.newSingleThreadScheduledExecutor();
            long scDelay = Utils.getDailyEmailTime();
            try 
            {
    			scheduler.scheduleAtFixedRate(new DailyEmailJob(), scDelay, TimeUnit.DAYS.toSeconds(1), TimeUnit.SECONDS);
    			logger.info("Daily email job set to run every day at 8 am ET");
    		}
            catch (Exception e) 
            {
    			logger.error("unable to create scheduler for DailyEmailJob " + e.getMessage(), e);
    		}    
            
            logger.info("completed Setting up Daily Email Job to run at 8 am in the east");
            
        } 
        catch (final Exception ex) 
        {
            logger.error("Unable to perform start-up " + ex.getMessage(), ex);
        }
       
    }
	
}
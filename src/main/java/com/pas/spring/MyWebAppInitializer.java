package com.pas.spring;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRegistration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import com.pas.util.FileDataLoader;
import com.sun.faces.config.FacesInitializer;

public class MyWebAppInitializer extends FacesInitializer implements WebApplicationInitializer 
{
	private static Logger logger = LogManager.getLogger(MyWebAppInitializer.class);	
	
	@Override
    public void onStartup(ServletContext sc) 
	{
        AnnotationConfigWebApplicationContext rootContext = new AnnotationConfigWebApplicationContext();
        rootContext.register(AppConfig.class);
        rootContext.register(SecurityConfig.class);       
        
        sc.addListener(new ContextLoaderListener(rootContext));

        sc.addFilter("securityFilter", new DelegatingFilterProxy("springSecurityFilterChain"))
          .addMappingForUrlPatterns(null, false, "/*");
       
        // Create the dispatcher servlet's Spring application context
        AnnotationConfigWebApplicationContext dispatcherContext = new AnnotationConfigWebApplicationContext();
        dispatcherContext.register(DispatcherConfig.class);

        // Register and map the dispatcher servlet
        ServletRegistration.Dynamic dispatcher = sc.addServlet("dispatcher", new DispatcherServlet(dispatcherContext));
        dispatcher.setLoadOnStartup(1);
        dispatcher.addMapping("/");
        
        try 
        {
            logger.info("ApplicationReady event trigger to insert");
            logger.info("Loading data files to table...");
            FileDataLoader fileDataLoader = new FileDataLoader();
            boolean success = fileDataLoader.load();
            if (success) 
            {
                logger.info("Successfully updated database.");
            }
            else 
            {
                logger.error("Failed to update database.");
            }
        } 
        catch (final Exception ex) 
        {
            logger.error("Unable to perform start-up " + ex.getMessage(), ex);
        }
       
    }
}
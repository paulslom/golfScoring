package com.pas.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jboss.weld.environment.servlet.EnhancedListener;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Configuration;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;

import com.google.common.reflect.ClassPath;
//import com.google.common.collect.ImmutableMap;

import com.sun.faces.config.FacesInitializer;

@Configuration
public class JsfInitializer implements ServletContextInitializer 
{
	private static Logger logger = LogManager.getLogger(JsfInitializer.class);	
	
    @Override
    public void onStartup(ServletContext context) throws ServletException 
    {
    	logger.info("entering JsfInitializer");
    	
        EnhancedListener cdiInitializer = new EnhancedListener();
        cdiInitializer.onStartup(null, context);

        ServletContainerInitializer facesInitializer = new FacesInitializer();
        
        context.setInitParameter("com.sun.faces.forceLoadConfiguration", Boolean.TRUE.toString()); 
        context.setInitParameter("jakarta.faces.DEFAULT_SUFFIX", ".xhtml");
        context.setInitParameter("jakarta.faces.STATE_SAVING_METHOD", "client");
        context.setInitParameter("jakarta.faces.PROJECT_STAGE", "Production");
        context.setInitParameter("jakarta.faces.VALIDATE_EMPTY_FIELDS", "false");
        context.setInitParameter("jakarta.faces.FACELETS_SKIP_COMMENTS", "true");
        context.setInitParameter("jakarta.faces.FACELETS_VIEW_MAPPINGS", "*.xhtml");
        context.setInitParameter("jakarta.faces.CONFIG_FILES", "/WEB-INF/main-faces-config.xml");
        context.setInitParameter("jakarta.faces.DATETIMECONVERTER_DEFAULT_TIMEZONE_IS_SYSTEM_TIMEZONE", "true");
        //context.setInitParameter("primefaces.THEME", "aristo");
        //context.setInitParameter("primefaces.PUBLIC_CAPTCHA_KEY", "DDDDD");
        //context.setInitParameter("primefaces.PRIVATE_CAPTCHA_KEY", "EEEEE");
        context.setInitParameter("primefaces.UPLOADER", "commons");
        
        facesInitializer.onStartup(loadJSFAnnotatedClasses("com.pas.beans", "com.pas.util"), context);
        
        logger.info("exiting JsfInitializer.  All annotated classes loaded");
    }
    
    private Set<Class<?>> loadJSFAnnotatedClasses(String... packageNames) 
    {
        Set<Class<?>> annotatedClasses = new HashSet<Class<?>>();
        try 
        {
            for (String packageName : packageNames) 
            {
                annotatedClasses.addAll(ClassPath.from(ClassLoader.getSystemClassLoader()).getAllClasses().stream()
                        .filter(clazz -> clazz.getPackageName().equalsIgnoreCase(packageName))
                        .map(clazz -> clazz.load()).collect(Collectors.toSet()));
            }
        } 
        catch (Exception e) 
        {
            return annotatedClasses;
        }

        return annotatedClasses;
    }

}
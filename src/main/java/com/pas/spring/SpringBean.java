package com.pas.spring;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletContext;

@Component
public class SpringBean 
{
	//private static Logger logger = LogManager.getLogger(SpringBean.class);	
	
	@Autowired
    private ServletContext servletContext;

    @PostConstruct
    public void showIt() 
    {
    	getContextRoot();        
    }
    
    public String getContextRoot()
    {
    	return servletContext.getContextPath();
    }
}

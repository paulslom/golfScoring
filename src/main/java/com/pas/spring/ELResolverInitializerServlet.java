package com.pas.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.jsf.el.SpringBeanFacesELResolver;

import jakarta.faces.context.FacesContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;

public class ELResolverInitializerServlet extends HttpServlet 
{

	private static Logger logger = LogManager.getLogger(ELResolverInitializerServlet.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	public ELResolverInitializerServlet() 
	{
	    super();
	}
	
	public void init(ServletConfig config) throws ServletException 
	{
	    FacesContext context = FacesContext.getCurrentInstance();
	    logger.info("::::::::::::::::::: Faces context: " + context);
	    context.getApplication().addELResolver(new SpringBeanFacesELResolver());	
	}

}

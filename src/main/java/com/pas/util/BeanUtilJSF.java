package com.pas.util;

import javax.el.ELContext;
import javax.faces.context.FacesContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.beans.TeeTime;

public class BeanUtilJSF
{
	private static Logger log = LogManager.getLogger(TeeTime.class);

    @SuppressWarnings("unchecked")
    public static <T> T getBean(String beanName) 
    {
        FacesContext context = FacesContext.getCurrentInstance();
        return (T) context.getApplication().evaluateExpressionGet(context, "#{" + beanName + "}", Object.class);
    }
    
    public static void setBean(String beanName, Object bean) 
    {    
        try 
        {
        	ELContext elContext = FacesContext.getCurrentInstance().getELContext();
        	elContext.getELResolver().setValue(elContext, null, beanName, bean);           
        } 
        catch (Exception e)
        {
            log.error("Error saving instance of bean " + beanName + ". Exception is = " + e.getMessage());           
        }
    }
}

package com.pas.spring;

import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/error")
public class SimpleErrorController implements ErrorController 
{
    private final ErrorAttributes errorAttributes;

    public SimpleErrorController(ErrorAttributes errorAttributes) 
    {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    @RequestMapping
    public Map<String, Object> error(HttpServletRequest aRequest) 
    {
        Map<String, Object> body = getErrorAttributes(aRequest);
        String trace = (String) body.get("trace");
        if (trace != null) {
            String[] lines = trace.split("\n\t");
            body.put("trace", lines);
        }
        return body;
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest request) 
    {
        WebRequest webRequest = new ServletWebRequest(request);
        
        ErrorAttributeOptions options = ErrorAttributeOptions
        	    .defaults()
        	    .including(ErrorAttributeOptions.Include.MESSAGE, ErrorAttributeOptions.Include.STACK_TRACE)
        	;
        
        return this.errorAttributes.getErrorAttributes(webRequest, options);
    }
}
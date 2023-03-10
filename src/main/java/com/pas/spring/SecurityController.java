package com.pas.spring;

import java.security.Principal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SecurityController 
{ 	
	@RequestMapping(value = "/username", method = RequestMethod.GET)
    @ResponseBody
    public String getCurrentUserName(Principal principal) 
	{
        return principal.getName();
    }
	
}
package com.pas.spring;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class MyAuthenticationSuccessHandler implements AuthenticationSuccessHandler 
{ 
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
      HttpServletResponse response, Authentication authentication) 
      throws IOException 
    {
        HttpSession session = request.getSession(true);
        if (session != null) 
        {
            session.setAttribute("currentGolfUser", authentication.getName()); //so we can know who this is later.
        }
        
        response.sendRedirect("auth/main.xhtml");
    }
}
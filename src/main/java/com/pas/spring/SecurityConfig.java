package com.pas.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        //securedEnabled = true,
        //jsr250Enabled =true,
        prePostEnabled = true)

public class SecurityConfig
{
	private static Logger logger = LogManager.getLogger(SecurityConfig.class);
 	
	@Bean
    public PasswordEncoder passwordEncoder() 
    {
        return new BCryptPasswordEncoder();
    }

	@Autowired MyAuthenticationSuccessHandler myAuthenticationSuccessHandler;
    @Autowired UserDetailsServiceImpl userDetailsService;
    
    @Bean
    @Order(1)        
    public SecurityFilterChain customFilterChain(HttpSecurity http, UserDetailsService userDetailsService, HandlerMappingIntrospector introspector) throws Exception 
    {
    	logger.info("entering customFilterChain of SecurityConfig");    	
    	
    	MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

    	http.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
        	.requestMatchers(
                mvcMatcherBuilder.pattern("/resources/**"),
                mvcMatcherBuilder.pattern("/jakarta.faces.resource/**"),
                mvcMatcherBuilder.pattern("/register.xhtml"),
                mvcMatcherBuilder.pattern("/index.html"))
            .permitAll().anyRequest().authenticated()
            
        );
    	    	
    	http.formLogin(formLogin -> formLogin            
                .permitAll()
                .successHandler(myAuthenticationSuccessHandler)
                .failureUrl("/logout.xhtml")
            )
    	
    		.logout(logout -> logout.logoutUrl("/logout.xhtml").permitAll());
    	  		
	    logger.info("exiting filterChain of SecurityConfig");
	    
        return http.build();
    }    
       
}

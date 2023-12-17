package com.pas.spring;

import java.security.Permission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        //securedEnabled = true,
        //jsr250Enabled =true,
        prePostEnabled = true)

public class SecurityConfig
{
	private static Logger log = LogManager.getLogger(SecurityConfig.class);

	AuthenticationManager authenticationManager;
   
    @Bean
    public PasswordEncoder passwordEncoder() 
    {
        return new BCryptPasswordEncoder();
    }

    @Autowired
    UserDetailsServiceImpl userDetailsService;
       
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, UserDetailsService userDetailsService, HandlerMappingIntrospector introspector) throws Exception 
    {
    	log.info("entering filterChain of SecurityConfig");    	
    	
    	MvcRequestMatcher.Builder mvcMatcherBuilder = new MvcRequestMatcher.Builder(introspector);

    	http.csrf().disable()
        .authorizeHttpRequests(auth -> auth

            .requestMatchers(
                mvcMatcherBuilder.pattern("/auth/admin/*"),
                mvcMatcherBuilder.pattern("/auth/*")
                ).hasAuthority("ROLE_ADMIN")

            .requestMatchers(
                mvcMatcherBuilder.pattern("/auth/*")
                ).hasAuthority("ROLE_USER")
            
            .requestMatchers(
                mvcMatcherBuilder.pattern("/resources/**"),
                mvcMatcherBuilder.pattern("/jakarta.faces.resource/**"),
                mvcMatcherBuilder.pattern("/register.xhtml"),
                mvcMatcherBuilder.pattern("/login.xhtml"),
                mvcMatcherBuilder.pattern("/index.html"))
            .permitAll().anyRequest().authenticated()
        );
    	
    	http.formLogin()
	    	.loginPage("/login.xhtml")
	    	.defaultSuccessUrl("/auth/main.xhtml")
	    	.successHandler(myAuthenticationSuccessHandler())
	    	.permitAll().failureUrl("/login.xhtml?error=true")
	    	.and()
	        .logout()
	        .logoutSuccessUrl("/login.xhtml")
	        .invalidateHttpSession(true)
	        .deleteCookies("JSESSIONID");
    	
    	//http.httpBasic();
    	
	    log.info("exiting filterChain of SecurityConfig");
	    
        return http.build();
    }
    
    @Bean
	public AuthenticationSuccessHandler myAuthenticationSuccessHandler()
	{
	    return new MyAuthenticationSuccessHandler();
	}

}

package com.pas.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.pas.dao.UsersAndAuthoritiesDAO;

@EnableWebSecurity
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter 
{
	  private static Logger log = LogManager.getLogger(SecurityConfig.class);
	
	  @Autowired
      PasswordEncoder passwordEncoder;
	  @Autowired
      UsersAndAuthoritiesDAO usersAndAuthoritiesDAO;
	  
	  @Override
	  protected void configure(AuthenticationManagerBuilder auth) throws Exception 
	  {
		  auth.userDetailsService(userDetailsService()).passwordEncoder(passwordEncoder());
	  }	  
	  
	  @Bean
	  public UserDetailsService userDetailsService() 
	  {
	      return new UserDetailsServiceImpl(usersAndAuthoritiesDAO);
	  };
	  
	  @Bean
	  public PasswordEncoder passwordEncoder() 
	  {
	     return new BCryptPasswordEncoder();
	  }
	  
	  @Override
	  protected void configure(HttpSecurity http) throws Exception 
	  {
		  log.info("entering SecurityConfig.java for Spring security configuration");
		  
	    //page authorizations		  
	    http.httpBasic()
	    	.and().authorizeRequests().antMatchers("/auth/admin/*").hasRole("ADMIN")
            .and().authorizeRequests().antMatchers("/auth/*").hasAnyRole("ADMIN","USER")
            .and().authorizeRequests().antMatchers("/javax.faces.resource/**", "/register.xhtml", "/login.xhtml", "/index.html").permitAll().anyRequest().authenticated();
	   	   
	    // login	    
	    http.formLogin()
	    	.loginPage("/login.xhtml")
	    	.defaultSuccessUrl("/auth/main.xhtml")
	    	.successHandler(myAuthenticationSuccessHandler())
	    	.permitAll().failureUrl("/login.xhtml?error=true");	    
	   
	    // logout
	    http
	       .logout()
	       .logoutSuccessUrl("/login.xhtml")
	       .invalidateHttpSession(true)
	       .deleteCookies("JSESSIONID");
	    
	    // not needed as JSF 2.2 is implicitly protected against CSRF
	    http.csrf().disable();
	  }
	  
	  @Bean
	  public AuthenticationSuccessHandler myAuthenticationSuccessHandler()
	  {
	      return new MyAuthenticationSuccessHandler();
	  }
	  
	
}

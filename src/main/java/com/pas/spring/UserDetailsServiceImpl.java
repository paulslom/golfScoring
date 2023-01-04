package com.pas.spring;

import org.springframework.security.core.userdetails.User.UserBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pas.beans.GolfUser;
import com.pas.dao.UsersAndAuthoritiesDAO;

@Service
public class UserDetailsServiceImpl implements UserDetailsService 
{
	  private static Logger log = LogManager.getLogger(UserDetailsServiceImpl.class);	
	  
	  private UsersAndAuthoritiesDAO usersAndAuthoritiesDAO;
	  
	  @Autowired
	  public UserDetailsServiceImpl(final UsersAndAuthoritiesDAO usersAndAuthoritiesDAO)
	  {	
		  this.usersAndAuthoritiesDAO = usersAndAuthoritiesDAO;
	  }	
	  
	  @Override
	  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException 
	  {
		  log.info("User " + username + " attempting to log in");
	      GolfUser golfuser = usersAndAuthoritiesDAO.readUserFromDB(username);

	      UserBuilder builder = null;
	   
	     if (golfuser != null && golfuser.getUserName() != null && golfuser.getUserName().trim().length() > 0) 
	     {
	         builder = org.springframework.security.core.userdetails.User.withUsername(username);
	         builder.password(golfuser.getPassword());
	         builder.roles(golfuser.getUserRoles());
	         
	         log.info("User " + username + " successfully found on database.");
	     } 
	     else 
	     {
	    	 log.info("User " + username + " not found on database.");
	         throw new UsernameNotFoundException("User not found.");
	     }

	     return builder.build();
	  }
	  
}
package com.pas.spring;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.userdetails.User.UserBuilder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.pas.beans.GolfUser;
import com.pas.dao.GolfUsersDAO;
import com.pas.dynamodb.DynamoClients;
import com.pas.dynamodb.DynamoUtil;

@Service
public class UserDetailsServiceImpl implements UserDetailsService 
{
	  private static Logger logger = LogManager.getLogger(UserDetailsServiceImpl.class);

	  @Override
	  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
	  {
		  logger.info("User " + username + " attempting to log in");

          DynamoClients dynamoClients = null;

          try
		  {
              dynamoClients = DynamoUtil.getDynamoClients();
			  GolfUsersDAO golfUsersDAO = new GolfUsersDAO(dynamoClients);
			  GolfUser golfuser = golfUsersDAO.getGolfUserFromDB(username);

			  UserBuilder builder = null;

			  if (golfuser != null && golfuser.getUserName() != null && golfuser.getUserName().trim().length() > 0)
			  {
				  builder = org.springframework.security.core.userdetails.User.withUsername(username);
				  builder.password(golfuser.getPassword());
				  builder.roles(golfuser.getUserRole());

				  logger.info("User " + username + " successfully found on database as " + golfuser.getUserName());
			  }
			  else
			  {
				  logger.info("User " + username + " not found on database.");
				  throw new UsernameNotFoundException("User not found.");
			  }

			  return builder.build();
          }
		  catch (Exception e)
		  {
			  logger.error(e.getMessage(),e);
              throw new RuntimeException(e);
          }

	  }
	  
}
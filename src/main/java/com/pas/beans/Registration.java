package com.pas.beans;

import java.io.Serializable;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.pas.util.SAMailUtility;

import jakarta.inject.Inject;

public class Registration implements Serializable
{
	private String firstName;
	private String lastName;
	private String emailAddress;
	private String username;
	private BigDecimal handicap;
	
	private String currentPassword;
	private String newPassword;
	private String confirmNewPassword;
	
	private static ResourceBundle genericProps = ResourceBundle.getBundle("ApplicationProperties");
	private static String NEWLINE = "<br/>";
	
	private static final long serialVersionUID = 1L;

	private static Logger logger = LogManager.getLogger(Registration.class);	
	
	@Inject GolfMain golfmain;

	
	
	public String emailAdminNewPlayer()
	{
		StringBuffer sb = new StringBuffer();
		sb.append("<H3>New Player Request</H3>");		

		sb.append(NEWLINE);
	
		sb.append("First Name: " + this.getFirstName());
		sb.append(NEWLINE);
		sb.append("Last Name: " + this.getLastName());
		sb.append(NEWLINE);
		sb.append("Handicap: " + this.getHandicap());		
		sb.append(NEWLINE);
		sb.append("Requested User Name: " + this.getUsername());		
		sb.append(NEWLINE);
		sb.append("Email Address: " + this.getEmailAddress());		
		
		ArrayList<String> emailRecipients = new ArrayList<String>();
		
		String mailTo = genericProps.getString("mailFrom");  //just use the gmail address for both from and to
		
		emailRecipients.add(mailTo);
		
		logger.info("emailing new player registration for " + this.getFirstName() + " " + this.getLastName() + ".  Email about to be sent to: " + emailRecipients);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");		
		String subjectLine = "New Player request sent to admin on " + sdf.format(new Date());
		SAMailUtility.sendEmail(subjectLine, sb.toString(), emailRecipients, true); //last param means use jsf
		
		return "success";
	}
	
	public String cancel()
	{
		logger.info("cancel clicked on new player registration screen");		
		return "success";
	}	
	
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmailAddress() {
		return emailAddress;
	}
	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public BigDecimal getHandicap() {
		return handicap;
	}
	public void setHandicap(BigDecimal handicap) {
		this.handicap = handicap;
	}

	public String getCurrentPassword() {
		return currentPassword;
	}

	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}

	public String getNewPassword() {
		return newPassword;
	}

	public void setNewPassword(String newPassword) {
		this.newPassword = newPassword;
	}

	public String getConfirmNewPassword() {
		return confirmNewPassword;
	}

	public void setConfirmNewPassword(String confirmNewPassword) {
		this.confirmNewPassword = confirmNewPassword;
	}
}

package com.pas.util;

import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.ResourceBundle;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SAMailUtility 
{
	private static Logger logger = LogManager.getLogger(SAMailUtility.class);
	
	public static final String CLASS_NAME = "com.lfg.util.SAMailUtility";

	public static final String ATTACHMENTS = "sendMailAttachments";

	public static final String SEND_EMAIL_ROOT = "sendEmail";

	public static final String TO_ELEMENT = "to";

	public static final String CC_ELEMENT = "cc";

	public static final String BCC_ELEMENT = "bcc";

	public static final String FROM_ELEMENT = "from";

	public static final String SUBJECT_ELEMENT = "subject";

	public static final String TEXT_ELEMENT = "text";
	
	private static ResourceBundle genericProps = ResourceBundle.getBundle("ApplicationProperties");
	
	URL resUrl = null;
	
	//catch all exceptions here.  Do NOT want this method to break the app.
	
	public static void sendEmail(String mailSubject, String emailMessage, ArrayList<String> ccArrayList, boolean useJSF)
	{
		logger.info("entering sendEmail()");
				
		String mailFrom = genericProps.getString("mailFrom");
		String mailTo = genericProps.getString("mailTo");
	
		StringBuffer mailBody = new StringBuffer();
	
		mailBody.append(emailMessage);
		
		sendTheEmail(mailFrom, mailTo, mailSubject, mailBody.toString(), ccArrayList, useJSF);		

		logger.info("leaving sendEmail()");
	}

	// catch all exceptions here. Do NOT want this method to break the app.

	private static void sendTheEmail(String mailFrom, String mailTo, String mailSubject, String mailBody, ArrayList<String> ccArrayList, boolean useJSF)
	{
		try 
		{
			Properties props = System.getProperties();
			props.put("mail.smtp.host", genericProps.getString("MAIL_SERVER"));
			props.put("mail.smtp.port", genericProps.getString("MAIL_PORT"));
		    props.put("mail.smtp.ssl.enable", "true");
		    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		    props.put("mail.smtp.auth", "true");
		    
		    Session session = Session.getInstance(props, new javax.mail.Authenticator() 
		    {
	            protected PasswordAuthentication getPasswordAuthentication() 
	            {
	            	String mailPW = genericProps.getString("mail_golf_password");
	                return new PasswordAuthentication(mailFrom, mailPW);
	            }

	        });

	        // Used to debug SMTP issues
	        session.setDebug(true);
			MimeMessage message = new MimeMessage(session);			
			
			message.setFrom(new InternetAddress(genericProps.getString("mailFrom")));
			message.setSubject(mailSubject);
			message.setContent(mailBody, "text/html");
			
			message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(mailTo));
			
   			if (ccArrayList != null)
			{
				Address[] addressArray = new Address[ccArrayList.size()];
				for (int i = 0; i < ccArrayList.size(); i++)
				{
					String ccEntry = ccArrayList.get(i);
					InternetAddress iAddr = new InternetAddress(ccEntry);
					addressArray[i] = iAddr;
				}
				message.addRecipients(MimeMessage.RecipientType.BCC, addressArray);
			}

			message.saveChanges();

			Transport.send(message);
			
			String logMessage = "Email successfully sent to " + ccArrayList.size() + " recipients: " + ccArrayList;
			logger.info(logMessage);
			
			if (useJSF)
		    {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, logMessage, null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
		    }
		} 		
		catch (AddressException e)
		{
			logger.error("AddressException encountered in SAMailUtility => ", e);
			if (useJSF)
		    {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email error: " + e.getMessage(),null);
				FacesContext.getCurrentInstance().addMessage(null, msg);	
		    }
		} 
		catch (MessagingException e) 
		{
			logger.error("MessagingException encountered in SAMailUtility => ", e);
			if (useJSF)
		    {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,"Email error: " + e.getMessage(),null);
				FacesContext.getCurrentInstance().addMessage(null, msg);
		    }
		}

	}

	

}
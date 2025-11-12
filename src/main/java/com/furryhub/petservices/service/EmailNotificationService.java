package com.furryhub.petservices.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.furryhub.petservices.model.entity.User;
import com.furryhub.petservices.util.EmailService;

@Service
public class EmailNotificationService {
	
	private static final Logger logger = LoggerFactory.getLogger(EmailNotificationService.class);
	
	@Autowired
	EmailService emailService;
	
	@Value("${app.frontend.base-url}")
    private String frontendBaseUrl;
	
	@Value("${baseUrl}")
	private String baseUrl;
	
	private String registrationMsg=  "Please verify your email by clicking the link below:\n";
	
	private String resetMsg= "Please reset your password by clicking the link below:\n";
	
	private String registrationEndpoint = "/api/auth/verify-email?token=";
	
	public void registerCustomerNotification(User user,String verificationToken)
	{
		String to = user.getEmail();
        String verificationUrl = baseUrl + registrationEndpoint + verificationToken;
        String subject = "Email Verification";
        String text = "Dear " + user.getFirstName() + ",\n\n"
                + registrationMsg
                + verificationUrl + "\n\n"
                + "Thank you,\nFurryHub Team";
		emailService.sendEmail(to, subject, text);
		logger.info("Verification email sent to {}", to);
	}
    
	
	public void registerProviderNotification(User user,String verificationToken)
	{
		String to = user.getEmail();
        String verificationUrl = baseUrl + registrationEndpoint + verificationToken;
        String subject = "Email Verification";
        String text = "Dear " + user.getFirstName() + ",\n\n"
                + registrationMsg
                + verificationUrl + "\n\n"
                + "Thank you,\nFurryHub Team";
		emailService.sendEmail(to, subject, text);
		logger.info("Verification email sent to {}", to);
	}
	
	
    public void resetPasswordNotification(User user,String resetToken)
    {
    	
    	 String to = user.getEmail();
         String resetUrl = frontendBaseUrl + "/reset-password";
         String subject = "Password Reset";
         String text = "Dear " + user.getFirstName() + ",\n\n"
                 + resetMsg
                 + resetUrl + "\n\n"
                 + "Use the token below to reset your password:\n"
                 + "Token: " + resetToken + "\n\n"
                 + "Thank you,\nFurryHub Team";
    	emailService.sendEmail(to, subject, text);
    }
}
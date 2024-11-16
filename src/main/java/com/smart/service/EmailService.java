package com.smart.service;

import java.util.Properties;

import org.springframework.stereotype.Service;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
	
public boolean sendEmail(String message, String subject, String to, String from) {
		
		boolean flag= false;
		
		//get system properties
		Properties properties = System.getProperties();
		properties.put("mail.smtp.auth",true);
		properties.put("mail.smtp.starttls.enable",true);
		properties.put("mail.smtp.port","587");
		properties.put("mail.smtp.host","smtp.gmail.com");
		
        String username="diptidesai05";
        String password="aabwnnxgrfcuaaks";
		
		Session session = Session.getInstance(properties, new Authenticator() {
			
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		session.setDebug(true);
		Message m = new MimeMessage(session);
		try {
			m.setFrom(new InternetAddress(from));
			m.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
			m.setSubject(subject);
			m.setText(message);
			Transport.send(m);
			System.out.println("Message is sent");
			flag= true;
		}catch(Exception e) {
			e.printStackTrace();
		}
		
		return flag;
	}

}

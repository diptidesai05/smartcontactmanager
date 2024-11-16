package com.smart.controller;

import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	//Forgot password Email id Form handler
	@RequestMapping("/forgotPassword")
	public String openForgotPasswordForm() {
		
		return "forgot_password_form";

	}

	//Send OTP
	@PostMapping("/sendOTP")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
			
		System.out.println("Email "+email);
		//Random random = new Random(1000);
		//int otp = random.nextInt(99999);
		Supplier<String> otp = () -> {

			String otp1 = "";

			for (int i = 0; i < 6; i++) {
				otp1 = otp1 + (int) (Math.random() * 10);
			}

			return otp1;

		};
		int otpRandom = Integer.parseInt(otp.get());
		System.out.println("otpRandom "+otpRandom);
		String subject="OTP From SCM application";
		String message=""
				+"<div style='border:1px soild #e2e2e2; padding:20px'>"
				+"<h1>"
				+"OTP is"
				+"<b>"+otpRandom
				+"</br>"
				+"<h1>"
				+"</div>";
		String to=email;
		String from="diptidesai05@gmail.com";
		boolean flag=emailService.sendEmail(message, subject, to, from);
		if(flag)
		{
			session.setAttribute("myotp", otpRandom);
			session.setAttribute("email", email);
			return "verify_otp";
			
		}else {
			session.setAttribute("message", new Message("Your email does not exist", "success"));
			return "forgot_password_form";
		}
		
	}

    //verify OTP
	@PostMapping("/verify_otp")
	public String verifyOTP(@RequestParam("otp") Integer otp, HttpSession session) {
		
		System.out.println("In verifyOTP method");
		Integer myotp= (int)session.getAttribute("myotp");
		System.out.println("otp in session "+myotp);
		System.out.println("otp in parameter "+otp);
		String email=(String)session.getAttribute("email");
		if(myotp.equals(otp)) {
			
			System.out.println("OTP matched");
			User user = userRepository.getUserByUserName(email);
			System.out.println("user "+user);
			if(user==null) {
				
				session.setAttribute("message", new Message("User with this email id does not exist", "warning"));
				return "forgot_password_form";
				
			}else {
				
			}
			return "change_password_form";
			
		}else {
			
			session.setAttribute("message", new Message("wrong OTP", "warning"));
			return "verify_otp";
		}
		
	}
	
	//change to new password handler
    @PostMapping("/changePasswordToNewPassword")
	public String changeToNewPassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
    	
    	System.out.println("Inside changeToNewPassword");
    	String email=(String)session.getAttribute("email");	
    	User user = userRepository.getUserByUserName(email);
    	user.setPassword(bCryptPasswordEncoder.encode(newpassword));
		System.out.println("user "+user);
		userRepository.save(user);
		//session.setAttribute("message", new Message("Your password is changed", "success"));
		return "redirect:/signin?change=password changed successfully";
	}
}



package com.smart.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
//http://localhost:8080/smartcontact
@Controller
public class HomeController {

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	@GetMapping("/test")
	@ResponseBody
	public String test() {

		User user = new User();
		user.setName("Dipti Desai");
		user.setEmail("dipti@gmail.com");
		userRepo.save(user);
		return "working";
	}

	@RequestMapping("/smartcontact")
	public String home(Model model) {

		model.addAttribute("title", "Home-Smart Contact Manager");
		return "home";
	}

	@RequestMapping("/about")
	public String about(Model model) {

		model.addAttribute("title", "About-Smart Contact Manager");
		return "about";
	}

	@RequestMapping("/signup")
	public String signup(Model model) {

		model.addAttribute("title", "Signup-Smart Contact Manager");
		model.addAttribute("user", new User());
		return "signup";
	}

	// handler for registering user
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user, BindingResult result,
			@RequestParam(value = "agreement", defaultValue = "false") boolean agreement, Model model,
			HttpSession session) {

		System.out.println("in register user");
		System.out.println("agreement " + agreement);
		System.out.println("User " + user);

		try {
			if (!agreement) {

				System.out.println("You have not agreed the terms and conditions");
				throw new Exception("You have not agreed the terms and conditions");
			}
			if (result.hasErrors()) {
				System.out.println("Errors " + result.toString());
				model.addAttribute("user", user);
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
			User savedUser = userRepo.save(user);
			model.addAttribute("user", new User());
			session.setAttribute("message", new Message("Successfully Registered", "alert-success"));
		} catch (Exception e) {
			e.printStackTrace();
			model.addAttribute("user", user);
			session.setAttribute("message", new Message("Something went wrong !!" + e.getMessage(), "alert-danger"));
		}

		return "signup";

	}
	// handler for login

	// @GetMapping("/login")// does not works
	@GetMapping("/signin")
	public String customLogin(Model model) {

		System.out.println("in customLogin");
		model.addAttribute("title", "Home-Smart Contact Manager");
		return "login";
	}
}

package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpSession;



@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	UserRepository userRepository;
	
	@Autowired
	ContactRepository contactRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	// method to add common attributes to all responses (to handlers)
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("userName = "+userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user "+user);
		model.addAttribute("user", user);
	}
	
	// method to open user dashboard home
	@RequestMapping("index")
	public String dashboard(Model model) {
		
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
	// method to open Add form 
	@GetMapping("add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
	//processing add contact form
	@PostMapping("process-contact")
	public String processAddContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal, 
			HttpSession session) {
		try {
			String userName = principal.getName();
			System.out.println("userName = " + userName);
			User user = userRepository.getUserByUserName(userName);
			contact.setUser(user);
			System.out.println("user " + user);
			//saving profile image starts
			if(file.isEmpty()) {
				contact.setImage("default_contact.png");
			}else {
				contact.setImage(file.getOriginalFilename());
				File saveFile=new ClassPathResource("static/image").getFile();
				Path filePath=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			//saving profile image ends
			user.getContacts().add(contact);
			userRepository.save(user);
			System.out.println("Data " + contact);
			session.setAttribute("message", new Message("your contact is added, Add more", "success"));
		}catch(Exception e) {
		   System.out.println("Error "+e);
		   e.printStackTrace();
		   session.setAttribute("message", new Message("Something went wrong. Please try again", "danger"));
		}
		return "normal/add_contact_form";
	}
	
	// show contacts pagination
	@GetMapping("/show_user_contacts/{page}")
	public String showContacts(@PathVariable("page")Integer page, Model m, Principal principal){
		System.out.println("in show contacts");
		m.addAttribute("title", "Show User contacts");
		String userName = principal.getName();
		System.out.println("userName = " + userName);
		User user = userRepository.getUserByUserName(userName);
		Pageable pageable= PageRequest.of(page,3);
		Page<Contact> contacts = contactRepository.findContactsByUser(user.getId(), pageable);
		System.out.println(contacts);
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		m.addAttribute("totalpages", contacts.getTotalPages());
		return "normal/show_user_contacts";
		
	}
	
	@GetMapping("/{cId}/contact/")
	public String showContactDetail(@PathVariable("cId")Integer cId, Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("userName = "+userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user "+user);
		System.out.println("cId "+cId);
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("title", "Contact Details");
			model.addAttribute("contact", contact);
			
		}
		return "normal/contactdetail";
	}
	
	@GetMapping("delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId, Model model, Principal principal, HttpSession session) {
		String userName = principal.getName();
		System.out.println("userName = "+userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user "+user);
		System.out.println("cId "+cId);
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();

		if(user.getId()==contact.getUser().getId()) {
			model.addAttribute("title", "Delete Contact");
			model.addAttribute("contact", contact);
			user.getContacts().remove(contact);
			userRepository.save(user);
			
			//TODO - delete profile image associated with contact
			contactRepository.delete(contact);
			session.setAttribute("message", new Message("Contact deleted", "success"));
		}
		return "redirect:/user/show_user_contacts/0";
		
	}
	
	//Open update form handlder
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model) {
		
        System.out.println("updateForm");
        System.out.println("cId "+cId);
		Optional<Contact> contactOptional = contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		model.addAttribute("title", "Update Contact");
		model.addAttribute("contact", contact);
		return "normal/update_form";
		
	}
	
	// process update handler
	@RequestMapping(value="/process-update_contact", method=RequestMethod.POST)
	public String processUpdateForm(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Model model,
			HttpSession session,
			Principal principal
			) {
		System.out.println("In processUpdateForm");
		System.out.println("contact "+contact);
		String userName = principal.getName();
		System.out.println("userName = "+userName);
		User user = userRepository.getUserByUserName(userName);
		System.out.println("user "+user);
		Optional<Contact> contactOptional = contactRepository.findById(contact.getCid());
		Contact oldContact = contactOptional.get(); 
		
		
		  if(!file.isEmpty()) {

				try {

					// delete old photo
					File deleteFileFolderPath = new ClassPathResource("static/image").getFile();
					File fileToDelete = new File(deleteFileFolderPath, oldContact.getImage());
					fileToDelete.delete();
					
					// upload or update new photo
					contact.setImage(file.getOriginalFilename());
					File saveFile = new ClassPathResource("static/image").getFile();
					Path filePath = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());
					Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
					
					System.out.println("Image is uploaded");
				} catch (Exception e) {

				}

			} else {
				contact.setImage(oldContact.getImage());
			}
			contact.setUser(user);
			contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is updated", "success"));
		 
		return "redirect:/user/"+contact.getCid()+"/contact/";
	}
	
	@GetMapping("/user_profile")
	public String yourProfile(Model model) {
		
		model.addAttribute("title", "Your Profile Details");
		return "normal/user_profile";
	}

	//open setting handler
	@GetMapping("/settings")
	public String openSettings() {
		
		System.out.println("openSettings method");
		return "normal/settings";
	}
	
	//change password handler
	@PostMapping("/changePassword")
	public String changePassword(@RequestParam("oldpassword") String oldpassword, 
			                     @RequestParam("newpassword") String newpassword,
			                     Principal principal,
			                     HttpSession session) {
		System.out.println("in changePassword");
		System.out.println("oldpassword "+oldpassword);
		System.out.println("newpassword "+newpassword);
		String userName = principal.getName();
		System.out.println("userName = "+userName);
		User currentUser = userRepository.getUserByUserName(userName);
		System.out.println("currentUser "+currentUser);
		if(bCryptPasswordEncoder.matches(oldpassword, currentUser.getPassword())) {
			currentUser.setPassword(bCryptPasswordEncoder.encode(newpassword));
			userRepository.save(currentUser);
			session.setAttribute("message", new Message("Your password is changed", "success"));
			return "redirect:/user/index";
		}else {
			session.setAttribute("message", new Message("Please correct your old password", "danger"));
			return "redirect:/user/settings";
			
		}
		
	}
	
	
}

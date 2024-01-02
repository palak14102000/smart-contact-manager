package com.smart.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonCreator.Mode;
import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entitites.Contact;
import com.smart.entitites.User;
import com.smart.helper.Message;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private ContactRepository contactRepository;
	//method to adding common data to response
	@ModelAttribute
	public void addCommonData(Model model,Principal principal) {

		String username=principal.getName();
		System.out.println("USERNAME"+username);
		//get the user using username(email)
		User user=userRepository.getUserByUserName(username);
		System.out.println("USER"+user);
		model.addAttribute("user",user);
		
		
	}
	
	//dashboard home
	@RequestMapping("/index")
	public String dashboard(Model model,Principal principal) {
		
		model.addAttribute("title","User Dashboard");
		return "normal/user_dashboard";
	}
	// open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model) {
		
		model.addAttribute("title","Add Contact");
		model.addAttribute("contact",new Contact());
		return "normal/add_contact_form";
	}
	//processing add contact form
	@PostMapping("/process-contact")
	 //@RequestMapping(value = { "/process-contact" }, method = RequestMethod.POST, consumes = {"multipart/form-data"})
    //public String processContact(@ModelAttribute("contact") Contact contact,  @RequestPart("file") MultipartFile file) {
public String processContact(@ModelAttribute Contact contact,@RequestParam("profile-image")MultipartFile file, Principal principal,HttpSession session) {
		 try {
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		//processing and uploading file.
		
		if(file.isEmpty()) {
			// if the file is empty then try our message
			System.out.println("ERROR --- File is Empty");
			contact.setImage("contact.png");
			
		}
		else {
			//upload the file to folder and update the name to contact
			contact.setImage(file.getOriginalFilename());
		
			File saveFile=new ClassPathResource("static/img").getFile();
			Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
		}
		contact.setUser(user);
		user.getContacts().add(contact);
		
		this.userRepository.save(user);
		System.out.println("DATA"+contact);
		System.out.println("Added to data base");
		//message success
		session.setAttribute("message", new Message("Contact is successfully added!! Add more..","success"));
		 }
		 catch(Exception e) {
			 System.out.println("EROOR "+e.getMessage());
			 e.printStackTrace();
			 //message error
			 session.setAttribute("message", new Message("Something went wrong!! Try Again..","danger"));

		 }
		return "normal/add_contact_form";
	}
	
	@GetMapping("/show-contacts/{page}")
	public String showContacts(@PathVariable("page") Integer page ,Model m,Principal principal) {
		
		
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		int id=user.getId();
		
		//PageRequest return object of pageable
		Pageable pages=PageRequest.of(page,8);
		Page<Contact> contacts=this.contactRepository.findContactByUser(id,pages);
		m.addAttribute("title","View User Contacts");
		m.addAttribute("contacts",contacts);
		m.addAttribute("currentPage",page);
		m.addAttribute("totalPages",contacts.getTotalPages());
		
		
		return "normal/showContact";
		
	}
	//showing particular contact details
	@RequestMapping("/{cid}/contact")
	public String showContactDetail(@PathVariable("cid") Integer cid,Model model,Principal principal){
		System.out.println("CID"+cid);
		Optional <Contact> contactoptional=this.contactRepository.findById(cid);
		Contact contact=contactoptional.get();
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		if(contact.getUser().getId()==user.getId())
		model.addAttribute("contact", contact);
		return "normal/contact_detail";
	}
	
	@GetMapping("/delete/{cid}")
	public String delete(@PathVariable("cid") Integer cid,Model model,Principal principal,HttpSession session) {
		
		Optional<Contact> contactoptional=this.contactRepository.findById(cid);
		Contact contact=contactoptional.get();
		String name=principal.getName();
		User user=this.userRepository.getUserByUserName(name);
		
		user.getContacts().remove(contact);
		this.userRepository.save(user);
		
			session.setAttribute("message",new Message("Contact deleted successfully...","success"));
		
		

		
		
		return "redirect:/user/show-contacts/0";
	}
	
	// open update form handl er
	@PostMapping("/open-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid,Model model) {
		
		model.addAttribute("title","Update Contact");
		
		Contact contact=this.contactRepository.findById(cid).get();
		model.addAttribute("contact",contact);
		return "normal/update_form";
	}
	//update contact handler
	@PostMapping("/process-update")
	public String UpdateHandler(@ModelAttribute Contact contact,@RequestParam("profile-image") MultipartFile file,Model model,HttpSession session,Principal principal) {
		try {
			//fetching old contact details
			Contact oldcontatdetails=this.contactRepository.findById(contact.getCid()).get();
			if(!file.isEmpty()) {
				//rewrite file
				//firstly delete old photo
				
				File deleteFile=new ClassPathResource("static/img").getFile();
				File file1=new File(deleteFile,oldcontatdetails.getImage());
				file1.delete();
				
				//then update new photo
				
				File saveFile=new ClassPathResource("static/img").getFile();
				Path path=Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				contact.setImage(file.getOriginalFilename());
			
			}
			else {
				contact.setImage(oldcontatdetails.getImage());
			}
			User user=this.userRepository.getUserByUserName(principal.getName());
			contact.setUser(user);
			this.contactRepository.save(contact);
			session.setAttribute("message", new Message("Your contact is upadted...","success"));
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
		System.out.println("CONTACT NAME"+contact.getName());
		System.out.println("CONTACT ID"+contact.getCid());
		return "redirect:/user/"+contact.getCid()+"/contact";
	}
	
	//showing user profile
	@GetMapping("/profile")
	public String profile(Model model,Principal principal) {
		
		return "normal/profile";
	}
	
}

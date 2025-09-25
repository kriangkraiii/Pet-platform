package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import com.ecom.service.PetService;
import com.ecom.service.UserService;

@RequestMapping("/admin/pet")
public class AdminPetController {
	
	@Autowired
	private PetService petService;
	
	@Autowired
	private UserService userService;
	
	// ADMIN PAGE
	@ModelAttribute("admin")
	public UserDtls getLoggedInAdmin(Principal principal) {
		if (principal != null) {
			return userService.getUserByEmail(principal.getName());
		}
		return null;
	}
	
		// TO PET PAGE
			@GetMapping
			public String adminShowPets(Model model, Principal principal) {
				if (principal == null) {
					return "redirect:/login";
				}

				String email = principal.getName();
				
				UserDtls user = userService.getUserByEmail(email);

				List<Pet> pets = petService.getUserPets(user);
				model.addAttribute("pets", pets);

				if (pets == null || pets.isEmpty()) {
					model.addAttribute("noPetsMessage", "You have no pets added. Please add a pet.");
				}

				return "admin/pet"; // ชื่อไฟล์ HTML ที่จะแสดงผล
			}

}

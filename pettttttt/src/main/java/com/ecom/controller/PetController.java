package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import com.ecom.service.PetService;
import com.ecom.service.UserService;

@Controller
@RequestMapping("/user/pet")
public class PetController {

	@Autowired
	private PetService petService;

	@Autowired
	private UserService userService;

	@ModelAttribute("user")
	public UserDtls getLoggedInUser(Principal principal) {
		if (principal != null) {
			return userService.getUserByEmail(principal.getName());
		}
		return null;
	}

	@GetMapping
	public String showPets(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName();
		UserDtls user = userService.getUserByEmail(email);

		List<Pet> pets = petService.getUserPets(user);
		model.addAttribute("pets", pets);

		if (pets == null || pets.isEmpty()) {
			model.addAttribute("noPetsMsg", "คุณยังไม่มีสัตว์เลี้ยงในระบบ");
		}

		return "pet";
	}

	@PostMapping("/add")
	public String addPet(@RequestParam String name, @RequestParam String type, @RequestParam String breed,
			@RequestParam int age, @RequestParam String color, Principal principal,
			@RequestParam(required = false) String description, @RequestParam String imagePet) {

		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName(); // ดึงอีเมลของผู้ใช้ที่ล็อกอิน
		UserDtls user = userService.getUserByEmail(email); // ดึงข้อมูลผู้ใช้จากฐานข้อมูล

		if (user == null) {
			return "redirect:/login";
		}

		petService.addPet(name, type, breed, age, color, user, description, imagePet);
		return "redirect:/user/pet";
	}
}
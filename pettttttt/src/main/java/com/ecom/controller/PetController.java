package com.ecom.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import com.ecom.service.PetService;
import com.ecom.service.UserService;

import jakarta.servlet.http.HttpSession;

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
			model.addAttribute("noPetsMsg", "You have no pets added yet.");
		}

		return "pet";
	}

	@GetMapping("/add")
	public String showAddPetForm(Model model, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName();
		UserDtls user = userService.getUserByEmail(email);

		if (user == null) {
			return "redirect:/login";
		}

		model.addAttribute("pet", new Pet());
		return "add_pet"; // ชื่อไฟล์ HTML สำหรับฟอร์มเพิ่มสัตว์เลี้ยง
	}
	// Add new pet
	@PostMapping("/add")
	public String addPet(@RequestParam String name, @RequestParam String type, @RequestParam String breed,
			@RequestParam int age, @RequestParam String color, Principal principal,
			@RequestParam(required = false) String description, @RequestParam("imagePet") MultipartFile imageFile,
			HttpSession session) {

		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName();
		UserDtls user = userService.getUserByEmail(email);

		if (user == null) {
			return "redirect:/login";
		}

		String imagePath = "/img/pet_img/default.jpg"; // default image path

		try {
			if (!imageFile.isEmpty()) {
				String originalFilename = imageFile.getOriginalFilename();
				String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

				// ตรวจสอบว่าเป็นไฟล์ภาพ
				if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(fileExtension)) {
					session.setAttribute("errorImagePetMsg", "Only image files are allowed (jpg, jpeg, png, gif, webp)");
					return "redirect:/user/pet";
				}

				String fileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");
				String uploadDir = "src/main/resources/static/img/pet_img/";
				File uploadFolder = new File(uploadDir);
				if (!uploadFolder.exists()) {
					uploadFolder.mkdirs();
				}

				Path filePath = Paths.get(uploadDir + fileName);
				Files.copy(imageFile.getInputStream(), filePath);

				imagePath = "/img/pet_img/" + fileName;
			}

			petService.addPet(name, type, breed, age, color, user, description, imagePath);
			session.setAttribute("succAddPetMsg", "Pet added successfully!");
		} catch (IOException e) {
			e.printStackTrace();
			session.setAttribute("errorAddPetMsg", "Failed to upload image: " + e.getMessage());
		}

		return "redirect:/user/pet";
	}

	// Delete pet
	@PostMapping("/delete/{id}")
	public String deletePet(@PathVariable("id") int petId, HttpSession session, Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName();
		UserDtls user = userService.getUserByEmail(email);

		Pet pet = petService.getPetById(petId);
		if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
			session.setAttribute("errorNotPMsg", "Not found or you don't have permission to delete this pet");
			return "redirect:/user/pet";
		}

		try {
			// ลบไฟล์ภาพถ้าไม่ใช่ภาพเริ่มต้น
			if (pet.getImagePet() != null && !pet.getImagePet().equals("/img/pet_img/default.jpg")) {
				String imagePath = "src/main/resources/static" + pet.getImagePet();
				Files.deleteIfExists(Paths.get(imagePath));
			}

			petService.deletePet(petId);
			session.setAttribute("succDPMsg", "Pet deleted successfully!");
		} catch (IOException e) {
			e.printStackTrace();
			session.setAttribute("errorDPMsg", "Pet deleted failed: " + e.getMessage());
		}

		return "redirect:/user/pet";
	}

	// Edit pet - show form
	@GetMapping("/edit/{id}")
	public String showEditPetForm(@PathVariable("id") int petId, Model model, HttpSession session,
			Principal principal) {
		if (principal == null) {
			return "redirect:/login";
		}

		String email = principal.getName();
		UserDtls user = userService.getUserByEmail(email);

		Pet pet = petService.getPetById(petId);
		if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
			session.setAttribute("errorNoPetMsg", "not found or you don't have permission to edit this pet");
			return "redirect:/user/pet";
		}

		model.addAttribute("pet", pet);
		return "edit_pet"; // ชื่อไฟล์ HTML สำหรับฟอร์มแก้ไข
	}

	// Edit pet - handle form submission
	@PostMapping("/edit/{id}")
	public String updatePet(@RequestParam String name,
	                        @RequestParam String type,
	                        @RequestParam String breed,
	                        @RequestParam int age,
	                        @RequestParam String color,
	                        @RequestParam String description,
	                        @RequestParam("imagePet") MultipartFile imageFile,
	                        @PathVariable("id") int petId,
	                        HttpSession session,
	                        Principal principal) {
	    if (principal == null) {
	        return "redirect:/login";
	    }

	    String email = principal.getName();
	    UserDtls user = userService.getUserByEmail(email);

	    Pet existingPet = petService.getPetById(petId);
	    if (existingPet == null || !existingPet.getOwner().getId().equals(user.getId())) {
	        session.setAttribute("errorNoPetMsg", "Not found or you don't have permission to edit this pet");
	        return "redirect:/user/pet";
	    }

	    try {
	        String imagePath = existingPet.getImagePet();

	        if (!imageFile.isEmpty()) {
	            String originalFilename = imageFile.getOriginalFilename();
	            String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

	            if (!List.of("jpg", "jpeg", "png", "gif", "webp").contains(fileExtension)) {
	                session.setAttribute("errorImagePMsg", "only image files are allowed (jpg, jpeg, png, gif, webp)");
	                return "redirect:/user/pet";
	            }

	            if (imagePath != null && !imagePath.equals("/img/pet_img/default.jpg")) {
	                String oldImagePath = "src/main/resources/static" + imagePath;
	                Files.deleteIfExists(Paths.get(oldImagePath));
	            }

	            String fileName = UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("\\s+", "_");
	            String uploadDir = "src/main/resources/static/img/pet_img/";
	            File uploadFolder = new File(uploadDir);
	            if (!uploadFolder.exists()) {
	                uploadFolder.mkdirs();
	            }

	            Path filePath = Paths.get(uploadDir, fileName);
	            Files.copy(imageFile.getInputStream(), filePath);

	            imagePath = "/img/pet_img/" + fileName;
	        }

	        // อัปเดตข้อมูลสัตว์เลี้ยง
	        existingPet.setName(name);
	        existingPet.setType(type);
	        existingPet.setBreed(breed);
	        existingPet.setAge(age);
	        existingPet.setColor(color);
	        existingPet.setDescription(description);
	        existingPet.setImagePet(imagePath);
	        existingPet.setOwner(user);

	        petService.updatePet(existingPet);
	        session.setAttribute("succUPMsg", "updated successfully!");
	    } catch (IOException e) {
	        e.printStackTrace();
	        session.setAttribute("errorUPMsg", "updated failed " + e.getMessage());
	    }

	    return "redirect:/user/pet";
	}
	
	
}
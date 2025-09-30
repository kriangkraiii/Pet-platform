package com.ecom.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import com.ecom.repository.PetRepository;
import com.ecom.service.PetService;

@Service
public class PetServiceImpl implements PetService {

	@Autowired
	private PetRepository petRepository;

	@Override
	public List<Pet> getUserPets(UserDtls user) {
		return petRepository.findByOwner(user);
	}

	@Override
	public void addPet(String name, String type, String breed, String color, UserDtls owner, String description, String imagePet) {
	    Pet pet = new Pet();
	    pet.setName(name);
	    pet.setType(type);
	    pet.setBreed(breed);
	    pet.setColor(color);
	    pet.setOwner(owner); // บันทึก owner (user ที่ล็อกอิน)
	    pet.setDescription(description);
	    pet.setImagePet(imagePet);
	    petRepository.save(pet);
	}

	@Override
	public void addPet(Pet pet) {
		// TODO Auto-generated method stub

	}

	@Override
	public Pet getPetById(int id) {
		return petRepository.findById(id).orElse(null);
	}

	

	@Override
	public void deletePet(int id) {
	    petRepository.deleteById(id);
	}

	@Override
	public void updatePet(Pet pet) {
		        petRepository.save(pet);
		
	}
	
	@Override
	public List<Pet> getAllPets() {
		return petRepository.findAll();
	}
}
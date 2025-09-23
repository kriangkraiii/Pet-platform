package com.ecom.service;

import com.ecom.model.Pet;
import com.ecom.model.UserDtls;
import java.util.List;

public interface PetService {
	public void addPet(String name, String type, String breed, int age, String color, UserDtls owner,String description,String imagePet);

	public List<Pet> getUserPets(UserDtls user);

	public void addPet(Pet pet);

	public Pet getPetById(int id);

	public void updatePet(Pet pet);

	public void deletePet(int id);
}

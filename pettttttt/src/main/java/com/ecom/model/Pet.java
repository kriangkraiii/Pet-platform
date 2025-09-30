package com.ecom.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Pet {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;
	private String type;
	private String breed;
//	private Integer age;
	private String imagePet;
	private String description;
	private String color;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private UserDtls owner;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getBreed() {
		return breed;
	}

	public void setBreed(String breed) {
		this.breed = breed;
	}

//	public Integer getAge() {
//		return age;
//	}
//
//	public void setAge(Integer age) {
//		this.age = age;
//	}

	public String getImagePet() {
		return imagePet;
	}

	public void setImagePet(String imagePet) {
		this.imagePet = imagePet;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public UserDtls getOwner() {
		return owner;
	}

	public void setOwner(UserDtls owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return "Pet [id=" + id + ", name=" + name + ", type=" + type + ", breed=" + breed 
				+ ", imagePet=" + imagePet + ", description=" + description + ", color=" + color 
				+ ", owner=" + owner + "]";
	}

	public Pet(Integer id, String name, String type, String breed, String imagePet, String description,
			String color, UserDtls owner) {
		super();
		this.id = id;
		this.name = name;
		this.type = type;
		this.breed = breed;
		
		this.imagePet = imagePet;
		this.description = description;
		this.color = color;
		this.owner = owner;
	}

	public Pet() {
		// TODO Auto-generated constructor stub
	}

}

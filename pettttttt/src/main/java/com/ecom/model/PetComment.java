package com.ecom.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pet_comments")
public class PetComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserDtls user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    @ManyToOne
    @JoinColumn(name = "post_id") // foreign key ในตาราง comment
    private CommunityPost post; // <-- เพิ่มตรงนี้
    // getters / setters

    public CommunityPost getPost() {
		return post;
	}

	public void setPost(CommunityPost post) {
		this.post = post;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    public UserDtls getUser() {
        return user;
    }

    public void setUser(UserDtls user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}


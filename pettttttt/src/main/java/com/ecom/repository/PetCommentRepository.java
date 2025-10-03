package com.ecom.repository;

import com.ecom.model.Pet;
import com.ecom.model.PetComment;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetCommentRepository extends JpaRepository<PetComment, Long> {

    // Find all comments for a pet, sorted by creation time (latest first)
    List<PetComment> findByPetOrderByCreatedAtDesc(Pet pet);

    // Count comments related to a specific pet
    long countByPet(Pet pet);

    // Find comments by post ID, sorted by creation time
    @Query("SELECT c FROM PetComment c WHERE c.post.id = :postId ORDER BY c.createdAt DESC")
    List<PetComment> findByPostId(@Param("postId") Long postId);

    // Count comments for a specific post
    @Query("SELECT COUNT(c) FROM PetComment c WHERE c.post.id = :postId")
    long countByPostId(@Param("postId") Long postId);

    // Delete comments by post ID
    @Modifying
    @Query("DELETE FROM PetComment c WHERE c.post.id = :postId")
    void deleteByPostId(@Param("postId") Long postId);

    // Delete comments by pet
    @Modifying
    @Query("DELETE FROM PetComment c WHERE c.pet = :pet")
    void deleteByPet(@Param("pet") Pet pet);
 // Find comment by ID and user ID (for edit permission check)
    @Query("SELECT c FROM PetComment c WHERE c.id = :commentId AND c.user.id = :userId")
    Optional<PetComment> findByIdAndUserId(@Param("commentId") Long commentId, @Param("userId") Integer userId);

}

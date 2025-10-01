package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.PostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PostCommentRepository extends JpaRepository<PostComment, Long> {
	List<PostComment> findByPostOrderByCreatedAtDesc(CommunityPost post);

	long countByPost(CommunityPost post);
}

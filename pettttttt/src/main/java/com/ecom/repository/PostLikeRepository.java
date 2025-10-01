package com.ecom.repository;

import com.ecom.model.CommunityPost;
import com.ecom.model.PostLike;
import com.ecom.model.UserDtls;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByUserAndPost(UserDtls user, CommunityPost post);

    PostLike findByUserAndPost(UserDtls user, CommunityPost post);

    long countByPost(CommunityPost post);

    void deleteByPost(CommunityPost post);

    void deleteByPostId(Long postId);
}

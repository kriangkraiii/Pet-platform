package com.ecom.service.impl;

import com.ecom.model.CommunityPost;
import com.ecom.repository.CommunityPostRepository;
import com.ecom.repository.NotificationRepository;
import com.ecom.repository.PetCommentRepository;
import com.ecom.repository.PostLikeRepository;
import com.ecom.service.CommunityPostService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class CommunityPostServiceImpl implements CommunityPostService {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    @Autowired
    private PetCommentRepository petCommentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    @Transactional
    public boolean deletePostWithDependencies(Long postId) {
        CommunityPost post = communityPostRepository.findById(postId).orElse(null);
        if (post == null) 
            return false;

        // 1) ลบ comments/likes/notifications ที่อิงโพสต์
        petCommentRepository.deleteByPostId(postId);
        postLikeRepository.deleteByPostId(postId);
        notificationRepository.deleteByPostId(postId);

        // 2) ลบไฟล์รูป (ถ้ามี)
        String postImage = post.getPostImage();
        if (postImage != null && !postImage.isBlank()) {
            try {
                Files.deleteIfExists(Paths.get("src/main/resources/static" + postImage));
            } catch (Exception ignored) {}
        }

        // 3) ลบโพสต์
        communityPostRepository.delete(post);
        return true;
    }

}

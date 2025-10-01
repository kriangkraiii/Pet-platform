package com.ecom.controller;

import com.ecom.model.Pet;
import com.ecom.model.PetComment;
import com.ecom.model.PetLike;
import com.ecom.model.PostLike;
import com.ecom.model.UserDtls;
import com.ecom.model.CommunityPost;
import com.ecom.repository.PetCommentRepository;
import com.ecom.repository.PetLikeRepository;
import com.ecom.repository.PostLikeRepository;
import com.ecom.repository.CommunityPostRepository;
import com.ecom.repository.NotificationRepository;
import com.ecom.service.CommunityPostService;
import com.ecom.service.NotificationService;
import com.ecom.service.PetService;
import com.ecom.service.UserService;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.nio.file.StandardOpenOption;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Controller
public class CommunityController {

	@Autowired
	private PetCommentRepository petCommentRepository;

	@Autowired
	private PetService petService;

	@Autowired
	private UserService userService;

	@Autowired
	private PetLikeRepository petLikeRepository;

	@Autowired
	private CommunityPostRepository communityPostRepository;

	@Autowired
	private NotificationService notificationService;

	@Autowired
	private PostLikeRepository postLikeRepository;

	@Autowired
	private NotificationRepository notificationRepository;

	@Autowired
	private CommunityPostService communityPostService;

	@ModelAttribute
	public void addNavbarData(Model model, Principal principal) {
		if (principal != null) {
			UserDtls currentUser = userService.getUserByEmail(principal.getName());
			model.addAttribute("user", currentUser);
		}
		model.addAttribute("countCart", 0);
	}

	@GetMapping("/community")
	public String communityPage(Model model, Principal principal) {
		List<CommunityPost> posts = communityPostRepository.findAllByOrderByCreatedAtDesc();
		UserDtls currentUser = null;

		// ดึงข้อมูลผู้ใช้ปัจจุบัน (ถ้ามีการล็อกอิน)
		if (principal != null) {
			currentUser = userService.getUserByEmail(principal.getName());
			List<Pet> userPets = petService.getPetsByOwner(currentUser);
			model.addAttribute("userPets", userPets);
		}

		// นับจำนวน like และเช็กว่า user ปัจจุบันกด like โพสต์ไหนบ้าง
		for (CommunityPost post : posts) {
			long likeCount = postLikeRepository.countByPost(post);
			boolean likedByCurrentUser = (currentUser != null
					&& postLikeRepository.existsByUserAndPost(currentUser, post));

			post.setLikeCount(likeCount);
			post.setLikedByCurrentUser(likedByCurrentUser);
		}

		model.addAttribute("communityPosts", posts);
		return "community";
	}

	/*
	@PostMapping("/community/create")
	public String createPost(@RequestParam("petId") int petId, @RequestParam("description") String description,
			@RequestParam("postImage") MultipartFile postImage, Principal principal, Model model, HttpSession session) {
		if (principal == null) {
			return "redirect:/signin";
		}

		UserDtls user = userService.getUserByEmail(principal.getName());
		Pet pet = petService.getPetById(petId);

		if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
			session.setAttribute("errorMsg", "Invalid pet selection.");
			return "redirect:/community";
		}

		try {
			String postImagePath = null;

			// Upload post image
			if (!postImage.isEmpty()) {
				String fileName = System.currentTimeMillis() + "_" + postImage.getOriginalFilename();
				String uploadDir = "src/main/resources/static/upload/posts/";
				Files.createDirectories(Paths.get(uploadDir));
				Path path = Paths.get(uploadDir + fileName);
				Files.write(path, postImage.getBytes());
				postImagePath = "/upload/posts/" + fileName;
			}

			// Create new community post
			CommunityPost newPost = new CommunityPost();
			newPost.setUser(user);
			newPost.setPet(pet);
			newPost.setDescription(description);
			newPost.setPostImage(postImagePath);

			communityPostRepository.save(newPost);

			session.setAttribute("succMsg", "Post shared successfully!");
		} catch (IOException e) {
			e.printStackTrace();
			session.setAttribute("errorMsg", "Failed to upload image.");
		}

		return "redirect:/community";
	}
	*/
	
	
	@PostMapping("/community/create")
	public String createPost(
	        @RequestParam("petId") int petId,
	        @RequestParam("description") String description,
	        @RequestParam("postImage") MultipartFile postImage,
	        Principal principal,
	        Model model,
	        HttpSession session) {

	    if (principal == null) {
	        return "redirect:/signin";
	    }

	    UserDtls user = userService.getUserByEmail(principal.getName());
	    Pet pet = petService.getPetById(petId);

	    if (pet == null || !pet.getOwner().getId().equals(user.getId())) {
	        session.setAttribute("errorMsg", "Invalid pet selection.");
	        return "redirect:/community";
	    }

	    try {
	        String postImagePath = null;

	        if (postImage != null && !postImage.isEmpty()) {
	            String fileName = System.currentTimeMillis() + "_" + postImage.getOriginalFilename();

	            // เขียนไฟล์ลง classpath: static/upload/posts
	            File staticDir = new ClassPathResource("static").getFile();
	            File uploadDir = new File(staticDir, "upload" + File.separator + "posts");

	            if (!uploadDir.exists()) {
	                uploadDir.mkdirs();
	            }

	            Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + fileName);
	            Files.copy(postImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	            // path สำหรับแสดงผลในหน้าเว็บ
	            postImagePath = "/upload/posts/" + fileName;
	        }

	        CommunityPost newPost = new CommunityPost();
	        newPost.setUser(user);
	        newPost.setPet(pet);
	        newPost.setDescription(description);
	        newPost.setPostImage(postImagePath);

	        communityPostRepository.save(newPost);

	        session.setAttribute("succMsg", "Post shared successfully!");

	    } catch (IOException e) {
	        e.printStackTrace();
	        session.setAttribute("errorMsg", "Failed to upload image.");
	    }

	    return "redirect:/community";
	}
    
	

	@PostMapping("/community/post/{postId}/like")
	@ResponseBody
	public int likePost(@PathVariable Long postId, Principal principal) {
		// ถ้าไม่ได้ล็อกอิน
		if (principal == null) {
			return 0;
		}

		// ดึงข้อมูลผู้ใช้และโพสต์
		UserDtls user = userService.getUserByEmail(principal.getName());
		CommunityPost post = communityPostRepository.findById(postId).orElse(null);

		if (post == null) {
			return 0;
		}

		// ถ้ายังไม่ได้กด Like
		if (!postLikeRepository.existsByUserAndPost(user, post)) {
			PostLike like = new PostLike(post, user);
			postLikeRepository.save(like);

			// สร้าง Notification สำหรับการกด Like
			notificationService.createLikeNotification(user, post.getPet(), post);
		}

		// ส่งกลับจำนวน Like ล่าสุด
		return (int) postLikeRepository.countByPost(post);
	}

	@PostMapping("/community/post/{postId}/unlike")
	@ResponseBody
	public int unlikePost(@PathVariable Long postId, Principal principal) {
		// ถ้าไม่ได้ล็อกอิน
		if (principal == null) {
			return 0;
		}

		// ดึงข้อมูลผู้ใช้และโพสต์
		UserDtls user = userService.getUserByEmail(principal.getName());
		CommunityPost post = communityPostRepository.findById(postId).orElse(null);

		if (post == null) {
			return 0;
		}

		// ค้นหา Like ที่มีอยู่ และลบออกหากพบ
		PostLike like = postLikeRepository.findByUserAndPost(user, post);
		if (like != null) {
			postLikeRepository.delete(like);
		}

		// ส่งกลับจำนวน Like ล่าสุดหลังจาก Unlike
		return (int) postLikeRepository.countByPost(post);
	}

	@PostMapping("/community/post/{postId}/delete")
	public String deletePost(@PathVariable Long postId, Principal principal, HttpSession session) {
		if (principal == null) {
			session.setAttribute("errorMsg", "You need to login to delete posts.");
			return "redirect:/signin";
		}

		UserDtls user = userService.getUserByEmail(principal.getName());
		CommunityPost post = communityPostRepository.findById(postId).orElse(null);

		if (post == null || !post.getUser().getId().equals(user.getId())) {
			session.setAttribute("errorMsg", "You can only delete your own posts.");
			return "redirect:/community";
		}

		try {
			boolean ok = communityPostService.deletePostWithDependencies(postId);
			if (ok) {
				session.setAttribute("succMsg", "Post deleted successfully.");
			} else {
				session.setAttribute("errorMsg", "Post not found or already deleted.");
			}
		} catch (Exception e) {
			e.printStackTrace();
			session.setAttribute("errorMsg", "Failed to delete post: " + e.getMessage());
		}

		return "redirect:/community";
	}

	@GetMapping("/community/pet/{petId}")
	public String petDetails(@PathVariable int petId, Model model, Principal principal) {

		// ดึงข้อมูลสัตว์เลี้ยงจาก ID
		Pet pet = petService.getPetById(petId);
		if (pet == null) {
			return "redirect:/community";
		}

		// ถ้าต้องใช้ currentUser (เช่น เพื่อเช็กว่าเคยกด like หรือเป็นเจ้าของ)
		UserDtls currentUser = null;
		if (principal != null) {
			currentUser = userService.getUserByEmail(principal.getName());
		}

		// ดึงโพสต์ของสัตว์ตัวนี้ เรียงจากใหม่ไปเก่า
		List<CommunityPost> petPosts = communityPostRepository.findByPetOrderByCreatedAtDesc(pet);

		// ส่งข้อมูลไปยัง View
		model.addAttribute("pet", pet);
		model.addAttribute("owner", pet.getOwner());
		model.addAttribute("petPosts", petPosts);
		model.addAttribute("postsCount", petPosts != null ? petPosts.size() : 0);

		return "community_pet";
	}

	@GetMapping("/community/pet/{petId}/comments")
	public String commentFeed(@PathVariable int petId, Model model, Principal principal) {
		Pet pet = petService.getPetById(petId);
		if (pet == null) {
			return "redirect:/community";
		}

		UserDtls currentUser = null;
		if (principal != null) {
			currentUser = userService.getUserByEmail(principal.getName());
		}

		pet.setLikeCount(petLikeRepository.countByPet(pet));
		pet.setLikedByCurrentUser(currentUser != null && petLikeRepository.existsByUserAndPet(currentUser, pet));

		List<PetComment> comments = petCommentRepository.findByPetOrderByCreatedAtDesc(pet);
		long commentCount = petCommentRepository.countByPet(pet);

		model.addAttribute("pet", pet);
		model.addAttribute("comments", comments);
		model.addAttribute("commentCount", commentCount);

		return "community_comments";
	}

	@PostMapping("/community/pet/{petId}/comments")
	public String addComment(@PathVariable int petId, @RequestParam("content") String content, Principal principal,
			Model model) {
		if (principal == null) {
			return "redirect:/signin";
		}
		content = content == null ? "" : content.trim();
		if (content.isEmpty()) {
			return "redirect:/community/pet/" + petId + "/comments";
		}

		Pet pet = petService.getPetById(petId);
		if (pet == null) {
			return "redirect:/community";
		}

		UserDtls user = userService.getUserByEmail(principal.getName());

		PetComment c = new PetComment();
		c.setPet(pet);
		c.setUser(user);
		c.setContent(content);
		petCommentRepository.save(c);

		return "redirect:/community/pet/" + petId + "/comments#comments";
	}

	@GetMapping("/community/post/{postId}/comments")
	public String postCommentFeed(@PathVariable Long postId, Model model, Principal principal) {
		// ดึงโพสต์จากฐานข้อมูล
		CommunityPost post = communityPostRepository.findById(postId).orElse(null);
		if (post == null) {
			return "redirect:/community";
		}

		// ดึงข้อมูลผู้ใช้ปัจจุบัน (ถ้ามีการล็อกอิน)
		UserDtls currentUser = (principal != null) ? userService.getUserByEmail(principal.getName()) : null;

		// นับจำนวน Like ของโพสต์ และเช็กว่า user ปัจจุบันได้กด Like หรือไม่
		long likeCount = postLikeRepository.countByPost(post);
		boolean likedByCurrentUser = currentUser != null && postLikeRepository.existsByUserAndPost(currentUser, post);

		// ดึงคอมเมนต์ของโพสต์ และนับจำนวนคอมเมนต์
		List<PetComment> comments = petCommentRepository.findByPostId(postId);
		long commentCount = petCommentRepository.countByPostId(postId);

		// ใส่ข้อมูลลงใน model เพื่อส่งไปยัง view
		model.addAttribute("post", post);
		model.addAttribute("pet", post.getPet());
		model.addAttribute("comments", comments);
		model.addAttribute("commentCount", commentCount);
		model.addAttribute("likeCount", likeCount);
		model.addAttribute("likedByCurrentUser", likedByCurrentUser);

		return "community_comments";
	}

	@GetMapping("/community/post/{postId}/edit")
	public String showUpdateForm(@PathVariable Long postId, Model model, Principal principal) {
		CommunityPost post = communityPostRepository.findById(postId)
				.orElseThrow(() -> new RuntimeException("Post not found"));

		UserDtls currentUser = userService.getUserByEmail(principal.getName());
		List<Pet> myPets = petService.getPetsByOwner(currentUser);

		model.addAttribute("post", post);
		model.addAttribute("myPets", myPets);
		return "updatePost"; // ไฟล์ updatePost.html
	}

	/*
	@PostMapping("/community/post/{postId}/update")
	public String updatePost(@PathVariable Long postId, @RequestParam int petId, @RequestParam String description,
			@RequestParam(value = "postImage", required = false) MultipartFile postImage, Principal principal,
			HttpSession session) throws IOException {

		UserDtls currentUser = userService.getUserByEmail(principal.getName());
		CommunityPost post = communityPostRepository.findById(postId)
				.orElseThrow(() -> new RuntimeException("Post not found"));

		if (!post.getUser().getId().equals(currentUser.getId())) {
			session.setAttribute("errorMsg", "You can only update your own posts.");
			return "redirect:/community";
		}

		Pet pet = petService.getPetById(petId);
		if (pet == null || !pet.getOwner().getId().equals(currentUser.getId())) {
			session.setAttribute("errorMsg", "Invalid pet selection.");
			return "redirect:/community";
		}

		post.setPet(pet);
		post.setDescription(description);

		if (postImage != null && !postImage.isEmpty()) {
			String fileName = UUID.randomUUID() + "_" + postImage.getOriginalFilename();
			String uploadDir = "src/main/resources/static/upload/posts/";
			Files.createDirectories(Paths.get(uploadDir));
			Path path = Paths.get(uploadDir + fileName);
			Files.write(path, postImage.getBytes());

			post.setPostImage("/upload/posts/" + fileName);
		}

		communityPostRepository.save(post);
		session.setAttribute("succMsg", "Post updated successfully!");
		return "redirect:/community";
	}*/
	
	@PostMapping("/community/post/{postId}/update")
	public String updatePost(
	        @PathVariable Long postId,
	        @RequestParam int petId,
	        @RequestParam String description,
	        @RequestParam(value = "postImage", required = false) MultipartFile postImage,
	        Principal principal,
	        HttpSession session
	) throws IOException {

	    if (principal == null) {
	        return "redirect:/signin";
	    }

	    UserDtls currentUser = userService.getUserByEmail(principal.getName());

	    CommunityPost post = communityPostRepository.findById(postId)
	            .orElseThrow(() -> new RuntimeException("Post not found"));

	    // ตรวจสอบว่าเจ้าของโพสต์ตรงกับผู้ใช้งานปัจจุบันหรือไม่
	    if (!post.getUser().getId().equals(currentUser.getId())) {
	        session.setAttribute("errorMsg", "You can only update your own posts.");
	        return "redirect:/community";
	    }

	    Pet pet = petService.getPetById(petId);

	    // ตรวจสอบว่า pet มีอยู่และเป็นของผู้ใช้งานปัจจุบันหรือไม่
	    if (pet == null || !pet.getOwner().getId().equals(currentUser.getId())) {
	        session.setAttribute("errorMsg", "Invalid pet selection.");
	        return "redirect:/community";
	    }

	    post.setPet(pet);
	    post.setDescription(description);

	    // อัปเดตรูปภาพโดยเขียนลง classpath: static/upload/posts
	    if (postImage != null && !postImage.isEmpty()) {
	        String fileName = java.util.UUID.randomUUID() + "_" + postImage.getOriginalFilename();

	        File staticDir = new org.springframework.core.io.ClassPathResource("static").getFile();
	        File uploadDir = new File(staticDir, "upload" + File.separator + "posts");

	        if (!uploadDir.exists()) {
	            uploadDir.mkdirs();
	        }

	        Path path = Paths.get(uploadDir.getAbsolutePath() + File.separator + fileName);
	        Files.copy(postImage.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

	        post.setPostImage("/upload/posts/" + fileName);
	    }

	    communityPostRepository.save(post);
	    session.setAttribute("succMsg", "Post updated successfully!");

	    return "redirect:/community";
	}

	
	

	@PostMapping("/community/comment/{commentId}/delete")
	public String deleteComment(@PathVariable Long commentId, Principal principal, HttpSession session) {
		if (principal == null) {
			session.setAttribute("errorMsg", "You need to login to delete comments.");
			return "redirect:/signin";
		}

		UserDtls currentUser = userService.getUserByEmail(principal.getName());
		PetComment comment = petCommentRepository.findById(commentId).orElse(null);

		if (comment == null) {
			session.setAttribute("errorMsg", "Comment not found.");
			return "redirect:/community";
		}

		CommunityPost post = comment.getPost();

		boolean isOwnerComment = comment.getUser().getId().equals(currentUser.getId());
		boolean isOwnerPost = post.getUser().getId().equals(currentUser.getId());

		if (!isOwnerComment && !isOwnerPost) {
			session.setAttribute("errorMsg", "You cannot delete this comment.");
			return "redirect:/community";
		}

		petCommentRepository.delete(comment);
		session.setAttribute("succMsg", "Comment deleted successfully.");
		return "redirect:/community/post/" + post.getId() + "/comments"; // <--- แก้ตรงนี้
	}

	@PostMapping("/community/post/{postId}/comments")
	public String addCommentToPost(@PathVariable Long postId, @RequestParam("content") String content,
			Principal principal, HttpSession session) {
		if (principal == null) {
			return "redirect:/signin";
		}
		content = content == null ? "" : content.trim();
		if (content.isEmpty()) {
			return "redirect:/community/post/" + postId + "/comments";
		}

		CommunityPost post = communityPostRepository.findById(postId).orElse(null);
		if (post == null) {
			return "redirect:/community";
		}

		UserDtls user = userService.getUserByEmail(principal.getName());

		PetComment comment = new PetComment();
		comment.setPost(post);
		comment.setPet(post.getPet());
		comment.setUser(user);
		comment.setContent(content);

		petCommentRepository.save(comment);

		// ✅ สร้าง notification
		notificationService.createCommentNotification(user, post.getPet(), post, content);

		session.setAttribute("succMsg", "Comment added successfully!");
		return "redirect:/community/post/" + postId + "/comments#comments";
	}

}
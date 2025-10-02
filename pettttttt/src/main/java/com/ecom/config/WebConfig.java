package com.ecom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// Admin static resources
		registry.addResourceHandler("/admin/js/**").addResourceLocations("classpath:/static/admin/js/");
		registry.addResourceHandler("/admin/css/**").addResourceLocations("classpath:/static/admin/css/")
				.setCachePeriod(0);

		// General static resources
		registry.addResourceHandler("/css/**").addResourceLocations("classpath:/static/css/");
		registry.addResourceHandler("/js/**").addResourceLocations("classpath:/static/js/");
		registry.addResourceHandler("/img/**").addResourceLocations("classpath:/static/img/").setCachePeriod(3600);
		registry.addResourceHandler("/upload/posts/**").addResourceLocations("classpath:/static/upload/posts/").setCachePeriod(3600);
		
		
		// Pet images from external uploads directory
		String petUploadPath = System.getProperty("user.dir") + "/uploads/pet_img/";
		registry.addResourceHandler("/img/pet_img/**").addResourceLocations("file:" + petUploadPath).setCachePeriod(3600);

		// Profile images - serve from both external uploads and static directory
		String profileUploadPath = System.getProperty("user.dir") + "/uploads/profile_img/";
		registry.addResourceHandler("/img/profile_img/**")
				.addResourceLocations("file:" + profileUploadPath, "classpath:/static/img/profile_img/")
				.setCachePeriod(0);
	
	 // Post images from external uploads directory
	    String postUploadPath = System.getProperty("user.dir") + "/uploads/posts/";
	    registry.addResourceHandler("/uploads/posts/**")
	            .addResourceLocations("file:" + postUploadPath)
	            .setCachePeriod(3600);

	}
}

package com.ecom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/img/profile_img/**")
                .addResourceLocations("classpath:/static/img/profile_img/");
                
                
        registry.addResourceHandler("/admin/js/**")
                .addResourceLocations("classpath:/static/admin/js/")
        		.setCachePeriod(0);
        registry.addResourceHandler("/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/")
                .setCachePeriod(0);
                
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
        
     // ✅ New handler: serve uploaded pet images from outside "src"
        String uploadPath = System.getProperty("user.dir") + "/uploads/pet_img/";
        registry.addResourceHandler("/img/pet_img/**")
                .addResourceLocations("file:" + uploadPath)
                .setCachePeriod(0);
        
        // profile images
        registry.addResourceHandler("/img/profile_img/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/profile_img/")
                .setCachePeriod(0);
    }
}

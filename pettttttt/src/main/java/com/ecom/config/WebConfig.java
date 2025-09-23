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
                .addResourceLocations("classpath:/static/admin/js/");
                
        registry.addResourceHandler("/admin/css/**")
                .addResourceLocations("classpath:/static/admin/css/");
                
        registry.addResourceHandler("/css/**")
                .addResourceLocations("classpath:/static/css/");
                
        registry.addResourceHandler("/js/**")
                .addResourceLocations("classpath:/static/js/");
    }
}

package com.example.petshop.config;

import com.example.petshop.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//            .authorizeHttpRequests(authz -> authz
//                .requestMatchers("/", "/products/**", "/categories/**", 
//                               "/auth/**", "/css/**", "/js/**", "/images/**", 
//                               "/uploads/**", "/static/**").permitAll()
//                .requestMatchers("/admin/**").hasRole("ADMIN")
//                .requestMatchers("/cart/**", "/orders/**").hasAnyRole("USER", "ADMIN")
//                .anyRequest().authenticated()
//            )
//            .formLogin(form -> form
//                .loginPage("/auth/login")
//                .defaultSuccessUrl("/", true)
//                .permitAll()
//            )
//            .logout(logout -> logout
//                .logoutUrl("/auth/logout")
//                .logoutSuccessUrl("/")
//                .permitAll()
//            )
//            .userDetailsService(userDetailsService);
//
//        return http.build();
//    }
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
            .csrf(csrf -> csrf.disable());
        return http.build();
    }
}

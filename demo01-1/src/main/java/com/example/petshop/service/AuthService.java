package com.example.petshop.service;

import com.example.petshop.dto.RegisterDto;
import com.example.petshop.entity.Role;
import com.example.petshop.entity.User;
import com.example.petshop.repository.RoleRepository;
import com.example.petshop.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final MailService mailService;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, 
                      PasswordEncoder passwordEncoder, MailService mailService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailService = mailService;
    }

    @Transactional
    public User register(RegisterDto registerDto) {
        if (userRepository.existsByUsername(registerDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(registerDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("User role not found"));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        mailService.sendWelcomeEmail(savedUser);
        
        return savedUser;
    }
}

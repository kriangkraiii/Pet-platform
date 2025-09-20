package com.example.petshop.controller;

import com.example.petshop.dto.RegisterDto;
import com.example.petshop.entity.Role;
import com.example.petshop.entity.User;
import com.example.petshop.repository.RoleRepository;
import com.example.petshop.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository userRepository, RoleRepository roleRepository, 
                         PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegisterDto registerDto,
                          BindingResult result, Model model,
                          RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        if (userRepository.existsByUsername(registerDto.getUsername())) {
            model.addAttribute("errorMessage", "Username already exists");
            return "register";
        }

        if (userRepository.existsByEmail(registerDto.getEmail())) {
            model.addAttribute("errorMessage", "Email already exists");
            return "register";
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setEmail(registerDto.getEmail());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));

        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("User role not found"));
        user.getRoles().add(userRole);

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/auth/login";
    }
}

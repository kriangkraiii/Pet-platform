package com.example.petshop.config;

import com.example.petshop.entity.Role;
import com.example.petshop.entity.User;
import com.example.petshop.repository.RoleRepository;
import com.example.petshop.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RoleRepository roleRepository, UserRepository userRepository, 
                          PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize roles
        if (roleRepository.findByName(Role.RoleName.ROLE_USER).isEmpty()) {
            Role userRole = new Role();
            userRole.setName(Role.RoleName.ROLE_USER);
            roleRepository.save(userRole);
        }

        if (roleRepository.findByName(Role.RoleName.ROLE_ADMIN).isEmpty()) {
            Role adminRole = new Role();
            adminRole.setName(Role.RoleName.ROLE_ADMIN);
            roleRepository.save(adminRole);
        }

        // Create default admin user
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@petshop.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN).get();
            Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER).get();
            admin.getRoles().add(adminRole);
            admin.getRoles().add(userRole);
            
            userRepository.save(admin);
        }
    }
}

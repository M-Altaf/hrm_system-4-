package com.example.hrm.system.security;

import com.example.hrm.system.entity.Role;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.RoleRepository;
import com.example.hrm.system.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class UserRegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserRegistrationService(UserRepository userRepository,
                                   PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Transactional
    public User registerNewUser(String username, String rawPassword, Long roleId) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }

        // Fetch Role entity from DB
        Role role;
        if (roleId != null) {
            role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));
        } else {
            // Default to EMPLOYEE role if no roleId provided
            role = (Role) roleRepository.findByName("EMPLOYEE")
                    .orElseThrow(() -> new ResourceNotFoundException("Default role 'EMPLOYEE' not found in DB"));
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);                // ← Role entity, not enum
        user.setEnabled(true);
        user.setCreateAt(LocalDateTime.now());

        return userRepository.save(user);
    }
}
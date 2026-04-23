package com.example.hrm.system.serviceImpl;
import com.example.hrm.system.dtos.requestdto.SignupRequestDto;
import com.example.hrm.system.entity.Role;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.RoleRepository;
import com.example.hrm.system.repository.UserRepository;
import com.example.hrm.system.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User registerUser(SignupRequestDto dto) {

        //  duplicate username check
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        //  duplicate email check
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        //  get role from DB
        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found: " + dto.getRole()));
        //  create user
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreateAt(LocalDateTime.now());
        return userRepository.save(user);
    }
}
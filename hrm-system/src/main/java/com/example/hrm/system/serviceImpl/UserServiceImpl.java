package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.SignupRequestDto;
import com.example.hrm.system.dtos.responsedto.UserResponseDto;
import com.example.hrm.system.entity.Role;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.exception.ResourceNotFoundException;
import com.example.hrm.system.repository.RoleRepository;
import com.example.hrm.system.repository.UserRepository;
import com.example.hrm.system.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role not found: " + dto.getRole()));
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(role);
        user.setEnabled(true);
        user.setCreateAt(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserResponseDto(
                        u.getId(),
                        u.getUsername(),
                        u.getEmail(),
                        u.getRole().getName()
                ))
                .collect(Collectors.toList());
    }
}
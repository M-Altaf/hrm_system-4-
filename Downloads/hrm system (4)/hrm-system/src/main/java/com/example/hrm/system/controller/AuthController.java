package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.LoginRequestDto;
import com.example.hrm.system.dtos.requestdto.SignupRequest;
import com.example.hrm.system.dtos.responsedto.LoginResponseDto;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.repository.UserRepository;
import com.example.hrm.system.security.JwtUtil;
import com.example.hrm.system.security.UserRegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRegistrationService userRegistrationService;
    private final UserRepository userRepository;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRegistrationService userRegistrationService,
                          UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRegistrationService = userRegistrationService;
        this.userRepository = userRepository;
    }

    // POST /auth/signup
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        User savedUser = userRegistrationService.registerNewUser(
                request.getUsername(),
                request.getPassword(),
                request.getRoleId()     // ← Long, not enum
        );
        return ResponseEntity.ok("User registered: "
                + savedUser.getUsername()
                + " | Role: " + savedUser.getRole().getName());
    }

    // POST /auth/login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // Fetch role from DB to include in token
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow();

        String token = jwtUtil.generateToken(
                user.getUsername(),
                user.getRole().getName()
        );

        return ResponseEntity.ok(new LoginResponseDto(
                token
        ));
    }
}
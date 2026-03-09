package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.LoginRequestDto;
import com.example.hrm.system.dtos.requestdto.SignupRequest;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.security.UserRegistrationService;  // ← changed
import com.example.hrm.system.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRegistrationService userRegistrationService;  // ← changed

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserRegistrationService userRegistrationService) {  // ← removed PasswordEncoder
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userRegistrationService = userRegistrationService;
    }

    // ---------------- SIGNUP ----------------
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
        try {
            User savedUser = userRegistrationService.registerNewUser(  // ← changed
                    request.getUsername(),
                    request.getPassword()
            );
            return ResponseEntity.ok("User registered successfully: " + savedUser.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // ---------------- LOGIN ----------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtUtil.generateToken(request.getUsername());
        return ResponseEntity.ok(token);
    }
}
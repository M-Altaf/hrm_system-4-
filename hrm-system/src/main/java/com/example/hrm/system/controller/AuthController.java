package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.LoginRequestDto;
import com.example.hrm.system.dtos.requestdto.SignupRequestDto;
import com.example.hrm.system.dtos.responsedto.LoginResponseDto;
import com.example.hrm.system.dtos.responsedto.SignupResponseDto;
import com.example.hrm.system.entity.User;
import com.example.hrm.system.security.CustomUserDetails;
import com.example.hrm.system.security.JwtUtil;
import com.example.hrm.system.services.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtUtil jwtUtil,
                          UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    // 🔐 LOGIN ONLY
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto dto) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        dto.getUsername(),
                        dto.getPassword()
                )
        );

        CustomUserDetails userDetails =
                (CustomUserDetails) authentication.getPrincipal();

        String token = jwtUtil.generateToken(
                userDetails.getUserId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getRole()
        );

        return ResponseEntity.ok(
                new LoginResponseDto(
                        token,
                        String.valueOf(userDetails.getUserId()),
                        userDetails.getUsername(),
                        userDetails.getRole(),
                        userDetails.getEmail()
                )
        );
    }

    // 🆕 SIGNUP
    @PostMapping("/signup")
    public ResponseEntity<SignupResponseDto> signup(
            @Valid @RequestBody SignupRequestDto signupRequest) {

        try {
            User user = userService.registerUser(signupRequest);

            SignupResponseDto response = new SignupResponseDto(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getRole().getName(),
                    "User registered successfully"
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (RuntimeException e) {

            SignupResponseDto error = new SignupResponseDto(

                    null,
                    null,
                    null,
                    null,
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }
}
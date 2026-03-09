package com.example.hrm.system.dtos.requestdto;

import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;

    // getters & setters
}
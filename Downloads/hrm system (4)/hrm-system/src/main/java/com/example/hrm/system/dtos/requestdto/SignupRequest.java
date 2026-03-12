package com.example.hrm.system.dtos.requestdto;

import com.example.hrm.system.entity.Role;
import lombok.Data;

@Data
public class SignupRequest {
    private String username;
    private String password;
    private Long roleId;       // ← send 1, 2, 3, or 4 from Postman
    //   null = defaults to EMPLOYEE
}
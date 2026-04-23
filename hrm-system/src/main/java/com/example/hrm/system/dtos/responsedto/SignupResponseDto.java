package com.example.hrm.system.dtos.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String role;
    private String message;    // Additional field for signup message

}

package com.example.hrm.system.dtos.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotNull
    private String username;

    @NotNull
    private String password;

}

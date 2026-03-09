package com.example.hrm.system.dtos.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthResponseDto {
   private  String token;
}
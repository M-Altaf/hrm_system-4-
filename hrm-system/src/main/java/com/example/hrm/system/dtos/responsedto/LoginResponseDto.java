// ── LoginResponseDto.java ─────────────────────────────────────────
package com.example.hrm.system.dtos.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponseDto {
   private String token;
   private String userId;
   private String username;
   private String role;
   private String email;
}
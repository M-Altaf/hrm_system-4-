// ── LoginResponseDto.java ─────────────────────────────────────────
package com.example.hrm.system.dtos.responsedto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDto {
   private String token;
}

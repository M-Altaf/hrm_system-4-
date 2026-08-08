package com.example.hrm.system.dtos.requestdto;

import jakarta.validation.constraints.NotNull;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceCheckOutDto {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employeeId")
    private Long employeeId;

    @NotNull(message = "Check-out time is required")
    @JsonProperty("checkOut")
    private LocalTime checkOut;

    @JsonProperty("remarks")
    private String remarks; // Optional remarks
}
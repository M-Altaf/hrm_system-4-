package com.example.hrm.system.dtos.requestdto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
// ✅ CORRECT
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AttendanceRequestDto {

    @NotNull(message = "Employee ID is required")
    @JsonProperty("employeeId")
    private Long employeeId;

    @NotNull(message = "Date is required")
    @JsonProperty("date")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    @JsonProperty("checkIn")
    private LocalTime checkIn;

    @JsonProperty("lateReason")  // ✅ ADD THIS
    private String lateReason;

    @JsonProperty("remarks")
    private String remarks;
}
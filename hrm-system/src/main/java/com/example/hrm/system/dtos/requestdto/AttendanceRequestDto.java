package com.example.hrm.system.dtos.requestdto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class AttendanceRequestDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Date is required")
    private LocalDate date;

    @NotNull(message = "Check-in time is required")
    private LocalTime checkIn;

    private String lateReason;
}
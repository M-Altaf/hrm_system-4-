package com.example.hrm.system.dtos.requestdto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AttendanceCheckOutDto {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    @NotNull(message = "Check out time is required")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime checkOut;

}
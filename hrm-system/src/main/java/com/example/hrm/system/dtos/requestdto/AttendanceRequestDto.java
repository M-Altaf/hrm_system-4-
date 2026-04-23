package com.example.hrm.system.dtos.requestdto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
public class AttendanceRequestDto {
    private Long employeeId;
    private LocalDate date;
    private LocalTime checkIn;
    private String lateReason;

    // getters & setters
}
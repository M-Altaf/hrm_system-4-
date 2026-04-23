package com.example.hrm.system.dtos.responsedto;

import com.example.hrm.system.emums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponseDto {

    private Long id;

    private Long employeeId;

    private String employeeName;        // e.g., "John Doe"


    private LocalDate date;              // e.g., "2026-04-21"


    private LocalTime checkIn;           // e.g., "09:05:00"

    private LocalTime checkOut;          // e.g., "17:30:00"

    private Double workingHours;         // e.g., 8.41

    private AttendanceStatus status;     // PRESENT, LATE, ABSENT, etc.

    private String lateReason;           // e.g., "Traffic jam"
}

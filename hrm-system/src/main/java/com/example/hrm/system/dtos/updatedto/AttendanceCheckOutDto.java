package com.example.hrm.system.dtos.updatedto;



import lombok.Data;
import java.time.LocalTime;

@Data
public class AttendanceCheckOutDto {
    private Long employeeId;
    private LocalTime checkOut;
}
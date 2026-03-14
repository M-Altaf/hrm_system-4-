package com.example.hrm.system.dtos.responsedto;




import com.example.hrm.system.emums.AttendanceStatus;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AttendanceResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workingHours;
    private AttendanceStatus status;
    private String lateReason;
}

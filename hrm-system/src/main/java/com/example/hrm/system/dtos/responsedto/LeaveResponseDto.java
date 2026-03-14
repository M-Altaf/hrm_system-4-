package com.example.hrm.system.dtos.responsedto;

import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.emums.LeaveType;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LeaveResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private LeaveType leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;
    private LeaveStatus status;
    private LocalDateTime appliedDate;
    private String rejectionReason;
    private String approvedByName;
}

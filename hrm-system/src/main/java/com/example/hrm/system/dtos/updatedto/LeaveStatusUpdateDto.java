package com.example.hrm.system.dtos.updatedto;


import com.example.hrm.system.emums.LeaveStatus;
import lombok.Data;

@Data
public class LeaveStatusUpdateDto {
    private LeaveStatus status;         // APPROVED or REJECTED
    private String rejectionReason;     // required if REJECTED
    private Long approvedById;          // HR/Manager user id
}
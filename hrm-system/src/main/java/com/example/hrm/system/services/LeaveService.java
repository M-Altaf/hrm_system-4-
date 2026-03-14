package com.example.hrm.system.services;



import com.example.hrm.system.dtos.requestdto.LeaveRequestDto;

import com.example.hrm.system.dtos.responsedto.LeaveResponseDto;
import com.example.hrm.system.dtos.updatedto.LeaveStatusUpdateDto;

import java.util.List;

public interface LeaveService {
    LeaveResponseDto applyLeave(LeaveRequestDto dto);
    LeaveResponseDto updateLeaveStatus(Long leaveId, LeaveStatusUpdateDto dto);
    LeaveResponseDto cancelLeave(Long leaveId, Long employeeId);
    List<LeaveResponseDto> getLeavesByEmployee(Long employeeId);
    List<LeaveResponseDto> getPendingLeaves();
    List<LeaveResponseDto> getAllLeaves();
    LeaveResponseDto getLeaveById(Long id);
}

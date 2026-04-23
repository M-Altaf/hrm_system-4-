package com.example.hrm.system.services;

import com.example.hrm.system.dtos.requestdto.*;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {

    AttendanceResponseDto checkIn(AttendanceRequestDto dto);

    AttendanceResponseDto checkOut(AttendanceCheckOutDto dto);

    AttendanceResponseDto getAttendanceById(Long id);

    List<AttendanceResponseDto> getAttendanceByEmployee(Long employeeId);

    List<AttendanceResponseDto> getAttendanceByDate(LocalDate date);

    List<AttendanceResponseDto> getAttendanceByEmployeeAndDateRange(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    AttendanceReportDto getMonthlyReport(Long employeeId, int month, int year);
}
package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.AttendanceRequestDto;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceCheckOutDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.services.AttendanceService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    // POST /api/attendance/checkin
    @PostMapping("/checkin")
    public ResponseEntity<AttendanceResponseDto> checkIn(
            @RequestBody AttendanceRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(attendanceService.checkIn(dto));
    }

    // PUT /api/attendance/{id}/checkout
    @PutMapping("/{id}/checkout")
    public ResponseEntity<AttendanceResponseDto> checkOut(
            @PathVariable Long id,
            @RequestBody AttendanceCheckOutDto dto) {
        return ResponseEntity.ok(attendanceService.checkOut(id, dto));
    }

    // GET /api/attendance/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> getById(
            @PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    // GET /api/attendance/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponseDto>> getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployee(employeeId));
    }

    // GET /api/attendance/date?date=2026-03-13
    @GetMapping("/date")
    public ResponseEntity<List<AttendanceResponseDto>> getByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    // GET /api/attendance/employee/{employeeId}/range?startDate=2026-03-01&endDate=2026-03-31
    @GetMapping("/employee/{employeeId}/range")
    public ResponseEntity<List<AttendanceResponseDto>> getByDateRange(
            @PathVariable Long employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployeeAndDateRange(
                        employeeId, startDate, endDate));
    }

    // GET /api/attendance/report/{employeeId}?month=3&year=2026
    @GetMapping("/report/{employeeId}")
    public ResponseEntity<AttendanceReportDto> getMonthlyReport(
            @PathVariable Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {
        return ResponseEntity.ok(
                attendanceService.getMonthlyReport(employeeId, month, year));
    }
}

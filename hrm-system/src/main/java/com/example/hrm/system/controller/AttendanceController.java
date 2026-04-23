package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.*;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.services.AttendanceService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/attendance")
public class AttendanceController {

    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    //  CHECK-IN
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponseDto> checkIn(
            @RequestBody AttendanceRequestDto dto) {

        return ResponseEntity.status(201)
                .body(attendanceService.checkIn(dto));
    }

    //  CHECK-OUT
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponseDto> checkOut(
            @RequestBody AttendanceCheckOutDto dto) {

        return ResponseEntity.ok(attendanceService.checkOut(dto));
    }

    //  GET BY ID
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    //  GET BY EMPLOYEE
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponseDto>> getByEmployee(
            @PathVariable Long employeeId) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployee(employeeId));
    }

    //  GET BY DATE
    @GetMapping("/date")
    public ResponseEntity<List<AttendanceResponseDto>> getByDate(
            @RequestParam String date) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByDate(LocalDate.parse(date)));
    }

    //  DATE RANGE
    @GetMapping("/range")
    public ResponseEntity<List<AttendanceResponseDto>> getByRange(
            @RequestParam Long employeeId,
            @RequestParam String start,
            @RequestParam String end) {

        return ResponseEntity.ok(
                attendanceService.getAttendanceByEmployeeAndDateRange(
                        employeeId,
                        LocalDate.parse(start),
                        LocalDate.parse(end)
                )
        );
    }

    //  MONTHLY REPORT
    @GetMapping("/report")
    public ResponseEntity<AttendanceReportDto> getReport(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year) {

        return ResponseEntity.ok(
                attendanceService.getMonthlyReport(employeeId, month, year));
    }
}
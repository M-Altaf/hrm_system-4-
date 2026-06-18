package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.AttendanceCheckOutDto;
import com.example.hrm.system.dtos.requestdto.AttendanceRequestDto;
import com.example.hrm.system.dtos.responsedto.AttendanceResponseDto;
import com.example.hrm.system.dtos.updatedto.AttendanceReportDto;
import com.example.hrm.system.services.AttendanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Validated
public class AttendanceController {

    private final AttendanceService attendanceService;

    // ================= CHECK-IN =================
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponseDto> checkIn(
            @Valid @RequestBody AttendanceRequestDto dto) {

        log.info("Check-in request received for employeeId={}, date={}",
                dto.getEmployeeId(), dto.getDate());

        AttendanceResponseDto response = attendanceService.checkIn(dto);

        log.info("Check-in successful for employeeId={}, attendanceId={}, status={}",
                dto.getEmployeeId(), response.getId(), response.getStatus());

        return ResponseEntity.status(201).body(response);
    }

    // ================= CHECK-OUT =================
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponseDto> checkOut(
            @Valid @RequestBody AttendanceCheckOutDto dto) {

        log.info("Check-out request received for employeeId={}", dto.getEmployeeId());

        AttendanceResponseDto response = attendanceService.checkOut(dto);

        log.info("Check-out successful for employeeId={}, attendanceId={}, workingHours={}",
                dto.getEmployeeId(), response.getId(), response.getWorkingHours());

        return ResponseEntity.ok(response);
    }

    // ================= GET BY ID =================
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> getById(@PathVariable Long id) {
        log.debug("Fetching attendance record by id={}", id);
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    // ================= GET BY EMPLOYEE =================
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponseDto>> getByEmployee(@PathVariable Long employeeId) {
        log.debug("Fetching attendance records for employeeId={}", employeeId);
        List<AttendanceResponseDto> result = attendanceService.getAttendanceByEmployee(employeeId);
        log.debug("Found {} attendance records for employeeId={}", result.size(), employeeId);
        return ResponseEntity.ok(result);
    }

    // ================= GET BY DATE =================
    @GetMapping("/date")
    public ResponseEntity<List<AttendanceResponseDto>> getByDate(
            @RequestParam("date") LocalDate date) {

        log.debug("Fetching attendance records for date={}", date);
        return ResponseEntity.ok(attendanceService.getAttendanceByDate(date));
    }

    // ================= GET BY DATE RANGE =================
    @GetMapping("/range")
    public ResponseEntity<List<AttendanceResponseDto>> getByRange(
            @RequestParam Long employeeId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        log.debug("Fetching attendance records for employeeId={} between {} and {}",
                employeeId, start, end);
        return ResponseEntity.ok(attendanceService.getAttendanceByEmployeeAndDateRange(employeeId, start, end));
    }

    // ================= MONTHLY REPORT =================
    @GetMapping("/report")
    public ResponseEntity<AttendanceReportDto> getMonthlyReport(
            @RequestParam Long employeeId,
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(2000) @Max(2100) int year) {

        log.info("Generating monthly report for employeeId={}, month={}, year={}",
                employeeId, month, year);

        AttendanceReportDto report = attendanceService.getMonthlyReport(employeeId, month, year);

        log.info("Monthly report generated for employeeId={}: presentDays={}, absentDays={}, attendancePercentage={}",
                employeeId, report.getPresentDays(), report.getAbsentDays(), report.getAttendancePercentage());

        return ResponseEntity.ok(report);
    }
}
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
import org.springframework.http.HttpStatus;
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
    /**
     * Check-in endpoint for employee attendance
     *
     * @param dto AttendanceRequestDto containing employeeId, date, checkIn time, and optional lateReason
     * @return ResponseEntity with AttendanceResponseDto (HTTP 201 Created)
     */
    @PostMapping("/check-in")
    public ResponseEntity<AttendanceResponseDto> checkIn(
            @Valid @RequestBody AttendanceRequestDto dto) {

        log.info(">>> CHECK-IN Request received for employeeId={}, date={}, checkInTime={}",
                dto.getEmployeeId(), dto.getDate(), dto.getCheckIn());

        try {
            AttendanceResponseDto response = attendanceService.checkIn(dto);

            log.info("<<< CHECK-IN Successful: attendanceId={}, status={}, employeeId={}",
                    response.getId(), response.getStatus(), response.getEmployeeId());

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            log.warn("<<< CHECK-IN Failed - Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("<<< CHECK-IN Failed - Unexpected Error: ", e);
            throw e;
        }
    }

    // ================= CHECK-OUT =================
    /**
     * Check-out endpoint for employee attendance
     *
     * @param dto AttendanceCheckOutDto containing employeeId and checkOut time
     * @return ResponseEntity with AttendanceResponseDto (HTTP 200 OK)
     */
    @PostMapping("/check-out")
    public ResponseEntity<AttendanceResponseDto> checkOut(
            @Valid @RequestBody AttendanceCheckOutDto dto) {

        log.info(">>> CHECK-OUT Request received for employeeId={}, checkOutTime={}",
                dto.getEmployeeId(), dto.getCheckOut());

        try {
            AttendanceResponseDto response = attendanceService.checkOut(dto);

            log.info("<<< CHECK-OUT Successful: attendanceId={}, workingHours={}, employeeId={}",
                    response.getId(), response.getWorkingHours(), response.getEmployeeId());

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.warn("<<< CHECK-OUT Failed - Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("<<< CHECK-OUT Failed - Unexpected Error: ", e);
            throw e;
        }
    }

    // ================= GET BY ID =================
    /**
     * Retrieve attendance record by ID
     *
     * @param id Attendance record ID
     * @return ResponseEntity with AttendanceResponseDto (HTTP 200 OK)
     */
    @GetMapping("/{id}")
    public ResponseEntity<AttendanceResponseDto> getById(@PathVariable Long id) {
        log.debug(">>> GET BY ID Request: id={}", id);

        if (id == null || id <= 0) {
            log.warn("<<< Invalid ID: {}", id);
            throw new IllegalArgumentException("ID must be a positive number");
        }

        AttendanceResponseDto response = attendanceService.getAttendanceById(id);
        log.debug("<<< GET BY ID Successful: attendanceId={}", id);
        return ResponseEntity.ok(response);
    }

    // ================= GET BY EMPLOYEE =================
    /**
     * Retrieve all attendance records for a specific employee
     *
     * @param employeeId Employee ID
     * @return ResponseEntity with List of AttendanceResponseDto (HTTP 200 OK)
     */
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<AttendanceResponseDto>> getByEmployee(
            @PathVariable Long employeeId) {

        log.info(">>> GET BY EMPLOYEE Request: employeeId={}", employeeId);

        if (employeeId == null || employeeId <= 0) {
            log.warn("<<< Invalid Employee ID: {}", employeeId);
            throw new IllegalArgumentException("Employee ID must be a positive number");
        }

        List<AttendanceResponseDto> result = attendanceService.getAttendanceByEmployee(employeeId);

        log.info("<<< GET BY EMPLOYEE Successful: found {} records for employeeId={}",
                result.size(), employeeId);

        return ResponseEntity.ok(result);
    }

    // ================= GET BY DATE =================
    /**
     * Retrieve all attendance records for a specific date
     *
     * @param date LocalDate
     * @return ResponseEntity with List of AttendanceResponseDto (HTTP 200 OK)
     */
    @GetMapping("/date")
    public ResponseEntity<List<AttendanceResponseDto>> getByDate(
            @RequestParam("date") LocalDate date) {

        log.debug(">>> GET BY DATE Request: date={}", date);

        if (date == null) {
            log.warn("<<< Date parameter is missing");
            throw new IllegalArgumentException("Date parameter is required");
        }

        List<AttendanceResponseDto> result = attendanceService.getAttendanceByDate(date);

        log.debug("<<< GET BY DATE Successful: found {} records for date={}",
                result.size(), date);

        return ResponseEntity.ok(result);
    }

    // ================= GET BY DATE RANGE =================
    /**
     * Retrieve attendance records for an employee within a date range
     *
     * @param employeeId Employee ID
     * @param start Start date (inclusive)
     * @param end End date (inclusive)
     * @return ResponseEntity with List of AttendanceResponseDto (HTTP 200 OK)
     */
    @GetMapping("/range")
    public ResponseEntity<List<AttendanceResponseDto>> getByRange(
            @RequestParam Long employeeId,
            @RequestParam LocalDate start,
            @RequestParam LocalDate end) {

        log.info(">>> GET BY RANGE Request: employeeId={}, start={}, end={}",
                employeeId, start, end);

        // Validate parameters
        if (employeeId == null || employeeId <= 0) {
            log.warn("<<< Invalid Employee ID: {}", employeeId);
            throw new IllegalArgumentException("Employee ID must be a positive number");
        }

        if (start == null) {
            log.warn("<<< Start date is missing");
            throw new IllegalArgumentException("Start date is required");
        }

        if (end == null) {
            log.warn("<<< End date is missing");
            throw new IllegalArgumentException("End date is required");
        }

        if (start.isAfter(end)) {
            log.warn("<<< Start date {} is after end date {}", start, end);
            throw new IllegalArgumentException("Start date cannot be after end date");
        }

        List<AttendanceResponseDto> result = attendanceService
                .getAttendanceByEmployeeAndDateRange(employeeId, start, end);

        log.info("<<< GET BY RANGE Successful: found {} records", result.size());

        return ResponseEntity.ok(result);
    }

    // ================= MONTHLY REPORT =================
    /**
     * Generate monthly attendance report for an employee
     *
     * @param employeeId Employee ID
     * @param month Month (1-12)
     * @param year Year (2000-2100)
     * @return ResponseEntity with AttendanceReportDto (HTTP 200 OK)
     */
    @GetMapping("/report")
    public ResponseEntity<AttendanceReportDto> getMonthlyReport(
            @RequestParam Long employeeId,
            @RequestParam @Min(1) @Max(12) int month,
            @RequestParam @Min(2000) @Max(2100) int year) {

        log.info(">>> GET MONTHLY REPORT Request: employeeId={}, month={}, year={}",
                employeeId, month, year);

        // Validate employeeId
        if (employeeId == null || employeeId <= 0) {
            log.warn("<<< Invalid Employee ID: {}", employeeId);
            throw new IllegalArgumentException("Employee ID must be a positive number");
        }

        try {
            AttendanceReportDto report = attendanceService.getMonthlyReport(employeeId, month, year);

            log.info("<<< GET MONTHLY REPORT Successful: employeeId={}, attendance%={}",
                    employeeId, report.getAttendancePercentage());

            return ResponseEntity.ok(report);
        } catch (IllegalArgumentException e) {
            log.warn("<<< GET MONTHLY REPORT Failed - Validation Error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("<<< GET MONTHLY REPORT Failed - Unexpected Error: ", e);
            throw e;
        }
    }
}
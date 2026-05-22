package com.example.hrm.system.controller;


import com.example.hrm.system.dtos.requestdto.NotificationBulkRequestDto;
import com.example.hrm.system.dtos.requestdto.NotificationRequestDto;
import com.example.hrm.system.dtos.responsedto.NotificationResponseDto;
import com.example.hrm.system.emums.NotificationType;
import com.example.hrm.system.services.NotificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // ── SEND SINGLE NOTIFICATION ──────────────────────────────────────
    // POST /api/notifications/send
    // Body: { "employeeId":1, "title":"Hello", "message":"...", "type":"LEAVE", "sentById":2 }
    @PostMapping("/send")
    public ResponseEntity<NotificationResponseDto> sendNotification(
            @Valid @RequestBody NotificationRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.sendNotification(dto));
    }

    // ── SEND BULK NOTIFICATION ────────────────────────────────────────
    // POST /api/notifications/send-bulk
    // Body: { "title":"...", "message":"...", "type":"GENERAL", "departmentId":1, "sentById":2 }
    // Note: departmentId is optional — if null, sends to ALL employees
    @PostMapping("/send-bulk")
    public ResponseEntity<List<NotificationResponseDto>> sendBulkNotification(
            @Valid @RequestBody NotificationBulkRequestDto dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(notificationService.sendBulkNotification(dto));
    }

    // ── GET ALL NOTIFICATIONS BY EMPLOYEE ─────────────────────────────
    // GET /api/notifications/employee/{employeeId}
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<NotificationResponseDto>> getByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                notificationService.getByEmployee(employeeId));
    }

    // ── GET UNREAD NOTIFICATIONS BY EMPLOYEE ──────────────────────────
    // GET /api/notifications/employee/{employeeId}/unread
    @GetMapping("/employee/{employeeId}/unread")
    public ResponseEntity<List<NotificationResponseDto>> getUnreadByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                notificationService.getUnreadByEmployee(employeeId));
    }

    // ── GET UNREAD COUNT ──────────────────────────────────────────────
    // GET /api/notifications/employee/{employeeId}/unread/count
    @GetMapping("/employee/{employeeId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                notificationService.getUnreadCount(employeeId));
    }

    // ── GET BY EMPLOYEE AND TYPE ──────────────────────────────────────
    // GET /api/notifications/employee/{employeeId}/type?type=LEAVE
    // Available types: LEAVE, PAYROLL, ATTENDANCE, WARNING, GENERAL
    @GetMapping("/employee/{employeeId}/type")
    public ResponseEntity<List<NotificationResponseDto>> getByEmployeeAndType(
            @PathVariable Long employeeId,
            @RequestParam NotificationType type) {
        return ResponseEntity.ok(
                notificationService.getByEmployeeAndType(employeeId, type));
    }

    // ── MARK SINGLE NOTIFICATION AS READ ─────────────────────────────
    // PUT /api/notifications/{id}/read
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable Long id) {
        return ResponseEntity.ok(
                notificationService.markAsRead(id));
    }

    // ── MARK ALL AS READ FOR EMPLOYEE ─────────────────────────────────
    // PUT /api/notifications/employee/{employeeId}/read-all
    @PutMapping("/employee/{employeeId}/read-all")
    public ResponseEntity<List<NotificationResponseDto>> markAllAsRead(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(
                notificationService.markAllAsRead(employeeId));
    }

    // ── DELETE SINGLE NOTIFICATION ────────────────────────────────────
    // DELETE /api/notifications/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long id) {
        notificationService.deleteNotification(id);
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    // ── DELETE ALL NOTIFICATIONS FOR EMPLOYEE ─────────────────────────
    // DELETE /api/notifications/employee/{employeeId}
    @DeleteMapping("/employee/{employeeId}")
    public ResponseEntity<Void> deleteAllByEmployee(
            @PathVariable Long employeeId) {
        notificationService.deleteAllByEmployee(employeeId);
        return ResponseEntity.noContent().build();   // 204 No Content
    }

    // ── SEND LEAVE NOTIFICATION (internal trigger endpoint) ───────────
    // POST /api/notifications/leave?employeeId=1&status=APPROVED&leaveType=ANNUAL
    @PostMapping("/leave")
    public ResponseEntity<Void> sendLeaveNotification(
            @RequestParam Long employeeId,
            @RequestParam String status,
            @RequestParam String leaveType) {
        notificationService.sendLeaveNotification(
                employeeId, status, leaveType);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ── SEND PAYROLL NOTIFICATION (internal trigger endpoint) ─────────
    // POST /api/notifications/payroll?employeeId=1&month=4&year=2026&netSalary=85000
    @PostMapping("/payroll")
    public ResponseEntity<Void> sendPayrollNotification(
            @RequestParam Long employeeId,
            @RequestParam int month,
            @RequestParam int year,
            @RequestParam Double netSalary) {
        notificationService.sendPayrollNotification(
                employeeId, month, year, netSalary);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ── SEND ATTENDANCE WARNING (internal trigger endpoint) ───────────
    // POST /api/notifications/attendance-warning?employeeId=1&lateDays=4
    @PostMapping("/attendance-warning")
    public ResponseEntity<Void> sendAttendanceWarning(
            @RequestParam Long employeeId,
            @RequestParam int lateDays) {
        notificationService.sendAttendanceWarning(employeeId, lateDays);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

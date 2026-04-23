package com.example.hrm.system.controller;



import com.example.hrm.system.dtos.responsedto.AuditLogResponseDto;
import com.example.hrm.system.emums.AuditAction;
import com.example.hrm.system.services.AuditLogService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000")
@RequestMapping("/api/audit-logs")
public class AuditLogController {

    private final AuditLogService auditLogService;

    public AuditLogController(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    // GET /api/audit-logs — all logs latest first
    @GetMapping
    public ResponseEntity<List<AuditLogResponseDto>> getAll() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    // GET /api/audit-logs/user/{username}
    @GetMapping("/user/{username}")
    public ResponseEntity<List<AuditLogResponseDto>> getByUsername(
            @PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getByUsername(username));
    }

    // GET /api/audit-logs/entity/{entityName}
    @GetMapping("/entity/{entityName}")
    public ResponseEntity<List<AuditLogResponseDto>> getByEntity(
            @PathVariable String entityName) {
        return ResponseEntity.ok(
                auditLogService.getByEntityName(entityName));
    }

    // GET /api/audit-logs/entity/{entityName}/{entityId}
    @GetMapping("/entity/{entityName}/{entityId}")
    public ResponseEntity<List<AuditLogResponseDto>> getByEntityAndId(
            @PathVariable String entityName,
            @PathVariable Long entityId) {
        return ResponseEntity.ok(
                auditLogService.getByEntityNameAndId(entityName, entityId));
    }

    // GET /api/audit-logs/action/{action}
    @GetMapping("/action/{action}")
    public ResponseEntity<List<AuditLogResponseDto>> getByAction(
            @PathVariable AuditAction action) {
        return ResponseEntity.ok(auditLogService.getByAction(action));
    }

    // GET /api/audit-logs/range?start=2026-03-01T00:00:00&end=2026-03-31T23:59:59
    @GetMapping("/range")
    public ResponseEntity<List<AuditLogResponseDto>> getByDateRange(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {
        return ResponseEntity.ok(
                auditLogService.getByDateRange(start, end));
    }

    // GET /api/audit-logs/user/{username}/range?start=...&end=...
    @GetMapping("/user/{username}/range")
    public ResponseEntity<List<AuditLogResponseDto>> getByUsernameAndRange(
            @PathVariable String username,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime start,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime end) {
        return ResponseEntity.ok(
                auditLogService.getByUsernameAndDateRange(
                        username, start, end));
    }
}

package com.example.hrm.system.controller;


import com.example.hrm.system.dtos.requestdto.LeaveRequestDto;
import com.example.hrm.system.dtos.responsedto.LeaveResponseDto;
import com.example.hrm.system.dtos.updatedto.LeaveStatusUpdateDto;
import com.example.hrm.system.services.LeaveService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/leaves")
public class LeaveController {

    private final LeaveService leaveService;

    public LeaveController(LeaveService leaveService) {
        this.leaveService = leaveService;
    }

    // POST /api/leaves — apply for leave
    @PostMapping("/apply")
    public ResponseEntity<LeaveResponseDto> applyLeave(
            @RequestBody LeaveRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(leaveService.applyLeave(dto));
    }

    // GET /api/leaves — all leaves (ADMIN/HR)
    @GetMapping("/getAll")
    public ResponseEntity<List<LeaveResponseDto>> getAllLeaves() {
        return ResponseEntity.ok(leaveService.getAllLeaves());
    }

    // GET /api/leaves/pending — pending leaves (ADMIN/HR)
    @GetMapping("/pending")
    public ResponseEntity<List<LeaveResponseDto>> getPendingLeaves() {
        return ResponseEntity.ok(leaveService.getPendingLeaves());
    }

    // GET /api/leaves/{id} — single leave
    @GetMapping("/{id}")
    public ResponseEntity<LeaveResponseDto> getLeaveById(
            @PathVariable Long id) {
        return ResponseEntity.ok(leaveService.getLeaveById(id));
    }

    // GET /api/leaves/employee/{employeeId} — leaves by employee
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<LeaveResponseDto>> getLeavesByEmployee(
            @PathVariable Long employeeId) {
        return ResponseEntity.ok(leaveService.getLeavesByEmployee(employeeId));
    }

    // PUT /api/leaves/{id}/status — approve or reject (ADMIN/HR/MANAGER)
    @PutMapping("/{id}/status")
    public ResponseEntity<LeaveResponseDto> updateLeaveStatus(
            @PathVariable Long id,
            @RequestBody LeaveStatusUpdateDto dto) {
        return ResponseEntity.ok(leaveService.updateLeaveStatus(id, dto));
    }

    // PUT /api/leaves/{id}/cancel — cancel leave (EMPLOYEE)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<LeaveResponseDto> cancelLeave(
            @PathVariable Long id,
            @RequestParam Long employeeId) {
        return ResponseEntity.ok(leaveService.cancelLeave(id, employeeId));
    }
}
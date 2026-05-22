package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ── CREATE ───────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @Valid @RequestBody EmployeeRequestDto dto) {

        log.info("Creating new employee with email: {}", dto.getEmail());

        EmployeeResponseDto created = employeeService.createEmployee(dto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header("Location", "/api/v1/employees/" + created.getId())
                .body(created);
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    @GetMapping
    public ResponseEntity<List<EmployeeResponseDto>> getAllEmployees() {

        log.info("Fetching all employees");

        return ResponseEntity.ok(employeeService.getAllEmployees());
    }

    // ── GET BY ID ────────────────────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(
            @PathVariable Long id) {

        log.info("Fetching employee with id: {}", id);

        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    // ── FULL UPDATE (PUT) ────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto dto) {

        log.info("Updating employee with id: {}", id);

        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    // ── PARTIAL UPDATE (PATCH) ───────────────────────────────────────

    @PatchMapping("/{id}")
    public ResponseEntity<EmployeeResponseDto> patchEmployee(
            @PathVariable Long id,
            @RequestBody EmployeePatchDto dto) {

        log.info("Patching employee with id: {}", id);

        return ResponseEntity.ok(employeeService.patchEmployee(id, dto));
    }

    // ── DELETE ───────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(
            @PathVariable Long id) {

        log.info("Deleting employee with id: {}", id);

        employeeService.deleteEmployee(id);

        return ResponseEntity.noContent().build();
    }
}
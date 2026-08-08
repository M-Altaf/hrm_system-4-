package com.example.hrm.system.controller;

import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.services.EmployeeService;
import io.jsonwebtoken.io.IOException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // ── CREATE ───────────────────────────────────────────────────────

    @PostMapping("/create_emp")
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

    @GetMapping("/getall")
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
    @PostMapping("/{id}/profile-picture")
    public ResponseEntity<Void> uploadProfilePicture(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException, java.io.IOException {

        log.info("Profile picture upload request for employee id: {}", id);
        employeeService.uploadProfilePicture(id, file.getBytes(), file.getContentType());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/profile-picture")
    public ResponseEntity<byte[]> getProfilePicture(@PathVariable Long id) {
        Employee employee = employeeService.getEmployeeWithPicture(id);

        if (employee.getProfilePicture() == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(employee.getProfilePictureType()))
                .body(employee.getProfilePicture());
    }

    @DeleteMapping("/{id}/profile-picture")
    public ResponseEntity<Void> deleteProfilePicture(@PathVariable Long id) {
        log.info("Profile picture delete request for employee id: {}", id);
        employeeService.deleteProfilePicture(id);
        return ResponseEntity.noContent().build();
    }
}
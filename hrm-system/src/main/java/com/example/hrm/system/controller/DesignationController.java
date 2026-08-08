package com.example.hrm.system.controller;



import com.example.hrm.system.dtos.requestdto.DesignationRequestDto;
import com.example.hrm.system.dtos.responsedto.DesignationResponseDto;
import com.example.hrm.system.serviceImpl.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    @GetMapping
    public ResponseEntity<List<DesignationResponseDto>> getAllDesignations() {
        log.info("Fetching all designations");
        return ResponseEntity.ok(designationService.getAllDesignations());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DesignationResponseDto> getDesignationById(@PathVariable Long id) {
        return ResponseEntity.ok(designationService.getDesignationById(id));
    }

    @PostMapping
    public ResponseEntity<DesignationResponseDto> createDesignation(
            @Valid @RequestBody DesignationRequestDto dto) {
        log.info("Creating designation: {}", dto.getTitle());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(designationService.createDesignation(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DesignationResponseDto> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequestDto dto) {
        return ResponseEntity.ok(designationService.updateDesignation(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.noContent().build();
    }
}
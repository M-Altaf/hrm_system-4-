package com.example.hrm.system.serviceImpl;


import com.example.hrm.system.dtos.requestdto.DepartmentRequestDto;
import com.example.hrm.system.dtos.responsedto.DepartmentResponseDto;
import com.example.hrm.system.entity.Department;
import com.example.hrm.system.repository.DepartmentRepository;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;

    // ── GET ALL ──────────────────────────────────────────────────────
    public List<DepartmentResponseDto> getAllDepartments() {
        return departmentRepository.findAll()
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ────────────────────────────────────────────────────
    public DepartmentResponseDto getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        return toDto(dept);
    }

    // ── CREATE ───────────────────────────────────────────────────────
    public DepartmentResponseDto createDepartment(DepartmentRequestDto dto) {
        if (departmentRepository.existsByName(dto.getName())) {
            throw new RuntimeException("Department with name '" + dto.getName() + "' already exists");
        }
        Department dept = new Department();
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        return toDto(departmentRepository.save(dept));
    }

    // ── UPDATE ───────────────────────────────────────────────────────
    public DepartmentResponseDto updateDepartment(Long id, DepartmentRequestDto dto) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + id));
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        return toDto(departmentRepository.save(dept));
    }

    // ── DELETE ───────────────────────────────────────────────────────
    public void deleteDepartment(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("Department not found with id: " + id);
        }
        departmentRepository.deleteById(id);
    }

    // ── MAPPER ───────────────────────────────────────────────────────
    private DepartmentResponseDto toDto(Department dept) {
        DepartmentResponseDto dto = new DepartmentResponseDto();
        dto.setId(dept.getId());
        dto.setName(dept.getName());
        dto.setDescription(dept.getDescription());
        return dto;
    }
}
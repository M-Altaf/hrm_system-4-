package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.DesignationRequestDto;
import com.example.hrm.system.dtos.responsedto.DesignationResponseDto;
import com.example.hrm.system.entity.Department;
import com.example.hrm.system.entity.Designation;
import com.example.hrm.system.repository.DepartmentRepository;
import com.example.hrm.system.repository.DesignationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DesignationService {

    private final DesignationRepository designationRepository;
    private final DepartmentRepository departmentRepository;

    @Transactional(readOnly = true)
    public List<DesignationResponseDto> getAllDesignations() {
        return designationRepository.findAll()
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DesignationResponseDto getDesignationById(Long id) {
        Designation desig = designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));
        return toDto(desig);
    }

    @Transactional
    public DesignationResponseDto createDesignation(DesignationRequestDto dto) {
        if (designationRepository.existsByTitle(dto.getTitle())) {
            throw new RuntimeException("Designation '" + dto.getTitle() + "' already exists");
        }

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("Department not found with id: " + dto.getDepartmentId()));

        Designation design = new Designation();
        design.setTitle(dto.getTitle());
        design.setBaseSalary(dto.getBaseSalary());
        design.setDepartment(department);
        return toDto(designationRepository.save(design));
    }

    @Transactional
    public DesignationResponseDto updateDesignation(Long id, DesignationRequestDto dto) {
        Designation desig = designationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Designation not found with id: " + id));

        desig.setTitle(dto.getTitle());
        desig.setBaseSalary(dto.getBaseSalary());

        if (dto.getDepartmentId() != null) {
            Department department = departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new RuntimeException("Department not found with id: " + dto.getDepartmentId()));
            desig.setDepartment(department);
        }

        return toDto(designationRepository.save(desig));
    }

    @Transactional
    public void deleteDesignation(Long id) {
        if (!designationRepository.existsById(id)) {
            throw new RuntimeException("Designation not found with id: " + id);
        }
        designationRepository.deleteById(id);
    }

    private DesignationResponseDto toDto(Designation desig) {
        DesignationResponseDto dto = new DesignationResponseDto();
        dto.setId(desig.getId());
        dto.setTitle(desig.getTitle());
        dto.setBaseSalary(desig.getBaseSalary());
        if (desig.getDepartment() != null) {
            dto.setDepartmentId(desig.getDepartment().getId());
            dto.setDepartmentName(desig.getDepartment().getName());
        }
        return dto;
    }
}
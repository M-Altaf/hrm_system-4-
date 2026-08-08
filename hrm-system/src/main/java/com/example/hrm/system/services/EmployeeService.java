package com.example.hrm.system.services;
import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.entity.Employee;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto dto);
    List<EmployeeResponseDto> getAllEmployees();
    EmployeeResponseDto getEmployeeById(Long id);
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto);
    EmployeeResponseDto patchEmployee(Long id, EmployeePatchDto dto);  // ← new
    void deleteEmployee(Long id);

    @Transactional
    void uploadProfilePicture(Long id, byte[] imageBytes, String contentType);

    @Transactional(readOnly = true)
    Employee getEmployeeWithPicture(Long id);

    @Transactional
    void deleteProfilePicture(Long id);
}
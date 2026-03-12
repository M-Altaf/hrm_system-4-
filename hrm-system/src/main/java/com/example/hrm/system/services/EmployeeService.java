package com.example.hrm.system.services;
import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;

import java.util.List;

public interface EmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto dto);
    List<EmployeeResponseDto> getAllEmployees();
    EmployeeResponseDto getEmployeeById(Long id);
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto);
    EmployeeResponseDto patchEmployee(Long id, EmployeePatchDto dto);  // ← new
    void deleteEmployee(Long id);
}
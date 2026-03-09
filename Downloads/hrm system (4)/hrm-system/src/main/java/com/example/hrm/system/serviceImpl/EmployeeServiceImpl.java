package com.example.hrm.system.serviceImpl;


import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.repository.EmployeeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl {

    private final EmployeeRepository employeeRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    // CREATE
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        Employee employee = mapToEntity(dto);
        return mapToDto(employeeRepository.save(employee));
    }

    // GET ALL
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // GET BY ID
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    // UPDATE
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Employee not found with id: " + id));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setPosition(dto.getPosition());
        employee.setSalary(dto.getSalary());

        return mapToDto(employeeRepository.save(employee));
    }

    // DELETE
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new RuntimeException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    // ---- mappers ----
    private Employee mapToEntity(EmployeeRequestDto dto) {
        Employee e = new Employee();
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmail(dto.getEmail());
        e.setDepartment(dto.getDepartment());
        e.setPosition(dto.getPosition());
        e.setSalary(dto.getSalary());
        return e;
    }

    private EmployeeResponseDto mapToDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(e.getId());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setDepartment(e.getDepartment());
        dto.setPosition(e.getPosition());
        dto.setSalary(e.getSalary());
        return dto;
    }
}

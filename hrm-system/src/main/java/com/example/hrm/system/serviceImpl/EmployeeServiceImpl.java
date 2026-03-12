package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.exeption.ResourceNotFoundException;
import com.example.hrm.system.repository.*;
import com.example.hrm.system.services.EmployeeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository,
                               DepartmentRepository departmentRepository,
                               DesignationRepository designationRepository,
                               CategoryRepository categoryRepository,
                               UserRepository userRepository) {
        this.employeeRepository = employeeRepository;
        this.departmentRepository = departmentRepository;
        this.designationRepository = designationRepository;
        this.categoryRepository = categoryRepository;
        this.userRepository = userRepository;
    }

    // ── CREATE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }
        return mapToDto(employeeRepository.save(mapToEntity(dto)));
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    // ── UPDATE (PUT) ─────────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());

        // Resolve and set FK entities
        employee.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId())));

        employee.setDesignation(designationRepository.findById(dto.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + dto.getDesignationId())));

        employee.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId())));

        employee.setUser(userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUserId())));

        return mapToDto(employeeRepository.save(employee));
    }

    // ── PATCH (partial update) ───────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto patchEmployee(Long id, EmployeePatchDto dto) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Only update fields that are NOT null
        if (dto.getFirstName() != null)  employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)   employee.setLastName(dto.getLastName());
        if (dto.getPhone() != null)      employee.setPhone(dto.getPhone());
        if (dto.getHireDate() != null)   employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null)     employee.setStatus(dto.getStatus());

        if (dto.getEmail() != null) {
            if (employeeRepository.existsByEmail(dto.getEmail())) {
                throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
            }
            employee.setEmail(dto.getEmail());
        }

        if (dto.getDepartmentId() != null) {
            employee.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId())));
        }

        if (dto.getDesignationId() != null) {
            employee.setDesignation(designationRepository.findById(dto.getDesignationId())
                    .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + dto.getDesignationId())));
        }

        if (dto.getCategoryId() != null) {
            employee.setCategory(categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId())));
        }

        if (dto.getUserId() != null) {
            employee.setUser(userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUserId())));
        }

        return mapToDto(employeeRepository.save(employee));
    }

    // ── DELETE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    // ── MAPPERS ──────────────────────────────────────────────────────

    private Employee mapToEntity(EmployeeRequestDto dto) {
        Employee e = new Employee();
        e.setFirstName(dto.getFirstName());
        e.setLastName(dto.getLastName());
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setHireDate(dto.getHireDate());
        e.setStatus(dto.getStatus());

        e.setDepartment(departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found: " + dto.getDepartmentId())));

        e.setDesignation(designationRepository.findById(dto.getDesignationId())
                .orElseThrow(() -> new ResourceNotFoundException("Designation not found: " + dto.getDesignationId())));

        e.setCategory(categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found: " + dto.getCategoryId())));

        e.setUser(userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + dto.getUserId())));

        return e;
    }

    private EmployeeResponseDto mapToDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(e.getId());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus());

        dto.setDepartmentId(e.getDepartment().getId());
        dto.setDepartmentName(e.getDepartment().getName());

        dto.setDesignationId(e.getDesignation().getId());
        dto.setDesignationTitle(e.getDesignation().getTitle());

        dto.setCategoryId(e.getCategory().getId());
        dto.setCategoryName(e.getCategory().getName());

        dto.setUserId(e.getUser().getId());
        dto.setUsername(e.getUser().getUsername());

        return dto;
    }
}
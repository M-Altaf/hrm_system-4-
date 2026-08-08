package com.example.hrm.system.serviceImpl;

import com.example.hrm.system.dtos.requestdto.EmployeePatchDto;
import com.example.hrm.system.dtos.requestdto.EmployeeRequestDto;
import com.example.hrm.system.dtos.responsedto.EmployeeResponseDto;
import com.example.hrm.system.emums.EmployeeStatus;
import com.example.hrm.system.entity.Employee;
import com.example.hrm.system.exception.ResourceNotFoundException;
import com.example.hrm.system.repository.*;
import com.example.hrm.system.services.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final DepartmentRepository departmentRepository;
    private final DesignationRepository designationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    // ── CREATE ───────────────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto dto) {
        log.info("Creating employee with email: {}", dto.getEmail());

        if (employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        Employee employee = mapToEntity(dto);
        return mapToDto(employeeRepository.save(employee));
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {
        log.info("Fetching all employees");
        return employeeRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ID ────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(Long id) {
        log.info("Fetching employee with id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        return mapToDto(employee);
    }

    // ── FULL UPDATE (PUT) ────────────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto dto) {
        log.info("Updating employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        // Check email conflict only if email is being changed
        if (!employee.getEmail().equals(dto.getEmail()) &&
                employeeRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use: " + dto.getEmail());
        }

        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setPhone(dto.getPhone());
        employee.setHireDate(dto.getHireDate());
        employee.setStatus(dto.getStatus());

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

    // ── PARTIAL UPDATE (PATCH) ───────────────────────────────────────

    @Override
    @Transactional
    public EmployeeResponseDto patchEmployee(Long id, EmployeePatchDto dto) {
        log.info("Patching employee with id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (dto.getFirstName() != null)  employee.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null)   employee.setLastName(dto.getLastName());
        if (dto.getPhone() != null)      employee.setPhone(dto.getPhone());
        if (dto.getHireDate() != null)   employee.setHireDate(dto.getHireDate());
        if (dto.getStatus() != null)     employee.setStatus(dto.getStatus());

        if (dto.getEmail() != null) {
            if (!employee.getEmail().equals(dto.getEmail()) &&
                    employeeRepository.existsByEmail(dto.getEmail())) {
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
        log.info("Deleting employee with id: {}", id);
        if (!employeeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Employee not found with id: " + id);
        }
        employeeRepository.deleteById(id);
    }

    @Transactional
    @Override
    public void uploadProfilePicture(Long id, byte[] imageBytes, String contentType) {
        log.info("Uploading profile picture for employee id: {}", id);

        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));

        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }
        if (imageBytes.length > 2 * 1024 * 1024) {
            throw new IllegalArgumentException("Profile picture must be under 2MB");
        }
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image (jpg, png, etc.)");
        }

        employee.setProfilePicture(imageBytes);
        employee.setProfilePictureType(contentType);
        employeeRepository.save(employee);

        log.info("Profile picture uploaded successfully for employee id: {}", id);
    }

// ── PROFILE PICTURE: FETCH RAW BYTES ─────────────────────────────

    @Transactional(readOnly = true)
    @Override
    public Employee getEmployeeWithPicture(Long id) {
        log.debug("Fetching profile picture for employee id: {}", id);
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
    }

// ── PROFILE PICTURE: DELETE ──────────────────────────────────────

    @Transactional
    @Override
    public void deleteProfilePicture(Long id) {
        log.info("Deleting profile picture for employee id: {}", id);
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id: " + id));
        employee.setProfilePicture(null);
        employee.setProfilePictureType(null);
        employeeRepository.save(employee);
    }

    // ── MAPPER: DTO → ENTITY ─────────────────────────────────────────

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

    // ── MAPPER: ENTITY → DTO ─────────────────────────────────────────
    private EmployeeResponseDto mapToDto(Employee e) {
        EmployeeResponseDto dto = new EmployeeResponseDto();
        dto.setId(e.getId());
        dto.setFirstName(e.getFirstName());
        dto.setLastName(e.getLastName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setHireDate(e.getHireDate());
        dto.setStatus(e.getStatus() != null ? EmployeeStatus.valueOf(e.getStatus().name()) : null);
        dto.setHasProfilePicture(e.getProfilePicture() != null && e.getProfilePicture().length > 0);

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
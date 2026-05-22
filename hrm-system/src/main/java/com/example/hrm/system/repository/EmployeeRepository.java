package com.example.hrm.system.repository;

import com.example.hrm.system.emums.EmployeeStatus;
import com.example.hrm.system.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByUserId(Long userId);                          // prevent duplicate user linking
    List<Employee> findByDepartmentId(Long departmentId);         // get employees by department
    List<Employee> findByDesignationId(Long designationId);       // get employees by designation
    List<Employee> findByCategoryId(Long categoryId);             // get employees by category
    List<Employee> findByStatus(EmployeeStatus status);                // get active/inactive employees
}
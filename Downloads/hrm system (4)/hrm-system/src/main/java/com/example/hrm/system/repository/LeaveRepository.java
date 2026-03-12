package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);                // all leaves for an employee
    List<Leave> findByEmployeeIdAndStatus(Long employeeId,
                                          String status);         // filter by status (PENDING etc)
    List<Leave> findByStatus(String status);                      // all pending leaves (for HR)
    long countByEmployeeIdAndLeaveType(Long employeeId,
                                       String leaveType);         // count leaves by type
}

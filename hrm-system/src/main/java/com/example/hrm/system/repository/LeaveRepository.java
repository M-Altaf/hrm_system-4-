package com.example.hrm.system.repository;

import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.entity.Leave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeaveRepository extends JpaRepository<Leave, Long> {
    List<Leave> findByEmployeeId(Long employeeId);
    List<Leave> findByEmployeeIdAndStatus(Long employeeId, LeaveStatus status);
    List<Leave> findByStatus(String status);
    long countByEmployeeIdAndLeaveType(Long employeeId, String leaveType);
}

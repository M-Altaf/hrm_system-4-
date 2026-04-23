// Repository = talks directly to database
// Spring auto-generates SQL from method names
package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    // SELECT * FROM attendances WHERE employee_id = ?
    List<Attendance> findByEmployeeId(Long employeeId);

    // SELECT * FROM attendances WHERE date = ?
    List<Attendance> findByDate(LocalDate date);

    // SELECT * FROM attendances WHERE employee_id = ? AND date = ?
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId, LocalDate date);

    // SELECT * FROM attendances WHERE employee_id = ? AND date BETWEEN ? AND ?
    List<Attendance> findByEmployeeIdAndDateBetween(
            Long employeeId, LocalDate startDate, LocalDate endDate);

    // SELECT COUNT(*) > 0 WHERE employee_id = ? AND date = ?
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);
}
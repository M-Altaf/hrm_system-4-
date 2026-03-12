package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByEmployeeId(Long employeeId);
    List<Attendance> findByEmployeeIdAndDateBetween(Long employeeId,
                                                    LocalDate startDate,
                                                    LocalDate endDate);  // monthly report
    Optional<Attendance> findByEmployeeIdAndDate(Long employeeId,
                                                 LocalDate date);        // check duplicate
    boolean existsByEmployeeIdAndDate(Long employeeId, LocalDate date);   // prevent double checkin
    List<Attendance> findByDate(LocalDate date);                          // all attendance for a day
}
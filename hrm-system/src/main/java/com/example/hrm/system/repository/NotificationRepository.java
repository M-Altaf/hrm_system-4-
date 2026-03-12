package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByEmployeeId(Long employeeId);
    List<Notification> findByEmployeeIdAndStatus(Long employeeId,
                                                 String status);         // UNREAD notifications
    long countByEmployeeIdAndStatus(Long employeeId, String status);      // unread count badge
}

package com.example.hrm.system.repository;

import com.example.hrm.system.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByPerformedById(Long userId);                      // logs by user
    List<AuditLog> findByEntityName(String entityName);                   // logs by entity type
    List<AuditLog> findByEntityNameAndEntityId(String entityName,
                                               Long entityId);           // logs for specific record
    List<AuditLog> findByTimestampBetween(LocalDateTime start,
                                          LocalDateTime end);            // logs in date range
}

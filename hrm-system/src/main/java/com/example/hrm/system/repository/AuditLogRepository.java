package com.example.hrm.system.repository;


import com.example.hrm.system.emums.AuditAction;
import com.example.hrm.system.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByPerformedByUsername(String username);
    List<AuditLog> findByEntityName(String entityName);
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);
    List<AuditLog> findByAction(AuditAction action);
    List<AuditLog> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    List<AuditLog> findByPerformedByUsernameAndTimestampBetween(
            String username, LocalDateTime start, LocalDateTime end);
    List<AuditLog> findAllByOrderByTimestampDesc();  // latest first
}

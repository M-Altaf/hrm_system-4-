package com.example.hrm.system.services;



import com.example.hrm.system.dtos.responsedto.AuditLogResponseDto;
import com.example.hrm.system.emums.AuditAction;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {

    // Internal — called by AOP
    void log(AuditAction action,
             String entityName,
             Long entityId,
             String description,
             String performedByUsername);

    // Queries
    List<AuditLogResponseDto> getAllLogs();
    List<AuditLogResponseDto> getByUsername(String username);
    List<AuditLogResponseDto> getByEntityName(String entityName);
    List<AuditLogResponseDto> getByEntityNameAndId(String entityName, Long entityId);
    List<AuditLogResponseDto> getByAction(AuditAction action);
    List<AuditLogResponseDto> getByDateRange(LocalDateTime start, LocalDateTime end);
    List<AuditLogResponseDto> getByUsernameAndDateRange(
            String username, LocalDateTime start, LocalDateTime end);
}

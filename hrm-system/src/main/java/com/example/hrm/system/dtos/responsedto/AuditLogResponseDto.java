package com.example.hrm.system.dtos.responsedto;

import com.example.hrm.system.emums.AuditAction;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AuditLogResponseDto {
    private Long id;
    private AuditAction action;
    private String entityName;
    private Long entityId;
    private String description;
    private String performedByUsername;
    private String ipAddress;
    private LocalDateTime timestamp;
}
package com.example.hrm.system.serviceImpl;



import com.example.hrm.system.dtos.responsedto.AuditLogResponseDto;
import com.example.hrm.system.emums.AuditAction;
import com.example.hrm.system.entity.AuditLog;
import com.example.hrm.system.repository.AuditLogRepository;
import com.example.hrm.system.services.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogServiceImpl(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // ── LOG — called internally by AOP ───────────────────────────────

    @Override
    @Transactional
    public void log(AuditAction action,
                    String entityName,
                    Long entityId,
                    String description,
                    String performedByUsername) {
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(action);
        auditLog.setEntityName(entityName);
        auditLog.setEntityId(entityId);
        auditLog.setDescription(description);
        auditLog.setPerformedByUsername(performedByUsername);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }

    // ── GET ALL ──────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getAllLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc()
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY USERNAME ──────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByUsername(String username) {
        return auditLogRepository.findByPerformedByUsername(username)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ENTITY NAME ───────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByEntityName(String entityName) {
        return auditLogRepository.findByEntityName(entityName)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ENTITY NAME AND ID ────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByEntityNameAndId(
            String entityName, Long entityId) {
        return auditLogRepository
                .findByEntityNameAndEntityId(entityName, entityId)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY ACTION ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByAction(AuditAction action) {
        return auditLogRepository.findByAction(action)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY DATE RANGE ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByDateRange(
            LocalDateTime start, LocalDateTime end) {
        return auditLogRepository.findByTimestampBetween(start, end)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── GET BY USERNAME AND DATE RANGE ───────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<AuditLogResponseDto> getByUsernameAndDateRange(
            String username, LocalDateTime start, LocalDateTime end) {
        return auditLogRepository
                .findByPerformedByUsernameAndTimestampBetween(
                        username, start, end)
                .stream().map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // ── MAPPER ───────────────────────────────────────────────────────

    private AuditLogResponseDto mapToDto(AuditLog a) {
        AuditLogResponseDto dto = new AuditLogResponseDto();
        dto.setId(a.getId());
        dto.setAction(a.getAction());
        dto.setEntityName(a.getEntityName());
        dto.setEntityId(a.getEntityId());
        dto.setDescription(a.getDescription());
        dto.setPerformedByUsername(a.getPerformedByUsername());
        dto.setIpAddress(a.getIpAddress());
        dto.setTimestamp(a.getTimestamp());
        return dto;
    }
}
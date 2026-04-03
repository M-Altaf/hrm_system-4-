package com.example.hrm.system.entity;


import com.example.hrm.system.emums.AuditAction;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AuditAction action;         // CREATE, UPDATE, DELETE etc

    private String entityName;          // "Employee", "Leave", "Payroll"
    private Long entityId;              // which record was affected
    private String description;         // human readable description
    private String performedByUsername; // who did it
    private String ipAddress;           // from where
    private LocalDateTime timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy;
}
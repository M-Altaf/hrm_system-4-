package com.example.hrm.system.entity;

import com.example.hrm.system.emums.LeaveStatus;
import com.example.hrm.system.emums.LeaveType;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "leaves")
public class Leave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    private LocalDate startDate;
    private LocalDate endDate;
    private Integer totalDays;
    private String reason;

    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    private LocalDateTime appliedDate;
    private LocalDateTime updatedAt;
    private String rejectionReason;     // filled when HR rejects

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")   // HR/Manager who approved
    private User approvedBy;
}
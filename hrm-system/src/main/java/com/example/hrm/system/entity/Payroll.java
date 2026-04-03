package com.example.hrm.system.entity;


import com.example.hrm.system.emums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "payrolls")
public class Payroll {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer month;
    private Integer year;
    private Double basicSalary;
    private Double bonus;
    private Double deduction;
    private Double tax;
    private Double netSalary;
    private Integer presentDays;        // from attendance
    private Integer absentDays;         // from attendance
    private Integer lateDays;           // from attendance
    private Double perDaySalary;        // basicSalary / working days
    private String notes;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime generatedDate;
    private LocalDateTime paidDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}

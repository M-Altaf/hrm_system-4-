package com.example.hrm.system.entity;

import com.example.hrm.system.emums.PaymentStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
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
    private Integer presentDays;
    private Integer absentDays;
    private Integer lateDays;
    private Double perDaySalary;
    private String notes;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private LocalDateTime generatedDate;
    private LocalDateTime paidDate;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
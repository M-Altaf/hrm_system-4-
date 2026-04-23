package com.example.hrm.system.entity;

import com.example.hrm.system.emums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
@Data
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate date;

    private LocalTime checkIn;
    private LocalTime checkOut;

    private Double workingHours;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private String lateReason;

    // getters & setters
}
package com.example.hrm.system.entity;

import com.example.hrm.system.emums.AttendanceStatus;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "attendance")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workingHours;

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    private String lateReason;
}
package com.example.hrm.system.entity;



import com.example.hrm.system.emums.AttendanceStatus;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Entity
@Table(name = "attendances")
public class Attendance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private LocalTime checkIn;
    private LocalTime checkOut;
    private Double workingHours;        // auto calculated
    private String lateReason;          // filled if LATE

    @Enumerated(EnumType.STRING)
    private AttendanceStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;
}
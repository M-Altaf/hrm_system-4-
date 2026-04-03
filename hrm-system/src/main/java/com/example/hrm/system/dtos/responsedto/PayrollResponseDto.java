package com.example.hrm.system.dtos.responsedto;


import com.example.hrm.system.emums.PaymentStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PayrollResponseDto {
    private Long id;
    private Long employeeId;
    private String employeeName;
    private String department;
    private String designation;
    private Integer month;
    private Integer year;
    private Double basicSalary;
    private Double perDaySalary;
    private Integer presentDays;
    private Integer absentDays;
    private Integer lateDays;
    private Double bonus;
    private Double deduction;
    private Double tax;
    private Double netSalary;
    private String notes;
    private PaymentStatus paymentStatus;
    private LocalDateTime generatedDate;
    private LocalDateTime paidDate;
}

package com.example.hrm.system.dtos.requestdto;


import lombok.Data;

@Data
public class PayrollRequestDto {
    private Long employeeId;
    private Integer month;
    private Integer year;
    private Double bonus;           // optional extra bonus
    private Double extraDeduction;  // optional extra deduction
    private String notes;
}

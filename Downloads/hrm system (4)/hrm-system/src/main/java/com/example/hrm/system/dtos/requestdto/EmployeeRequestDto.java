package com.example.hrm.system.dtos.requestdto;

import lombok.Data;

@Data
public class EmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private Double salary;
}
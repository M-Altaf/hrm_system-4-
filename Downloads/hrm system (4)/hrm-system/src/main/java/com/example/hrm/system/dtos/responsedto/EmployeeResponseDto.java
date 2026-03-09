package com.example.hrm.system.dtos.responsedto;

import lombok.Data;

@Data
public class EmployeeResponseDto {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String department;
    private String position;
    private Double salary;
}

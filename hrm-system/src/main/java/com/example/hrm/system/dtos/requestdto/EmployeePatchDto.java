package com.example.hrm.system.dtos.requestdto;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeePatchDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private String status;

    // IDs for FK fields — null means don't update
    private Long departmentId;
    private Long designationId;
    private Long categoryId;
    private Long userId;
}
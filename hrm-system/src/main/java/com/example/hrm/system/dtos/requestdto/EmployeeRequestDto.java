package com.example.hrm.system.dtos.requestdto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EmployeeRequestDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private String status;

    // IDs only — not String, not objects
    private Long departmentId;
    private Long designationId;
    private Long categoryId;
    private Long userId;
}
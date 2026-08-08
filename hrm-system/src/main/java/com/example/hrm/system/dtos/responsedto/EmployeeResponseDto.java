package com.example.hrm.system.dtos.responsedto;

import com.example.hrm.system.emums.EmployeeStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class EmployeeResponseDto{
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private LocalDate hireDate;
    private EmployeeStatus status;
    private Long departmentId;
    private String departmentName;
    private Long designationId;
    private String designationTitle;
    private  Long categoryId;
    private String categoryName;
    private Long userId;
    private String username;
    private boolean hasProfilePicture;
}

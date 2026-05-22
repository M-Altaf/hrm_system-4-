package com.example.hrm.system.dtos.requestdto;

import com.example.hrm.system.emums.EmployeeStatus;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.processing.Pattern;


import java.time.LocalDate;

@Data
@NoArgsConstructor
public class EmployeeRequestDto {

    @NotBlank(message = "First name is required")
    @Size(max = 50)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(max = 50)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

//    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Invalid phone number format")
    private String phone;

    @NotNull(message = "Hire date is required")
    @PastOrPresent(message = "Hire date cannot be in the future")
    private LocalDate hireDate;
    private EmployeeStatus status;

    @NotNull(message = "Department is required")
    private Long departmentId;

    @NotNull(message = "Designation is required")
    private Long designationId;

    @NotNull(message = "Category is required")
    private Long categoryId;

    @NotNull(message = "User is required")
    private Long userId;
}
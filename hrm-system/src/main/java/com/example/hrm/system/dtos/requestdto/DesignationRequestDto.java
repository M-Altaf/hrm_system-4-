package com.example.hrm.system.dtos.requestdto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DesignationRequestDto {

    @NotBlank(message = "Designation title is required")
    private String title;

    private Double baseSalary;

    @NotNull(message = "Department id is required")
    private Long departmentId;

    public DesignationRequestDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public Double getBaseSalary() { return baseSalary; }
    public void setBaseSalary(Double baseSalary) { this.baseSalary = baseSalary; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}
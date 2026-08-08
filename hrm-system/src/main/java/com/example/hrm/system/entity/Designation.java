package com.example.hrm.system.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "designations")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Double baseSalary;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @JsonIgnore
    @OneToMany(mappedBy = "designation", fetch = FetchType.LAZY)
    private List<Employee> employees;

    // Constructor
    public Designation() {}

    // Getters
    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Double getBaseSalary() { return baseSalary; }
    public Department getDepartment() { return department; }
    public List<Employee> getEmployees() { return employees; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setBaseSalary(Double baseSalary) { this.baseSalary = baseSalary; }
    public void setDepartment(Department department) { this.department = department; }
    public void setEmployees(List<Employee> employees) { this.employees = employees; }
}
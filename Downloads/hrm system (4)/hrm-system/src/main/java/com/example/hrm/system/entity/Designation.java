package com.example.hrm.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import jakarta.persistence.Id;

@Data
@Entity
@Table(name = "designations")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private Double baseSalary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @OneToMany(mappedBy = "designation", fetch = FetchType.LAZY)
    private List<Employee> employees;
}
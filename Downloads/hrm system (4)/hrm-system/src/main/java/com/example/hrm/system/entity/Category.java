package com.example.hrm.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import jakarta.persistence.Id;

@Data
@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Employee> employees;
}
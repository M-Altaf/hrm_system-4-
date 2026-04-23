package com.example.hrm.system.entity;

import jakarta.persistence.*;
import lombok.Data;
import jakarta.persistence.Id;

@Data
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private Long id;
    private String name;
    private String description;
}

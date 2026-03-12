package com.example.hrm.system.entity;


import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String password;
    private  boolean enabled;
    private LocalDateTime createAt;

    // ── Foreign Keys ─────────────────────────────────────────────────

    @ManyToOne(fetch = FetchType.LAZY)         // ← Role entity, FK to roles table
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

}
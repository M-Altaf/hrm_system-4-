package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Just declare method signatures — Spring generates the implementation automatically
    Optional<Role> findByName(String name);   // ← no static, nobody, no return null

    // findById(Long id) already comes FREE from JpaRepository — don't redeclare it
}

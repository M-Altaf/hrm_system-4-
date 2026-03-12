package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {
    Optional<Designation> findByTitle(String title);
    boolean existsByTitle(String title);
    List<Designation> findByDepartmentId(Long departmentId);  // get all designations by department
}


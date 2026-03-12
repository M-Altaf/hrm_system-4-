package com.example.hrm.system.repository;

import com.example.hrm.system.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {
    Optional<Address> findByEmployeeId(Long employeeId);          // get address by employee
    boolean existsByEmployeeId(Long employeeId);
}

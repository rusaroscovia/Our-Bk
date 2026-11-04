package com.bk.repository;

import com.bk.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Optional<Employee> findByNormalizedName(String normalizedName);

    List<Employee> findAllByOrderByNameAsc();
}

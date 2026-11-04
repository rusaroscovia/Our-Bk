package com.bk.service;

import com.bk.model.Employee;
import com.bk.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    public EmployeeService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Transactional(readOnly = true)
    public List<Employee> getEmployees() {
        return employeeRepository.findAllByOrderByNameAsc();
    }

    @Transactional
    public Employee register(String name) {
        String validatedName = validateName(name);
        if (employeeRepository.findByNormalizedName(NameNormalizer.normalize(validatedName)).isPresent()) {
            throw new DuplicateEmployeeException(validatedName);
        }

        try {
            return employeeRepository.saveAndFlush(new Employee(validatedName));
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseConstraint.matches(exception, "uk_employees_normalized_name")) {
                throw new DuplicateEmployeeException(validatedName);
            }
            throw exception;
        }
    }

    @Transactional
    public Employee rename(long employeeId, String name) {
        String validatedName = validateName(name);
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        String normalizedName = NameNormalizer.normalize(validatedName);
        employeeRepository.findByNormalizedName(normalizedName)
                .filter(existing -> !existing.getId().equals(employeeId))
                .ifPresent(existing -> {
                    throw new DuplicateEmployeeException(validatedName);
                });
        employee.setName(validatedName);

        try {
            return employeeRepository.saveAndFlush(employee);
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseConstraint.matches(exception, "uk_employees_normalized_name")) {
                throw new DuplicateEmployeeException(validatedName);
            }
            throw exception;
        }
    }

    private String validateName(String name) {
        if (name == null || name.isBlank() || name.trim().length() > 100) {
            throw new IllegalArgumentException("Employee name must contain 1 to 100 characters.");
        }
        return name.trim();
    }
}

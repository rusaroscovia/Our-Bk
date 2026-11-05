package com.bk.repository;

import com.bk.model.Payment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @EntityGraph(attributePaths = "employee")
    List<Payment> findByPaymentDateBetweenOrderByPaymentDateDesc(LocalDate start, LocalDate end);

    @EntityGraph(attributePaths = "employee")
    List<Payment> findByEmployeeIdOrderByPaymentDateDesc(Long employeeId);
}

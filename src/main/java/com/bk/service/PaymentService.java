package com.bk.service;

import com.bk.model.Employee;
import com.bk.model.Payment;
import com.bk.repository.EmployeeRepository;
import com.bk.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class PaymentService {

    private final EmployeeRepository employeeRepository;
    private final PaymentRepository paymentRepository;

    public PaymentService(EmployeeRepository employeeRepository, PaymentRepository paymentRepository) {
        this.employeeRepository = employeeRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public Payment record(long employeeId, BigDecimal amountRwf, LocalDate paymentDate) {
        ProductService.validateAmount(amountRwf, "Payment");
        validateDate(paymentDate, "Payment date");
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Employee", employeeId));
        return paymentRepository.save(new Payment(employee, amountRwf, paymentDate));
    }

    @Transactional(readOnly = true)
    public List<Payment> getPayments(LocalDate start, LocalDate end) {
        validateRange(start, end);
        return paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(start, end);
    }

    @Transactional(readOnly = true)
    public List<Payment> getPaymentsForEmployee(long employeeId) {
        if (!employeeRepository.existsById(employeeId)) {
            throw new ResourceNotFoundException("Employee", employeeId);
        }
        return paymentRepository.findByEmployeeIdOrderByPaymentDateDesc(employeeId);
    }

    static void validateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            throw new IllegalArgumentException("Date range must have a start date before or equal to its end date.");
        }
    }

    static void validateDate(LocalDate date, String label) {
        if (date == null || date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(label + " is required and cannot be in the future.");
        }
    }
}

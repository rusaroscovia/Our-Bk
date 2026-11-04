package com.bk.service;

import com.bk.model.Expense;
import com.bk.model.Payment;
import com.bk.repository.ExpenseRepository;
import com.bk.repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialService {

    private final PaymentRepository paymentRepository;
    private final ExpenseRepository expenseRepository;

    public FinancialService(PaymentRepository paymentRepository, ExpenseRepository expenseRepository) {
        this.paymentRepository = paymentRepository;
        this.expenseRepository = expenseRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummary getSummary(LocalDate start, LocalDate end) {
        PaymentService.validateRange(start, end);
        List<Payment> payments = paymentRepository.findByPaymentDateBetweenOrderByPaymentDateDesc(start, end);
        List<Expense> expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(start, end);
        BigDecimal collected = payments.stream()
                .map(Payment::getAmountRwf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spent = expenses.stream()
                .map(Expense::getAmountRwf)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long employeesPaid = payments.stream()
                .map(payment -> payment.getEmployee().getId())
                .distinct()
                .count();
        return new FinancialSummary(collected, spent, collected.subtract(spent), employeesPaid);
    }
}

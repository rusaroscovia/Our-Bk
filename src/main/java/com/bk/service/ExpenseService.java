package com.bk.service;

import com.bk.model.Expense;
import com.bk.repository.ExpenseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;

    public ExpenseService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Transactional
    public Expense record(String description, BigDecimal amountRwf, LocalDate expenseDate) {
        if (description == null || description.isBlank() || description.trim().length() > 200) {
            throw new IllegalArgumentException("Expense description must contain 1 to 200 characters.");
        }
        ProductService.validateAmount(amountRwf, "Expense");
        PaymentService.validateDate(expenseDate, "Expense date");
        return expenseRepository.save(new Expense(description.trim(), amountRwf, expenseDate));
    }

    @Transactional(readOnly = true)
    public List<Expense> getExpenses(LocalDate start, LocalDate end) {
        PaymentService.validateRange(start, end);
        return expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDesc(start, end);
    }
}

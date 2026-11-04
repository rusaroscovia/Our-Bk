package com.bk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String description;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 0)
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amountRwf;

    @NotNull
    @Column(nullable = false)
    private LocalDate expenseDate;

    protected Expense() {
    }

    public Expense(String description, BigDecimal amountRwf, LocalDate expenseDate) {
        this.description = description;
        this.amountRwf = amountRwf;
        this.expenseDate = expenseDate;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getAmountRwf() {
        return amountRwf;
    }

    public LocalDate getExpenseDate() {
        return expenseDate;
    }
}

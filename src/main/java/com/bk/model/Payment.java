package com.bk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 0)
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal amountRwf;

    @NotNull
    @Column(nullable = false)
    private LocalDate paymentDate;

    protected Payment() {
    }

    public Payment(Employee employee, BigDecimal amountRwf, LocalDate paymentDate) {
        this.employee = employee;
        this.amountRwf = amountRwf;
        this.paymentDate = paymentDate;
    }

    public Long getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public BigDecimal getAmountRwf() {
        return amountRwf;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }
}

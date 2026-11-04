package com.bk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Digits;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true, length = 20)
    private ProductName name;

    @NotNull
    @Positive
    @Digits(integer = 12, fraction = 0)
    @Column(nullable = false, precision = 12, scale = 0)
    private BigDecimal priceRwf;

    protected Product() {
    }

    public Product(ProductName name, BigDecimal priceRwf) {
        this.name = name;
        this.priceRwf = priceRwf;
    }

    public Long getId() {
        return id;
    }

    public ProductName getName() {
        return name;
    }

    public BigDecimal getPriceRwf() {
        return priceRwf;
    }

    public void setPriceRwf(BigDecimal priceRwf) {
        this.priceRwf = priceRwf;
    }
}

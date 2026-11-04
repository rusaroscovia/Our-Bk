package com.bk.service;

import java.math.BigDecimal;

public record FinancialSummary(
        BigDecimal totalCollectedRwf,
        BigDecimal totalSpentRwf,
        BigDecimal balanceRwf,
        long employeesPaid
) {
}

package com.example.beach_safety_water_quality.service;

import java.math.BigDecimal;

public class FinancialSummary {

    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netBalance;

    public FinancialSummary() {
    }

    public FinancialSummary(BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal netBalance) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.netBalance = netBalance;
    }

    public BigDecimal getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(BigDecimal totalIncome) {
        this.totalIncome = totalIncome;
    }

    public BigDecimal getTotalExpense() {
        return totalExpense;
    }

    public void setTotalExpense(BigDecimal totalExpense) {
        this.totalExpense = totalExpense;
    }

    public BigDecimal getNetBalance() {
        return netBalance;
    }

    public void setNetBalance(BigDecimal netBalance) {
        this.netBalance = netBalance;
    }
}


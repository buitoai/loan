package com.loan.decision.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditScoringInput {

    private int totalLoans;
    private int activeLoans;
    private int closedLoans;
    private int latePayments;
    private int defaults;
    private BigDecimal currentDebt;
    private BigDecimal monthlyIncome;
    private int creditAgeMonths;
}

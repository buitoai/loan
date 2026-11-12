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
public class RiskAnalysisInput {

    private BigDecimal currentDebt;
    private BigDecimal loanAmount;
    /** Loan term in months */
    private int loanTerm;
    private BigDecimal monthlyIncome;
    private int latePayments;
    private int defaults;
}

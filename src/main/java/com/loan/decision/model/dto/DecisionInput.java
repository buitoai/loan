package com.loan.decision.model.dto;

import com.loan.decision.model.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionInput {

    private int creditScore;
    private RiskLevel riskLevel;
    private BigDecimal loanAmount;
}

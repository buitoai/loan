package com.loan.decision.model.dto;

import com.loan.decision.model.entity.LoanDecision;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionResult {

    private LoanDecision decision;

    /** Decision reason — never null or blank */
    private String reason;

    private LocalDateTime processedAt;
}

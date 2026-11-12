package com.loan.decision.service;

import com.loan.decision.model.dto.DecisionInput;
import com.loan.decision.model.dto.DecisionResult;
import com.loan.decision.model.entity.LoanDecision;
import com.loan.decision.model.entity.RiskLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Default implementation of {@link DecisionEngineService}.
 * Reads decision thresholds from {@link ConfigService} and applies
 * ordered business rules to produce a {@link DecisionResult}.
 */
@Service
@RequiredArgsConstructor
public class DecisionEngineServiceImpl implements DecisionEngineService {

    private static final long DEFAULT_APPROVE_THRESHOLD  = 700L;
    private static final long DEFAULT_REJECT_THRESHOLD   = 600L;
    private static final long DEFAULT_LARGE_AMOUNT_LIMIT = 500_000_000L;

    private final ConfigService configService;

    @Override
    public DecisionResult makeDecision(DecisionInput input) {
        // --- read thresholds (fall back to defaults when config is absent) ---
        long approveThreshold = resolveThreshold("APPROVE_SCORE_THRESHOLD", DEFAULT_APPROVE_THRESHOLD);
        long rejectThreshold  = resolveThreshold("REJECT_SCORE_THRESHOLD",  DEFAULT_REJECT_THRESHOLD);
        long largeAmountLimit = resolveThreshold("LARGE_AMOUNT_THRESHOLD",  DEFAULT_LARGE_AMOUNT_LIMIT);

        int       creditScore = input.getCreditScore();
        RiskLevel riskLevel   = input.getRiskLevel();
        BigDecimal loanAmount = input.getLoanAmount();

        LoanDecision decision;
        String reason;

        // Rule 1 – large loan amount requires manual review
        if (loanAmount != null && loanAmount.compareTo(BigDecimal.valueOf(largeAmountLimit)) > 0) {
            decision = LoanDecision.PENDING_REVIEW;
            reason   = "Loan amount exceeds large amount threshold, requires manual review";

        // Rule 2 – strong credit score + low risk → approve
        } else if (creditScore >= approveThreshold && riskLevel == RiskLevel.LOW) {
            decision = LoanDecision.APPROVED;
            reason   = "Credit score and risk level meet approval criteria";

        // Rule 3 – weak credit score or high risk → reject
        } else if (creditScore < rejectThreshold || riskLevel == RiskLevel.HIGH) {
            decision = LoanDecision.REJECTED;
            reason   = (riskLevel == RiskLevel.HIGH)
                    ? "High risk level detected"
                    : "Credit score below minimum threshold";

        // Rule 4 – everything else needs manual review
        } else {
            decision = LoanDecision.PENDING_REVIEW;
            reason   = "Credit score and risk level require manual review";
        }

        return DecisionResult.builder()
                .decision(decision)
                .reason(reason)
                .processedAt(LocalDateTime.now())
                .build();
    }

    // -------------------------------------------------------------------------
    // helpers
    // -------------------------------------------------------------------------

    private long resolveThreshold(String key, long defaultValue) {
        Long value = configService.getConfigAsLong(key);
        return (value != null) ? value : defaultValue;
    }
}

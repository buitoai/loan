package com.loan.decision.service;

import com.loan.decision.model.dto.CreditScoreResult;
import com.loan.decision.model.dto.CreditScoringInput;

/**
 * Service interface for credit score calculation.
 */
public interface CreditScoringService {

    /**
     * Calculates the credit score for the given input.
     *
     * @param input credit scoring input data
     * @return credit score result with full breakdown
     */
    CreditScoreResult calculateScore(CreditScoringInput input);
}

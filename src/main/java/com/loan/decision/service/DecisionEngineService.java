package com.loan.decision.service;

import com.loan.decision.model.dto.DecisionInput;
import com.loan.decision.model.dto.DecisionResult;

/**
 * Service interface for the loan decision engine.
 * Applies configurable business rules to produce a loan decision.
 */
public interface DecisionEngineService {

    /**
     * Evaluate a loan application and return a decision.
     *
     * @param input the decision input containing credit score, risk level, and loan amount
     * @return a {@link DecisionResult} with a non-null, non-blank reason and a processed timestamp
     */
    DecisionResult makeDecision(DecisionInput input);
}

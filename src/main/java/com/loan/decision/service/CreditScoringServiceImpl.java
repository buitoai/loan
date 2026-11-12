package com.loan.decision.service;

import com.loan.decision.model.dto.CreditScoreResult;
import com.loan.decision.model.dto.CreditScoringInput;
import com.loan.decision.util.CreditScoreCalculator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of {@link CreditScoringService} that delegates score
 * computation to the stateless {@link CreditScoreCalculator} utility.
 */
@Service
public class CreditScoringServiceImpl implements CreditScoringService {

    @Override
    public CreditScoreResult calculateScore(CreditScoringInput input) {
        double paymentHistoryScore = CreditScoreCalculator.calculatePaymentHistoryScore(
                input.getLatePayments(), input.getTotalLoans());

        double currentDebtScore = CreditScoreCalculator.calculateCurrentDebtScore(
                input.getCurrentDebt(), input.getMonthlyIncome());

        double creditAgeScore = CreditScoreCalculator.calculateCreditAgeScore(
                input.getCreditAgeMonths());

        double creditMixScore = CreditScoreCalculator.calculateCreditMixScore(
                input.getClosedLoans(), input.getTotalLoans());

        double newCreditScore = CreditScoreCalculator.calculateNewCreditScore(
                input.getActiveLoans());

        int finalScore = CreditScoreCalculator.calculateScore(input);

        return CreditScoreResult.builder()
                .score(finalScore)
                .paymentHistoryScore(paymentHistoryScore)
                .currentDebtScore(currentDebtScore)
                .creditAgeScore(creditAgeScore)
                .creditMixScore(creditMixScore)
                .newCreditScore(newCreditScore)
                .calculatedAt(LocalDateTime.now())
                .build();
    }
}

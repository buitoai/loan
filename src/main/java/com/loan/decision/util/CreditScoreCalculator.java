package com.loan.decision.util;

import com.loan.decision.model.dto.CreditScoringInput;

import java.math.BigDecimal;

/**
 * Utility class for FICO-based credit score calculation.
 * All methods are pure functions with no side effects.
 */
public final class CreditScoreCalculator {

    private static final int SCORE_MIN = 300;
    private static final int SCORE_MAX = 850;
    private static final int SCORE_RANGE = 550; // 850 - 300

    private static final double WEIGHT_PAYMENT_HISTORY = 0.35;
    private static final double WEIGHT_CURRENT_DEBT    = 0.30;
    private static final double WEIGHT_CREDIT_AGE      = 0.15;
    private static final double WEIGHT_CREDIT_MIX      = 0.10;
    private static final double WEIGHT_NEW_CREDIT      = 0.10;

    private static final double CREDIT_AGE_FULL_MONTHS = 120.0;
    private static final double NEW_CREDIT_MAX_LOANS   = 10.0;

    /** Prevent instantiation — utility class. */
    private CreditScoreCalculator() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Calculates the FICO-based credit score for the given input.
     *
     * @param input credit scoring input data (all fields non-negative)
     * @return integer credit score clamped to [300, 850]
     */
    public static int calculateScore(CreditScoringInput input) {
        double paymentHistory = calculatePaymentHistoryScore(input.getLatePayments(), input.getTotalLoans());
        double currentDebt    = calculateCurrentDebtScore(input.getCurrentDebt(), input.getMonthlyIncome());
        double creditAge      = calculateCreditAgeScore(input.getCreditAgeMonths());
        double creditMix      = calculateCreditMixScore(input.getClosedLoans(), input.getTotalLoans());
        double newCredit      = calculateNewCreditScore(input.getActiveLoans());

        double weightedSum = paymentHistory * WEIGHT_PAYMENT_HISTORY
                           + currentDebt    * WEIGHT_CURRENT_DEBT
                           + creditAge      * WEIGHT_CREDIT_AGE
                           + creditMix      * WEIGHT_CREDIT_MIX
                           + newCredit      * WEIGHT_NEW_CREDIT;

        double rawScore = SCORE_MIN + weightedSum * SCORE_RANGE;

        // Clamp to [300, 850]
        int score = (int) Math.round(rawScore);
        return Math.max(SCORE_MIN, Math.min(SCORE_MAX, score));
    }

    /**
     * Calculates the payment history component score.
     * Formula: 1 - (latePayments / max(totalLoans, 1))
     *
     * @param latePayments number of late payments (non-negative)
     * @param totalLoans   total number of loans (non-negative)
     * @return score in [0, 1]
     */
    public static double calculatePaymentHistoryScore(int latePayments, int totalLoans) {
        int denominator = Math.max(totalLoans, 1);
        double ratio = (double) latePayments / denominator;
        return Math.max(0.0, Math.min(1.0, 1.0 - ratio));
    }

    /**
     * Calculates the current debt component score.
     * Formula: 1 - min(currentDebt / (monthlyIncome * 12), 1)
     * If monthlyIncome is null or zero, returns 0 (maximum debt burden).
     *
     * @param currentDebt   total current outstanding debt (non-negative)
     * @param monthlyIncome monthly income (non-negative)
     * @return score in [0, 1]
     */
    public static double calculateCurrentDebtScore(BigDecimal currentDebt, BigDecimal monthlyIncome) {
        // Null or zero income → treat as very high debt ratio
        if (monthlyIncome == null || monthlyIncome.compareTo(BigDecimal.ZERO) == 0) {
            return 0.0;
        }

        BigDecimal annualIncome = monthlyIncome.multiply(BigDecimal.valueOf(12));
        double debtRatio = currentDebt.divide(annualIncome, 10, java.math.RoundingMode.HALF_UP).doubleValue();
        return Math.max(0.0, 1.0 - Math.min(debtRatio, 1.0));
    }

    /**
     * Calculates the credit age component score.
     * Formula: min(creditAgeMonths / 120.0, 1)
     *
     * @param creditAgeMonths age of credit history in months (non-negative)
     * @return score in [0, 1]
     */
    public static double calculateCreditAgeScore(int creditAgeMonths) {
        return Math.min(creditAgeMonths / CREDIT_AGE_FULL_MONTHS, 1.0);
    }

    /**
     * Calculates the credit mix component score.
     * Formula: min(closedLoans / max(totalLoans, 1), 1)
     *
     * @param closedLoans number of successfully closed loans (non-negative)
     * @param totalLoans  total number of loans (non-negative)
     * @return score in [0, 1]
     */
    public static double calculateCreditMixScore(int closedLoans, int totalLoans) {
        int denominator = Math.max(totalLoans, 1);
        return Math.min((double) closedLoans / denominator, 1.0);
    }

    /**
     * Calculates the new credit component score.
     * Formula: 1 - min(activeLoans / 10.0, 1)
     *
     * @param activeLoans number of currently active loans (non-negative)
     * @return score in [0, 1]
     */
    public static double calculateNewCreditScore(int activeLoans) {
        return Math.max(0.0, 1.0 - Math.min(activeLoans / NEW_CREDIT_MAX_LOANS, 1.0));
    }
}

package com.loan.decision.service;

import com.loan.decision.model.dto.CreditScoreDistribution;
import com.loan.decision.model.dto.ReportSummary;
import com.loan.decision.model.dto.RiskDistribution;

import java.time.LocalDate;

/**
 * Service interface for generating loan application reports.
 */
public interface ReportService {

    /**
     * Returns a summary of loan applications within the given date range.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return {@link ReportSummary} with counts, amounts, and rates
     */
    ReportSummary getSummary(LocalDate from, LocalDate to);

    /**
     * Returns the distribution of credit scores across predefined ranges.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return {@link CreditScoreDistribution}
     */
    CreditScoreDistribution getCreditScoreDistribution(LocalDate from, LocalDate to);

    /**
     * Returns the distribution of risk levels (LOW / MEDIUM / HIGH).
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return {@link RiskDistribution}
     */
    RiskDistribution getRiskDistribution(LocalDate from, LocalDate to);

    /**
     * Exports loan applications in the given date range as a UTF-8 encoded CSV byte array.
     *
     * @param from start date (inclusive)
     * @param to   end date (inclusive)
     * @return CSV content as {@code byte[]}
     */
    byte[] exportCsv(LocalDate from, LocalDate to);
}

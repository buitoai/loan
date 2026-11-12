package com.loan.decision.controller;

import com.loan.decision.model.dto.CreditScoreDistribution;
import com.loan.decision.model.dto.ReportSummary;
import com.loan.decision.model.dto.RiskDistribution;
import com.loan.decision.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST controller for report and statistics endpoints.
 *
 * <p>All endpoints require the {@code MANAGER} or {@code ADMIN} role, enforced via
 * {@code @PreAuthorize("hasAnyRole('MANAGER','ADMIN')")} at the class level.
 * Covers summary reports, credit-score distribution, risk distribution, and CSV export.
 *
 * <p>Supports Requirements 9.1–9.6.
 */
@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // -------------------------------------------------------------------------
    // Summary
    // -------------------------------------------------------------------------

    /**
     * Returns a summary of loan applications within the given date range.
     *
     * <p>The summary includes total application counts by status, total approved amount,
     * approval rate, and rejection rate.
     *
     * <p>Supports Requirements 9.1, 9.2.
     *
     * @param from start date (inclusive), ISO date format (yyyy-MM-dd)
     * @param to   end date (inclusive), ISO date format (yyyy-MM-dd)
     * @return 200 OK with a {@link ReportSummary}
     */
    @GetMapping("/summary")
    public ResponseEntity<ReportSummary> getSummary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        ReportSummary summary = reportService.getSummary(from, to);
        return ResponseEntity.ok(summary);
    }

    // -------------------------------------------------------------------------
    // Credit-Score Distribution
    // -------------------------------------------------------------------------

    /**
     * Returns the distribution of credit scores across predefined ranges.
     *
     * <p>Ranges: 300–499, 500–599, 600–699, 700–749, 750–850.
     *
     * <p>Supports Requirement 9.3.
     *
     * @param from start date (inclusive), ISO date format (yyyy-MM-dd)
     * @param to   end date (inclusive), ISO date format (yyyy-MM-dd)
     * @return 200 OK with a {@link CreditScoreDistribution}
     */
    @GetMapping("/credit-score-distribution")
    public ResponseEntity<CreditScoreDistribution> getCreditScoreDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        CreditScoreDistribution distribution = reportService.getCreditScoreDistribution(from, to);
        return ResponseEntity.ok(distribution);
    }

    // -------------------------------------------------------------------------
    // Risk Distribution
    // -------------------------------------------------------------------------

    /**
     * Returns the distribution of risk levels (LOW / MEDIUM / HIGH).
     *
     * <p>Supports Requirement 9.4.
     *
     * @param from start date (inclusive), ISO date format (yyyy-MM-dd)
     * @param to   end date (inclusive), ISO date format (yyyy-MM-dd)
     * @return 200 OK with a {@link RiskDistribution}
     */
    @GetMapping("/risk-distribution")
    public ResponseEntity<RiskDistribution> getRiskDistribution(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        RiskDistribution distribution = reportService.getRiskDistribution(from, to);
        return ResponseEntity.ok(distribution);
    }

    // -------------------------------------------------------------------------
    // CSV Export
    // -------------------------------------------------------------------------

    /**
     * Exports loan applications in the given date range as a CSV file download.
     *
     * <p>The response sets {@code Content-Type: text/csv} and
     * {@code Content-Disposition: attachment; filename=report.csv} so that browsers
     * and HTTP clients treat the response as a file download.
     *
     * <p>Supports Requirement 9.6.
     *
     * @param from start date (inclusive), ISO date format (yyyy-MM-dd)
     * @param to   end date (inclusive), ISO date format (yyyy-MM-dd)
     * @return 200 OK with CSV bytes and appropriate download headers
     */
    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        byte[] csvBytes = reportService.exportCsv(from, to);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        headers.setContentDispositionFormData("attachment", "report.csv");

        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }
}

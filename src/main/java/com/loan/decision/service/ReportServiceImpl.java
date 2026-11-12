package com.loan.decision.service;

import com.loan.decision.model.dto.CreditScoreDistribution;
import com.loan.decision.model.dto.ReportSummary;
import com.loan.decision.model.dto.RiskDistribution;
import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.LoanStatus;
import com.loan.decision.model.entity.RiskLevel;
import com.loan.decision.repository.LoanApplicationRepository;
import com.loan.decision.util.DataMaskingUtil;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Default implementation of {@link ReportService}.
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final LoanApplicationRepository loanApplicationRepository;

    public ReportServiceImpl(LoanApplicationRepository loanApplicationRepository) {
        this.loanApplicationRepository = loanApplicationRepository;
    }

    // -------------------------------------------------------------------------
    // getSummary
    // -------------------------------------------------------------------------

    @Override
    public ReportSummary getSummary(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<LoanApplication> applications = loanApplicationRepository
                .findWithFilters(null, fromDateTime, toDateTime, null, null, Pageable.unpaged())
                .getContent();

        long totalCount = applications.size();

        // Count by status
        Map<String, Long> countByStatus = new LinkedHashMap<>();
        for (LoanStatus status : LoanStatus.values()) {
            countByStatus.put(status.name(), 0L);
        }
        for (LoanApplication app : applications) {
            String key = app.getStatus().name();
            countByStatus.merge(key, 1L, Long::sum);
        }

        // Approved count and total approved amount
        long approvedCount = countByStatus.getOrDefault(LoanStatus.APPROVED.name(), 0L);
        long rejectedCount = countByStatus.getOrDefault(LoanStatus.REJECTED.name(), 0L);

        BigDecimal totalApprovedAmount = applications.stream()
                .filter(app -> LoanStatus.APPROVED == app.getStatus())
                .map(LoanApplication::getLoanAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double approvalRate = totalCount == 0 ? 0.0 : (double) approvedCount / totalCount;
        double rejectionRate = totalCount == 0 ? 0.0 : (double) rejectedCount / totalCount;

        return ReportSummary.builder()
                .countByStatus(countByStatus)
                .totalApprovedAmount(totalApprovedAmount)
                .approvalRate(approvalRate)
                .rejectionRate(rejectionRate)
                .totalApplications(totalCount)
                .from(from)
                .to(to)
                .build();
    }

    // -------------------------------------------------------------------------
    // getCreditScoreDistribution
    // -------------------------------------------------------------------------

    @Override
    public CreditScoreDistribution getCreditScoreDistribution(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<LoanApplication> applications = loanApplicationRepository
                .findWithFilters(null, fromDateTime, toDateTime, null, null, Pageable.unpaged())
                .getContent();

        long range300to499 = 0;
        long range500to599 = 0;
        long range600to699 = 0;
        long range700to749 = 0;
        long range750to850 = 0;

        for (LoanApplication app : applications) {
            Integer score = app.getCreditScore();
            if (score == null) {
                continue;
            }
            if (score >= 300 && score <= 499) {
                range300to499++;
            } else if (score >= 500 && score <= 599) {
                range500to599++;
            } else if (score >= 600 && score <= 699) {
                range600to699++;
            } else if (score >= 700 && score <= 749) {
                range700to749++;
            } else if (score >= 750 && score <= 850) {
                range750to850++;
            }
        }

        return CreditScoreDistribution.builder()
                .range300to499(range300to499)
                .range500to599(range500to599)
                .range600to699(range600to699)
                .range700to749(range700to749)
                .range750to850(range750to850)
                .build();
    }

    // -------------------------------------------------------------------------
    // getRiskDistribution
    // -------------------------------------------------------------------------

    @Override
    public RiskDistribution getRiskDistribution(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<LoanApplication> applications = loanApplicationRepository
                .findWithFilters(null, fromDateTime, toDateTime, null, null, Pageable.unpaged())
                .getContent();

        long lowCount = 0;
        long mediumCount = 0;
        long highCount = 0;

        for (LoanApplication app : applications) {
            RiskLevel riskLevel = app.getRiskLevel();
            if (riskLevel == null) {
                continue;
            }
            switch (riskLevel) {
                case LOW -> lowCount++;
                case MEDIUM -> mediumCount++;
                case HIGH -> highCount++;
            }
        }

        return RiskDistribution.builder()
                .lowCount(lowCount)
                .mediumCount(mediumCount)
                .highCount(highCount)
                .build();
    }

    // -------------------------------------------------------------------------
    // exportCsv
    // -------------------------------------------------------------------------

    @Override
    public byte[] exportCsv(LocalDate from, LocalDate to) {
        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.atTime(LocalTime.MAX);

        List<LoanApplication> applications = loanApplicationRepository
                .findWithFilters(null, fromDateTime, toDateTime, null, null, Pageable.unpaged())
                .getContent();

        StringBuilder csv = new StringBuilder();

        // Header row
        csv.append("applicationId,applicantName,maskedIdNumber,status,creditScore,riskLevel,decision,loanAmount,submittedAt\n");

        // Data rows
        for (LoanApplication app : applications) {
            csv.append(nullToEmpty(app.getApplicationId()));
            csv.append(',');
            csv.append(escapeCsvField(app.getApplicantName()));
            csv.append(',');
            csv.append(escapeCsvField(DataMaskingUtil.maskIdNumber(app.getIdNumber())));
            csv.append(',');
            csv.append(nullToEmpty(app.getStatus()));
            csv.append(',');
            csv.append(nullToEmpty(app.getCreditScore()));
            csv.append(',');
            csv.append(nullToEmpty(app.getRiskLevel()));
            csv.append(',');
            csv.append(nullToEmpty(app.getDecision()));
            csv.append(',');
            csv.append(nullToEmpty(app.getLoanAmount()));
            csv.append(',');
            csv.append(nullToEmpty(app.getSubmittedAt()));
            csv.append('\n');
        }

        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private String nullToEmpty(Object value) {
        return value == null ? "" : value.toString();
    }

    /**
     * Wraps a CSV field in double-quotes if it contains a comma, double-quote, or newline.
     * Internal double-quotes are escaped by doubling them.
     */
    private String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}

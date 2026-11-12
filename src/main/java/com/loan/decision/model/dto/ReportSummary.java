package com.loan.decision.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportSummary {

    /** Maps loan status name to application count. */
    private Map<String, Long> countByStatus;

    private BigDecimal totalApprovedAmount;

    /** Approval rate in range 0.0 to 1.0 */
    private double approvalRate;

    /** Rejection rate in range 0.0 to 1.0 */
    private double rejectionRate;

    private long totalApplications;

    private LocalDate from;
    private LocalDate to;
}

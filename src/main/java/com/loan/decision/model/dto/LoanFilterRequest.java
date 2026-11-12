package com.loan.decision.model.dto;

import com.loan.decision.model.entity.LoanStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanFilterRequest {

    /** Filter by loan status (nullable — no filter if null) */
    private LoanStatus status;

    private LocalDate fromDate;

    private LocalDate toDate;

    private BigDecimal minAmount;

    private BigDecimal maxAmount;

    /** Search term for applicant name or ID number */
    private String searchTerm;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;
}

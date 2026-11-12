package com.loan.decision.model.dto;

import com.loan.decision.model.entity.EmploymentType;
import com.loan.decision.model.entity.LoanDecision;
import com.loan.decision.model.entity.LoanStatus;
import com.loan.decision.model.entity.RiskLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplicationResponse {

    private Long applicationId;
    private String applicantName;
    private String maskedIdNumber;
    private Long customerId;

    // Personal info
    private LocalDate dateOfBirth;
    private String gender;
    private String address;

    // Financial info
    private BigDecimal monthlyIncome;
    private EmploymentType employmentType;
    private String companyName;
    private BigDecimal currentDebt;

    // Loan info
    private BigDecimal loanAmount;
    private Integer loanTerm;
    private BigDecimal interestRate;
    private String loanPurpose;

    // Scoring & decision
    private Integer creditScore;
    private RiskLevel riskLevel;
    private LoanDecision decision;
    private LoanStatus status;
    private String rejectionReason;

    // Timestamps
    private LocalDateTime submittedAt;
    private LocalDateTime processedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

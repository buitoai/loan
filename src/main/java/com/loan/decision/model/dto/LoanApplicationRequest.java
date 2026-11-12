package com.loan.decision.model.dto;

import com.loan.decision.model.entity.EmploymentType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class LoanApplicationRequest {

    @NotBlank(message = "Applicant name is required")
    private String applicantName;

    @NotBlank(message = "ID number is required")
    private String idNumber;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    private String gender;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotBlank(message = "Email is required")
    private String email;

    @NotNull(message = "Employment type is required")
    private EmploymentType employmentType;

    private String companyName;

    @NotNull(message = "Monthly income is required")
    @DecimalMin(value = "0.01", message = "Monthly income must be greater than 0")
    private BigDecimal monthlyIncome;

    @NotNull(message = "Current debt is required")
    @DecimalMin(value = "0", message = "Current debt cannot be negative")
    @Builder.Default
    private BigDecimal currentDebt = BigDecimal.ZERO;

    @NotNull(message = "Loan amount is required")
    @DecimalMin(value = "1000000", message = "Loan amount must be at least 1,000,000")
    @DecimalMax(value = "5000000000", message = "Loan amount must not exceed 5,000,000,000")
    private BigDecimal loanAmount;

    @NotNull(message = "Loan term is required")
    @Min(value = 1, message = "Loan term must be at least 1 month")
    private Integer loanTerm;

    @NotBlank(message = "Loan purpose is required")
    private String loanPurpose;
}

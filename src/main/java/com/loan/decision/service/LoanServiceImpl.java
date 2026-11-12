package com.loan.decision.service;

import com.loan.decision.exception.CreditScoringException;
import com.loan.decision.exception.DuplicateApplicationException;
import com.loan.decision.exception.InvalidLoanStateException;
import com.loan.decision.exception.LoanNotFoundException;
import com.loan.decision.exception.UserNotFoundException;
import com.loan.decision.model.dto.CreditScoreResult;
import com.loan.decision.model.dto.CreditScoringInput;
import com.loan.decision.model.dto.DecisionInput;
import com.loan.decision.model.dto.DecisionResult;
import com.loan.decision.model.dto.LoanApplicationRequest;
import com.loan.decision.model.dto.LoanApplicationResponse;
import com.loan.decision.model.dto.LoanFilterRequest;
import com.loan.decision.model.dto.RiskAnalysisInput;
import com.loan.decision.model.entity.CreditHistory;
import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.LoanDecision;
import com.loan.decision.model.entity.LoanStatus;
import com.loan.decision.model.entity.RiskLevel;
import com.loan.decision.model.entity.Role;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.CreditHistoryRepository;
import com.loan.decision.repository.LoanApplicationRepository;
import com.loan.decision.repository.UserRepository;
import com.loan.decision.util.DataMaskingUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {

    private static final int MAX_PAGE_SIZE = 20;

    private final LoanApplicationRepository loanApplicationRepository;
    private final UserRepository userRepository;
    private final CreditHistoryRepository creditHistoryRepository;
    private final AuditService auditService;
    private final CreditScoringService creditScoringService;
    private final RiskAnalyzerService riskAnalyzerService;
    private final DecisionEngineService decisionEngineService;
    private final NotificationService notificationService;

    // -------------------------------------------------------------------------
    // Create
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse createApplication(LoanApplicationRequest req, Long customerId) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException(customerId));

        LoanApplication application = LoanApplication.builder()
                .customer(customer)
                .status(LoanStatus.DRAFT)
                .applicantName(req.getApplicantName())
                .idNumber(req.getIdNumber())
                .dateOfBirth(req.getDateOfBirth())
                .gender(req.getGender())
                .address(req.getAddress())
                .monthlyIncome(req.getMonthlyIncome())
                .employmentType(req.getEmploymentType())
                .companyName(req.getCompanyName())
                .currentDebt(req.getCurrentDebt())
                .loanAmount(req.getLoanAmount())
                .loanTerm(req.getLoanTerm())
                .loanPurpose(req.getLoanPurpose())
                .build();

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logAction(
                "CREATE", "LoanApplication",
                customerId, saved.getApplicationId(),
                null, "DRAFT", null);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Submit
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse submitApplication(Long applicationId, Long customerId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.DRAFT) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.DRAFT.name());
        }

        if (!application.getCustomer().getUserId().equals(customerId)) {
            throw new AccessDeniedException("You do not have permission to submit this application");
        }

        boolean duplicate = loanApplicationRepository.existsByIdNumberAndStatusIn(
                application.getIdNumber(),
                Arrays.asList(LoanStatus.SUBMITTED, LoanStatus.PROCESSING));
        if (duplicate) {
            throw new DuplicateApplicationException(application.getIdNumber());
        }

        application.setStatus(LoanStatus.SUBMITTED);
        application.setSubmittedAt(LocalDateTime.now());

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logStatusChange(customerId, applicationId, "DRAFT", "SUBMITTED", null);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Validate (officer)
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse validateApplication(Long applicationId, Long officerId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.SUBMITTED) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.SUBMITTED.name());
        }

        application.setStatus(LoanStatus.PROCESSING);

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logStatusChange(officerId, applicationId, "SUBMITTED", "PROCESSING", null);

        runAutomatedPipeline(saved);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Automated pipeline
    // -------------------------------------------------------------------------

    private void runAutomatedPipeline(LoanApplication application) {
        Long applicationId = application.getApplicationId();
        try {
            // Step 1 – Credit Scoring
            Optional<CreditHistory> historyOpt = creditHistoryRepository.findByCustomer(application.getCustomer());

            CreditScoringInput scoringInput;
            int latePayments = 0;
            int defaults = 0;

            if (historyOpt.isPresent()) {
                CreditHistory history = historyOpt.get();
                latePayments = history.getLatePayments() != null ? history.getLatePayments() : 0;
                defaults = history.getDefaults() != null ? history.getDefaults() : 0;
                scoringInput = CreditScoringInput.builder()
                        .totalLoans(history.getTotalLoans() != null ? history.getTotalLoans() : 0)
                        .activeLoans(history.getActiveLoans() != null ? history.getActiveLoans() : 0)
                        .closedLoans(history.getClosedLoans() != null ? history.getClosedLoans() : 0)
                        .latePayments(latePayments)
                        .defaults(defaults)
                        .currentDebt(application.getCurrentDebt() != null ? application.getCurrentDebt() : BigDecimal.ZERO)
                        .monthlyIncome(application.getMonthlyIncome() != null ? application.getMonthlyIncome() : BigDecimal.ZERO)
                        .creditAgeMonths(history.getCreditAgeMonths() != null ? history.getCreditAgeMonths() : 0)
                        .build();
            } else {
                scoringInput = CreditScoringInput.builder()
                        .totalLoans(0)
                        .activeLoans(0)
                        .closedLoans(0)
                        .latePayments(0)
                        .defaults(0)
                        .currentDebt(BigDecimal.ZERO)
                        .monthlyIncome(BigDecimal.ZERO)
                        .creditAgeMonths(0)
                        .build();
            }

            CreditScoreResult scoreResult = creditScoringService.calculateScore(scoringInput);
            application.setCreditScore(scoreResult.getScore());
            loanApplicationRepository.save(application);
            log.info("Application {} credit score: {}", applicationId, scoreResult.getScore());

            // Step 2 – Risk Analysis
            RiskAnalysisInput riskInput = RiskAnalysisInput.builder()
                    .currentDebt(application.getCurrentDebt() != null ? application.getCurrentDebt() : BigDecimal.ZERO)
                    .loanAmount(application.getLoanAmount())
                    .loanTerm(application.getLoanTerm() != null ? application.getLoanTerm() : 0)
                    .monthlyIncome(application.getMonthlyIncome() != null ? application.getMonthlyIncome() : BigDecimal.ZERO)
                    .latePayments(latePayments)
                    .defaults(defaults)
                    .build();

            RiskLevel riskLevel = riskAnalyzerService.analyzeRisk(riskInput);
            application.setRiskLevel(riskLevel);
            loanApplicationRepository.save(application);

            // Step 3 – Decision
            DecisionInput decisionInput = DecisionInput.builder()
                    .creditScore(application.getCreditScore() != null ? application.getCreditScore() : 0)
                    .riskLevel(application.getRiskLevel())
                    .loanAmount(application.getLoanAmount())
                    .build();

            DecisionResult decisionResult = decisionEngineService.makeDecision(decisionInput);
            application.setDecision(decisionResult.getDecision());
            application.setProcessedAt(decisionResult.getProcessedAt());

            LoanStatus newStatus;
            switch (decisionResult.getDecision()) {
                case APPROVED:
                    newStatus = LoanStatus.APPROVED;
                    break;
                case REJECTED:
                    newStatus = LoanStatus.REJECTED;
                    application.setRejectionReason(decisionResult.getReason());
                    break;
                default:
                    newStatus = LoanStatus.PENDING_REVIEW;
                    break;
            }
            application.setStatus(newStatus);
            loanApplicationRepository.save(application);

            auditService.logStatusChange(null, applicationId, "PROCESSING", newStatus.name(), null);

            // Step 4 – Notification
            if (decisionResult.getDecision() == LoanDecision.APPROVED
                    || decisionResult.getDecision() == LoanDecision.REJECTED) {
                notificationService.sendDecisionNotification(application);
            }

        } catch (Exception e) {
            log.error("Automated pipeline failed for application {}: {}", applicationId, e.getMessage(), e);
            auditService.logAction("PIPELINE_ERROR", "LoanApplication", null, applicationId,
                    null, e.getMessage(), null);
            throw new CreditScoringException("Automated pipeline failed for application " + applicationId, e);
        }
    }

    // -------------------------------------------------------------------------
    // Return (officer sends back to customer)
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse returnApplication(Long applicationId, Long officerId, String note) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.SUBMITTED) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.SUBMITTED.name());
        }

        application.setStatus(LoanStatus.DRAFT);
        application.setRejectionReason(note);

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logStatusChange(officerId, applicationId, "SUBMITTED", "DRAFT", null);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Approve (manager)
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse approveApplication(Long applicationId, Long managerId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.PENDING_REVIEW) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.PENDING_REVIEW.name());
        }

        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new UserNotFoundException(managerId));

        application.setStatus(LoanStatus.APPROVED);
        application.setDecision(LoanDecision.APPROVED);
        application.setApprovedBy(manager);
        application.setProcessedAt(LocalDateTime.now());

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logStatusChange(managerId, applicationId, "PENDING_REVIEW", "APPROVED", null);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Reject (manager)
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse rejectApplication(Long applicationId, Long managerId, String reason) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.PENDING_REVIEW) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.PENDING_REVIEW.name());
        }

        if (reason == null || reason.length() < 10) {
            throw new InvalidLoanStateException("Rejection reason must be at least 10 characters long");
        }

        application.setStatus(LoanStatus.REJECTED);
        application.setDecision(LoanDecision.REJECTED);
        application.setRejectionReason(reason);
        application.setProcessedAt(LocalDateTime.now());

        LoanApplication saved = loanApplicationRepository.save(application);

        auditService.logStatusChange(managerId, applicationId, "PENDING_REVIEW", "REJECTED", null);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Query
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getApplications(LoanFilterRequest filter, Pageable pageable) {
        // Enforce max page size
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable bounded = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        LocalDateTime dateFrom = filter.getFromDate() != null
                ? filter.getFromDate().atStartOfDay() : null;
        LocalDateTime dateTo = filter.getToDate() != null
                ? filter.getToDate().atTime(23, 59, 59) : null;

        Page<LoanApplication> page = loanApplicationRepository.findWithFilters(
                filter.getStatus(),
                dateFrom,
                dateTo,
                filter.getMinAmount(),
                filter.getMaxAmount(),
                bounded);

        return page.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public LoanApplicationResponse getApplicationById(Long applicationId, Long userId, Role role) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (role == Role.CUSTOMER && !application.getCustomer().getUserId().equals(userId)) {
            throw new AccessDeniedException("You do not have permission to view this application");
        }

        return toResponse(application);
    }

    // -------------------------------------------------------------------------
    // Update
    // -------------------------------------------------------------------------

    @Override
    public LoanApplicationResponse updateApplication(Long applicationId, LoanApplicationRequest req, Long customerId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
                .orElseThrow(() -> new LoanNotFoundException(applicationId));

        if (application.getStatus() != LoanStatus.DRAFT) {
            throw new InvalidLoanStateException(application.getStatus().name(), LoanStatus.DRAFT.name());
        }

        if (!application.getCustomer().getUserId().equals(customerId)) {
            throw new AccessDeniedException("You do not have permission to update this application");
        }

        application.setApplicantName(req.getApplicantName());
        application.setIdNumber(req.getIdNumber());
        application.setDateOfBirth(req.getDateOfBirth());
        application.setGender(req.getGender());
        application.setAddress(req.getAddress());
        application.setMonthlyIncome(req.getMonthlyIncome());
        application.setEmploymentType(req.getEmploymentType());
        application.setCompanyName(req.getCompanyName());
        application.setCurrentDebt(req.getCurrentDebt());
        application.setLoanAmount(req.getLoanAmount());
        application.setLoanTerm(req.getLoanTerm());
        application.setLoanPurpose(req.getLoanPurpose());

        LoanApplication saved = loanApplicationRepository.save(application);

        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // My Applications (customer-specific)
    // -------------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public Page<LoanApplicationResponse> getMyApplications(Long customerId, Pageable pageable) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> new UserNotFoundException(customerId));

        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Pageable bounded = PageRequest.of(pageable.getPageNumber(), pageSize, pageable.getSort());

        return loanApplicationRepository.findByCustomer(customer, bounded)
                .map(this::toResponse);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private LoanApplicationResponse toResponse(LoanApplication app) {
        return LoanApplicationResponse.builder()
                .applicationId(app.getApplicationId())
                .applicantName(app.getApplicantName())
                .maskedIdNumber(DataMaskingUtil.maskIdNumber(app.getIdNumber()))
                .customerId(app.getCustomer() != null ? app.getCustomer().getUserId() : null)
                // Personal info
                .dateOfBirth(app.getDateOfBirth())
                .gender(app.getGender())
                .address(app.getAddress())
                // Financial info
                .monthlyIncome(app.getMonthlyIncome())
                .employmentType(app.getEmploymentType())
                .companyName(app.getCompanyName())
                .currentDebt(app.getCurrentDebt())
                // Loan info
                .loanAmount(app.getLoanAmount())
                .loanTerm(app.getLoanTerm())
                .interestRate(app.getInterestRate())
                .loanPurpose(app.getLoanPurpose())
                // Scoring & decision
                .creditScore(app.getCreditScore())
                .riskLevel(app.getRiskLevel())
                .decision(app.getDecision())
                .status(app.getStatus())
                .rejectionReason(app.getRejectionReason())
                // Timestamps
                .submittedAt(app.getSubmittedAt())
                .processedAt(app.getProcessedAt())
                .createdAt(app.getCreatedAt())
                .updatedAt(app.getUpdatedAt())
                .build();
    }
}

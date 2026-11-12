package com.loan.decision.service;

import com.loan.decision.model.dto.LoanApplicationRequest;
import com.loan.decision.model.dto.LoanApplicationResponse;
import com.loan.decision.model.dto.LoanFilterRequest;
import com.loan.decision.model.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for loan application lifecycle operations.
 */
public interface LoanService {

    /**
     * Creates a new loan application in DRAFT status for the given customer.
     */
    LoanApplicationResponse createApplication(LoanApplicationRequest req, Long customerId);

    /**
     * Submits a DRAFT application, transitioning it to SUBMITTED status.
     */
    LoanApplicationResponse submitApplication(Long applicationId, Long customerId);

    /**
     * Validates a SUBMITTED application by a credit officer, transitioning it to PROCESSING.
     */
    LoanApplicationResponse validateApplication(Long applicationId, Long officerId);

    /**
     * Returns a SUBMITTED application back to DRAFT status with an officer note.
     */
    LoanApplicationResponse returnApplication(Long applicationId, Long officerId, String note);

    /**
     * Approves a PENDING_REVIEW application, transitioning it to APPROVED.
     */
    LoanApplicationResponse approveApplication(Long applicationId, Long managerId);

    /**
     * Rejects a PENDING_REVIEW application with a reason, transitioning it to REJECTED.
     */
    LoanApplicationResponse rejectApplication(Long applicationId, Long managerId, String reason);

    /**
     * Returns a paginated, filtered list of loan applications.
     */
    Page<LoanApplicationResponse> getApplications(LoanFilterRequest filter, Pageable pageable);

    /**
     * Retrieves a single loan application by ID, enforcing ownership for CUSTOMER role.
     */
    LoanApplicationResponse getApplicationById(Long applicationId, Long userId, Role role);

    /**
     * Updates a DRAFT application's fields.
     */
    LoanApplicationResponse updateApplication(Long applicationId, LoanApplicationRequest req, Long customerId);

    /**
     * Returns a paginated list of loan applications belonging to the given customer.
     */
    Page<LoanApplicationResponse> getMyApplications(Long customerId, Pageable pageable);
}

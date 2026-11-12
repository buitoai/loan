package com.loan.decision.controller;

import com.loan.decision.model.dto.LoanApplicationRequest;
import com.loan.decision.model.dto.LoanApplicationResponse;
import com.loan.decision.model.dto.LoanFilterRequest;
import com.loan.decision.model.dto.LoanRejectRequest;
import com.loan.decision.model.dto.PagedResponse;
import com.loan.decision.model.entity.Role;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.UserRepository;
import com.loan.decision.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST controller for loan application lifecycle endpoints.
 *
 * <p>All endpoints are secured via {@code @PreAuthorize} annotations that enforce
 * role-based access control. Ownership checks for CUSTOMER-scoped operations are
 * delegated to {@link LoanService}.
 */
@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private Role getCurrentUserRole() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getRole)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // -------------------------------------------------------------------------
    // Endpoints
    // -------------------------------------------------------------------------

    /**
     * Creates a new loan application in DRAFT status for the authenticated customer.
     *
     * @param req the loan application details
     * @return 201 Created with the created application
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> createApplication(
            @Valid @RequestBody LoanApplicationRequest req) {
        LoanApplicationResponse response = loanService.createApplication(req, getCurrentUserId());
        return ResponseEntity.status(201).body(response);
    }

    /**
     * Returns a paginated, filtered list of all loan applications.
     * Accessible by CREDIT_OFFICER and MANAGER roles.
     *
     * @param filter   optional filter criteria
     * @param pageable pagination and sorting parameters
     * @return 200 OK with paged results
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('CREDIT_OFFICER', 'MANAGER')")
    public ResponseEntity<PagedResponse<LoanApplicationResponse>> getApplications(
            @ModelAttribute LoanFilterRequest filter,
            Pageable pageable) {
        Page<LoanApplicationResponse> page = loanService.getApplications(filter, pageable);
        return ResponseEntity.ok(PagedResponse.of(page));
    }

    /**
     * Returns a paginated list of loan applications belonging to the authenticated customer.
     *
     * @param pageable pagination and sorting parameters
     * @return 200 OK with paged results
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PagedResponse<LoanApplicationResponse>> getMyApplications(Pageable pageable) {
        Page<LoanApplicationResponse> page = loanService.getMyApplications(getCurrentUserId(), pageable);
        return ResponseEntity.ok(PagedResponse.of(page));
    }

    /**
     * Retrieves a single loan application by ID.
     * All authenticated roles may call this endpoint; the service enforces ownership for CUSTOMER.
     *
     * @param id the application ID
     * @return 200 OK with the application details
     */
    @GetMapping("/{id}")
    public ResponseEntity<LoanApplicationResponse> getApplicationById(@PathVariable Long id) {
        LoanApplicationResponse response = loanService.getApplicationById(
                id, getCurrentUserId(), getCurrentUserRole());
        return ResponseEntity.ok(response);
    }

    /**
     * Updates a DRAFT loan application for the authenticated customer.
     *
     * @param id  the application ID
     * @param req the updated application details
     * @return 200 OK with the updated application
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody LoanApplicationRequest req) {
        LoanApplicationResponse response = loanService.updateApplication(id, req, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Submits a DRAFT application, transitioning it to SUBMITTED status.
     *
     * @param id the application ID
     * @return 200 OK with the updated application
     */
    @PostMapping("/{id}/submit")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<LoanApplicationResponse> submitApplication(@PathVariable Long id) {
        LoanApplicationResponse response = loanService.submitApplication(id, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates a SUBMITTED application by a credit officer, triggering the automated pipeline.
     *
     * @param id the application ID
     * @return 200 OK with the updated application
     */
    @PostMapping("/{id}/validate")
    @PreAuthorize("hasRole('CREDIT_OFFICER')")
    public ResponseEntity<LoanApplicationResponse> validateApplication(@PathVariable Long id) {
        LoanApplicationResponse response = loanService.validateApplication(id, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Returns a SUBMITTED application back to DRAFT status with an officer note.
     *
     * @param id   the application ID
     * @param body request body containing the "note" key
     * @return 200 OK with the updated application
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('CREDIT_OFFICER')")
    public ResponseEntity<LoanApplicationResponse> returnApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        LoanApplicationResponse response = loanService.returnApplication(id, getCurrentUserId(), body.get("note"));
        return ResponseEntity.ok(response);
    }

    /**
     * Approves a PENDING_REVIEW application, transitioning it to APPROVED.
     *
     * @param id the application ID
     * @return 200 OK with the updated application
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanApplicationResponse> approveApplication(@PathVariable Long id) {
        LoanApplicationResponse response = loanService.approveApplication(id, getCurrentUserId());
        return ResponseEntity.ok(response);
    }

    /**
     * Rejects a PENDING_REVIEW application with a reason, transitioning it to REJECTED.
     *
     * @param id  the application ID
     * @param req the rejection details including the reason
     * @return 200 OK with the updated application
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<LoanApplicationResponse> rejectApplication(
            @PathVariable Long id,
            @Valid @RequestBody LoanRejectRequest req) {
        LoanApplicationResponse response = loanService.rejectApplication(
                id, getCurrentUserId(), req.getRejectionReason());
        return ResponseEntity.ok(response);
    }
}

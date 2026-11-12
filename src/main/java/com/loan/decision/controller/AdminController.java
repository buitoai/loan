package com.loan.decision.controller;

import com.loan.decision.model.dto.ConfigUpdateRequest;
import com.loan.decision.model.dto.PagedResponse;
import com.loan.decision.model.entity.AuditLog;
import com.loan.decision.model.entity.SystemConfig;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.AuditLogRepository;
import com.loan.decision.repository.SystemConfigRepository;
import com.loan.decision.repository.UserRepository;
import com.loan.decision.service.ConfigService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for admin-only operations.
 *
 * <p>All endpoints require the {@code ADMIN} role, enforced via
 * {@code @PreAuthorize("hasRole('ADMIN')")} at the class level.
 * Covers system configuration management, audit log inspection, and user listing.
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ConfigService configService;
    private final SystemConfigRepository systemConfigRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    /**
     * Resolves the user ID of the currently authenticated principal.
     *
     * @return the {@code userId} of the authenticated user
     * @throws UsernameNotFoundException if the principal cannot be found in the database
     */
    private Long getCurrentUserId() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(User::getUserId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    // -------------------------------------------------------------------------
    // System Configuration
    // -------------------------------------------------------------------------

    /**
     * Returns the full list of system configuration entries.
     *
     * <p>Supports Requirement 10.1: ADMIN can read all system configuration parameters.
     *
     * @return 200 OK with the list of {@link SystemConfig} entities
     */
    @GetMapping("/configs")
    public ResponseEntity<List<SystemConfig>> getAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        return ResponseEntity.ok(configs);
    }

    /**
     * Updates a single system configuration entry identified by its key.
     *
     * <p>Delegates validation and persistence to {@link ConfigService#updateConfig},
     * which enforces threshold constraints and type correctness before saving.
     * The authenticated admin's ID is recorded as the updater.
     *
     * <p>Supports Requirements 10.1, 10.3, 10.4, 10.5.
     *
     * @param key     the configuration key (path variable)
     * @param request the new value wrapped in a {@link ConfigUpdateRequest}
     * @return 200 OK with the updated {@link SystemConfig} entity
     */
    @PutMapping("/configs/{key}")
    public ResponseEntity<SystemConfig> updateConfig(
            @PathVariable String key,
            @Valid @RequestBody ConfigUpdateRequest request) {

        Long adminId = getCurrentUserId();
        configService.updateConfig(key, request.getConfigValue(), adminId);

        // Return the freshly persisted entity so the caller sees the updated state
        SystemConfig updated = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new IllegalStateException("Config key not found after update: " + key));
        return ResponseEntity.ok(updated);
    }

    // -------------------------------------------------------------------------
    // Audit Logs
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of audit log entries with optional filters.
     *
     * <p>All filter parameters are optional:
     * <ul>
     *   <li>{@code fromDate} — lower bound of {@code createdAt} (inclusive)</li>
     *   <li>{@code toDate}   — upper bound of {@code createdAt} (inclusive)</li>
     *   <li>{@code action}   — exact match on the action field</li>
     *   <li>{@code username} — exact match on the associated user's username</li>
     * </ul>
     *
     * <p>Supports Requirement 11.6: ADMIN can search audit logs by time range, action, and username.
     *
     * @param fromDate optional start of the time range (ISO date-time format)
     * @param toDate   optional end of the time range (ISO date-time format)
     * @param action   optional action type filter
     * @param username optional username filter
     * @param pageable pagination and sorting parameters
     * @return 200 OK with a paged list of {@link AuditLog} entries
     */
    @GetMapping("/audit-logs")
    public ResponseEntity<PagedResponse<AuditLog>> getAuditLogs(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            Pageable pageable) {

        Page<AuditLog> page = auditLogRepository.findWithFilters(
                username, action, fromDate, toDate, pageable);
        return ResponseEntity.ok(PagedResponse.of(page));
    }

    // -------------------------------------------------------------------------
    // User Management
    // -------------------------------------------------------------------------

    /**
     * Returns a paginated list of all users in the system.
     *
     * <p>Supports Requirement 1.7: ADMIN can view and manage all user accounts.
     *
     * @param pageable pagination and sorting parameters
     * @return 200 OK with a paged list of {@link User} entities
     */
    @GetMapping("/users")
    public ResponseEntity<PagedResponse<User>> getAllUsers(Pageable pageable) {
        Page<User> page = userRepository.findAll(pageable);
        return ResponseEntity.ok(PagedResponse.of(page));
    }
}

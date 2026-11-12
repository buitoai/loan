package com.loan.decision.service;

import com.loan.decision.model.entity.AuditLog;
import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.AuditLogRepository;
import com.loan.decision.repository.LoanApplicationRepository;
import com.loan.decision.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Asynchronous implementation of {@link AuditService}.
 *
 * <p>Every method is annotated with {@link Async} so Spring executes it on a
 * dedicated thread pool (configured via {@code @EnableAsync} in a config class).
 * All exceptions are caught internally and logged — they are never propagated to
 * the caller, keeping audit logging transparent to business logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final LoanApplicationRepository loanApplicationRepository;

    // -------------------------------------------------------------------------
    // AuditService implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Creates an {@link AuditLog} with action {@code STATUS_CHANGE} and entity
     * {@code LoanApplication}. The old and new status values are stored as plain
     * JSON strings (e.g. {@code "PENDING"}).
     */
    @Async
    @Override
    public void logStatusChange(Long userId, Long applicationId,
                                String oldStatus, String newStatus, String ipAddress) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .action("STATUS_CHANGE")
                    .entity("LoanApplication")
                    .oldValue(toJsonString(oldStatus))
                    .newValue(toJsonString(newStatus))
                    .ipAddress(ipAddress);

            resolveUser(userId, builder);
            resolveApplication(applicationId, builder);

            auditLogRepository.save(builder.build());

            log.debug("AuditLog saved: STATUS_CHANGE applicationId={} {} -> {}",
                    applicationId, oldStatus, newStatus);
        } catch (Exception ex) {
            log.error("Failed to save audit log [STATUS_CHANGE] userId={} applicationId={}: {}",
                    userId, applicationId, ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates an {@link AuditLog} with action {@code LOGIN_SUCCESS} or
     * {@code LOGIN_FAILED} and entity {@code User}. The username is stored in
     * {@code newValue}.
     */
    @Async
    @Override
    public void logLogin(String username, boolean success, String ipAddress) {
        try {
            String action = success ? "LOGIN_SUCCESS" : "LOGIN_FAILED";

            AuditLog auditLog = AuditLog.builder()
                    .action(action)
                    .entity("User")
                    .newValue(username)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);

            log.debug("AuditLog saved: {} username={}", action, username);
        } catch (Exception ex) {
            log.error("Failed to save audit log [LOGIN] username={} success={}: {}",
                    username, success, ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Creates an {@link AuditLog} with action {@code CONFIG_CHANGE} and entity
     * {@code SystemConfig}.
     */
    @Async
    @Override
    public void logConfigChange(Long userId, String configKey, String oldValue, String newValue) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .action("CONFIG_CHANGE")
                    .entity("SystemConfig")
                    .oldValue(oldValue)
                    .newValue(newValue);

            resolveUser(userId, builder);

            auditLogRepository.save(builder.build());

            log.debug("AuditLog saved: CONFIG_CHANGE key={} userId={}", configKey, userId);
        } catch (Exception ex) {
            log.error("Failed to save audit log [CONFIG_CHANGE] userId={} configKey={}: {}",
                    userId, configKey, ex.getMessage(), ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generic method that creates and saves an {@link AuditLog} with all
     * provided fields.
     */
    @Async
    @Override
    public void logAction(String action, String entity, Long userId, Long applicationId,
                          String oldValue, String newValue, String ipAddress) {
        try {
            AuditLog.AuditLogBuilder builder = AuditLog.builder()
                    .action(action)
                    .entity(entity)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress);

            resolveUser(userId, builder);
            resolveApplication(applicationId, builder);

            auditLogRepository.save(builder.build());

            log.debug("AuditLog saved: action={} entity={} userId={} applicationId={}",
                    action, entity, userId, applicationId);
        } catch (Exception ex) {
            log.error("Failed to save audit log [{}] entity={} userId={} applicationId={}: {}",
                    action, entity, userId, applicationId, ex.getMessage(), ex);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Loads the {@link User} for the given {@code userId} and sets it on the builder.
     * If {@code userId} is null or the user is not found, the field is left unset.
     */
    private void resolveUser(Long userId, AuditLog.AuditLogBuilder builder) {
        if (userId == null) {
            return;
        }
        userRepository.findById(userId).ifPresent(builder::user);
    }

    /**
     * Loads the {@link LoanApplication} for the given {@code applicationId} and sets
     * it on the builder. If {@code applicationId} is null or not found, the field is
     * left unset.
     */
    private void resolveApplication(Long applicationId, AuditLog.AuditLogBuilder builder) {
        if (applicationId == null) {
            return;
        }
        loanApplicationRepository.findById(applicationId).ifPresent(builder::application);
    }

    /**
     * Wraps a plain string value in JSON quotes so it can be stored in a JSON column.
     * Returns {@code null} if the input is {@code null}.
     *
     * <p>Example: {@code "PENDING"} → {@code "\"PENDING\""}
     */
    private String toJsonString(String value) {
        if (value == null) {
            return null;
        }
        // Escape any embedded double-quotes to produce valid JSON
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}

package com.loan.decision.service;

/**
 * Service interface for recording audit events asynchronously.
 *
 * <p>All methods are fire-and-forget: implementations run in a separate thread pool
 * and must never propagate exceptions to the caller.
 */
public interface AuditService {

    /**
     * Records a loan-application status change.
     *
     * @param userId        the ID of the user who triggered the change (may be null)
     * @param applicationId the ID of the affected loan application (may be null)
     * @param oldStatus     the previous status value
     * @param newStatus     the new status value
     * @param ipAddress     the originating IP address (may be null)
     */
    void logStatusChange(Long userId, Long applicationId, String oldStatus, String newStatus, String ipAddress);

    /**
     * Records a login attempt.
     *
     * @param username  the username that attempted to log in
     * @param success   {@code true} if the login succeeded, {@code false} otherwise
     * @param ipAddress the originating IP address (may be null)
     */
    void logLogin(String username, boolean success, String ipAddress);

    /**
     * Records a system-configuration change.
     *
     * @param userId    the ID of the user who made the change (may be null)
     * @param configKey the configuration key that was changed
     * @param oldValue  the previous value
     * @param newValue  the new value
     */
    void logConfigChange(Long userId, String configKey, String oldValue, String newValue);

    /**
     * Generic audit-log entry for any action.
     *
     * @param action        the action name (e.g. "APPROVE", "REJECT")
     * @param entity        the entity type affected (e.g. "LoanApplication")
     * @param userId        the ID of the acting user (may be null)
     * @param applicationId the ID of the related loan application (may be null)
     * @param oldValue      the previous value / state (may be null)
     * @param newValue      the new value / state (may be null)
     * @param ipAddress     the originating IP address (may be null)
     */
    void logAction(String action, String entity, Long userId, Long applicationId,
                   String oldValue, String newValue, String ipAddress);
}

package com.loan.decision.service;

import com.loan.decision.model.entity.LoanApplication;

/**
 * Service interface for managing loan decision notifications.
 *
 * <p>Handles sending email notifications to customers when a loan decision
 * is made, and provides retry logic for failed notifications.
 */
public interface NotificationService {

    /**
     * Creates a notification record and sends an email to the customer
     * informing them of the loan decision.
     *
     * @param application the loan application whose decision should be communicated
     */
    void sendDecisionNotification(LoanApplication application);

    /**
     * Retries sending a previously failed notification.
     * Only retries if the notification is in FAILED status and has fewer than 3 retry attempts.
     *
     * @param notificationId the ID of the notification to retry
     */
    void retrySendNotification(Long notificationId);
}

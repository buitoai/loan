package com.loan.decision.service;

import com.loan.decision.model.entity.Notification;
import com.loan.decision.model.entity.NotificationStatus;
import com.loan.decision.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Scheduled job that retries failed notifications.
 *
 * <p>Runs every 5 minutes and picks up any {@link Notification} records in
 * {@link NotificationStatus#FAILED} state that have been attempted fewer than
 * 3 times, delegating the actual retry logic to {@link NotificationService}.
 */
@Component
public class NotificationRetryScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationRetryScheduler.class);

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    public NotificationRetryScheduler(NotificationRepository notificationRepository,
                                      NotificationService notificationService) {
        this.notificationRepository = notificationRepository;
        this.notificationService = notificationService;
    }

    /**
     * Queries for FAILED notifications with {@code retryCount < 3} and retries each one.
     * Runs with a fixed delay of 5 minutes (300,000 ms) between the end of the last
     * execution and the start of the next.
     */
    @Scheduled(fixedDelay = 300000)
    public void retryFailedNotifications() {
        List<Notification> failedNotifications =
                notificationRepository.findByStatusAndRetryCountLessThan(
                        NotificationStatus.FAILED, MAX_RETRY_COUNT);

        if (failedNotifications.isEmpty()) {
            log.debug("No failed notifications eligible for retry.");
            return;
        }

        log.info("Retrying {} failed notification(s).", failedNotifications.size());

        for (Notification notification : failedNotifications) {
            try {
                notificationService.retrySendNotification(notification.getNotificationId());
            } catch (Exception e) {
                log.error("Error retrying notification id={}: {}",
                        notification.getNotificationId(), e.getMessage(), e);
            }
        }

        log.info("Finished retrying {} failed notification(s).", failedNotifications.size());
    }
}

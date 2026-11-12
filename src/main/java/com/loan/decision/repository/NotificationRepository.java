package com.loan.decision.repository;

import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.Notification;
import com.loan.decision.model.entity.NotificationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific loan application with a given status.
     *
     * @param application the loan application
     * @param status      the notification status to filter by
     * @return list of matching notifications
     */
    List<Notification> findByApplicationAndStatus(LoanApplication application, NotificationStatus status);

    /**
     * Find all notifications with a given status whose retry count is less than the specified value.
     * Used by the retry scheduler to find FAILED notifications eligible for retry (retryCount < 3).
     *
     * @param status     the notification status (typically FAILED)
     * @param retryCount the upper bound (exclusive) for retry count
     * @return list of matching notifications
     */
    List<Notification> findByStatusAndRetryCountLessThan(NotificationStatus status, int retryCount);
}

package com.loan.decision.service;

import com.loan.decision.model.entity.LoanApplication;
import com.loan.decision.model.entity.Notification;
import com.loan.decision.model.entity.NotificationStatus;
import com.loan.decision.model.entity.NotificationType;
import com.loan.decision.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of {@link NotificationService} that sends email notifications
 * via {@link JavaMailSender} and persists notification records to the database.
 *
 * <p>Each notification goes through the following lifecycle:
 * <ol>
 *   <li>A {@link Notification} record is created with status {@code PENDING}.</li>
 *   <li>An email is sent via {@link JavaMailSender}.</li>
 *   <li>On success the status is updated to {@code SENT}; on failure to {@code FAILED}.</li>
 * </ol>
 *
 * <p>Failed notifications can be retried up to 3 times via
 * {@link #retrySendNotification(Long)}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private static final int MAX_RETRY_COUNT = 3;

    private final NotificationRepository notificationRepository;
    private final JavaMailSender mailSender;

    // -------------------------------------------------------------------------
    // NotificationService implementation
    // -------------------------------------------------------------------------

    /**
     * {@inheritDoc}
     *
     * <p>Creates a {@link Notification} entity with status {@code PENDING}, saves it,
     * then attempts to send an email. Updates the status to {@code SENT} or
     * {@code FAILED} depending on the outcome.
     */
    @Override
    public void sendDecisionNotification(LoanApplication application) {
        String subject = "Kết quả xét duyệt hồ sơ vay #" + application.getApplicationId();
        String content = buildEmailContent(application);

        // Create and persist the notification record in PENDING state
        Notification notification = Notification.builder()
                .user(application.getCustomer())
                .application(application)
                .type(NotificationType.EMAIL)
                .subject(subject)
                .content(content)
                .status(NotificationStatus.PENDING)
                .retryCount(0)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created [id={}] for application [id={}]",
                notification.getNotificationId(), application.getApplicationId());

        // Attempt to send the email
        doSendEmail(notification, application.getCustomer().getEmail(), subject, content);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Loads the notification, validates that it is eligible for retry
     * ({@code status == FAILED} and {@code retryCount < 3}), increments the retry
     * counter, and attempts to resend the email.
     */
    @Override
    public void retrySendNotification(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElse(null);

        if (notification == null) {
            log.warn("retrySendNotification: notification [id={}] not found — skipping", notificationId);
            return;
        }

        if (notification.getRetryCount() >= MAX_RETRY_COUNT) {
            log.warn("retrySendNotification: notification [id={}] has reached max retries ({}) — skipping",
                    notificationId, MAX_RETRY_COUNT);
            return;
        }

        if (notification.getStatus() != NotificationStatus.FAILED) {
            log.warn("retrySendNotification: notification [id={}] is not in FAILED status (current: {}) — skipping",
                    notificationId, notification.getStatus());
            return;
        }

        // Increment retry counter before attempting
        notification.setRetryCount(notification.getRetryCount() + 1);
        log.info("Retrying notification [id={}], attempt #{}", notificationId, notification.getRetryCount());

        String recipientEmail = notification.getUser().getEmail();
        doSendEmail(notification, recipientEmail, notification.getSubject(), notification.getContent());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    /**
     * Attempts to send an email and updates the notification status accordingly.
     * The notification is saved regardless of the outcome.
     *
     * @param notification   the notification entity to update
     * @param recipientEmail the destination email address
     * @param subject        the email subject
     * @param content        the email body text
     */
    private void doSendEmail(Notification notification,
                             String recipientEmail,
                             String subject,
                             String content) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured — marking notification [id={}] as FAILED",
                    notification.getNotificationId());
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(recipientEmail);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);

            notification.setStatus(NotificationStatus.SENT);
            notification.setSentAt(LocalDateTime.now());
            notificationRepository.save(notification);

            log.info("Email sent successfully for notification [id={}] to [{}]",
                    notification.getNotificationId(), recipientEmail);

        } catch (Exception ex) {
            notification.setStatus(NotificationStatus.FAILED);
            notificationRepository.save(notification);

            log.error("Failed to send email for notification [id={}] to [{}]: {}",
                    notification.getNotificationId(), recipientEmail, ex.getMessage(), ex);
        }
    }

    /**
     * Builds a human-readable email body for a loan decision notification.
     *
     * @param app the loan application
     * @return formatted email content string
     */
    private String buildEmailContent(LoanApplication app) {
        String decisionText = app.getDecision() != null ? app.getDecision().name() : "ĐANG XỬ LÝ";
        String nextSteps;

        if (app.getDecision() != null) {
            switch (app.getDecision()) {
                case APPROVED:
                    nextSteps = "Hồ sơ của bạn đã được phê duyệt. Vui lòng liên hệ chi nhánh gần nhất "
                            + "để hoàn tất thủ tục giải ngân trong vòng 7 ngày làm việc.";
                    break;
                case REJECTED:
                    nextSteps = "Rất tiếc, hồ sơ của bạn không đáp ứng đủ điều kiện xét duyệt lần này. "
                            + "Bạn có thể nộp lại hồ sơ sau 6 tháng hoặc liên hệ tư vấn viên để được hỗ trợ.";
                    break;
                default:
                    nextSteps = "Hồ sơ của bạn đang được xem xét. Chúng tôi sẽ thông báo kết quả sớm nhất.";
            }
        } else {
            nextSteps = "Hồ sơ của bạn đang được xem xét. Chúng tôi sẽ thông báo kết quả sớm nhất.";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Kính gửi Quý khách %s,%n%n",
                app.getApplicantName() != null ? app.getApplicantName() : "Quý khách"));
        sb.append(String.format("Chúng tôi xin thông báo kết quả xét duyệt hồ sơ vay của bạn:%n%n"));
        sb.append(String.format("  - Mã hồ sơ    : #%d%n", app.getApplicationId()));
        sb.append(String.format("  - Kết quả     : %s%n", decisionText));
        sb.append(String.format("  - Số tiền vay : %,.0f VND%n", app.getLoanAmount()));
        sb.append(String.format("  - Thời hạn vay: %d tháng%n", app.getLoanTerm()));
        if (app.getInterestRate() != null) {
            sb.append(String.format("  - Lãi suất    : %.2f%%%n", app.getInterestRate()));
        }
        sb.append(String.format("%nBước tiếp theo:%n%s%n%n", nextSteps));
        sb.append(String.format(
                "Nếu có thắc mắc, vui lòng liên hệ hotline 1800-xxxx hoặc email support@loandecision.vn.%n%n"
                        + "Trân trọng,%n"
                        + "Đội ngũ Xét duyệt Vay vốn"));
        return sb.toString();
    }
}

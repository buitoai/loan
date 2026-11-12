package com.loan.decision.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks failed login attempts per username and enforces account lockout.
 *
 * <p>Security policy (Requirement 12.3):
 * <ul>
 *   <li>After <strong>5 failed attempts within a 15-minute window</strong> the account is
 *       locked for <strong>30 minutes</strong>.</li>
 *   <li>A successful login clears the failure record immediately.</li>
 *   <li>Expired locks are cleared lazily on the next {@link #isLocked(String)} call.</li>
 * </ul>
 *
 * <p>All state is held in a {@link ConcurrentHashMap}; no external cache dependency is required.
 * Mutations use {@link ConcurrentHashMap#compute} to guarantee atomicity without explicit
 * synchronization.
 */
@Service
public class LoginAttemptService {

    /** Maximum number of consecutive failures before the account is locked. */
    static final int MAX_ATTEMPTS = 5;

    /** Length of the sliding window in which failures are counted (15 minutes). */
    static final long ATTEMPT_WINDOW_MINUTES = 15;

    /** Duration of the lockout once the threshold is exceeded (30 minutes). */
    static final long LOCKOUT_DURATION_MINUTES = 30;

    /**
     * Immutable snapshot of the login-attempt state for a single username.
     *
     * @param failedAttempts number of consecutive failures recorded in the current window
     * @param firstFailedAt  timestamp of the first failure in the current window;
     *                       {@code null} when there are no recorded failures
     * @param lockedUntil    timestamp until which the account is locked;
     *                       {@code null} when the account is not locked
     */
    record LoginAttemptRecord(int failedAttempts, Instant firstFailedAt, Instant lockedUntil) {

        /** Convenience factory for a brand-new first failure. */
        static LoginAttemptRecord firstFailure(Instant now) {
            return new LoginAttemptRecord(1, now, null);
        }
    }

    private final ConcurrentHashMap<String, LoginAttemptRecord> attempts =
            new ConcurrentHashMap<>();

    /**
     * Records a failed login attempt for the given username.
     *
     * <p>Behaviour:
     * <ol>
     *   <li>If no prior record exists, or the previous window has expired, a fresh record is
     *       created with {@code failedAttempts = 1}.</li>
     *   <li>Otherwise the counter is incremented.</li>
     *   <li>When the counter reaches {@value #MAX_ATTEMPTS} and all failures fall within the
     *       {@value #ATTEMPT_WINDOW_MINUTES}-minute window, {@code lockedUntil} is set to
     *       {@code now + }{@value #LOCKOUT_DURATION_MINUTES} minutes.</li>
     * </ol>
     *
     * @param username the username that failed authentication (never {@code null})
     */
    public void recordFailedAttempt(String username) {
        Instant now = Instant.now();
        attempts.compute(username, (key, existing) -> {
            // No prior record → start a fresh window
            if (existing == null) {
                return LoginAttemptRecord.firstFailure(now);
            }

            // Window has expired → reset and start fresh
            Instant windowStart = existing.firstFailedAt();
            if (windowStart != null &&
                    now.isAfter(windowStart.plusSeconds(ATTEMPT_WINDOW_MINUTES * 60))) {
                return LoginAttemptRecord.firstFailure(now);
            }

            int newCount = existing.failedAttempts() + 1;
            Instant lockedUntil = existing.lockedUntil();

            // Lock the account when the threshold is reached within the window
            if (newCount >= MAX_ATTEMPTS && lockedUntil == null) {
                lockedUntil = now.plusSeconds(LOCKOUT_DURATION_MINUTES * 60);
            }

            return new LoginAttemptRecord(newCount, existing.firstFailedAt(), lockedUntil);
        });
    }

    /**
     * Returns {@code true} if the account is currently locked.
     *
     * <p>An expired lock is cleared lazily: if {@code lockedUntil} is in the past the entry is
     * removed from the map and {@code false} is returned.
     *
     * @param username the username to check (never {@code null})
     * @return {@code true} when the account is locked and the lockout period has not yet elapsed
     */
    public boolean isLocked(String username) {
        Instant now = Instant.now();
        LoginAttemptRecord record = attempts.get(username);

        if (record == null || record.lockedUntil() == null) {
            return false;
        }

        if (now.isBefore(record.lockedUntil())) {
            return true;
        }

        // Lock has expired — remove the stale entry
        attempts.remove(username, record);
        return false;
    }

    /**
     * Clears all failure state for the given username after a successful login.
     *
     * @param username the username that authenticated successfully (never {@code null})
     */
    public void recordSuccessfulLogin(String username) {
        attempts.remove(username);
    }
}

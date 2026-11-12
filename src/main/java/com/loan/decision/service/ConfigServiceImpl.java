package com.loan.decision.service;

import com.loan.decision.exception.ConfigValidationException;
import com.loan.decision.exception.UserNotFoundException;
import com.loan.decision.model.entity.SystemConfig;
import com.loan.decision.model.entity.User;
import com.loan.decision.repository.SystemConfigRepository;
import com.loan.decision.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of {@link ConfigService} that reads/writes {@link SystemConfig} entities
 * and maintains an in-memory cache backed by a {@link ConcurrentHashMap}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private static final String APPROVE_SCORE_THRESHOLD = "APPROVE_SCORE_THRESHOLD";
    private static final String REJECT_SCORE_THRESHOLD  = "REJECT_SCORE_THRESHOLD";

    private final SystemConfigRepository systemConfigRepository;
    private final UserRepository userRepository;

    /** In-memory cache: configKey → configValue */
    private final ConcurrentHashMap<String, String> configCache = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Loads all config entries from the database into the in-memory cache on startup.
     */
    @PostConstruct
    public void loadAllConfigs() {
        List<SystemConfig> configs = systemConfigRepository.findAll();
        configs.forEach(c -> configCache.put(c.getConfigKey(), c.getConfigValue()));
        log.info("ConfigServiceImpl: loaded {} config entries into cache", configs.size());
    }

    // -------------------------------------------------------------------------
    // Read operations
    // -------------------------------------------------------------------------

    /**
     * Returns the config value for the given key.
     * Checks the cache first; on a cache miss, queries the DB and populates the cache.
     */
    @Override
    public String getConfig(String key) {
        String cached = configCache.get(key);
        if (cached != null) {
            return cached;
        }

        // Cache miss — load from DB
        return systemConfigRepository.findByConfigKey(key)
                .map(config -> {
                    configCache.put(config.getConfigKey(), config.getConfigValue());
                    return config.getConfigValue();
                })
                .orElse(null);
    }

    /**
     * Returns the config value parsed as a {@link Long}.
     *
     * @throws NumberFormatException if the stored value cannot be parsed as Long
     */
    @Override
    public Long getConfigAsLong(String key) {
        String value = getConfig(key);
        if (value == null) {
            return null;
        }
        return Long.parseLong(value.trim());
    }

    // -------------------------------------------------------------------------
    // Write operations
    // -------------------------------------------------------------------------

    /**
     * Validates, persists, and caches an updated config value.
     *
     * @throws ConfigValidationException if the key does not exist in the DB or validation fails
     * @throws UserNotFoundException     if {@code updatedBy} does not match any user
     */
    @Override
    @Transactional
    public void updateConfig(String key, String value, Long updatedBy) {
        // 1. Validate before touching the DB
        validateConfig(key, value);

        // 2. Locate the existing record
        SystemConfig config = systemConfigRepository.findByConfigKey(key)
                .orElseThrow(() -> new ConfigValidationException(
                        "Config key not found: " + key));

        // 3. Resolve the user performing the update
        User user = userRepository.findById(updatedBy)
                .orElseThrow(() -> new UserNotFoundException(
                        "User not found with id: " + updatedBy));

        // 4. Apply changes
        config.setConfigValue(value);
        config.setUpdatedBy(user);
        config.setUpdatedAt(LocalDateTime.now());

        // 5. Persist
        systemConfigRepository.save(config);

        // 6. Refresh cache
        configCache.put(key, value);

        log.info("Config updated: key={}, newValue={}, updatedBy={}", key, value, updatedBy);
    }

    // -------------------------------------------------------------------------
    // Validation
    // -------------------------------------------------------------------------

    /**
     * Validates a proposed config value.
     *
     * <p>For {@code APPROVE_SCORE_THRESHOLD} and {@code REJECT_SCORE_THRESHOLD} the method
     * enforces the invariant {@code approveThreshold > rejectThreshold}.
     *
     * <p>For all other keys the method verifies that the value is parseable as the
     * {@code dataType} declared in the stored {@link SystemConfig} record.
     *
     * @throws ConfigValidationException if any constraint is violated
     */
    @Override
    public void validateConfig(String key, String value) {
        if (value == null || value.isBlank()) {
            throw new ConfigValidationException("Config value must not be blank for key: " + key);
        }

        if (APPROVE_SCORE_THRESHOLD.equals(key)) {
            long newApprove = parseAsLongOrThrow(value, key);
            Long currentReject = getConfigAsLong(REJECT_SCORE_THRESHOLD);
            if (currentReject != null && newApprove <= currentReject) {
                throw new ConfigValidationException(
                        String.format(
                                "APPROVE_SCORE_THRESHOLD (%d) must be greater than REJECT_SCORE_THRESHOLD (%d)",
                                newApprove, currentReject));
            }
            return;
        }

        if (REJECT_SCORE_THRESHOLD.equals(key)) {
            long newReject = parseAsLongOrThrow(value, key);
            Long currentApprove = getConfigAsLong(APPROVE_SCORE_THRESHOLD);
            if (currentApprove != null && newReject >= currentApprove) {
                throw new ConfigValidationException(
                        String.format(
                                "REJECT_SCORE_THRESHOLD (%d) must be less than APPROVE_SCORE_THRESHOLD (%d)",
                                newReject, currentApprove));
            }
            return;
        }

        // General type validation based on the dataType stored in the DB
        systemConfigRepository.findByConfigKey(key).ifPresent(config -> {
            String dataType = config.getDataType();
            if (dataType == null) {
                return;
            }
            switch (dataType.toUpperCase()) {
                case "INTEGER" -> {
                    try {
                        Integer.parseInt(value.trim());
                    } catch (NumberFormatException e) {
                        throw new ConfigValidationException(
                                String.format("Value '%s' is not a valid INTEGER for key: %s", value, key));
                    }
                }
                case "LONG" -> {
                    try {
                        Long.parseLong(value.trim());
                    } catch (NumberFormatException e) {
                        throw new ConfigValidationException(
                                String.format("Value '%s' is not a valid LONG for key: %s", value, key));
                    }
                }
                case "BOOLEAN" -> {
                    String lower = value.trim().toLowerCase();
                    if (!"true".equals(lower) && !"false".equals(lower)) {
                        throw new ConfigValidationException(
                                String.format("Value '%s' is not a valid BOOLEAN for key: %s (expected 'true' or 'false')", value, key));
                    }
                }
                case "DOUBLE" -> {
                    try {
                        Double.parseDouble(value.trim());
                    } catch (NumberFormatException e) {
                        throw new ConfigValidationException(
                                String.format("Value '%s' is not a valid DOUBLE for key: %s", value, key));
                    }
                }
                // STRING and unknown types: no further validation needed
                default -> { /* no-op */ }
            }
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private long parseAsLongOrThrow(String value, String key) {
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new ConfigValidationException(
                    String.format("Value '%s' is not a valid numeric value for key: %s", value, key));
        }
    }
}

package com.loan.decision.service;

/**
 * Service interface for managing system configuration.
 * Provides read/write access to SystemConfig entities with in-memory caching.
 */
public interface ConfigService {

    /**
     * Retrieve a config value by key.
     * Returns from cache if available; otherwise loads from DB and caches it.
     *
     * @param key the config key
     * @return the config value as String, or null if not found
     */
    String getConfig(String key);

    /**
     * Retrieve a config value parsed as Long.
     *
     * @param key the config key
     * @return the config value as Long, or null if not found
     * @throws NumberFormatException if the value cannot be parsed as Long
     */
    Long getConfigAsLong(String key);

    /**
     * Update a config value in DB and cache.
     * Validates the new value before persisting.
     *
     * @param key       the config key
     * @param value     the new value
     * @param updatedBy the ID of the user performing the update
     * @throws com.loan.decision.exception.ConfigValidationException if validation fails or key not found
     */
    void updateConfig(String key, String value, Long updatedBy);

    /**
     * Validate a config value for a given key.
     * Enforces threshold constraints and type correctness.
     *
     * @param key   the config key
     * @param value the value to validate
     * @throws com.loan.decision.exception.ConfigValidationException if validation fails
     */
    void validateConfig(String key, String value);
}

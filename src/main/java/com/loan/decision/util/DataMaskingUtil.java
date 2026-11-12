package com.loan.decision.util;

/**
 * Utility class for masking sensitive data before logging or displaying.
 */
public final class DataMaskingUtil {

    private DataMaskingUtil() {
        // Utility class — no instantiation
    }

    /**
     * Masks an ID number by replacing all characters except the last 4 with '*'.
     *
     * <p>Examples:
     * <ul>
     *   <li>{@code "123456789012"} → {@code "********9012"}</li>
     *   <li>{@code "1234"}         → {@code "1234"} (length ≤ 4, returned as-is)</li>
     *   <li>{@code null}           → {@code null}</li>
     *   <li>{@code "  "}           → {@code "  "} (blank, returned as-is)</li>
     * </ul>
     *
     * @param idNumber the ID number string to mask
     * @return the masked string, or the original value if null/blank/length ≤ 4
     */
    public static String maskIdNumber(String idNumber) {
        if (idNumber == null || idNumber.isBlank()) {
            return idNumber;
        }

        int length = idNumber.length();
        if (length <= 4) {
            return idNumber;
        }

        int maskCount = length - 4;
        String masked = "*".repeat(maskCount);
        String lastFour = idNumber.substring(maskCount);
        return masked + lastFour;
    }
}

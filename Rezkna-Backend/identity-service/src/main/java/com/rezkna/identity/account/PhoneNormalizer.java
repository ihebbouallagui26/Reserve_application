package com.rezkna.identity.account;

/**
 * Normalizes phone numbers to a consistent international form so the same physical
 * number always matches on login regardless of how it was typed. Tunisian mobile
 * numbers are the only case handled explicitly (8 local digits -> 216-prefixed);
 * anything already carrying a country code is left as-is once non-digit characters
 * and a leading international "00" are stripped.
 */
final class PhoneNormalizer {

    private static final String TUNISIA_COUNTRY_CODE = "216";
    private static final int LOCAL_NUMBER_LENGTH = 8;

    private PhoneNormalizer() {
    }

    static String normalize(String rawPhone) {
        if (rawPhone == null) {
            return null;
        }
        String digits = rawPhone.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        if (digits.startsWith("00")) {
            digits = digits.substring(2);
        }
        if (digits.length() == LOCAL_NUMBER_LENGTH) {
            digits = TUNISIA_COUNTRY_CODE + digits;
        }
        return digits;
    }

    /** Last 8 digits - the part of the number that identifies a person regardless of country code. */
    static String tail(String normalizedPhone) {
        if (normalizedPhone == null || normalizedPhone.length() <= LOCAL_NUMBER_LENGTH) {
            return normalizedPhone;
        }
        return normalizedPhone.substring(normalizedPhone.length() - LOCAL_NUMBER_LENGTH);
    }
}

package com.rezkna.identity.account;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Generates 6-digit OTP codes (000000-999999) using a cryptographically secure RNG. */
@Component
class OtpGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    String generate() {
        int value = secureRandom.nextInt(1_000_000);
        return String.format("%06d", value);
    }
}

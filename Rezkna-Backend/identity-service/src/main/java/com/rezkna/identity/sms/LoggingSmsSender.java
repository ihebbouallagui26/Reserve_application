package com.rezkna.identity.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Development/test stand-in for a real SMS provider. Never logs the message content -
 * whatever it contains (an OTP code today, possibly other text later) - only a
 * confirmation with the phone number masked.
 */
@Component
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    @Override
    public void send(String phone, String message) {
        log.info("OTP sent to {}", mask(phone));
    }

    private String mask(String phone) {
        if (phone == null || phone.length() <= 4) {
            return "****";
        }
        int visible = 4;
        return "*".repeat(phone.length() - visible) + phone.substring(phone.length() - visible);
    }
}

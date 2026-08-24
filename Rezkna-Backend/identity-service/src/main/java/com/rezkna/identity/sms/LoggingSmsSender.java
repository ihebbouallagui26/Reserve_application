package com.rezkna.identity.sms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Development/test stand-in for a real SMS provider. Never logs the message content -
 * whatever it contains (an OTP code today, possibly other text later) - only a
 * confirmation with the phone number masked, unless identity.sms.log-otp-for-dev is
 * explicitly enabled (local dev only, defaults to false) so a developer can read a
 * generated OTP out of the container logs to complete a real /phone/verify call.
 */
@Component
public class LoggingSmsSender implements SmsSender {

    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);

    private final boolean logOtpForDev;

    public LoggingSmsSender(@Value("${identity.sms.log-otp-for-dev:false}") boolean logOtpForDev) {
        this.logOtpForDev = logOtpForDev;
    }

    @Override
    public void send(String phone, String message) {
        if (logOtpForDev) {
            log.info("DEV OTP for {}: {}", mask(phone), message);
        } else {
            log.info("OTP sent to {}", mask(phone));
        }
    }

    private String mask(String phone) {
        if (phone == null || phone.length() <= 4) {
            return "****";
        }
        int visible = 4;
        return "*".repeat(phone.length() - visible) + phone.substring(phone.length() - visible);
    }
}

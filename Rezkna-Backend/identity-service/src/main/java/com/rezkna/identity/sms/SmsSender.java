package com.rezkna.identity.sms;

/**
 * Abstraction over the SMS provider. Sprint 1 ships only LoggingSmsSender; a real
 * provider (engagement-service's concern per the sprint plan) can implement this
 * interface later without any caller needing to change.
 */
public interface SmsSender {

    void send(String phone, String message);
}

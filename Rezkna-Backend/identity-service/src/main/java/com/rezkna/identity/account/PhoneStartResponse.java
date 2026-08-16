package com.rezkna.identity.account;

public record PhoneStartResponse(
        String phone,
        int expiresInMinutes
) {
}

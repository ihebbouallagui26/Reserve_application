package com.rezkna.identity.account;

public record PreferencesResponse(
        boolean notifySms,
        boolean notifyEmail,
        boolean marketingOptIn
) {
}

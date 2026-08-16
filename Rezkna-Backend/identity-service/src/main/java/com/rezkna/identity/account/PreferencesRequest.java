package com.rezkna.identity.account;

public record PreferencesRequest(
        boolean notifySms,
        boolean notifyEmail,
        boolean marketingOptIn
) {
}

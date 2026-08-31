package com.rezkna.identity.account;

/** Explicit allow-list of publicly-safe configuration. Never add a field here without
 * checking it cannot leak a secret (client secrets, app secrets, JWT secret, credentials
 * all stay server-side) - this record's shape is a deliberate boundary, not convenience. */
public record PublicConfigResponse(
        String googleClientId,
        String facebookAppId
) {
}

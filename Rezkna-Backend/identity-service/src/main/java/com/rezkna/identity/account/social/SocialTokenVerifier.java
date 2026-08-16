package com.rezkna.identity.account.social;

public interface SocialTokenVerifier {

    /** Lowercase provider key this verifier handles, e.g. "google". */
    String provider();

    /**
     * Verifies the token against the provider's own servers (never trusting the client
     * alone) and returns the resulting profile. Throws
     * org.springframework.security.authentication.BadCredentialsException if the token
     * is invalid, expired, or not issued for this application.
     */
    SocialProfile verify(String token);
}

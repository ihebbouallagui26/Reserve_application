package com.rezkna.identity.account.social;

/** Identity handed back by a provider once its token has been verified server-side. */
public record SocialProfile(String provider, String providerId, String email, String name) {
}

package com.rezkna.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Validates JWTs signed with a shared HMAC secret (the same secret every Rezkna
 * service is configured with). Sprint 0 scope is validation only - issuing real
 * tokens is Sprint 1's job (identity-service login/registration).
 * <p>
 * Deliberately generic: only the standard "sub" claim is read here. Per-token-type
 * claims (role, property scope, etc., per the diner vs owner/host JWT distinction
 * in the technical documentation) belong to whichever service issues that token
 * type, once that business logic actually exists.
 */
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Parses and validates the token: signature, structure, and expiration.
     * Throws io.jsonwebtoken.JwtException (or a subtype) if any check fails.
     */
    public Claims validate(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractSubject(String token) {
        return validate(token).getSubject();
    }
}

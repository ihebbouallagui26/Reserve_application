package com.rezkna.restaurant.restaurant;

import com.rezkna.common.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * restaurant-service's own reader for partner JWTs - the same role PlatformTokenVerifier
 * plays for platform tokens, built on the same shared common-lib.JwtService. restaurant-
 * service has no Maven dependency on identity-service, so identity-service's
 * PartnerTokenService/PartnerPrincipal classes cannot be reused directly - this is a
 * local, read-only counterpart (no generateToken(): restaurant-service never issues
 * partner tokens, only identity-service does, at /partner/login).
 */
@Component
public class PartnerTokenVerifier {

    private static final String TOKEN_TYPE = "partner";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public PartnerTokenVerifier(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** Validates the bearer token and enforces typ=partner. Returns sub/pid/role. */
    public PartnerPrincipal requireClaims(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        Claims claims;
        try {
            claims = jwtService.validate(token);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BadCredentialsException("Invalid or expired token");
        }
        if (!TOKEN_TYPE.equals(claims.get("typ", String.class))) {
            throw new BadCredentialsException("Wrong token type");
        }
        return new PartnerPrincipal(claims.getSubject(), claims.get("pid", String.class), claims.get("role", String.class));
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing or invalid authentication token");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}

package com.rezkna.restaurant.restaurant;

import com.rezkna.common.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

/**
 * restaurant-service's own reader for platform-owner JWTs - the same role identity-
 * service's DinerTokenService/PartnerTokenService/PlatformTokenService each play for
 * their own token type, built on the same shared common-lib.JwtService. restaurant-
 * service has no Maven dependency on identity-service, so identity-service's
 * PlatformTokenService/PlatformPrincipal classes cannot be reused directly - this is a
 * local, read-only counterpart (no generateToken(): restaurant-service never issues
 * platform tokens, only identity-service does, at /platform/login).
 */
@Component
public class PlatformTokenVerifier {

    private static final String TOKEN_TYPE = "platform";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtService jwtService;

    public PlatformTokenVerifier(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** Validates the bearer token and enforces typ=platform. Returns sub/role. */
    public PlatformPrincipal requireClaims(String authorizationHeader) {
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
        return new PlatformPrincipal(claims.getSubject(), claims.get("role", String.class));
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing or invalid authentication token");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}

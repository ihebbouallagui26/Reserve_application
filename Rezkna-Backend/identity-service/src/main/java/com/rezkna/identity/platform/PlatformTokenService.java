package com.rezkna.identity.platform;

import com.rezkna.common.security.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/** Issues and reads platform owner JWTs - the platform equivalent of DinerTokenService/
 * PartnerTokenService. No pid/propertyId claim: a platform owner's scope is global. */
@Service
public class PlatformTokenService {

    private static final String TOKEN_TYPE = "platform";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final JwtService jwtService;
    private final long tokenDays;

    public PlatformTokenService(@Value("${jwt.secret}") String jwtSecret,
                                 @Value("${identity.jwt.platform-token-days}") long tokenDays,
                                 JwtService jwtService) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtService = jwtService;
        this.tokenDays = tokenDays;
    }

    public String generateToken(PlatformUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId())
                .claim("typ", TOKEN_TYPE)
                .claim("email", user.getEmail())
                .claim("role", user.getRole())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenDays, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
    }

    /** Validates the bearer token and enforces typ=platform. Returns sub/email/role. */
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
        return new PlatformPrincipal(claims.getSubject(), claims.get("email", String.class), claims.get("role", String.class));
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing or invalid authentication token");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}

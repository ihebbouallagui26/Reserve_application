package com.rezkna.identity.account;

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

/**
 * Issues and reads diner JWTs. common-lib's JwtService only proves a token is
 * cryptographically valid (signature, structure, expiration) - it deliberately does not
 * know about "typ". This class owns the second layer: is this specific token a diner
 * token, and who is it for.
 */
@Service
public class DinerTokenService {

    private static final String TOKEN_TYPE = "diner";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final JwtService jwtService;
    private final long tokenDays;

    public DinerTokenService(@Value("${jwt.secret}") String jwtSecret,
                              @Value("${identity.jwt.diner-token-days}") long tokenDays,
                              JwtService jwtService) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtService = jwtService;
        this.tokenDays = tokenDays;
    }

    public String generateToken(DinerAccount account) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(account.getId())
                .claim("typ", TOKEN_TYPE)
                .claim("email", account.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenDays, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
    }

    /** Validates the bearer token and enforces typ=diner. Returns the accountId (sub). */
    public String requireAccountId(String authorizationHeader) {
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
        return claims.getSubject();
    }

    private String extractBearerToken(String header) {
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new BadCredentialsException("Missing or invalid authentication token");
        }
        return header.substring(BEARER_PREFIX.length());
    }
}

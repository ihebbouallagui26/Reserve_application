package com.rezkna.identity.partner;

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

/** Issues and reads partner (owner/host) JWTs - the partner equivalent of DinerTokenService. */
@Service
public class PartnerTokenService {

    private static final String TOKEN_TYPE = "partner";
    private static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey signingKey;
    private final JwtService jwtService;
    private final long tokenDays;

    public PartnerTokenService(@Value("${jwt.secret}") String jwtSecret,
                                @Value("${identity.jwt.partner-token-days}") long tokenDays,
                                JwtService jwtService) {
        this.signingKey = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtService = jwtService;
        this.tokenDays = tokenDays;
    }

    public String generateToken(PartnerUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getId())
                .claim("typ", TOKEN_TYPE)
                .claim("pid", user.getPropertyId())
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(tokenDays, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
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

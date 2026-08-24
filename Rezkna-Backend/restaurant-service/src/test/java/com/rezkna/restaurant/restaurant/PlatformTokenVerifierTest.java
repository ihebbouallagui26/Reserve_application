package com.rezkna.restaurant.restaurant;

import com.rezkna.common.security.JwtService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformTokenVerifierTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long-for-hs256!!";

    private final JwtService jwtService = new JwtService(SECRET);
    private final PlatformTokenVerifier verifier = new PlatformTokenVerifier(jwtService);
    private final SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    private String tokenWithTyp(String typ) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("user-1")
                .claim("typ", typ)
                .claim("role", "PLATFORM_OWNER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(1, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
    }

    @Test
    void requireClaimsRoundTripsForAValidPlatformToken() {
        String token = tokenWithTyp("platform");

        PlatformPrincipal principal = verifier.requireClaims("Bearer " + token);

        assertThat(principal.userId()).isEqualTo("user-1");
        assertThat(principal.role()).isEqualTo("PLATFORM_OWNER");
    }

    @Test
    void rejectsADinerToken() {
        String token = tokenWithTyp("diner");

        assertThatThrownBy(() -> verifier.requireClaims("Bearer " + token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAPartnerToken() {
        String token = tokenWithTyp("partner");

        assertThatThrownBy(() -> verifier.requireClaims("Bearer " + token))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> verifier.requireClaims(null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAMalformedToken() {
        assertThatThrownBy(() -> verifier.requireClaims("Bearer not-a-real-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAnExpiredToken() {
        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        String expiredToken = Jwts.builder()
                .subject("user-1")
                .claim("typ", "platform")
                .claim("role", "PLATFORM_OWNER")
                .issuedAt(Date.from(past.minus(1, ChronoUnit.DAYS)))
                .expiration(Date.from(past))
                .signWith(signingKey)
                .compact();

        assertThatThrownBy(() -> verifier.requireClaims("Bearer " + expiredToken))
                .isInstanceOf(BadCredentialsException.class);
    }
}

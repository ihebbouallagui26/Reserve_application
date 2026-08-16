package com.rezkna.identity.account;

import com.rezkna.common.security.JwtService;
import com.rezkna.identity.partner.PartnerTokenService;
import com.rezkna.identity.partner.PartnerUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DinerTokenServiceTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long-for-hs256!!";

    private final JwtService jwtService = new JwtService(SECRET);
    private final DinerTokenService dinerTokenService = new DinerTokenService(SECRET, 60, jwtService);

    @Test
    void generatesTokenWithExpectedClaims() {
        DinerAccount account = new DinerAccount();
        account.setId("acc-1");
        account.setEmail("diner@example.com");

        String token = dinerTokenService.generateToken(account);
        var claims = jwtService.validate(token);

        assertThat(claims.getSubject()).isEqualTo("acc-1");
        assertThat(claims.get("typ", String.class)).isEqualTo("diner");
        assertThat(claims.get("email", String.class)).isEqualTo("diner@example.com");
        assertThat(claims.getExpiration()).isAfter(Date.from(Instant.now().plusSeconds(59L * 24 * 3600)));
    }

    @Test
    void requireAccountIdRoundTripsForAValidDinerToken() {
        DinerAccount account = new DinerAccount();
        account.setId("acc-1");
        account.setEmail("diner@example.com");

        String token = dinerTokenService.generateToken(account);

        assertThat(dinerTokenService.requireAccountId("Bearer " + token)).isEqualTo("acc-1");
    }

    @Test
    void rejectsAPartnerTokenOnADinerRoute() {
        PartnerTokenService partnerTokenService = new PartnerTokenService(SECRET, 14, jwtService);
        PartnerUser user = new PartnerUser();
        user.setId("user-1");
        user.setPropertyId("prop-1");
        user.setRole("OWNER");
        String partnerToken = partnerTokenService.generateToken(user);

        assertThatThrownBy(() -> dinerTokenService.requireAccountId("Bearer " + partnerToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> dinerTokenService.requireAccountId(null))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMalformedAuthorizationHeader() {
        assertThatThrownBy(() -> dinerTokenService.requireAccountId("not-a-bearer-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant past = Instant.now().minusSeconds(3600);
        String expiredToken = Jwts.builder()
                .subject("acc-1")
                .claim("typ", "diner")
                .issuedAt(Date.from(past.minusSeconds(3600)))
                .expiration(Date.from(past))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> dinerTokenService.requireAccountId("Bearer " + expiredToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsBadSignature() {
        SecretKey wrongKey = Keys.hmacShaKeyFor("a-completely-different-secret-value-32bytes!".getBytes(StandardCharsets.UTF_8));
        String tokenSignedWithWrongKey = Jwts.builder()
                .subject("acc-1")
                .claim("typ", "diner")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(wrongKey)
                .compact();

        assertThatThrownBy(() -> dinerTokenService.requireAccountId("Bearer " + tokenSignedWithWrongKey))
                .isInstanceOf(BadCredentialsException.class);
    }
}

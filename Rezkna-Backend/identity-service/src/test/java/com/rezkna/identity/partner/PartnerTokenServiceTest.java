package com.rezkna.identity.partner;

import com.rezkna.common.security.JwtService;
import com.rezkna.identity.account.DinerAccount;
import com.rezkna.identity.account.DinerTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerTokenServiceTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long-for-hs256!!";

    private final JwtService jwtService = new JwtService(SECRET);
    private final PartnerTokenService partnerTokenService = new PartnerTokenService(SECRET, 14, jwtService);

    @Test
    void generatesTokenWithExpectedClaims() {
        PartnerUser user = new PartnerUser();
        user.setId("user-1");
        user.setPropertyId("prop-1");
        user.setRole("OWNER");
        user.setEmail("owner@example.com");

        String token = partnerTokenService.generateToken(user);
        var claims = jwtService.validate(token);

        assertThat(claims.getSubject()).isEqualTo("user-1");
        assertThat(claims.get("typ", String.class)).isEqualTo("partner");
        assertThat(claims.get("pid", String.class)).isEqualTo("prop-1");
        assertThat(claims.get("role", String.class)).isEqualTo("OWNER");
        assertThat(claims.get("email", String.class)).isEqualTo("owner@example.com");
    }

    @Test
    void requireClaimsRoundTripsForAValidPartnerToken() {
        PartnerUser user = new PartnerUser();
        user.setId("user-1");
        user.setPropertyId("prop-1");
        user.setRole("HOST");
        String token = partnerTokenService.generateToken(user);

        PartnerPrincipal principal = partnerTokenService.requireClaims("Bearer " + token);

        assertThat(principal.userId()).isEqualTo("user-1");
        assertThat(principal.propertyId()).isEqualTo("prop-1");
        assertThat(principal.role()).isEqualTo("HOST");
    }

    @Test
    void rejectsADinerTokenOnAPartnerRoute() {
        DinerTokenService dinerTokenService = new DinerTokenService(SECRET, 60, jwtService);
        DinerAccount account = new DinerAccount();
        account.setId("acc-1");
        String dinerToken = dinerTokenService.generateToken(account);

        assertThatThrownBy(() -> partnerTokenService.requireClaims("Bearer " + dinerToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> partnerTokenService.requireClaims(null))
                .isInstanceOf(BadCredentialsException.class);
    }
}

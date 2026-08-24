package com.rezkna.identity.platform;

import com.rezkna.common.security.JwtService;
import com.rezkna.identity.account.DinerAccount;
import com.rezkna.identity.account.DinerTokenService;
import com.rezkna.identity.partner.PartnerTokenService;
import com.rezkna.identity.partner.PartnerUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlatformTokenServiceTest {

    private static final String SECRET = "test-secret-at-least-32-bytes-long-for-hs256!!";

    private final JwtService jwtService = new JwtService(SECRET);
    private final PlatformTokenService platformTokenService = new PlatformTokenService(SECRET, 7, jwtService);

    @Test
    void generatesTokenWithExpectedClaims() {
        PlatformUser user = new PlatformUser();
        user.setId("platform-1");
        user.setEmail("admin@rezkna.com");
        user.setRole("PLATFORM_OWNER");

        String token = platformTokenService.generateToken(user);
        var claims = jwtService.validate(token);

        assertThat(claims.getSubject()).isEqualTo("platform-1");
        assertThat(claims.get("typ", String.class)).isEqualTo("platform");
        assertThat(claims.get("email", String.class)).isEqualTo("admin@rezkna.com");
        assertThat(claims.get("role", String.class)).isEqualTo("PLATFORM_OWNER");
        assertThat(claims.get("pid", String.class)).isNull();
    }

    @Test
    void requireClaimsRoundTripsForAValidPlatformToken() {
        PlatformUser user = new PlatformUser();
        user.setId("platform-1");
        user.setEmail("admin@rezkna.com");
        user.setRole("PLATFORM_OWNER");
        String token = platformTokenService.generateToken(user);

        PlatformPrincipal principal = platformTokenService.requireClaims("Bearer " + token);

        assertThat(principal.userId()).isEqualTo("platform-1");
        assertThat(principal.email()).isEqualTo("admin@rezkna.com");
        assertThat(principal.role()).isEqualTo("PLATFORM_OWNER");
    }

    @Test
    void rejectsADinerTokenOnAPlatformRoute() {
        DinerTokenService dinerTokenService = new DinerTokenService(SECRET, 60, jwtService);
        DinerAccount account = new DinerAccount();
        account.setId("acc-1");
        String dinerToken = dinerTokenService.generateToken(account);

        assertThatThrownBy(() -> platformTokenService.requireClaims("Bearer " + dinerToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsAPartnerTokenOnAPlatformRoute() {
        PartnerTokenService partnerTokenService = new PartnerTokenService(SECRET, 14, jwtService);
        PartnerUser partnerUser = new PartnerUser();
        partnerUser.setId("partner-1");
        partnerUser.setPropertyId("prop-1");
        partnerUser.setRole("OWNER");
        String partnerToken = partnerTokenService.generateToken(partnerUser);

        assertThatThrownBy(() -> platformTokenService.requireClaims("Bearer " + partnerToken))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsMissingAuthorizationHeader() {
        assertThatThrownBy(() -> platformTokenService.requireClaims(null))
                .isInstanceOf(BadCredentialsException.class);
    }
}

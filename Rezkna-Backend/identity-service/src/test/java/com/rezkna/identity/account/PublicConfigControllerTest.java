package com.rezkna.identity.account;

import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.identity.config.SecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicConfigController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class})
class PublicConfigControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    private String tokenWithTyp(String typ) {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .claim("typ", typ)
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    // --- Access (values come from src/test/resources/application.yml:
    // google.client-id=test-google-client-id, facebook.app-id=test-facebook-app-id) ---

    @Test
    void returns200WithoutAnyToken() throws Exception {
        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void returns200WithADinerToken() throws Exception {
        mockMvc.perform(get("/config").header("Authorization", "Bearer " + tokenWithTyp("diner")))
                .andExpect(status().isOk());
    }

    @Test
    void returns200WithAPartnerToken() throws Exception {
        mockMvc.perform(get("/config").header("Authorization", "Bearer " + tokenWithTyp("partner")))
                .andExpect(status().isOk());
    }

    @Test
    void returns200WithAPlatformToken() throws Exception {
        mockMvc.perform(get("/config").header("Authorization", "Bearer " + tokenWithTyp("platform")))
                .andExpect(status().isOk());
    }

    // --- Content ---

    @Test
    void responseContainsOnlyTheAllowedPublicFields() throws Exception {
        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.googleClientId").exists())
                .andExpect(jsonPath("$.data.facebookAppId").exists())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void responseNeverContainsAnySecret() throws Exception {
        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facebookAppSecret").doesNotExist())
                .andExpect(jsonPath("$.data.appSecret").doesNotExist())
                .andExpect(jsonPath("$.data.jwtSecret").doesNotExist())
                .andExpect(jsonPath("$.data.secret").doesNotExist());
    }

    @Test
    void exposesTheConfiguredGoogleClientId() throws Exception {
        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.googleClientId").value("test-google-client-id"));
    }

    @Test
    void exposesTheConfiguredFacebookAppId() throws Exception {
        mockMvc.perform(get("/config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.facebookAppId").value("test-facebook-app-id"));
    }

    // --- Private routes stay private ---

    @Test
    void unrelatedProtectedRouteStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/platform/me"))
                .andExpect(status().isUnauthorized());
    }

    // --- Absent configuration (separate context: empty client-id/app-id) ---

    @Nested
    @TestPropertySource(properties = {"google.client-id=", "facebook.app-id="})
    class WhenNotConfigured {

        @Autowired
        private MockMvc mockMvc;

        @Test
        void fallsBackToEmptyStringsRatherThanNullOr404Or500() throws Exception {
            mockMvc.perform(get("/config"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.googleClientId").value(""))
                    .andExpect(jsonPath("$.data.facebookAppId").value(""));
        }
    }
}

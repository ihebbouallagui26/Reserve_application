package com.rezkna.identity.platform;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.identity.config.SecurityConfig;
import com.rezkna.identity.partner.PartnerUser;
import com.rezkna.identity.partner.PartnerUserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PlatformController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class})
class PlatformControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PlatformUserRepository platformUserRepository;

    @MockitoBean
    private PartnerUserRepository partnerUserRepository;

    @MockitoBean
    private PlatformTokenService tokenService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    /** Any request to a protected route needs a token that passes common-lib's generic
     * crypto check; the platform-typ/role check itself is exercised via the mocked tokenService. */
    private String anyValidlySignedToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    // --- POST /platform/login ---

    @Test
    void loginReturns200AndTokenOnSuccess() throws Exception {
        PlatformUser user = new PlatformUser();
        user.setId("p1");
        user.setEmail("admin@rezkna.com");
        user.setPasswordHash("hashed");
        user.setName("Rezkna Admin");
        user.setRole("PLATFORM_OWNER");
        user.setActive(true);

        when(platformUserRepository.findByEmail("admin@rezkna.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(platformUserRepository.save(any())).thenReturn(user);
        when(tokenService.generateToken(any())).thenReturn("platform-jwt");

        PlatformLoginRequest request = new PlatformLoginRequest("admin@rezkna.com", "password123");

        mockMvc.perform(post("/platform/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.token").value("platform-jwt"))
                .andExpect(jsonPath("$.data.user.email").value("admin@rezkna.com"))
                .andExpect(jsonPath("$.data.user.role").value("PLATFORM_OWNER"));
    }

    @Test
    void loginReturns401ForWrongPassword() throws Exception {
        PlatformUser user = new PlatformUser();
        user.setId("p1");
        user.setEmail("admin@rezkna.com");
        user.setPasswordHash("hashed");
        user.setActive(true);

        when(platformUserRepository.findByEmail("admin@rezkna.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed")).thenReturn(false);

        PlatformLoginRequest request = new PlatformLoginRequest("admin@rezkna.com", "wrong-password");

        mockMvc.perform(post("/platform/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void loginReturns401ForUnknownEmail() throws Exception {
        when(platformUserRepository.findByEmail("nobody@rezkna.com")).thenReturn(Optional.empty());

        PlatformLoginRequest request = new PlatformLoginRequest("nobody@rezkna.com", "password123");

        mockMvc.perform(post("/platform/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void loginReturns401ForInactivePlatformUser() throws Exception {
        PlatformUser inactive = new PlatformUser();
        inactive.setId("p1");
        inactive.setEmail("former-admin@rezkna.com");
        inactive.setPasswordHash("hashed");
        inactive.setActive(false);

        when(platformUserRepository.findByEmail("former-admin@rezkna.com")).thenReturn(Optional.of(inactive));

        PlatformLoginRequest request = new PlatformLoginRequest("former-admin@rezkna.com", "password123");

        mockMvc.perform(post("/platform/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void loginNeverReturnsPasswordHash() throws Exception {
        PlatformUser user = new PlatformUser();
        user.setId("p1");
        user.setEmail("admin@rezkna.com");
        user.setPasswordHash("hashed");
        user.setRole("PLATFORM_OWNER");
        user.setActive(true);

        when(platformUserRepository.findByEmail("admin@rezkna.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(platformUserRepository.save(any())).thenReturn(user);
        when(tokenService.generateToken(any())).thenReturn("platform-jwt");

        PlatformLoginRequest request = new PlatformLoginRequest("admin@rezkna.com", "password123");

        mockMvc.perform(post("/platform/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.user.passwordHash").doesNotExist());
    }

    // --- GET /platform/me ---

    @Test
    void meReturns200ForAValidPlatformToken() throws Exception {
        PlatformUser user = new PlatformUser();
        user.setId("p1");
        user.setEmail("admin@rezkna.com");
        user.setName("Rezkna Admin");
        user.setRole("PLATFORM_OWNER");
        user.setActive(true);

        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(platformUserRepository.findById("p1")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/platform/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("admin@rezkna.com"))
                .andExpect(jsonPath("$.data.role").value("PLATFORM_OWNER"))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void meReturns401WithoutAToken() throws Exception {
        mockMvc.perform(get("/platform/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns401ForADinerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/platform/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns401ForAPartnerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/platform/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns401ForAnInvalidToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(get("/platform/me").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns401ForAnExpiredToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(get("/platform/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /platform/partners ---

    @Test
    void listPartnersReturns200WithRealPartnerUsers() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        PartnerUser partner = new PartnerUser();
        partner.setId("partner-1");
        partner.setEmail("owner@restaurant.com");
        partner.setPropertyId("property-1");
        partner.setName("Restaurant Owner");
        partner.setRole("OWNER");
        partner.setActive(true);
        when(partnerUserRepository.findAll()).thenReturn(List.of(partner));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].id").value("partner-1"))
                .andExpect(jsonPath("$.data[0].email").value("owner@restaurant.com"))
                .andExpect(jsonPath("$.data[0].propertyId").value("property-1"))
                .andExpect(jsonPath("$.data[0].role").value("OWNER"));
    }

    @Test
    void listPartnersReturnsEmptyListWhenNoPartners() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void listPartnersReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/platform/partners"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPartnersReturns401ForADinerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPartnersReturns401ForAPartnerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPartnersReturns401ForAnInvalidToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer not-a-real-token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listPartnersReturns403ForPlatformTokenWithAdminRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p2", "admin2@rezkna.com", "ADMIN"));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isForbidden());
    }

    @Test
    void listPartnersReturns200ForPlatformOwnerRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void listPartnersNeverReturnsPasswordHash() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        PartnerUser partner = new PartnerUser();
        partner.setId("partner-1");
        partner.setEmail("owner@restaurant.com");
        partner.setPropertyId("property-1");
        partner.setPasswordHash("hashed-secret");
        partner.setRole("OWNER");
        partner.setActive(true);
        when(partnerUserRepository.findAll()).thenReturn(List.of(partner));

        mockMvc.perform(get("/platform/partners").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].passwordHash").doesNotExist());
    }

    // --- PATCH /platform/partners/{id}/status ---

    @Test
    void updatePartnerStatusReturns200AndUpdatesActive() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        PartnerUser partner = new PartnerUser();
        partner.setId("partner-1");
        partner.setEmail("owner@restaurant.com");
        partner.setPropertyId("property-1");
        partner.setRole("OWNER");
        partner.setActive(true);
        when(partnerUserRepository.findById("partner-1")).thenReturn(Optional.of(partner));
        when(partnerUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(false));
    }

    @Test
    void updatePartnerStatusReturns404ForUnknownPartner() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/platform/partners/unknown/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePartnerStatusReturns401WithoutToken() throws Exception {
        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePartnerStatusReturns401ForADinerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePartnerStatusReturns401ForAPartnerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updatePartnerStatusReturns403ForPlatformTokenWithAdminRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p2", "admin2@rezkna.com", "ADMIN"));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": false}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updatePartnerStatusReturns200ForPlatformOwner() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        PartnerUser partner = new PartnerUser();
        partner.setId("partner-1");
        partner.setEmail("owner@restaurant.com");
        partner.setPropertyId("property-1");
        partner.setRole("OWNER");
        partner.setActive(false);
        when(partnerUserRepository.findById("partner-1")).thenReturn(Optional.of(partner));
        when(partnerUserRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"active\": true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void updatePartnerStatusReturns400ForMissingActiveField() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        mockMvc.perform(patch("/platform/partners/partner-1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    // --- POST /platform/partners ---

    private String validCreatePartnerBody() {
        return "{\"email\": \"owner@restaurant.com\", \"password\": \"password123\", "
                + "\"name\": \"Restaurant Owner\", \"propertyId\": \"restaurant-1\"}";
    }

    @Test
    void createPartnerReturns200OnSuccess() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.existsByEmailAndPropertyId("owner@restaurant.com", "restaurant-1")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(partnerUserRepository.save(any())).thenAnswer(invocation -> {
            PartnerUser saved = invocation.getArgument(0);
            saved.setId("partner-1");
            return saved;
        });

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.email").value("owner@restaurant.com"))
                .andExpect(jsonPath("$.data.propertyId").value("restaurant-1"))
                .andExpect(jsonPath("$.data.role").value("OWNER"))
                .andExpect(jsonPath("$.data.active").value(true))
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }

    @Test
    void createPartnerInvalidEmailReturns400() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        String body = "{\"email\": \"not-an-email\", \"password\": \"password123\", "
                + "\"name\": \"Restaurant Owner\", \"propertyId\": \"restaurant-1\"}";

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPartnerMissingPasswordReturns400() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        String body = "{\"email\": \"owner@restaurant.com\", "
                + "\"name\": \"Restaurant Owner\", \"propertyId\": \"restaurant-1\"}";

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPartnerMissingNameReturns400() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        String body = "{\"email\": \"owner@restaurant.com\", \"password\": \"password123\", "
                + "\"propertyId\": \"restaurant-1\"}";

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPartnerMissingPropertyIdReturns400() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));

        String body = "{\"email\": \"owner@restaurant.com\", \"password\": \"password123\", "
                + "\"name\": \"Restaurant Owner\"}";

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPartnerDuplicateEmailAndPropertyReturns409() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.existsByEmailAndPropertyId("owner@restaurant.com", "restaurant-1")).thenReturn(true);

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isConflict());
    }

    @Test
    void createPartnerReturns401WithoutToken() throws Exception {
        mockMvc.perform(post("/platform/partners")
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPartnerReturns401ForADinerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPartnerReturns401ForAPartnerToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPartnerReturns401ForAnInvalidToken() throws Exception {
        when(tokenService.requireClaims(anyString())).thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createPartnerReturns403ForNonPlatformOwnerRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p2", "admin2@rezkna.com", "ADMIN"));

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPartnerNeverReturnsPasswordHash() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PlatformPrincipal("p1", "admin@rezkna.com", "PLATFORM_OWNER"));
        when(partnerUserRepository.existsByEmailAndPropertyId("owner@restaurant.com", "restaurant-1")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed-password");
        when(partnerUserRepository.save(any())).thenAnswer(invocation -> {
            PartnerUser saved = invocation.getArgument(0);
            saved.setId("partner-1");
            return saved;
        });

        mockMvc.perform(post("/platform/partners")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validCreatePartnerBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.passwordHash").doesNotExist());
    }
}

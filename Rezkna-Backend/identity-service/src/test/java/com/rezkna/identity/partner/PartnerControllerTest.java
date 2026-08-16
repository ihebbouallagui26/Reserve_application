package com.rezkna.identity.partner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.identity.config.SecurityConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class})
class PartnerControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PartnerUserRepository partnerUserRepository;

    @MockitoBean
    private PartnerTokenService tokenService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private String anyValidlySignedToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    @Test
    void loginReturns401ForWrongCredentials() throws Exception {
        when(partnerUserRepository.findByEmail("owner@example.com")).thenReturn(List.of());

        PartnerLoginRequest request = new PartnerLoginRequest("owner@example.com", "password123", null);

        mockMvc.perform(post("/partner/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturns401ForAnInactiveAccountEvenWithCorrectPassword() throws Exception {
        PartnerUser inactive = new PartnerUser();
        inactive.setId("u1");
        inactive.setEmail("former-owner@example.com");
        inactive.setPropertyId("prop-a");
        inactive.setPasswordHash("hashed");
        inactive.setRole("OWNER");
        inactive.setActive(false);

        when(partnerUserRepository.findByEmail("former-owner@example.com")).thenReturn(List.of(inactive));

        PartnerLoginRequest request = new PartnerLoginRequest("former-owner@example.com", "password123", null);

        mockMvc.perform(post("/partner/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginReturns409WhenMultiplePropertiesAndNonePicked() throws Exception {
        PartnerUser userA = new PartnerUser();
        userA.setId("u1");
        userA.setEmail("owner@example.com");
        userA.setPropertyId("prop-a");
        userA.setPasswordHash("hashed");
        userA.setRole("OWNER");
        userA.setActive(true);

        PartnerUser userB = new PartnerUser();
        userB.setId("u2");
        userB.setEmail("owner@example.com");
        userB.setPropertyId("prop-b");
        userB.setPasswordHash("hashed");
        userB.setRole("OWNER");
        userB.setActive(true);

        when(partnerUserRepository.findByEmail("owner@example.com")).thenReturn(List.of(userA, userB));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);

        PartnerLoginRequest request = new PartnerLoginRequest("owner@example.com", "password123", null);

        mockMvc.perform(post("/partner/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void loginNeverReturnsAPropertyObjectField() throws Exception {
        PartnerUser user = new PartnerUser();
        user.setId("u1");
        user.setEmail("owner@example.com");
        user.setPropertyId("prop-a");
        user.setPasswordHash("hashed");
        user.setRole("OWNER");
        user.setActive(true);

        when(partnerUserRepository.findByEmail("owner@example.com")).thenReturn(List.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        when(partnerUserRepository.save(any())).thenReturn(user);
        when(tokenService.generateToken(any())).thenReturn("partner-jwt");

        PartnerLoginRequest request = new PartnerLoginRequest("owner@example.com", "password123", null);

        mockMvc.perform(post("/partner/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.propertyId").value("prop-a"))
                .andExpect(jsonPath("$.data.restaurants[0].propertyId").value("prop-a"))
                .andExpect(jsonPath("$.data.property").doesNotExist());
    }

    @Test
    void createStaffReturns403ForHostRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PartnerPrincipal("u1", "prop-a", "HOST"));

        CreateStaffRequest request = new CreateStaffRequest("New Host", "new@example.com", "password123", "HOST");

        mockMvc.perform(post("/partner/staff")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createStaffReturns200ForOwnerRole() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PartnerPrincipal("u1", "prop-a", "OWNER"));
        when(partnerUserRepository.existsByEmailAndPropertyId("new@example.com", "prop-a")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(partnerUserRepository.save(any())).thenAnswer(invocation -> {
            PartnerUser u = invocation.getArgument(0);
            u.setId("u2");
            return u;
        });

        CreateStaffRequest request = new CreateStaffRequest("New Host", "new@example.com", "password123", "HOST");

        mockMvc.perform(post("/partner/staff")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.role").value("HOST"));
    }

    @Test
    void deleteStaffReturns404WhenStaffBelongsToAnotherProperty() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PartnerPrincipal("u1", "prop-a", "OWNER"));
        PartnerUser otherPropertyStaff = new PartnerUser();
        otherPropertyStaff.setId("u3");
        otherPropertyStaff.setPropertyId("prop-b");
        when(partnerUserRepository.findById("u3")).thenReturn(Optional.of(otherPropertyStaff));

        mockMvc.perform(delete("/partner/staff/u3")
                        .header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStaffReturns200WithTheDeletedIdOnSuccess() throws Exception {
        when(tokenService.requireClaims(anyString())).thenReturn(new PartnerPrincipal("u1", "prop-a", "OWNER"));
        PartnerUser samePropertyStaff = new PartnerUser();
        samePropertyStaff.setId("u4");
        samePropertyStaff.setPropertyId("prop-a");
        when(partnerUserRepository.findById("u4")).thenReturn(Optional.of(samePropertyStaff));

        mockMvc.perform(delete("/partner/staff/u4")
                        .header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("u4"));
    }
}

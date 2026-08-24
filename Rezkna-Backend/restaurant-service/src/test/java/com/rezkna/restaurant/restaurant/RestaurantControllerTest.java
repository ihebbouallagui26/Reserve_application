package com.rezkna.restaurant.restaurant;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.restaurant.config.SecurityConfig;
import com.rezkna.restaurant.exception.RestaurantExceptionHandler;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.BadCredentialsException;
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

@WebMvcTest(RestaurantController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class, RestaurantExceptionHandler.class})
class RestaurantControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @MockitoBean
    private PlatformTokenVerifier platformTokenVerifier;

    private String anyValidlySignedToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    private Restaurant sampleRestaurant() {
        Restaurant restaurant = new Restaurant();
        restaurant.setId("r1");
        restaurant.setName("Le Rezkna");
        restaurant.setAddress("1 Avenue Habib Bourguiba");
        restaurant.setCity("Tunis");
        restaurant.setStatus(RestaurantStatus.PENDING);
        restaurant.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        restaurant.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return restaurant;
    }

    private void mockPlatformOwner() {
        when(platformTokenVerifier.requireClaims(anyString()))
                .thenReturn(new PlatformPrincipal("p1", "PLATFORM_OWNER"));
    }

    // --- POST /restaurants ---

    @Test
    void createReturnsSuccessForPlatformOwner() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.save(any())).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId("r1");
            return r;
        });

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.name").value("Le Rezkna"));
    }

    @Test
    void createReturns401WithoutToken() throws Exception {
        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns401ForDinerToken() throws Exception {
        when(platformTokenVerifier.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns401ForPartnerToken() throws Exception {
        when(platformTokenVerifier.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns401ForInvalidToken() throws Exception {
        when(platformTokenVerifier.requireClaims(anyString())).thenThrow(new BadCredentialsException("Invalid or expired token"));

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createReturns400ForInvalidValidation() throws Exception {
        mockPlatformOwner();

        RestaurantCreateRequest request = new RestaurantCreateRequest("", "", "");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createSetsStatusPending() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.save(any())).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId("r1");
            return r;
        });

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void createSetsCreatedAtAndUpdatedAt() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.save(any())).thenAnswer(invocation -> {
            Restaurant r = invocation.getArgument(0);
            r.setId("r1");
            return r;
        });

        RestaurantCreateRequest request = new RestaurantCreateRequest("Le Rezkna", "1 Avenue Habib Bourguiba", "Tunis");

        mockMvc.perform(post("/restaurants")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.createdAt").exists())
                .andExpect(jsonPath("$.data.updatedAt").exists());
    }

    // --- GET /restaurants ---

    @Test
    void listReturnsRealRestaurants() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findAll()).thenReturn(List.of(sampleRestaurant()));

        mockMvc.perform(get("/restaurants").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Le Rezkna"));
    }

    @Test
    void listReturns401WithoutToken() throws Exception {
        mockMvc.perform(get("/restaurants"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listReturns401ForWrongTokenType() throws Exception {
        when(platformTokenVerifier.requireClaims(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/restaurants").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    // --- GET /restaurants/{id} ---

    @Test
    void getByIdReturnsRestaurant() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));

        mockMvc.perform(get("/restaurants/r1").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("r1"));
    }

    @Test
    void getByIdReturns404ForUnknownId() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurants/unknown").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isNotFound());
    }

    // --- PUT /restaurants/{id} ---

    @Test
    void updateModifiesNameAddressCity() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantUpdateRequest request = new RestaurantUpdateRequest("New Name", "New Address", "New City");

        mockMvc.perform(put("/restaurants/r1")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("New Name"))
                .andExpect(jsonPath("$.data.address").value("New Address"))
                .andExpect(jsonPath("$.data.city").value("New City"));
    }

    @Test
    void updateDoesNotChangeStatus() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantUpdateRequest request = new RestaurantUpdateRequest("New Name", "New Address", "New City");

        mockMvc.perform(put("/restaurants/r1")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void updateRefreshesUpdatedAt() throws Exception {
        mockPlatformOwner();
        Restaurant existing = sampleRestaurant();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(existing));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        RestaurantUpdateRequest request = new RestaurantUpdateRequest("New Name", "New Address", "New City");

        mockMvc.perform(put("/restaurants/r1")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(result -> {
                    String body = result.getResponse().getContentAsString();
                    org.assertj.core.api.Assertions.assertThat(body).doesNotContain("\"updatedAt\":\"2026-01-01T00:00:00Z\"");
                });
    }

    // --- PATCH /restaurants/{id}/status ---

    @Test
    void patchStatusToActive() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/restaurants/r1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RestaurantStatusRequest("ACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("ACTIVE"));
    }

    @Test
    void patchStatusToInactive() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/restaurants/r1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RestaurantStatusRequest("INACTIVE"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("INACTIVE"));
    }

    @Test
    void patchStatusToPending() throws Exception {
        mockPlatformOwner();
        Restaurant active = sampleRestaurant();
        active.setStatus(RestaurantStatus.ACTIVE);
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(active));
        when(restaurantRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(patch("/restaurants/r1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RestaurantStatusRequest("PENDING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void patchStatusReturns400ForInvalidValue() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("r1")).thenReturn(Optional.of(sampleRestaurant()));

        mockMvc.perform(patch("/restaurants/r1/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RestaurantStatusRequest("CLOSED"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void patchStatusReturns404ForUnknownId() throws Exception {
        mockPlatformOwner();
        when(restaurantRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(patch("/restaurants/unknown/status")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(new RestaurantStatusRequest("ACTIVE"))))
                .andExpect(status().isNotFound());
    }

    // --- Isolation ---

    @Test
    void platformTokenWithWrongRoleReturns403() throws Exception {
        when(platformTokenVerifier.requireClaims(anyString()))
                .thenReturn(new PlatformPrincipal("p1", "ADMIN"));

        mockMvc.perform(get("/restaurants").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isForbidden());
    }
}

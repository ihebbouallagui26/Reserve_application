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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MenuController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class, RestaurantExceptionHandler.class})
class MenuControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MenuRepository menuRepository;

    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @MockitoBean
    private PartnerTokenVerifier partnerTokenVerifier;

    private String anyValidlySignedToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    private String validMenuBody() {
        return "{\"items\": [{\"name\": \"Couscous\", \"price\": 12.50}]}";
    }

    private void mockPartner(String propertyId, String role) {
        when(partnerTokenVerifier.requireClaims(anyString()))
                .thenReturn(new PartnerPrincipal("partner-1", propertyId, role));
    }

    // --- Success ---

    @Test
    void ownerOfRestaurantACanModifyA() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.empty());
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.restaurantId").value("restaurant-A"))
                .andExpect(jsonPath("$.data.items[0].name").value("Couscous"));
    }

    @Test
    void hostCanAlsoModifyTheirRestaurantMenu() throws Exception {
        mockPartner("restaurant-A", "HOST");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.empty());
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isOk());
    }

    @Test
    void updateReplacesExistingItems() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        Menu existing = new Menu();
        existing.setRestaurantId("restaurant-A");
        MenuItem oldItem = new MenuItem();
        oldItem.setId("old-item");
        oldItem.setName("Old Dish");
        oldItem.setPrice(new BigDecimal("5.00"));
        existing.setItems(java.util.List.of(oldItem));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.of(existing));
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(1))
                .andExpect(jsonPath("$.data.items[0].name").value("Couscous"));
    }

    @Test
    void supportsMultipleItemsWithDifferentCategories() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.empty());
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        String body = "{\"items\": ["
                + "{\"name\": \"Couscous\", \"price\": 12.50, \"category\": \"Main\"},"
                + "{\"name\": \"Baklava\", \"price\": 4.00, \"category\": \"Dessert\"}"
                + "]}";

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items.length()").value(2))
                .andExpect(jsonPath("$.data.items[0].category").value("Main"))
                .andExpect(jsonPath("$.data.items[1].category").value("Dessert"));
    }

    @Test
    void restaurantNotYetActiveCanStillHaveItsMenuWritten() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.empty());
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Restaurant status is irrelevant to write access - only existence and ownership
        // matter (Restaurant() defaults to null status here, standing in for PENDING/INACTIVE).
        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isOk());
    }

    // --- Authorization ---

    @Test
    void ownerOfACannotModifyB() throws Exception {
        mockPartner("restaurant-A", "OWNER");

        mockMvc.perform(put("/restaurants/restaurant-B/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns401WithoutToken() throws Exception {
        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns401ForInvalidToken() throws Exception {
        when(partnerTokenVerifier.requireClaims(anyString()))
                .thenThrow(new BadCredentialsException("Invalid or expired token"));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns401ForWrongTokenType() throws Exception {
        when(partnerTokenVerifier.requireClaims(anyString()))
                .thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void returns403WhenPidClaimIsAbsent() throws Exception {
        when(partnerTokenVerifier.requireClaims(anyString()))
                .thenReturn(new PartnerPrincipal("partner-1", null, "OWNER"));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns403ForMismatchedPid() throws Exception {
        mockPartner("restaurant-other", "OWNER");

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isForbidden());
    }

    @Test
    void returns404ForUnknownRestaurant() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.empty());

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(validMenuBody()))
                .andExpect(status().isNotFound());
    }

    // --- Validation ---

    @Test
    void returns400ForBlankItemName() throws Exception {
        mockPartner("restaurant-A", "OWNER");

        String body = "{\"items\": [{\"name\": \"\", \"price\": 12.50}]}";

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returns400ForNegativePrice() throws Exception {
        mockPartner("restaurant-A", "OWNER");

        String body = "{\"items\": [{\"name\": \"Couscous\", \"price\": -1}]}";

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void acceptsAnEmptyItemList() throws Exception {
        mockPartner("restaurant-A", "OWNER");
        when(restaurantRepository.findById("restaurant-A")).thenReturn(Optional.of(new Restaurant()));
        when(menuRepository.findByRestaurantId("restaurant-A")).thenReturn(Optional.empty());
        when(menuRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        mockMvc.perform(put("/restaurants/restaurant-A/menu")
                        .header("Authorization", "Bearer " + anyValidlySignedToken())
                        .contentType("application/json")
                        .content("{\"items\": []}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }
}

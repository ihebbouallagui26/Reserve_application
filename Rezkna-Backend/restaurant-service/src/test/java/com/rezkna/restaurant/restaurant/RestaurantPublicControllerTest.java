package com.rezkna.restaurant.restaurant;

import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.restaurant.config.SecurityConfig;
import com.rezkna.restaurant.exception.RestaurantExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestaurantPublicController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class, RestaurantExceptionHandler.class})
class RestaurantPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RestaurantRepository restaurantRepository;

    @MockitoBean
    private MenuRepository menuRepository;

    private Restaurant restaurantWithStatus(String id, RestaurantStatus status) {
        Restaurant restaurant = new Restaurant();
        restaurant.setId(id);
        restaurant.setName("Le Rezkna");
        restaurant.setAddress("1 Avenue Habib Bourguiba");
        restaurant.setCity("Tunis");
        restaurant.setStatus(status);
        restaurant.setCreatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        restaurant.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return restaurant;
    }

    // --- GET /restaurants/public ---

    @Test
    void listPublicReturns200WithoutToken() throws Exception {
        when(restaurantRepository.findByStatus(RestaurantStatus.ACTIVE)).thenReturn(List.of());

        mockMvc.perform(get("/restaurants/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));
    }

    @Test
    void listPublicReturnsOnlyActiveRestaurants() throws Exception {
        when(restaurantRepository.findByStatus(RestaurantStatus.ACTIVE))
                .thenReturn(List.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));

        mockMvc.perform(get("/restaurants/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].id").value("r1"))
                .andExpect(jsonPath("$.data[0].name").value("Le Rezkna"));
    }

    @Test
    void listPublicNeverExposesStatusOrTimestamps() throws Exception {
        when(restaurantRepository.findByStatus(RestaurantStatus.ACTIVE))
                .thenReturn(List.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));

        mockMvc.perform(get("/restaurants/public"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].status").doesNotExist())
                .andExpect(jsonPath("$.data[0].createdAt").doesNotExist())
                .andExpect(jsonPath("$.data[0].updatedAt").doesNotExist());
    }

    // --- GET /restaurants/public/{id} ---

    @Test
    void getPublicByIdReturns200ForActiveRestaurant() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));

        mockMvc.perform(get("/restaurants/public/r1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value("r1"))
                .andExpect(jsonPath("$.data.name").value("Le Rezkna"));
    }

    @Test
    void getPublicByIdReturns404ForInactiveRestaurant() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.INACTIVE)));

        mockMvc.perform(get("/restaurants/public/r1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicByIdReturns404ForPendingRestaurant() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.PENDING)));

        mockMvc.perform(get("/restaurants/public/r1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicByIdReturns404ForUnknownId() throws Exception {
        when(restaurantRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurants/public/unknown"))
                .andExpect(status().isNotFound());
    }

    // --- GET /restaurants/public/search ---

    @Test
    void searchReturns200WithValidParametersAndNoAuthorizationHeader() throws Exception {
        when(restaurantRepository.findByStatusAndLocationNear(any(), any(), any()))
                .thenReturn(List.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));

        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "10.1815")
                        .param("radiusKm", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data[0].id").value("r1"));
    }

    @Test
    void searchReturns400ForLatitudeBelowMinimum() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "-91")
                        .param("lng", "10.1815")
                        .param("radiusKm", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForLatitudeAboveMaximum() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "91")
                        .param("lng", "10.1815")
                        .param("radiusKm", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForLongitudeOutOfRange() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "181")
                        .param("radiusKm", "5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForZeroRadius() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "10.1815")
                        .param("radiusKm", "0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForNegativeRadius() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "10.1815")
                        .param("radiusKm", "-5"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForRadiusExceedingMaximum() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "10.1815")
                        .param("radiusKm", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchReturns400ForMissingParameter() throws Exception {
        mockMvc.perform(get("/restaurants/public/search")
                        .param("lat", "36.8065")
                        .param("lng", "10.1815"))
                .andExpect(status().isBadRequest());
    }

    // --- GET /restaurants/public/{restaurantId}/menu ---

    private Menu menuWithItems(String restaurantId, MenuItem... items) {
        Menu menu = new Menu();
        menu.setId("menu-1");
        menu.setRestaurantId(restaurantId);
        menu.setItems(List.of(items));
        menu.setUpdatedAt(Instant.parse("2026-01-01T00:00:00Z"));
        return menu;
    }

    private MenuItem item(String name, String price) {
        MenuItem item = new MenuItem();
        item.setId("item-1");
        item.setName(name);
        item.setPrice(new java.math.BigDecimal(price));
        item.setAvailable(true);
        return item;
    }

    @Test
    void getPublicMenuReturns200ForActiveRestaurantWithMenu() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));
        when(menuRepository.findByRestaurantId("r1"))
                .thenReturn(Optional.of(menuWithItems("r1", item("Couscous", "12.50"))));

        mockMvc.perform(get("/restaurants/public/r1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.items[0].name").value("Couscous"))
                .andExpect(jsonPath("$.data.items[0].price").value(12.50));
    }

    @Test
    void getPublicMenuReturns200WithEmptyItemsWhenNoMenuExistsYet() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));
        when(menuRepository.findByRestaurantId("r1")).thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurants/public/r1/menu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items").isEmpty());
    }

    @Test
    void getPublicMenuReturns404ForInactiveRestaurant() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.INACTIVE)));

        mockMvc.perform(get("/restaurants/public/r1/menu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicMenuReturns404ForPendingRestaurant() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.PENDING)));

        mockMvc.perform(get("/restaurants/public/r1/menu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicMenuReturns404ForUnknownRestaurant() throws Exception {
        when(restaurantRepository.findById("unknown")).thenReturn(Optional.empty());

        mockMvc.perform(get("/restaurants/public/unknown/menu"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublicMenuRequiresNoAuthorizationHeader() throws Exception {
        when(restaurantRepository.findById("r1"))
                .thenReturn(Optional.of(restaurantWithStatus("r1", RestaurantStatus.ACTIVE)));
        when(menuRepository.findByRestaurantId("r1")).thenReturn(Optional.empty());

        // No .header("Authorization", ...) at all - proves the route needs none.
        mockMvc.perform(get("/restaurants/public/r1/menu"))
                .andExpect(status().isOk());
    }
}

package com.rezkna.identity.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.identity.account.social.SocialLoginVerifier;
import com.rezkna.identity.config.SecurityConfig;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class})
class AccountControllerTest {

    private static final String TEST_SECRET = "test-secret-at-least-32-bytes-long-for-hs256-tests!!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DinerAccountRepository accountRepository;

    @MockitoBean
    private DinerTokenService tokenService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SocialLoginVerifier socialLoginVerifier;

    /** Any request to a protected route needs a token that passes common-lib's generic
     * crypto check; the diner-typ check itself is exercised via the mocked tokenService. */
    private String anyValidlySignedToken() {
        SecretKey key = Keys.hmacShaKeyFor(TEST_SECRET.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .subject("whatever")
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();
    }

    @Test
    void registerReturns200AndTokenOnSuccess() throws Exception {
        when(accountRepository.existsByEmail("diner@example.com")).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashed");
        when(accountRepository.save(any())).thenAnswer(invocation -> {
            DinerAccount a = invocation.getArgument(0);
            a.setId("acc-1");
            return a;
        });
        when(tokenService.generateToken(any())).thenReturn("jwt-token");

        RegisterRequest request = new RegisterRequest("diner@example.com", "password123", "Diner", null, null, null);

        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }

    @Test
    void registerReturns409WhenEmailAlreadyExists() throws Exception {
        when(accountRepository.existsByEmail("diner@example.com")).thenReturn(true);

        RegisterRequest request = new RegisterRequest("diner@example.com", "password123", "Diner", null, null, null);

        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void registerReturns400ForShortPassword() throws Exception {
        RegisterRequest request = new RegisterRequest("diner@example.com", "short", "Diner", null, null, null);

        mockMvc.perform(post("/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturns401ForWrongCredentials() throws Exception {
        when(accountRepository.findByEmail("diner@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("diner@example.com", "password123");

        mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void meReturns401WithoutAToken() throws Exception {
        mockMvc.perform(get("/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns401WhenTokenTypeIsWrong() throws Exception {
        when(tokenService.requireAccountId(anyString())).thenThrow(new BadCredentialsException("Wrong token type"));

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void meReturns200ForAValidDinerToken() throws Exception {
        DinerAccount account = new DinerAccount();
        account.setId("acc-1");
        account.setEmail("diner@example.com");
        account.setName("Diner");

        when(tokenService.requireAccountId(anyString())).thenReturn("acc-1");
        when(accountRepository.findById("acc-1")).thenReturn(Optional.of(account));

        mockMvc.perform(get("/me").header("Authorization", "Bearer " + anyValidlySignedToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("diner@example.com"));
    }
}

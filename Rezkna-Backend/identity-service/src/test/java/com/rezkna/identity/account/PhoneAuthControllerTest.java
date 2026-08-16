package com.rezkna.identity.account;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rezkna.common.config.CommonLibAutoConfiguration;
import com.rezkna.common.config.JwtAutoConfiguration;
import com.rezkna.identity.config.SecurityConfig;
import com.rezkna.identity.sms.SmsSender;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PhoneAuthController.class)
@Import({SecurityConfig.class, JwtAutoConfiguration.class, CommonLibAutoConfiguration.class})
class PhoneAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PhoneVerificationCodeRepository codeRepository;

    @MockitoBean
    private DinerAccountRepository accountRepository;

    @MockitoBean
    private DinerTokenService tokenService;

    @MockitoBean
    private SmsSender smsSender;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private OtpGenerator otpGenerator;

    @Test
    void startReturns200AndNeverExposesTheCode() throws Exception {
        when(otpGenerator.generate()).thenReturn("123456");
        when(passwordEncoder.encode("123456")).thenReturn("hashed-code");

        PhoneStartRequest request = new PhoneStartRequest("12345678");

        mockMvc.perform(post("/phone/start")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.expiresInMinutes").value(10))
                .andExpect(jsonPath("$.data.phone").value("21612345678"));

        verify(codeRepository).deleteByPhone("21612345678");
        verify(smsSender).send(eq("21612345678"), contains("123456"));
        verify(codeRepository).save(any());
    }

    @Test
    void verifyReturns401ForWrongCode() throws Exception {
        PhoneVerificationCode entity = new PhoneVerificationCode();
        entity.setPhone("21612345678");
        entity.setCodeHash("hashed-code");
        entity.setExpiresAt(Instant.now().plusSeconds(600));
        entity.setAttempts(0);

        when(codeRepository.findByPhone("21612345678")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("000000", "hashed-code")).thenReturn(false);

        PhoneVerifyRequest request = new PhoneVerifyRequest("12345678", "000000", null);

        mockMvc.perform(post("/phone/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.ok").value(false));
    }

    @Test
    void verifyReturns429AfterFifthFailedAttempt() throws Exception {
        PhoneVerificationCode entity = new PhoneVerificationCode();
        entity.setPhone("21612345678");
        entity.setCodeHash("hashed-code");
        entity.setExpiresAt(Instant.now().plusSeconds(600));
        entity.setAttempts(5);

        when(codeRepository.findByPhone("21612345678")).thenReturn(Optional.of(entity));

        PhoneVerifyRequest request = new PhoneVerifyRequest("12345678", "000000", null);

        mockMvc.perform(post("/phone/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isTooManyRequests());

        verify(codeRepository).deleteByPhone("21612345678");
    }

    @Test
    void verifyReturns401ForExpiredCode() throws Exception {
        PhoneVerificationCode entity = new PhoneVerificationCode();
        entity.setPhone("21612345678");
        entity.setCodeHash("hashed-code");
        entity.setExpiresAt(Instant.now().minusSeconds(1));
        entity.setAttempts(0);

        when(codeRepository.findByPhone("21612345678")).thenReturn(Optional.of(entity));

        PhoneVerifyRequest request = new PhoneVerifyRequest("12345678", "123456", null);

        mockMvc.perform(post("/phone/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void verifyReturns200AndCreatesNewAccountOnFirstLogin() throws Exception {
        PhoneVerificationCode entity = new PhoneVerificationCode();
        entity.setPhone("21612345678");
        entity.setCodeHash("hashed-code");
        entity.setExpiresAt(Instant.now().plusSeconds(600));
        entity.setAttempts(0);

        when(codeRepository.findByPhone("21612345678")).thenReturn(Optional.of(entity));
        when(passwordEncoder.matches("123456", "hashed-code")).thenReturn(true);
        when(accountRepository.findByPhone("21612345678")).thenReturn(Optional.empty());
        when(accountRepository.save(any())).thenAnswer(invocation -> {
            DinerAccount a = invocation.getArgument(0);
            a.setId("acc-new");
            return a;
        });
        when(tokenService.generateToken(any())).thenReturn("jwt-token");

        PhoneVerifyRequest request = new PhoneVerifyRequest("12345678", "123456", "New Diner");

        mockMvc.perform(post("/phone/verify")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.isNew").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"));
    }
}

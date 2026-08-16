package com.rezkna.identity.account.social;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FacebookTokenVerifierTest {

    private static final String OUR_APP_ID = "1234567890";

    @Mock
    private RestTemplate restTemplate;

    private FacebookTokenVerifier verifierWith(String appId, String appSecret) {
        return new FacebookTokenVerifier(restTemplate, appId, appSecret);
    }

    @Test
    void acceptsAValidTokenWithMatchingAppId() {
        FacebookTokenVerifier verifier = verifierWith(OUR_APP_ID, "app-secret");
        FacebookDebugTokenResponse response = new FacebookDebugTokenResponse(new FacebookDebugTokenData(true, OUR_APP_ID));

        assertThatCode(() -> verifier.validate(response)).doesNotThrowAnyException();
    }

    @Test
    void rejectsATokenIssuedForADifferentApp() {
        FacebookTokenVerifier verifier = verifierWith(OUR_APP_ID, "app-secret");
        FacebookDebugTokenResponse response = new FacebookDebugTokenResponse(new FacebookDebugTokenData(true, "some-other-app-id"));

        assertThatThrownBy(() -> verifier.validate(response)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsWhenFacebookReportsTheTokenAsInvalid() {
        FacebookTokenVerifier verifier = verifierWith(OUR_APP_ID, "app-secret");
        FacebookDebugTokenResponse response = new FacebookDebugTokenResponse(new FacebookDebugTokenData(false, OUR_APP_ID));

        assertThatThrownBy(() -> verifier.validate(response)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsWhenDebugTokenCallFails() {
        FacebookTokenVerifier verifier = verifierWith(OUR_APP_ID, "app-secret");
        when(restTemplate.getForObject(anyString(), any(), anyString(), anyString()))
                .thenThrow(new RestClientException("400 - invalid token"));

        assertThatThrownBy(() -> verifier.fetchDebugToken("some-invalid-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsWhenAppCredentialsAreNotConfigured() {
        FacebookTokenVerifier verifier = verifierWith("", "");

        assertThatThrownBy(() -> verifier.fetchDebugToken("any-token"))
                .isInstanceOf(BadCredentialsException.class);
    }
}

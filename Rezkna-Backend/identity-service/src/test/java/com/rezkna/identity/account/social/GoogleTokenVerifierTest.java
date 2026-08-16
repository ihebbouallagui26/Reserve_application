package com.rezkna.identity.account.social;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoogleTokenVerifierTest {

    private static final String OUR_CLIENT_ID = "our-client-id.apps.googleusercontent.com";

    @Mock
    private RestTemplate restTemplate;

    private GoogleTokenVerifier verifier;

    private GoogleTokenVerifier verifierWith(String clientId) {
        return new GoogleTokenVerifier(restTemplate, clientId);
    }

    @Test
    void acceptsATokenWhoseAudienceMatchesOurClientId() {
        verifier = verifierWith(OUR_CLIENT_ID);
        GoogleTokenInfo info = new GoogleTokenInfo(OUR_CLIENT_ID, "sub-1", "diner@example.com", "Diner");

        SocialProfile profile = verifier.decide(info);

        assertThat(profile.provider()).isEqualTo("google");
        assertThat(profile.providerId()).isEqualTo("sub-1");
        assertThat(profile.email()).isEqualTo("diner@example.com");
    }

    @Test
    void rejectsATokenIssuedForADifferentApplication() {
        verifier = verifierWith(OUR_CLIENT_ID);
        GoogleTokenInfo info = new GoogleTokenInfo("someone-elses-client-id", "sub-1", "diner@example.com", "Diner");

        assertThatThrownBy(() -> verifier.decide(info)).isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsWhenGoogleRejectsTheTokenOutright() {
        verifier = verifierWith(OUR_CLIENT_ID);
        when(restTemplate.getForObject(anyString(), any(), anyString()))
                .thenThrow(new RestClientException("400 - invalid or expired token"));

        assertThatThrownBy(() -> verifier.fetchTokenInfo("some-invalid-token"))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void rejectsWhenClientIdIsNotConfigured() {
        verifier = verifierWith("");
        GoogleTokenInfo info = new GoogleTokenInfo("", "sub-1", "diner@example.com", "Diner");

        assertThatThrownBy(() -> verifier.decide(info)).isInstanceOf(BadCredentialsException.class);
    }
}

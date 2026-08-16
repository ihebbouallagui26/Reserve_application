package com.rezkna.identity.account.social;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies a Google ID token server-to-server. Google's tokeninfo endpoint already
 * rejects invalid/expired/tampered tokens (non-2xx response); the check this class adds
 * on top is that the token was actually issued for OUR application ("aud" must equal our
 * configured client id) - a token that is valid but issued for a different app must be
 * rejected too, otherwise a token confusion attack is possible.
 */
@Component
public class GoogleTokenVerifier implements SocialTokenVerifier {

    private static final String TOKENINFO_URL = "https://oauth2.googleapis.com/tokeninfo?id_token={token}";

    private final RestTemplate restTemplate;
    private final String googleClientId;

    public GoogleTokenVerifier(RestTemplate restTemplate, @Value("${google.client-id:}") String googleClientId) {
        this.restTemplate = restTemplate;
        this.googleClientId = googleClientId;
    }

    @Override
    public String provider() {
        return "google";
    }

    @Override
    public SocialProfile verify(String idToken) {
        GoogleTokenInfo info = fetchTokenInfo(idToken);
        return decide(info);
    }

    GoogleTokenInfo fetchTokenInfo(String idToken) {
        try {
            return restTemplate.getForObject(TOKENINFO_URL, GoogleTokenInfo.class, idToken);
        } catch (RestClientException e) {
            throw new BadCredentialsException("Invalid Google token");
        }
    }

    SocialProfile decide(GoogleTokenInfo info) {
        if (info == null || info.aud() == null || !info.aud().equals(googleClientId) || googleClientId.isBlank()) {
            throw new BadCredentialsException("Invalid Google token");
        }
        return new SocialProfile("google", info.sub(), info.email(), info.name());
    }
}

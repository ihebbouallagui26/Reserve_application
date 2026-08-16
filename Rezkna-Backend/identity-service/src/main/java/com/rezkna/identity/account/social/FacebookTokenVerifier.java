package com.rezkna.identity.account.social;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * Verifies a Facebook user access token server-to-server via /debug_token, then only
 * calls /me once that token is confirmed both authentic AND issued for our app
 * (data.app_id == our configured FACEBOOK_APP_ID). A token that merely answers /me
 * successfully is not proof of anything - Graph API tokens issued for other apps can
 * also resolve a profile, which is exactly the confusion this two-step check prevents.
 */
@Component
public class FacebookTokenVerifier implements SocialTokenVerifier {

    private static final String DEBUG_TOKEN_URL =
            "https://graph.facebook.com/debug_token?input_token={input}&access_token={app}";
    private static final String PROFILE_URL =
            "https://graph.facebook.com/me?fields=id,name,email&access_token={token}";

    private final RestTemplate restTemplate;
    private final String facebookAppId;
    private final String facebookAppSecret;

    public FacebookTokenVerifier(RestTemplate restTemplate,
                                  @Value("${facebook.app-id:}") String facebookAppId,
                                  @Value("${facebook.app-secret:}") String facebookAppSecret) {
        this.restTemplate = restTemplate;
        this.facebookAppId = facebookAppId;
        this.facebookAppSecret = facebookAppSecret;
    }

    @Override
    public String provider() {
        return "facebook";
    }

    @Override
    public SocialProfile verify(String userToken) {
        FacebookDebugTokenResponse debugResponse = fetchDebugToken(userToken);
        validate(debugResponse);
        FacebookProfile profile = fetchProfile(userToken);
        return new SocialProfile("facebook", profile.id(), profile.email(), profile.name());
    }

    FacebookDebugTokenResponse fetchDebugToken(String userToken) {
        if (facebookAppId.isBlank() || facebookAppSecret.isBlank()) {
            throw new BadCredentialsException("Invalid Facebook token");
        }
        String appAccessToken = facebookAppId + "|" + facebookAppSecret;
        try {
            return restTemplate.getForObject(DEBUG_TOKEN_URL, FacebookDebugTokenResponse.class, userToken, appAccessToken);
        } catch (RestClientException e) {
            throw new BadCredentialsException("Invalid Facebook token");
        }
    }

    void validate(FacebookDebugTokenResponse response) {
        if (response == null || response.data() == null
                || !response.data().isValid()
                || !facebookAppId.equals(response.data().appId())) {
            throw new BadCredentialsException("Invalid Facebook token");
        }
    }

    FacebookProfile fetchProfile(String userToken) {
        try {
            FacebookProfile profile = restTemplate.getForObject(PROFILE_URL, FacebookProfile.class, userToken);
            if (profile == null) {
                throw new BadCredentialsException("Invalid Facebook token");
            }
            return profile;
        } catch (RestClientException e) {
            throw new BadCredentialsException("Invalid Facebook token");
        }
    }
}

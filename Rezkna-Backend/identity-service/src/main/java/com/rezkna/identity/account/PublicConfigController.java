package com.rezkna.identity.account;

import com.rezkna.common.response.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Settings the app needs before anyone signs in - the new-architecture equivalent of the
 * old backend's PublicConfigController (GET /api/discovery/config). Deliberately reads
 * only google.client-id/facebook.app-id (never facebook.app-secret or any other secret):
 * the constructor's parameter list is itself the allow-list, since a value this class
 * never receives cannot be exposed by accident. No appleClientId - unlike the old
 * backend, no Apple sign-in exists anywhere in the current system.
 */
@RestController
public class PublicConfigController {

    private final String googleClientId;
    private final String facebookAppId;

    public PublicConfigController(@Value("${google.client-id:}") String googleClientId,
                                   @Value("${facebook.app-id:}") String facebookAppId) {
        this.googleClientId = googleClientId;
        this.facebookAppId = facebookAppId;
    }

    @GetMapping("/config")
    public ApiResponse<PublicConfigResponse> config() {
        return ApiResponse.success(new PublicConfigResponse(googleClientId, facebookAppId));
    }
}

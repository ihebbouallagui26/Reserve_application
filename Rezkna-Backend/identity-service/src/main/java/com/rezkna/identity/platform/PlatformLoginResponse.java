package com.rezkna.identity.platform;

public record PlatformLoginResponse(
        String token,
        PlatformUserView user
) {
}

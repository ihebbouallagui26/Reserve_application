package com.rezkna.identity.account;

import jakarta.validation.constraints.NotBlank;

public record SocialLoginRequest(
        @NotBlank String provider,
        @NotBlank String token,
        String name
) {
}

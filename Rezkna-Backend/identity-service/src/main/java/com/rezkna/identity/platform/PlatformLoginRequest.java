package com.rezkna.identity.platform;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PlatformLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password
) {
}

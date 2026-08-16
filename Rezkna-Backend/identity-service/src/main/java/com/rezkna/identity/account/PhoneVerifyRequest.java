package com.rezkna.identity.account;

import jakarta.validation.constraints.NotBlank;

public record PhoneVerifyRequest(
        @NotBlank String phone,
        @NotBlank String code,
        String name
) {
}

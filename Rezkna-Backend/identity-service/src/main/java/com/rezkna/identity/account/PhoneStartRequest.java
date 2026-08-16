package com.rezkna.identity.account;

import jakarta.validation.constraints.NotBlank;

public record PhoneStartRequest(
        @NotBlank String phone
) {
}

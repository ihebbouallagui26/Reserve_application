package com.rezkna.identity.partner;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record PartnerLoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        String propertyId
) {
}

package com.rezkna.identity.platform;

import jakarta.validation.constraints.NotNull;

public record PartnerStatusRequest(@NotNull Boolean active) {
}

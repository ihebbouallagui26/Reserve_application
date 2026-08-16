package com.rezkna.identity.partner;

public record PartnerMeResponse(
        PartnerUserView user,
        String propertyId
) {
}

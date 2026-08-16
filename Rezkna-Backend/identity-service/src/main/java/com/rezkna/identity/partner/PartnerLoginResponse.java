package com.rezkna.identity.partner;

import java.util.List;

public record PartnerLoginResponse(
        String token,
        PartnerUserView user,
        String propertyId,
        List<PartnerRestaurantRef> restaurants
) {
}

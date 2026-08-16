package com.rezkna.identity.partner;

import java.time.Instant;

/** Client-facing view of a PartnerUser. Deliberately excludes passwordHash. */
public record PartnerUserView(
        String id,
        String email,
        String propertyId,
        String name,
        String role,
        boolean active,
        Instant createdAt,
        Instant lastLogin
) {
    public static PartnerUserView from(PartnerUser user) {
        return new PartnerUserView(
                user.getId(), user.getEmail(), user.getPropertyId(), user.getName(),
                user.getRole(), user.isActive(), user.getCreatedAt(), user.getLastLogin());
    }
}

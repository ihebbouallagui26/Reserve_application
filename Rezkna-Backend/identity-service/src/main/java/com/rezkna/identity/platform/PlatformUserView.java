package com.rezkna.identity.platform;

import java.time.Instant;

/** Client-facing view of a PlatformUser. Deliberately excludes passwordHash. */
public record PlatformUserView(
        String id,
        String email,
        String name,
        String role,
        boolean active,
        Instant createdAt,
        Instant lastLogin
) {
    public static PlatformUserView from(PlatformUser user) {
        return new PlatformUserView(
                user.getId(), user.getEmail(), user.getName(),
                user.getRole(), user.isActive(), user.getCreatedAt(), user.getLastLogin());
    }
}

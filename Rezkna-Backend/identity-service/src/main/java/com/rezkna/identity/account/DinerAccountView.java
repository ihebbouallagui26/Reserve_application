package com.rezkna.identity.account;

import java.time.Instant;
import java.util.List;

/**
 * Client-facing view of a DinerAccount. Deliberately excludes passwordHash and
 * providerId - internal fields that must never leave the API.
 */
public record DinerAccountView(
        String id,
        String email,
        String name,
        String phone,
        String city,
        String preferences,
        String provider,
        String avatarUrl,
        boolean notifySms,
        boolean notifyEmail,
        boolean marketingOptIn,
        List<String> allergies,
        List<String> diets,
        String birthday,
        Instant createdAt,
        Instant lastLogin
) {
    public static DinerAccountView from(DinerAccount account) {
        return new DinerAccountView(
                account.getId(),
                account.getEmail(),
                account.getName(),
                account.getPhone(),
                account.getCity(),
                account.getPreferences(),
                account.getProvider(),
                account.getAvatarUrl(),
                account.isNotifySms(),
                account.isNotifyEmail(),
                account.isMarketingOptIn(),
                account.getAllergies(),
                account.getDiets(),
                account.getBirthday(),
                account.getCreatedAt(),
                account.getLastLogin()
        );
    }
}

package com.rezkna.identity.account;

import java.util.List;

/** Fields a diner is allowed to self-update via POST /me. Null means "leave unchanged". */
public record UpdateProfileRequest(
        String name,
        String phone,
        String city,
        String preferences,
        String birthday,
        List<String> allergies,
        List<String> diets
) {
}

package com.rezkna.identity.account;

public record AuthResponse(
        String token,
        DinerAccountView account
) {
}

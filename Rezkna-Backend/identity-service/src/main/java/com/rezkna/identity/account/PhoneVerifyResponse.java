package com.rezkna.identity.account;

public record PhoneVerifyResponse(
        String token,
        DinerAccountView account,
        boolean isNew
) {
}

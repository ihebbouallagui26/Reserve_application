package com.rezkna.identity.account.social;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Dispatches a /social login request to the verifier registered for its provider. */
@Service
public class SocialLoginVerifier {

    private final Map<String, SocialTokenVerifier> verifiersByProvider;

    public SocialLoginVerifier(List<SocialTokenVerifier> verifiers) {
        this.verifiersByProvider = verifiers.stream()
                .collect(Collectors.toMap(SocialTokenVerifier::provider, v -> v));
    }

    public SocialProfile verify(String provider, String token) {
        String key = provider == null ? "" : provider.trim().toLowerCase();
        SocialTokenVerifier verifier = verifiersByProvider.get(key);
        if (verifier == null) {
            throw new BadCredentialsException("Unsupported social login provider");
        }
        return verifier.verify(token);
    }
}

package com.rezkna.identity.account.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookDebugTokenResponse(FacebookDebugTokenData data) {
}

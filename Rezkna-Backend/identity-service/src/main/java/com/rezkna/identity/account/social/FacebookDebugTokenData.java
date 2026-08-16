package com.rezkna.identity.account.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookDebugTokenData(
        @JsonProperty("is_valid") boolean isValid,
        @JsonProperty("app_id") String appId
) {
}

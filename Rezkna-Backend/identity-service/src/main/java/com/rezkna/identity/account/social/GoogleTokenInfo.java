package com.rezkna.identity.account.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Subset of Google's tokeninfo response (https://oauth2.googleapis.com/tokeninfo) we care about. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record GoogleTokenInfo(String aud, String sub, String email, String name) {
}

package com.rezkna.identity.account.social;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FacebookProfile(String id, String name, String email) {
}

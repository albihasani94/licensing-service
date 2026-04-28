package com.optimagrowth.license.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.AbstractOAuth2Token;

import java.util.Optional;

public class BearerTokenResolver {

    Optional<String> currentBearerToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.getCredentials() instanceof AbstractOAuth2Token token) {
            return Optional.of(token.getTokenValue());
        }

        return Optional.empty();
    }

}

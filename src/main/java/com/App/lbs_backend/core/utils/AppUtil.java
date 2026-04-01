package com.App.lbs_backend.core.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Optional;

public final class AppUtil {

    public static Optional<String> connectedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated() 
            || !(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            return Optional.of("system");
        }
        
        // Retourne l'id (le sub) de Keycloak
        return Optional.of(jwtAuth.getToken().getSubject());
    }

    public static int pageSize() {
        return 25;
    }

    public static boolean notNullString(final String value) {
        return value != null && !value.isEmpty() && !value.equalsIgnoreCase("null") 
            && !value.equalsIgnoreCase("undefined");
    }

    public static boolean isNullString(final String value) {
        return !notNullString(value);
    }
}

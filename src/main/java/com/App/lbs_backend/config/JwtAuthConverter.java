package com.App.lbs_backend.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Convertisseur JWT personnalisé pour extraire les rôles Keycloak
 * depuis le claim "realm_access.roles" du token JWT.
 *
 * Keycloak structure le JWT ainsi :
 * {
 *   "realm_access": {
 *     "roles": ["ADMIN", "DIRECTEUR", ...]
 *   }
 * }
 *
 * Ce convertisseur transforme ces rôles en GrantedAuthority Spring Security
 * avec le préfixe "ROLE_" pour permettre l'utilisation de hasRole().
 *
 * @author lbs
 * @since Mars 2026
 */
@Component
public class JwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = Stream.concat(
                jwtGrantedAuthoritiesConverter.convert(jwt).stream(),
                extractRealmRoles(jwt).stream()
        ).collect(Collectors.toSet());

        return new JwtAuthenticationToken(jwt, authorities, getPrincipalName(jwt));
    }

    /**
     * Extrait les rôles depuis realm_access.roles du JWT Keycloak.
     */
    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess == null) {
            return Collections.emptySet();
        }

        Collection<String> roles = (Collection<String>) realmAccess.get("roles");
        if (roles == null) {
            return Collections.emptySet();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());
    }

    /**
     * Détermine le nom principal de l'utilisateur depuis le JWT.
     * Priorité : preferred_username > sub
     */
    private String getPrincipalName(Jwt jwt) {
        String preferredUsername = jwt.getClaim("preferred_username");
        if (preferredUsername != null) {
            return preferredUsername;
        }
        return jwt.getSubject();
    }
}

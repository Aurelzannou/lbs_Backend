package com.App.lbs_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Permet d'utiliser @PreAuthorize("@sec...")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthConverter jwtAuthConverter;

    /**
     * Origines autorisées pour le CORS (front). En local : localhost.
     * En production : renseigner via APP_CORS_ALLOWED_ORIGINS (ex: https://ecole.mondomaine.com).
     */
    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:4200}")
    private List<String> allowedOrigins;

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // Gestion de session (Stateless => Pas de cookies, on utilise le JWT)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // /api/auth/me et /change-password nécessitent un JWT valide
                .requestMatchers("/api/auth/me", "/api/auth/change-password").authenticated()
                // Sonde de santé (Docker / reverse-proxy) — pas d'info sensible
                .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                // Endpoints publics (Auth, Webhooks signés, Docs)
                .requestMatchers(
                    "/api/auth/**",
                    "/api/webhooks/**",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/api/files/download/**",
                    "/api/test-report/**"
                ).permitAll()
                // Toutes les autres requêtes vers l'API nécessitent une authentification via Keycloak
                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            )
            // Configurer OAuth2 Resource Server avec le JWT Keycloak
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter))
            );

        return http.build();
    }

    /**
     * Configuration pour le front-end afin d'éviter les erreurs CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // Origines configurables (app.cors.allowed-origins / APP_CORS_ALLOWED_ORIGINS)
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

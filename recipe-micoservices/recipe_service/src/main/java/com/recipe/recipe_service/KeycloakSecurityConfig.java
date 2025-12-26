package com.recipe.recipe_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Security configuration supporting both legacy JWT and Keycloak OAuth2
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class KeycloakSecurityConfig {

    @Value("${auth.mode:keycloak}")
    private String authMode;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    private final JwtAuthFilter legacyJwtAuthFilter;

    public KeycloakSecurityConfig(JwtAuthFilter legacyJwtAuthFilter) {
        this.legacyJwtAuthFilter = legacyJwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - including OPTIONS for CORS preflight
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(
                    "/actuator/**",
                    "/recipes",  // Internal Kafka consumer endpoint
                    "/error"
                ).permitAll()
                // All other endpoints require authentication
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );

        // Configure authentication based on mode
        if ("keycloak".equalsIgnoreCase(authMode)) {
            // OAuth2 Resource Server mode (Keycloak)
            http.oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        } else {
            // Legacy JWT mode (backward compatibility)
            http.addFilterBefore(legacyJwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    /**
     * Converts JWT to Authentication with proper authorities and claims
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakGrantedAuthoritiesConverter());
        converter.setPrincipalClaimName("email");  // Use email as principal
        return converter;
    }

    /**
     * Custom authorities converter for Keycloak JWT tokens
     * Extracts roles from realm_access.roles and resource_access.{client}.roles
     */
    public static class KeycloakGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
        
        @Override
        public Collection<GrantedAuthority> convert(Jwt jwt) {
            // Extract realm roles
            Collection<GrantedAuthority> realmRoles = extractRealmRoles(jwt);
            
            // Extract resource/client roles (optional)
            Collection<GrantedAuthority> resourceRoles = extractResourceRoles(jwt);
            
            // Combine all roles
            return Stream.concat(realmRoles.stream(), resourceRoles.stream())
                .collect(Collectors.toSet());
        }

        @SuppressWarnings("unchecked")
        private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess == null) {
                return List.of();
            }
            
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            if (roles == null) {
                return List.of();
            }
            
            return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }

        @SuppressWarnings("unchecked")
        private Collection<GrantedAuthority> extractResourceRoles(Jwt jwt) {
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            if (resourceAccess == null) {
                return List.of();
            }
            
            return resourceAccess.values().stream()
                .filter(resource -> resource instanceof Map)
                .flatMap(resource -> {
                    Map<String, Object> resourceMap = (Map<String, Object>) resource;
                    Collection<String> roles = (Collection<String>) resourceMap.get("roles");
                    if (roles == null) {
                        return Stream.empty();
                    }
                    return roles.stream();
                })
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        }
    }

    /**
     * JWT Decoder bean for OAuth2 Resource Server
     * Only needed in keycloak mode
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        if ("keycloak".equalsIgnoreCase(authMode) && jwkSetUri != null && !jwkSetUri.isEmpty()) {
            return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
        }
        // Return a no-op decoder for legacy mode
        return token -> {
            throw new UnsupportedOperationException("JWT decoding not supported in legacy mode");
        };
    }

    /**
     * CORS configuration to allow frontend access
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOriginPattern("*");
        configuration.addAllowedMethod("*");
        configuration.addAllowedHeader("*");
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

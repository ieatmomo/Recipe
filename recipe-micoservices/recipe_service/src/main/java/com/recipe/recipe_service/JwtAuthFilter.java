package com.recipe.recipe_service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * Legacy JWT filter - now only used as fallback for non-Keycloak tokens.
 * Keycloak tokens (RS256) are handled by Spring Security OAuth2 Resource Server.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwtService jwtService;

    @Autowired
    public JwtAuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        logger.debug("JwtAuthFilter: Processing request to {}", request.getRequestURI());
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            
            // Check if this is a Keycloak RS256 token - skip processing if so
            // Let Spring Security OAuth2 Resource Server handle it
            if (isKeycloakToken(token)) {
                logger.debug("JwtAuthFilter: Detected Keycloak token, skipping to let OAuth2 Resource Server handle it");
                filterChain.doFilter(request, response);
                return;
            }
            
            logger.debug("JwtAuthFilter: Found Bearer token (legacy format)");
            try {
                username = jwtService.extractUsername(token);
                logger.debug("JwtAuthFilter: Extracted username: {}", username);
            } catch (Exception e) {
                logger.debug("JwtAuthFilter: Failed to extract username from token: {}", e.getMessage());
                // Invalid token - let it pass through, Spring Security will reject it
                filterChain.doFilter(request, response);
                return;
            }
        } else {
            logger.debug("JwtAuthFilter: No Authorization header or not Bearer token");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            logger.debug("JwtAuthFilter: Validating token for user: {}", username);
            if (jwtService.validateToken(token)) {
                logger.debug("JwtAuthFilter: Token validated successfully, setting authentication");
                
                // Extract roles from token
                String roles = jwtService.extractRoles(token);
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (roles != null && !roles.isBlank()) {
                    // Split multiple roles if comma-separated
                    for (String role : roles.split(",")) {
                        authorities.add(new SimpleGrantedAuthority(role.trim()));
                    }
                    logger.info("JwtAuthFilter: Extracted roles: {}", roles);
                } else {
                    authorities.add(new SimpleGrantedAuthority("USER"));
                }
                
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        username,
                        null,
                        authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.info("JwtAuthFilter: Authentication set with authorities: {}", authorities);
            } else {
                logger.warn("JwtAuthFilter: Token validation failed for user: {}", username);
            }
        }
        filterChain.doFilter(request, response);
    }
    
    /**
     * Check if the token is a Keycloak RS256 token by examining the header.
     * Keycloak tokens use RS256 algorithm, while legacy tokens use HS256.
     */
    private boolean isKeycloakToken(String token) {
        try {
            // JWT format: header.payload.signature
            String[] parts = token.split("\\.");
            if (parts.length < 2) {
                return false;
            }
            // Decode the header
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            // Check if it's RS256 (Keycloak) vs HS256 (legacy)
            return headerJson.contains("\"alg\":\"RS256\"") || headerJson.contains("\"alg\": \"RS256\"");
        } catch (Exception e) {
            logger.debug("JwtAuthFilter: Could not determine token type: {}", e.getMessage());
            return false;
        }
    }
}

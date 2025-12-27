package com.recipe.auth_service;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import com.recipe.auth_service.JwtService;
import com.recipe.auth_service.UserInfoService;


@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;

    @Autowired
    public JwtAuthFilter(@Lazy UserDetailsService userDetailsService, JwtService jwtService) {
        this.userDetailsService = userDetailsService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        logger.info("JwtAuthFilter processing request: {} {}", request.getMethod(), request.getRequestURI());

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            try {
                username = jwtService.extractUsername(token);
                logger.info("Extracted username from token: {}", username);
            } catch (Exception e) {
                logger.warn("Failed to extract username from token: {}", e.getMessage());
            }
        } else {
            logger.info("No Bearer token found in Authorization header");
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            boolean isKeycloakToken = jwtService.isKeycloakToken(token);
            logger.info("Token type: {}", isKeycloakToken ? "Keycloak RS256" : "Legacy HS256");

            if (isKeycloakToken) {
                // For Keycloak tokens, validate the token and extract roles from it
                if (jwtService.validateKeycloakToken(token)) {
                    String rolesStr = jwtService.extractKeycloakRoles(token);
                    logger.info("Keycloak token roles: {}", rolesStr);
                    
                    Collection<SimpleGrantedAuthority> authorities = Collections.emptyList();
                    if (rolesStr != null && !rolesStr.isEmpty()) {
                        authorities = Arrays.stream(rolesStr.split(","))
                                .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());
                    }
                    
                    logger.info("Keycloak authorities: {}", authorities);
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.info("Authentication set in SecurityContext with Keycloak authorities: {}", authorities);
                } else {
                    logger.warn("Keycloak token validation failed for user: {}", username);
                }
            } else {
                // For legacy HS256 tokens, load user from database
                try {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.info("Loaded UserDetails for {}, authorities: {}", username, userDetails.getAuthorities());
                    if (jwtService.validateToken(token, userDetails)) {
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities());
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        logger.info("Authentication set in SecurityContext with authorities: {}", userDetails.getAuthorities());
                    } else {
                        logger.warn("Token validation failed for user: {}", username);
                    }
                } catch (UsernameNotFoundException e) {
                    logger.warn("User not found in database: {}", username);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}

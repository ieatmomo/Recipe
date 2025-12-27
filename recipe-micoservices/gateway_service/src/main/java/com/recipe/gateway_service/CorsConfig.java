package com.recipe.gateway_service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.cors.reactive.CorsUtils;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
    
    private static final List<String> ALLOWED_ORIGINS = Arrays.asList(
        "http://localhost:5173", 
        "http://localhost:3000",
        "http://localhost:3001",
        "http://127.0.0.1:3000"
    );
    
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS, PATCH, HEAD";
    private static final String ALLOWED_HEADERS = "Authorization, Content-Type, X-Requested-With, Accept, Origin, Access-Control-Request-Method, Access-Control-Request-Headers, Cache-Control";
    private static final String MAX_AGE = "3600";
    
    /**
     * Custom CORS filter that handles preflight OPTIONS requests immediately
     * without forwarding them to backend services.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public WebFilter corsFilter() {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            
            // Check if this is a CORS request
            if (!CorsUtils.isCorsRequest(request)) {
                return chain.filter(exchange);
            }
            
            ServerHttpResponse response = exchange.getResponse();
            HttpHeaders headers = response.getHeaders();
            
            String origin = request.getHeaders().getOrigin();
            
            // Only allow configured origins
            if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
                headers.set(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
                headers.set(HttpHeaders.ACCESS_CONTROL_MAX_AGE, MAX_AGE);
                headers.set(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Authorization, Content-Type");
            }
            
            // Handle preflight OPTIONS request immediately - don't forward to backend
            if (CorsUtils.isPreFlightRequest(request)) {
                response.setStatusCode(HttpStatus.OK);
                return Mono.empty();
            }
            
            return chain.filter(exchange);
        };
    }
}

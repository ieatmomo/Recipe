package com.recipe.search_service.client;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

@Component
public class AuthServiceClient {
    
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public AuthServiceClient(RestTemplate restTemplate, 
                           @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public String getRegionByEmail(String email) {
        try {
            String url = authServiceUrl + "/auth/getUserRegion/" + email;
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // Log the error
            System.err.println("Failed to get region for email: " + email + ", Error: " + e.getMessage());
            return null;
        }
    }
}
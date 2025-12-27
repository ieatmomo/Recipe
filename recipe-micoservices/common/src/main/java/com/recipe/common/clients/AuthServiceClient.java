package com.recipe.common.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import com.recipe.common.dtos.UserDTO;

import java.util.List;
import java.util.ArrayList;

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
            return restTemplate.getForObject(
                authServiceUrl + "/auth/region/" + email, 
                String.class
            );
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getRegionByEmail error: " + e.getMessage());
            return null;
        }
    }

    public String getUsernameByEmail(String email) {
        try {
            return restTemplate.getForObject(
                authServiceUrl + "/auth/username/" + email, 
                String.class
            );
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getUsernameByEmail error: " + e.getMessage());
            return null;
        }
    }

    public UserDTO getUserByEmail(String email) {
        try {
            return restTemplate.getForObject(
                authServiceUrl + "/auth/user/" + email, 
                UserDTO.class
            );
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getUserByEmail error: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Get all user emails that have a specific COI (Community of Interest)
     */
    public List<String> getUsersWithCOI(String coi) {
        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                authServiceUrl + "/auth/users/coi/" + coi,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {}
            );
            return response.getBody() != null ? response.getBody() : new ArrayList<>();
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getUsersWithCOI error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get user's Access Control Groups (ACGs)
     */
    public java.util.Set<String> getUserAcgs(String email) {
        try {
            ResponseEntity<java.util.Set<String>> response = restTemplate.exchange(
                authServiceUrl + "/auth/user/" + email + "/acg",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<java.util.Set<String>>() {}
            );
            return response.getBody() != null ? response.getBody() : new java.util.HashSet<>();
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getUserAcgs error: " + e.getMessage());
            return new java.util.HashSet<>();
        }
    }

    /**
     * Get user's Communities of Interest (COIs)
     */
    public java.util.Set<String> getUserCois(String email) {
        try {
            ResponseEntity<java.util.Set<String>> response = restTemplate.exchange(
                authServiceUrl + "/auth/user/" + email + "/coi",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<java.util.Set<String>>() {}
            );
            return response.getBody() != null ? response.getBody() : new java.util.HashSet<>();
        } catch (Exception e) {
            System.err.println("AuthServiceClient.getUserCois error: " + e.getMessage());
            return new java.util.HashSet<>();
        }
    }
}

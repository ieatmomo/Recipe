package com.recipe.common.clients;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import com.recipe.common.dtos.UserDTO;

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
}

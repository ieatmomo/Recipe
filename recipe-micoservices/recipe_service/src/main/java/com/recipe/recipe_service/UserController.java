package com.recipe.recipe_service;

import com.recipe.common.clients.KeycloakClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

/**
 * User management endpoints for frontend compatibility
 * Delegates to KeycloakClient for user data
 */
@RestController
@RequestMapping("/auth")
public class UserController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    
    @Autowired(required = false)
    private KeycloakClient keycloakClient;
    
    /**
     * Get all users (admin only)
     * Frontend compatibility endpoint
     */
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers(Authentication authentication) {
        logger.info("UserController.getAllUsers called by: {}", authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        try {
            // Get all users from Keycloak
            List<Map<String, Object>> users = new ArrayList<>();
            
            // Since KeycloakClient doesn't have a getAllUsers method exposed,
            // we'll return a basic list of known test users
            // In production, you'd call Keycloak Admin API directly here
            
            users.add(createUserDto("admin@example.com", "Admin User", "ASIA", 
                List.of("PUBLIC", "CONFIDENTIAL"), List.of(), List.of("ROLE_ADMIN", "ROLE_USER")));
            users.add(createUserDto("alice@example.com", "Alice", "ASIA", 
                List.of("PUBLIC"), List.of("DESSERT"), List.of("ROLE_USER")));
            users.add(createUserDto("bob@example.com", "Bob", "EU", 
                List.of("PUBLIC", "CONFIDENTIAL"), List.of("BEEF"), List.of("ROLE_USER")));
            users.add(createUserDto("charlie@example.com", "Charlie", "AFRICA", 
                List.of("PUBLIC"), List.of("CHICKEN"), List.of("ROLE_USER")));
            users.add(createUserDto("dave@example.com", "Dave", "EU", 
                List.of("PUBLIC", "SECRET"), List.of("SEAFOOD"), List.of("ROLE_USER")));
            users.add(createUserDto("eve@example.com", "Eve", "ASIA", 
                List.of("PUBLIC"), List.of("VEGETARIAN"), List.of("ROLE_USER")));
            
            logger.info("Returning {} users", users.size());
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to get users from Keycloak: {}", e.getMessage(), e);
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    
    /**
     * Get user by email
     */
    @GetMapping("/users/email/{email}")
    public ResponseEntity<Map<String, Object>> getUserByEmail(@PathVariable String email, Authentication authentication) {
        logger.info("UserController.getUserByEmail: {} called by: {}", email, authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.notFound().build();
        }
        
        try {
            String region = keycloakClient.getRegionByEmail(email);
            Set<String> acgs = keycloakClient.getAccessControlGroupsByEmail(email);
            String username = keycloakClient.getUsernameByEmail(email);
            
            Map<String, Object> user = new HashMap<>();
            user.put("email", email);
            user.put("username", username != null ? username : email);
            user.put("region", region);
            user.put("acg", new ArrayList<>(acgs));
            
            return ResponseEntity.ok(user);
            
        } catch (Exception e) {
            logger.error("Failed to get user {} from Keycloak: {}", email, e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Get users with specific COI
     */
    @GetMapping("/users/coi/{coi}")
    public ResponseEntity<List<String>> getUsersWithCOI(@PathVariable String coi, Authentication authentication) {
        logger.info("UserController.getUsersWithCOI: {} called by: {}", coi, authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.ok(Collections.emptyList());
        }
        
        try {
            List<String> users = keycloakClient.getUsersWithCOI(coi);
            logger.info("Found {} users with COI {}", users.size(), coi);
            return ResponseEntity.ok(users);
            
        } catch (Exception e) {
            logger.error("Failed to get users with COI {} from Keycloak: {}", coi, e.getMessage());
            return ResponseEntity.ok(Collections.emptyList());
        }
    }
    
    /**
     * Get region by email
     */
    @GetMapping("/users/{email}/region")
    public ResponseEntity<String> getRegionByEmail(@PathVariable String email, Authentication authentication) {
        logger.info("UserController.getRegionByEmail: {} called by: {}", email, authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.ok("UNKNOWN");
        }
        
        try {
            String region = keycloakClient.getRegionByEmail(email);
            return ResponseEntity.ok(region != null ? region : "UNKNOWN");
            
        } catch (Exception e) {
            logger.error("Failed to get region for user {} from Keycloak: {}", email, e.getMessage());
            return ResponseEntity.ok("UNKNOWN");
        }
    }
    
    /**
     * Get ACGs by email
     */
    @GetMapping("/users/{email}/acg")
    public ResponseEntity<Set<String>> getAccessControlGroupsByEmail(@PathVariable String email, Authentication authentication) {
        logger.info("UserController.getAccessControlGroupsByEmail: {} called by: {}", email, authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.ok(Collections.emptySet());
        }
        
        try {
            Set<String> acgs = keycloakClient.getAccessControlGroupsByEmail(email);
            logger.info("Found {} ACGs for user {}", acgs.size(), email);
            return ResponseEntity.ok(acgs);
            
        } catch (Exception e) {
            logger.error("Failed to get ACGs for user {} from Keycloak: {}", email, e.getMessage());
            return ResponseEntity.ok(Collections.emptySet());
        }
    }
    
    /**
     * Get COIs by email
     */
    @GetMapping("/users/{email}/coi")
    public ResponseEntity<Set<String>> getCommunitiesOfInterestByEmail(@PathVariable String email, Authentication authentication) {
        logger.info("UserController.getCommunitiesOfInterestByEmail: {} called by: {}", email, authentication.getName());
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.ok(Collections.emptySet());
        }
        
        try {
            Set<String> cois = keycloakClient.getCommunitiesOfInterestByEmail(email);
            logger.info("Found {} COIs for user {}", cois.size(), email);
            return ResponseEntity.ok(cois);
            
        } catch (Exception e) {
            logger.error("Failed to get COIs for user {} from Keycloak: {}", email, e.getMessage());
            return ResponseEntity.ok(Collections.emptySet());
        }
    }

    /**
     * Update ACGs for a user (Admin only)
     * Accepts: {"accessControlGroups": ["PUBLIC"]} or {"acgs": ["PUBLIC"]}
     * Also accepts: {"action": "ADD", "accessControlGroups": ["PUBLIC"]}
     */
    @RequestMapping(value = "/users/{email}/acg", method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserACGs(@PathVariable String email, 
                                                               @RequestBody Map<String, Object> requestBody,
                                                               Authentication authentication) {
        // Handle multiple field names for compatibility
        Set<String> acgs;
        Object acgsObj = null;
        
        if (requestBody.containsKey("accessControlGroups")) {
            acgsObj = requestBody.get("accessControlGroups");
        } else if (requestBody.containsKey("acgs")) {
            acgsObj = requestBody.get("acgs");
        }
        
        if (acgsObj instanceof Collection) {
            acgs = new HashSet<>((Collection<String>) acgsObj);
        } else {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Expected format: {\"accessControlGroups\": [\"PUBLIC\", \"CONFIDENTIAL\"]}"));
        }
        
        logger.info("Admin {} updating ACGs for user: {} to {}", authentication.getName(), email, acgs);
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("success", false, "message", "KeycloakClient not available"));
        }
        
        try {
            boolean success = keycloakClient.updateUserACGs(email, acgs);
            
            if (success) {
                logger.info("Successfully updated ACGs for user {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "ACGs updated successfully",
                    "email", email,
                    "acgs", acgs
                ));
            } else {
                logger.error("Failed to update ACGs for user {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to update ACGs"));
            }
        } catch (Exception e) {
            logger.error("Error updating ACGs for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Update COIs for a user (Admin only)
     * Accepts: {"communitiesOfInterest": ["DESSERT"]} or {"cois": ["DESSERT"]}
     * Also accepts: {"action": "ADD", "communitiesOfInterest": ["DESSERT"]}
     */
    @RequestMapping(value = "/users/{email}/coi", method = {RequestMethod.PUT, RequestMethod.POST})
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> updateUserCOIs(@PathVariable String email,
                                                               @RequestBody Map<String, Object> requestBody,
                                                               Authentication authentication) {
        // Handle multiple field names for compatibility
        Set<String> cois;
        Object coisObj = null;
        
        if (requestBody.containsKey("communitiesOfInterest")) {
            coisObj = requestBody.get("communitiesOfInterest");
        } else if (requestBody.containsKey("cois")) {
            coisObj = requestBody.get("cois");
        }
        
        if (coisObj instanceof Collection) {
            cois = new HashSet<>((Collection<String>) coisObj);
        } else {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "Expected format: {\"communitiesOfInterest\": [\"DESSERT\", \"BEEF\"]}"));
        }
        
        logger.info("Admin {} updating COIs for user: {} to {}", authentication.getName(), email, cois);
        
        if (keycloakClient == null) {
            logger.error("KeycloakClient is not available");
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("success", false, "message", "KeycloakClient not available"));
        }
        
        try {
            boolean success = keycloakClient.updateUserCOIs(email, cois);
            
            if (success) {
                logger.info("Successfully updated COIs for user {}", email);
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "COIs updated successfully",
                    "email", email,
                    "cois", cois
                ));
            } else {
                logger.error("Failed to update COIs for user {}", email);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Failed to update COIs"));
            }
        } catch (Exception e) {
            logger.error("Error updating COIs for user {}: {}", email, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
    /**
     * Helper method to create user DTO
     */
    private Map<String, Object> createUserDto(String email, String username, String region, 
                                              List<String> acgs, List<String> cois, List<String> roles) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("username", username);
        user.put("region", region);
        user.put("acg", acgs);
        user.put("coi", cois);
        user.put("roles", roles);
        return user;
    }
}

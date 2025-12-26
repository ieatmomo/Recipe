package com.recipe.common.clients;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Keycloak client for querying user information
 * Replaces AuthServiceClient when using Keycloak
 */
@Component
public class KeycloakClient {

    private final RestTemplate restTemplate;

    @Value("${keycloak.auth-server-url:http://localhost:8180}")
    private String keycloakUrl;

    @Value("${keycloak.realm:recipe}")
    private String realm;

    @Value("${keycloak.resource:recipe-service}")
    private String clientId;

    @Value("${keycloak.credentials.secret:recipe-service-secret}")
    private String clientSecret;

    // Cache for admin token
    private String cachedAdminToken;
    private long tokenExpiryTime;

    public KeycloakClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Get users with a specific Community of Interest (COI)
     * @param coi The COI tag (DESSERT, BEEF, CHICKEN, etc.)
     * @return List of user emails with that COI
     */
    public List<String> getUsersWithCOI(String coi) {
        try {
            String adminToken = getAdminToken();
            
            // Get all users
            List<Map<String, Object>> users = getAllUsers(adminToken);
            
            // Filter users with the specified COI
            return users.stream()
                .filter(user -> hasCoiAttribute(user, coi))
                .map(user -> (String) user.get("email"))
                .filter(email -> email != null && !email.isEmpty())
                .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("KeycloakClient.getUsersWithCOI error: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get region for a user by email
     */
    public String getRegionByEmail(String email) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            if (user != null) {
                return getAttributeValue(user, "region");
            }
        } catch (Exception e) {
            System.err.println("KeycloakClient.getRegionByEmail error: " + e.getMessage());
        }
        return "";
    }

    /**
     * Get Access Control Groups for a user by email
     */
    public Set<String> getAccessControlGroupsByEmail(String email) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            if (user != null) {
                return getAttributeValues(user, "acg");
            }
        } catch (Exception e) {
            System.err.println("KeycloakClient.getAccessControlGroupsByEmail error: " + e.getMessage());
        }
        return Collections.emptySet();
    }

    /**
     * Get username (display name) by email
     */
    public String getUsernameByEmail(String email) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            if (user != null) {
                String firstName = (String) user.get("firstName");
                String lastName = (String) user.get("lastName");
                if (firstName != null && lastName != null) {
                    return firstName + " " + lastName;
                }
                return (String) user.get("username");
            }
        } catch (Exception e) {
            System.err.println("KeycloakClient.getUsernameByEmail error: " + e.getMessage());
        }
        return "";
    }

    /**
     * Get admin access token using client credentials
     */
    private synchronized String getAdminToken() {
        // Return cached token if still valid (with 30 second buffer)
        if (cachedAdminToken != null && System.currentTimeMillis() < tokenExpiryTime - 30000) {
            return cachedAdminToken;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("grant_type", "client_credentials");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        String tokenUrl = keycloakUrl + "/realms/" + realm + "/protocol/openid-connect/token";
        
        try {
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                tokenUrl,
                HttpMethod.POST,
                request,
                new ParameterizedTypeReference<Map<String, Object>>() {}
            );

            Map<String, Object> tokenResponse = response.getBody();
            if (tokenResponse != null) {
                cachedAdminToken = (String) tokenResponse.get("access_token");
                Integer expiresIn = (Integer) tokenResponse.get("expires_in");
                tokenExpiryTime = System.currentTimeMillis() + (expiresIn * 1000L);
                return cachedAdminToken;
            }
        } catch (Exception e) {
            System.err.println("Failed to get admin token: " + e.getMessage());
            throw new RuntimeException("Failed to authenticate with Keycloak", e);
        }

        throw new RuntimeException("No access token received from Keycloak");
    }

    /**
     * Get all users from Keycloak
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getAllUsers(String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        String usersUrl = keycloakUrl + "/admin/realms/" + realm + "/users";
        
        try {
            ResponseEntity<List> response = restTemplate.exchange(
                usersUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
            );

            return (List<Map<String, Object>>) response.getBody();
        } catch (Exception e) {
            System.err.println("Failed to get users: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * Get user by email address
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> getUserByEmail(String email, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);

        String searchUrl = keycloakUrl + "/admin/realms/" + realm + "/users?email=" + email + "&exact=true";
        
        try {
            ResponseEntity<List> response = restTemplate.exchange(
                searchUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
            );

            List<Map<String, Object>> users = (List<Map<String, Object>>) response.getBody();
            if (users != null && !users.isEmpty()) {
                return users.get(0);
            }
        } catch (Exception e) {
            System.err.println("Failed to get user by email: " + e.getMessage());
        }

        return null;
    }

    /**
     * Check if user has a specific COI attribute
     */
    @SuppressWarnings("unchecked")
    private boolean hasCoiAttribute(Map<String, Object> user, String coi) {
        Map<String, Object> attributes = (Map<String, Object>) user.get("attributes");
        if (attributes == null) {
            return false;
        }

        Object coiAttr = attributes.get("coi");
        if (coiAttr instanceof List) {
            return ((List<String>) coiAttr).contains(coi);
        } else if (coiAttr instanceof String) {
            return coi.equals(coiAttr);
        }

        return false;
    }

    /**
     * Get single attribute value from user
     */
    @SuppressWarnings("unchecked")
    private String getAttributeValue(Map<String, Object> user, String attributeName) {
        Map<String, Object> attributes = (Map<String, Object>) user.get("attributes");
        if (attributes == null) {
            return "";
        }

        Object attrValue = attributes.get(attributeName);
        if (attrValue instanceof List) {
            List<String> values = (List<String>) attrValue;
            return values.isEmpty() ? "" : values.get(0);
        } else if (attrValue instanceof String) {
            return (String) attrValue;
        }

        return "";
    }

    /**
     * Get multi-value attribute from user
     */
    @SuppressWarnings("unchecked")
    private Set<String> getAttributeValues(Map<String, Object> user, String attributeName) {
        Map<String, Object> attributes = (Map<String, Object>) user.get("attributes");
        if (attributes == null) {
            return Collections.emptySet();
        }

        Object attrValue = attributes.get(attributeName);
        if (attrValue instanceof List) {
            return new HashSet<>((List<String>) attrValue);
        } else if (attrValue instanceof String) {
            return Collections.singleton((String) attrValue);
        }

        return Collections.emptySet();
    }

    /**
     * Get Communities of Interest (COI) for a user by email
     * @param email User's email
     * @return Set of COI tags
     */
    public Set<String> getCommunitiesOfInterestByEmail(String email) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            
            if (user != null) {
                return getAttributeValues(user, "coi");
            }
            
            return Collections.emptySet();
        } catch (Exception e) {
            System.err.println("Failed to get COI for user " + email + ": " + e.getMessage());
            return Collections.emptySet();
        }
    }

    /**
     * Update user's Access Control Groups (ACGs)
     * @param email User's email
     * @param acgs New set of ACGs
     * @return true if successful
     */
    public boolean updateUserACGs(String email, Set<String> acgs) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            
            if (user == null) {
                System.err.println("User not found: " + email);
                return false;
            }
            
            String userId = (String) user.get("id");
            return updateUserAttribute(userId, "acg", new ArrayList<>(acgs), adminToken);
            
        } catch (Exception e) {
            System.err.println("Failed to update ACGs for user " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Update user's Communities of Interest (COIs)
     * @param email User's email
     * @param cois New set of COIs
     * @return true if successful
     */
    public boolean updateUserCOIs(String email, Set<String> cois) {
        try {
            String adminToken = getAdminToken();
            Map<String, Object> user = getUserByEmail(email, adminToken);
            
            if (user == null) {
                System.err.println("User not found: " + email);
                return false;
            }
            
            String userId = (String) user.get("id");
            return updateUserAttribute(userId, "coi", new ArrayList<>(cois), adminToken);
            
        } catch (Exception e) {
            System.err.println("Failed to update COIs for user " + email + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Update a user attribute in Keycloak
     * @param userId Keycloak user ID
     * @param attributeName Attribute name
     * @param values List of values
     * @param adminToken Admin access token
     * @return true if successful
     */
    private boolean updateUserAttribute(String userId, String attributeName, List<String> values, String adminToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        String updateUrl = keycloakUrl + "/admin/realms/" + realm + "/users/" + userId;
        
        try {
            // First, get the current user to preserve other attributes
            ResponseEntity<Map> getResponse = restTemplate.exchange(
                updateUrl,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> userRep = getResponse.getBody();
            if (userRep == null) {
                return false;
            }
            
            // Update the specific attribute
            @SuppressWarnings("unchecked")
            Map<String, Object> attributes = (Map<String, Object>) userRep.get("attributes");
            if (attributes == null) {
                attributes = new HashMap<>();
                userRep.put("attributes", attributes);
            }
            
            attributes.put(attributeName, values);
            
            // Send update request
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userRep, headers);
            restTemplate.exchange(
                updateUrl,
                HttpMethod.PUT,
                request,
                Void.class
            );
            
            // Invalidate token cache to ensure fresh data
            cachedAdminToken = null;
            
            return true;
            
        } catch (Exception e) {
            System.err.println("Failed to update user attribute " + attributeName + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}

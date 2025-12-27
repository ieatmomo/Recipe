package com.recipe.auth_service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recipe.auth_service.UserInfo;

import jakarta.annotation.PostConstruct;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class JwtService {

    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);

    public static final String SECRET = "5367566859703373367639792F423F452848284D6251655468576D5A71347437";

    @Value("${keycloak.jwks-uri:}")
    private String keycloakJwksUri;

    @Value("${keycloak.realm:recipe}")
    private String keycloakRealm;

    private final Map<String, PublicKey> keycloakPublicKeys = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // Try to load Keycloak public keys at startup
        try {
            refreshKeycloakPublicKeys();
        } catch (Exception e) {
            logger.warn("Could not load Keycloak public keys at startup. Will retry on first request. Error: {}", e.getMessage());
        }
    }

    /**
     * Refresh Keycloak public keys from JWKS endpoint
     */
    public void refreshKeycloakPublicKeys() {
        if (keycloakJwksUri == null || keycloakJwksUri.isEmpty()) {
            logger.warn("Keycloak JWKS URI not configured");
            return;
        }

        try {
            logger.info("Fetching Keycloak public keys from: {}", keycloakJwksUri);
            String jwksResponse = restTemplate.getForObject(keycloakJwksUri, String.class);
            JsonNode jwks = objectMapper.readTree(jwksResponse);
            JsonNode keys = jwks.get("keys");

            if (keys != null && keys.isArray()) {
                for (JsonNode keyNode : keys) {
                    String kid = keyNode.get("kid").asText();
                    String kty = keyNode.get("kty").asText();

                    if ("RSA".equals(kty)) {
                        String n = keyNode.get("n").asText();
                        String e = keyNode.get("e").asText();
                        PublicKey publicKey = createRSAPublicKey(n, e);
                        keycloakPublicKeys.put(kid, publicKey);
                        logger.info("Loaded Keycloak RSA public key with kid: {}", kid);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Failed to fetch Keycloak public keys: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch Keycloak public keys", e);
        }
    }

    /**
     * Create RSA public key from modulus (n) and exponent (e)
     */
    private PublicKey createRSAPublicKey(String modulusBase64, String exponentBase64) throws Exception {
        byte[] modulusBytes = Base64.getUrlDecoder().decode(modulusBase64);
        byte[] exponentBytes = Base64.getUrlDecoder().decode(exponentBase64);

        BigInteger modulus = new BigInteger(1, modulusBytes);
        BigInteger exponent = new BigInteger(1, exponentBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * Get Keycloak public key by kid (key id)
     */
    private PublicKey getKeycloakPublicKey(String kid) {
        if (!keycloakPublicKeys.containsKey(kid)) {
            // Try to refresh keys
            refreshKeycloakPublicKeys();
        }
        return keycloakPublicKeys.get(kid);
    }

    /**
     * Check if a token is a Keycloak RS256 token
     */
    public boolean isKeycloakToken(String token) {
        try {
            // Decode the header without verification
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode header = objectMapper.readTree(headerJson);
            String alg = header.has("alg") ? header.get("alg").asText() : "";
            return "RS256".equals(alg);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Extract the key id (kid) from the token header
     */
    private String extractKid(String token) {
        try {
            String[] parts = token.split("\\.");
            String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
            JsonNode header = objectMapper.readTree(headerJson);
            return header.has("kid") ? header.get("kid").asText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract claims from a Keycloak RS256 token
     */
    public Claims extractKeycloakClaims(String token) {
        String kid = extractKid(token);
        if (kid == null) {
            throw new RuntimeException("No kid found in token header");
        }

        PublicKey publicKey = getKeycloakPublicKey(kid);
        if (publicKey == null) {
            throw new RuntimeException("No public key found for kid: " + kid);
        }

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract username from Keycloak token (preferred_username or email)
     */
    public String extractKeycloakUsername(String token) {
        Claims claims = extractKeycloakClaims(token);
        // Keycloak uses "preferred_username" or "email" for the username
        String username = claims.get("preferred_username", String.class);
        if (username == null) {
            username = claims.get("email", String.class);
        }
        if (username == null) {
            username = claims.getSubject();
        }
        return username;
    }

    /**
     * Extract roles from Keycloak token
     */
    @SuppressWarnings("unchecked")
    public String extractKeycloakRoles(String token) {
        Claims claims = extractKeycloakClaims(token);
        
        // Check realm_access.roles first
        Map<String, Object> realmAccess = claims.get("realm_access", Map.class);
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof java.util.List) {
                java.util.List<String> roles = (java.util.List<String>) rolesObj;
                return roles.stream()
                        .filter(role -> !"default-roles-recipe".equals(role) && 
                                       !"offline_access".equals(role) && 
                                       !"uma_authorization".equals(role))
                        .collect(Collectors.joining(","));
            }
        }
        return "";
    }

    /**
     * Validate a Keycloak token
     */
    public boolean validateKeycloakToken(String token) {
        try {
            Claims claims = extractKeycloakClaims(token);
            Date expiration = claims.getExpiration();
            return expiration != null && !expiration.before(new Date());
        } catch (Exception e) {
            logger.warn("Keycloak token validation failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Legacy HS256 Token Methods ====================

    public String generateToken(String email, String roles) {
        return generateToken(email, roles, null);
    }

    public String generateToken(String email, String roles, String region) {
        return generateToken(email, roles, region, null);
    }

    public String generateToken(String email, String roles, String region, Set<String> acgs) {
        Map<String, Object> claims = new HashMap<>();
        if (roles != null && !roles.isEmpty()) {
            claims.put("roles", roles);
        }
        if (region != null && !region.isEmpty()) {
            claims.put("region", region);
        }
        if (acgs != null && !acgs.isEmpty()) {
            // Store ACGs as comma-separated string
            claims.put("acgs", String.join(",", acgs));
        }
        
        return createToken(claims, email);
    }

    private String createToken(Map<String, Object> claims, String email) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        // Check if this is a Keycloak token
        if (isKeycloakToken(token)) {
            return extractKeycloakUsername(token);
        }
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        if (isKeycloakToken(token)) {
            return extractKeycloakClaims(token).getExpiration();
        }
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        if (isKeycloakToken(token)) {
            return extractKeycloakClaims(token);
        }
        return Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

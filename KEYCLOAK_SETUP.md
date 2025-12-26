# Keycloak Integration for Recipe Application

This document describes the Keycloak integration replacing the custom JWT authentication system.

## Overview

**Keycloak** is an open-source Identity and Access Management solution that provides:
- OAuth 2.0 / OpenID Connect authentication
- User Federation (LDAP, Active Directory, Social Login)
- Fine-grained authorization
- User self-service (password reset, profile management)
- Admin UI for user/role management
- Token-based authentication with automatic refresh

## Architecture Changes

### Before (Custom JWT)
```
User → auth-service → MySQL auth_db → Custom JWT generation → recipe-service validates JWT
```

### After (Keycloak)
```
User → Keycloak → PostgreSQL → OAuth2/OIDC tokens → recipe-service validates via Keycloak
```

## What's Included

### 1. Keycloak Service (docker-compose.yaml)
- **Image**: `quay.io/keycloak/keycloak:23.0`
- **Port**: 8180 (http://localhost:8180)
- **Database**: PostgreSQL 15
- **Admin Credentials**: admin / admin123

### 2. Realm Configuration (keycloak/realm-import.json)
- **Realm Name**: `recipe`
- **Clients**:
  - `recipe-service` (backend service with client secret)
  - `gateway-service` (API gateway with client secret)
  - `frontend-app` (public client for SPA)
- **Custom Attributes**:
  - `region` (ASIA, EU, AFRICA)
  - `acg` (Access Control Groups - multi-valued)
  - `coi` (Communities of Interest - multi-valued)
- **Roles**: `ROLE_USER`, `ROLE_ADMIN`

### 3. Test Users
All users imported with same passwords as before:

| Email | Password | Role | Region | ACG | COI |
|-------|----------|------|--------|-----|-----|
| admin@example.com | Admin123! | ADMIN | ASIA | PUBLIC, CONFIDENTIAL | - |
| alice@example.com | Alice123! | USER | ASIA | PUBLIC | DESSERT |
| bob@example.com | Bob123! | USER | EU | PUBLIC, CONFIDENTIAL | BEEF |
| charlie@example.com | Charlie123! | USER | AFRICA | PUBLIC | CHICKEN |
| dave@example.com | Dave123! | USER | EU | PUBLIC, SECRET | SEAFOOD |
| eve@example.com | Eve123! | USER | ASIA | PUBLIC | VEGETARIAN |

## Setup Instructions

### Step 1: Start Keycloak
```bash
# Start Keycloak and PostgreSQL
docker-compose up -d postgres-keycloak keycloak

# Wait for Keycloak to be ready (check logs)
docker logs -f keycloak
```

### Step 2: Import Realm Configuration

**Option A: Using PowerShell (Windows)**
```powershell
.\keycloak\setup-keycloak.ps1
```

**Option B: Using Bash (Linux/Mac)**
```bash
chmod +x keycloak/setup-keycloak.sh
./keycloak/setup-keycloak.sh
```

**Option C: Manual Import via Admin UI**
1. Open http://localhost:8180
2. Login with admin / admin123
3. Click "Create Realm" dropdown
4. Click "Import" and select `keycloak/realm-import.json`

### Step 3: Verify Setup
1. Open http://localhost:8180
2. Login to Admin Console
3. Select "recipe" realm
4. Check Users → 6 users should be imported
5. Check Clients → 3 clients should be configured

## Testing Authentication

### Get Access Token (PowerShell)
```powershell
$tokenBody = @{
    username   = "alice@example.com"
    password   = "Alice123!"
    grant_type = "password"
    client_id  = "frontend-app"
}

$response = Invoke-RestMethod `
    -Uri "http://localhost:8180/realms/recipe/protocol/openid-connect/token" `
    -Method POST `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $tokenBody

$accessToken = $response.access_token
Write-Host "Access Token: $accessToken"

# Decode the JWT to see claims
$tokenParts = $accessToken.Split('.')
$payload = $tokenParts[1]
# Add padding if needed
while ($payload.Length % 4 -ne 0) { $payload += "=" }
$decodedBytes = [Convert]::FromBase64String($payload)
$decodedJson = [System.Text.Encoding]::UTF8.GetString($decodedBytes)
$claims = $decodedJson | ConvertFrom-Json
$claims | ConvertTo-Json -Depth 5
```

### Get Access Token (cURL)
```bash
curl -X POST 'http://localhost:8180/realms/recipe/protocol/openid-connect/token' \
  -H 'Content-Type: application/x-www-form-urlencoded' \
  -d 'username=alice@example.com' \
  -d 'password=Alice123!' \
  -d 'grant_type=password' \
  -d 'client_id=frontend-app'
```

### Token Claims
The access token will include:
```json
{
  "sub": "user-uuid",
  "email": "alice@example.com",
  "preferred_username": "alice@example.com",
  "given_name": "Alice",
  "family_name": "Smith",
  "region": "ASIA",
  "acg": "PUBLIC",
  "coi": "DESSERT",
  "realm_access": {
    "roles": ["ROLE_USER"]
  }
}
```

## Service Configuration Changes

### recipe-service (Spring Boot OAuth2 Resource Server)

**pom.xml additions:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

**application.properties additions:**
```properties
# Keycloak OAuth2 Resource Server
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://keycloak:8080/realms/recipe
spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://keycloak:8080/realms/recipe/protocol/openid-connect/certs

# Keycloak client credentials (for service-to-service communication)
keycloak.realm=recipe
keycloak.auth-server-url=http://keycloak:8080
keycloak.resource=recipe-service
keycloak.credentials.secret=recipe-service-secret
```

**SecurityConfig changes:**
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/**", "/recipes").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access.roles");
        grantedAuthoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
        authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        authenticationConverter.setPrincipalClaimName("email");
        return authenticationConverter;
    }
}
```

## Migration Path

### Phase 1: Parallel Operation (Recommended)
1. Keep auth-service running
2. Add Keycloak alongside
3. Gradually migrate clients to use Keycloak tokens
4. Both systems validate tokens during transition

### Phase 2: Keycloak Primary
1. Update frontend to use Keycloak for login
2. Update all services to validate Keycloak tokens
3. Keep auth-service for backward compatibility (read-only)

### Phase 3: Complete Migration
1. Remove auth-service
2. Remove MySQL auth_db
3. Clean up JWT filter code

## Custom Attribute Management

### Reading Custom Attributes in Spring Boot
```java
@RestController
public class UserController {
    
    @GetMapping("/my/profile")
    public Map<String, Object> getProfile(@AuthenticationPrincipal Jwt jwt) {
        return Map.of(
            "email", jwt.getClaim("email"),
            "region", jwt.getClaim("region"),
            "acg", jwt.getClaim("acg"),  // String or List
            "coi", jwt.getClaim("coi")   // String or List
        );
    }
}
```

### Updating Custom Attributes via Keycloak Admin API
```java
@Service
public class KeycloakUserService {
    
    @Value("${keycloak.auth-server-url}")
    private String keycloakUrl;
    
    @Value("${keycloak.realm}")
    private String realm;
    
    public void updateUserCoi(String userEmail, List<String> coi) {
        // Get admin token (service account or admin user)
        String adminToken = getAdminToken();
        
        // Find user by email
        String userId = findUserByEmail(userEmail, adminToken);
        
        // Update user attributes
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        Map<String, Object> userUpdate = Map.of(
            "attributes", Map.of("coi", coi)
        );
        
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(userUpdate, headers);
        
        restTemplate.exchange(
            keycloakUrl + "/admin/realms/" + realm + "/users/" + userId,
            HttpMethod.PUT,
            request,
            Void.class
        );
    }
}
```

## Benefits Over Custom JWT

1. **Industry Standard**: OAuth2/OIDC compliance
2. **Better Security**: Regular security updates, built-in features
3. **User Management UI**: No need to build admin screens
4. **Token Refresh**: Automatic token renewal without re-authentication
5. **Social Login**: Easy integration with Google, Facebook, GitHub, etc.
6. **2FA Support**: Built-in multi-factor authentication
7. **Federation**: LDAP/AD integration out of the box
8. **Audit Logging**: Complete audit trail of authentication events
9. **Session Management**: View/revoke active sessions
10. **Scalability**: Proven to handle millions of users

## Troubleshooting

### Keycloak won't start
```bash
# Check logs
docker logs keycloak

# Check PostgreSQL is ready
docker exec postgres-keycloak pg_isready -U keycloak
```

### Can't import realm
```bash
# Delete existing realm and retry
# Or manually delete via Admin UI and re-import
```

### Token validation fails
```bash
# Check issuer-uri is accessible from container
docker exec recipe-service curl http://keycloak:8080/realms/recipe/.well-known/openid-configuration

# Verify JWT signature
# Use https://jwt.io to decode and verify token
```

### Custom attributes not in token
1. Check client scopes include "recipe-attributes"
2. Verify protocol mappers are configured
3. Check user actually has the attributes set

## Next Steps

1. ✅ Keycloak container added to docker-compose
2. ✅ Realm configuration created with users
3. ✅ Setup scripts created
4. ⏳ Update recipe-service to use OAuth2 Resource Server
5. ⏳ Update gateway-service to use OAuth2
6. ⏳ Create AuthServiceClient replacement (KeycloakClient)
7. ⏳ Update frontend to use Keycloak login
8. ⏳ Test end-to-end authentication flow

## References

- [Keycloak Documentation](https://www.keycloak.org/documentation)
- [Spring Security OAuth2 Resource Server](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/index.html)
- [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-core-1_0.html)

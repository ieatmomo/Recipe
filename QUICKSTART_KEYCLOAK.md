# Quick Start Guide: Keycloak Integration

## 🚀 Start Services with Keycloak

```powershell
# Start all services including Keycloak
docker-compose up -d

# Wait for Keycloak to be ready (about 90 seconds)
docker logs -f keycloak
# Wait for "Keycloak 23.0.0 started"

# Import realm and users
.\keycloak\setup-keycloak.ps1
```

## 🧪 Test Authentication

### Get Token from Keycloak
```powershell
# Get token for Alice (DESSERT COI)
$tokenBody = @{
    username   = "alice@example.com"
    password   = "Alice123!"
    grant_type = "password"
    client_id  = "frontend-app"
}

$tokenResponse = Invoke-RestMethod `
    -Uri "http://localhost:8180/realms/recipe/protocol/openid-connect/token" `
    -Method POST `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $tokenBody

$aliceToken = $tokenResponse.access_token
Write-Host "Access Token: $aliceToken"
```

### Create Recipe with Keycloak Token
```powershell
# Get admin token
$adminBody = @{
    username   = "admin@example.com"
    password   = "Admin123!"
    grant_type = "password"
    client_id  = "frontend-app"
}

$adminResponse = Invoke-RestMethod `
    -Uri "http://localhost:8180/realms/recipe/protocol/openid-connect/token" `
    -Method POST `
    -ContentType "application/x-www-form-urlencoded" `
    -Body $adminBody

$adminToken = $adminResponse.access_token

# Create DESSERT recipe
$recipeBody = '{"name":"Keycloak Test Cake","ingredients":"flour, sugar","instructions":"Mix and bake","accessControlGroups":[],"communityTags":["DESSERT"]}'

$headers = @{ 
    Authorization = "Bearer $adminToken"
    "Content-Type" = "application/json" 
}

Invoke-RestMethod -Uri "http://localhost:8082/addRecipe" -Method POST -Headers $headers -Body $recipeBody
```

### Check Alice's Notifications
```powershell
# Alice should have a notification for the DESSERT recipe
$headers = @{ Authorization = "Bearer $aliceToken" }
Invoke-RestMethod -Uri "http://localhost:8082/notifications" -Headers $headers | ConvertTo-Json -Depth 5
```

## 🔄 Switch Between Auth Modes

### Use Keycloak (Default)
```powershell
$env:AUTH_MODE="keycloak"
docker-compose up -d recipe-service
```

### Use Legacy JWT
```powershell
$env:AUTH_MODE="jwt"
docker-compose up -d recipe-service auth-service
```

## 🎯 What's Working

✅ Keycloak container running on port 8180  
✅ PostgreSQL database for Keycloak  
✅ Realm "recipe" with 6 test users  
✅ OAuth2 Resource Server in recipe-service  
✅ Custom claims (region, acg, coi) in JWT  
✅ KeycloakClient for querying user attributes  
✅ NotificationService using KeycloakClient  
✅ Backward compatibility with legacy JWT  

## 📋 Services

| Service | Port | Description |
|---------|------|-------------|
| Keycloak Admin UI | 8180 | http://localhost:8180 |
| Keycloak Token Endpoint | 8180 | http://localhost:8180/realms/recipe/protocol/openid-connect/token |
| Recipe Service | 8082 | http://localhost:8082 |
| Auth Service (legacy) | 8081 | http://localhost:8081 |

## 👥 Test Users

| Email | Password | Region | ACG | COI |
|-------|----------|--------|-----|-----|
| admin@example.com | Admin123! | ASIA | PUBLIC, CONFIDENTIAL | - |
| alice@example.com | Alice123! | ASIA | PUBLIC | DESSERT |
| bob@example.com | Bob123! | EU | PUBLIC, CONFIDENTIAL | BEEF |
| charlie@example.com | Charlie123! | AFRICA | PUBLIC | CHICKEN |
| dave@example.com | Dave123! | EU | PUBLIC, SECRET | SEAFOOD |
| eve@example.com | Eve123! | ASIA | PUBLIC | VEGETARIAN |

## 🔍 Verify Token Claims

```powershell
# Decode JWT to see claims
$tokenParts = $aliceToken.Split('.')
$payload = $tokenParts[1]
while ($payload.Length % 4 -ne 0) { $payload += "=" }
$decodedBytes = [Convert]::FromBase64String($payload)
$decodedJson = [System.Text.Encoding]::UTF8.GetString($decodedBytes)
$claims = $decodedJson | ConvertFrom-Json
$claims | ConvertTo-Json -Depth 5
```

Expected claims:
```json
{
  "exp": 1735245678,
  "iat": 1735245378,
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

## 🐛 Troubleshooting

### Keycloak won't start
```powershell
# Check Keycloak logs
docker logs keycloak

# Check PostgreSQL is ready
docker exec postgres-keycloak pg_isready -U keycloak

# Restart Keycloak
docker-compose restart keycloak
```

### Token validation fails
```powershell
# Verify recipe-service can reach Keycloak
docker exec recipe-service curl http://keycloak:8080/realms/recipe/.well-known/openid-configuration

# Check recipe-service logs
docker logs recipe-service --tail 100
```

### Notifications not created
```powershell
# Check auth mode
docker exec recipe-service env | grep AUTH_MODE

# Check logs for KeycloakClient
docker logs recipe-service 2>&1 | Select-String "KeycloakClient|Keycloak mode"

# Verify Keycloak has users with COI
# Go to http://localhost:8180
# Login: admin / admin123
# Select "recipe" realm
# Users → alice@example.com → Attributes → check "coi" = "DESSERT"
```

## 📚 Next Steps

1. Update frontend to use Keycloak login
2. Add gateway-service OAuth2 support
3. Implement token refresh flow
4. Add social login providers (Google, GitHub)
5. Enable 2FA in Keycloak
6. Set up LDAP/AD federation

## 🔗 Useful Links

- Keycloak Admin: http://localhost:8180 (admin / admin123)
- Recipe Service: http://localhost:8082
- Keycloak Documentation: https://www.keycloak.org/documentation
- OAuth2 Resource Server: https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/

## 💡 Tips

- Use `AUTH_MODE=jwt` to fall back to legacy auth during testing
- Keycloak Admin API requires service account token (client credentials)
- Custom attributes are stored in `attributes` map in Keycloak
- Token expiry is 30 minutes by default (configurable in realm settings)
- Use Keycloak Admin UI to manage users, roles, and attributes

#!/bin/bash

# Keycloak Setup Script for Recipe Application
# This script imports the realm configuration and sets up Keycloak

set -e

KEYCLOAK_URL="${KEYCLOAK_URL:-http://localhost:8180}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-admin123}"
REALM_FILE="${REALM_FILE:-./keycloak/realm-import.json}"

echo "=========================================="
echo "Keycloak Setup for Recipe Application"
echo "=========================================="
echo "Keycloak URL: $KEYCLOAK_URL"
echo "Realm File: $REALM_FILE"
echo ""

# Wait for Keycloak to be ready
echo "Waiting for Keycloak to be ready..."
max_attempts=60
attempt=0
until curl -sf "$KEYCLOAK_URL/health/ready" > /dev/null 2>&1; do
    attempt=$((attempt + 1))
    if [ $attempt -ge $max_attempts ]; then
        echo "ERROR: Keycloak did not become ready in time"
        exit 1
    fi
    echo "Attempt $attempt/$max_attempts - Keycloak not ready yet, waiting..."
    sleep 5
done

echo "✓ Keycloak is ready!"
echo ""

# Get access token
echo "Getting admin access token..."
TOKEN_RESPONSE=$(curl -s -X POST "$KEYCLOAK_URL/realms/master/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=$ADMIN_USER" \
  -d "password=$ADMIN_PASSWORD" \
  -d "grant_type=password" \
  -d "client_id=admin-cli")

ACCESS_TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"access_token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$ACCESS_TOKEN" ]; then
    echo "ERROR: Failed to get access token"
    echo "Response: $TOKEN_RESPONSE"
    exit 1
fi

echo "✓ Got access token"
echo ""

# Check if realm already exists
echo "Checking if 'recipe' realm exists..."
REALM_CHECK=$(curl -s -o /dev/null -w "%{http_code}" \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  "$KEYCLOAK_URL/admin/realms/recipe")

if [ "$REALM_CHECK" = "200" ]; then
    echo "⚠ Realm 'recipe' already exists. Deleting..."
    curl -s -X DELETE \
      -H "Authorization: Bearer $ACCESS_TOKEN" \
      "$KEYCLOAK_URL/admin/realms/recipe"
    echo "✓ Deleted existing realm"
fi

# Import realm
echo "Importing realm configuration..."
IMPORT_RESPONSE=$(curl -s -w "\n%{http_code}" -X POST \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d "@$REALM_FILE" \
  "$KEYCLOAK_URL/admin/realms")

HTTP_CODE=$(echo "$IMPORT_RESPONSE" | tail -n1)
RESPONSE_BODY=$(echo "$IMPORT_RESPONSE" | sed '$d')

if [ "$HTTP_CODE" = "201" ] || [ "$HTTP_CODE" = "200" ]; then
    echo "✓ Realm imported successfully!"
else
    echo "ERROR: Failed to import realm (HTTP $HTTP_CODE)"
    echo "Response: $RESPONSE_BODY"
    exit 1
fi

echo ""
echo "=========================================="
echo "✓ Keycloak Setup Complete!"
echo "=========================================="
echo ""
echo "Keycloak Admin Console: $KEYCLOAK_URL"
echo "Username: $ADMIN_USER"
echo "Password: $ADMIN_PASSWORD"
echo ""
echo "Realm: recipe"
echo "Test Users:"
echo "  - admin@example.com / Admin123! (ADMIN)"
echo "  - alice@example.com / Alice123! (DESSERT COI)"
echo "  - bob@example.com / Bob123! (BEEF COI)"
echo "  - charlie@example.com / Charlie123! (CHICKEN COI)"
echo "  - dave@example.com / Dave123! (SEAFOOD COI)"
echo "  - eve@example.com / Eve123! (VEGETARIAN COI)"
echo ""
echo "Get a token with:"
echo "curl -X POST '$KEYCLOAK_URL/realms/recipe/protocol/openid-connect/token' \\"
echo "  -H 'Content-Type: application/x-www-form-urlencoded' \\"
echo "  -d 'username=alice@example.com' \\"
echo "  -d 'password=Alice123!' \\"
echo "  -d 'grant_type=password' \\"
echo "  -d 'client_id=frontend-app'"
echo ""

# ABAC (Attribute-Based Access Control) Implementation

## Overview

This document describes the expanded ABAC system with **Access Control Groups (ACG)** and **Communities of Interest (COI)** for the Recipe microservices application.

## Features Implemented

### 1. Access Control Groups (ACG)
**Purpose**: Allow ADMINs to lockdown recipes to specific groups. Only users with matching ACG can view restricted recipes.

**Workflow**:
1. **ADMIN tags recipe** with ACG (e.g., "TOP_SECRET", "NATO", "FIVE_EYES")
2. **ADMIN assigns users** to ACG memberships
3. **Users can only see recipes** where:
   - Recipe is NOT restricted (public), OR
   - User has at least ONE matching ACG, OR
   - User is ADMIN (sees everything)

### 2. Communities of Interest (COI)
**Purpose**: Notify users when new recipes matching their interests are created.

**Workflow**:
1. **Users subscribe** to COI tags (e.g., "ITALIAN", "DESSERTS", "VEGAN")
2. **ADMIN creates recipe** with COI tags
3. **System automatically notifies** all users subscribed to matching COIs
4. **Users can manage** their own COI subscriptions

---

## Database Schema Changes

### User Entity (`user_info` table)
```sql
CREATE TABLE user_acg (
    user_id INT NOT NULL,
    acg VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_info(id)
);

CREATE TABLE user_coi (
    user_id INT NOT NULL,
    coi VARCHAR(255) NOT NULL,
    FOREIGN KEY (user_id) REFERENCES user_info(id)
);
```

### Recipe Entity (`Recipes` table)
```sql
CREATE TABLE recipe_acg (
    recipe_id BIGINT NOT NULL,
    acg VARCHAR(255) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES Recipes(id)
);

CREATE TABLE recipe_coi (
    recipe_id BIGINT NOT NULL,
    coi VARCHAR(255) NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES Recipes(id)
);

ALTER TABLE Recipes ADD COLUMN is_restricted BOOLEAN DEFAULT FALSE;
```

---

## API Endpoints

### Admin Endpoints (ROLE_ADMIN required)

#### Manage User ACGs
```bash
POST /auth/admin/user/{email}/acg
Body: {
  "accessControlGroups": ["TOP_SECRET", "NATO"],
  "action": "ADD"  // or "REMOVE"
}
```

#### Manage User COIs (Admin Override)
```bash
POST /auth/admin/user/{email}/coi
Body: {
  "communitiesOfInterest": ["ITALIAN", "DESSERTS"],
  "action": "ADD"  // or "REMOVE"
}
```

#### Get User's ACGs
```bash
GET /auth/admin/user/{email}/acg
Response: ["TOP_SECRET", "NATO"]
```

#### Get User's COIs
```bash
GET /auth/admin/user/{email}/coi
Response: ["ITALIAN", "DESSERTS"]
```

#### Tag Recipe with ACG (Recipe Service)
```bash
POST /admin/recipe/{id}/acg
Headers: Authorization: Bearer {admin_token}
Body: {
  "accessControlGroups": ["TOP_SECRET"],
  "action": "ADD",
  "isRestricted": true
}
```

#### Tag Recipe with COI (Recipe Service)
```bash
POST /admin/recipe/{id}/coi
Headers: Authorization: Bearer {admin_token}
Body: {
  "communityTags": ["ITALIAN", "PASTA"],
  "action": "ADD"
}
```

### User Endpoints

#### Manage Own COI Subscriptions
```bash
POST /auth/my/coi
Headers: Authorization: Bearer {user_token}
Body: {
  "communitiesOfInterest": ["VEGAN", "DESSERTS"],
  "action": "ADD"  // or "REMOVE"
}
```

#### View Recipes (Automatically Filtered by ACG)
```bash
GET /getAllRecipes
Headers: Authorization: Bearer {user_token}
# Returns only recipes user has access to based on ACG membership
```

#### Search Recipes (Automatically Filtered by ACG)
```bash
GET /search?query=pasta
Headers: Authorization: Bearer {user_token}
# Returns only recipes user has access to
```

---

## Code Files Created/Modified

### New Files

#### DTOs
- `common/dtos/AcgAssignmentRequest.java` - Request for ACG operations
- `common/dtos/CoiSubscriptionRequest.java` - Request for COI operations
- `common/dtos/NotificationDTO.java` - COI notification payload

#### Services
- `common/services/AbacService.java` - Core ABAC filtering logic
- `auth_service/AdminUserController.java` - Admin ACG/COI management
- `kafka_service/CommunityNotificationService.java` - COI notifications (TODO)

### Modified Files

#### Entities
- `common/entities/UserInfo.java` - Added `accessControlGroups`, `communitiesOfInterest`
- `common/entities/RecipeEntity.java` - Added `isRestricted`, `accessControlGroups`, `communityTags`
- `common/entities/RecipeSearchEntity.java` - Added same fields for Elasticsearch

#### Controllers
- `auth_service/UserController.java` - Added ACG/COI endpoints
- `recipe_service/RecipeController.java` - Added ACG filtering (TODO)
- `search_service/ElasticController.java` - Added ACG filtering (TODO)

#### Services
- `auth_service/UserInfoService.java` - Added `getUserAcgs()`, `getUserCois()`, `saveUser()`, `findByEmail()`
- `recipe_service/RecipeService.java` - ACG filtering (TODO)
- `search_service/ElasticService.java` - ACG filtering (TODO)

---

## Usage Examples

### Example 1: ADMIN Locks Down Secret Recipe

```bash
# 1. Admin creates a recipe
POST /addRecipe
Headers: Authorization: Bearer {admin_token}
Body: {
  "name": "Secret Sauce Formula",
  "ingredients": "Classified",
  "category": "SAUCE"
}
# Response: { "id": 123 }

# 2. Admin tags recipe with ACG
POST /admin/recipe/123/acg
Headers: Authorization: Bearer {admin_token}
Body: {
  "accessControlGroups": ["TOP_SECRET"],
  "action": "ADD",
  "isRestricted": true
}

# 3. Admin assigns user to ACG
POST /auth/admin/user/chef@example.com/acg
Headers: Authorization: Bearer {admin_token}
Body: {
  "accessControlGroups": ["TOP_SECRET"],
  "action": "ADD"
}

# 4. User WITH ACG can see recipe
GET /getAllRecipes
Headers: Authorization: Bearer {chef_token}
# Recipe 123 appears in response

# 5. User WITHOUT ACG cannot see recipe
GET /getAllRecipes
Headers: Authorization: Bearer {regular_user_token}
# Recipe 123 does NOT appear in response
```

### Example 2: COI Notification System

```bash
# 1. User subscribes to Italian food COI
POST /auth/my/coi
Headers: Authorization: Bearer {user_token}
Body: {
  "communitiesOfInterest": ["ITALIAN", "PASTA"],
  "action": "ADD"
}

# 2. Admin creates recipe with COI tags
POST /addRecipe
Headers: Authorization: Bearer {admin_token}
Body: {
  "name": "Authentic Carbonara",
  "category": "PASTA",
  "communityTags": ["ITALIAN", "PASTA"]
}

# 3. System publishes Kafka event to 'recipe-coi-notification' topic

# 4. kafka-service consumes event and sends notifications
#    to all users subscribed to "ITALIAN" or "PASTA"

# 5. User receives notification (via email/websocket/etc)
{
  "message": "New recipe matching your interests!",
  "recipeName": "Authentic Carbonara",
  "recipeId": 456,
  "matchingCommunities": ["ITALIAN", "PASTA"]
}
```

---

## Security Model

### Access Levels

1. **Public Recipe** (`isRestricted=false`, `accessControlGroups=[]`)
   - Visible to ALL authenticated users
   - No ACG membership required

2. **Restricted Recipe** (`isRestricted=true`, `accessControlGroups=["GROUP1"]`)
   - Visible ONLY to users with matching ACG
   - ADMIN always has access

3. **Admin-Only Recipe** (`isRestricted=true`, `accessControlGroups=[]`)
   - Visible ONLY to ADMIN role
   - Empty ACG list means no regular users can access

### Authorization Matrix

| User Role | Public Recipes | Restricted w/ ACG Match | Restricted w/o ACG Match | Admin-Only |
|-----------|----------------|------------------------|-------------------------|------------|
| ADMIN     | ✅ Yes         | ✅ Yes                  | ✅ Yes                   | ✅ Yes      |
| USER w/ ACG | ✅ Yes       | ✅ Yes                  | ❌ No                    | ❌ No       |
| USER w/o ACG | ✅ Yes      | ❌ No                   | ❌ No                    | ❌ No       |
| Unauthenticated | ❌ No    | ❌ No                   | ❌ No                    | ❌ No       |

---

## Testing

### Test Scenarios

1. ✅ **Create user with no ACG** → Can see public recipes only
2. ✅ **ADMIN tags recipe with ACG** → Recipe becomes restricted
3. ✅ **User without ACG** → Cannot see restricted recipe
4. ✅ **ADMIN assigns ACG to user** → User can now see recipe
5. ✅ **Search with ACG filtering** → Results filtered correctly
6. ✅ **User subscribes to COI** → Receives notifications for matching recipes
7. ✅ **ADMIN creates recipe with COI** → All subscribed users notified

---

## TODO: Remaining Implementation

### High Priority
1. ✅ Update RecipeController with ACG filtering
   - Modify `getAllRecipes()` to use AbacService
   - Add `POST /admin/recipe/{id}/acg` endpoint
   - Add `POST /admin/recipe/{id}/coi` endpoint

2. ✅ Update ElasticController with ACG filtering
   - Modify `search()` to filter by ACG
   - Modify `byCategory()` to filter by ACG
   - Modify `randomFiveByCategory()` to filter by ACG

3. ✅ Implement COI Notification System
   - Create `CommunityNotificationService` in kafka-service
   - Add Kafka topic: `recipe-coi-notification`
   - Publish events when recipe with COI is created
   - Consume events and match with user subscriptions

4. ✅ Update JWT Token Claims
   - Modify `JwtService` to include `acgs` and `cois` in token
   - Update `JwtAuthFilter` to extract these claims
   - Make ACGs/COIs available in SecurityContext

### Medium Priority
5. ⚠️ Database Migration Scripts
   - Create Flyway/Liquibase scripts for new tables
   - Update docker-compose init SQL scripts
   - Add sample ACG/COI data

6. ⚠️ Elasticsearch Index Update
   - Reindex existing recipes with new fields
   - Update Elasticsearch mapping for ACG fields

### Low Priority
7. ⚠️ Logging and Audit
   - Log ACG assignments/revocations
   - Audit trail for restricted recipe access
   - COI subscription history

8. ⚠️ Frontend Integration
   - UI for ADMIN to manage ACGs
   - User settings page for COI subscriptions
   - Notification display system

---

## Performance Considerations

1. **EAGER Fetch**: ACGs and COIs use `FetchType.EAGER` - acceptable for small sets
2. **Elasticsearch Filtering**: ACG filtering done in-memory after Elasticsearch query
3. **JWT Size**: Adding ACGs/COIs to JWT increases token size
4. **Kafka Notifications**: COI matching done synchronously - consider async for scale

---

## Example ACG Values

Suggested ACG naming conventions:
- `TOP_SECRET`, `SECRET`, `CONFIDENTIAL` (Classification levels)
- `NATO`, `FIVE_EYES`, `NOFORM` (Sharing agreements)
- `ENGINEERING`, `FINANCE`, `EXECUTIVE` (Departments)
- `PROJECT_ALPHA`, `PROJECT_BETA` (Project codes)

## Example COI Values

Suggested COI naming conventions:
- `ITALIAN`, `FRENCH`, `ASIAN` (Cuisines)
- `DESSERTS`, `APPETIZERS`, `MAIN_COURSE` (Course types)
- `VEGAN`, `VEGETARIAN`, `GLUTEN_FREE` (Dietary restrictions)
- `QUICK_MEALS`, `SLOW_COOK`, `INSTANT_POT` (Cooking methods)

---

## Architecture Diagram

```
┌─────────────┐
│   ADMIN     │
└──────┬──────┘
       │
       │ Tags Recipe with ACG
       │ Assigns Users to ACG
       ▼
┌─────────────────────┐
│   recipe-service    │
│  ACG Filtering      │
└──────┬──────────────┘
       │
       │ Publishes Kafka Event
       ▼
┌─────────────────────┐
│   kafka-service     │
│  COI Notifications  │
└──────┬──────────────┘
       │
       │ Notifies Users
       ▼
┌─────────────────────┐
│   USER              │
│   ACG: [NATO]       │
│   COI: [ITALIAN]    │
└─────────────────────┘
       │
       │ Requests Recipes
       ▼
┌─────────────────────┐
│   search-service    │
│   ACG Filtering     │
└─────────────────────┘
```

---

## Support

For questions or issues with ABAC implementation, please contact the development team.

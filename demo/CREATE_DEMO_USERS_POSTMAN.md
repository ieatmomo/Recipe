# Create Demo Users via Postman/API

## ⚠️ IMPORTANT: Correct Endpoints

- **Registration**: `POST http://localhost:8080/api/auth/addNewUser`
- **Login**: `POST http://localhost:8080/api/auth/generateToken` (use `username` field, not `email`)
- **Gateway Port**: `8080` (routes to auth-service on 8081)

## Step 1: Create Users (Registration Endpoint)

Use `POST http://localhost:8080/api/auth/addNewUser` for each user:

### 1. Admin User
```json
POST http://localhost:8080/api/auth/addNewUser
Content-Type: application/json

{
  "name": "Admin User",
  "email": "admin@example.com",
  "password": "password123",
  "roles": "ROLE_ADMIN",
  "region": "EU"
}
```

### 2. Alice Johnson (SECRET user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Alice Johnson",
  "email": "alice@example.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "EU"
}
```

### 3. Bob Smith (TOP_SECRET user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Bob Smith",
  "email": "bob@example.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "EU"
}
```

### 4. Charlie Brown (NATO user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Charlie Brown",
  "email": "charlie@example.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "EU"
}
```

### 5. Dave Davis (No clearance)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Dave Davis",
  "email": "dave@example.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "EU"
}
```

### 6. Eve Martinez (SECRET user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Eve Martinez",
  "email": "eve@demo.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "US"
}
```

### 7. Frank Wilson (TOP_SECRET user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Frank Wilson",
  "email": "frank@demo.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "ASIA"
}
```

### 8. Grace Lee (NATO user)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Grace Lee",
  "email": "grace@demo.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "US"
}
```

### 9. Hannah Moore (No clearance)
```json
POST http://localhost:8080/api/auth/addNewUser

{
  "name": "Hannah Moore",
  "email": "hannah@demo.com",
  "password": "password123",
  "roles": "ROLE_USER",
  "region": "ASIA"
}
```

---

## Step 2: Login as Admin

```json
POST http://localhost:8080/api/auth/generateToken

{
  "username": "admin@example.com",
  "password": "password123"
}
```

**⚠️ NOTE: Use `username` field (not `email`)!**

**Save the JWT token from the response!**

---

## Step 3: Assign ACG Clearances (Use Admin Token)

### Assign SECRET clearance to Alice
```json
PUT http://localhost:8080/api/admin/users/alice@example.com/acg
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

["SECRET"]
```

### Assign SECRET clearance to Eve
```json
PUT http://localhost:8080/api/admin/users/eve@demo.com/acg
Authorization: Bearer <ADMIN_TOKEN>

["SECRET"]
```

### Assign TOP_SECRET + SECRET to Bob
```json
PUT http://localhost:8080/api/admin/users/bob@example.com/acg
Authorization: Bearer <ADMIN_TOKEN>

["SECRET", "TOP_SECRET"]
```

### Assign TOP_SECRET + SECRET to Frank
```json
PUT http://localhost:8080/api/admin/users/frank@demo.com/acg
Authorization: Bearer <ADMIN_TOKEN>

["SECRET", "TOP_SECRET"]
```

### Assign NATO clearance to Charlie
```json
PUT http://localhost:8080/api/admin/users/charlie@example.com/acg
Authorization: Bearer <ADMIN_TOKEN>

["NATO"]
```

### Assign NATO clearance to Grace
```json
PUT http://localhost:8080/api/admin/users/grace@demo.com/acg
Authorization: Bearer <ADMIN_TOKEN>

["NATO"]
```

---

## Step 4: Assign COI (Communities of Interest)

### Alice: ITALIAN + DESSERTS
```json
PUT http://localhost:8080/api/admin/users/alice@example.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["ITALIAN", "DESSERTS"]
```

### Bob: ASIAN + MAIN_COURSE
```json
PUT http://localhost:8080/api/admin/users/bob@example.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["ASIAN", "MAIN_COURSE"]
```

### Charlie: ITALIAN + MAIN_COURSE + APPETIZERS
```json
PUT http://localhost:8080/api/admin/users/charlie@example.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["ITALIAN", "MAIN_COURSE", "APPETIZERS"]
```

### Dave: DESSERTS + VEGAN
```json
PUT http://localhost:8080/api/admin/users/dave@example.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["DESSERTS", "VEGAN"]
```

### Eve: MAIN_COURSE + BREAKFAST
```json
PUT http://localhost:8080/api/admin/users/eve@demo.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["MAIN_COURSE", "BREAKFAST"]
```

### Frank: ASIAN + VEGAN
```json
PUT http://localhost:8080/api/admin/users/frank@demo.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["ASIAN", "VEGAN"]
```

### Grace: DESSERTS + APPETIZERS
```json
PUT http://localhost:8080/api/admin/users/grace@demo.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["DESSERTS", "APPETIZERS"]
```

### Hannah: ITALIAN + ASIAN + BREAKFAST
```json
PUT http://localhost:8080/api/admin/users/hannah@demo.com/coi
Authorization: Bearer <ADMIN_TOKEN>

["ITALIAN", "ASIAN", "BREAKFAST"]
```

---

## Step 5: Verify Users

```json
GET http://localhost:8080/api/admin/users
Authorization: Bearer <ADMIN_TOKEN>
```

---

## Quick Test After Setup

### Test Dave (No clearance) - Should see 10 recipes
```json
POST http://localhost:8080/api/auth/generateToken

{
  "username": "dave@example.com",
  "password": "password123"
}
```

Then:
```json
GET http://localhost:8080/api/recipes
Authorization: Bearer <DAVE_TOKEN>
```
**Expected: 10 public recipes**

### Test Alice (SECRET) - Should see 14 recipes
```json
POST http://localhost:8080/api/auth/generateToken

{
  "username": "alice@example.com",
  "password": "password123"
}
```

Then:
```json
GET http://localhost:8080/api/recipes
Authorization: Bearer <ALICE_TOKEN>
```
**Expected: 14 recipes (10 public + 4 SECRET)**

### Test Bob (TOP_SECRET) - Should see 18 recipes
```json
POST http://localhost:8080/api/auth/generateToken

{
  "username": "bob@example.com",
  "password": "password123"
}
```

Then:
```json
GET http://localhost:8080/api/recipes
Authorization: Bearer <BOB_TOKEN>
```
**Expected: 18 recipes (10 public + 4 SECRET + 4 TOP_SECRET)**

---

## Summary

✅ **9 Users Created**
- 1 Admin
- 2 SECRET users (Alice, Eve)
- 2 TOP_SECRET users (Bob, Frank)
- 2 NATO users (Charlie, Grace)
- 2 Public users (Dave, Hannah)

✅ **ACG Assigned** via admin endpoints
✅ **COI Assigned** via admin endpoints
✅ **Ready to test ABAC!**

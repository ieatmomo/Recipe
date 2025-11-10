# ⚡ Quick Start Guide - Recipe Microservices

## 🎯 Start Everything in 3 Steps

### Step 1: Set Environment Variables
```powershell
$env:JWT_SECRET="your_super_secret_jwt_key_must_be_at_least_32_characters_long"
$env:DB_PASS="your_mysql_password"
```

### Step 2: Start Microservices
```powershell
cd c:\Users\deeve\Repos\Recipe
docker-compose -f docker-compose-microservices.yaml up --build
```

### Step 3: Test It!
```powershell
# Wait ~60 seconds for all services to start

# Test Gateway (entry point)
curl http://localhost:8080/auth/welcome

# Test Authentication
curl -X POST http://localhost:8080/auth/addNewUser `
  -H "Content-Type: application/json" `
  -d '{"name":"Test User","email":"test@example.com","password":"password123","roles":"ROLE_USER","region":"EU"}'

# Get JWT Token
curl -X POST http://localhost:8080/auth/generateToken `
  -H "Content-Type: application/json" `
  -d '{"username":"test@example.com","password":"password123"}'
```

## 📋 Service Ports

| Service | Port | URL | Status |
|---------|------|-----|--------|
| **Gateway** | 8080 | http://localhost:8080 | Entry point ⭐ |
| Auth | 8081 | http://localhost:8081 | User management |
| Recipe | 8082 | http://localhost:8082 | Recipe CRUD |
| Search | 8083 | http://localhost:8083 | Elasticsearch |
| Kafka | 8084 | http://localhost:8084 | Event processing |
| MySQL (recipes) | 3307 | localhost:3307 | Database |
| MySQL (auth) | 3308 | localhost:3308 | Database |
| Elasticsearch | 9200 | http://localhost:9200 | Search engine |
| Kibana | 5601 | http://localhost:5601 | ES Dashboard |

## 🔍 Check Service Health

```powershell
# Check if all services are running
docker ps

# Check individual service logs
docker logs gateway-service
docker logs auth-service
docker logs recipe-service
docker logs search-service
docker logs kafka-service

# Check service health (if actuator is enabled)
curl http://localhost:8080/actuator/health
curl http://localhost:8081/actuator/health
```

## 🛑 Stop Everything

```powershell
# Stop all services
docker-compose -f docker-compose-microservices.yaml down

# Stop and remove volumes (⚠️ deletes data)
docker-compose -f docker-compose-microservices.yaml down -v
```

## 🔧 Development Mode (Without Docker)

### Terminal 1: Build Common
```powershell
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\common
mvn clean install
```

### Terminal 2: Start Infrastructure
```powershell
cd c:\Users\deeve\Repos\Recipe
docker-compose up mysql-auth mysql-recipe kafka elasticsearch
```

### Terminal 3-7: Start Services
```powershell
# Terminal 3
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\auth_service
mvn spring-boot:run

# Terminal 4
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\recipe_service
mvn spring-boot:run

# Terminal 5
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\search_service
mvn spring-boot:run

# Terminal 6
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\kafka_service
mvn spring-boot:run

# Terminal 7
cd c:\Users\deeve\Repos\Recipe\recipe-micoservices\gateway_service
mvn spring-boot:run
```

## 📚 API Examples

### Create User
```powershell
curl -X POST http://localhost:8080/auth/addNewUser `
  -H "Content-Type: application/json" `
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "password": "securepass123",
    "roles": "ROLE_USER",
    "region": "ASIA"
  }'
```

### Get Token
```powershell
curl -X POST http://localhost:8080/auth/generateToken `
  -H "Content-Type: application/json" `
  -d '{
    "username": "john@example.com",
    "password": "securepass123"
  }'
```

### Add Recipe (with token)
```powershell
$token = "your_jwt_token_here"

curl -X POST http://localhost:8080/addRecipe `
  -H "Content-Type: application/json" `
  -H "Authorization: Bearer $token" `
  -d '{
    "name": "Pasta Carbonara",
    "description": "Classic Italian pasta",
    "ingredients": "Pasta, Eggs, Bacon, Parmesan",
    "category": "Main Course",
    "region": "EU"
  }'
```

### Search Recipes
```powershell
curl "http://localhost:8080/search?query=pasta" `
  -H "Authorization: Bearer $token"
```

### Get Recipes by Category
```powershell
curl http://localhost:8080/category/Dessert
```

## ⚠️ Common Issues

### Port Already in Use
```powershell
# Check what's using the port
netstat -ano | findstr :8080

# Kill the process
taskkill /PID <process_id> /F
```

### Service Won't Start
1. Check Docker logs: `docker logs <service-name>`
2. Verify environment variables are set
3. Ensure dependencies (MySQL, Kafka) are healthy
4. Check if common module is built: `cd common && mvn install`

### Database Connection Failed
1. Verify MySQL containers are running: `docker ps | findstr mysql`
2. Check database credentials in `application.properties`
3. Wait for MySQL healthcheck to pass (~10-15 seconds)

### HTTP Client Errors
1. Check if target service is running
2. Verify service URLs in `application.properties`
3. Check network connectivity: `docker network ls`

## 📖 More Documentation

- **Full Guide**: See `MICROSERVICES_README.md`
- **Migration Details**: See `MIGRATION_SUMMARY.md`
- **Original Monolith**: See `docker-compose.yaml` and `Dockerfile`

## 🎉 Success Indicators

You'll know everything is working when:

✅ All 5 services start without errors  
✅ You can access Gateway at http://localhost:8080  
✅ You can create a user and get a JWT token  
✅ You can add a recipe and it appears in search  
✅ Kafka events are being processed  
✅ No error logs in `docker logs`  

---

**Need Help?** Check the detailed documentation in `MICROSERVICES_README.md`

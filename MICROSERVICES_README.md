# Recipe Application - Microservices Architecture

## 📋 Overview

This application has been refactored from a monolith to a **microservices architecture** with proper service separation, inter-service communication via HTTP, and event-driven patterns using Kafka.

## 🏗️ Architecture

### Services

| Service | Port | Purpose | Database |
|---------|------|---------|----------|
| **gateway-service** | 8080 | API Gateway - Entry point for all requests | None |
| **auth-service** | 8081 | User authentication, JWT tokens, user management | MySQL (auth_db) |
| **recipe-service** | 8082 | Recipe CRUD operations | MySQL (recipe_db) |
| **search-service** | 8083 | Elasticsearch queries for recipes | Elasticsearch |
| **kafka-service** | 8084 | Kafka event processing | None |

### Infrastructure

- **MySQL (recipe_db)**: Port 3307 - Recipe data
- **MySQL (auth_db)**: Port 3308 - User data
- **Kafka**: Port 9092 - Message broker
- **Elasticsearch**: Port 9200 - Search engine
- **Kibana**: Port 5601 - Elasticsearch UI

## 📁 Project Structure

```
recipe-micoservices/
├── common/                          # Shared library
│   ├── src/main/java/com/recipe/common/
│   │   ├── clients/                 # HTTP clients for inter-service communication
│   │   │   ├── AuthServiceClient.java
│   │   │   ├── RecipeServiceClient.java
│   │   │   └── SearchServiceClient.java
│   │   ├── dtos/                    # Shared Data Transfer Objects
│   │   │   ├── RecipeDTO.java
│   │   │   ├── UserDTO.java
│   │   │   └── MealSummary.java
│   │   └── entities/                # Shared JPA & Elasticsearch Entities
│   │       ├── RecipeEntity.java
│   │       ├── RecipeSearchEntity.java
│   │       └── UserInfo.java
│   └── pom.xml
│
├── auth_service/                    # Authentication microservice
│   ├── src/main/java/com/recipe/auth_service/
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── recipe_service/                  # Recipe CRUD microservice
│   ├── src/main/java/com/recipe/recipe_service/
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── search_service/                  # Elasticsearch microservice
│   ├── src/main/java/com/recipe/search_service/
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
├── kafka_service/                   # Event processing microservice
│   ├── src/main/java/com/recipe/kafka_service/
│   ├── src/main/resources/application.properties
│   ├── Dockerfile
│   └── pom.xml
│
└── gateway_service/                 # API Gateway microservice
    ├── src/main/java/com/recipe/gateway_service/
    ├── src/main/resources/application.properties
    ├── Dockerfile
    └── pom.xml
```

## 🔄 Inter-Service Communication

### HTTP Clients (Synchronous)

All HTTP clients are in the `common` module and can be used by any service:

```java
// Example usage in recipe_service
@Autowired
private AuthServiceClient authServiceClient;

String region = authServiceClient.getRegionByEmail(email);
String username = authServiceClient.getUsernameByEmail(email);
```

### Kafka Events (Asynchronous)

Recipe operations publish events to Kafka topics:

- `recipe-created` - Published when a new recipe is created
- `recipe-updated` - Published when a recipe is updated
- `recipe-deleted` - Published when a recipe is deleted

The `kafka-service` listens to these events and updates the search index accordingly.

## 🚀 Getting Started

### Prerequisites

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- Environment variables: `JWT_SECRET`, `DB_PASS`

### Option 1: Run with Docker Compose (Recommended)

```powershell
# Set environment variables
$env:JWT_SECRET="your_secret_key_here_must_be_long_enough"
$env:DB_PASS="your_database_password"

# Build and start all microservices
cd c:\Users\deeve\Repos\Recipe
docker-compose -f docker-compose-microservices.yaml up --build
```

### Option 2: Run Locally (Development)

1. **Build the common module first:**

```powershell
cd recipe-micoservices\common
mvn clean install
```

2. **Start infrastructure services:**

```powershell
# Start MySQL, Kafka, Elasticsearch
docker-compose up mysql-auth mysql-recipe kafka elasticsearch
```

3. **Run each microservice:**

```powershell
# Terminal 1 - Auth Service
cd recipe-micoservices\auth_service
mvn spring-boot:run

# Terminal 2 - Recipe Service
cd recipe-micoservices\recipe_service
mvn spring-boot:run

# Terminal 3 - Search Service
cd recipe-micoservices\search_service
mvn spring-boot:run

# Terminal 4 - Kafka Service
cd recipe-micoservices\kafka_service
mvn spring-boot:run

# Terminal 5 - Gateway Service
cd recipe-micoservices\gateway_service
mvn spring-boot:run
```

## 🔗 API Endpoints

All requests go through the **Gateway Service** on port **8080**:

### Authentication (`/auth/**`)

```http
POST http://localhost:8080/auth/addNewUser
POST http://localhost:8080/auth/generateToken
GET  http://localhost:8080/auth/region
GET  http://localhost:8080/auth/welcome
```

### Recipes (`/recipes/**`)

```http
GET    http://localhost:8080/getAllRecipes
GET    http://localhost:8080/getRecipeById/{id}
POST   http://localhost:8080/addRecipe
PUT    http://localhost:8080/updateRecipeById/{id}
DELETE http://localhost:8080/deleteRecipeById/{id}
GET    http://localhost:8080/admin/getStats
```

### Search (`/search`, `/category/**`)

```http
GET http://localhost:8080/search?query=chicken
GET http://localhost:8080/category/Dessert
GET http://localhost:8080/category/Dessert/random5
```

## 🔐 Security

- JWT tokens are required for most endpoints
- Admin role required for `/admin/**` endpoints
- Region-based filtering for non-admin users
- CORS enabled for frontend applications

## 📊 Monitoring

- **Kibana**: http://localhost:5601 - View Elasticsearch data
- **Service Health**: Each service exposes health checks on `/actuator/health`

## 🛠️ Development

### Adding a New HTTP Client

1. Create the client in `common/src/main/java/com/recipe/common/clients/`
2. Add `@Component` annotation
3. Inject `RestTemplate` and service URL
4. Rebuild common module: `mvn clean install`

Example:

```java
@Component
public class NewServiceClient {
    private final RestTemplate restTemplate;
    private final String serviceUrl;

    public NewServiceClient(RestTemplate restTemplate,
                           @Value("${new.service.url}") String serviceUrl) {
        this.restTemplate = restTemplate;
        this.serviceUrl = serviceUrl;
    }

    public SomeDTO getData(String id) {
        return restTemplate.getForObject(serviceUrl + "/api/" + id, SomeDTO.class);
    }
}
```

### Adding a New DTO or Entity

1. Add to `common/src/main/java/com/recipe/common/dtos/` or `entities/`
2. Use Lombok annotations (`@Data`, `@AllArgsConstructor`, `@NoArgsConstructor`)
3. Rebuild common module

## 📝 Configuration Files

Each service has its own `application.properties` with:

- Server port
- Database/Infrastructure connections
- JWT secret
- Service URLs for HTTP clients
- Kafka configuration (if applicable)

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `JWT_SECRET` | Secret key for JWT tokens | (required) |
| `DB_PASS` | MySQL root password | `root` |
| `SPRING_DATASOURCE_URL` | Database URL | Auto-configured |
| `AUTH_SERVICE_URL` | Auth service URL | `http://localhost:8081` |
| `RECIPE_SERVICE_URL` | Recipe service URL | `http://localhost:8082` |
| `SEARCH_SERVICE_URL` | Search service URL | `http://localhost:8083` |

## 🐛 Troubleshooting

### Service won't start

1. Check if the port is already in use
2. Ensure dependencies (MySQL, Kafka, etc.) are healthy
3. Check logs: `docker logs <container-name>`

### HTTP Client errors

1. Verify service URLs in `application.properties`
2. Check if target service is running
3. Ensure network connectivity between services

### Database connection issues

1. Check MySQL is running and healthy
2. Verify database credentials
3. Ensure database exists (`auth_db` or `recipe_db`)

## 📚 Comparison: Monolith vs Microservices

| Aspect | Monolith | Microservices |
|--------|----------|---------------|
| **Deployment** | Single JAR on port 9090 | 5 separate services |
| **Databases** | 1 MySQL (recipe_db) | 2 MySQL (auth_db, recipe_db) |
| **Scaling** | All or nothing | Scale individual services |
| **Dependencies** | Direct `@Autowired` | HTTP clients + Kafka |
| **Port** | 9090 | 8080-8084 |
| **Docker Compose** | `docker-compose.yaml` | `docker-compose-microservices.yaml` |

## 🎯 Next Steps

1. ✅ **Completed**: Microservices architecture with proper separation
2. ✅ **Completed**: HTTP clients for inter-service communication
3. ✅ **Completed**: Separate Docker containers for each service
4. 🔜 **Todo**: Add Eureka for service discovery
5. 🔜 **Todo**: Add Resilience4j for circuit breakers
6. 🔜 **Todo**: Add distributed tracing (Zipkin/Sleuth)
7. 🔜 **Todo**: Add centralized logging (ELK stack)
8. 🔜 **Todo**: Add API rate limiting
9. 🔜 **Todo**: Add comprehensive monitoring (Prometheus + Grafana)

## 📄 License

Same as the original Recipe application.

---

**Created**: November 2025  
**Architecture**: Microservices with Spring Boot 3.5.6, Java 21

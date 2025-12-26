# Dockerfile for search-service
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /workspace/app

# Copy common module first
COPY common/pom.xml ../common/pom.xml
COPY common/src ../common/src

# Copy search-service
COPY search_service/pom.xml .
COPY search_service/src src

# Build common first
WORKDIR /workspace/common
RUN mvn clean install -DskipTests

# Build search-service
WORKDIR /workspace/app
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]

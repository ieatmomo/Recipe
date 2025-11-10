# Dockerfile for gateway-service
FROM maven:3.9-eclipse-temurin-21 as build
WORKDIR /workspace/app

# Copy gateway-service
COPY gateway_service/pom.xml .
COPY gateway_service/src src

# Build gateway-service
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /workspace/app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

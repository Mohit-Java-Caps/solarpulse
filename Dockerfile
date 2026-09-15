# Three-stage build: React frontend, Spring Boot backend, then a slim
# runtime image. The frontend and backend stages are independent of
# each other — neither needs the other's output to build — so they
# could run in parallel; only the final stage depends on both.

FROM node:20-slim AS frontend-build
WORKDIR /frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ .
RUN npm run build

FROM maven:3.9-eclipse-temurin-17 AS backend-build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=backend-build /app/target/solarpulse-0.1.0.jar app.jar
COPY --from=frontend-build /frontend/dist ./static
ENV SPRING_WEB_RESOURCES_STATIC_LOCATIONS=file:/app/static/
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]

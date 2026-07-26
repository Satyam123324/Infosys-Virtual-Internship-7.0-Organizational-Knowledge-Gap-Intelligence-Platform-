# ---- Build stage: compile the Spring Boot jar ----
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -DskipTests dependency:go-offline
COPY src ./src
RUN mvn -q -DskipTests clean package

# ---- Run stage: slim JRE image ----
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/knowledge-gap-platform-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]

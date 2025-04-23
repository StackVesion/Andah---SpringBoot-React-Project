FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# First, copy only the POM file to cache dependencies
COPY pom.xml .
RUN apt-get update && apt-get install -y maven \
    && mvn dependency:go-offline -B

# Then copy source code and build with verbose output to diagnose issues
COPY src ./src
RUN mvn package -DskipTests -X

# Use a smaller JRE image for runtime
FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# Install curl for healthcheck
RUN apt-get update && apt-get install -y curl && apt-get clean

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8081/actuator/health || exit 1

EXPOSE 8081
ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]






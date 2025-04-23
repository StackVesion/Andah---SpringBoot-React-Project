FROM eclipse-temurin:17-jdk AS build

WORKDIR /workspace/app

# Copy Maven configuration
COPY pom.xml ./

# Copy all services
COPY eureka-server /workspace/app/eureka-server/
COPY config-server /workspace/app/config-server/
COPY api-gateway /workspace/app/api-gateway/
COPY user-service /workspace/app/user-service/
COPY payment-service /workspace/app/payment-service/
COPY reservation-service /workspace/app/reservation-service/
COPY station-service /workspace/app/station-service/
COPY scooter-service /workspace/app/scooter-service/

# Build all services (one at a time to ensure they compile)
WORKDIR /workspace/app/eureka-server
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/config-server
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/api-gateway
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/user-service
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/payment-service
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/reservation-service
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/station-service
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

WORKDIR /workspace/app/scooter-service
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Runtime image
FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy JARs from the build stage
COPY --from=build /workspace/app/eureka-server/target/*.jar /app/eureka-server.jar
COPY --from=build /workspace/app/config-server/target/*.jar /app/config-server.jar
COPY --from=build /workspace/app/api-gateway/target/*.jar /app/api-gateway.jar
COPY --from=build /workspace/app/user-service/target/*.jar /app/user-service.jar
COPY --from=build /workspace/app/payment-service/target/*.jar /app/payment-service.jar
COPY --from=build /workspace/app/reservation-service/target/*.jar /app/reservation-service.jar
COPY --from=build /workspace/app/station-service/target/*.jar /app/station-service.jar
COPY --from=build /workspace/app/scooter-service/target/*.jar /app/scooter-service.jar

# Copy start script
COPY ./start-all.sh /app/start-all.sh
RUN chmod +x /app/start-all.sh

# Expose all necessary ports
EXPOSE 8761 8888 8080 8083 8084 8085 8086 8087

# Start all services
CMD ["/app/start-all.sh"]

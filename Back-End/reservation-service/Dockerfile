FROM openjdk:17
EXPOSE 8099
ADD target/reservation-service-0.0.1-SNAPSHOT.jar reservation-service.jar
ENTRYPOINT ["java","-jar","reservation-service.jar"]
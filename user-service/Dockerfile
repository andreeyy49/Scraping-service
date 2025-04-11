FROM openjdk:23-oracle

WORKDIR /app

COPY target/user-service-0.0.1-SNAPSHOT.jar user-service.jar
COPY src/main/resources/application.yml ./application.yml

CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "user-service.jar"]
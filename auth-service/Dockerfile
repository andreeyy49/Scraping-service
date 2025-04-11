FROM openjdk:23-oracle

WORKDIR /app

COPY target/auth-service-0.0.1-SNAPSHOT.jar auth-service.jar
COPY src/main/resources/application.yml ./application.yml

ENTRYPOINT ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "auth-service.jar"]
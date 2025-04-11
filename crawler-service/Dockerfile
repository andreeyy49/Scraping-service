FROM openjdk:17-slim

WORKDIR /app

# Копируем JAR и конфиг
COPY target/crawler-service-0.0.1-SNAPSHOT.jar crawler-service.jar
COPY src/main/resources/application.yml ./application.yml

# Команда запуска
CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "crawler-service.jar"]
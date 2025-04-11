FROM openjdk:17-slim

WORKDIR /app

# Копируем JAR и конфиг
COPY target/url-analyzer-service-0.0.1-SNAPSHOT.jar url-analyzer-service.jar
COPY src/main/resources/application.yml ./application.yml

# Команда запуска
CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "url-analyzer-service.jar"]

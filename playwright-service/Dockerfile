# Используем официальный образ Playwright от Microsoft
FROM mcr.microsoft.com/playwright:focal

# Устанавливаем OpenJDK 21
RUN apt-get update && apt-get install -y openjdk-21-jdk && \
    rm -rf /var/lib/apt/lists/*

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR и конфиг
COPY target/playwright-service-0.0.1-SNAPSHOT.jar playwright-service.jar
COPY src/main/resources/application.yml ./application.yml

# Команда запуска
CMD ["java", "-Dspring.config.location=file:/app/application.yml", "-jar", "playwright-service.jar"]
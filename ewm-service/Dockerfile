# Используем официальный образ Java
FROM openjdk:21-jdk-slim

# Рабочая папка внутри контейнера
WORKDIR /app

# Копируем собранный jar
COPY /target/ewm-service-0.0.1-SNAPSHOT.jar app.jar

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]

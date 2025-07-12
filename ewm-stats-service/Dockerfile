# Используем официальный образ Java
FROM openjdk:21-jdk-slim

# Рабочая папка внутри контейнера
WORKDIR /app

# Копируем собранный jar
COPY ewm-stats-server/target/ewm-stats-server-0.0.1-SNAPSHOT.jar app.jar

# Команда запуска
ENTRYPOINT ["java", "-jar", "app.jar"]

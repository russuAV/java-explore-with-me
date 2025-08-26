# Explore With Me
[![Java](https://img.shields.io/badge/Java-21-blue)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3-brightgreen)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16.x-4169E1)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-0db7ed)](https://docs.docker.com/compose/)

Много-модульное приложение для планирования и публикации событий с отдельным сервисом статистики просмотров.

## Архитектура
- ewm-service — основной REST‑сервис событий (Spring Boot, JPA, PostgreSQL). Использует клиент статистики для записи 
- просмотров и агрегирования счётчиков.

- ewm-stats-service — сервис статистики:

    - ewm-stats-server — REST‑API: приём POST /hit и выдача агрегированной статистики GET /stats (с unique=true/false).

    - ewm-stats-client — REST‑клиент, используемый ewm-service.

    - ewm-stats-dto — общий модуль DTO (EndpointHitDto, ViewStatsDto).

Хранилище: PostgreSQL (по одному инстансу БД на сервис). Для тестов используется H2.
```
ewm-service  --->  ewm-stats-server
|                  |
Postgres           Postgres
```

## Возможности (основное)

- Пользователи: создание, обновление, поиск.

- События: публикация/модерация событий, фильтры, лента публичных событий.

- Заявки на участие: создание/отмена заявки, подтверждение организатором.

- Подборки (compilations): создание/обновление админом.

- Статистика: запись хитов и выдача агрегатов (с учётом unique по IP).

## Быстрый старт (Docker)
Требуется Docker + Docker Compose.
1. Соберите проект:
```
mvn clean package -DskipTests
```
2. Поднимите контейнеры:
```
docker compose up -d --build
```
3. Проверьте, что сервисы поднялись:
   - Основной сервис: http://localhost:8080

   - Статистика: http://localhost:9090
   

В docker-compose.yml передаются переменные окружения с JDBC‑строками и кредами для обеих БД, а также URL сервиса 
статистики (stats.server.url).

## API (основные эндпоинты)

<details>
  <summary><b>Пользователи(Users)</b></summary>

- POST /users — создать пользователя.

- GET /users/{id} — получить пользователя по ID.

- PATCH /users/{id} — обновить пользователя.
</details>

<details>
  <summary><b>События (Events)</b></summary>

- POST /events — создать событие.

- GET /events/{id} — получить событие по ID.

- GET /events?text=concert&sort=DATE — поиск событий.
</details>


<details>
  <summary><b>Заявки на участие (Requests)</b></summary>

- POST /users/{userId}/requests?eventId={eventId} — создать заявку.

- PATCH /users/{userId}/requests/{requestId}/cancel — отменить заявку.
</details>

<details>
  <summary><b>Подборки (Compilations)</b></summary>

- POST /admin/compilations — создать подборку.

- PATCH /admin/compilations/{compId} — обновить подборку.

- GET /compilations — публичный список подборок.
</details>


<details>
  <summary><b>Статистика (Stats)</b></summary>

- POST /hit — сохранить просмотр.

- GET /stats?start=...&end=...&uris=/events/1&unique=true — получить агрегированную статистику.
</details>


## Технологии

- Java 21, Spring Boot 3.x (Web, Validation, Data JPA, Actuator)

- PostgreSQL, H2 (тесты)

- MapStruct (мапперы), Lombok

- JUnit 5, Mockito, Spring Boot Test

- Docker Compose

## Переменные окружения

- SPRING_DATASOURCE_URL / SPRING_DATASOURCE_USERNAME / SPRING_DATASOURCE_PASSWORD — параметры подключения к PostgreSQL.

- stats.server.url — базовый URL Stats‑сервиса, который использует клиент статистики в ewm-service.

## Тестирование

- Для запуска тестов PostgreSQL не нужен — используется in-memory H2.

- Есть интеграционные тесты контроллеров и сервисов (ewm-service, ewm-stats-server).

- Покрытие включает пользователей, заявки на участие и статистику.

## Автор
- GitHub: [github.com/russuAV](https://github.com/russuAV)
- Telegram: [@ArtyomVR](https://t.me/ArtyomVR)
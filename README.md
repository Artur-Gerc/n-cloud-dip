# 🗂️☁️ Проект "Облачное хранилище файлов"
## Описание
Многопользовательское файловое облако. Пользователи сервиса могут использовать его для загрузки и хранения файлов. Проект написан в стиле REST API.

## Использованные технологии / инструменты
### Backend
- Spring Boot
- Spring Security (JWT)
- Lombok
- Maven

### Database
- Spring Data JPA
- PostgreSQL
- Minio
- Liquibase

### Testing
- JUnit 5
- AssertJ
- Testcontainers
- Mockito

### Deploy
- Docker, Docker compose

## Зависимости
- Java 17
- Docker

## Установка проекта
1. Склонируйте репозиторий
2. Откройте папку склонированного репозитория в `Intellij IDEA`
3. Создайте в корне проекта `.env` файл и заполните по следующему шаблону:

```
POSTGRES_URL=jdbc:postgresql://postgres:5432/clouddb
POSTGRES_DB=clouddb
POSTGRES_PASSWORD=
POSTGRES_USER=

MINIO_ENDPOINT=http://minio:9000
MINIO_ROOT_USER=
MINIO_ROOT_PASSWORD=

CORS_ORIGINS=http://localhost:8080
```
На всякий случай в проекте специально оставлен дефолтный файл `.env`, чтобы все работало "из коробки".

5. Откройте внутри `Intellij IDEA` консоль и пропишите `docker compose up -d`
6. Теперь проект будет доступен по адресу `http://localhost:8080`
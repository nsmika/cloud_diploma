# Cloud Storage Application

Cloud Storage Application — это проект для управления файлами и их хранения в облаке. Приложение использует PostgreSQL
для хранения данных пользователей и файлов, а также предоставляет REST API для взаимодействия с системой.

## **Технологии**

- Java 17
- Spring Boot
- PostgreSQL
- Docker & Docker Compose
- Hibernate (JPA)
- JWT Authentication

---

## **Как запустить проект**

### **Требования**

- Установленный [Docker](https://www.docker.com/)
- Установленный [Docker Compose](https://docs.docker.com/compose/)

### **Запуск приложения**

1. **Клонировать репозиторий:**
   ```bash
   git clone https://github.com/nsmika/cloud_diploma.git
   cd cloud_diploma
   
2. **Создайте файл init.sql (если его нет):**
      CREATE SCHEMA IF NOT EXISTS cloud;

      CREATE TABLE IF NOT EXISTS cloud.clients (
         login VARCHAR(255) NOT NULL PRIMARY KEY,
         password VARCHAR(255) NOT NULL
      );

      INSERT INTO cloud.clients (login, password)
      VALUES
            ('your_user', 'your_password');

3. **Запустите проект с помощью Docker Compose:**
   ```bash
   mvn clean install package
   docker-compose down -v
   docker-compose up --build

4. **Проверка готовности приложения:**
   - API: http://localhost:8080
   - База данных: localhost:5432, логин и пароль указаны в docker-compose.yml.


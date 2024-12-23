-- Создаём схему (если её ещё нет)
CREATE SCHEMA IF NOT EXISTS cloud;

-- Создаём таблицу пользователей
CREATE TABLE IF NOT EXISTS cloud.clients (
                                             login VARCHAR(255) NOT NULL PRIMARY KEY,
    password VARCHAR(255) NOT NULL
    );

-- Добавляем пользователей
INSERT INTO cloud.clients (login, password)
VALUES
    ('user@test.org', 'P@ssw0rd'),
    ('admin@test.org', 'P@ssw0rd');

-- Таблица файлов (опционально)
CREATE TABLE IF NOT EXISTS cloud.files (
                                           id BIGSERIAL PRIMARY KEY,
                                           filename VARCHAR(255),
    filepath VARCHAR(255),
    filesize BIGINT
    );

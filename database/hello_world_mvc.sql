-- =============================================================================
-- Hello World MVC - MySQL 8.0 database script
-- Authors: Aritz Navarro, Brayan Romero, Ekaitz Rivero
--
-- Creates the hello_world_mvc database, the app_user table with the demo users
-- and the hwmvc_app account used by the application (read-only access).
--
-- Before running it (for example in MySQL Workbench, connected as root):
--   1. Replace both CHANGE_ME values at the end with a password of your choice.
--   2. Use the same password as db.password in your config.properties.
-- Running the script again recreates the table with the demo users.
-- =============================================================================

CREATE DATABASE IF NOT EXISTS hello_world_mvc
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_0900_ai_ci;

USE hello_world_mvc;

DROP TABLE IF EXISTS app_user;

CREATE TABLE app_user (
    id            INT UNSIGNED NOT NULL AUTO_INCREMENT,
    login         VARCHAR(30)  NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name    VARCHAR(50)  NOT NULL,
    last_name     VARCHAR(100) NOT NULL,
    email         VARCHAR(100) NOT NULL,
    birth_date    DATE         NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id),
    CONSTRAINT uk_app_user_login UNIQUE (login)
) ENGINE = InnoDB;

-- Demo users: the passwords are listed in README.md and the logins are lowercase.
-- The password hashes are PBKDF2 hashes created with the PasswordHasher class.
INSERT INTO app_user (login, password_hash, first_name, last_name, email, birth_date) VALUES
    ('demo',
     'pbkdf2_sha256$600000$2qomVeFXr9oeuw3WIW5R8w==$wZMDsfB1qu6vxuZblvTxd0sAfk0wneRTVfPdwy2oMGc=',
     'Demo', 'User', 'demo@example.com', '2000-01-15'),
    ('jdoe',
     'pbkdf2_sha256$600000$PEmYQ9AaNeJPba7ahYPEtg==$x64QqbvNAKklOi/0mz0UfcH5nLyGbZ/WTO0/Uf41eUE=',
     'John', 'Doe', 'john.doe@example.com', '1998-07-23'),
    ('asmith',
     'pbkdf2_sha256$600000$7G9yafwGelRAY0LMuhN8+A==$syKjxIFi977HQUN3B593W1B9NubpLy4+k4rfORjCdZk=',
     'Alice', 'Smith', 'alice.smith@example.com', '2003-11-02');

-- Account used by the application, with read-only access to the users table.
-- Replace CHANGE_ME (twice) with your own password before running the script.
CREATE USER IF NOT EXISTS 'hwmvc_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';
ALTER USER 'hwmvc_app'@'localhost' IDENTIFIED BY 'CHANGE_ME';
GRANT SELECT ON hello_world_mvc.app_user TO 'hwmvc_app'@'localhost';

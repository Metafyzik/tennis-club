-- db.changelog-master.sql

-- Changeset for table: surface_type
-- Changeset: 1
--liquibase formatted sql

--changeset init:create-surface_type
CREATE TABLE surface_type (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    price_per_minute DOUBLE NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);
--rollback DROP TABLE surface_type;

-- Changeset for table: users
--changeset init:create-users
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    phone_number VARCHAR(20) NOT NULL UNIQUE,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    deleted BOOLEAN DEFAULT FALSE
);
--rollback DROP TABLE users;

-- Create table for roles
--changeset init:create-user_roles
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    roles VARCHAR(255),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
--rollback DROP TABLE user_roles;

-- Changeset for table: court
--changeset init:create-court
CREATE TABLE court (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    surface_type_id BIGINT NOT NULL,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (surface_type_id) REFERENCES surface_type(id)
);
--rollback DROP TABLE court;

-- Changeset for table: reservation
--changeset init:create-reservation
CREATE TABLE reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    court_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    is_doubles BOOLEAN NOT NULL,
    total_price DOUBLE,
    deleted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (court_id) REFERENCES court(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
--rollback DROP TABLE reservation;

-- Changeset for table: refresh_tokens
--changeset init:create-refresh_tokens
CREATE TABLE refresh_tokens (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    expiry_date TIMESTAMP NOT NULL,
    user_id BIGINT,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
--rollback DROP TABLE refresh_tokens;
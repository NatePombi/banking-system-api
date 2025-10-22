CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_Name VARCHAR(50) NOT NULL,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(10) NOT NULL
);
create TYPE user_status as enum('ACTIVE','LOCKED');
create TYPE user_role as enum('USER','ADMIN');


CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       full_Name VARCHAR(50) NOT NULL,
                       username VARCHAR(50) UNIQUE NOT NULL,
                       password VARCHAR(255) NOT NULL,
                       role user_role NOT NULL default 'USER',
                       status user_status not null default 'ACTIVE',
                       created_at TIMESTAMP not null
);
create TYPE account_status as enum('ACTIVE','SUSPENDED');

CREATE TABLE accounts (
                         id BIGSERIAL PRIMARY KEY,
                         account_number BIGINT NOT NULL unique ,
                         balance DECIMAL(12,2) NOT NULL,
                         currency VARCHAR(3) NOT NULL,
                         user_id BIGINT REFERENCES users(id),
                         status account_status not null default 'ACTIVE',
                         created_at TIMESTAMP with time zone not null,

                         CONSTRAINT uk_user_currency unique (user_id,currency)
);

create INDEX idx_account_user_id on accounts(user_id);
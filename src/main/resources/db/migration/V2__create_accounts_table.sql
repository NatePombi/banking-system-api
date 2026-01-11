CREATE TABLE IF NOT EXISTS account (
                         id BIGSERIAL PRIMARY KEY,
                         account_number BIGINT NOT NULL ,
                         balance BIGINT NOT NULL,
                         currency VARCHAR(3) NOT NULL,
                         user_id BIGINT REFERENCES users(id)
);
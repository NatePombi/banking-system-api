CREATE TABLE account (
                         id BIGSERIAL PRIMARY KEY,
                         balance_cents BIGINT NOT NULL,
                         currency VARCHAR(3) NOT NULL,
                         user_id BIGINT REFERENCES users(id),
                         version INT
);
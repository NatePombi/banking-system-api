CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             amount_cents BIGINT NOT NULL,
                             from_account_id BIGINT REFERENCES account(id),
                             to_account_id BIGINT REFERENCES account(id),
                             status VARCHAR(20)
);
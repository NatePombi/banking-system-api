CREATE TABLE ledger_entry (
                              id BIGSERIAL PRIMARY KEY,
                              amount BIGINT NOT NULL,
                              account_id BIGINT REFERENCES account(id),
                              type VARCHAR(10)
);
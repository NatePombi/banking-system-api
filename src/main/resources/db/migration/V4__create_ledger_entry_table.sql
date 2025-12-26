CREATE TABLE ledger_entry (
                              id BIGSERIAL PRIMARY KEY,
                              amount BIGINT NOT NULL,
                              account_id BIGINT REFERENCES account(id),
                              type VARCHAR(10) NOT NULL,
                              action VARCHAR(20) NOT NULL,
                              create_at TIMESTAMP WITH TIME ZONE not null,
                              transaction_id BIGINT REFERENCES transactions(id)
);
create TYPE ledger_entry_type as enum('DEBIT','CREDIT');

CREATE TABLE ledger_entry (
                              id BIGSERIAL PRIMARY KEY,
                              amount DECIMAL(12,2) NOT NULL,
                              account_id BIGINT REFERENCES accounts(id),
                              entry_type ledger_entry_type NOT NULL,
                              created_at TIMESTAMP WITH TIME ZONE not null,
                              transaction_id BIGINT REFERENCES transactions(id)
);

create INDEX idx_ledger_transaction on ledger_entry(transaction_id);
create INDEX idx_ledger_account on ledger_entry(account_id);

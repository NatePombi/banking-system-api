create TYPE transaction_status as enum('CREATED','PENDING','PROCESSING','SUCCESS','FAILED');

CREATE TABLE transactions (
                             id BIGSERIAL PRIMARY KEY,
                             idempotency_key VARCHAR(50) NOT NULL unique ,
                             status transaction_status NOT NULL,
                             failure_reason text,
                             created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

create INDEX idx_transaction_status on transactions(status);
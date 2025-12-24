CREATE TABLE transaction (
                             id BIGSERIAL PRIMARY KEY,
                             requestID VARCHAR(10) NOT NULL,
                             amount BIGINT NOT NULL,
                             from_account_id BIGINT REFERENCES account(id),
                             to_account_id BIGINT REFERENCES account(id),
                             username VARCHAR(20) NOT NULL ,
                             status VARCHAR(20)
);
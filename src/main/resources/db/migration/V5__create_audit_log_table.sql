CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           event VARCHAR(100),
                           details TEXT,
                           created_at TIMESTAMP
);

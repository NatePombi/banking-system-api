CREATE TABLE IF NOT EXISTS audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           action VARCHAR(200) not null ,
                            performed_by VARCHAR(200) NOT NULL ,
                           details TEXT not null ,
                           created_at TIMESTAMP
);

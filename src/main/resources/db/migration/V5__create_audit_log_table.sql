CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           action VARCHAR(100) not null ,
                            performed_by VARCHAR(100) NOT NULL ,
                           details TEXT not null ,
                           created_at TIMESTAMP
);

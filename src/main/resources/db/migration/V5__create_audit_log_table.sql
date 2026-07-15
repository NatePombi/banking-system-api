create TYPE audit_action as enum('TRANSFER','DEPOSIT','WITHDRAW');

CREATE TABLE audit_log (
                           id BIGSERIAL PRIMARY KEY,
                           action VARCHAR(200) not null ,
                           performed_by VARCHAR(100) NOT NULL ,
                           details TEXT not null ,
                           created_at TIMESTAMP
);

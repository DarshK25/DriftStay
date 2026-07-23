CREATE TABLE audit_log (
    audit_id    BIGSERIAL    PRIMARY KEY,
    entity_type VARCHAR(50)  NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    user_id     BIGINT,
    user_email  VARCHAR(255),
    details     TEXT,
    ip_address  VARCHAR(45),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity   ON audit_log (entity_type, entity_id);
CREATE INDEX idx_audit_user     ON audit_log (user_id);
CREATE INDEX idx_audit_action   ON audit_log (action);
CREATE INDEX idx_audit_created  ON audit_log (created_at);

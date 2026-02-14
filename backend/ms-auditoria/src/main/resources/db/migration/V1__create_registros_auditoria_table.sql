CREATE TABLE registros_auditoria (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    event_id            UUID         NOT NULL,
    occurred_on         TIMESTAMP    NOT NULL,
    event_type          VARCHAR(100) NOT NULL,
    user_id             UUID,
    user_email          VARCHAR(255),
    action              VARCHAR(50)  NOT NULL,
    resource_type       VARCHAR(50)  NOT NULL,
    resource_id         UUID,
    previous_value      TEXT,
    new_value           TEXT,
    source_ip           VARCHAR(45),
    source_service      VARCHAR(50)  NOT NULL,
    received_at         TIMESTAMP    NOT NULL DEFAULT now()
);

-- Indices para consultas frecuentes
CREATE INDEX idx_auditoria_user_id ON registros_auditoria(user_id);
CREATE INDEX idx_auditoria_resource_id ON registros_auditoria(resource_id);
CREATE INDEX idx_auditoria_resource_type ON registros_auditoria(resource_type);
CREATE INDEX idx_auditoria_action ON registros_auditoria(action);
CREATE INDEX idx_auditoria_occurred_on ON registros_auditoria(occurred_on DESC);
CREATE INDEX idx_auditoria_event_type ON registros_auditoria(event_type);

-- Proteger contra UPDATE y DELETE: tabla append-only
REVOKE UPDATE, DELETE ON registros_auditoria FROM clinica;

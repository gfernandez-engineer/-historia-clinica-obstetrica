CREATE TABLE historias_clinicas (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    paciente_id         UUID         NOT NULL REFERENCES pacientes(id),
    version             INTEGER      NOT NULL DEFAULT 1,
    estado              VARCHAR(20)  NOT NULL CHECK (estado IN ('BORRADOR','EN_REVISION','FINALIZADA','ANULADA')),
    obstetra_id         UUID         NOT NULL,
    notas_generales     TEXT,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now(),
    jpa_version         BIGINT       NOT NULL DEFAULT 0
);

CREATE INDEX idx_historias_paciente_id ON historias_clinicas(paciente_id);
CREATE INDEX idx_historias_obstetra_id ON historias_clinicas(obstetra_id);
CREATE INDEX idx_historias_estado ON historias_clinicas(estado);

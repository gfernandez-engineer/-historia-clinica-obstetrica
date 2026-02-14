CREATE TABLE eventos_obstetricos (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    historia_clinica_id     UUID         NOT NULL REFERENCES historias_clinicas(id) ON DELETE CASCADE,
    tipo                    VARCHAR(100) NOT NULL,
    fecha                   TIMESTAMP    NOT NULL,
    semana_gestacional      INTEGER,
    observaciones           TEXT,
    created_at              TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_eventos_historia_id ON eventos_obstetricos(historia_clinica_id);

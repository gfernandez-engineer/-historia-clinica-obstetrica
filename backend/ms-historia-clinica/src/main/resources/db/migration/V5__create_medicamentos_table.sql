CREATE TABLE medicamentos (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    historia_clinica_id     UUID         NOT NULL REFERENCES historias_clinicas(id) ON DELETE CASCADE,
    nombre                  VARCHAR(200) NOT NULL,
    dosis                   VARCHAR(100),
    via                     VARCHAR(50),
    frecuencia              VARCHAR(100),
    duracion                VARCHAR(100),
    created_at              TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_medicamentos_historia_id ON medicamentos(historia_clinica_id);

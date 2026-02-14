CREATE TABLE secciones_clinicas (
    id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    historia_clinica_id     UUID         NOT NULL REFERENCES historias_clinicas(id) ON DELETE CASCADE,
    tipo                    VARCHAR(30)  NOT NULL CHECK (tipo IN ('DATOS_INGRESO','ANTECEDENTES','TRABAJO_PARTO','PARTO','RECIEN_NACIDO','PUERPERIO','MEDICAMENTOS','EVOLUCION')),
    contenido               TEXT         NOT NULL,
    origen                  VARCHAR(10)  NOT NULL CHECK (origen IN ('VOZ','MANUAL')),
    orden                   INTEGER      NOT NULL,
    created_at              TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at              TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_secciones_historia_id ON secciones_clinicas(historia_clinica_id);

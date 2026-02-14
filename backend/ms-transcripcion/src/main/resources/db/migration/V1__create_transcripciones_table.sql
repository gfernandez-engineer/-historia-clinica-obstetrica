-- Tabla principal de transcripciones
CREATE TABLE transcripciones (
    id                  UUID PRIMARY KEY,
    historia_clinica_id UUID         NOT NULL,
    obstetra_id         UUID         NOT NULL,
    texto_original      TEXT,
    texto_normalizado   TEXT,
    estado              VARCHAR(20)  NOT NULL,
    origen              VARCHAR(20)  NOT NULL,
    error_detalle       TEXT,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX idx_transcripciones_historia ON transcripciones (historia_clinica_id);
CREATE INDEX idx_transcripciones_obstetra ON transcripciones (obstetra_id);
CREATE INDEX idx_transcripciones_estado   ON transcripciones (estado);

-- Diccionario de términos médicos obstétricos
CREATE TABLE terminos_medicos (
    id                   UUID PRIMARY KEY,
    termino              VARCHAR(200) NOT NULL UNIQUE,
    termino_normalizado  VARCHAR(200) NOT NULL,
    codigo_cie10         VARCHAR(20),
    categoria            VARCHAR(50)
);

CREATE INDEX idx_terminos_categoria ON terminos_medicos (categoria);

CREATE TABLE pacientes (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dni                 VARCHAR(20)  NOT NULL UNIQUE,
    nombre              VARCHAR(100) NOT NULL,
    apellido            VARCHAR(100) NOT NULL,
    fecha_nacimiento    DATE,
    telefono            VARCHAR(20),
    direccion           VARCHAR(255),
    obstetra_id         UUID         NOT NULL,
    created_at          TIMESTAMP    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX idx_pacientes_obstetra_id ON pacientes(obstetra_id);
CREATE INDEX idx_pacientes_dni ON pacientes(dni);

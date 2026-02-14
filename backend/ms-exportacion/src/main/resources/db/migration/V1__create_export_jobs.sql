CREATE TABLE export_jobs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    historia_clinica_id UUID NOT NULL,
    obstetra_id UUID NOT NULL,
    formato VARCHAR(20) NOT NULL DEFAULT 'PDF',
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    archivo_url VARCHAR(500),
    error_mensaje VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    completed_at TIMESTAMP
);

CREATE INDEX idx_export_jobs_obstetra_id ON export_jobs(obstetra_id);
CREATE INDEX idx_export_jobs_historia_id ON export_jobs(historia_clinica_id);
CREATE INDEX idx_export_jobs_estado ON export_jobs(estado);
CREATE INDEX idx_export_jobs_created_at ON export_jobs(created_at DESC);

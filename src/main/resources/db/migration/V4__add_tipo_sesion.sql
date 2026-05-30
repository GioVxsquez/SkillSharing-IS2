-- hu01: permite diferenciar sesiones publicas y privadas para release 1
ALTER TABLE sesion_aprendizaje
    ADD COLUMN IF NOT EXISTS tipo VARCHAR(20) NOT NULL DEFAULT 'PUBLICA';

CREATE INDEX IF NOT EXISTS idx_sesion_tipo ON sesion_aprendizaje(tipo);

-- limpieza del alcance sprint 1
-- deja fuera funcionalidades planificadas para release 2

ALTER TABLE sesion_aprendizaje DROP COLUMN IF EXISTS material_cargado;
ALTER TABLE sesion_aprendizaje DROP COLUMN IF EXISTS habilidad_id;

DROP TABLE IF EXISTS material_educativo CASCADE;
DROP TABLE IF EXISTS usuario_habilidad CASCADE;
DROP TABLE IF EXISTS notificacion CASCADE;
DROP TABLE IF EXISTS habilidad CASCADE;

UPDATE sesion_aprendizaje
SET estado = 'ACTIVA'
WHERE estado = 'PENDIENTE';

UPDATE usuario
SET rol = 'INSTRUCTOR'
WHERE rol = 'ADMIN';

-- =========================================================
-- V2: tabla de invitaciones privadas a sesiones
-- hu06: el organizador puede invitar asistentes
-- hu07: el invitado puede aceptar o rechazar
-- hu28: el usuario puede ver sus invitaciones pendientes
-- =========================================================
CREATE TABLE invitaciones (
    invitacion_id  BIGSERIAL    PRIMARY KEY,
    sesion_id      BIGINT       NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    invitado_id    BIGINT       NOT NULL REFERENCES usuario(usuario_id)           ON DELETE CASCADE,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invitacion_sesion_usuario UNIQUE (sesion_id, invitado_id)
);

CREATE INDEX idx_invitacion_invitado ON invitaciones(invitado_id);
CREATE INDEX idx_invitacion_sesion   ON invitaciones(sesion_id);

-- esquema consolidado del release 1 sprint 1
-- alcance: uh01, uh02, uh04, uh06, uh07, uh10, uh14, uh15, uh16, uh17, uh23, uh24, uh26, uh28, uh29

CREATE TABLE usuario (
    usuario_id      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    foto_perfil     VARCHAR(500),
    rol             VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ',
    activo          BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sesion_aprendizaje (
    sesion_id          BIGSERIAL PRIMARY KEY,
    titulo             VARCHAR(200) NOT NULL,
    descripcion        TEXT,
    fecha_sesion       TIMESTAMP NOT NULL,
    modalidad          VARCHAR(20) NOT NULL DEFAULT 'VIRTUAL',
    estado             VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    tipo               VARCHAR(20) NOT NULL DEFAULT 'PUBLICA',
    max_participantes  INTEGER NOT NULL DEFAULT 20,
    link_sesion        VARCHAR(500),
    lugar              VARCHAR(300),
    instructor_id      BIGINT NOT NULL REFERENCES usuario(usuario_id),
    fecha_creacion     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inscripcion (
    inscripcion_id     BIGSERIAL PRIMARY KEY,
    sesion_id          BIGINT NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    usuario_id         BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    rol_sesion         VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ',
    fecha_inscripcion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_inscripcion_sesion_usuario UNIQUE (sesion_id, usuario_id)
);

CREATE TABLE invitaciones (
    invitacion_id  BIGSERIAL PRIMARY KEY,
    sesion_id      BIGINT NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    invitado_id    BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    estado         VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invitacion_sesion_usuario UNIQUE (sesion_id, invitado_id)
);

CREATE TABLE verificacion_token (
    token_id      BIGSERIAL PRIMARY KEY,
    token         VARCHAR(255) NOT NULL UNIQUE,
    usuario_id    BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    fecha_expira  TIMESTAMP NOT NULL,
    usado         BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sesion_estado ON sesion_aprendizaje(estado);
CREATE INDEX idx_sesion_tipo ON sesion_aprendizaje(tipo);
CREATE INDEX idx_sesion_instructor ON sesion_aprendizaje(instructor_id);
CREATE INDEX idx_inscripcion_usuario ON inscripcion(usuario_id);
CREATE INDEX idx_inscripcion_sesion ON inscripcion(sesion_id);
CREATE INDEX idx_invitacion_invitado ON invitaciones(invitado_id);
CREATE INDEX idx_invitacion_sesion ON invitaciones(sesion_id);
CREATE INDEX idx_verificacion_token ON verificacion_token(token);

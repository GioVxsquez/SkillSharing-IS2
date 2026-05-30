-- esquema consolidado release 1 - skillsharing
-- este archivo es para informe, revision y der
-- no reemplaza las migraciones flyway v1 v2 v3 v4

CREATE TABLE usuario (
    usuario_id      BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    foto_perfil     VARCHAR(500),
    rol             VARCHAR(20)  NOT NULL DEFAULT 'APRENDIZ',
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE habilidad (
    habilidad_id  BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE,
    descripcion   VARCHAR(300)
);

CREATE TABLE usuario_habilidad (
    usuario_id   BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    habilidad_id BIGINT NOT NULL REFERENCES habilidad(habilidad_id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, habilidad_id)
);

CREATE TABLE sesion_aprendizaje (
    sesion_id           BIGSERIAL PRIMARY KEY,
    titulo              VARCHAR(200) NOT NULL,
    descripcion         TEXT,
    fecha_sesion        TIMESTAMP    NOT NULL,
    modalidad           VARCHAR(20)  NOT NULL DEFAULT 'VIRTUAL',
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    tipo                VARCHAR(20)  NOT NULL DEFAULT 'PUBLICA',
    max_participantes   INTEGER      NOT NULL DEFAULT 20,
    link_sesion         VARCHAR(500),
    lugar               VARCHAR(300),
    instructor_id       BIGINT       NOT NULL REFERENCES usuario(usuario_id),
    habilidad_id        BIGINT       REFERENCES habilidad(habilidad_id),
    material_cargado    BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE material_educativo (
    material_id     BIGSERIAL PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    ruta_archivo    VARCHAR(500) NOT NULL,
    tipo_archivo    VARCHAR(50)  NOT NULL DEFAULT 'PDF',
    sesion_id       BIGINT       NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    fecha_subida    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE inscripcion (
    inscripcion_id    BIGSERIAL PRIMARY KEY,
    sesion_id         BIGINT      NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    usuario_id        BIGINT      NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    rol_sesion        VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ',
    fecha_inscripcion TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sesion_id, usuario_id)
);

CREATE TABLE invitaciones (
    invitacion_id  BIGSERIAL    PRIMARY KEY,
    sesion_id      BIGINT       NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    invitado_id    BIGINT       NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    estado         VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_envio    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_invitacion_sesion_usuario UNIQUE (sesion_id, invitado_id)
);

CREATE TABLE notificacion (
    notificacion_id BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT       NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    sesion_id       BIGINT       REFERENCES sesion_aprendizaje(sesion_id) ON DELETE SET NULL,
    mensaje         VARCHAR(300) NOT NULL,
    visto           BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE verificacion_token (
    token_id     BIGSERIAL       PRIMARY KEY,
    token        VARCHAR(255)    NOT NULL UNIQUE,
    usuario_id   BIGINT          NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    fecha_expira TIMESTAMP       NOT NULL,
    usado        BOOLEAN         NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_sesion_estado        ON sesion_aprendizaje(estado);
CREATE INDEX idx_sesion_tipo          ON sesion_aprendizaje(tipo);
CREATE INDEX idx_sesion_habilidad     ON sesion_aprendizaje(habilidad_id);
CREATE INDEX idx_sesion_instructor    ON sesion_aprendizaje(instructor_id);
CREATE INDEX idx_inscripcion_usuario  ON inscripcion(usuario_id);
CREATE INDEX idx_inscripcion_sesion   ON inscripcion(sesion_id);
CREATE INDEX idx_invitacion_invitado  ON invitaciones(invitado_id);
CREATE INDEX idx_invitacion_sesion    ON invitaciones(sesion_id);
CREATE INDEX idx_notificacion_usuario ON notificacion(usuario_id);
CREATE INDEX idx_material_sesion      ON material_educativo(sesion_id);
CREATE INDEX idx_verificacion_token   ON verificacion_token(token);

INSERT INTO habilidad (nombre, descripcion) VALUES
    ('programacion', 'desarrollo de software, algoritmos y estructuras de datos'),
    ('idiomas', 'aprendizaje de lenguajes como ingles, frances, aleman, etc'),
    ('cocina', 'tecnicas culinarias, recetas y nutricion'),
    ('diseno grafico', 'herramientas de diseno visual, figma, photoshop, etc'),
    ('matematicas', 'algebra, calculo, estadistica y probabilidad'),
    ('musica', 'teoria musical, instrumentos y produccion'),
    ('fotografia', 'tecnicas de captura, composicion y edicion'),
    ('marketing digital', 'estrategias digitales, seo y redes sociales'),
    ('finanzas', 'inversion, presupuesto personal y economia'),
    ('idioma ingles', 'conversacion, gramatica y pronunciacion en ingles');

-- =========================================================
-- esquema inicial de skillsharing
-- adaptado del proyecto myevent para el contexto de aprendizaje
-- cambios clave:
--   evento         -> sesion_aprendizaje
--   categoria      -> habilidad
--   participacion  -> inscripcion
-- nuevo: material_educativo (obligatorio para aprobacion - hu05)
-- nuevo: usuario_habilidad (perfil con habilidades - hu22)
-- =========================================================

-- tabla de usuarios
CREATE TABLE usuario (
    usuario_id      SERIAL PRIMARY KEY,
    nombre          VARCHAR(100) NOT NULL,
    email           VARCHAR(150) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    foto_perfil     VARCHAR(500),
    rol             VARCHAR(20)  NOT NULL DEFAULT 'APRENDIZ',
    activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_registro  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- tabla de habilidades (reemplaza a categoria del myevent original)
-- hu28: el buscador filtra sesiones por habilidad requerida
CREATE TABLE habilidad (
    habilidad_id  SERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE,
    descripcion   VARCHAR(300)
);

-- hu22: relacion muchos a muchos entre usuario y habilidad
-- un usuario puede tener varias habilidades en su perfil
CREATE TABLE usuario_habilidad (
    usuario_id   INTEGER NOT NULL REFERENCES usuario(usuario_id)   ON DELETE CASCADE,
    habilidad_id INTEGER NOT NULL REFERENCES habilidad(habilidad_id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, habilidad_id)
);

-- sesion_aprendizaje reemplaza a evento del myevent original
-- tiene habilidad_requerida en lugar de categoria social
-- el campo material_cargado controla si el admin puede aprobarla (hu05)
CREATE TABLE sesion_aprendizaje (
    sesion_id           SERIAL PRIMARY KEY,
    titulo              VARCHAR(200) NOT NULL,
    descripcion         TEXT,
    fecha_sesion        TIMESTAMP    NOT NULL,
    modalidad           VARCHAR(20)  NOT NULL DEFAULT 'VIRTUAL',
    estado              VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    max_participantes   INTEGER      NOT NULL DEFAULT 20,
    link_sesion         VARCHAR(500),
    lugar               VARCHAR(300),
    instructor_id       INTEGER      NOT NULL REFERENCES usuario(usuario_id),
    habilidad_id        INTEGER      REFERENCES habilidad(habilidad_id),
    -- hu05: flag que indica si ya se subio al menos un material educativo
    -- el admin no puede aprobar si esto es false
    material_cargado    BOOLEAN      NOT NULL DEFAULT FALSE,
    fecha_creacion      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- hu05: material educativo obligatorio para aprobacion de la sesion
-- el instructor debe subir al menos un archivo (pdf, etc) antes de que el admin apruebe
CREATE TABLE material_educativo (
    material_id     SERIAL PRIMARY KEY,
    nombre          VARCHAR(200) NOT NULL,
    ruta_archivo    VARCHAR(500) NOT NULL,
    tipo_archivo    VARCHAR(50)  NOT NULL DEFAULT 'PDF',
    sesion_id       INTEGER      NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    fecha_subida    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- inscripcion reemplaza a participacion del myevent original
-- un usuario se inscribe a una sesion en rol de aprendiz
CREATE TABLE inscripcion (
    inscripcion_id    SERIAL PRIMARY KEY,
    sesion_id         INTEGER   NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    usuario_id        INTEGER   NOT NULL REFERENCES usuario(usuario_id)           ON DELETE CASCADE,
    rol_sesion        VARCHAR(20) NOT NULL DEFAULT 'APRENDIZ',
    fecha_inscripcion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sesion_id, usuario_id)
);

-- notificaciones para usuarios cuando una sesion cambia de estado
-- patron observer (semana 5): se crea automaticamente al aprobar/rechazar una sesion
CREATE TABLE notificacion (
    notificacion_id SERIAL PRIMARY KEY,
    usuario_id      INTEGER    NOT NULL REFERENCES usuario(usuario_id)            ON DELETE CASCADE,
    sesion_id       INTEGER    REFERENCES sesion_aprendizaje(sesion_id)           ON DELETE SET NULL,
    mensaje         VARCHAR(300) NOT NULL,
    visto           BOOLEAN    NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- indices para mejorar rendimiento en consultas frecuentes
CREATE INDEX idx_sesion_estado          ON sesion_aprendizaje(estado);
CREATE INDEX idx_sesion_habilidad       ON sesion_aprendizaje(habilidad_id);
CREATE INDEX idx_sesion_instructor      ON sesion_aprendizaje(instructor_id);
CREATE INDEX idx_inscripcion_usuario    ON inscripcion(usuario_id);
CREATE INDEX idx_inscripcion_sesion     ON inscripcion(sesion_id);
CREATE INDEX idx_notificacion_usuario   ON notificacion(usuario_id);
CREATE INDEX idx_material_sesion        ON material_educativo(sesion_id);

-- habilidades iniciales del sistema
INSERT INTO habilidad (nombre, descripcion) VALUES
    ('programacion',     'desarrollo de software, algoritmos y estructuras de datos'),
    ('idiomas',          'aprendizaje de lenguajes como ingles, frances, aleman, etc'),
    ('cocina',           'tecnicas culinarias, recetas y nutricion'),
    ('diseno grafico',   'herramientas de diseno visual, figma, photoshop, etc'),
    ('matematicas',      'algebra, calculo, estadistica y probabilidad'),
    ('musica',           'teoria musical, instrumentos y produccion'),
    ('fotografia',       'tecnicas de captura, composicion y edicion'),
    ('marketing digital','estrategias digitales, seo y redes sociales'),
    ('finanzas',         'inversion, presupuesto personal y economia'),
    ('idioma ingles',    'conversacion, gramatica y pronunciacion en ingles');

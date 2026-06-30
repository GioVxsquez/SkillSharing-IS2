-- sprint 2: reintroduccion de tablas para release 2
-- habilidades, materiales, notificaciones, calificaciones y categoria en sesion

-- tabla de habilidades
CREATE TABLE habilidad (
    habilidad_id  BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL UNIQUE,
    descripcion   VARCHAR(300)
);

-- relacion usuario-habilidad (muchos a muchos) - us21/us22
CREATE TABLE usuario_habilidad (
    usuario_id   BIGINT NOT NULL REFERENCES usuario(usuario_id)   ON DELETE CASCADE,
    habilidad_id BIGINT NOT NULL REFERENCES habilidad(habilidad_id) ON DELETE CASCADE,
    PRIMARY KEY (usuario_id, habilidad_id)
);

-- material educativo subido por el instructor - us05/us27
CREATE TABLE material_educativo (
    material_id   BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(200) NOT NULL,
    ruta_archivo  VARCHAR(500) NOT NULL,
    tipo_archivo  VARCHAR(50)  NOT NULL DEFAULT 'PDF',
    sesion_id     BIGINT NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    fecha_subida  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- notificaciones del sistema - us12/us13
CREATE TABLE notificacion (
    notificacion_id BIGSERIAL PRIMARY KEY,
    usuario_id      BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    sesion_id       BIGINT REFERENCES sesion_aprendizaje(sesion_id) ON DELETE SET NULL,
    mensaje         VARCHAR(300) NOT NULL,
    visto           BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- calificaciones de sesiones por aprendices - us19/us20
CREATE TABLE calificacion (
    calificacion_id BIGSERIAL PRIMARY KEY,
    sesion_id       BIGINT NOT NULL REFERENCES sesion_aprendizaje(sesion_id) ON DELETE CASCADE,
    usuario_id      BIGINT NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    puntuacion      INTEGER NOT NULL CHECK (puntuacion BETWEEN 1 AND 5),
    comentario      VARCHAR(500),
    fecha_creacion  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (sesion_id, usuario_id)
);

-- nuevas columnas en sesion_aprendizaje
ALTER TABLE sesion_aprendizaje ADD COLUMN IF NOT EXISTS categoria VARCHAR(100);
ALTER TABLE sesion_aprendizaje ADD COLUMN IF NOT EXISTS habilidad_id BIGINT REFERENCES habilidad(habilidad_id);

-- indices de rendimiento
CREATE INDEX idx_habilidad_nombre ON habilidad(nombre);
CREATE INDEX idx_material_sesion ON material_educativo(sesion_id);
CREATE INDEX idx_notificacion_usuario ON notificacion(usuario_id);
CREATE INDEX idx_notificacion_visto ON notificacion(usuario_id, visto);
CREATE INDEX idx_calificacion_sesion ON calificacion(sesion_id);
CREATE INDEX idx_sesion_categoria ON sesion_aprendizaje(categoria);
CREATE INDEX idx_sesion_habilidad ON sesion_aprendizaje(habilidad_id);

-- datos iniciales de habilidades
INSERT INTO habilidad (nombre, descripcion) VALUES
    ('Programacion',     'Desarrollo de software, algoritmos y estructuras de datos'),
    ('Idiomas',          'Aprendizaje de lenguajes como ingles, frances, aleman, etc'),
    ('Cocina',           'Tecnicas culinarias, recetas y nutricion'),
    ('Diseno Grafico',   'Herramientas de diseno visual, Figma, Photoshop, etc'),
    ('Matematicas',      'Algebra, calculo, estadistica y probabilidad'),
    ('Musica',           'Teoria musical, instrumentos y produccion'),
    ('Fotografia',       'Tecnicas de captura, composicion y edicion'),
    ('Marketing Digital','Estrategias digitales, SEO y redes sociales'),
    ('Finanzas',         'Inversion, presupuesto personal y economia'),
    ('Idioma Ingles',    'Conversacion, gramatica y pronunciacion en ingles');

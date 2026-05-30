-- migracion v3: tabla de tokens de verificacion de cuenta (HU29 - activar cuenta)
-- cuando un usuario se registra, se genera un token unico y se guarda aqui
-- cuando el usuario hace clic en el enlace del correo, se busca este token
-- y se activa su cuenta (activo = true en la tabla usuario)

CREATE TABLE verificacion_token (
    token_id     BIGSERIAL       PRIMARY KEY,
    token        VARCHAR(255)    NOT NULL UNIQUE,
    usuario_id   BIGINT          NOT NULL REFERENCES usuario(usuario_id) ON DELETE CASCADE,
    fecha_expira TIMESTAMP       NOT NULL,
    usado        BOOLEAN         NOT NULL DEFAULT FALSE
);

-- indice para buscar rapidamente por token (es lo que llegara en la url del correo)
CREATE INDEX idx_verificacion_token ON verificacion_token(token);

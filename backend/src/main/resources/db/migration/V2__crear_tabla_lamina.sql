-- V2: tabla de laminas, cada una pertenece a un album
-- cantidad = copias que tiene el coleccionista (0 = faltante, >1 = repetida)
CREATE TABLE lamina (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    album_id  BIGINT       NOT NULL,
    numero    INT          NOT NULL,
    nombre    VARCHAR(100) NOT NULL,
    tipo      VARCHAR(30)  NOT NULL,
    cantidad  INT          NOT NULL DEFAULT 0,
    foto      VARCHAR(255) NULL,
    CONSTRAINT pk_lamina PRIMARY KEY (id),
    CONSTRAINT fk_lamina_album FOREIGN KEY (album_id) REFERENCES album (id) ON DELETE CASCADE,
    CONSTRAINT uk_lamina_album_numero UNIQUE (album_id, numero),
    CONSTRAINT ck_lamina_numero CHECK (numero >= 1),
    CONSTRAINT ck_lamina_cantidad CHECK (cantidad >= 0)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

-- Acelera las consultas de faltantes (cantidad = 0) y repetidas (cantidad > 1) por album
CREATE INDEX ix_lamina_album_cantidad ON lamina (album_id, cantidad);

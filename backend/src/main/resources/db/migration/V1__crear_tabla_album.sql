-- V1: tabla de albumes de laminas
CREATE TABLE album (
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    nombre            VARCHAR(100) NOT NULL,
    imagen            VARCHAR(500) NULL,
    fecha_lanzamiento DATE         NOT NULL,
    tipo_laminas      VARCHAR(30)  NOT NULL,
    total_laminas     INT          NOT NULL,
    editorial         VARCHAR(100) NULL,
    descripcion       VARCHAR(500) NULL,
    CONSTRAINT pk_album PRIMARY KEY (id),
    CONSTRAINT uk_album_nombre UNIQUE (nombre),
    CONSTRAINT ck_album_total_laminas CHECK (total_laminas >= 1)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

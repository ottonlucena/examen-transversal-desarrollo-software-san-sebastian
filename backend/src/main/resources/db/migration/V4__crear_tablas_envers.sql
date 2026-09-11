-- V4: tablas de Hibernate Envers para el historial de cambios.
-- revinfo: una fila por transaccion (revision) con fecha (epoch ms) y usuario.
-- *_aud: una fila por cada version de un registro; revtype 0 = creacion, 1 = modificacion, 2 = eliminacion.
-- Las tablas _aud no tienen FK hacia album/lamina: deben conservar el historial de registros eliminados.
CREATE TABLE revinfo (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    revtstmp BIGINT       NOT NULL,
    usuario  VARCHAR(100) NULL,
    CONSTRAINT pk_revinfo PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE album_aud (
    id                BIGINT       NOT NULL,
    rev               BIGINT       NOT NULL,
    revtype           TINYINT      NULL,
    nombre            VARCHAR(100) NULL,
    imagen            VARCHAR(500) NULL,
    fecha_lanzamiento DATE         NULL,
    tipo_laminas      VARCHAR(30)  NULL,
    total_laminas     INT          NULL,
    editorial         VARCHAR(100) NULL,
    descripcion       VARCHAR(500) NULL,
    CONSTRAINT pk_album_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_album_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE lamina_aud (
    id       BIGINT       NOT NULL,
    rev      BIGINT       NOT NULL,
    revtype  TINYINT      NULL,
    album_id BIGINT       NULL,
    numero   INT          NULL,
    nombre   VARCHAR(100) NULL,
    tipo     VARCHAR(30)  NULL,
    cantidad INT          NULL,
    foto     VARCHAR(255) NULL,
    CONSTRAINT pk_lamina_aud PRIMARY KEY (id, rev),
    CONSTRAINT fk_lamina_aud_revinfo FOREIGN KEY (rev) REFERENCES revinfo (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

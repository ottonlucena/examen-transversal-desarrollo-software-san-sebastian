-- V3: columnas de auditoria (JPA Auditing) en album y lamina.
-- Las filas existentes quedan con la fecha de la migracion y el usuario 'sistema'.
ALTER TABLE album
    ADD COLUMN creado_en      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN creado_por     VARCHAR(100) NOT NULL DEFAULT 'sistema',
    ADD COLUMN modificado_en  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN modificado_por VARCHAR(100) NOT NULL DEFAULT 'sistema';

ALTER TABLE lamina
    ADD COLUMN creado_en      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN creado_por     VARCHAR(100) NOT NULL DEFAULT 'sistema',
    ADD COLUMN modificado_en  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN modificado_por VARCHAR(100) NOT NULL DEFAULT 'sistema';

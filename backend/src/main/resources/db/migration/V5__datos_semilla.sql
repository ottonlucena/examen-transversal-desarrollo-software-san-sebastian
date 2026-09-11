-- V5: datos de ejemplo para probar la API de inmediato (2 albumes con laminas en distintos estados).
-- Tambien se registra una revision inicial en las tablas de Envers, para que el historial de
-- estos registros comience con una CREACION hecha por 'flyway-V5'.

-- Album 1: 20 laminas, se catalogan 18 (la 19 y la 20 quedan como no catalogadas)
INSERT INTO album (nombre, imagen, fecha_lanzamiento, tipo_laminas, total_laminas, editorial, descripcion,
                   creado_por, modificado_por)
VALUES ('Copa Mundial 2026', NULL, '2026-04-15', 'ADHESIVA', 20, 'Panini',
        'Álbum de ejemplo con selecciones anfitrionas, estadios y especiales', 'flyway-V5', 'flyway-V5');
SET @mundial = LAST_INSERT_ID();

INSERT INTO lamina (album_id, numero, nombre, tipo, cantidad, creado_por, modificado_por) VALUES
    (@mundial,  1, 'Trofeo de la Copa',       'ESPECIAL',  1, 'flyway-V5', 'flyway-V5'),
    (@mundial,  2, 'Balón oficial',           'ESPECIAL',  0, 'flyway-V5', 'flyway-V5'),
    (@mundial,  3, 'Mascota oficial',         'BRILLANTE', 2, 'flyway-V5', 'flyway-V5'),
    (@mundial,  4, 'Escudo Chile',            'ESCUDO',    1, 'flyway-V5', 'flyway-V5'),
    (@mundial,  5, 'Escudo Argentina',        'ESCUDO',    3, 'flyway-V5', 'flyway-V5'),
    (@mundial,  6, 'Escudo Brasil',           'ESCUDO',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial,  7, 'Escudo México',           'ESCUDO',    1, 'flyway-V5', 'flyway-V5'),
    (@mundial,  8, 'Escudo Estados Unidos',   'ESCUDO',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial,  9, 'Escudo Canadá',           'ESCUDO',    2, 'flyway-V5', 'flyway-V5'),
    (@mundial, 10, 'Estadio Azteca',          'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@mundial, 11, 'Estadio MetLife',         'NORMAL',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial, 12, 'Estadio BC Place',        'NORMAL',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial, 13, 'Plantel Chile',           'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@mundial, 14, 'Plantel Argentina',       'NORMAL',    2, 'flyway-V5', 'flyway-V5'),
    (@mundial, 15, 'Plantel Brasil',          'NORMAL',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial, 16, 'Plantel México',          'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@mundial, 17, 'Plantel Estados Unidos',  'NORMAL',    0, 'flyway-V5', 'flyway-V5'),
    (@mundial, 18, 'Plantel Canadá',          'NORMAL',    1, 'flyway-V5', 'flyway-V5');

-- Album 2: 12 laminas, todas catalogadas
INSERT INTO album (nombre, imagen, fecha_lanzamiento, tipo_laminas, total_laminas, editorial, descripcion,
                   creado_por, modificado_por)
VALUES ('Fauna Chilena', NULL, '2024-09-01', 'TARJETA', 12, 'Editorial Ejemplo',
        'Álbum de ejemplo con animales nativos de Chile', 'flyway-V5', 'flyway-V5');
SET @fauna = LAST_INSERT_ID();

INSERT INTO lamina (album_id, numero, nombre, tipo, cantidad, creado_por, modificado_por) VALUES
    (@fauna,  1, 'Cóndor andino',        'BRILLANTE', 1, 'flyway-V5', 'flyway-V5'),
    (@fauna,  2, 'Huemul',               'ESPECIAL',  0, 'flyway-V5', 'flyway-V5'),
    (@fauna,  3, 'Pudú',                 'NORMAL',    2, 'flyway-V5', 'flyway-V5'),
    (@fauna,  4, 'Puma',                 'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@fauna,  5, 'Zorro culpeo',         'NORMAL',    0, 'flyway-V5', 'flyway-V5'),
    (@fauna,  6, 'Chungungo',            'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@fauna,  7, 'Pingüino de Humboldt', 'NORMAL',    3, 'flyway-V5', 'flyway-V5'),
    (@fauna,  8, 'Flamenco chileno',     'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@fauna,  9, 'Guanaco',              'NORMAL',    1, 'flyway-V5', 'flyway-V5'),
    (@fauna, 10, 'Monito del monte',     'BRILLANTE', 0, 'flyway-V5', 'flyway-V5'),
    (@fauna, 11, 'Picaflor de Arica',    'ESPECIAL',  0, 'flyway-V5', 'flyway-V5'),
    (@fauna, 12, 'Ranita de Darwin',     'NORMAL',    1, 'flyway-V5', 'flyway-V5');

-- Revision inicial de Envers (revtype 0 = creacion) para los datos de ejemplo
INSERT INTO revinfo (revtstmp, usuario)
VALUES (CAST(UNIX_TIMESTAMP(NOW(3)) * 1000 AS UNSIGNED), 'flyway-V5');
SET @rev = LAST_INSERT_ID();

INSERT INTO album_aud (id, rev, revtype, nombre, imagen, fecha_lanzamiento, tipo_laminas, total_laminas,
                       editorial, descripcion)
SELECT id, @rev, 0, nombre, imagen, fecha_lanzamiento, tipo_laminas, total_laminas, editorial, descripcion
FROM album
WHERE id IN (@mundial, @fauna);

INSERT INTO lamina_aud (id, rev, revtype, album_id, numero, nombre, tipo, cantidad, foto)
SELECT id, @rev, 0, album_id, numero, nombre, tipo, cantidad, foto
FROM lamina
WHERE album_id IN (@mundial, @fauna);

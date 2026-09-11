# Guía paso a paso: informe y capturas

Examen Transversal · Desarrollo de Software Web II · Prof. Boris Marcelo Belmar Muñoz
Entrega: **17-09-2026** · Archivo: `EXT_GRUPO_APELLIDO_NOMBRE`

> La colección Bruno (`docs/api/bruno`) tiene **una request por captura, con el mismo número** (12 a 41), ordenadas según su ejecución. Cada request trae asserts que verifican el resultado esperado.

---

## 1. Preparar el entorno (una sola vez, el día de las capturas)

1. Parte de una base limpia, para que los ids sean ordenados y reproducibles:
   ```bash
   cd ~/Escritorio/EXAMEN-TRANSVERSAL
   docker compose down -v          # ⚠️ borra la BD y las fotos de desarrollo
   docker compose up -d --build    # levanta MySQL + API; Flyway aplica V1..V5
   docker compose ps               # mysql "(healthy)", api "Up"
   ```
2. Genera el archivo de 6 MB para la captura 36; no se versiona:
   ```bash
   cd docs/api/bruno
   python3 -c "import os; open('archivos/grande.png','wb').write(b'\x89PNG\r\n\x1a\n' + os.urandom(6*1024*1024))"
   ```
3. Abre la app **Bruno** → *Open Collection* → carpeta `docs/api/bruno` → elige el entorno **local** (arriba a la derecha).
4. Herramienta de captura en Ubuntu:
   - **Flameshot** (`sudo apt install flameshot`, luego `flameshot gui`), o
   - `Shift + Impr Pant` para capturar una región.

   Guarda cada imagen en `docs/evidencias/` con el nombre exacto de la tabla de la §3.
5. Consejos para que las capturas quepan en 10 páginas:
   - Captura **solo la región útil**: método + URL + status + parte relevante del body (y el header `X-Usuario` cuando corresponda).
   - Usa zoom al 110–125 % y el mismo tema (claro u oscuro) en todas.
   - En Bruno, colapsa los arrays largos y deja visibles los totales.

## 2. Orden de ejecución

Ejecuta las requests de Bruno **en orden numérico, sin saltarte ninguna**: cada una usa ids que crean las anteriores (`albumId`, `laminaPruebaId`, `lamina2Id`…).

- Si una request falla o la repites fuera de orden, vuelve al paso 1.1 y empieza de nuevo.
- Para comprobar antes que todo pasa, corre la colección completa: `cd docs/api/bruno && npx @usebruno/cli run -r --env local`. Termina borrando su álbum de prueba, así que se puede repetir.

---

## 3. Lista de capturas

★ = va en el cuerpo del informe (≈ 22). El resto se guarda en `docs/evidencias/` (repo y ZIP) y se cita en la tabla de pruebas del informe.

### 3.1 Configuración del proyecto (indicador 1 · 20 pts)

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★01 | `01-initializr.png` | start.spring.io con Maven, Java 21, Boot 4.1.1, group `cl.ipss`, artifact `coleccion-laminas` y las dependencias Web, JPA, MySQL, Validation, Flyway y Lombok | Abre https://start.spring.io, completa los campos y captura **sin** generar |
| ★02 | `02-pom-dependencias.png` | Bloque `<dependencies>` del `pom.xml` | IDE → `backend/pom.xml` |
| ★03 | `03-application-properties.png` | Datasource con variables de entorno, `ddl-auto=validate`, Flyway, Envers | IDE → `backend/src/main/resources/application.properties` |
| 04 | `04-estructura-proyecto.png` | Árbol de paquetes (`audit`, `config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `service`, `validation`) | IDE → panel del proyecto |
| ★05 | `05-docker-compose-ps.png` | `coleccion-mysql (healthy)` y `coleccion-api Up` | `docker compose ps` |
| 06 | `06-arranque-api.png` | Flyway aplicando V1..V5 + `Started ColeccionLaminasApplication` | `docker compose logs api \| grep -E "Migrating\|Successfully\|Started"` |

### 3.2 Base de datos y migraciones (indicadores 2 y 4)

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| 07 | `07-carpeta-migraciones.png` | `db/migration` con V1…V5 | IDE |
| ★08 | `08-flyway-schema-history.png` | Las 5 migraciones con `success = 1` | `docker compose exec mysql mysql -ucoleccion -pcoleccion coleccion_laminas -e "SELECT installed_rank, version, description, installed_on, success FROM flyway_schema_history;"` |
| 09 | `09-tablas-mysql.png` | `album`, `lamina`, `album_aud`, `lamina_aud`, `revinfo`, `flyway_schema_history` | Mismo comando con `SHOW TABLES;` |
| ★10 | `10-diagrama-er.png` | album 1—N lamina; tablas `_aud` → `revinfo` | MySQL Workbench o DBeaver → conectar a `localhost:3306` (usuario `coleccion`, clave `coleccion`) → *Reverse Engineer* / *ER Diagram* |

### 3.3 Documentación de la API

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★11 | `11-swagger-ui.png` | Swagger UI con los 5 grupos (1. Álbumes … 5. Auditoría) | Navegador → http://localhost:8080/swagger-ui.html |

### 3.4 Pruebas de endpoints (colección Bruno)

En cada captura deben verse **método, URL, status y body**.

**1. Álbumes**
| # | Archivo | Request Bruno | Resultado esperado |
|---|---------|---------------|--------------------|
| ★12 | `12-album-crear-201.png` | 12 Crear álbum | 201, header `Location`, `auditoria.creadoPor = equipo` |
| ★13 | `13-album-validacion-400.png` | 13 Validación de datos | 400 con `errores` por campo |
| 14 | `14-album-duplicado-409.png` | 14 Nombre duplicado | 409 |
| 15 | `15-album-listar-paginado.png` | 15 Listar álbumes paginado | 200 con `content` y `page` (incluye los álbumes de ejemplo) |
| 16 | `16-album-obtener-404.png` | 16 Álbum inexistente | 404 ProblemDetail |
| 17 | `17-album-actualizar-200.png` | 17 Actualizar álbum | 200, `modificadoPor = bruno` |

**2. Láminas**
| # | Archivo | Request Bruno | Resultado esperado |
|---|---------|---------------|--------------------|
| 18 | `18-lamina-crear-201.png` | 18 Crear lámina | 201, estado `FALTANTE` |
| 19 | `19-lamina-fuera-rango-409.png` | 19 Número fuera de rango | 409 |
| 20 | `20-lamina-listar-filtro.png` | 20 Listar láminas por tipo | 200 solo `NORMAL` |
| 21 | `21-lamina-actualizar-200.png` | 21 Actualizar lámina | 200 |
| 22 | `22-lamina-eliminar-204.png` | 22 Eliminar lámina | 204 |

**3. Funcionalidades especiales (indicador 3 · 25 pts)**
| # | Archivo | Request Bruno | Resultado esperado |
|---|---------|---------------|--------------------|
| ★23 | `23-lote-201.png` | 23 Carga en lote | 201 con 10 láminas |
| ★24 | `24-lote-conflictos-409.png` | 24 Lote con conflictos | 409 con `errores` por posición |
| 25 | `25-lote-repetidos-400.png` | 25 Lote con números repetidos | 400 (`@NumerosUnicos`) |
| ★26 | `26-registrar-200.png` | 26 Registrar obtenidas | 200, cantidades 1 / 2 / 3 |
| 27 | `27-registrar-no-catalogado-409.png` | 27 Registrar número no catalogado | 409 |
| 28 | `28-cantidad-patch-200.png` | 28 Restar una copia | 200, cantidad 2 |
| 29 | `29-cantidad-negativa-409.png` | 29 Cantidad negativa | 409 |
| ★30 | `30-faltantes.png` | 30 Láminas faltantes | 200: láminas 4–10 y no catalogadas 11–20 (total 17) |
| ★31 | `31-repetidas.png` | 31 Láminas repetidas | 200: láminas 2 y 3 con 1 repetida cada una |
| ★32 | `32-resumen.png` | 32 Resumen del álbum | 200: 3 obtenidas, 17 faltantes, 15 % |

**4. Fotos**
| # | Archivo | Request Bruno | Resultado esperado |
|---|---------|---------------|--------------------|
| ★33 | `33-foto-subir-200.png` | 33 Subir foto (pestaña *Body*: Multipart, campo `archivo`) | 200 con `fotoUrl` |
| 34 | `34-foto-ver-200.png` | 34 Ver foto | 200 `image/png` (Bruno muestra la imagen) |
| ★35 | `35-foto-falsa-415.png` | 35 Archivo que no es imagen | 415 |
| 36 | `36-foto-grande-413.png` | 36 Archivo mayor a 5 MB | 413 |
| 37 | `37-foto-eliminar-204.png` | 37 Quitar foto | 204 |

### 3.5 Auditoría y validaciones (indicador 4 · 20 pts)

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★38 | `38-auditoria-header.png` | Pestaña *Headers* con `X-Usuario: ana` + respuesta con `creadoPor = equipo` y `modificadoPor = ana` | Bruno 38 |
| 39 | `39-historial-lamina.png` | `CREACION` (equipo) → `MODIFICACION` (bruno) → `ELIMINACION` (carla) | Bruno 39 |
| 40 | `40-album-eliminar-204.png` | 204 con `X-Usuario: diego` | Bruno 40 |
| ★41 | `41-historial-album.png` | CREACION → MODIFICACION → MODIFICACION → ELIMINACION (diego) | Bruno 41 |
| ★42 | `42-revinfo-lamina-aud.png` | Revisión, fecha, usuario y `revtype` de cada cambio | `docker compose exec mysql mysql -ucoleccion -pcoleccion coleccion_laminas -e "SELECT r.id, FROM_UNIXTIME(r.revtstmp/1000) fecha, r.usuario, a.id lamina, a.revtype, a.numero, a.cantidad FROM lamina_aud a JOIN revinfo r ON r.id=a.rev ORDER BY r.id DESC LIMIT 15;"` |
| 43 | `43-validador-numeros-unicos.png` | Código de `NumerosUnicosValidator` | IDE → `validation/NumerosUnicosValidator.java` |

### 3.6 Resumen de pruebas automatizadas

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★44 | `44-script-e2e.png` | Final de la salida: `RESULTADO: 108 OK, 0 FAIL` | `bash pruebas/e2e.sh` (con la API arriba) |
| ★45 | `45-mvnw-verify.png` | `Tests run: 11, Failures: 0` y `BUILD SUCCESS` | `cd backend && ./mvnw verify` |
| 46 | `46-bruno-cli.png` | Resumen de la colección Bruno: todas las requests y asserts en verde | `cd docs/api/bruno && npx @usebruno/cli run -r --env local` (en una BD limpia) |

---

## 4. Redacción del informe

### 4.1 Formato (obligatorio)

Usa Google Docs, LibreOffice Writer o Word y **exporta a PDF**; el profesor pidió no enviar Word.

| Ajuste | Valor |
|--------|-------|
| Fuente | Arial 12 en todo el texto (títulos Arial 14 negrita) |
| Interlineado | 1,15 |
| Alineación | Justificada |
| Márgenes | 2,5 cm |
| Números de página | Pie de página, desde la página 2 |
| Extensión | 5 a 10 páginas, **incluida la portada** |
| Imágenes | Tabla de 2 columnas sin bordes, cada imagen ≈ 8 cm de ancho, con pie "Figura N: …" |
| Índice | Automático, a partir de los estilos Título 1 y Título 2 |

### 4.2 Estructura y contenido página por página

| Pág. | Sección | Qué escribir | Figuras |
|:---:|---------|--------------|---------|
| 1 | **Portada** | Logo IPSS (recórtalo del PDF del examen), "Sistema de Gestión de Colección de Láminas – API REST", asignatura *Desarrollo de Software Web II*, profesor *Boris Marcelo Belmar Muñoz*, integrantes, grupo, fecha | — |
| 2 | **Índice** | Automático | — |
| 3 | **1. Introducción** | Problema (coleccionistas que necesitan saber qué láminas les faltan y cuáles tienen repetidas), objetivo, alcance (API REST sin frontend) y **justificación de la tecnología** (§4.3) | — |
| 4 | **2. Desarrollo · 2.1 Configuración** | Initializr, para qué sirve cada dependencia, properties con variables de entorno, Docker Compose | 01, 02, 03, 05 |
| 5 | **2.2 Arquitectura y modelo de datos** | Capas controller → service → repository, DTOs (no se exponen entidades), Album 1:N Lamina, regla `cantidad` → faltante / obtenida / repetida | 10 (+04) |
| 6 | **2.3 API y funcionalidades especiales** | Tabla de endpoints (copiarla del README), lote todo o nada, registrar obtenidas, faltantes (incluye no catalogadas), repetidas, resumen en una consulta, fotos multipart | 11 |
| 7 | **2.4 Validaciones y auditoría** | Bean Validation, reglas de negocio (409), `@NumerosUnicos`, errores RFC 9457, validación de fotos por contenido; auditoría en 3 capas: **migraciones Flyway** (lo que pidió el profesor), JPA Auditing con `X-Usuario`, Envers con historial | 08, 38, 41, 42 |
| 8–9 | **3. Pruebas** | Tabla resumen (§4.4) + grilla de capturas ★ + script e2e + `mvnw verify` | 12, 13, 23, 24, 26, 30–33, 35, 44, 45 |
| 10 | **4. Conclusión** y **5. Bibliografía** | Qué se logró por indicador, dificultades resueltas (enums de MySQL, auditoría de borrados en cascada, precisión de fechas) y mejoras futuras (autenticación, frontend) | — |

### 4.3 Justificación de la tecnología (el profesor la pidió explícitamente)

- **Spring Boot 4.1 + Java 21:** lo pide el enunciado; es estándar en la industria, trae auto-configuración y un ecosistema (Data JPA, Validation) que cubre todos los requisitos; Java 21 es LTS.
- **MySQL 8.4:** obligatorio según el enunciado y el profesor; versión LTS; relacional, adecuado para la relación álbum-lámina y con integridad (FK, UNIQUE, CHECK).
- **Flyway:** el profesor indicó que la auditoría del esquema se hace con migraciones; deja un historial versionado y reproducible (`flyway_schema_history`).
- **Hibernate Envers + JPA Auditing:** registran quién, cuándo y qué cambió en cada registro, incluidos los eliminados.
- **Docker Compose:** mismo entorno en cualquier computador; MySQL sin instalación local.
- **springdoc/Swagger + Bruno:** documentación navegable y colección versionable en Git (Bruno fue sugerido en clase).

### 4.4 Tabla de pruebas (modelo)

| N° | Endpoint | Caso | Esperado | Obtenido | Fig. |
|:--:|----------|------|:--------:|:--------:|:----:|
| 1 | POST /api/albumes | Álbum válido | 201 | 201 ✅ | 12 |
| 2 | POST /api/albumes | Campos inválidos | 400 | 400 ✅ | 13 |
| … | … | … | … | … | … |

Una fila por cada captura 12–41. Agrega una fila final: "Script e2e: 108 comprobaciones, 0 fallos (Fig. 44)".

### 4.5 Bibliografía (APA 7, ejemplos)

- Broadcom. (2026). *Spring Boot reference documentation* (Versión 4.1). https://docs.spring.io/spring-boot/
- Broadcom. (2026). *Spring Data JPA reference documentation*. https://docs.spring.io/spring-data/jpa/reference/
- Red Hat. (2026). *Hibernate ORM user guide: Envers* (Versión 7.4). https://docs.hibernate.org/orm/
- Redgate. (2026). *Flyway documentation*. https://documentation.red-gate.com/flyway
- Oracle. (2026). *MySQL 8.4 reference manual*. https://dev.mysql.com/doc/refman/8.4/en/
- springdoc. (2026). *springdoc-openapi* (Versión 3.1). https://springdoc.org/
- Docker Inc. (2026). *Docker Compose documentation*. https://docs.docker.com/compose/
- Bruno. (2026). *Bruno documentation*. https://docs.usebruno.com/
- Nottingham, M., Wilde, E., & Dalal, S. (2023). *RFC 9457: Problem details for HTTP APIs*. IETF. https://www.rfc-editor.org/rfc/rfc9457

---

## 5. Entrega

1. **GitHub:** crea el repositorio, sube el proyecto y verifica que no suban `target/`, `uploads/`, `.env` ni `grande.png`. Si el repo es privado, invita al profesor.
2. Crea `ENLACE_GITHUB.txt` con la URL del repositorio.
3. Exporta el informe a PDF: `EXT_GRUPO_APELLIDO_NOMBRE.pdf`.
4. Arma el ZIP `EXT_GRUPO_APELLIDO_NOMBRE.zip` con el proyecto (sin `target/`), el PDF, `ENLACE_GITHUB.txt` y `docs/evidencias/`.
5. Revisa antes de subir:
   - [ ] El PDF abre y tiene entre 5 y 10 páginas
   - [ ] Arial 12, interlineado 1,15, justificado, páginas numeradas
   - [ ] Portada con logo, profesor e integrantes
   - [ ] Justificación de la tecnología
   - [ ] Evidencias de migraciones (08) y de auditoría (38, 41, 42)
   - [ ] Tabla de pruebas completa
   - [ ] El ZIP descomprime y `docker compose up -d --build` funciona en limpio
6. Súbelo a la plataforma **antes del 17-09-2026**.

## 6. Pendientes para consultar al profesor

- ¿Se puede incluir un anexo de capturas fuera del límite de 10 páginas? Si la respuesta es sí, las capturas sin ★ van al anexo.
- ¿Qué apellido y nombre van en el archivo si el trabajo es grupal?

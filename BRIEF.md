# BRIEF — Examen Transversal · Desarrollo de Software Web II (IPSS)

**Proyecto:** API REST para gestionar colecciones de láminas de álbumes
**Stack:** Java 21 · Spring Boot 4.1.x · Maven (Spring Initializr) · Spring Data JPA · **MySQL 8.4 (obligatorio)** · **Flyway** · Docker Compose
**Modalidad:** grupal (máx. 3) · **Peso:** 40 % de la nota final · **Exigencia:** 60 % (60 pts = 4,0)
**Plazo:** **jueves 17-09-2026** (el 18 es feriado; el profesor debe tener las notas antes del viernes)
**Entregables:** informe en **PDF** + repositorio **GitHub** (link en un `.txt`) + **ZIP** con proyecto e informe → `EXT_GRUPO_APELLIDO_NOMBRE`

> Fuentes: el enunciado y la rúbrica de la asignatura, más las indicaciones del profesor en clase. Cuando el profesor aclaró o cambió algo, esta versión del brief ya lo incorpora.

---

## 1. Qué hay que construir

Un backend REST (el frontend **no** es obligatorio) que permita:

1. CRUD de **álbumes**.
2. CRUD de **láminas** de cada álbum, con **foto opcional** (subida por `multipart`).
3. **Carga de láminas en lote** (enviar un listado en una sola petición).
4. Listar las **láminas faltantes** y las **repetidas**, con la **cantidad de repetidas de cada una**.
5. Documentar la API (request/response) y **probar cada endpoint, con un screenshot por prueba en el informe**.

El profesor pidió seguir estas funcionalidades **al pie de la letra**.

---

## 2. Análisis de la rúbrica (100 pts)

### 2.1 Indicadores

| # | Indicador | Pts | Nivel Sobresaliente (90–100 %) | Cómo lo aseguramos |
|---|-----------|:---:|--------------------------------|--------------------|
| 1 | Configuración del proyecto y conexión a BD (IL1) | **20** | Proyecto y conexión MySQL "perfectamente configurados sin errores"; dependencias correctas | Initializr limpio, `pom.xml` sin dependencias sobrantes, MySQL en Docker Compose, configuración externalizada en properties y variables de entorno, arranque sin errores |
| 2 | Modelo de datos y API REST (IL3) | **25** | Entidades y API "perfectamente diseñadas"; CRUD "completo y eficiente" | `Album` 1:N `Lamina`, DTOs, códigos HTTP correctos, rutas REST coherentes, paginación |
| 3 | Funcionalidades especiales de láminas (IL4) | **25** | "Operaciones eficientes y precisas"; separación entre lógica de negocio y acceso a datos | Carga en lote transaccional, faltantes y repetidas resueltas con consultas en el repositorio, lógica en los Services, controllers delgados |
| 4 | **Validaciones y auditoría** (IL2) | **20** | "Validaciones avanzadas y auditorías detalladas para rastrear cambios" | Bean Validation, validaciones de negocio, errores uniformes, **migraciones Flyway** (lo que pidió el profesor), JPA Auditing y Hibernate Envers |
| 5 | Calidad del informe y documentación del código | **10** | Informe bien estructurado; explicación clara de todo el código | Informe PDF con formato exacto, justificación de la tecnología, Javadoc, Swagger y colección Postman |

### 2.2 Riesgos

- **Informe con tope de 15 páginas, anexo incluido** (aclaración recibida por el grupo el 13-09; el enunciado dice 5 a 10) y un screenshot por prueba: las capturas representativas van en el cuerpo y el resto en el Anexo A, en grilla.
- **"Eficiente"** aparece en los indicadores 2 y 3: faltantes y repetidas se resuelven con consultas a la BD, nunca filtrando listas en memoria; la carga en lote va con `@Transactional`.
- **Plazo corto (~1 semana):** el alcance mínimo va primero y los extras (Envers, tests) después.

---

## 3. Diseño

### 3.1 Arquitectura en capas

```
Cliente (Postman / Swagger UI)
      │ JSON · multipart
      ▼
controller ──► service (interfaz + impl) ──► repository (JpaRepository) ──► MySQL 8.4
   │  DTOs         │ reglas de negocio,              ▲
   │  @Valid       │ @Transactional                  │ esquema versionado
   ▼               ▼                                  │
exception      FileStorageService ──► volumen uploads/  Flyway (db/migration)
(ProblemDetail)
```

### 3.2 Estructura del repositorio

```
EXAMEN-TRANSVERSAL/                  ← repo GitHub
├── AGENTS.md · CLAUDE.md · BRIEF.md · README.md
├── compose.yaml                     ← mysql + api + volúmenes
├── .env.example                     ← variables (sin secretos reales)
├── backend/                         ← proyecto generado con Spring Initializr
│   ├── Dockerfile                   ← multi-stage (Maven → JRE 21)
│   ├── mvnw · pom.xml
│   └── src/main/
│       ├── java/cl/ipss/coleccion/
│       │   ├── config/        (JpaAuditingConfig, OpenApiConfig)
│       │   ├── controller/    (AlbumController, LaminaController)
│       │   ├── dto/           (records request/response)
│       │   ├── entity/        (Album, Lamina, TipoLamina, EntidadAuditable, Revision)
│       │   ├── exception/     (RecursoNoEncontradoException, ReglaNegocioException, GlobalExceptionHandler)
│       │   ├── mapper/        (AlbumMapper, LaminaMapper)
│       │   ├── repository/    (AlbumRepository, LaminaRepository)
│       │   ├── service/       (AlbumService, LaminaService, FileStorageService + impl/)
│       │   └── validation/    (validadores personalizados)
│       └── resources/
│           ├── application.properties
│           └── db/migration/  (V1__..., V2__..., ...)
└── docs/
    ├── api/postman/                 ← colección Postman (+ Newman)
    ├── evidencias/                  ← screenshots numerados
    └── informe/                     ← fuente y PDF del informe
```

### 3.3 Modelo de datos

**Album** (tabla `album`)
| Campo | Columna | Tipo | Validación |
|-------|---------|------|------------|
| id | `id` | BIGINT PK autoincremental | — |
| nombre | `nombre` | VARCHAR(100) | `@NotBlank`, `@Size(max=100)`, **único** |
| imagen | `imagen` | VARCHAR(500) | opcional, URL válida |
| fechaLanzamiento | `fecha_lanzamiento` | DATE | `@NotNull`, `@PastOrPresent` |
| tipoLaminas | `tipo_laminas` | VARCHAR(30) (enum) | `@NotNull` (ADHESIVA, TROQUELADA, TARJETA…) |
| totalLaminas | `total_laminas` | INT | `@NotNull`, `@Min(1)` |
| editorial, descripcion | … | VARCHAR | opcionales |
| auditoría | `creado_en`, `modificado_en`, `creado_por`, `modificado_por` | | automático |

**Lamina** (tabla `lamina`)
| Campo | Columna | Tipo | Validación |
|-------|---------|------|------------|
| id | `id` | BIGINT PK | — |
| numero | `numero` | INT | `@NotNull`, `@Min(1)`, **≤ totalLaminas del álbum**, **único por álbum** (`UNIQUE(album_id, numero)`) |
| nombre | `nombre` | VARCHAR(100) | `@NotBlank` |
| tipo | `tipo` | VARCHAR(30) (enum `TipoLamina`) | `@NotNull` (NORMAL, BRILLANTE, ESPECIAL, ESCUDO…) |
| cantidad | `cantidad` | INT DEFAULT 0 | `@Min(0)`: copias que tiene el coleccionista |
| foto | `foto` | VARCHAR(255) | **opcional**; guarda solo el nombre/ruta del archivo |
| album | `album_id` | FK → album.id, NOT NULL, ON DELETE CASCADE | — |
| auditoría | ídem | | automático |

**Regla de negocio central:**
- **Faltante** → `cantidad = 0`
- **Obtenida** → `cantidad ≥ 1`
- **Repetida** → `cantidad > 1`; **repetidas = cantidad − 1**

### 3.4 Endpoints

**Álbumes** — `/api/albumes`
| Método | Ruta | Descripción | Respuestas |
|--------|------|-------------|------------|
| POST | `/api/albumes` | Crear álbum | 201 + `Location` · 400 · 409 |
| GET | `/api/albumes` | Listar (paginado `?page&size&sort`) | 200 |
| GET | `/api/albumes/{id}` | Obtener | 200 · 404 |
| PUT | `/api/albumes/{id}` | Actualizar | 200 · 400 · 404 · 409 |
| DELETE | `/api/albumes/{id}` | Eliminar (cascada a láminas y fotos) | 204 · 404 |
| GET | `/api/albumes/{id}/resumen` | Totales: obtenidas, faltantes, repetidas, % completado | 200 · 404 |
| GET | `/api/albumes/{id}/historial` | Revisiones de auditoría (Envers) | 200 · 404 |

**Láminas**
| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/albumes/{albumId}/laminas` | Crear una lámina |
| POST | `/api/albumes/{albumId}/laminas/lote` | **Carga masiva**: lista de láminas; todo o nada |
| POST | `/api/albumes/{albumId}/laminas/registrar` | **Registrar obtenidas por número**: `{"numeros":[1,5,5,12]}` suma cantidades |
| GET | `/api/albumes/{albumId}/laminas` | Listar (filtro opcional `?tipo=`) |
| GET | `/api/albumes/{albumId}/laminas/faltantes` | **Faltantes** |
| GET | `/api/albumes/{albumId}/laminas/repetidas` | **Repetidas con cantidad de repetidas c/u** |
| GET | `/api/laminas/{id}` | Obtener |
| PUT | `/api/laminas/{id}` | Actualizar |
| PATCH | `/api/laminas/{id}/cantidad` | Sumar o restar copias (`{"delta": -1}`) |
| DELETE | `/api/laminas/{id}` | Eliminar |
| POST | `/api/laminas/{id}/foto` | **Subir foto opcional** (`multipart/form-data`, campo `archivo`) |
| GET | `/api/laminas/{id}/foto` | Ver la foto (bytes + `Content-Type`) |
| DELETE | `/api/laminas/{id}/foto` | Quitar la foto |
| GET | `/api/laminas/{id}/historial` | Revisiones de auditoría (Envers) |

Respuesta de repetidas (implementada así en la Fase 3):
```json
{
  "albumId": 1, "laminasRepetidas": 2, "totalCopiasRepetidas": 3,
  "laminas": [
    { "id": 5, "numero": 5, "nombre": "Messi", "tipo": "BRILLANTE", "cantidad": 3, "repetidas": 2, "estado": "REPETIDA" },
    { "id": 9, "numero": 9, "nombre": "Escudo Chile", "tipo": "ESCUDO", "cantidad": 2, "repetidas": 1, "estado": "REPETIDA" }
  ]
}
```
Faltantes devuelve `laminas` (catalogadas con cantidad 0) **y** `numerosNoCatalogados` (números de 1..total que aún no se cargan), además de `totalFaltantes`. Así el listado es preciso aunque el catálogo esté incompleto.

Error uniforme (RFC 9457 `ProblemDetail`):
```json
{
  "type": "about:blank", "title": "Datos inválidos", "status": 400,
  "detail": "La solicitud contiene errores de validación",
  "instance": "/api/albumes",
  "errores": { "nombre": "no debe estar vacío", "totalLaminas": "debe ser mayor o igual a 1" }
}
```

### 3.5 Validaciones (indicador 4)

- **Básicas:** `@Valid` en los controllers y Bean Validation en los DTOs.
- **Avanzadas:**
  - `GlobalExceptionHandler` (`@RestControllerAdvice`) con respuestas `ProblemDetail` para 400, 404, 409, 413, 415 y 500.
  - Reglas de negocio en el Service:
    - número de lámina dentro de `1..totalLaminas`;
    - número duplicado en el álbum → 409;
    - nombre de álbum duplicado → 409;
    - bajar `totalLaminas` por debajo del número de lámina más alto → 409;
    - la cantidad nunca queda negativa;
    - lote con números repetidos dentro de la lista → 400 indicando qué posiciones fallan, con rollback completo.
  - Validador personalizado (`@Constraint`), p. ej. `@NumerosUnicos` para el lote.
  - Foto: solo `image/jpeg`, `image/png` o `image/webp`, máximo 5 MB, con verificación del contenido (no solo de la extensión).
  - Integridad también en la BD: `NOT NULL`, `UNIQUE`, `FOREIGN KEY` y `CHECK (cantidad >= 0)` en las migraciones.

### 3.6 Auditoría (indicador 4). Prioridad según el profesor

1. **Migraciones Flyway (OBLIGATORIO):** el esquema se versiona en `db/migration` (`V1__crear_tabla_album.sql`, `V2__crear_tabla_lamina.sql`, `V3__agregar_columnas_auditoria.sql`, `V4__crear_tablas_envers.sql`, `V5__datos_semilla.sql`…). Las columnas de auditoría van en una migración aparte (V3) para que el historial muestre la evolución del esquema. Hibernate solo **valida** (`ddl-auto=validate`). La tabla `flyway_schema_history` registra qué cambio se aplicó, cuándo, con qué checksum y si tuvo éxito. **Esa es la evidencia principal para el informe.**
2. **JPA Auditing:** `@EnableJpaAuditing` + clase base `EntidadAuditable` con `@CreatedDate`, `@LastModifiedDate`, `@CreatedBy` y `@LastModifiedBy`. Un `AuditorAware` lee el header `X-Usuario` o usa "sistema".
3. **Hibernate Envers (Sobresaliente, "rastrear cambios"):** `@Audited` en `Album` y `Lamina`, con tablas `album_aud`, `lamina_aud` y `revinfo`, **también creadas por migración**. Endpoints `/historial`.
4. **Logging SLF4J** en los Services: una línea INFO por cada operación de escritura.

### 3.7 Configuración (indicador 1)

**Spring Initializr** (start.spring.io): Maven · Java 21 · Spring Boot **4.1.x** (última estable) · Group `cl.ipss` · Artifact `coleccion-laminas` · Package `cl.ipss.coleccion` · Packaging Jar · Properties.

**Dependencias desde Initializr:** Spring Web · Spring Data JPA · MySQL Driver · Validation · Flyway Migration · Lombok (opcional).
**Agregar a mano:**
- `org.hibernate.orm:hibernate-envers`, versión gestionada por Boot.
- `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.1.0`. Springdoc **3.x** es la línea compatible con Boot 4; la 2.x no sirve.

Hay que confirmar que el `pom.xml` generado incluya `flyway-mysql`, que desde Flyway 10 es necesario para MySQL.

`application.properties` (esqueleto):
```properties
spring.application.name=coleccion-laminas
server.port=8080

spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/coleccion_laminas?serverTimezone=America/Santiago}
spring.datasource.username=${DB_USER:coleccion}
spring.datasource.password=${DB_PASSWORD:coleccion}

# El esquema lo gestiona Flyway; Hibernate solo valida
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false
spring.jpa.show-sql=false
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration

# Envers: sufijo en minúsculas (MySQL en Linux distingue mayúsculas en nombres de tabla)
spring.jpa.properties.org.hibernate.envers.audit_table_suffix=_aud

spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=6MB
app.upload.dir=${UPLOAD_DIR:uploads}

# ProblemDetail lo genera GlobalExceptionHandler (no activar spring.mvc.problemdetails.enabled, ver AGENTS §11)
springdoc.swagger-ui.path=/swagger-ui.html
```

### 3.8 Docker

`compose.yaml` en la raíz:
- **mysql:** imagen `mysql:8.4` (LTS), base `coleccion_laminas`, usuario y clave desde `.env`, volumen `mysql-data`, `healthcheck` con `mysqladmin ping`, puerto `3306`.
- **api:** build desde `backend/Dockerfile` (multi-stage: `eclipse-temurin:21-jdk` con el Maven Wrapper para compilar y `eclipse-temurin:21-jre` con usuario sin privilegios para ejecutar), `depends_on: mysql (service_healthy)`, `DB_URL=jdbc:mysql://mysql:3306/...`, volumen `uploads` montado en `/app/uploads`, puerto `8080`.

Comandos:
- Todo el sistema: `docker compose up --build`
- Desarrollo local: `docker compose up -d mysql` + `./mvnw spring-boot:run`

### 3.9 Documentación y pruebas

- **Swagger UI** en `/swagger-ui.html`, con `@Tag`, `@Operation`, `@ApiResponse` y `@Schema` en endpoints y DTOs.
- **Colección Postman** en `docs/api/postman/`, con las 30 requests numeradas 12–41, variables encadenadas y tests; se ejecuta también con Newman. El profesor citó Bruno como ejemplo; el grupo eligió Postman (13-09).
- **README.md:** requisitos, cómo levantar el proyecto, variables, tabla de endpoints y ejemplos.
- **Pruebas manuales:** caso feliz + al menos un caso de error por endpoint (400, 404, 409, 413/415 en fotos) → screenshots en `docs/evidencias/NN-endpoint-caso.png`.
- **Tests automáticos (extra):** JUnit 5 + Mockito sobre `LaminaService` (faltantes, repetidas, lote, registrar) y un `@WebMvcTest` del controller.

---

## 4. Informe (PDF)

| Requisito | Valor |
|-----------|-------|
| Formato | **PDF** (el profesor pidió evitar Word) |
| Extensión | **5 a 15 páginas, incluido el Anexo A** (aclaración recibida por el grupo el 13-09; el enunciado dice 5 a 10) |
| Letra | Arial 12 · interlineado 1,15 · justificado · páginas numeradas |
| Portada | Título, asignatura, **logo IPSS**, profesor (Boris Marcelo Belmar Muñoz) y alumnos (Grupo 2: Valeria Gómez y Otton Lucena) |
| Estructura | Portada · Índice · Introducción · Desarrollo · Pruebas · Conclusión · Bibliografía · Anexo A (capturas) |
| Nombre | `EXT_2_LUCENA_GOMEZ_VALERIA_OTTON.pdf` (confirmado por el grupo) |
| Generación | `python3 docs/informe/generar_informe.py`: todas las cifras salen de `docs/evidencias/` |

**Distribución sugerida:**
1. Portada
2. Índice
3. Introducción: problema, objetivo, alcance y **justificación de la tecnología** (Spring Boot, MySQL, Flyway, Docker: por qué cada uno)
4. Desarrollo: configuración (Initializr, `pom.xml`, properties, Docker Compose) + arquitectura en capas
5. Desarrollo: modelo de datos (diagrama ER) + entidades, repositorios y services
6. Desarrollo: validaciones, manejo de errores y **auditoría** (migraciones Flyway + `flyway_schema_history`, JPA Auditing, Envers)
7. Desarrollo: tabla de endpoints
8–9. Pruebas: tabla resumen + screenshots agrupados
10. Conclusión + bibliografía APA (docs de Spring Boot, Spring Data JPA, Flyway, Hibernate Envers, springdoc, MySQL, Docker)
11–15. Anexo A: la captura de cada prueba de la colección que no está en el cuerpo, más proyecto, BD y salidas completas de las suites

---

## 5. Checklist de entrega

**Código**
- [x] Proyecto Maven generado con Initializr; `./mvnw verify` pasa (Testcontainers + MySQL 8.4)
- [x] `compose.yaml` levanta MySQL 8.4 y la API sin errores
- [x] Flyway crea todo el esquema (V1–V5); `ddl-auto=validate` no reporta diferencias
- [x] Entidades `Album` y `Lamina` con los campos pedidos (nombre, imagen, fecha de lanzamiento, tipo de láminas…)
- [x] CRUD completo de álbumes
- [x] CRUD completo de láminas
- [x] Foto opcional (subir, ver, quitar) por `multipart`, guardada en disco
- [x] Carga de láminas en lote (transaccional)
- [x] Endpoint de faltantes
- [x] Endpoint de repetidas con la cantidad de repetidas por lámina
- [x] Resumen del álbum
- [x] Registrar obtenidas por número y ajustar cantidad (PATCH)
- [x] Capas controller / service / repository + DTOs (nunca se exponen entidades)
- [x] Bean Validation + reglas de negocio + validador personalizado (`@NumerosUnicos`)
- [x] `GlobalExceptionHandler` con `ProblemDetail` (400, 404, 409, 413, 415, 500)
- [x] JPA Auditing (fechas y usuario vía header `X-Usuario`, bloque `auditoria` en las respuestas)
- [x] Envers + endpoints `/historial` (incluye registros eliminados)
- [x] Logging en los Services
- [x] Swagger funcionando (21 operaciones documentadas, header `X-Usuario` en escrituras)
- [x] Javadoc en las clases públicas
- [x] README + colección Postman (30 requests numeradas como las capturas; migrada desde Bruno el 13-09) + datos semilla (V5)
- [x] Tests automáticos: 11 (unitarios, MockMvc, integración) + e2e (110) + Newman (30 requests, 31 assertions); verificado desde cero el 13-09

**Informe** (verificado sobre el PDF final del 13-09)
- [x] Formato exacto (Arial 12, 1,15, justificado, numerado): `pdffonts` lista solo ArialMT, Arial-BoldMT y Arial-ItalicMT; 14 páginas; el script corta si el PDF no incrusta Arial
- [x] Portada completa con logo IPSS, profesor, integrantes y grupo
- [x] Justificación de la tecnología
- [x] Todas las secciones (más el Anexo A)
- [x] Screenshot de cada prueba: 47 capturas revisadas una por una; cada request 12–41 tiene su figura (cuerpo o Anexo A)
- [x] Evidencia de migraciones (`flyway_schema_history`) y de auditoría (`revinfo` + `lamina_aud`)

**Entrega**
- [x] Repositorio en GitHub público (verificado con `git clone` sin credenciales el 13-09), con la Fase 6 subida
- [x] `ENLACE_GITHUB.txt` con el link
- [x] ZIP = proyecto (sin `target/`, `uploads/`, `.env`, `.git/`, `.docx` ni material del curso) + informe PDF + `.txt` con el link + `docs/evidencias/`; probado desde cero
- [x] Nombre: `EXT_2_LUCENA_GOMEZ_VALERIA_OTTON`
- [ ] Subido antes del **17-09-2026**

---

## 6. Plan de trabajo

| Fase | Tareas | Indicador | Fecha objetivo |
|------|--------|-----------|----------------|
| 1 | Initializr, `pom.xml`, `compose.yaml`, properties, `V1`/`V2` con Flyway, arranque limpio | 1 (20) | 11-09 |
| 2 | Entidades, repositorios, DTOs, mappers, CRUD de álbum y lámina | 2 (25) | 12-09 |
| 3 | Lote, registrar, faltantes, repetidas, resumen, fotos | 3 (25) | 13-09 |
| 4 | Validaciones, `ProblemDetail`, JPA Auditing, Envers (`V3`), logging | 4 (20) | 14-09 |
| 5 | Swagger, Bruno, README, tests, screenshots | 5 (10) | 15-09 |
|   | *Fase 1 completada el 10-09: Boot 4.1.1, compose (MySQL 8.4 + API), Flyway V1–V2, Testcontainers* | | ✅ |
|   | *Fase 2 completada el 10-09: entidades, repositorios, DTOs, mappers, CRUD de álbum y lámina, errores ProblemDetail; 29/29 pruebas e2e OK* | | ✅ |
|   | *Fase 3 completada el 10-09: lote, registrar, PATCH cantidad, faltantes, repetidas, resumen (1 consulta agregada), fotos multipart; 55/55 pruebas e2e OK* | | ✅ |
|   | *Fase 4 completada el 10-09: migraciones V3 (columnas auditoría) y V4 (Envers), JPA Auditing con `X-Usuario`, historial con Envers* | | ✅ |
|   | *Fase 5 completada el 10-09: Swagger/OpenAPI, colección Bruno, README, V5 datos semilla, tests JUnit/MockMvc, `pruebas/e2e.sh`* | | ✅ |
| 6 | Informe PDF, revisión de formato, GitHub y ZIP | 5 (10) | 16-09 |
|   | *Fase 6 completada el 13-09: verificación desde cero (11 tests, 110 e2e, Newman 30 requests y 31 assertions), colección migrada de Bruno a Postman, 47 evidencias, informe en Arial (14 páginas, Anexo A), ZIP probado desde cero y push. Falta solo subir el ZIP a la plataforma* | | ✅ |
| — | Colchón | — | 17-09 |

## 7. Pendientes para confirmar con el profesor
1. ~~¿Los anexos con screenshots cuentan dentro del máximo de 10 páginas?~~ Resuelto el 13-09: se acepta anexo; el profesor confirmó que el anexo no cuenta dentro del límite de páginas.
2. ~~¿Qué apellido/nombre va en `EXT_GRUPO_APELLIDO_NOMBRE` si el trabajo es grupal?~~ Resuelto: `EXT_2_LUCENA_GOMEZ_VALERIA_OTTON`.
3. ~~¿El repositorio de GitHub puede ser privado?~~ No aplica: el repositorio es público.

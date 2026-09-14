# AGENTS.md — Colección de Láminas (Examen Transversal · Desarrollo de Software Web II)

Reglas y lineamientos para cualquier agente de código o persona que trabaje en este repositorio.
Complementa a `BRIEF.md`, que contiene el análisis, el modelo de datos, los endpoints y el plan. **Léelo antes de empezar cualquier tarea.**

---

## 1. Contexto

API REST en Spring Boot para coleccionistas de láminas de álbumes: CRUD de álbumes y láminas, foto opcional por lámina, carga en lote y listados de faltantes y repetidas. Es un examen evaluado con rúbrica (100 pts), así que **cada decisión debe poder mostrarse como evidencia en el informe**.

### Orden de prioridad de las fuentes
Si dos fuentes se contradicen, gana la de más arriba:
1. **Indicaciones del profesor en clase** (`text-clase`): MySQL obligatorio, auditoría = migraciones, Docker, sin NoSQL.
2. **Rúbrica** (`Rúbrica examen - Desarrollo de sotfware II (1).pdf`): "al pie de la letra".
3. **Enunciado** (`Examen transversal (1).pdf`).
4. **`BRIEF.md`**.

Los PDFs y `text-clase` son material de referencia: **no se modifican ni se mueven**.

---

## 2. Stack y versiones (verificadas el 10-09-2026)

| Componente | Versión | Nota |
|------------|---------|------|
| Java | **21** (LTS) | Boot 4.1 requiere Java 17+ |
| Spring Boot | **4.1.x** (última estable, 4.1.1 a ago-2026) | Spring Framework 7, Jakarta EE 11, **Jackson 3** |
| Build | Maven con **wrapper** (`./mvnw`) | Boot exige Maven ≥ 3.6.3 |
| BD | **MySQL 8.4** (LTS) en Docker | **Obligatorio. No usar H2, PostgreSQL ni NoSQL.** |
| Migraciones | **Flyway** (`spring-boot-starter-flyway` + `flyway-mysql`) | Obligatorio (auditoría según el profesor) |
| ORM | Spring Data JPA / Hibernate 7 | `ddl-auto=validate` |
| Auditoría histórica | `org.hibernate.orm:hibernate-envers` | Versión gestionada por Boot |
| Validación | `spring-boot-starter-validation` | Jakarta Bean Validation |
| Docs API | `springdoc-openapi-starter-webmvc-ui` **3.1.0** | Boot 4 ⇒ springdoc **3.x**; la 2.x no funciona |
| Pruebas API | Postman (colección en `docs/api/postman/`, ejecutable con Newman) | El profesor citó Bruno como ejemplo; el grupo eligió Postman (13-09) |
| Tests de integración | Testcontainers 2.x (`testcontainers-mysql`, imagen `mysql:8.4`) | `./mvnw verify` usa MySQL real; **requiere Docker corriendo** |
| Contenedores | Docker Compose v2 (`compose.yaml`) | |

**No agregar dependencias fuera de esta tabla sin justificarlo.** La rúbrica premia "dependencias correctamente establecidas" y el `pom.xml` debe quedar limpio.

---

## 3. Estructura del repositorio

```
/                         AGENTS.md · CLAUDE.md · BRIEF.md · README.md · compose.yaml · .env.example
backend/                  proyecto Spring Boot (Initializr) + Dockerfile
  src/main/java/cl/ipss/coleccion/
    audit/ config/ controller/ dto/ entity/ exception/ mapper/ repository/ service/ service/impl/ validation/
  src/main/resources/
    application.properties
    db/migration/         V{n}__{descripcion}.sql
  src/test/java/cl/ipss/coleccion/
docs/
  api/postman/            colección Postman + archivos de prueba
  evidencias/             screenshots NN-endpoint-caso.png
  informe/                informe (fuente + PDF) y GUIA_INFORME_Y_CAPTURAS.md
pruebas/e2e.sh            pruebas end-to-end (curl + python3)
```

## 4. Comandos

| Acción | Comando (desde la raíz) |
|--------|-------------------------|
| Levantar todo | `docker compose up --build` |
| Solo MySQL (desarrollo) | `docker compose up -d mysql` |
| Correr la API local | `cd backend && ./mvnw spring-boot:run` |
| Compilar + tests | `cd backend && ./mvnw verify` (Docker debe estar corriendo: Testcontainers) |
| Reconstruir solo la API | `docker compose up -d --build api` |
| Ver migraciones aplicadas | `docker compose exec mysql mysql -u coleccion -p coleccion_laminas -e "SELECT * FROM flyway_schema_history;"` |
| Pruebas end-to-end | `bash pruebas/e2e.sh` (con la API levantada) |
| Colección Postman (Newman) | `npx newman run docs/api/postman/coleccion-laminas.postman_collection.json --working-dir docs/api/postman` |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Detener y borrar datos | `docker compose down -v` (**destructivo: pedir confirmación antes**) |

---

## 5. Reglas de arquitectura

1. **Capas estrictas:** `controller → service → repository`. Un controller nunca usa un repository directamente.
2. **Controllers delgados:** reciben el request, lo validan con `@Valid`, llaman al service y devuelven el `ResponseEntity`. No tienen lógica de negocio ni `try/catch`.
3. **Services:** interfaz en `service/` e implementación en `service/impl/`. Toda la lógica de negocio vive aquí. Las escrituras llevan `@Transactional`; las lecturas, `@Transactional(readOnly = true)`.
4. **Repositories:** extienden `JpaRepository`. Faltantes, repetidas y resumen se resuelven **con consultas** (métodos derivados o `@Query` JPQL/proyecciones), **nunca** cargando la lista completa y filtrando en Java.
5. **DTOs:** `record` de Java para request y response. **Nunca se expone una entidad JPA en un endpoint.** Los mappers son clases manuales en `mapper/` (sin MapStruct).
6. **Inyección por constructor** con campos `final`. Sin `@Autowired` en campos.
7. **Evitar N+1:** relaciones `@ManyToOne(fetch = LAZY)` y `open-in-view=false`. Cuando haga falta, usar `JOIN FETCH` o `@EntityGraph`.

## 6. Persistencia y migraciones (Flyway)

1. **El esquema lo crea y lo cambia solo Flyway.** `spring.jpa.hibernate.ddl-auto=validate` siempre. **Prohibido** usar `update`, `create` o `create-drop` en el código entregado.
2. Scripts en `backend/src/main/resources/db/migration/` con nombre `V{n}__{descripcion_en_snake_case}.sql` (dos guiones bajos). Ej.: `V1__crear_tabla_album.sql`.
3. **Nunca editar una migración ya aplicada.** Todo cambio va en una migración nueva (`V5__agregar_columna_x.sql`). Si se edita una aplicada, Flyway falla por checksum.
4. Nombres de tablas y columnas en **snake_case y minúsculas**, para que coincidan con la estrategia de nombres de Spring (`fechaLanzamiento` → `fecha_lanzamiento`). En Linux, MySQL distingue mayúsculas en nombres de tabla.
5. La integridad también se define en SQL: `NOT NULL`, `UNIQUE (album_id, numero)`, `FOREIGN KEY ... ON DELETE CASCADE`, `CHECK (cantidad >= 0)`. Motor `InnoDB` y `utf8mb4`.
6. Las tablas de Envers (`revinfo`, `album_aud`, `lamina_aud`) **también se crean por migración**. Se usa una entidad de revisión propia (`Revision`, tabla `revinfo`, id `IDENTITY` y columna `usuario`) para controlar el esquema. Sufijo configurado en `_aud`.
7. Si `validate` falla por tipos en las tablas `_aud`, se puede generar el DDL de referencia en un perfil local con `spring.jpa.properties.jakarta.persistence.schema-generation.scripts.action=create` y copiarlo a la migración. Ese perfil no se commitea activado.
8. Datos semilla en una migración propia (`V5__datos_semilla.sql`) para que el profesor pueda probar de inmediato.
9. Secuencia planificada: `V1` álbum · `V2` lámina · `V3` columnas de auditoría · `V4` tablas Envers · `V5` datos semilla. Cada cambio posterior va en `V6`, `V7`, etc.

## 7. API REST

1. Rutas en **español, plural y kebab/minúsculas**: `/api/albumes`, `/api/albumes/{albumId}/laminas`, `/api/laminas/{id}`. El listado exacto está en `BRIEF.md §3.4`; no inventar rutas nuevas sin actualizar el brief.
2. Verbos y códigos:

   | Operación | Código |
   |-----------|--------|
   | POST que crea | `201 Created` + header `Location` |
   | GET | `200` |
   | PUT / PATCH | `200` con el recurso actualizado |
   | DELETE | `204 No Content` |
   | Validación | `400` |
   | No existe | `404` |
   | Duplicado o conflicto de negocio | `409` |
   | Archivo muy grande | `413` |
   | Tipo de archivo inválido | `415` |

3. Los listados de álbumes van paginados (`Pageable`); los de láminas por álbum pueden ir sin paginar (son acotados por `totalLaminas`).
4. **Errores siempre como `ProblemDetail` (RFC 9457)** desde un único `@RestControllerAdvice`, con propiedad extra `errores` (mapa campo → mensaje) en los 400. Nunca devolver stack traces.
5. Mensajes de error y de validación **en español**.
6. JSON en camelCase y fechas ISO-8601 (`2026-09-10`).

## 8. Validaciones

- Bean Validation en los DTOs de entrada (`@NotBlank`, `@NotNull`, `@Size`, `@Min`, `@PastOrPresent`, `@URL`…).
- Reglas de negocio en el service, con excepciones propias (`RecursoNoEncontradoException` → 404, `ReglaNegocioException` → 409):
  - número de lámina en `1..totalLaminas`;
  - número único por álbum;
  - nombre de álbum único;
  - `cantidad` nunca negativa;
  - no reducir `totalLaminas` por debajo de la lámina de mayor número;
  - lote sin números duplicados (todo o nada).
- Al menos **un validador personalizado** (`@Constraint` + `ConstraintValidator`) en `validation/`.
- Fotos: solo JPEG, PNG o WEBP, máximo 5 MB; se verifica el tipo real del contenido, no solo la extensión.

## 9. Auditoría

1. **Flyway** (obligatorio): historial versionado del esquema en `flyway_schema_history`.
2. **JPA Auditing:** `@EnableJpaAuditing` en `config/`, clase base `EntidadAuditable` (`@MappedSuperclass` + `@EntityListeners(AuditingEntityListener.class)`) con `creadoEn`, `modificadoEn`, `creadoPor` y `modificadoPor`. El `AuditorAware` toma el header `X-Usuario` y, si no viene, usa `"sistema"`.
3. **Envers:** `@Audited` en `Album` y `Lamina`. Historial expuesto en `GET .../{id}/historial` usando `AuditReader`, con número de revisión, fecha, usuario, tipo (`ADD`/`MOD`/`DEL`) y snapshot.
   - Envers solo registra lo que pasa por Hibernate. Por eso, al eliminar un álbum se borran sus láminas como entidades (`deleteAll`) antes del álbum; la cascada `ON DELETE CASCADE` de la BD queda solo como respaldo. **No usar `deleteAllInBatch` ni `@Query` de borrado/actualización masiva en entidades auditadas.**
   - Las fechas de JPA Auditing se truncan a microsegundos (`DateTimeProvider` en `JpaAuditingConfig`) para que coincidan con `DATETIME(6)`.
   - Las actualizaciones hacen `saveAndFlush`/`flush` antes de mapear la respuesta, para que `modificadoEn/Por` salgan ya actualizados.
   - Los campos de `EntidadAuditable` **no** se auditan en las tablas `_aud` (superclase sin `@Audited`); el autor y la fecha de cada versión están en `revinfo`.
4. **Logging:** SLF4J (`private static final Logger log = LoggerFactory.getLogger(...)` o `@Slf4j`). Una línea `INFO` por cada escritura, con ids y sin datos sensibles. **Prohibido `System.out.println`.**

## 10. Archivos (fotos)

- Se reciben con `multipart/form-data` (`@RequestParam("archivo") MultipartFile`). **Prohibido guardar imágenes en Base64 o como BLOB en la BD.**
- Se guardan en `app.upload.dir` (volumen Docker `/app/uploads`) con nombre `UUID + extensión`. En la BD solo va el nombre del archivo.
- Hay que prevenir el path traversal: normalizar la ruta y verificar que quede dentro del directorio de subida.
- Al reemplazar o quitar una foto, o al eliminar una lámina o un álbum, se borra el archivo físico, siempre con `FileStorageService.eliminarAlConfirmar(...)`. El borrado ocurre solo después del commit, y un archivo recién subido se borra si la transacción hace rollback. **Nunca borrar archivos directamente dentro de una transacción.**
- El tipo se valida por los bytes iniciales (JPEG `FF D8 FF`, PNG `89 50 4E 47…`, WEBP `RIFF…WEBP`), no por la extensión ni por el Content-Type declarado.
- Toda la lógica de archivos va en `FileStorageService`, nunca en el controller.

## 11. Particularidades de Spring Boot 4

- **Jackson 3:** los paquetes de databind cambiaron a `tools.jackson.*`. Las anotaciones (`@JsonProperty`, `@JsonFormat`, etc.) siguen en `com.fasterxml.jackson.annotation`. No importar `com.fasterxml.jackson.databind`.
- **Starters modulares:** usar los que genera Initializr (p. ej. `spring-boot-starter-webmvc`, `spring-boot-starter-flyway`). No copiar `pom.xml` de tutoriales de Boot 2 o 3.
- **Tests:** `@MockBean` ya no existe; usar `@MockitoBean` (`org.springframework.test.context.bean.override.mockito`). Para `@WebMvcTest` se necesita `spring-boot-starter-webmvc-test`. Verificar los paquetes nuevos con Context7 antes de escribir el import.
- **springdoc 3.x**, nunca 2.x.
- **Enums en MySQL (Hibernate 6.2+/7):** `@Enumerated(EnumType.STRING)` se mapea por defecto al tipo nativo `ENUM` de MySQL, y `validate` falla contra columnas `VARCHAR`. Siempre agregar `@JdbcTypeCode(SqlTypes.VARCHAR)` (verificado en Fase 2).
- **Paginación:** devolver `PagedModel<T>` (`new PagedModel<>(page)`) desde el controller, nunca `Page<T>` directamente. El JSON queda estable: `{content, page:{size, number, totalElements, totalPages}}`.
- **Spring Data 4:** `PropertyReferenceException` está en `org.springframework.data.core` (antes en `data.mapping`).
- **Errores:** `GlobalExceptionHandler` extiende `ResponseEntityExceptionHandler`. Los nuevos casos se agregan ahí; no activar `spring.mvc.problemdetails.enabled`, porque duplicaría el handler.
- Ante cualquier duda de API, consultar la documentación de la versión 4.1 (§15). No confiar en ejemplos antiguos.

## 12. Estilo de código

- Dominio **en español y sin tildes ni ñ en identificadores**: `Album`, `Lamina`, `fechaLanzamiento`, `obtenerFaltantes()`. Palabras técnicas del framework en inglés como es habitual (`Controller`, `Service`, `Repository`, `Mapper`, `Config`).
- Clases en `PascalCase`, métodos y variables en `camelCase`, constantes en `UPPER_SNAKE_CASE`, enums en mayúsculas.
- **Javadoc en español** en toda clase pública y en los métodos de services y controllers: qué hace, parámetros, qué retorna y qué excepciones lanza. La rúbrica evalúa la "documentación del código".
- Comentarios solo cuando explican un **porqué** no evidente.
- Lombok permitido con moderación: `@Getter`, `@Setter`, `@NoArgsConstructor` y `@RequiredArgsConstructor`. **No usar `@Data` ni `@EqualsAndHashCode` o `@ToString` automáticos en entidades con relaciones** (ciclos y problemas con proxies).
- Enums persistidos con `@Enumerated(EnumType.STRING)`.
- Tipos: `Long` para ids, `LocalDate` para fechas y `Instant`/`LocalDateTime` para auditoría.
- Métodos cortos, sin código muerto, sin imports sin usar y sin warnings de compilación.

## 13. Configuración y secretos

- Toda configuración vive en `application.properties`, con variables de entorno y valores por defecto de desarrollo: `${DB_URL:...}`, `${DB_USER:...}`, `${DB_PASSWORD:...}`, `${UPLOAD_DIR:uploads}`.
- `.env` **no se commitea**; se versiona `.env.example`.
- `.gitignore` debe incluir `target/`, `uploads/`, `.env`, `.idea/`, `*.iml` y `.vscode/`.
- Zona horaria `America/Santiago` en la URL JDBC.

## 14. Pruebas y evidencias

- Cada endpoint tiene en la colección Postman **un caso feliz y al menos un caso de error**.
- Cada prueba ejecutada genera un screenshot en `docs/evidencias/` con el nombre `NN-recurso-accion-caso.png` (ej. `07-laminas-repetidas-ok.png`, `08-laminas-lote-duplicado-400.png`). Van todos al informe.
- Evidencias obligatorias además de los endpoints:
  - Initializr;
  - `pom.xml`;
  - `application.properties`;
  - `docker compose ps`;
  - tabla `flyway_schema_history`;
  - tablas `_aud` con registros;
  - Swagger UI.
- Tests automáticos, todos obligatorios en `./mvnw verify`:
  - unitarios con Mockito para la lógica de `ColeccionServiceImpl`;
  - `@WebMvcTest` + `@MockitoBean` para la capa web;
  - integración con Testcontainers, que aplica todas las migraciones y valida el esquema.
- Todo endpoint nuevo o modificado debe tener:
  - su request en la colección Postman, numerada y con tests (`pm.test`);
  - sus casos en `pruebas/e2e.sh`;
  - la actualización de la guía de capturas si cambia la numeración.
- Antes de dar una fase por terminada deben pasar las tres suites: `./mvnw verify`, `bash pruebas/e2e.sh` y la colección Postman con Newman.

## 15. Uso de documentación: Context7 y Tavily

- **Context7 primero** para cualquier API de librería (Spring Boot, Spring Data JPA, Hibernate/Envers, Flyway, springdoc, Bean Validation, Docker Compose). IDs verificados:
  - Spring Boot 4.1: `/spring-projects/spring-boot/v4.1.0`
  - springdoc: `/springdoc/springdoc-openapi`
  - Otros: resolver con `npx ctx7@latest library "<Nombre>" "<consulta>"` (p. ej. "Flyway", "Hibernate ORM", "Spring Data JPA").
- **Tavily** para lo que no está en la documentación: versión más reciente de un artefacto, matriz de compatibilidad, bugs conocidos y mensajes de error concretos. Preferir fuentes oficiales (spring.io, github.com/spring-projects, hibernate.org, documentation.red-gate.com/flyway, springdoc.org, mvnrepository.com).
- No inventar propiedades, anotaciones ni coordenadas Maven. Si no se pudo verificar, decirlo.

## 16. Definición de "terminado" para cada tarea

- [ ] Compila y `./mvnw verify` pasa sin warnings nuevos
- [ ] La app arranca contra MySQL en Docker y `validate` no reporta diferencias
- [ ] Si cambió el esquema, hay una migración nueva y no se editó una anterior
- [ ] Endpoint probado en Postman (caso feliz y caso de error) y visible en Swagger
- [ ] Javadoc agregado o actualizado
- [ ] Checklist de `BRIEF.md §5` marcado
- [ ] README o colección Postman actualizados si cambió la API

## 17. Prohibido

- Usar una BD distinta de MySQL, o NoSQL.
- `ddl-auto` distinto de `validate` en el código entregado; editar migraciones aplicadas.
- Lógica de negocio en controllers; repositories en controllers; exponer entidades.
- Imágenes en Base64 o BLOB en la BD.
- Filtrar faltantes o repetidas en memoria.
- Secretos en el repo.
- Dependencias sin uso en el `pom.xml`.
- Construir un frontend: está fuera de alcance salvo que el grupo lo decida explícitamente.
- `git push`, `git commit` o `docker compose down -v` sin que el usuario lo pida.

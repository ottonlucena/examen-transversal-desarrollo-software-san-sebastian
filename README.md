# Colección de Láminas – API REST

Sistema de gestión para coleccionistas de láminas de álbumes: CRUD de álbumes y láminas, foto opcional por lámina, carga masiva, registro de láminas obtenidas, listados de **faltantes** y **repetidas** (con la cantidad de repetidas de cada una) y auditoría completa de cambios.

Examen Transversal · Desarrollo de Software Web II · Instituto Profesional San Sebastián

## Stack

| Componente | Versión |
|------------|---------|
| Java | 21 |
| Spring Boot (Web MVC, Data JPA, Validation) | 4.1.1 |
| MySQL | 8.4 (Docker) |
| Flyway (migraciones) | gestionado por Spring Boot |
| Hibernate Envers (historial) | 7.4 |
| springdoc-openapi (Swagger UI) | 3.1.0 |
| Docker Compose | v2 |
| Pruebas | JUnit 5, Mockito, MockMvc, Testcontainers, Bruno, script e2e |

## Inicio rápido

Requisitos: Docker con Docker Compose v2.

```bash
docker compose up -d --build     # MySQL + API; Flyway crea el esquema y carga los datos de ejemplo
docker compose ps                # mysql "(healthy)" y api "Up"
```

| Recurso | URL |
|---------|-----|
| API | http://localhost:8080/api |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| OpenAPI (JSON) | http://localhost:8080/v3/api-docs |

**Datos de ejemplo** (migración V5): con la base recién creada, los álbumes de ejemplo quedan con id 1 (*Copa Mundial 2026*, 20 láminas, 18 catalogadas) e id 2 (*Fauna Chilena*, 12 láminas). Para probar de inmediato:

```bash
curl http://localhost:8080/api/albumes/1/resumen
curl http://localhost:8080/api/albumes/1/laminas/faltantes
curl http://localhost:8080/api/albumes/1/laminas/repetidas
```

Para detener los servicios sin perder los datos: `docker compose stop`. Para borrar la base y las fotos: `docker compose down -v` (⚠️ destructivo).

### Variables de entorno

Opcionales; si no se definen se usan los valores por defecto (`cp .env.example .env` para cambiarlos).

| Variable | Por defecto | Uso |
|----------|-------------|-----|
| `DB_NAME` | `coleccion_laminas` | Base de datos |
| `DB_USER` / `DB_PASSWORD` | `coleccion` / `coleccion` | Usuario de la aplicación |
| `DB_ROOT_PASSWORD` | `root` | Root de MySQL |
| `DB_PORT` / `API_PORT` | `3306` / `8080` | Puertos publicados |

### Desarrollo local (sin contenedor para la API)

Requiere Java 21.

```bash
docker compose up -d mysql
cd backend && ./mvnw spring-boot:run
```

## Endpoints

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/albumes` | Crear álbum |
| GET | `/api/albumes?page&size&sort` | Listar álbumes (paginado, máx. 100) |
| GET | `/api/albumes/{id}` | Obtener álbum |
| PUT | `/api/albumes/{id}` | Actualizar álbum |
| DELETE | `/api/albumes/{id}` | Eliminar álbum, sus láminas y sus fotos |
| POST | `/api/albumes/{albumId}/laminas` | Agregar lámina |
| GET | `/api/albumes/{albumId}/laminas?tipo=` | Listar láminas del álbum |
| GET | `/api/laminas/{id}` | Obtener lámina |
| PUT | `/api/laminas/{id}` | Actualizar lámina |
| DELETE | `/api/laminas/{id}` | Eliminar lámina |
| POST | `/api/albumes/{albumId}/laminas/lote` | **Carga masiva** (todo o nada) |
| POST | `/api/albumes/{albumId}/laminas/registrar` | **Registrar obtenidas** (`{"numeros":[1,5,5]}`) |
| PATCH | `/api/laminas/{id}/cantidad` | Sumar o restar copias (`{"delta":-1}`) |
| GET | `/api/albumes/{albumId}/laminas/faltantes` | **Faltantes** (sin copias + no catalogadas) |
| GET | `/api/albumes/{albumId}/laminas/repetidas` | **Repetidas** con cantidad de repetidas por lámina |
| GET | `/api/albumes/{albumId}/resumen` | Totales y % completado |
| POST | `/api/laminas/{id}/foto` | Subir foto (`multipart/form-data`, campo `archivo`; JPEG/PNG/WEBP ≤ 5 MB) |
| GET | `/api/laminas/{id}/foto` | Ver foto |
| DELETE | `/api/laminas/{id}/foto` | Quitar foto |
| GET | `/api/albumes/{id}/historial` | Historial de cambios del álbum |
| GET | `/api/laminas/{id}/historial` | Historial de cambios de la lámina |

Los request y response de cada endpoint, con ejemplos, están en Swagger UI y en la colección Bruno.

### Errores

Todos los errores usan el formato RFC 9457 (`application/problem+json`):

```json
{
  "type": "about:blank",
  "title": "Datos inválidos",
  "status": 400,
  "detail": "La solicitud contiene errores de validación",
  "instance": "/api/albumes",
  "timestamp": "2026-09-11T02:03:31.409Z",
  "errores": { "nombre": "El nombre es obligatorio", "totalLaminas": "El álbum debe tener al menos 1 lámina" }
}
```

| Código | Cuándo |
|:------:|--------|
| 400 | Datos inválidos, JSON mal formado, parámetros con tipo incorrecto |
| 404 | Recurso inexistente |
| 409 | Regla de negocio (duplicados, número fuera de rango, cantidad negativa, lote con conflictos) |
| 413 | Foto mayor a 5 MB |
| 415 | Archivo que no es JPEG/PNG/WEBP o petición que no es multipart |

## Auditoría

- **Migraciones Flyway** (`backend/src/main/resources/db/migration`): el esquema se versiona de V1 a V5. La tabla `flyway_schema_history` registra cada cambio. Hibernate solo valida (`ddl-auto=validate`).
- **JPA Auditing:** cada álbum y lámina guarda `creadoEn/creadoPor` y `modificadoEn/modificadoPor`. El usuario se toma del header **`X-Usuario`** (si falta o no es válido se usa `sistema`).
- **Hibernate Envers:** cada cambio queda en `album_aud`, `lamina_aud` y `revinfo` (revisión, fecha, usuario). Se consulta en los endpoints `/historial`, incluso para registros eliminados.

## Pruebas

```bash
cd backend && ./mvnw verify                          # unitarias + MockMvc + integración (Testcontainers, requiere Docker)
bash pruebas/e2e.sh                                  # 108 comprobaciones end-to-end contra la API levantada
cd docs/api/bruno && npx @usebruno/cli run -r --env local   # colección Bruno completa
```

La colección Bruno (`docs/api/bruno`) también se abre en la app de escritorio Bruno (*Open Collection*, entorno `local`). Antes de la request 36 (archivo > 5 MB), genera el archivo grande, que no se versiona:

```bash
cd docs/api/bruno
python3 -c "import os; open('archivos/grande.png','wb').write(b'\x89PNG\r\n\x1a\n' + os.urandom(6*1024*1024))"
```

## Estructura

```
├── compose.yaml              MySQL 8.4 + API
├── backend/                  Proyecto Spring Boot (Maven)
│   └── src/main/java/cl/ipss/coleccion/
│       ├── controller/       Endpoints REST (sin lógica de negocio)
│       ├── service/          Interfaces y lógica de negocio (impl/)
│       ├── repository/       Spring Data JPA
│       ├── entity/           Entidades JPA auditadas
│       ├── dto/  mapper/     Request/response y conversión (no se exponen entidades)
│       ├── exception/        Manejo global de errores (ProblemDetail)
│       ├── validation/       Validador personalizado @NumerosUnicos
│       ├── audit/  config/   Usuario de auditoría, JPA Auditing, OpenAPI
│       └── resources/db/migration/   V1..V5
├── docs/api/bruno/           Colección Bruno (requests numeradas)
├── docs/informe/             Guía del informe y capturas
└── pruebas/e2e.sh            Pruebas end-to-end
```

Más detalle: [`BRIEF.md`](BRIEF.md) (análisis y diseño) y [`AGENTS.md`](AGENTS.md) (reglas del proyecto).

# Guía de capturas, informe y entrega

Examen Transversal · Desarrollo de Software Web II · Prof. Boris Marcelo Belmar Muñoz
Grupo 2: Valeria Gómez y Otton Lucena · Archivo: `EXT_2_LUCENA_GOMEZ_VALERIA_OTTON` · Entrega: **17-09-2026**

> Las pruebas de la API se hacen con **Postman** (decisión del grupo del 13-09; el profesor citó Bruno solo como ejemplo). La colección está en `docs/api/postman/coleccion-laminas.postman_collection.json` y se verificó con Newman sobre una base limpia: **30 requests y 31 assertions, 0 fallos** (evidencia 46).
>
> Las capturas y el informe **ya están listos** (sección 0). Las secciones 1 a 5 explican cómo se tomaron y cómo regenerar el informe si hay que repetir alguna; las secciones 6 a 8 son la referencia y la lista de entrega.

---

## 0. Estado de las capturas (14-09-2026)

**Las 47 capturas están tomadas, revisadas una por una y dentro del informe**, así que no queda ninguna pendiente. Todas viven en `docs/evidencias/` con el **nombre exacto** que aparece en la sección 6.

| Origen | Capturas | Nota |
|--------|----------|------|
| Pantalla (Initializr, editor, Swagger, DBeaver) | 01, 02, 03, 04, 07, 10, 11, 43 | Tomadas a mano |
| Postman (requests 12 a 41 de la colección) | 12 a 41 | Una por prueba: caso feliz y casos de error |
| Terminal (script de capturas) | 05, 06, 08, 09, 42, 44, 45, 46, 47 | Generadas de la salida real de cada comando. No las edites |

Verificado el 14-09: están los 47 `.png` (sección 4), el PDF tiene 14 páginas en Arial y el ZIP coincide con el repositorio.

### Si hay que repetir alguna captura

1. Las de Postman (12 a 41) **necesitan la base recién creada**: la request 12 crea el álbum *Qatar 2022* y falla con 409 si ya existe. Deja la base limpia con `docker compose down -v` (⚠️ borra datos y fotos) y `docker compose up -d --build`, o elimina solo ese álbum con `curl -X DELETE http://localhost:8080/api/albumes/{id}`.
2. Repite la secuencia completa de la sección 3, porque las requests encadenan variables (`albumId`, `laminaId`).
3. Al guardar, usa el mismo nombre de archivo y acepta **Reemplazar**.
4. Regenera el informe (sección 5) y rehaz el ZIP: las cifras y las figuras se toman de `docs/evidencias/`.

---

## 1. Preparación (una sola vez)

### 1.1 Importar la colección en Postman

Postman ya está instalado (versión 11.77.2).

1. Abre Postman. Si te pide iniciar sesión para usar colecciones, inicia sesión o crea una cuenta gratuita.
2. Presiona **Import**, arriba a la izquierda, o `Ctrl + O`.
3. Elige **files** y selecciona `~/Escritorio/EXAMEN-TRANSVERSAL/docs/api/postman/coleccion-laminas.postman_collection.json`.
4. Presiona **Import**. En *Collections*, a la izquierda, aparece **Colección de Láminas API** con 5 carpetas:
   - 1. Álbumes
   - 2. Láminas
   - 3. Colección
   - 4. Fotos de láminas
   - 5. Auditoría
5. No hace falta crear un entorno. La URL `http://localhost:8080` ya está en la variable `baseUrl` de la colección, así que arriba a la derecha puede decir *No environment*.

### 1.2 Configurar la carpeta de archivos (para las fotos 33, 35 y 36)

Las requests de fotos usan archivos de `docs/api/postman/archivos/` (`lamina.png`, `falsa.png` y `grande.png`). Para que Postman los encuentre:

1. Abre **Settings**: el ícono de engranaje ⚙ arriba a la derecha → *Settings*.
2. En **General**, busca **Working directory** y presiona **Choose**.
3. Selecciona la carpeta `~/Escritorio/EXAMEN-TRANSVERSAL/docs/api/postman` y cierra *Settings*.

Si en la pestaña *Body* de la request 33, 35 o 36 el archivo aparece en rojo o con una advertencia, haz clic en el campo del archivo (*Select Files*) y elige a mano el archivo de `docs/api/postman/archivos/`: `lamina.png` para la 33, `falsa.png` para la 35 y `grande.png` para la 36.

### 1.3 Cómo sacar y guardar cada captura

1. Deja en pantalla lo que quieres capturar.
2. Presiona **`Mayús + Impr Pant`** (captura de un área) y arrastra el mouse sobre la zona útil.
3. Aparece la ventana **Guardar captura de pantalla**:
   - en *Nombre* escribe el nombre exacto, por ejemplo `12-album-crear-201.png`;
   - la carpeta ya está puesta en `EXAMEN-TRANSVERSAL/docs/evidencias`. Si no, elígela.
4. Presiona **Guardar**.

Si la ventana no aparece y la imagen se va a `~/Imágenes`, muévela con este comando (cambia el nombre final):

```bash
mv "$(ls -t ~/Imágenes/*.png | head -1)" ~/Escritorio/EXAMEN-TRANSVERSAL/docs/evidencias/12-album-crear-201.png
```

En Postman, la captura debe mostrar a la vez:
- **arriba**: el método y la URL de la request;
- **abajo**: el código de estado (por ejemplo `201 Created`) y el *Body* de la respuesta.

Captura solo esa región, no la pantalla completa. Usa el mismo tema, claro u oscuro, en todas.

### 1.4 Dejar la base de datos limpia (obligatorio antes de Postman)

La base local tiene datos agregados a mano (álbumes "Qatar 2022/2026"). La colección crea un álbum llamado "Qatar 2022" y cada request usa ids que guardan las anteriores, así que **debe ejecutarse sobre una base recién creada**:

```bash
cd ~/Escritorio/EXAMEN-TRANSVERSAL
docker compose down -v          # ⚠️ borra la BD y las fotos de desarrollo (tus capturas .png NO se tocan)
docker compose up -d --build    # MySQL + API; Flyway aplica V1..V5 y carga 2 álbumes de ejemplo
docker compose ps               # espera a ver: coleccion-mysql "Up (healthy)" y coleccion-api "Up"
ls -l docs/api/postman/archivos/grande.png   # debe existir (≈ 6,3 MB)
```

Si `grande.png` no existe, créalo así (no se sube a Git):

```bash
cd ~/Escritorio/EXAMEN-TRANSVERSAL/docs/api/postman
python3 -c "import os; open('archivos/grande.png','wb').write(b'\x89PNG\r\n\x1a\n' + os.urandom(6*1024*1024))"
```

Hasta terminar la request 41, **no ejecutes** el script e2e, Newman, el *Runner* de Postman ni pruebas en Swagger, porque cambian los ids.

---

## 2. Capturas que no dependen de la base de datos (01, 04, 43, 10)

### 2.1 `01-initializr.png`: Spring Initializr

Abre https://start.spring.io. La página viene por defecto con **Gradle y Java 17**, así que hay que cambiarlos. Completa todo así:

| Campo | Valor que debes marcar o escribir |
|-------|-----------------------------------|
| Project | **Maven** |
| Language | **Java** |
| Spring Boot | **4.1.1** |
| Group | `cl.ipss` |
| Artifact | `coleccion-laminas` |
| Name (si aparece) | `coleccion-laminas` |
| Description (si aparece) | `API REST para gestion de colecciones de laminas de albumes` |
| Package name | bórralo y escribe `cl.ipss.coleccion` |
| Packaging | **Jar** |
| Configuration | **Properties** |
| Java | **21** |

Luego haz clic en **ADD DEPENDENCIES…** y agrega estas 6, escribiendo el nombre en el buscador y haciendo clic en cada una:

1. **Spring Web**
2. **Spring Data JPA**
3. **MySQL Driver**
4. **Validation**
5. **Flyway Migration**
6. **Lombok**

**No presiones GENERATE.** Captura la página completa: el panel izquierdo con las opciones y el derecho con las 6 dependencias.

### 2.2 `04-estructura-proyecto.png`: paquetes en VS Code

1. En VS Code, en el panel *Explorador* de la izquierda, abre `backend` → `src` → `main` → `java` → `cl` → `ipss` → `coleccion`.
2. Deben verse las carpetas `audit`, `config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `service`, `validation` y el archivo `ColeccionLaminasApplication.java`.
3. Captura solo el panel del explorador con esas carpetas.

### 2.3 `43-validador-numeros-unicos.png`: el validador

1. En VS Code abre `backend/src/main/java/cl/ipss/coleccion/validation/`**`NumerosUnicosValidator.java`**. No abras `NumerosUnicos.java`, que es la anotación.
2. Se debe ver `public class NumerosUnicosValidator implements ConstraintValidator<...>` y el método `isValid`.
3. Captura el editor con ese código.

### 2.4 `10-diagrama-er.png`: diagrama entidad-relación en DBeaver

MySQL **no se abre en el navegador ni en Postman**. `localhost:3306` es para programas como DBeaver. El puerto es **3306** (tres-tres-cero-seis), **no 3036**. DBeaver CE ya está instalado.

1. Comprueba que los contenedores estén arriba con `docker compose ps`.
2. Abre DBeaver. En el *Navegador de base de datos*:
   - si ya existe la conexión **localhost (MySQL)**, haz clic derecho sobre ella y elige **Editar conexión** (F4);
   - si no existe, entra a **Base de datos → Nueva conexión → MySQL**.
3. Pestaña **Principal (Main)**:

   | Campo | Valor |
   |-------|-------|
   | Server Host | `localhost` |
   | Port | `3306` |
   | Database | `coleccion_laminas` |
   | Autenticación | Database Native |
   | Username | `coleccion` |
   | Password | `coleccion` (marca *Guardar contraseña*) |

4. Pestaña **Propiedades del driver (Driver properties)**: `allowPublicKeyRetrieval` = `true` (tu conexión ya lo tiene). Si aparece un error de SSL, agrega también `useSSL` = `false`.
5. Presiona **Probar conexión**. Si pide descargar el driver, acepta **Descargar**. Debe decir *Conectado*.
6. Presiona **Aceptar**. Luego abre la conexión → `coleccion_laminas`, haz clic derecho sobre `coleccion_laminas` y elige **Ver diagrama** (*View Diagram*).
7. En el diagrama deben verse `album` → `lamina` (1 a N) y `album_aud`, `lamina_aud` → `revinfo`. Ordénalo con el botón *Ajustar/Arrange* de la barra del diagrama y usa el zoom para que quepa.
8. Captura el diagrama.

Si falla la conexión:

| Mensaje | Causa y solución |
|---------|------------------|
| *Communications link failure* / *Connection refused* | Los contenedores están abajo (`docker compose up -d`) o el puerto quedó mal escrito (debe ser **3306**) |
| *Access denied for user* | Usuario o clave incorrectos: usa `coleccion` / `coleccion` |
| *Public Key Retrieval is not allowed* | Falta `allowPublicKeyRetrieval=true` en las propiedades del driver |
| *Unknown database* | En *Database* escribe `coleccion_laminas`, con guion bajo |

---

## 3. Capturas en Postman (12 a 41): en orden y una sola vez

### 3.1 Cómo ejecutar cada request

1. En *Collections*, abre **Colección de Láminas API** y la carpeta que corresponda.
2. Haz clic en la request, por ejemplo **12 Crear álbum (201)**. Se abre en una pestaña con el método, la URL y las pestañas *Params*, *Headers* y *Body*.
3. Presiona **Send**, o `Ctrl + Enter`.
4. En el panel de abajo aparecen:
   - el **Status**, por ejemplo `201 Created`;
   - el **Body** de la respuesta, en modo *Pretty*;
   - la pestaña **Test Results**, donde el test debe aparecer como **PASS**.
5. Revisa que el código coincida con la columna *Esperado* y recién ahí captura. Deben verse el método, la URL, el Status y el Body.
6. **No saltes ninguna ni repitas una**: cada request guarda ids en las variables de la colección (`albumId`, `laminaPruebaId`, `lamina2Id`, `lamina3Id`, `lamina4Id`) y las siguientes los usan. Si una da un código distinto al esperado, vuelve al paso 1.4 (base limpia) y empieza de nuevo desde la 12.

Las requests que llevan el encabezado `X-Usuario` ya lo traen puesto, en la pestaña *Headers*. Las de fotos (33, 35 y 36) ya traen el archivo en *Body → form-data*, campo `archivo`.

### 3.2 Lista de requests y capturas

| Req. | Carpeta | Archivo a guardar | Esperado | Qué debe verse |
|------|---------|-------------------|:--------:|----------------|
| 12 | 1. Álbumes | `12-album-crear-201.png` | 201 | Álbum "Qatar 2022" con `id` y `auditoria.creadoPor = "equipo"` |
| 13 | 1. Álbumes | `13-album-validacion-400.png` | 400 | `errores` con los campos `nombre`, `imagen`, `fechaLanzamiento`, `totalLaminas` |
| 14 | 1. Álbumes | `14-album-duplicado-409.png` | 409 | Mensaje de nombre duplicado |
| 15 | 1. Álbumes | `15-album-listar-paginado.png` | 200 | `content` con los álbumes y el bloque `page` |
| 16 | 1. Álbumes | `16-album-obtener-404.png` | 404 | ProblemDetail "Recurso no encontrado" |
| 17 | 1. Álbumes | `17-album-actualizar-200.png` | 200 | `auditoria.modificadoPor = "bruno"` (nombre del usuario de prueba) |
| 18 | 2. Láminas | `18-lamina-crear-201.png` | 201 | Lámina con `estado = "FALTANTE"` |
| 19 | 2. Láminas | `19-lamina-fuera-rango-409.png` | 409 | Número que excede el total del álbum |
| 20 | 2. Láminas | `20-lamina-listar-filtro.png` | 200 | Solo láminas de tipo `NORMAL` |
| 21 | 2. Láminas | `21-lamina-actualizar-200.png` | 200 | Lámina actualizada a tipo `ESPECIAL` |
| 22 | 2. Láminas | `22-lamina-eliminar-204.png` | 204 | Sin cuerpo (No Content) |
| 23 | 3. Colección | `23-lote-201.png` | 201 | Lista de 10 láminas creadas |
| 24 | 3. Colección | `24-lote-conflictos-409.png` | 409 | `errores` con `laminas[0].numero` y `laminas[1].numero` |
| 25 | 3. Colección | `25-lote-repetidos-400.png` | 400 | "El lote contiene números repetidos: 11" |
| 26 | 3. Colección | `26-registrar-200.png` | 200 | `copiasRegistradas = 6` y cantidades 1, 2 y 3 |
| 27 | 3. Colección | `27-registrar-no-catalogado-409.png` | 409 | Número 99 no catalogado |
| 28 | 3. Colección | `28-cantidad-patch-200.png` | 200 | `cantidad = 2` y `repetidas = 1` |
| 29 | 3. Colección | `29-cantidad-negativa-409.png` | 409 | "La cantidad no puede quedar negativa" |
| 30 | 3. Colección | `30-faltantes.png` | 200 | Láminas 4 a 10, `numerosNoCatalogados` 11 a 20, `totalFaltantes = 17` |
| 31 | 3. Colección | `31-repetidas.png` | 200 | Láminas 2 y 3 con `repetidas = 1` |
| 32 | 3. Colección | `32-resumen.png` | 200 | 10 catalogadas, 3 obtenidas, 17 faltantes, 15 % |
| 33 | 4. Fotos | `33-foto-subir-200.png` | 200 | `fotoUrl` en la respuesta |
| 34 | 4. Fotos | `34-foto-ver-200.png` | 200 | Postman muestra la imagen (`image/png`) |
| 35 | 4. Fotos | `35-foto-falsa-415.png` | 415 | Archivo que no es imagen, aunque termine en `.png` |
| 36 | 4. Fotos | `36-foto-grande-413.png` | 413 | Archivo mayor a 5 MB |
| 37 | 4. Fotos | `37-foto-eliminar-204.png` | 204 | Sin cuerpo |
| 38 | 5. Auditoría | `38-auditoria-header.png` | 200 | **Pestaña *Headers* de la request con `X-Usuario: ana`** + respuesta con `creadoPor = "equipo"` y `modificadoPor = "ana"` |
| 39 | 5. Auditoría | `39-historial-lamina.png` | 200 | CREACION (equipo) → MODIFICACION (bruno) → ELIMINACION (carla) |
| 40 | 5. Auditoría | `40-album-eliminar-204.png` | 204 | Sin cuerpo; en *Headers*, `X-Usuario: diego` |
| 41 | 5. Auditoría | `41-historial-album.png` | 200 | CREACION → MODIFICACION → MODIFICACION → ELIMINACION (diego) |

### 3.3 Si quieres comprobar la colección antes (opcional)

Ejecuta toda la colección en una terminal. Hazlo **antes** del paso 1.4 o después de terminar las capturas, nunca en medio:

```bash
cd ~/Escritorio/EXAMEN-TRANSVERSAL
npx newman run docs/api/postman/coleccion-laminas.postman_collection.json --working-dir docs/api/postman
```

Al final debe mostrar `requests 30 / 0 failed` y `assertions 31 / 0 failed`. Deja datos en la base, así que después repite el paso 1.4.

---

## 4. Comprobar que no falte ninguna captura

```bash
cd ~/Escritorio/EXAMEN-TRANSVERSAL/docs/evidencias
for f in 01-initializr 02-pom-dependencias 03-application-properties 04-estructura-proyecto 05-docker-compose-ps \
  06-arranque-api 07-carpeta-migraciones 08-flyway-schema-history 09-tablas-mysql 10-diagrama-er 11-swagger-ui \
  12-album-crear-201 13-album-validacion-400 14-album-duplicado-409 15-album-listar-paginado 16-album-obtener-404 \
  17-album-actualizar-200 18-lamina-crear-201 19-lamina-fuera-rango-409 20-lamina-listar-filtro 21-lamina-actualizar-200 \
  22-lamina-eliminar-204 23-lote-201 24-lote-conflictos-409 25-lote-repetidos-400 26-registrar-200 \
  27-registrar-no-catalogado-409 28-cantidad-patch-200 29-cantidad-negativa-409 30-faltantes 31-repetidas 32-resumen \
  33-foto-subir-200 34-foto-ver-200 35-foto-falsa-415 36-foto-grande-413 37-foto-eliminar-204 38-auditoria-header \
  39-historial-lamina 40-album-eliminar-204 41-historial-album 42-revinfo-lamina-aud 43-validador-numeros-unicos \
  44-script-e2e 45-mvnw-verify 46-newman 47-resumen-suites; do [ -f "$f.png" ] || echo "FALTA $f.png"; done; echo "revision terminada"
```

Si solo imprime `revision terminada`, están las 47 (así se verificó el 14-09). El comando comprueba que el archivo exista, no que muestre lo correcto: si repites alguna, ábrela y revísala antes de regenerar el informe.

---

## 5. Armar el informe con las capturas

### Opción A (recomendada): regenerarlo automáticamente

El script arma el PDF completo con todas tus capturas en su lugar, el formato exigido y el índice con números de página. Las cifras las toma de `docs/evidencias/*.txt`.

```bash
sudo apt install python3-docx      # una sola vez (Pillow y LibreOffice ya están instalados)
cd ~/Escritorio/EXAMEN-TRANSVERSAL
python3 docs/informe/generar_informe.py
```

Al terminar muestra el número de páginas (debe estar entre 5 y 15) y, si falta alguna captura, la lista `CAPTURAS PENDIENTES`. El PDF queda en `docs/informe/EXT_2_LUCENA_GOMEZ_VALERIA_OTTON.pdf`.

Para que salga en **Arial** real y no en Liberation Sans, instala antes la fuente con `sudo apt install ttf-mscorefonts-installer` y acepta la licencia.

### Opción B: editarlo a mano (Claude web, Writer, Docs)

Mantén esta correspondencia entre figuras y capturas, porque la Tabla 2 del informe cita las figuras por número:

- **Cuerpo:** Fig. 1 → 01 · Fig. 2 → 02 · Fig. 3 → 03 · Fig. 4 → 05 · Fig. 5 → 10 · Fig. 6 → 11 · Fig. 7 → 08 · Fig. 8 → 38 · Fig. 9 → 41 · Fig. 10 → 42 · Fig. 11 → 13 · Fig. 12 → 23 · Fig. 13 → 24 · Fig. 14 → 26 · Fig. 15 → 30 · Fig. 16 → 31 · Fig. 17 → 32 · Fig. 18 → 33 · Fig. 19 → 35 · Fig. 20 → 47
- **Anexo A.1 (colección Postman):** Fig. 21 a 39 → 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 25, 27, 28, 29, 34, 36, 37, 39, 40 (en ese orden)
- **Anexo A.2:** Fig. 40 → 04 · Fig. 41 → 07 · Fig. 42 → 43 · Fig. 43 → 09 · Fig. 44 → 46 · Fig. 45 → 06 · Fig. 46 → 44 · Fig. 47 → 45

No cambies las cifras del texto: todas vienen de comandos ejecutados (11 tests, 110 comprobaciones e2e, 30 requests y 31 assertions de Newman, 21 operaciones, 5 migraciones). Donde el texto hable de la colección de pruebas, debe decir **Postman**.

---

## 6. Referencia: lista completa de capturas

★ = va en el cuerpo del informe. El resto va al **Anexo A**, de modo que cada prueba 12–41 tiene su captura en el PDF. Todas quedan también en `docs/evidencias/` (repo y ZIP).

### 6.1 Configuración del proyecto (indicador 1 · 20 pts)

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★01 | `01-initializr.png` | Maven, Java 21, Boot 4.1.1, `cl.ipss`, `coleccion-laminas`, `cl.ipss.coleccion`, Jar, Properties y las 6 dependencias | Paso 2.1 |
| ★02 | `02-pom-dependencias.png` | Bloque `<dependencies>` del `pom.xml` | VS Code → `backend/pom.xml` |
| ★03 | `03-application-properties.png` | Datasource con variables de entorno, `ddl-auto=validate`, Flyway, Envers | VS Code → `application.properties` |
| 04 | `04-estructura-proyecto.png` | Paquetes `audit` … `validation` | Paso 2.2 |
| ★05 | `05-docker-compose-ps.png` | `coleccion-mysql (healthy)` y `coleccion-api Up` | Generada por comando |
| 06 | `06-arranque-api.png` | Flyway aplicando V1..V5 + `Started ColeccionLaminasApplication` | Generada por comando |

### 6.2 Base de datos y migraciones (indicadores 2 y 4)

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| 07 | `07-carpeta-migraciones.png` | `db/migration` con V1…V5 | VS Code |
| ★08 | `08-flyway-schema-history.png` | Las 5 migraciones con `success = 1` | Generada por comando |
| 09 | `09-tablas-mysql.png` | `album`, `lamina`, `album_aud`, `lamina_aud`, `revinfo`, `flyway_schema_history` | Generada por comando |
| ★10 | `10-diagrama-er.png` | album 1—N lamina; tablas `_aud` → `revinfo` | Paso 2.4 (DBeaver, puerto 3306) |

### 6.3 Documentación de la API

| # | Archivo | Qué debe verse | Cómo obtenerla |
|---|---------|----------------|----------------|
| ★11 | `11-swagger-ui.png` | Swagger UI con los 5 grupos (1. Álbumes … 5. Auditoría) | Navegador → http://localhost:8080/swagger-ui.html |

### 6.4 Pruebas de endpoints (colección Postman, 12 a 41)

La lista está en el paso 3.2. Van en el cuerpo (★): 13, 23, 24, 26, 30, 31, 32, 33, 35, 38 y 41. Las demás van al Anexo A.

### 6.5 Auditoría y pruebas automáticas (generadas por comando)

| # | Archivo | Qué muestra |
|---|---------|-------------|
| ★42 | `42-revinfo-lamina-aud.png` | Revisión, fecha, usuario y `revtype` de cada cambio (la revisión 37, de `diego`, elimina las láminas del álbum) |
| 43 | `43-validador-numeros-unicos.png` | Código de `NumerosUnicosValidator` (captura tuya, paso 2.3) |
| 44 | `44-script-e2e.png` | Final de `bash pruebas/e2e.sh`: `RESULTADO: 110 OK, 0 FAIL` |
| 45 | `45-mvnw-verify.png` | `Tests run: 11, Failures: 0` y `BUILD SUCCESS` |
| 46 | `46-newman.png` | Newman (colección Postman): 30 requests y 31 assertions, 0 fallos |
| ★47 | `47-resumen-suites.png` | Totales de las tres suites en una sola imagen |

Cada captura generada por comando tiene al lado su `.txt` con la salida literal. Esa es la fuente de las cifras del informe.

---

## 7. Referencia: formato y contenido del informe

### 7.1 Formato (obligatorio)

| Ajuste | Valor |
|--------|-------|
| Formato de entrega | **PDF** (el profesor pidió no enviar Word) |
| Fuente | Arial 12 en el cuerpo (títulos Arial 14 y 12 negrita; tablas y pies de figura a 10) |
| Interlineado | 1,15 |
| Alineación | Justificada |
| Márgenes | 2,5 cm |
| Números de página | Pie de página; la portada sin número |
| Extensión | 5 a 15 páginas, **incluidos la portada y el Anexo A** (el enunciado dice 5 a 10; el grupo recibió el 13-09 la aclaración de que se acepta anexo hasta 15) |
| Portada | Logo IPSS, título, asignatura, profesor Boris Marcelo Belmar Muñoz, integrantes Valeria Gómez y Otton Lucena, Grupo 2, fecha |

### 7.2 Estructura

| Sección | Contenido | Figuras |
|---------|-----------|---------|
| Portada e Índice | — | — |
| 1. Introducción | Problema, objetivo, alcance (API REST sin frontend) y **justificación de la tecnología** | — |
| 2.1 Configuración | Initializr, dependencias, properties con variables de entorno, Docker Compose | 01, 02, 03, 05 |
| 2.2 Arquitectura y modelo | Capas, DTOs, interfaz de usuario = Swagger UI + Postman, Album 1:N Lamina, regla de `cantidad` | 10, 11 |
| 2.3 API y funcionalidades especiales | Tabla de endpoints, lote todo o nada, registrar, faltantes, repetidas, resumen, fotos multipart | — |
| 2.4 Validaciones y auditoría | Bean Validation, `@NumerosUnicos`, reglas 409, RFC 9457; Flyway, JPA Auditing, Envers | 08, 38, 41, 42 |
| 3. Pruebas | Tres niveles, Tabla 2 (una fila por request 12–41) y capturas ★ | 13, 23, 24, 26, 30–33, 35, 47 |
| 4. Conclusión y 5. Bibliografía | Logros, dificultades resueltas, mejoras; APA 7 | — |
| Anexo A | Una captura por cada prueba restante + proyecto, BD y suites | 04, 06, 07, 09, 12, 14–22, 25, 27–29, 34, 36, 37, 39, 40, 43–46 |

---

## 8. Entrega

1. `ENLACE_GITHUB.txt` ya está creado en la raíz, con la URL del repositorio público.
2. Informe: `docs/informe/EXT_2_LUCENA_GOMEZ_VALERIA_OTTON.pdf`.
3. ZIP `EXT_2_LUCENA_GOMEZ_VALERIA_OTTON.zip` con el proyecto (sin `target/`, `uploads/`, `.env`, `grande.png` ni material del curso), el PDF, `ENLACE_GITHUB.txt` y `docs/evidencias/`.
4. Revisado antes de subir (verificación del 14-09-2026):
   - [x] Están las 47 capturas (sección 4) y cada una muestra lo que dice su nombre
   - [x] El PDF abre y tiene 14 páginas (el máximo es 15, con el Anexo A incluido)
   - [x] Arial 12, interlineado 1,15, justificado, páginas numeradas (`pdffonts` solo lista ArialMT, Arial-BoldMT y Arial-ItalicMT)
   - [x] Portada con logo, profesor e integrantes
   - [x] El ZIP descomprime y `docker compose up -d --build` funciona en limpio; adentro no va material del curso (solo el PDF del informe, sin el enunciado ni la rúbrica)
   - [x] Las tres suites pasan: `./mvnw verify` 11/0, `pruebas/e2e.sh` 110/0 y Newman 30 requests / 31 assertions sobre base limpia
   - [x] Commit y push hechos (`git status` limpio, `main` al día con `origin/main`)
5. **Único paso que queda:** súbelo a la plataforma **antes del 17-09-2026**.

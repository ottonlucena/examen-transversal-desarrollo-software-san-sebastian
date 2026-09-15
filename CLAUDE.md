# CLAUDE.md

Las reglas del proyecto viven en `AGENTS.md` (fuente única, compartida con otros agentes) y el análisis y el plan, en `BRIEF.md`. Se importan aquí para que Claude Code las cargue siempre:

@AGENTS.md
@BRIEF.md

---

## Instrucciones específicas para Claude Code

### Idioma y comunicación
- Responder **en español**. Código con identificadores en español según `AGENTS.md §12`.
- Al terminar una tarea, reportar qué se hizo, cómo se verificó (comando y resultado) y qué falta.
- Si algo contradice la rúbrica o las indicaciones del profesor, avisar antes de implementarlo.

### Flujo de trabajo
1. Trabajar **por fases** según `BRIEF.md §6`, una fase por sesión cuando sea posible, y marcar el checklist de `BRIEF.md §5` al completar cada ítem.
2. Antes de escribir código que use una API de Spring Boot 4, JPA, Envers, Flyway o springdoc, **consultar Context7** (`npx ctx7@latest docs <id> "<consulta>"`) con los IDs de `AGENTS.md §15`. Máximo 3 consultas por pregunta.
3. Si Context7 no resuelve la duda (versión más reciente, compatibilidad, un error concreto), usar **Tavily** (`tavily_search`), priorizando dominios oficiales.
4. Después de cada cambio en `backend/`, ejecutar `cd backend && ./mvnw verify`. Si se tocó el esquema o la configuración, levantar también `docker compose up -d mysql` y arrancar la app para confirmar que Flyway y `validate` pasan.
5. Si falla un test o el arranque, reportarlo con la salida real; no marcar la tarea como terminada.

### Límites
- **No hacer commit, push ni crear el repositorio remoto** sin pedido explícito. Repositorio git local en `main`, con remoto `origin` → https://github.com/ottonlucena/examen-transversal-desarrollo-software-san-sebastian.git
- **No ejecutar `docker compose down -v`** ni borrar `uploads/` sin confirmación: elimina datos y evidencias.
- No agregar dependencias, endpoints o entidades que no estén en `BRIEF.md` sin proponerlo primero.
- No generar el informe final sin que el grupo entregue los nombres de los integrantes y el número de grupo.

### Entorno local verificado (10-09-2026)
- Java 21.0.6 (Oracle) · Maven 3.8.7 (usar siempre `./mvnw`) · Docker 27.1.1 · Docker Compose v2.29.1 · Git 2.43.
- No hay cliente `mysql` instalado en el host: usar `docker compose exec mysql mysql ...`.

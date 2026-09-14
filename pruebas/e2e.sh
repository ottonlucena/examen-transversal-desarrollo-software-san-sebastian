#!/usr/bin/env bash
# Pruebas end-to-end de la API: CRUD, funcionalidades especiales, fotos y auditoria.
# Requisitos: API y MySQL levantados (docker compose up -d --build), curl y python3.
# Uso (desde la raiz del repo): bash pruebas/e2e.sh      Otra URL: B=http://host:8080 bash pruebas/e2e.sh
# Cada prueba crea sus propios datos (nombres unicos) y los elimina al final.
set -u
cd "$(dirname "$0")/.."
B=${B:-http://localhost:8080}
T=$(mktemp -d)
trap 'rm -rf "$T"' EXIT
OK=0
FAIL=0

# ---------- utilidades ----------
reporta() { # codigo esperado descripcion metodo ruta [usuario]
  local r=OK
  if [ "$1" = "$2" ]; then OK=$((OK + 1)); else FAIL=$((FAIL + 1)); r=FAIL; fi
  printf '[%s] %-58s %s %s -> %s (esperado %s)%s\n' "$r" "$3" "$4" "$5" "$1" "$2" "${6:+  [X-Usuario: $6]}"
  local ct
  ct=$(grep -i '^content-type:' "$T/h" | tr -d '\r' | cut -d' ' -f2-)
  if [[ "$ct" == image/* ]]; then
    echo "       Content-Type: $ct, $(stat -c%s "$T/b") bytes"
  elif [ -s "$T/b" ]; then
    echo "       $(head -c 400 "$T/b")"
  fi
}
req() { # metodo ruta esperado descripcion [json] [usuario]
  local args=(-s -D "$T/h" -o "$T/b" -w '%{http_code}' -X "$1" "$B$2")
  [ -n "${5:-}" ] && args+=(-H 'Content-Type: application/json' -d "$5")
  [ -n "${6:-}" ] && args+=(-H "X-Usuario: $6")
  reporta "$(curl "${args[@]}")" "$3" "$4" "$1" "$2" "${6:-}"
}
subir() { # ruta esperado descripcion [argumentos curl...]
  local p=$1 esp=$2 desc=$3
  shift 3
  reporta "$(curl -s -D "$T/h" -o "$T/b" -w '%{http_code}' -X POST "$B$p" "$@")" "$esp" "$desc" POST "$p"
}
py() { python3 -c "import json;d=json.load(open('$T/b'));print($1)"; }
igual() { # obtenido esperado descripcion
  if [ "$1" = "$2" ]; then OK=$((OK + 1)); echo "   [OK] $3: $1"; else FAIL=$((FAIL + 1)); echo "   [FAIL] $3: $1 (esperado $2)"; fi
}
chk() { igual "$(py "$1")" "$2" "$3"; }
fotos() { docker compose exec -T api sh -c 'ls /app/uploads | wc -l' | tr -d ' \r'; }
sql() { docker compose exec -T mysql sh -c "mysql -u\"\$MYSQL_USER\" -p\"\$MYSQL_PASSWORD\" \"\$MYSQL_DATABASE\" -e \"$1\"" 2>&1 | grep -v "Using a password"; }

python3 - "$T" <<'PY'
import sys, zlib, struct, os
d = sys.argv[1]
def chunk(t, data): return struct.pack('>I', len(data)) + t + data + struct.pack('>I', zlib.crc32(t + data) & 0xffffffff)
png = b'\x89PNG\r\n\x1a\n' + chunk(b'IHDR', struct.pack('>IIBBBBB', 1, 1, 8, 2, 0, 0, 0)) \
      + chunk(b'IDAT', zlib.compress(b'\x00\xff\x00\x00')) + chunk(b'IEND', b'')
open(f'{d}/lamina.png', 'wb').write(png)
open(f'{d}/lamina.jpg', 'wb').write(b'\xff\xd8\xff\xe0' + b'\x00\x10JFIF\x00' + os.urandom(64) + b'\xff\xd9')
open(f'{d}/falsa.png', 'wb').write(b'esto es texto, no una imagen')
open(f'{d}/vacia.png', 'wb').write(b'')
open(f'{d}/grande.png', 'wb').write(png[:8] + os.urandom(6 * 1024 * 1024))
PY

# =====================================================================
echo "===== 1. ALBUMES (CRUD) ====="
N="Album e2e $RANDOM$RANDOM"
req POST /api/albumes 201 "Crear album valido" "{\"nombre\":\"$N\",\"imagen\":\"https://ejemplo.cl/portada.png\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":10,\"editorial\":\"Panini\"}"
A=$(py 'd["id"]')
req GET /api/albumes/$A 200 "Obtener album creado"
igual "$(py 'd["nombre"]')" "$N" "nombre del album obtenido"
req POST /api/albumes 409 "Nombre duplicado (distinta mayuscula)" "{\"nombre\":\"${N^^}\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":10}"
req POST /api/albumes 400 "Validacion: nombre vacio, total 0, fecha futura, url" '{"nombre":" ","imagen":"no-es-url","fechaLanzamiento":"2999-01-01","tipoLaminas":"ADHESIVA","totalLaminas":0}'
req POST /api/albumes 400 "Enum inexistente en tipoLaminas" '{"nombre":"X","fechaLanzamiento":"2026-01-01","tipoLaminas":"PLASTICO","totalLaminas":5}'
req POST /api/albumes 400 "JSON mal formado" '{"nombre":'
req GET "/api/albumes?size=2&sort=nombre,desc" 200 "Listar paginado"
req GET "/api/albumes?sort=noExiste" 400 "Orden por propiedad inexistente"
req GET /api/albumes/999999 404 "Album inexistente"
req GET /api/albumes/abc 400 "Id con tipo invalido"
req PUT /api/albumes/$A 200 "Actualizar album" "{\"nombre\":\"$N\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":12,\"descripcion\":\"Edicion actualizada\"}"

echo "===== 2. LAMINAS (CRUD) ====="
req POST /api/albumes/$A/laminas 201 "Crear lamina 1 (cantidad por defecto 0)" '{"numero":1,"nombre":"Escudo Chile","tipo":"ESCUDO"}'
L1=$(py 'd["id"]')
req POST /api/albumes/$A/laminas 201 "Crear lamina 5 con 3 copias (repetida)" '{"numero":5,"nombre":"Alexis Sanchez","tipo":"BRILLANTE","cantidad":3}'
L5=$(py 'd["id"]')
req POST /api/albumes/$A/laminas 409 "Numero duplicado en el album" '{"numero":1,"nombre":"Otra","tipo":"NORMAL"}'
req POST /api/albumes/$A/laminas 409 "Numero mayor al total del album" '{"numero":13,"nombre":"Fuera","tipo":"NORMAL"}'
req POST /api/albumes/$A/laminas 400 "Validacion: numero 0, cantidad negativa" '{"numero":0,"nombre":"","tipo":"NORMAL","cantidad":-1}'
req POST /api/albumes/999999/laminas 404 "Crear lamina en album inexistente" '{"numero":1,"nombre":"X","tipo":"NORMAL"}'
req GET /api/albumes/$A/laminas 200 "Listar laminas del album"
req GET "/api/albumes/$A/laminas?tipo=BRILLANTE" 200 "Filtrar por tipo"
req GET "/api/albumes/$A/laminas?tipo=XYZ" 400 "Filtro con tipo invalido"
req GET /api/albumes/999999/laminas 404 "Listar laminas de album inexistente"
req GET /api/laminas/$L5 200 "Obtener lamina"
req PUT /api/laminas/$L1 200 "Actualizar lamina (sin cantidad = conserva)" '{"numero":2,"nombre":"Escudo Chile dorado","tipo":"ESPECIAL"}'
req PUT /api/laminas/$L1 409 "Actualizar a numero ya usado" '{"numero":5,"nombre":"X","tipo":"NORMAL"}'
req PUT /api/albumes/$A 409 "Reducir total bajo la lamina mayor (5)" "{\"nombre\":\"$N\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":3}"
req DELETE /api/laminas/$L1 204 "Eliminar lamina"
req GET /api/laminas/$L1 404 "Lamina eliminada ya no existe"
req DELETE /api/albumes/$A 204 "Eliminar album (cascada)"
req GET /api/laminas/$L5 404 "Lamina del album eliminado"
req DELETE /api/albumes/$A 404 "Eliminar album ya eliminado"

# =====================================================================
echo "===== 3. FUNCIONALIDADES ESPECIALES ====="
N="Album coleccion e2e $RANDOM$RANDOM"
req POST /api/albumes 201 "Setup: album de 10 laminas" "{\"nombre\":\"$N\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":10}"
A=$(py 'd["id"]')
LOTE='{"laminas":['
for i in 1 2 3 4 5 6 7 8; do LOTE+="{\"numero\":$i,\"nombre\":\"Jugador $i\",\"tipo\":\"NORMAL\"},"; done
LOTE="${LOTE%,}]}"
req POST /api/albumes/$A/laminas/lote 201 "Lote de 8 laminas (1..8)" "$LOTE"
chk 'len(d)' 8 "laminas creadas"
read -r L1 L2 L3 L4 L5 _ <<< "$(py '" ".join(str(x["id"]) for x in d)')"
req POST /api/albumes/$A/laminas/lote 400 "Numeros repetidos dentro del lote (@NumerosUnicos)" '{"laminas":[{"numero":9,"nombre":"A","tipo":"NORMAL"},{"numero":9,"nombre":"B","tipo":"NORMAL"}]}'
req POST /api/albumes/$A/laminas/lote 409 "Lote con conflictos: 8 existe, 11 fuera de rango" '{"laminas":[{"numero":8,"nombre":"A","tipo":"NORMAL"},{"numero":11,"nombre":"B","tipo":"NORMAL"},{"numero":9,"nombre":"C","tipo":"NORMAL"}]}'
req POST /api/albumes/$A/laminas/lote 400 "Lote vacio" '{"laminas":[]}'
req POST /api/albumes/$A/laminas/lote 400 "Lote con item invalido (numero 0)" '{"laminas":[{"numero":0,"nombre":"","tipo":"NORMAL"}]}'
req POST /api/albumes/999999/laminas/lote 404 "Lote en album inexistente" '{"laminas":[{"numero":1,"nombre":"A","tipo":"NORMAL"}]}'

req POST /api/albumes/$A/laminas/registrar 200 "Registrar [1,2,2,3,3,3]" '{"numeros":[1,2,2,3,3,3]}'
chk 'd["copiasRegistradas"]' 6 "copias registradas"
chk '[(x["numero"],x["cantidad"],x["estado"]) for x in d["laminas"]]' "[(1, 1, 'OBTENIDA'), (2, 2, 'REPETIDA'), (3, 3, 'REPETIDA')]" "cantidades"
req POST /api/albumes/$A/laminas/registrar 409 "Registrar numero no catalogado (todo o nada)" '{"numeros":[1,99]}'
req GET /api/laminas/$L1 200 "Lamina 1 no cambio tras el rechazo"
chk 'd["cantidad"]' 1 "cantidad de lamina 1"
req POST /api/albumes/$A/laminas/registrar 400 "Registrar lista vacia" '{"numeros":[]}'
req POST /api/albumes/$A/laminas/registrar 400 "Registrar numero 0" '{"numeros":[0]}'

req PATCH /api/laminas/$L3/cantidad 200 "Restar 1 copia a la lamina 3" '{"delta":-1}'
chk '(d["cantidad"],d["repetidas"])' "(2, 1)" "cantidad y repetidas"
req PATCH /api/laminas/$L4/cantidad 409 "Restar a lamina sin copias (quedaria negativa)" '{"delta":-1}'
req PATCH /api/laminas/$L4/cantidad 400 "Sin delta" '{}'
req PATCH /api/laminas/999999/cantidad 404 "Lamina inexistente" '{"delta":1}'

req GET /api/albumes/$A/laminas/faltantes 200 "Faltantes"
chk '([x["numero"] for x in d["laminas"]], d["numerosNoCatalogados"], d["totalFaltantes"])' "([4, 5, 6, 7, 8], [9, 10], 7)" "faltantes (sin copias, no catalogadas, total)"
req GET /api/albumes/$A/laminas/repetidas 200 "Repetidas"
chk '([(x["numero"],x["repetidas"]) for x in d["laminas"]], d["laminasRepetidas"], d["totalCopiasRepetidas"])' "([(2, 1), (3, 1)], 2, 2)" "repetidas por lamina y totales"
req GET /api/albumes/$A/resumen 200 "Resumen"
chk '(d["catalogadas"],d["obtenidas"],d["faltantes"],d["laminasRepetidas"],d["copiasRepetidas"],d["porcentajeCompletado"])' "(8, 3, 7, 2, 2, 30.0)" "totales del resumen"
req POST /api/albumes 201 "Setup: album vacio" "{\"nombre\":\"$N vacio\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"TARJETA\",\"totalLaminas\":5}"
V=$(py 'd["id"]')
req GET /api/albumes/$V/resumen 200 "Resumen de album sin laminas"
chk '(d["catalogadas"],d["obtenidas"],d["faltantes"],d["porcentajeCompletado"])' "(0, 0, 5, 0.0)" "totales en cero"
req GET /api/albumes/999999/laminas/faltantes 404 "Faltantes de album inexistente"
req GET /api/albumes/999999/laminas/repetidas 404 "Repetidas de album inexistente"
req GET /api/albumes/999999/resumen 404 "Resumen de album inexistente"

echo "===== 4. FOTOS ====="
F0=$(fotos)
subir /api/laminas/$L2/foto 200 "Subir foto PNG" -F "archivo=@$T/lamina.png"
chk 'd["fotoUrl"]' "/api/laminas/$L2/foto" "fotoUrl"
req GET /api/laminas/$L2/foto 200 "Descargar foto"
cmp -s "$T/b" "$T/lamina.png" && igual si si "contenido descargado identico al subido" || igual no si "contenido descargado identico al subido"
subir /api/laminas/$L2/foto 200 "Reemplazar por JPEG" -F "archivo=@$T/lamina.jpg"
req GET /api/laminas/$L2/foto 200 "Descargar foto reemplazada"
igual "$(fotos)" "$((F0 + 1))" "archivos en el volumen (la foto anterior se borro)"
F1=$(fotos)
subir /api/laminas/$L3/foto 415 "Archivo de texto con extension .png" -F "archivo=@$T/falsa.png;type=image/png"
subir /api/laminas/$L3/foto 415 "Archivo vacio" -F "archivo=@$T/vacia.png"
subir /api/laminas/$L3/foto 413 "Archivo de 6 MB (maximo 5 MB)" -F "archivo=@$T/grande.png"
subir /api/laminas/$L3/foto 400 "Multipart sin el campo 'archivo'" -F "otro=@$T/lamina.png"
subir /api/laminas/$L3/foto 415 "Enviar JSON en vez de multipart" -H 'Content-Type: application/json' -d '{}'
subir /api/laminas/999999/foto 404 "Foto a lamina inexistente" -F "archivo=@$T/lamina.png"
igual "$(fotos)" "$F1" "los rechazos no dejaron archivos huerfanos"
req GET /api/laminas/$L3/foto 404 "Ver foto de lamina sin foto"
req DELETE /api/laminas/$L2/foto 204 "Quitar foto"
req GET /api/laminas/$L2/foto 404 "La foto ya no existe"
req DELETE /api/laminas/$L2/foto 404 "Quitar foto inexistente"
subir /api/laminas/$L5/foto 200 "Subir foto antes de borrar el album" -F "archivo=@$T/lamina.png"
req DELETE /api/albumes/$A 204 "Eliminar album con foto"
sleep 1
igual "$(fotos)" "$F0" "fotos del album borradas del disco"
req DELETE /api/albumes/$V 204 "Limpieza: album vacio"

# =====================================================================
echo "===== 5. AUDITORIA (JPA Auditing + Envers + Flyway) ====="
N="Album auditoria e2e $RANDOM$RANDOM"
req POST /api/albumes 201 "Crear album como 'ana'" "{\"nombre\":\"$N\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":10}" ana
A=$(py 'd["id"]')
chk '(d["auditoria"]["creadoPor"], d["auditoria"]["modificadoPor"])' "('ana', 'ana')" "creadoPor/modificadoPor"
CREADO=$(py 'd["auditoria"]["creadoEn"]')
sleep 1
req PUT /api/albumes/$A 200 "Modificar album como 'bruno'" "{\"nombre\":\"$N\",\"fechaLanzamiento\":\"2026-05-01\",\"tipoLaminas\":\"ADHESIVA\",\"totalLaminas\":12}" bruno
chk '(d["auditoria"]["creadoPor"], d["auditoria"]["modificadoPor"], d["auditoria"]["creadoEn"] == "'"$CREADO"'", d["auditoria"]["modificadoEn"] > d["auditoria"]["creadoEn"])' "('ana', 'bruno', True, True)" "creacion intacta, modificacion actualizada"
req POST /api/albumes/$A/laminas 201 "Crear lamina sin header X-Usuario" '{"numero":1,"nombre":"Escudo","tipo":"ESCUDO"}'
L=$(py 'd["id"]')
chk 'd["auditoria"]["creadoPor"]' sistema "usuario por defecto"
req POST /api/albumes/$A/laminas 201 "Crear lamina con header invalido" '{"numero":2,"nombre":"Otra","tipo":"NORMAL"}' "x y;DROP TABLE"
chk 'd["auditoria"]["creadoPor"]' sistema "header invalido se ignora"
req PATCH /api/laminas/$L/cantidad 200 "Sumar 2 copias como 'carla'" '{"delta":2}' carla
chk '(d["auditoria"]["creadoPor"], d["auditoria"]["modificadoPor"], d["cantidad"])' "('sistema', 'carla', 2)" "auditoria tras PATCH"
req GET /api/albumes/$A/historial 200 "Historial del album"
chk '[(x["operacion"], x["usuario"], x["datos"]["totalLaminas"]) for x in d]' "[('CREACION', 'ana', 10), ('MODIFICACION', 'bruno', 12)]" "revisiones del album"
chk '"auditoria" in d[0]["datos"]' False "snapshot sin bloque auditoria"
req GET /api/laminas/$L/historial 200 "Historial de la lamina"
chk '[(x["operacion"], x["usuario"], x["datos"]["cantidad"]) for x in d]' "[('CREACION', 'sistema', 0), ('MODIFICACION', 'carla', 2)]" "revisiones de la lamina"
req DELETE /api/albumes/$A 204 "Eliminar album como 'diego'" "" diego
req GET /api/albumes/$A 404 "El album ya no existe"
req GET /api/albumes/$A/historial 200 "Historial del album eliminado"
chk '(d[-1]["operacion"], d[-1]["usuario"], d[-1]["datos"]["nombre"] == "'"$N"'")' "('ELIMINACION', 'diego', True)" "revision de eliminacion con datos"
req GET /api/laminas/$L/historial 200 "Historial de la lamina eliminada con el album"
chk '(d[-1]["operacion"], d[-1]["usuario"], d[-1]["datos"]["albumId"])' "('ELIMINACION', 'diego', $A)" "eliminacion de la lamina registrada"
req GET /api/albumes/999999/historial 404 "Historial de album que nunca existio"
req GET /api/laminas/abc/historial 400 "Historial con id invalido"

echo "--- flyway_schema_history"
sql "SELECT installed_rank AS n, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
igual "$(sql 'SELECT COUNT(*) FROM flyway_schema_history WHERE success=1;' | tail -1)" 5 "migraciones aplicadas con exito (V1..V5)"
echo "--- revinfo + lamina_aud de la lamina $L"
sql "SELECT r.id AS rev, FROM_UNIXTIME(r.revtstmp/1000) AS fecha, r.usuario, a.revtype, a.numero, a.cantidad FROM lamina_aud a JOIN revinfo r ON r.id = a.rev WHERE a.id = $L ORDER BY r.id;"

echo
echo "===== RESULTADO: $OK OK, $FAIL FAIL ====="
[ "$FAIL" -eq 0 ]

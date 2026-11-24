#!/usr/bin/env bash
# Regresión TP02 de la Ola 2.
# Comprueba que los constructos inválidos no dejen IR del constructor y que
# las sentencias válidas vecinas conserven sus tercetos.
set -u

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAIZ="$(cd "$DIR/../../.." && pwd)"
BUILD="${TP3_BUILD:-/tmp/opencode/tp3build}"

if [[ -z "${TP3_BUILD:-}" || ! -f "$BUILD/Main.class" ]]; then
    mkdir -p "$BUILD"
    if ! javac -encoding UTF-8 -d "$BUILD" \
            "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
        echo "No se pudo compilar el proyecto."
        exit 1
    fi
fi

TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp2reg.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

fallos=0
casos=0

contar() {
    printf '%s\n' "$1" | grep -F -c -- "$2" || true
}

verificar() {
    local caso="$1" descripcion="$2" salida="$3" patron="$4" esperado="$5"
    local cantidad
    cantidad="$(contar "$salida" "$patron")"
    if [[ "$cantidad" -ne "$esperado" ]]; then
        echo "FALLA $caso"
        echo "  $descripcion: esperado $esperado, obtenido $cantidad"
        echo "  patron: $patron"
        fallos=$((fallos + 1))
    fi
}

# Cuenta solo líneas de error reales, no el eco del código fuente. Algunos
# tests repiten el texto del error en un comentario al comienzo del archivo.
verificarErrores() {
    local caso="$1" descripcion="$2" salida="$3" mensaje="$4" esperado="$5"
    local cantidad
    cantidad="$(printf '%s\n' "$salida" | grep -E -c "^Línea [0-9]+: Error: $mensaje" || true)"
    if [[ "$cantidad" -ne "$esperado" ]]; then
        echo "FALLA $caso"
        echo "  $descripcion: esperado $esperado, obtenido $cantidad"
        echo "  mensaje: $mensaje"
        fallos=$((fallos + 1))
    fi
}

ejecutar() {
    local ruta="$1" id="$2"
    cp "$ruta" "$TMP/$id.txt"
    java -cp "$BUILD" Main "$TMP/$id.txt" 2>&1
}

# --- Falta del orden: error presente, cero PAR, cero CALL, cero asignación ---
for entrada in \
    "18_falta_orden|$DIR/../generales/18_falta_orden.txt|1" \
    "sin_orden|$DIR/sin_orden.txt|2" \
    "orden_sin_corchetes|$DIR/orden_sin_corchetes.txt|2"; do
    IFS='|' read -r id ruta errores <<< "$entrada"
    casos=$((casos + 1))
    salida="$(ejecutar "$ruta" "falta_orden_$id")"
    verificar "$id" "error de orden" "$salida" \
        "Falta del orden de evaluacion y asignacion de parametros" "$errores"
    verificar "$id" "sin PAR" "$salida" "(PAR," 0
    verificar "$id" "sin CALL" "$salida" "(CALL," 0
    verificar "$id" "sin asignación desde foo" "$salida" "(:=, x:main, foo:main)" 0
    verificar "$id" "programa conservado" "$salida" "(PROGRAMA, --, --)" 1
    verificar "$id" "fin del programa conservado" "$salida" "(FIN_PROGRAMA, --, --)" 1
    verificar "$id" "declaración de foo conservada" "$salida" "(FUNCION, foo, --)" 1
    verificar "$id" "ret interno conservado" "$salida" "(ret, n:main:foo, --)" 1
done

# --- ':=' en expresión: error presente, cero tercetos del constructor ---
for entrada in \
    "17_asig_donde_igual|$DIR/../generales/17_asig_donde_igual.txt" \
    "usa_asig_sin_anidamiento|$DIR/../verificacion_r17_asig_expresiones/usa_asig_sin_anidamiento.txt"; do
    IFS='|' read -r id ruta <<< "$entrada"
    casos=$((casos + 1))
    salida="$(ejecutar "$ruta" "asig_$id")"
    verificar "$id" "error de ':=' donde debe usarse '='" "$salida" \
        "Uso de ':=' donde debe usarse '='" 1
    verificar "$id" "sin asignación interna" "$salida" '(:=, y:main, 1$ul)' 0
    verificar "$id" "sin asignación externa" "$salida" '(:=, x:main, [2])' 0
    verificar "$id" "programa conservado" "$salida" "(PROGRAMA, --, --)" 1
    verificar "$id" "fin del programa conservado" "$salida" "(FIN_PROGRAMA, --, --)" 1
done

# --- Válidos del tema 19: mismos tercetos que el baseline ---
casos=$((casos + 1))
salida="$(ejecutar "$DIR/estructura_respetada.txt" "r19_estructura")"
verificar "estructura_respetada.txt (r19)" "sin errores" "$salida" "Error:" 0
verificar "estructura_respetada.txt (r19)" "dos CALL" "$salida" "(CALL, foo:main, 2)" 2
verificar "estructura_respetada.txt (r19)" "cuatro PAR" "$salida" "(PAR," 4
verificar "estructura_respetada.txt (r19)" "asignación del primer CALL" "$salida" \
    '(:=, x:main, [8])' 1
verificar "estructura_respetada.txt (r19)" "asignación del segundo CALL" "$salida" \
    '(:=, x:main, [13])' 1

# --- Válidos del tema 17: mismos tercetos que el baseline ---
casos=$((casos + 1))
salida="$(ejecutar "$DIR/../verificacion_r17_asig_expresiones/estructura_respetada.txt" "r17_estructura")"
verificar "estructura_respetada.txt (r17)" "sin errores" "$salida" "Error:" 0
verificar "estructura_respetada.txt (r17)" "asignación interna" "$salida" \
    '(:=, y:main, 2$ul)' 1
verificar "estructura_respetada.txt (r17)" "asignación externa" "$salida" \
    '(:=, x:main, [2])' 1
verificar "estructura_respetada.txt (r17)" "asignación final" "$salida" \
    '(:=, x:main, [7])' 1
verificar "estructura_respetada.txt (r17)" "salto condicional" "$salida" \
    "(BF, [10], label_1)" 1

# --- Resolución de firma: solo la llamada válida genera CALL ---
casos=$((casos + 1))
salida="$(ejecutar "$DIR/resolucion_funcion.txt" "resolucion_funcion")"
verificar "resolucion_funcion.txt" "función inexistente" "$salida" \
    "Funcion no declarada 'ausente'" 1
verificar "resolucion_funcion.txt" "variable usada como función" "$salida" \
    "Uso incorrecto de 'x': no es una funcion" 1
verificar "resolucion_funcion.txt" "llamada válida conservada" "$salida" \
    "(CALL, f:main, 1)" 1

# --- Métodos: solo el método válido genera CALLMET ---
casos=$((casos + 1))
salida="$(ejecutar "$DIR/metodos.txt" "metodos")"
verificar "metodos.txt" "método inexistente" "$salida" \
    "Metodo no declarado o no visible 'inexistente'" 1
verificar "metodos.txt" "un solo CALLMET" "$salida" \
    "(CALLMET, objeto:main, m:main:ca)" 1
verificar "metodos.txt" "tipo incompatible" "$salida" \
    "Tipos incompatibles en el parametro real 'p' de 'm'" 1

# --- Método heredado sin orden: sin CALLMET ni asignaciones desde a1 ---
casos=$((casos + 1))
salida="$(ejecutar "$DIR/../verificacion_r28_r31_atributos/metodo_heredado_prefijado_sin_orden.txt" "metodo_sin_orden")"
verificarErrores "metodo_heredado_prefijado_sin_orden.txt" "tres errores de orden" "$salida" \
    "Falta del orden de evaluacion y asignacion de parametros" 3
verificar "metodo_heredado_prefijado_sin_orden.txt" "sin CALLMET" "$salida" "(CALLMET," 0
verificar "metodo_heredado_prefijado_sin_orden.txt" "sin asignaciones desde a1" "$salida" \
    '(:=, a1:main' 0
verificar "metodo_heredado_prefijado_sin_orden.txt" "programa conservado" "$salida" \
    "(PROGRAMA, --, --)" 1

printf 'Casos: %d\n' "$casos"
printf 'Fallos: %d\n' "$fallos"
if (( fallos > 0 )); then
    exit 1
fi

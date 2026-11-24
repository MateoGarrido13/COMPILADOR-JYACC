#!/usr/bin/env bash
# Verifica los casos de TP01 sobre identificadores divididos por un caracter
# invalido. Comprueba mensajes y, sobre todo, que no quede IR derivado de la
# primera mitad del identificador.
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

TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp1ids.XXXXXX")"
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

ejecutar() {
    local nombre="$1"
    cp "$DIR/$nombre" "$TMP/$nombre"
    java -cp "$BUILD" Main "$TMP/$nombre" 2>&1
}

casos=$((casos + 1))
salida="$(ejecutar id_invalido_derecha.txt)"
verificar "id_invalido_derecha.txt" "un error lexico sobre el ID completo" \
    "$salida" "Identificador invalido 'id#malo'" 1
verificar "id_invalido_derecha.txt" "sin mensaje con lexema vacio" \
    "$salida" "Lexema no reconocido ''" 0
verificar "id_invalido_derecha.txt" "sin asignacion desde la primera mitad" \
    "$salida" "(:=, x:main, id:main)" 0
verificar "id_invalido_derecha.txt" "sin asignacion desde la segunda mitad" \
    "$salida" "(:=, x:main, malo:main)" 0
verificar "id_invalido_derecha.txt" "sin tercetos de asignacion" \
    "$salida" "(:=," 0

casos=$((casos + 1))
salida="$(ejecutar id_invalido_izquierda.txt)"
verificar "id_invalido_izquierda.txt" "un error lexico sobre el ID completo" \
    "$salida" "Identificador invalido 'id#malo'" 1
verificar "id_invalido_izquierda.txt" "sin mensaje con lexema vacio" \
    "$salida" "Lexema no reconocido ''" 0
verificar "id_invalido_izquierda.txt" "resto del programa entregado" \
    "$salida" "(:=, x:main, 2\$ul)" 1
verificar "id_invalido_izquierda.txt" "sin asignacion desde la primera mitad" \
    "$salida" "(:=, id:main, 1\$ul)" 0
verificar "id_invalido_izquierda.txt" "sin asignacion desde la segunda mitad" \
    "$salida" "(:=, malo:main, 1\$ul)" 0

casos=$((casos + 1))
salida="$(ejecutar id_invalido_declaracion.txt)"
verificar "id_invalido_declaracion.txt" "un error lexico sobre el ID completo" \
    "$salida" "Identificador invalido 'id#malo'" 1
verificar "id_invalido_declaracion.txt" "sin mensaje con lexema vacio" \
    "$salida" "Lexema no reconocido ''" 0
verificar "id_invalido_declaracion.txt" "declaracion previa sigue disponible" \
    "$salida" "(:=, x:main, 1\$ul)" 1
verificar "id_invalido_declaracion.txt" "sin asignacion desde la primera mitad" \
    "$salida" "(:=, id:main, 1\$ul)" 0
verificar "id_invalido_declaracion.txt" "x no pierde su declaracion" \
    "$salida" "Error de ambito 'x' no es alcanzable" 0

casos=$((casos + 1))
salida="$(ejecutar id_invalido_en_cadena.txt)"
verificar "id_invalido_en_cadena.txt" "sin errores" \
    "$salida" "Error:" 0
verificar "id_invalido_en_cadena.txt" "sin error de identificador" \
    "$salida" "Identificador invalido" 0
verificar "id_invalido_en_cadena.txt" "cadena de una linea intacta" \
    "$salida" "(pout, id#malo, --)" 1

casos=$((casos + 1))
salida="$(ejecutar caracter_invalido_suelto.txt)"
verificar "caracter_invalido_suelto.txt" "caracter informado en el mensaje" \
    "$salida" "Lexema no reconocido '@'" 1
verificar "caracter_invalido_suelto.txt" "sin mensaje con lexema vacio" \
    "$salida" "Lexema no reconocido ''" 0

printf 'Casos: %d\n' "$casos"
printf 'Fallos: %d\n' "$fallos"
if (( fallos > 0 )); then
    exit 1
fi

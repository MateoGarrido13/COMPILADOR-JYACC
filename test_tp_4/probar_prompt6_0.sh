#!/usr/bin/env bash
# Script de prueba del Paquete 6.0 (compuerta de generacion del .asm).
#
# AJUSTE POST-6.0 (pedido del usuario): stdout vuelve al formato TP3, que el
# usuario quitara manualmente antes de entregar. Por eso este script ya NO
# verifica el contrato TP4 de consola; verifica solo que:
#  1. El proyecto compila.
#  2. caso valido: stdout TP3 completo y el .asm existe en la ruta esperada.
#  3. caso con error: stdout TP3 con el error y NO se genera .asm.
#  4. Regresion 4e (snapshots TP3 + prompts 4d..2).
#
# Uso: bash test_tp_4/probar_prompt6_0.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p60_build}"
TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp4p60.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

FALLOS=0
verificar_texto() {
    local archivo="$1" descripcion="$2" patron="$3"
    if grep -qF -- "$patron" "$archivo"; then
        echo "   OK: $descripcion"
    else
        echo "   FALLA: falta '$descripcion' ($patron)"
        FALLOS=$((FALLOS + 1))
    fi
}
no_verificar_texto() {
    local archivo="$1" descripcion="$2" patron="$3"
    if grep -qF -- "$patron" "$archivo"; then
        echo "   FALLA: aparecio '$descripcion' ($patron)"
        FALLOS=$((FALLOS + 1))
    else
        echo "   OK: $descripcion"
    fi
}

echo "=== Prompt 6.0 de TP4 — Compuerta del .asm (stdout TP3 preservado) ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo

echo "[1/4] Compilando proyecto..."
mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

echo "[2/4] Caso valido: stdout TP3 y ruta del .asm..."
cp "$RAIZ/test/tp2/generales/00_programa_valido.txt" "$TMP/valido.txt"
rm -f "$TMP/valido.asm"
java -cp "$BUILD" Main "$TMP/valido.txt" > "$TMP/valido.out" 2>&1

verificar_texto "$TMP/valido.out" "codigo fuente numerado" 'Codigo fuente:'
verificar_texto "$TMP/valido.out" "tokens" 'Tokens detectados'
verificar_texto "$TMP/valido.out" "estructuras" 'Declaracion de variables'
verificar_texto "$TMP/valido.out" "tabla de simbolos" 'Contenido de la tabla de simbolos:'
verificar_texto "$TMP/valido.out" "codigo intermedio" 'Código intermedio:'
no_verificar_texto "$TMP/valido.out" "sin encabezado TP4" 'Errores de compilación:'
no_verificar_texto "$TMP/valido.out" "sin mensaje nuevo de ASM" 'ASM generado:'
if [[ -f "$TMP/valido.asm" ]]; then
    echo "   OK: archivo .asm creado en la ruta esperada"
else
    echo "   FALLA: no se creo $TMP/valido.asm"
    FALLOS=$((FALLOS + 1))
fi
echo

echo "[3/4] Caso con error: error impreso y sin .asm..."
cp "$RAIZ/test/tp2/generales/01_falta_nombre_programa.txt" "$TMP/error.txt"
rm -f "$TMP/error.asm"
java -cp "$BUILD" Main "$TMP/error.txt" > "$TMP/error.out" 2>&1

verificar_texto "$TMP/error.out" "error con numero de linea" 'Línea 4: Error: Falta de nombre de programa'
verificar_texto "$TMP/error.out" "codigo fuente numerado" 'Codigo fuente:'
verificar_texto "$TMP/error.out" "tabla de simbolos" 'Contenido de la tabla de simbolos:'
no_verificar_texto "$TMP/error.out" "sin mensaje nuevo de ASM" 'ASM no generado:'
if [[ -e "$TMP/error.asm" ]]; then
    echo "   FALLA: se genero .asm para un fuente con errores"
    FALLOS=$((FALLOS + 1))
else
    echo "   OK: no se genero .asm para un fuente con errores"
fi
echo

echo "[4/4] Regresion de 4e (snapshots TP3 y prompts 4d..2)..."
if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash probar_prompt4e.sh > /tmp/p6_0_regresion_4e.log 2>&1); then
    echo "   OK: probar_prompt4e.sh"
else
    echo "   FALLA: probar_prompt4e.sh (ver /tmp/p6_0_regresion_4e.log)"
    FALLOS=$((FALLOS + 1))
fi
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Prompt 6.0 pasaron."
echo
echo "=== Prueba del Prompt 6.0 de TP4 completada ==="
exit 0

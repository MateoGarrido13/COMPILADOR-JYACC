#!/usr/bin/env bash
# Script de prueba para el Prompt 2 de TP4 (GeneradorAssembler esqueleto).
#
# Verifica:
#  1. Compilación del proyecto
#  2. Ejecución del compilador sobre un caso de prueba
#  3. Generación del archivo .asm
#  4. Presencia de secciones esperadas en el .asm (.386, .model, .data, .code, etc.)
#  5. Que la salida por consola no contenga errores inesperados
#
# Uso: bash test_tp_4/probar_prompt2.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p2_prompt2}"
CASO="${RAIZ}/test/tp2/generales/00_programa_valido.txt"

echo "=== Prompt 2 de TP4 — Prueba de GeneradorAssembler esqueleto ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo "Caso: $CASO"
echo

# 1. Compilar
echo "[1/5] Compilando proyecto..."
mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilación exitosa."
echo

# 2. Ejecutar compilador
echo "[2/5] Ejecutando compilador sobre el caso de prueba..."
SALIDA="$(java -cp "$BUILD" Main "$CASO" 2>&1)"
CODIGO_SALIDA=$?
if [[ $CODIGO_SALIDA -ne 0 ]]; then
    echo "FALLA: el compilador terminó con código $CODIGO_SALIDA"
    echo "$SALIDA"
    exit 1
fi
echo "   OK: compilador ejecutado sin errores de salida."
echo

# 3. Verificar que se generó el .asm
ASM="${CASO%.txt}.asm"
echo "[3/5] Verificando generación de archivo .asm: $ASM"
if [[ ! -f "$ASM" ]]; then
    echo "FALLA: no se generó el archivo $ASM"
    exit 1
fi
echo "   OK: archivo .asm generado."
echo

# 4. Verificar secciones del .asm
echo "[4/5] Verificando secciones del .asm..."
FALLOS=0

verificar_linea() {
    local descripcion="$1"
    local patron="$2"
    if grep -qF -- "$patron" "$ASM"; then
        echo "   OK: $descripcion"
    else
        echo "   FALLA: falta '$descripcion' ($patron)"
        FALLOS=$((FALLOS + 1))
    fi
}

verificar_linea "Encabezado .386" ".386"
verificar_linea "Modelo flat stdcall" ".model flat, stdcall"
verificar_linea "Opción casemap:none" "option casemap:none"
verificar_linea "Include masm32rt.inc" "masm32rt.inc"
verificar_linea "Sección .DATA" ".data"
verificar_linea "Sección .CODE" ".code"
verificar_linea "Etiqueta start" "start:"
verificar_linea "ExitProcess" "invoke ExitProcess, 0"
verificar_linea "Fin end start" "end start"

echo
if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificación(es) fallaron en el .asm"
    exit 1
fi
echo "   OK: todas las secciones del .asm están presentes."
echo

# 5. Verificar que la salida por consola no tenga errores inesperados
echo "[5/5] Verificando salida por consola sin errores inesperados..."
if echo "$SALIDA" | grep -q "Error:"; then
    echo "ADVERTENCIA: la salida contiene 'Error:' (revisar si es esperado):"
    echo "$SALIDA" | grep "Error:" | head -5
else
    echo "   OK: sin mensajes de error en la salida por consola."
fi
echo

echo "=== Prueba del Prompt 2 de TP4 completada ==="
exit 0

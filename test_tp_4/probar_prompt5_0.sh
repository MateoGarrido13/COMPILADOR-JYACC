#!/usr/bin/env bash
# Script de prueba para el Paquete 5.0 del Prompt 5 (TP4).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. El caso 01_sin_chequeos_activos compila sin errores
#  3. El .asm generado NO contiene labels de fallo ni mensajes de error
#     (5.0 es solo infraestructura; los chequeos se activan en 5.b/5.e/5.f/5.h)
#  4. Regresiones de prompts anteriores
#
# Uso: bash test_tp_4/probar_prompt5_0.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p5_0_build}"
CASOS="$RAIZ/test_tp_4/Runtime chequeos"

echo "=== Paquete 5.0 de TP4 — Infraestructura de chequeos runtime ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo "Casos: $CASOS"
echo

mkdir -p "$BUILD"

echo "[1/4] Compilando proyecto..."
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

echo "[2/4] Ejecutando casos..."
CANTIDAD=0
for caso in "$CASOS"/*.txt; do
    SALIDA="$(java -cp "$BUILD" Main "$caso" 2>&1)"
    if echo "$SALIDA" | grep -q "Error:"; then
        echo "FALLA: errores al compilar $(basename "$caso")"
        echo "$SALIDA" | grep "Error:" | head -5
        exit 1
    fi
    CANTIDAD=$((CANTIDAD + 1))
done
echo "   OK: $CANTIDAD casos compilados sin errores."
echo

FALLOS=0
verificar() {
    local archivo="$1" descripcion="$2" patron="$3"
    if grep -qF -- "$patron" "$archivo"; then
        echo "   OK: $descripcion"
    else
        echo "   FALLA: falta '$descripcion' ($patron) en $(basename "$archivo")"
        FALLOS=$((FALLOS + 1))
    fi
}
no_verificar() {
    local archivo="$1" descripcion="$2" patron="$3"
    if grep -qF -- "$patron" "$archivo"; then
        echo "   FALLA: aparecio '$descripcion' ($patron) en $(basename "$archivo")"
        FALLOS=$((FALLOS + 1))
    else
        echo "   OK: $descripcion"
    fi
}

echo "[3/4] Verificando infraestructura 5.0 (sin chequeos activos)..."

ASM="$CASOS/01_sin_chequeos_activos.asm"
verificar "$ASM" "seccion .DATA presente" '.data'
verificar "$ASM" "seccion .CODE presente" '.code'
verificar "$ASM" "etiqueta start" 'start:'
verificar "$ASM" "salida limpia ExitProcess" 'invoke ExitProcess, 0'
verificar "$ASM" "fin del programa" 'end start'
# 5.0 es solo infraestructura: NO debe haber labels de fallo ni mensajes de error
no_verificar "$ASM" "sin label error_overflow_suma" 'error_overflow_suma:'
no_verificar "$ASM" "sin label error_overflow_float" 'error_overflow_float:'
no_verificar "$ASM" "sin label error_negativo_resta" 'error_negativo_resta:'
no_verificar "$ASM" "sin label error_enum_invalido" 'error_enum_invalido:'
no_verificar "$ASM" "sin mensaje overflow suma" 'overflow en suma de ulongint'
no_verificar "$ASM" "sin mensaje overflow float" 'overflow en producto de doublef'
no_verificar "$ASM" "sin mensaje negativo en resta" 'resultado negativo en resta de ulongint'
no_verificar "$ASM" "sin mensaje enum invalido" 'asignacion invalida a variable de tipo enumeracion'
# B1: sin chequeos activos no debe emitirse el salto de skip (no hay bloque que saltar)
no_verificar "$ASM" "sin jmp fin_normal" 'jmp fin_normal'
no_verificar "$ASM" "sin etiqueta fin_normal" 'fin_normal:'

echo

echo "[4/4] Regresiones..."
for script in probar_prompt4e.sh probar_prompt4d.sh probar_prompt4c.sh probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p5_0_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p5_0_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Paquete 5.0 pasaron."
echo
echo "=== Prueba del Paquete 5.0 de TP4 completada ==="
exit 0

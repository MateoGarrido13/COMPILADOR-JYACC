#!/usr/bin/env bash
# Script de prueba para el Paquete 5.e del Prompt 5 (TP4).
# Overflow en productos de punto flotante doublef.
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los casos en "producto_doublef/" y en "producto_doublef_overflow/" compilan sin errores
#  3. El .asm de cada caso contiene fmul seguido de fxam/fstsw/fstp st(0)/sahf/jc error_overflow_float
#  4. El .asm contiene msg_overflow_float en .DATA y el label error_overflow_float: en .CODE
#  5. B1: el flujo normal salta el bloque de labels de error (jmp fin_normal / fin_normal:)
#  6. Regresiones de prompts anteriores (incluyendo 5.0 y 5.b)
#
# Uso: bash test_tp_4/probar_prompt5_e.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p5e_build}"
CASOS="$RAIZ/test_tp_4/producto_doublef"
# CORRECCION: esta carpeta tiene el unico caso que realmente desborda
# (max_doublef * max_doublef). Antes no lo ejecutaba ningun script.
CASOS_OVERFLOW="$RAIZ/test_tp_4/producto_doublef_overflow"

echo "=== Paquete 5.e de TP4 — Overflow en productos de punto flotante doublef ==="
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
for caso in "$CASOS"/*.txt "$CASOS_OVERFLOW"/*.txt; do
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

echo "[3/4] Verificando chequeo 5.e en los .asm..."

ASM="$CASOS/01_producto_valido.asm"
verificar "$ASM" "fmul en producto valido" 'fmul b_main'
verificar "$ASM" "guardia fxam despues de fmul" 'fxam'
verificar "$ASM" "guardia fstsw sahf despues de fxam" 'fstsw ax'
verificar "$ASM" "B2: pop de ST(0) para no desbordar la pila FPU" 'fstp st(0)'
verificar "$ASM" "guardia sahf despues de fstsw" 'sahf'
verificar "$ASM" "salto jc error_overflow_float" 'jc error_overflow_float'
verificar "$ASM" "mensaje de error en .DATA" 'msg_overflow_float DB "Error: overflow en producto de doublef",0'
verificar "$ASM" "label de fallo en .CODE" 'error_overflow_float:'
verificar "$ASM" "codigo de salida 2 en label de fallo" 'invoke ExitProcess, 2'
verificar "$ASM" "B1: salto que evita caer en labels de error" 'jmp fin_normal'
verificar "$ASM" "B1: reincorporacion al flujo normal" 'fin_normal:'

ASM="$CASOS/02_producto_desborda.asm"
verificar "$ASM" "fmul en producto desborda" 'fmul b_main'
verificar "$ASM" "guardia fxam despues de fmul" 'fxam'
verificar "$ASM" "guardia fstsw sahf despues de fxam" 'fstsw ax'
verificar "$ASM" "B2: pop de ST(0) para no desbordar la pila FPU" 'fstp st(0)'
verificar "$ASM" "guardia sahf despues de fstsw" 'sahf'
verificar "$ASM" "salto jc error_overflow_float" 'jc error_overflow_float'
verificar "$ASM" "mensaje de error en .DATA" 'msg_overflow_float DB "Error: overflow en producto de doublef",0'
verificar "$ASM" "label de fallo en .CODE" 'error_overflow_float:'
verificar "$ASM" "codigo de salida 2 en label de fallo" 'invoke ExitProcess, 2'
verificar "$ASM" "B1: salto que evita caer en labels de error" 'jmp fin_normal'
verificar "$ASM" "B1: reincorporacion al flujo normal" 'fin_normal:'

ASM="$CASOS_OVERFLOW/02_producto_desborda.asm"
verificar "$ASM" "caso real max*max: fmul" 'fmul b_main'
verificar "$ASM" "caso real max*max: jc error_overflow_float" 'jc error_overflow_float'
verificar "$ASM" "caso real max*max: B2 fstp st(0)" 'fstp st(0)'
verificar "$ASM" "caso real max*max: B1 jmp fin_normal" 'jmp fin_normal'

echo

echo "[4/4] Regresiones..."
for script in probar_prompt5_0.sh probar_prompt5_b.sh probar_prompt4e.sh probar_prompt4d.sh probar_prompt4c.sh probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p5e_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p5e_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Paquete 5.e pasaron."
echo
echo "=== Prueba del Paquete 5.e de TP4 completada ==="
exit 0

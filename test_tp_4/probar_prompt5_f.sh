#!/usr/bin/env bash
# Script de prueba para el Paquete 5.f del Prompt 5 (TP4).
# Resultado negativo en restas de enteros sin signo ulongint.
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 2 casos en "resta_enteros_negativa/" compilan sin errores
#  3. El .asm de cada caso contiene sub eax, ... seguido de jb error_negativo_resta
#  4. El .asm contiene msg_negativo_resta en .DATA y el label error_negativo_resta: en .CODE
#  5. Regresiones de prompts anteriores (incluyendo 5.0, 5.b, 5.e)
#
# Uso: bash test_tp_4/probar_prompt5_f.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p5f_build}"
CASOS="$RAIZ/test_tp_4/resta_enteros_negativa"

echo "=== Paquete 5.f de TP4 — Resultado negativo en restas de enteros ulongint ==="
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

echo "[3/4] Verificando chequeo 5.f en los .asm..."

ASM="$CASOS/01_resta_valida.asm"
verificar "$ASM" "sub eax en resta valida" 'sub eax, c_10_ul_ul'
verificar "$ASM" "guardia jb error_negativo_resta despues de sub" 'jb error_negativo_resta'
verificar "$ASM" "mensaje de error en .DATA" 'msg_negativo_resta DB "Error: resultado negativo en resta de ulongint",0'
verificar "$ASM" "label de fallo en .CODE" 'error_negativo_resta:'
verificar "$ASM" "codigo de salida 3 en label de fallo" 'invoke ExitProcess, 3'

ASM="$CASOS/02_resta_negativa.asm"
verificar "$ASM" "sub eax en resta negativa" 'sub eax, c_20_ul_ul'
verificar "$ASM" "guardia jb error_negativo_resta despues de sub" 'jb error_negativo_resta'
verificar "$ASM" "mensaje de error en .DATA" 'msg_negativo_resta DB "Error: resultado negativo en resta de ulongint",0'
verificar "$ASM" "label de fallo en .CODE" 'error_negativo_resta:'
verificar "$ASM" "codigo de salida 3 en label de fallo" 'invoke ExitProcess, 3'

echo

echo "[4/4] Regresiones..."
for script in probar_prompt5_0.sh probar_prompt5_b.sh probar_prompt5_e.sh probar_prompt4e.sh probar_prompt4d.sh probar_prompt4c.sh probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p5f_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p5f_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Paquete 5.f pasaron."
echo
echo "=== Prueba del Paquete 5.f de TP4 completada ==="
exit 0

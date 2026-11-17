#!/usr/bin/env bash
# Script de prueba para el Prompt 4b de TP4 (salida por consola: pout).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 11 casos manuales en "Salida por consola/" compilan sin errores
#  3. .DATA con los formatos usados (y solo los usados)
#  4. INVOKE crt_printf con formato correcto, valor por valor para numeros
#     y "offset" para cadenas
#  5. Ningun terceto pout queda como comentario en .CODE
#
# Uso: bash test_tp_4/probar_prompt4b.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4b_build}"
CASOS="$RAIZ/test_tp_4/Salida por consola"

echo "=== Prompt 4b de TP4 — Prueba de pout ==="
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

echo "[3/4] Verificando formatos e invokes..."
ASM="$CASOS/01_pout_variable_ulongint.asm"
verificar "$ASM" "fmt_ulongint en .DATA" 'fmt_ulongint DB "%u",0'
verificar "$ASM" "pout de variable por valor" 'invoke crt_printf, offset fmt_ulongint, x_main'
no_verificar "$ASM" "sin fmt_doublef innecesario" 'fmt_doublef'
no_verificar "$ASM" "sin fmt_cadena innecesario" 'fmt_cadena'

ASM="$CASOS/02_pout_constantes.asm"
verificar "$ASM" "fmt_ulongint" 'fmt_ulongint DB "%u",0'
verificar "$ASM" "fmt_doublef" 'fmt_doublef DB "%f",0'
verificar "$ASM" "pout de constante ulongint" 'invoke crt_printf, offset fmt_ulongint, c_42_ul_ul'
verificar "$ASM" "pout de constante doublef" 'invoke crt_printf, offset fmt_doublef, c_3_5'

ASM="$CASOS/03_pout_expresion_ulongint.asm"
verificar "$ASM" "pout de @auxN" 'invoke crt_printf, offset fmt_ulongint, @aux6'

ASM="$CASOS/04_pout_doublef.asm"
verificar "$ASM" "fmt_doublef" 'fmt_doublef DB "%f",0'
verificar "$ASM" "pout de variable DQ" 'invoke crt_printf, offset fmt_doublef, z_main'
no_verificar "$ASM" "sin fmt_ulongint innecesario" 'fmt_ulongint'

ASM="$CASOS/05_pout_todf.asm"
verificar "$ASM" "todf usa fmt_doublef" 'invoke crt_printf, offset fmt_doublef, @aux3'

ASM="$CASOS/06_pout_cadenas.asm"
verificar "$ASM" "fmt_cadena en .DATA" 'fmt_cadena DB "%s",0'
verificar "$ASM" "cadena con offset" 'invoke crt_printf, offset fmt_cadena, offset hola'
verificar "$ASM" "cadena repetida comparte simbolo" 'invoke crt_printf, offset fmt_cadena, offset hola'
no_verificar "$ASM" "sin fmt_ulongint innecesario" 'fmt_ulongint'

ASM="$CASOS/07_pout_mixto.asm"
for fmt in fmt_ulongint fmt_doublef fmt_cadena; do
    verificar "$ASM" "$fmt presente" "$fmt DB"
done
verificar "$ASM" "cadena con offset" 'offset resultado'
verificar "$ASM" "ulongint por valor" 'offset fmt_ulongint, x_main'
verificar "$ASM" "doublef por valor" 'offset fmt_doublef, z_main'

ASM="$CASOS/08_pout_resultado_funcion.asm"
verificar "$ASM" "resultado de CALL usa fmt del retorno" 'invoke crt_printf, offset fmt_ulongint, @aux7'

ASM="$CASOS/09_pout_en_control_flujo.asm"
verificar "$ASM" "cadena dentro del if" 'offset fmt_cadena, offset tres_o_mas'
verificar "$ASM" "variable dentro del else" 'offset fmt_ulongint, x_main'
verificar "$ASM" "etiqueta de salto intacta" 'label_1:'

ASM="$CASOS/10_pout_atributo.asm"
verificar "$ASM" "atributo resuelto a nombre ASM" 'invoke crt_printf, offset fmt_ulongint, x_main_punto'

ASM="$CASOS/11_pout_dentro_de_funcion.asm"
verificar "$ASM" "pout de cadena en funcion" 'offset fmt_cadena, offset dentro'
# Desde el Prompt 4c el parametro 'a' vive en el stack frame, no en .DATA.
verificar "$ASM" "pout de parametro en funcion" 'offset fmt_ulongint, dword ptr [ebp+8]'
verificar "$ASM" "prologue de la funcion antes del pout" 'fn_eco_main:'

echo
echo "[4/4] Verificando que no queden pout como comentario..."
for caso in "$CASOS"/*.txt; do
    ASM="${caso%.txt}.asm"
    no_verificar "$ASM" "$(basename "$caso") sin pout comentado" '; (pout'
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron en los .asm"
    exit 1
fi
echo "   OK: todas las verificaciones de pout pasaron."
echo
echo "=== Prueba del Prompt 4b de TP4 completada ==="
exit 0

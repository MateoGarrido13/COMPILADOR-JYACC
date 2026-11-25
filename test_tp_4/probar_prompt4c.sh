#!/usr/bin/env bash
# Script de prueba para el Prompt 4c de TP4 (funciones/metodos, stack frame,
# parametros en pila, retorno EAX/ST(0), orden PAR y 'this' implicito).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 8 casos manuales en "Funciones_y_metodos/" compilan sin errores
#  3. Prologue/epilogue con EBP y limpieza de pila hecha por el caller
#  4. Retorno ulongint en EAX y doublef en ST(0) -> FSTP en @auxN
#  5. Ranuras por formal (orden de evaluacion [2,1] no cambia [ebp+8])
#  6. Llamada directa a metodo con 'this' en [esp]/[ebp+8]
#  7. Parametros y variables locales ausentes de .DATA
#  8. Regresiones de prompts anteriores
#
# Uso: bash test_tp_4/probar_prompt4c.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4c_build}"
CASOS="$RAIZ/test_tp_4/Funciones_y_metodos"

echo "=== Prompt 4c de TP4 — Prueba de funciones y metodos ==="
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
verificar_siguiente() {
    local archivo="$1" descripcion="$2" patron="$3" siguiente="$4"
    if grep -A1 -F -- "$patron" "$archivo" | grep -qF -- "$siguiente"; then
        echo "   OK: $descripcion"
    else
        echo "   FALLA: '$patron' no va seguido de '$siguiente' en $(basename "$archivo")"
        FALLOS=$((FALLOS + 1))
    fi
}

echo "[3/4] Verificando convenacion de llamada..."

ASM="$CASOS/01_funcion_ulongint.asm"
verificar "$ASM" "etiqueta fn_" 'fn_suma_main:'
verificar "$ASM" "salvado de EBP" 'push ebp'
verificar "$ASM" "frame base" 'mov ebp, esp'
verificar "$ASM" "primer formal en ebp+8" 'mov eax, dword ptr [ebp+8]'
verificar "$ASM" "segundo formal en ebp+12" 'add eax, dword ptr [ebp+12]'
verificar "$ASM" "retorno en EAX" 'mov @aux10, eax'
verificar "$ASM" "epilogue leave" 'leave'
verificar "$ASM" "etiqueta fin" 'fin_suma_main:'
verificar "$ASM" "area de 2 dwords" 'sub esp, 8'
verificar "$ASM" "call directo" 'call fn_suma_main'
verificar "$ASM" "caller limpia" 'add esp, 8'
no_verificar "$ASM" "parametro a fuera de .DATA" 'a_main_suma DD'
no_verificar "$ASM" "parametro b fuera de .DATA" 'b_main_suma DD'

ASM="$CASOS/02_funcion_doublef.asm"
verificar "$ASM" "param doublef 1 en ebp+8" 'fld qword ptr [ebp+8]'
verificar "$ASM" "param doublef 2 en ebp+16" 'fmul qword ptr [ebp+16]'
verificar "$ASM" "area de 2 qwords" 'sub esp, 16'
verificar "$ASM" "high dword del primer doublef" 'mov dword ptr [esp+4], eax'
verificar "$ASM" "low dword del primer doublef" 'mov dword ptr [esp+0], eax'
verificar "$ASM" "retorno ST(0) bajado a @aux" 'fstp @aux10'
verificar "$ASM" "caller limpia 16 bytes" 'add esp, 16'

ASM="$CASOS/03_orden_evaluacion_PAR.asm"
verificar "$ASM" "PAR de b se emite primero" '; (PAR, b:main:elegir, 1$ul)'
verificar_siguiente "$ASM" "b va a su ranura formal [esp+4]" \
    'mov eax, c_1_ul_ul' 'mov dword ptr [esp+4], eax'
verificar_siguiente "$ASM" "a va a su ranura formal [esp+0]" \
    'mov eax, c_2_ul_ul' 'mov dword ptr [esp+0], eax'
verificar "$ASM" "ret lee primer formal" 'mov eax, dword ptr [ebp+8]'

ASM="$CASOS/04_llamada_anidada.asm"
verificar "$ASM" "call interno" 'call fn_doble_main'
verificar "$ASM" "limpieza del call interno" 'add esp, 4'
verificar "$ASM" "resultado interno en aux" 'mov @aux11, eax'
verificar "$ASM" "PAR externo usa el aux" '; (PAR, a:main:combinar, [11])'
verificar "$ASM" "call externo" 'call fn_combinar_main'
verificar "$ASM" "limpieza del call externo" 'add esp, 8'

ASM="$CASOS/05_metodo_con_this.asm"
verificar "$ASM" "etiqueta mt_" 'mt_m_main_calculadora:'
verificar "$ASM" "this en ebp+8" '; this: dword ptr [ebp+8]'
verificar "$ASM" "formal en ebp+12" 'mov eax, dword ptr [ebp+12]'
verificar "$ASM" "receptor por offset" 'mov eax, offset c_main'
verificar "$ASM" "this en esp+0" 'mov dword ptr [esp], eax'
verificar "$ASM" "area = this + 2 dwords" 'sub esp, 12'
verificar "$ASM" "llamada directa al metodo" 'call mt_m_main_calculadora'
verificar "$ASM" "caller limpia metodo" 'add esp, 12'
verificar "$ASM" "placeholder DD del objeto" 'c_main DD ?'

ASM="$CASOS/06_variables_locales_stack.asm"
verificar "$ASM" "frame con locales" 'sub esp, 16'
verificar "$ASM" "local ulongint en ebp-4" 'mov dword ptr [ebp-4], eax'
verificar "$ASM" "local doublef en ebp-16" 'fstp qword ptr [ebp-16]'
no_verificar "$ASM" "local t fuera de .DATA" 't_main_mezcla DD'
no_verificar "$ASM" "local d fuera de .DATA" 'd_main_mezcla DQ'

ASM="$CASOS/07_pout_dentro_de_funcion.asm"
verificar "$ASM" "prologue antes del pout" 'fn_eco_main:'
verificar "$ASM" "pout de cadena en funcion" 'offset fmt_cadena, offset dentro'
verificar "$ASM" "pout de parametro via ebp" 'offset fmt_ulongint, dword ptr [ebp+8]'
verificar "$ASM" "retorno del eco" 'call fn_eco_main'

ASM="$CASOS/08_metodo_heredado.asm"
verificar "$ASM" "resolucion estatica del heredado" 'call mt_m_main_padre'
verificar "$ASM" "receptor heredado" 'mov eax, offset h_main'
verificar "$ASM" "limpieza del heredado" 'add esp, 12'

for caso in "$CASOS"/*.txt; do
    ASM="${caso%.txt}.asm"
    no_verificar "$ASM" "$(basename "$caso") sin CALL comentado" '; (CALL'
    no_verificar "$ASM" "$(basename "$caso") sin ret comentado" '; (ret'
    no_verificar "$ASM" "$(basename "$caso") sin FUNCION comentada" '; (FUNCION'
done
echo

echo "[4/4] Regresiones..."
for script in probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p4c_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p4c_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron en los .asm"
    exit 1
fi
echo "   OK: todas las verificaciones del Prompt 4c pasaron."
echo
echo "=== Prueba del Prompt 4c de TP4 completada ==="
exit 0

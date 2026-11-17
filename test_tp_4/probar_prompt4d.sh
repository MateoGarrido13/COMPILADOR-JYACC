#!/usr/bin/env bash
# Script de prueba para el Prompt 4d de TP4 (objetos, atributos, ReferenciaAtributo, COPY).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 4 casos manuales en "Objetos y atributos/" compilan sin errores
#  3. Atributos de objeto emitidos en .DATA (DD/DQ)
#  4. Lectura/escritura de atributo ulongint y doublef (mov/fld/fstp sobre nombre ASM)
#  5. COPY estructural: copia campo a campo entre objetos compatibles
#  6. Herencia: atributos heredados (prefijo clase) y propios
#  7. Regresiones de prompts anteriores
#
# Uso: bash test_tp_4/probar_prompt4d.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4d_build}"
CASOS="$RAIZ/test_tp_4/Objetos y atributos"

echo "=== Prompt 4d de TP4 — Prueba de objetos y atributos ==="
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

echo "[3/4] Verificando objetos y atributos..."

ASM="$CASOS/01_copia_estructural.asm"
verificar "$ASM" "atributos punto en .DATA" 'p1_x_main DD'
verificar "$ASM" "atributo y en .DATA" 'p1_y_main DD'
verificar "$ASM" "COPY: lee p1_x_main" 'mov eax, p1_x_main'
verificar "$ASM" "COPY: escribe p2_x_main" 'mov p2_x_main, eax'
verificar "$ASM" "COPY: lee p1_y_main" 'mov eax, p1_y_main'
verificar "$ASM" "COPY: escribe p2_y_main" 'mov p2_y_main, eax'
verificar "$ASM" "pout de atributo destino" 'invoke crt_printf, offset fmt_ulongint, p2_x_main'
verificar "$ASM" "pout de atributo destino y" 'invoke crt_printf, offset fmt_ulongint, p2_y_main'

ASM="$CASOS/02_atributo_doublef.asm"
verificar "$ASM" "atributo doublef en .DATA" 'p1_y_main DQ'
verificar "$ASM" "escritura doublef atributo" 'fstp p1_y_main'
verificar "$ASM" "pout doublef atributo" 'invoke crt_printf, offset fmt_doublef, p1_y_main'
verificar "$ASM" "formato doublef presente" 'fmt_doublef DB "%f",0'

ASM="$CASOS/03_herencia_atributos.asm"
verificar "$ASM" "atributo heredado padre.a en .DATA" 'h_padre_a_main DD'
verificar "$ASM" "atributo propio hija.b en .DATA" 'h_b_main DD'
verificar "$ASM" "escritura atributo heredado" 'mov h_padre_a_main, eax'
verificar "$ASM" "escritura atributo propio" 'mov h_b_main, eax'
verificar "$ASM" "lectura atributo propio para pout" 'invoke crt_printf, offset fmt_ulongint, h_b_main'
verificar "$ASM" "lectura atributo heredado para pout" 'invoke crt_printf, offset fmt_ulongint, h_padre_a_main'

ASM="$CASOS/04_copia_con_herencia.asm"
verificar "$ASM" "atributo base.x en .DATA" 'd1_base_x_main DD'
verificar "$ASM" "atributo derivada.y en .DATA" 'd1_y_main DD'
verificar "$ASM" "COPY herencia: lee d1_base_x_main" 'mov eax, d1_base_x_main'
verificar "$ASM" "COPY herencia: escribe d2_base_x_main" 'mov d2_base_x_main, eax'
verificar "$ASM" "COPY herencia: lee d1_y_main" 'mov eax, d1_y_main'
verificar "$ASM" "COPY herencia: escribe d2_y_main" 'mov d2_y_main, eax'
verificar "$ASM" "pout atributo heredado destino" 'invoke crt_printf, offset fmt_ulongint, d2_base_x_main'
verificar "$ASM" "pout atributo propio destino" 'invoke crt_printf, offset fmt_ulongint, d2_y_main'

echo

echo "[4/4] Regresiones..."
for script in probar_prompt4c.sh probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p4d_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p4d_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron en los .asm"
    exit 1
fi
echo "   OK: todas las verificaciones del Prompt 4d pasaron."
echo
echo "=== Prueba del Prompt 4d de TP4 completada ==="
exit 0

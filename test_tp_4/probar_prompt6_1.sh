#!/usr/bin/env bash
# Script de prueba del Paquete 6.1 (validacion MASM32 del .asm generado).
#
# Verifica:
#  1. Compilacion del proyecto.
#  2. Tres casos representativos (ulongint, doublef, objetos) generan .asm.
#  3. Encabezado, secciones y cierre: includes, .data/.code, start:, end start.
#  4. DQ para doublef, crt_printf con %f, sin ALIGN 8 ni REAL8.
#  5. Terminadores CRLF en el archivo escrito.
#
# Este entorno no tiene ml.exe/ml64.exe: el ensamblado y enlazado reales se
# hacen en Windows con los comandos que el propio script imprime al final.
#
# Uso: bash test_tp_4/probar_prompt6_1.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p61_build}"
TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp4p61.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

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

echo "=== Prompt 6.1 de TP4 — Validacion MASM32 del .asm ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo

echo "[1/5] Compilando proyecto..."
mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

echo "[2/5] Generando tres casos representativos..."
CASOS=(
    "test_tp_4/Salida por consola/01_pout_variable_ulongint.txt"
    "test_tp_4/Salida por consola/04_pout_doublef.txt"
    "test_tp_4/Integracion/04_objetos_multiherencia_copy.txt"
)
NOMBRES=(ulongint doublef objetos)
ASM=()
for i in 0 1 2; do
    cp "$RAIZ/${CASOS[$i]}" "$TMP/${NOMBRES[$i]}.txt"
    if ! java -cp "$BUILD" Main "$TMP/${NOMBRES[$i]}.txt" \
            > "$TMP/${NOMBRES[$i]}.out" 2>&1; then
        echo "FALLA: java termino con error en ${NOMBRES[$i]}"
        FALLOS=$((FALLOS + 1))
    fi
    if grep -q "Error:" "$TMP/${NOMBRES[$i]}.out"; then
        echo "FALLA: errores de compilacion en ${NOMBRES[$i]}"
        FALLOS=$((FALLOS + 1))
    fi
    ASM[$i]="$TMP/${NOMBRES[$i]}.asm"
    if [[ -f "${ASM[$i]}" ]]; then
        echo "   OK: ${NOMBRES[$i]}.asm generado"
        # Copia sin CR para las verificaciones textuales (grep -F no ve el CR).
        tr -d '\r' < "${ASM[$i]}" > "$TMP/${NOMBRES[$i]}.lf.asm"
    else
        echo "FALLA: falta ${ASM[$i]}"
        FALLOS=$((FALLOS + 1))
        : > "$TMP/${NOMBRES[$i]}.lf.asm"
    fi
done
echo

echo "[3/5] Verificando includes, secciones y cierre..."
for i in 0 1 2; do
    N="${NOMBRES[$i]}"
    A="$TMP/$N.lf.asm"
    verificar "$A" "$N: .386" '.386'
    verificar "$A" "$N: .model flat, stdcall" '.model flat, stdcall'
    verificar "$A" "$N: option casemap:none" 'option casemap:none'
    verificar "$A" "$N: include masm32rt.inc" 'include \masm32\include\masm32rt.inc'
    verificar "$A" "$N: seccion .data" '.data'
    verificar "$A" "$N: seccion .code" '.code'
    verificar "$A" "$N: entrada start" 'start:'
    verificar "$A" "$N: cierre end start" 'end start'
    verificar "$A" "$N: ExitProcess" 'invoke ExitProcess, 0'
done
echo

echo "[4/5] Verificando DQ, crt_printf y decisiones de alineacion..."
A="$TMP/doublef.lf.asm"
verificar "$A" 'doublef: formato %f' 'fmt_doublef DB "%f",0'
verificar "$A" 'doublef: invoke crt_printf' 'invoke crt_printf, offset fmt_doublef, '
verificar "$A" 'doublef: constante DQ' 'c_2_5 DQ 2.5'
SIMBOLO="$(grep -F 'invoke crt_printf, offset fmt_doublef,' "$A" \
           | head -1 | sed 's/.*fmt_doublef, *//')"
if [[ -n "$SIMBOLO" ]] && grep -qE "^[[:space:]]+$SIMBOLO DQ " "$A"; then
    echo "   OK: doublef: el operando de %f se declara DQ ($SIMBOLO)"
else
    echo "   FALLA: doublef: operando de %f sin declaracion DQ ($SIMBOLO)"
    FALLOS=$((FALLOS + 1))
fi
for i in 0 1 2; do
    no_verificar "$TMP/${NOMBRES[$i]}.lf.asm" "${NOMBRES[$i]}: sin ALIGN 8" 'ALIGN 8'
    no_verificar "$TMP/${NOMBRES[$i]}.lf.asm" "${NOMBRES[$i]}: sin REAL8" 'REAL8'
done
echo

echo "[5/5] Verificando terminadores CRLF..."
for i in 0 1 2; do
    RAW="${ASM[$i]}"
    LF_TOTAL="$(tr -cd '\n' < "$RAW" | wc -c)"
    CR_TOTAL="$(tr -cd '\r' < "$RAW" | wc -c)"
    # awk sale 0 cuando cada registro termina en CR; 1 si alguno no.
    if [[ "$LF_TOTAL" -gt 0 && "$LF_TOTAL" -eq "$CR_TOTAL" ]] \
       && LC_ALL=C awk '{ if (sub(/\r$/, "") == 0) bad++ } END { exit (bad > 0) }' "$RAW"; then
        echo "   OK: ${NOMBRES[$i]}: $LF_TOTAL lineas terminadas en CRLF"
    else
        echo "   FALLA: ${NOMBRES[$i]}: LF=$LF_TOTAL CR=$CR_TOTAL (se espera CRLF en cada linea)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

echo "--- Ensamblado/enlace real (pendiente en Windows) ---"
if command -v ml.exe >/dev/null 2>&1 || command -v ml >/dev/null 2>&1; then
    echo "   Se encontro ml: se recomienda ejecutar los tres casos con:"
else
    echo "   No hay ml.exe/ml64.exe en este entorno: no se ensamblo ni enlazo."
    echo "   En Windows (MASM32 SDK), ejecutar para cada caso:"
fi
echo "     ml.exe /c /coff /Fo caso.obj caso.asm"
echo "     link.exe /SUBSYSTEM:CONSOLE /OUT:caso.exe caso.obj"
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones estaticas del Prompt 6.1 pasaron."
echo
echo "=== Prueba del Prompt 6.1 de TP4 completada (ensamblado real: Windows) ==="
exit 0

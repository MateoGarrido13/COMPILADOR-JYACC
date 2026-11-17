#!/usr/bin/env bash
# Script de prueba para el Prompt 4a de TP4 (control de flujo: labels, BF, BI,
# comparadores ulongint y doublef).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. if/else con ulongint -> SETcc, Jcc inverso, JMP y etiquetas
#  3. los seis comparadores -> SETcc y Jcc inverso correctos
#  4. repeat anidado -> etiquetas de inicio/salida y salto incondicional
#  5. condicion doublef -> FLD/FCOMP/FSTSW/MOV AX/SAHF y @fpu_status en .DATA
#  6. regresion implicitas: salida por consola sin errores
#
# Uso: bash test_tp_4/probar_prompt4a.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4a_build}"
CASOS="/tmp/tp4p4a_casos"

echo "=== Prompt 4a de TP4 — Prueba de control de flujo ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo

mkdir -p "$BUILD" "$CASOS"

echo "[1/6] Compilando proyecto..."
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

echo "[2/6] Preparando casos de prueba..."
cat > "$CASOS/if_else.txt" <<'EOF'
prog
ulongint a, b, k;
begin
    a := 5$ul;
    b := 3$ul;
    if (a > b)
        k := 1$ul;
    else
        k := 2$ul;
    end_if;
end
EOF
cp "$RAIZ/test/TP3_y_TP4/SELECCION/08_comparadores.txt" "$CASOS/comparadores.txt"
cp "$RAIZ/test/TP3_y_TP4/ITERACION/04_repeat_anidado.txt" "$CASOS/repeat_anidado.txt"
cp "$RAIZ/test/tp2/verificacion_r33_conversion/en_condicion_if.txt" "$CASOS/doublef.txt"

for caso in if_else comparadores repeat_anidado doublef; do
    SALIDA="$(java -cp "$BUILD" Main "$CASOS/$caso.txt" 2>&1)"
    if echo "$SALIDA" | grep -q "Error:"; then
        echo "FALLA: errores al compilar $caso.txt"
        echo "$SALIDA" | grep "Error:" | head -5
        exit 1
    fi
done
echo "   OK: 4 casos compilados sin errores."
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

echo "[3/6] Verificando if/else (ulongint)..."
ASM="$CASOS/if_else.asm"
verificar "$ASM" "SETcc para '>'" "seta al"
verificar "$ASM" "movzx" "movzx eax, al"
verificar "$ASM" "booleano en @aux" "mov @aux4, eax"
verificar "$ASM" "BF con Jcc inverso (jbe)" "jbe label_1"
verificar "$ASM" "BI con jmp" "jmp label_2"
verificar "$ASM" "etiqueta label_1" "label_1:"
verificar "$ASM" "etiqueta label_2" "label_2:"
echo

echo "[4/6] Verificando los seis comparadores..."
ASM="$CASOS/comparadores.asm"
for patron in "seta al" "setb al" "setae al" "setbe al" "sete al" "setne al" \
              "jbe label_1" "jae label_2" "jb label_3" "ja label_4" \
              "jne label_5" "je label_6"; do
    verificar "$ASM" "instruccion '$patron'" "$patron"
done
echo

echo "[5/6] Verificando repeat anidado..."
ASM="$CASOS/repeat_anidado.asm"
for etiqueta in label_1 label_2 label_3 label_4; do
    verificar "$ASM" "etiqueta $etiqueta" "$etiqueta:"
done
verificar "$ASM" "salto de retorno del repeat interno" "jmp label_2"
verificar "$ASM" "salto de retorno del repeat externo" "jmp label_1"
echo

echo "[6/6] Verificando condicion doublef..."
ASM="$CASOS/doublef.asm"
verificar "$ASM" "@fpu_status en .DATA" "@fpu_status DW ?"
verificar "$ASM" "FLD del operando izquierdo" "fld @aux3"
verificar "$ASM" "FCOMP del operando derecho" "fcomp c_0_5"
verificar "$ASM" "FSTSW a memoria" "fstsw @fpu_status"
verificar "$ASM" "copia de estado a AX" "mov ax, @fpu_status"
verificar "$ASM" "SAHF" "sahf"
verificar "$ASM" "BF doublef con jbe" "jbe label_1"
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron en los .asm"
    exit 1
fi
echo "   OK: todas las instrucciones y secciones verificadas."
echo
echo "=== Prueba del Prompt 4a de TP4 completada ==="
exit 0

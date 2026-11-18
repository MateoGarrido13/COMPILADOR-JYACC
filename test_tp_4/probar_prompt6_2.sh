#!/usr/bin/env bash
# Script consolidado del Paquete 6.2 (regresion TP3 + suite TP4/Grupo 43).
#
# Verifica:
#  1. Compilacion del proyecto.
#  2. Regresion TP3 de los temas 13, 17, 19, 23, 25, 28, 31 y 33: todos los
#     fuentes compilan sin excepcion, con stdout TP3 determinista.
#  3. Chequeos runtime b/e/f/h: scripts propios + caso disparador y caso
#     valido limite por cada uno.
#  4. Programa integral: control de flujo + recursion + objetos con herencia
#     multiple, COPY y acceso prefijado + pout mixto.
#  5. Regresiones 4e, 6.0 y 6.1.
#
# Nota: no hay ml.exe/link aqui. Los chequeos son de compilacion Java y de
# texto del .asm; el disparo real en ejecucion queda para Windows.
#
# Uso: bash test_tp_4/probar_prompt6_2.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p62_build}"
TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp4p62.XXXXXX")"
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
correr_suite() {
    local nombre="$1" log="$TMP/suite_$1.log"
    if bash "$RAIZ/test_tp_4/$nombre" > "$log" 2>&1; then
        local n
        n="$(grep -c 'FALLA:' "$log" || true)"
        if [[ "$n" -eq 0 ]]; then
            echo "   OK: $nombre"
        else
            echo "   FALLA: $nombre ($n fallas, ver $log)"
            FALLOS=$((FALLOS + 1))
        fi
    else
        echo "   FALLA: $nombre (exit != 0, ver $log)"
        FALLOS=$((FALLOS + 1))
    fi
}

echo "=== Prompt 6.2 de TP4 — Regresion TP3 + suite TP4/Grupo 43 ==="
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

echo "[2/5] Regresion TP3 (temas 13, 17, 19, 23, 25, 28, 31, 33)..."
TEMAS=(
    verificacion_r13_estructura_while
    verificacion_r17_asig_expresiones
    verificacion_r19_param_reales
    verificacion_r23_enums
    verificacion_r25_objetos
    verificacion_r28_r31_atributos
    verificacion_r33_conversion
)
TOTAL_TP3=0
FALLOS_TP3=0
CONTADOR=0
for tema in "${TEMAS[@]}"; do
    CANTIDAD=0
    while IFS= read -r caso; do
        CANTIDAD=$((CANTIDAD + 1))
        CONTADOR=$((CONTADOR + 1))
        cp "$caso" "$TMP/tp3_$CONTADOR.txt"
        if ! java -cp "$BUILD" Main "$TMP/tp3_$CONTADOR.txt" \
                > "$TMP/tp3_$CONTADOR.run1" 2>&1; then
            echo "   FALLA: java exit != 0 en $(basename "$caso")"
            FALLOS_TP3=$((FALLOS_TP3 + 1))
        fi
        java -cp "$BUILD" Main "$TMP/tp3_$CONTADOR.txt" \
            > "$TMP/tp3_$CONTADOR.run2" 2>&1
        if ! grep -qF 'Codigo fuente:' "$TMP/tp3_$CONTADOR.run1" \
           || ! grep -qF 'Tokens detectados' "$TMP/tp3_$CONTADOR.run1"; then
            echo "   FALLA: stdout TP3 ausente en $(basename "$caso")"
            FALLOS_TP3=$((FALLOS_TP3 + 1))
        fi
        if grep -qE 'Exception|[[:space:]]at [A-Za-z0-9_$.]+\(' "$TMP/tp3_$CONTADOR.run1"; then
            echo "   FALLA: stack trace en $(basename "$caso")"
            FALLOS_TP3=$((FALLOS_TP3 + 1))
        fi
        if ! cmp -s "$TMP/tp3_$CONTADOR.run1" "$TMP/tp3_$CONTADOR.run2"; then
            echo "   FALLA: stdout no determinista en $(basename "$caso")"
            FALLOS_TP3=$((FALLOS_TP3 + 1))
        fi
    done < <(find "$RAIZ/test/tp2/$tema" -maxdepth 1 -name '*.txt' | sort)
    TOTAL_TP3=$((TOTAL_TP3 + CANTIDAD))
    echo "   $tema: $CANTIDAD casos"
done
# Fuentes semanticas adicionales (la plan dice "si aplica").
CANTIDAD=0
while IFS= read -r caso; do
    CANTIDAD=$((CANTIDAD + 1))
    CONTADOR=$((CONTADOR + 1))
    cp "$caso" "$TMP/tp3_$CONTADOR.txt"
    java -cp "$BUILD" Main "$TMP/tp3_$CONTADOR.txt" > "$TMP/tp3_$CONTADOR.run1" 2>&1
    java -cp "$BUILD" Main "$TMP/tp3_$CONTADOR.txt" > "$TMP/tp3_$CONTADOR.run2" 2>&1
    if ! grep -qF 'Codigo fuente:' "$TMP/tp3_$CONTADOR.run1" \
       || ! cmp -s "$TMP/tp3_$CONTADOR.run1" "$TMP/tp3_$CONTADOR.run2"; then
        echo "   FALLA: regresion semantica en $(basename "$caso")"
        FALLOS_TP3=$((FALLOS_TP3 + 1))
    fi
done < <(find "$RAIZ/test/TP3_SEMANTICAS" -name '*.txt' | sort)
TOTAL_TP3=$((TOTAL_TP3 + CANTIDAD))
echo "   test/TP3_SEMANTICAS: $CANTIDAD casos"
echo "   Total TP3: $TOTAL_TP3 casos, $FALLOS_TP3 fallas"
FALLOS=$((FALLOS + FALLOS_TP3))
echo

echo "[3/5] Chequeos runtime b/e/f/h..."
correr_suite probar_prompt5_b.sh
correr_suite probar_prompt5_e.sh
correr_suite probar_prompt5_f.sh
correr_suite probar_prompt5_h.sh

# Casos disparadores y sus guardias en el .asm generado.
verificar "$RAIZ/test_tp_4/resta_enteros_negativos/02_suma_desborda.asm" \
    'b: caso disparador 4294967295+1' 'jc error_overflow_suma'
verificar "$RAIZ/test_tp_4/resta_enteros_negativos/01_suma_valida.asm" \
    'b: caso valido con guardia instrumentada' 'jc error_overflow_suma'
verificar "$RAIZ/test_tp_4/producto_doublef_overflow/02_producto_desborda.asm" \
    'e: caso disparador max_doublef*max_doublef' 'jc error_overflow_float'
verificar "$RAIZ/test_tp_4/producto_doublef_overflow/01_producto_valido.asm" \
    'e: caso valido con guardia instrumentada' 'jc error_overflow_float'
verificar "$RAIZ/test_tp_4/resta_enteros_negativa/02_resta_negativa.asm" \
    'f: caso disparador 10-20' 'jb error_negativo_resta'
verificar "$RAIZ/test_tp_4/resta_enteros_negativa/01_resta_valida.asm" \
    'f: caso valido con guardia instrumentada' 'jb error_negativo_resta'
verificar "$RAIZ/test_tp_4/enumerados/02_asignacion_invalida.asm" \
    'h: caso disparador enum ulongint' 'error_enum_invalido'
verificar "$RAIZ/test_tp_4/enumerados/04_doublef_invalida.asm" \
    'h: caso disparador enum doublef' 'error_enum_invalido'
verificar "$RAIZ/test_tp_4/enumerados/01_asignacion_valida.asm" \
    'h: caso valido compilado' 'end start'
echo

echo "[4/5] Programa integral 6.2..."
cp "$RAIZ/test_tp_4/Integracion/07_programa_integral_6_2.txt" "$TMP/integral.txt"
java -cp "$BUILD" Main "$TMP/integral.txt" > "$TMP/integral.out" 2>&1
no_verificar "$TMP/integral.out" 'integral: sin errores de compilacion' 'Error:'
INTEGRAL="$TMP/integral.asm"
if [[ -f "$INTEGRAL" ]]; then
    tr -d '\r' < "$INTEGRAL" > "$TMP/integral.lf.asm"
    I="$TMP/integral.lf.asm"
    verificar "$I" 'integral: .data' '.data'
    verificar "$I" 'integral: .code' '.code'
    verificar "$I" 'integral: end start' 'end start'
    verificar "$I" 'integral: control de flujo (label_3)' 'label_3:'
    verificar "$I" 'integral: repeat cierra con jmp label_3' 'jmp label_3'
    verificar "$I" 'integral: funcion recursiva definida' 'fn_fact_main:'
    verificar "$I" 'integral: llamada recursiva' 'call fn_fact_main'
    verificar "$I" 'integral: atributo prefijado basea' 's2_basea_u_main'
    verificar "$I" 'integral: atributo doublef de baseb' 's2_baseb_v_main DQ ?'
    verificar "$I" 'integral: COPY posicional del qword' \
        'mov eax, dword ptr [s1_baseb_v_main+4]'
    verificar "$I" 'integral: pout cadena' 'invoke crt_printf, offset fmt_cadena'
    verificar "$I" 'integral: pout ulongint' 'invoke crt_printf, offset fmt_ulongint'
    verificar "$I" 'integral: pout doublef' 'invoke crt_printf, offset fmt_doublef'
else
    echo "   FALLA: no se genero el .asm integral"
    FALLOS=$((FALLOS + 1))
fi
echo

echo "[5/5] Regresiones 4e, 6.0 y 6.1..."
correr_suite probar_prompt4e.sh
correr_suite probar_prompt6_0.sh
correr_suite probar_prompt6_1.sh
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Prompt 6.2 pasaron ($TOTAL_TP3 fuentes TP3)."
echo
echo "=== Prueba del Prompt 6.2 de TP4 completada (runtime real: Windows) ==="
exit 0

#!/usr/bin/env bash
# Script de prueba para el Prompt 4e de TP4 (integracion de 4a..4d).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 6 casos integrales compilan sin errores
#  3. ASM integral: if anidado ulongint/doublef, repeat-while anidado,
#     llamada a funcion factorial + metodo, multiherencia + COPY + prefijos,
#     pout mixto (cadena/expresion/llamada/todf) y COPY con nombres distintos
#  4. Ningun operador ya implementado queda como comentario en el .ASM
#  5. Salida de consola de TP3 identica a las snapshots (fuente, tokens,
#     estructuras, errores, TS y tercetos)
#  6. Regresiones de prompts 4d, 4c, 4b, 4a, 3 y 2
#
# Uso: bash test_tp_4/probar_prompt4e.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4e_build}"
CASOS="$RAIZ/test_tp_4/Integracion"
CONSOLA="$CASOS/consola_tp3"

echo "=== Prompt 4e de TP4 — Prueba de integracion ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo "Casos: $CASOS"
echo

mkdir -p "$BUILD"

echo "[1/6] Compilando proyecto..."
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

echo "[2/6] Ejecutando casos integrales..."
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

echo "[3/6] Verificando codigo ASM integral..."

ASM="$CASOS/01_if_doublef_anidado.asm"
verificar "$ASM" "todf materializa doublef en @aux" 'fstp @aux5'
verificar "$ASM" "comparacion FPU con FCOMP" 'fcomp d_main'
verificar "$ASM" "estado FPU para BF doublef" 'fstsw @fpu_status'
verificar "$ASM" "BF doublef usa Jcc inverso" 'jbe label_3'
verificar "$ASM" "comparador ulongint anidado" 'cmp eax, c_2_ul_ul'
verificar "$ASM" "BF ulongint anidado" 'jb label_1'
verificar "$ASM" "BI del then interno" 'jmp label_2'
verificar "$ASM" "BI del then externo" 'jmp label_4'
verificar "$ASM" "etiquetas label_1..label_4" 'label_4:'
verificar "$ASM" "pout del resultado" 'invoke crt_printf, offset fmt_ulongint, k_main'

ASM="$CASOS/02_repeat_while_anidado.asm"
verificar "$ASM" "inicio del repeat externo" 'label_1:'
verificar "$ASM" "inicio del repeat interno" 'label_2:'
verificar "$ASM" "BF cierra el repeat interno" 'jae label_3'
verificar "$ASM" "BI vuelve al repeat interno" 'jmp label_2'
verificar "$ASM" "BF cierra el repeat externo" 'jae label_6'
verificar "$ASM" "BI vuelve al repeat externo" 'jmp label_1'
verificar "$ASM" "if dentro del repeat" 'jb label_4'
verificar "$ASM" "pout de variable" 'offset fmt_ulongint, acc_main'
verificar "$ASM" "pout de expresion" 'offset fmt_ulongint, @aux29'

ASM="$CASOS/03_recursion_factorial_metodo.asm"
verificar "$ASM" "prologo de fact" 'fn_fact_main:'
verificar "$ASM" "frame de fact" 'push ebp'
verificar "$ASM" "llamada a fact desde main" 'call fn_fact_main'
verificar "$ASM" "limpieza de la call a fact" 'add esp, 4'
verificar "$ASM" "resultado de fact en @aux" 'mov @aux17, eax'
verificar "$ASM" "resultado de fact copiado a r" 'mov r_main, eax'
verificar "$ASM" "multiplicacion del metodo escala" 'imul eax, dword ptr [ebp+12]'
verificar "$ASM" "epilogue de fact" 'fin_fact_main:'
verificar "$ASM" "metodo escala" 'mt_escala_main_calculadora:'
verificar "$ASM" "this del metodo" '; this: dword ptr [ebp+8]'
verificar "$ASM" "CALLMET integrado" 'call mt_escala_main_calculadora'
verificar "$ASM" "limpieza del CALLMET" 'add esp, 8'
verificar "$ASM" "pout de factorial" 'offset fmt_ulongint, r_main'
verificar "$ASM" "pout de escala" 'offset fmt_ulongint, m_main'
no_verificar "$ASM" "CALL no queda comentado" '; (CALL'

ASM="$CASOS/04_objetos_multiherencia_copy.asm"
verificar "$ASM" "atributo heredado de b1 en .DATA" 's1_b1_u_main DD'
verificar "$ASM" "atributo heredado de b2 (doublef) en .DATA" 's1_b2_v_main DQ'
verificar "$ASM" "atributo propio en .DATA" 's1_w_main DD'
verificar "$ASM" "COPY copia u" 'mov s2_b1_u_main, eax'
verificar "$ASM" "COPY copia high de v" 'mov dword ptr [s2_b2_v_main+4], eax'
verificar "$ASM" "COPY copia low de v" 'mov dword ptr [s2_b2_v_main], eax'
verificar "$ASM" "COPY copia w" 'mov s2_w_main, eax'
verificar "$ASM" "acceso prefijado en pout" 'offset fmt_ulongint, s2_b1_u_main'
verificar "$ASM" "pout de doble Heredado" 'offset fmt_doublef, s2_b2_v_main'
no_verificar "$ASM" "COPY no queda comentado" '; (COPY'

ASM="$CASOS/05_pout_expresiones_llamadas.asm"
verificar "$ASM" "cadena en .DATA" 'resultado DB "resultado=",0'
verificar "$ASM" "pout de cadena" 'offset fmt_cadena, offset resultado'
verificar "$ASM" "pout de resultado de llamada" 'offset fmt_ulongint, @aux11'
verificar "$ASM" "pout de expresion" 'offset fmt_ulongint, @aux14'
verificar "$ASM" "pout de todf" 'offset fmt_doublef, @aux16'
verificar "$ASM" "formato ulongint" 'fmt_ulongint DB "%u",0'
verificar "$ASM" "formato doublef" 'fmt_doublef DB "%f",0'
verificar "$ASM" "formato cadena" 'fmt_cadena DB "%s",0'

ASM="$CASOS/06_copy_layout_compat_distinto_nombre.asm"
verificar "$ASM" "COPY posicional de ulongint con otro nombre" 'mov b_y_main, eax'
verificar "$ASM" "COPY posicional de doublef high" 'mov dword ptr [b_e_main+4], eax'
verificar "$ASM" "COPY posicional de doublef low" 'mov dword ptr [b_e_main], eax'
verificar "$ASM" "pout del destino" 'offset fmt_ulongint, b_y_main'
no_verificar "$ASM" "sin COPY comentado por nombre distinto" 'atributo no encontrado'

for caso in "$CASOS"/*.txt; do
    ASM="${caso%.txt}.asm"
    no_verificar "$ASM" "$(basename "$caso") sin CALL comentado" '; (CALL'
    no_verificar "$ASM" "$(basename "$caso") sin ret comentado" '; (ret'
    no_verificar "$ASM" "$(basename "$caso") sin FUNCION comentada" '; (FUNCION'
    no_verificar "$ASM" "$(basename "$caso") sin BF comentado" '; (BF'
    no_verificar "$ASM" "$(basename "$caso") sin COPY comentado" '; (COPY'
done
echo

echo "[4/6] Verificando salida de consola de TP3 (snapshots)..."
TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp4e_consola.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT
consulta_snapshot() {
    local fuente="$1" esperado="$2"
    local nombre; nombre="$(basename "$fuente")"
    cp "$fuente" "$TMP/$nombre"
    if ! java -cp "$BUILD" Main "$TMP/$nombre" > "$TMP/$nombre.out" 2>&1; then
        echo "   FALLA: java fallo para $nombre"
        FALLOS=$((FALLOS + 1))
        return
    fi
    if diff -q "$esperado" "$TMP/$nombre.out" >/dev/null; then
        echo "   OK: consola de TP3 identica en $nombre"
    else
        echo "   FALLA: consola de TP3 cambio en $nombre"
        diff -u "$esperado" "$TMP/$nombre.out" | head -20
        FALLOS=$((FALLOS + 1))
    fi
}
consulta_snapshot "$RAIZ/test/tp2/generales/00_programa_valido.txt" \
    "$CONSOLA/01_programa_valido.out"
consulta_snapshot "$RAIZ/test/tp2/generales/01_falta_nombre_programa.txt" \
    "$CONSOLA/02_falta_nombre_programa.out"
consulta_snapshot "$RAIZ/test/tp2/verificacion_r25_objetos/atributos_heredados_orden.txt" \
    "$CONSOLA/03_atributos_heredados_orden.out"
consulta_snapshot "$RAIZ/test/tp2/verificacion_r17_asig_expresiones/anidamiento.txt" \
    "$CONSOLA/04_asignacion_en_expresion.out"
consulta_snapshot "$RAIZ/test/tp2/verificacion_r28_r31_atributos/metodo_heredado_prefijado_con_orden.txt" \
    "$CONSOLA/05_metodo_heredado.out"
echo

echo "[5/6] Verificando matriz de regresiones internas..."
for caso in "$CASOS"/*.txt; do
    ASM="${caso%.txt}.asm"
    verificar "$ASM" "$(basename "$caso") con encabezado MASM" '.model flat, stdcall'
    verificar "$ASM" "$(basename "$caso") con start/end" 'end start'
done
echo

echo "[6/6] Regresiones de prompts anteriores..."
for script in probar_prompt4d.sh probar_prompt4c.sh probar_prompt4b.sh \
              probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p4e_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p4e_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Prompt 4e pasaron."
echo
echo "=== Prueba del Prompt 4e de TP4 completada ==="
exit 0

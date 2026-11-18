#!/usr/bin/env bash
# Script de prueba para el Paquete 5.h del Prompt 5 (TP4).
# Asignación inválida a variable de tipo enumeración.
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Los 2 casos en "enumerados/" compilan sin errores
#  3. El .asm de cada caso contiene la guardia de enum:
#     mov eax, <valor> / cmp eax, <valor_permitido> / je enum_valido_N
#     (repetido para cada valor del enum) seguido de jmp error_enum_invalido
#  4. El .asm contiene msg_enum_invalido en .DATA y el label
#     error_enum_invalido: en .CODE con ExitProcess, 4
#  5. Regresiones de prompts anteriores (incluyendo 5.0, 5.b, 5.e)
#
# Uso: bash test_tp_4/probar_prompt5_h.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p5h_build}"
CASOS="$RAIZ/test_tp_4/enumerados"

echo "=== Paquete 5.h de TP4 — Asignación inválida a variable de tipo enumeración ==="
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

echo "[3/4] Verificando chequeo 5.h en los .asm..."

ASM="$CASOS/01_asignacion_valida.asm"
verificar "$ASM" "guardia mov eax en asignacion valida" 'mov eax, c_2_ul_ul'
verificar "$ASM" "cmp contra valor 1 del enum" 'cmp eax, 1'
verificar "$ASM" "cmp contra valor 2 del enum" 'cmp eax, 2'
verificar "$ASM" "cmp contra valor 3 del enum" 'cmp eax, 3'
verificar "$ASM" "salto je a enum_valido_0" 'je enum_valido_0'
verificar "$ASM" "jmp a error_enum_invalido" 'jmp error_enum_invalido'
verificar "$ASM" "etiqueta enum_valido_0:" 'enum_valido_0:'
verificar "$ASM" "etiqueta error_enum_invalido:" 'error_enum_invalido:'
verificar "$ASM" "mensaje de error en .DATA" 'msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0'
verificar "$ASM" "codigo de salida 4 en label de fallo" 'invoke ExitProcess, 4'
verificar "$ASM" "B1: salto que evita caer en labels de error" 'jmp fin_normal'
verificar "$ASM" "B1: reincorporacion al flujo normal" 'fin_normal:'

ASM="$CASOS/02_asignacion_invalida.asm"
verificar "$ASM" "guardia mov eax en asignacion invalida" 'mov eax, c_5_ul_ul'
verificar "$ASM" "cmp contra valor 1 del enum" 'cmp eax, 1'
verificar "$ASM" "cmp contra valor 2 del enum" 'cmp eax, 2'
verificar "$ASM" "cmp contra valor 3 del enum" 'cmp eax, 3'
verificar "$ASM" "salto je a enum_valido_0" 'je enum_valido_0'
verificar "$ASM" "jmp a error_enum_invalido" 'jmp error_enum_invalido'
verificar "$ASM" "etiqueta enum_valido_0:" 'enum_valido_0:'
verificar "$ASM" "etiqueta error_enum_invalido:" 'error_enum_invalido:'
verificar "$ASM" "mensaje de error en .DATA" 'msg_enum_invalido DB "Error: asignacion invalida a variable de tipo enumeracion",0'
verificar "$ASM" "codigo de salida 4 en label de fallo" 'invoke ExitProcess, 4'
verificar "$ASM" "B1: salto que evita caer en labels de error" 'jmp fin_normal'
verificar "$ASM" "B1: reincorporacion al flujo normal" 'fin_normal:'

echo

echo "[4/4] Regresiones..."
# CORRECCION: se agrega 5.f, ausente antes aunque el plan lo declaraba.
for script in probar_prompt5_0.sh probar_prompt5_b.sh probar_prompt5_e.sh probar_prompt5_f.sh probar_prompt4e.sh probar_prompt4d.sh probar_prompt4c.sh probar_prompt4b.sh probar_prompt4a.sh probar_prompt3.sh probar_prompt2.sh; do
    if (cd "$RAIZ/test_tp_4" && TP4_BUILD="$BUILD" bash "$script" >/tmp/p5h_regresion_${script}.log 2>&1); then
        echo "   OK: $script"
    else
        echo "   FALLA: $script (ver /tmp/p5h_regresion_${script}.log)"
        FALLOS=$((FALLOS + 1))
    fi
done
echo

if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "   OK: todas las verificaciones del Paquete 5.h pasaron."
echo
echo "=== Prueba del Paquete 5.h de TP4 completada ==="
exit 0

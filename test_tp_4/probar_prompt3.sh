#!/usr/bin/env bash
# Script de prueba para el Prompt 3 de TP4 (traduccion basica a .CODE).
#
# Verifica:
#  1. Compilacion del proyecto
#  2. Ejecucion del compilador sobre un caso con aritmetica y todf
#  3. Generacion del archivo .asm
#  4. Presencia de instrucciones MASM32 esperadas (mov, add, imul, fild, fstp, etc.)
#  5. Seccion .DATA con temporales @auxN y tipos correctos
#
# Uso: bash test_tp_4/probar_prompt3.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p3_prompt3}"
CASO_DIR="/tmp"

echo "=== Prompt 3 de TP4 — Prueba de traduccion a .CODE ==="
echo "Raiz: $RAIZ"
echo "Build: $BUILD"
echo

# 1. Compilar
echo "[1/4] Compilando proyecto..."
mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: No se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

# 2. Crear caso de prueba con aritmetica enteros, float y todf
echo "[2/4] Creando caso de prueba..."
CASO="$CASO_DIR/prog_prompt3.txt"
ASM="$CASO_DIR/prog_prompt3.asm"
cat > "$CASO" <<'EOF'
prog
ulongint x, y, z;
doublef a;
begin
    x := 5$ul;
    y := x + 3$ul;
    z := x * y;
    a := todf(x);
end
EOF
echo "   OK: caso creado en $CASO"
echo

# 3. Ejecutar compilador
echo "[3/4] Ejecutando compilador..."
SALIDA="$(java -cp "$BUILD" Main "$CASO" 2>&1)"
CODIGO_SALIDA=$?
if [[ $CODIGO_SALIDA -ne 0 ]]; then
    echo "FALLA: el compilador termino con codigo $CODIGO_SALIDA"
    echo "$SALIDA"
    exit 1
fi
echo "   OK: compilador ejecutado sin errores de salida."
echo

# 4. Verificar .asm
echo "[4/4] Verificando archivo .asm: $ASM"
if [[ ! -f "$ASM" ]]; then
    echo "FALLA: no se genero el archivo $ASM"
    exit 1
fi
echo "   OK: archivo .asm generado."

FALLOS=0

verificar_linea() {
    local descripcion="$1"
    local patron="$2"
    if grep -qF -- "$patron" "$ASM"; then
        echo "   OK: $descripcion"
    else
        echo "   FALLA: falta '$descripcion' ($patron)"
        FALLOS=$((FALLOS + 1))
    fi
}

# Encabezado
verificar_linea "Encabezado .386" ".386"
verificar_linea "Modelo flat stdcall" ".model flat, stdcall"
verificar_linea "Include masm32rt.inc" "masm32rt.inc"

# Secciones
verificar_linea "Seccion .DATA" ".data"
verificar_linea "Seccion .CODE" ".code"
verificar_linea "Etiqueta start" "start:"
verificar_linea "ExitProcess" "invoke ExitProcess, 0"
verificar_linea "Fin end start" "end start"

# Simbolos en .DATA
verificar_linea "Variable x (DD)" "x_main DD ?"
verificar_linea "Variable a (DQ)" "a_main DQ ?"
verificar_linea "Constante 5 (DD)" "c_5_ul_ul DD 5"
verificar_linea "Temporal @aux3 (DD)" "@aux3 DD ?"
verificar_linea "Temporal @aux7 (DQ)" "@aux7 DQ ?"

# Instrucciones de aritmetica entera
verificar_linea "mov eax (carga constante)" "mov eax, c_5_ul_ul"
verificar_linea "add eax (suma enteros)" "add eax, c_3_ul_ul"
verificar_linea "imul eax (multiplicacion enteros)" "imul eax, y_main"

# Instrucciones FPU para doublef y todf
verificar_linea "fild (conversion entero a float)" "fild x_main"
verificar_linea "fstp (almacenar float)" "fstp @aux7"
verificar_linea "fld (carga float)" "fld @aux7"

echo
if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron en el .asm"
    exit 1
fi
echo "   OK: todas las instrucciones y secciones del .asm estan presentes."
echo

echo "=== Prueba del Prompt 3 de TP4 completada ==="
exit 0

#!/usr/bin/env bash
# Script de prueba para el Prompt 4 de TP4 (control de flujo, pout,
# funciones/metodos y objetos/atributos en ASM MASM32 32-bit).
#
# Verifica:
#  1. Compilacion del proyecto.
#  2. Los casos de test_tp_4/*.txt compilan sin errores semanticos.
#  3. Cada caso genera su .asm y contiene las instrucciones/directivas
#     esperadas para el tema que ejercita.
#  4. Que la salida TP3 (tokens, TS, tercetos) no cambie en un caso conocido:
#     el IR de repeat-while se mantiene y no aparecen errores.
#
# Los .txt se copian a un directorio temporal antes de compilar para no
# generar .asm dentro del repositorio.
#
# Uso: bash test_tp_4/probar_prompt4.sh

set -u

RAIZ="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BUILD="${TP4_BUILD:-/tmp/tp4p4_prompt4}"
TRABAJO="${TP4_TRABAJO:-/tmp/tp4p4_casos_prompt4}"
CASOS="$RAIZ/test_tp_4"

echo "=== Prompt 4 de TP4 - Prueba de control de flujo, pout, funciones y objetos ==="
echo "Raiz:   $RAIZ"
echo "Build:  $BUILD"
echo "Casos:  $TRABAJO"
echo

# 1. Compilar
echo "[1/5] Compilando proyecto..."
mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "FALLA: no se pudo compilar el proyecto."
    exit 1
fi
echo "   OK: compilacion exitosa."
echo

# 2. Preparar copia de los casos
echo "[2/5] Copiando casos a $TRABAJO..."
rm -rf "$TRABAJO"
mkdir -p "$TRABAJO"
cp "$CASOS"/[0-9][0-9]_*.txt "$TRABAJO"/
echo "   OK: $(find "$TRABAJO" -maxdepth 1 -name '[0-9][0-9]_*.txt' | wc -l) casos copiados."
echo

FALLOS=0

falla() {
    echo "   FALLA: $1"
    FALLOS=$((FALLOS + 1))
}

# Ejecuta un caso y devuelve la salida en TRABAJO/<caso>.salida
# Falla si el compilador reporta errores de compilacion.
ejecutar() {
    local caso="$1"
    local salida="$TRABAJO/$caso.salida"
    if ! java -cp "$BUILD" Main "$TRABAJO/$caso.txt" >"$salida" 2>&1; then
        falla "$caso.txt: el compilador termino con codigo distinto de 0"
        return 1
    fi
    if grep -q "Error:" "$salida"; then
        falla "$caso.txt: se reportaron errores de compilacion"
        grep "Error:" "$salida" | head -3
        return 1
    fi
    if [[ ! -f "$TRABAJO/$caso.asm" ]]; then
        falla "$caso.txt: no se genero $caso.asm"
        return 1
    fi
    return 0
}

verificar() {
    local caso="$1"
    local patron="$2"
    local descripcion="$3"
    if grep -qF -- "$patron" "$TRABAJO/$caso.asm"; then
        echo "   OK: $caso: $descripcion"
    else
        falla "$caso.asm: falta '$descripcion' ($patron)"
    fi
}

# Verifica que la primera aparicion de $2 aparezca antes que la de $3.
verificar_orden() {
    local caso="$1"
    local primero="$2"
    local segundo="$3"
    local descripcion="$4"
    local n1 n2
    n1="$(grep -nF -- "$primero" "$TRABAJO/$caso.asm" | head -1 | cut -d: -f1)"
    n2="$(grep -nF -- "$segundo" "$TRABAJO/$caso.asm" | head -1 | cut -d: -f1)"
    if [[ -n "$n1" && -n "$n2" && "$n1" -lt "$n2" ]]; then
        echo "   OK: $caso: $descripcion"
    else
        falla "$caso.asm: orden incorrecto ($descripcion)"
    fi
}

# 3. Ejecutar todos los casos de Prompt 4
echo "[3/5] Ejecutando casos de Prompt 4..."
for archivo in "$CASOS"/[0-9][0-9]_*.txt; do
    caso="$(basename "$archivo" .txt)"
    if ejecutar "$caso"; then
        echo "   OK: $caso.txt compila sin errores y genera $caso.asm"
    fi
done
echo

# 4. Verificar contenido ASM por caso
echo "[4/5] Verificando instrucciones y convenciones en los .asm..."

# 01 - if/else anidado
verificar 01_if_else_anidado "setb al" "comparacion < como setb"
verificar 01_if_else_anidado "sete al" "comparacion == como sete"
verificar 01_if_else_anidado "je label_3" "BF del if externo a la rama else"
verificar 01_if_else_anidado "jmp label_4" "BI de cierre del then"
verificar 01_if_else_anidado "label_1:" "etiqueta del else interno"

# 02 - repeat-while
verificar 02_repeat_while "label_1:" "etiqueta de inicio del ciclo"
verificar 02_repeat_while "je label_4" "BF de salida del while"
verificar 02_repeat_while "jmp label_1" "BI de vuelta al inicio del ciclo"
verificar_orden 02_repeat_while "label_1:" "je label_4" "el cuerpo del ciclo precede a la condicion"

# 03 - funciones y orden de parametros
verificar 03_funciones_orden_parametros "; FUNCION foo" "definicion de funcion etiquetada"
verificar 03_funciones_orden_parametros "call foo_main" "CALL a la funcion"
verificar 03_funciones_orden_parametros "mov @tp4_ret_foo_main, eax" "ret guarda en el slot de retorno"
verificar 03_funciones_orden_parametros "mov eax, @tp4_ret_foo_main" "el llamador recupera el retorno"
verificar_orden 03_funciones_orden_parametros "mov b_main_foo, eax" "mov a_main_foo, eax" \
    "PAR respeta el orden evaluado b antes que a"

# 04 - metodos heredados con CALLMET
verificar 04_metodo_heredado "; METODO m" "definicion de metodo etiquetada"
verificar 04_metodo_heredado "call m_main_cb" "llamada al metodo heredado de cb"
verificar 04_metodo_heredado "call m_main_cc" "llamada al metodo heredado de cc"
verificar 04_metodo_heredado "@tp4_ret_m_main_cb DD ?" "slot de retorno por clase cb"
verificar 04_metodo_heredado "@tp4_ret_m_main_cc DD ?" "slot de retorno por clase cc"
verificar_orden 04_metodo_heredado "mov pb_main_cb_m, eax" "mov pa_main_cb_m, eax" \
    "CALLMET respeta el orden [2,1]"

# 05 - atributos propios y heredados
verificar 05_atributos_propios_heredados "mov a1_x_main, eax" "escritura del atributo propio"
verificar 05_atributos_propios_heredados "mov a1_padre_y_main, eax" "escritura del atributo heredado prefijado"
verificar 05_atributos_propios_heredados "fstp a1_padre_z_main" "escritura doublef heredada"
verificar 05_atributos_propios_heredados "invoke printf, addr @tp4_fmt_uint, a1_padre_y_main" "pout de atributo entero"
verificar 05_atributos_propios_heredados "invoke printf, addr @tp4_fmt_double, a1_padre_z_main" "pout de atributo doublef"

# 06 - COPY estructural
verificar 06_copy_objetos "; COPY h1:main := b1:main" "COPY reconocido"
verificar 06_copy_objetos "mov eax, b1_a_main" "copia del primer atributo entero"
verificar 06_copy_objetos "mov h1_padre_x_main, eax" "destino del atributo heredado"
verificar 06_copy_objetos "fld b1_b_main" "copia del atributo doublef"
verificar 06_copy_objetos "fstp h1_y_main" "destino doublef"

# 07 - pout de los tres tipos
verificar 07_pout_tipos '@tp4_fmt_uint DB "%u",10,0' "formato de ulongint"
verificar 07_pout_tipos '@tp4_fmt_double DB "%.20Lf",10,0' "formato de doublef"
verificar 07_pout_tipos '@tp4_fmt_string DB "%s",10,0' "formato de cadena"
verificar 07_pout_tipos "invoke printf, addr @tp4_fmt_uint, x_main" "pout de variable ulongint"
verificar 07_pout_tipos "invoke printf, addr @tp4_fmt_double, d_main" "pout de variable doublef"
verificar 07_pout_tipos "invoke printf, addr @tp4_fmt_string, addr hola_mundo" "pout de cadena con addr"

# 08 - parametro formal de tipo clase
verificar 08_parametro_clase "mov p_x_main_leer, eax" "PAR copia atributo entero al formal"
verificar 08_parametro_clase "fstp p_y_main_leer" "PAR copia atributo doublef al formal"
verificar 08_parametro_clase "; COPY p:main:leer := a1:main" "copia estructural real->formal"
verificar 08_parametro_clase "mov @tp4_ret_leer_main, eax" "retorno de la funcion con parametro clase"
verificar 08_parametro_clase "call leer_main" "CALL a la funcion"

# 09 - retorno doublef
verificar 09_funcion_doublef "@tp4_ret_mitad_main DQ ?" "slot de retorno doublef en .DATA"
verificar 09_funcion_doublef "fdiv c_2_0" "division doublef con FPU en la funcion"
verificar 09_funcion_doublef "fstp @tp4_ret_mitad_main" "ret guarda el doublef"
verificar 09_funcion_doublef "fld @tp4_ret_mitad_main" "el llamador recupera el doublef"

# 10 - comparacion doublef
verificar 10_if_doublef "fcomp b_main" "comparacion doublef con fcomp"
verificar 10_if_doublef "fstsw ax" "transferencia de banderas FPU"
verificar 10_if_doublef "sahf" "banderas FPU a CF/PF/ZF"
verificar 10_if_doublef "seta al" "condicion > para doublef"

# 11 - funciones anidadas homonimas resueltas por ambito exacto
verificar 11_funciones_anidadas "c_main_a:" "label de c declarada en a"
verificar 11_funciones_anidadas "c_main_b:" "label de c declarada en b"
verificar 11_funciones_anidadas "call c_main_a" "llamada a la c de a"
verificar 11_funciones_anidadas "call c_main_b" "llamada a la c de b"
verificar 11_funciones_anidadas "@tp4_ret_c_main_a DD ?" "slot de retorno de c en a"
verificar 11_funciones_anidadas "@tp4_ret_c_main_b DD ?" "slot de retorno de c en b"
if [[ "$(grep -c '^c_main_a:' "$TRABAJO/11_funciones_anidadas.asm")" -eq 1 \
        && "$(grep -c '^c_main_b:' "$TRABAJO/11_funciones_anidadas.asm")" -eq 1 ]]; then
    echo "   OK: 11_funciones_anidadas: cada label de funcion aparece una sola vez"
else
    falla "11_funciones_anidadas.asm: labels duplicados o ausentes para c anidada"
fi

# Marcadores CLASE/FIN_CLASE no generan instrucciones ejecutables.
if grep -q "FIN_CLASE" "$TRABAJO/06_copy_objetos.asm"; then
    falla "06_copy_objetos.asm: FIN_CLASE no debe emitir codigo"
else
    echo "   OK: 06_copy_objetos: CLASE/FIN_CLASE son no-op"
fi

echo

# 5. Regresion TP3: el IR y la salida de consola no cambian
echo "[5/5] Regresion TP3 sobre repeat-while..."
CASO_TP3="$RAIZ/test/TP3_y_TP4/ITERACION/01_repeat_simple.txt"
if [[ ! -f "$CASO_TP3" ]]; then
    falla "no existe el caso de regresion $CASO_TP3"
else
    cp "$CASO_TP3" "$TRABAJO/regresion_repeat.txt"
    if java -cp "$BUILD" Main "$TRABAJO/regresion_repeat.txt" >"$TRABAJO/regresion.salida" 2>&1; then
        echo "   OK: el caso TP3 compila"
    else
        falla "el caso TP3 termino con codigo distinto de 0"
    fi
    if grep -q "Error:" "$TRABAJO/regresion.salida"; then
        falla "el caso TP3 reporto errores"
    else
        echo "   OK: sin errores"
    fi
    if grep -qF "(BF, [5], label_2)" "$TRABAJO/regresion.salida"; then
        echo "   OK: el terceto BF del repeat se mantiene"
    else
        falla "cambio el terceto BF esperado del repeat"
    fi
    if grep -qF "(BI, label_1, --)" "$TRABAJO/regresion.salida"; then
        echo "   OK: el terceto BI del repeat se mantiene"
    else
        falla "cambio el terceto BI esperado del repeat"
    fi
    if grep -qF "Contenido de la tabla de simbolos:" "$TRABAJO/regresion.salida" \
            && grep -qF "Código intermedio:" "$TRABAJO/regresion.salida"; then
        echo "   OK: la salida TP3 (TS y tercetos) sigue presente"
    else
        falla "falta la salida TP3 de TS/tercetos"
    fi
    if [[ -f "$TRABAJO/regresion_repeat.asm" ]]; then
        echo "   OK: el caso TP3 tambien genera .asm"
    else
        falla "el caso TP3 no genero .asm"
    fi
fi

echo
if [[ $FALLOS -gt 0 ]]; then
    echo "RESULTADO: $FALLOS verificacion(es) fallaron"
    exit 1
fi
echo "RESULTADO: todas las verificaciones del Prompt 4 pasaron."
echo "=== Prueba del Prompt 4 de TP4 completada ==="
exit 0

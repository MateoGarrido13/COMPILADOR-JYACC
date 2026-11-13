#!/usr/bin/env bash
# Ejecuta los casos de test/TP3_SEMANTICAS contra el compilador del TP3.
#
# Cada archivo .txt tiene un .esperado con reglas de una linea:
#   SIN ERRORES            la salida no debe contener "Error:"
#   ERROR:<texto>          la salida debe contener <texto>
#   SIMBOLO:<clave>        la tabla debe tener "<clave> -> 300"
#   NO_SIMBOLO:<clave>     la tabla no debe tener "<clave> -> 300"
#   DECLARADO_SI:<clave>   la entrada <clave> debe tener "Declarado: si"
#   DECLARADO_NO:<clave>   la entrada <clave> debe tener "Declarado: no"
#   TERCETO:<texto>        la salida debe contener <texto>
set -u

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
RAIZ="$(cd "$DIR/../.." && pwd)"
BUILD="${TP3_BUILD:-/tmp/opencode/tp3build}"

mkdir -p "$BUILD"
if ! javac -encoding UTF-8 -d "$BUILD" \
        "$RAIZ"/*.java "$RAIZ"/"acciones semanticas"/*.java "$RAIZ"/otras_acciones/*.java; then
    echo "No se pudo compilar el proyecto."
    exit 1
fi

fallos=0
casos=0

estado_declaracion() {
    local clave="$1"
    awk -v clave="$clave" '
        $0 == clave " -> 300" { encontrado = 1; next }
        encontrado && /^Declarado: / { print $2; exit }
    '
}

for esperado in "$DIR"/prompt-*/*.esperado; do
    caso="${esperado%.esperado}.txt"
    casos=$((casos + 1))
    salida="$(java -cp "$BUILD" Main "$caso" 2>&1)"
    errores_caso=0

    while IFS= read -r regla; do
        case "$regla" in
            ""|\#*) continue ;;
            "SIN ERRORES")
                if grep -q "Error:" <<<"$salida"; then
                    echo "  fallo: se esperaban cero errores"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            ERROR:*)
                if ! grep -Fq -- "${regla#ERROR:}" <<<"$salida"; then
                    echo "  fallo: falta error '${regla#ERROR:}'"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            SIMBOLO:*)
                if ! grep -Fq -- "${regla#SIMBOLO:} -> 300" <<<"$salida"; then
                    echo "  fallo: falta simbolo '${regla#SIMBOLO:}'"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            NO_SIMBOLO:*)
                if grep -Fq -- "${regla#NO_SIMBOLO:} -> 300" <<<"$salida"; then
                    echo "  fallo: sobra simbolo '${regla#NO_SIMBOLO:}'"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            DECLARADO_SI:*)
                if [[ "$(estado_declaracion "${regla#DECLARADO_SI:}" <<<"$salida")" != "si" ]]; then
                    echo "  fallo: '${regla#DECLARADO_SI:}' no esta declarado"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            DECLARADO_NO:*)
                if [[ "$(estado_declaracion "${regla#DECLARADO_NO:}" <<<"$salida")" != "no" ]]; then
                    echo "  fallo: '${regla#DECLARADO_NO:}' figura como declarado"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            TERCETO:*)
                if ! grep -Fq -- "${regla#TERCETO:}" <<<"$salida"; then
                    echo "  fallo: falta terceto '${regla#TERCETO:}'"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            *)
                echo "  fallo: regla desconocida '$regla'"
                errores_caso=$((errores_caso + 1))
                ;;
        esac
    done < "$esperado"

    if [[ $errores_caso -eq 0 ]]; then
        echo "OK   ${caso#"$DIR"/}"
    else
        echo "FALLA ${caso#"$DIR"/}"
        fallos=$((fallos + 1))
    fi
done

echo
echo "$((casos - fallos))/$casos casos OK"
[[ $fallos -eq 0 ]]

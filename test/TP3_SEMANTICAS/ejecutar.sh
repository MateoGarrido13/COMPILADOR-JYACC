#!/usr/bin/env bash
# Ejecuta los casos de test/TP3_SEMANTICAS contra el compilador del TP3.
#
# Cada archivo .txt contiene el programa y, al final, un comentario multilinea
# {{ ... }} con las reglas. La primera linea del comentario es:
#   --- ESPERADO ---
# Las reglas son una por linea:
#   SIN ERRORES            la salida no debe contener "Error:"
#   ERROR:<texto>          la salida debe contener <texto>
#   SIMBOLO:<clave>        la tabla debe tener "<clave> -> 300"
#   NO_SIMBOLO:<clave>     la tabla no debe tener "<clave> -> 300"
#   DECLARADO_SI:<clave>   la entrada <clave> debe tener "Declarado: si"
#   DECLARADO_NO:<clave>   la entrada <clave> debe tener "Declarado: no"
#   TERCETO:<texto>        la salida debe contener <texto>
#   SIN TERCETOS           la salida no debe tener tercetos numerados
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

TMP="$(mktemp -d "${TMPDIR:-/tmp}/tp3sem.XXXXXX")"
trap 'rm -rf "$TMP"' EXIT

fallos=0
casos=0

estado_declaracion() {
    local clave="$1"
    awk -v clave="$clave" '
        $0 == clave " -> 300" { encontrado = 1; next }
        encontrado && /^Declarado: / { print $2; exit }
    '
}

for caso in "$DIR"/prompt-*/*.txt; do
    casos=$((casos + 1))
    nombre="${caso#"$DIR"/}"
    entrada="$TMP/entrada.txt"
    reglas="$TMP/reglas.txt"

    linea_marca="$(grep -n -x -- '--- ESPERADO ---' "$caso" | head -n 1 | cut -d: -f1)"
    if [[ -z "$linea_marca" ]]; then
        echo "FALLA $nombre"
        echo "  fallo: falta la linea '--- ESPERADO ---'"
        fallos=$((fallos + 1))
        continue
    fi

    # Las reglas viven dentro de un comentario multilinea {{ ... }} para que el
    # archivo tambien se pueda compilar directamente sin ensuciar la tabla.
    linea_llave=$((linea_marca - 1))
    if [[ "$(sed -n "${linea_llave}p" "$caso")" != "{{" ]]; then
        echo "FALLA $nombre"
        echo "  fallo: falta '{{' en la linea anterior a '--- ESPERADO ---'"
        fallos=$((fallos + 1))
        continue
    fi
    linea_cierre="$(awk -v desde="$linea_marca" 'NR > desde && /^\}\}$/ {print NR; exit}' "$caso")"
    if [[ -z "$linea_cierre" ]]; then
        echo "FALLA $nombre"
        echo "  fallo: falta '}}' que cierra el bloque de reglas"
        fallos=$((fallos + 1))
        continue
    fi

    # El comentario de reglas no se le pasa al compilador.
    head -n $((linea_llave - 1)) "$caso" > "$entrada"
    sed -n "$((linea_marca + 1)),$((linea_cierre - 1))p" "$caso" > "$reglas"

    salida="$(java -cp "$BUILD" Main "$entrada" 2>&1)"
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
            "SIN TERCETOS")
                if grep -Eq '^[0-9]+\. \(' <<<"$salida"; then
                    echo "  fallo: se esperaban cero tercetos"
                    errores_caso=$((errores_caso + 1))
                fi
                ;;
            *)
                echo "  fallo: regla desconocida '$regla'"
                errores_caso=$((errores_caso + 1))
                ;;
        esac
    done < "$reglas"

    if [[ $errores_caso -eq 0 ]]; then
        echo "OK   $nombre"
    else
        echo "FALLA $nombre"
        fallos=$((fallos + 1))
    fi
done

echo
echo "$((casos - fallos))/$casos casos OK"
[[ $fallos -eq 0 ]]

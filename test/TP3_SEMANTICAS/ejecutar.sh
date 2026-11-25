#!/usr/bin/env bash
# Ejecuta los casos de test/TP3_SEMANTICAS contra el compilador del TP3.
#
# Cada .txt tiene un bloque "{{ ... }}" al principio con la salida esperada
# descriptiva y el programa despues. El runner extrae ese bloque, compila solo
# el programa y comprueba:
#   - cada terceto "N. (...)" esperado figura en el stdout;
#   - cada entrada "- clave -> tipo, uso" figura en la tabla con el token
#     correspondiente (300 variable, 302 constante, 303 cadena);
#   - cada mensaje bajo "Errores:" figura en el stdout;
#   - "Sin errores." implica que no aparece "Error:".
#
# Las frases explicativas del bloque no son reglas y se ignoran.
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

datos_simbolo() {
    local clave="$1" token="$2"
    awk -v clave="$clave" -v token="$token" '
        $0 == clave " -> " token {
            encontrado = 1; tipo = ""; uso = ""; decl = ""; next
        }
        encontrado {
            if ($0 ~ /^Tipo de dato: /) {
                tipo = $0; sub(/^Tipo de dato: /, "", tipo)
            } else if ($0 ~ /^Uso: /) {
                uso = $0; sub(/^Uso: /, "", uso)
            } else if ($0 ~ /^Declarado: /) {
                decl = $0; sub(/^Declarado: /, "", decl)
            } else if ($0 ~ /^-+$/) {
                print tipo "|" uso "|" decl
                impreso = 1
                exit
            }
        }
        END {
            if (encontrado && !impreso) print tipo "|" uso "|" decl
        }
    ' <<<"$salida"
}

for caso in "$DIR"/prompt-*/*.txt; do
    casos=$((casos + 1))
    nombre="${caso#"$DIR"/}"
    entrada="$TMP/entrada.txt"
    reglas="$TMP/reglas.txt"

    linea_inicio="$(grep -n -m1 -- '^{{' "$caso" | head -n 1 | cut -d: -f1)"
    if [[ -z "$linea_inicio" ]]; then
        echo "FALLA $nombre"
        echo "  fallo: falta el bloque '{{' inicial"
        fallos=$((fallos + 1))
        continue
    fi

    linea_cierre="$(awk -v desde="$linea_inicio" \
        'NR > desde && /^\}\}$/ { print NR; exit }' "$caso")"
    if [[ -z "$linea_cierre" ]]; then
        echo "FALLA $nombre"
        echo "  fallo: falta '}}' que cierra el bloque esperado"
        fallos=$((fallos + 1))
        continue
    fi

    sed -n "$((linea_inicio + 1)),$((linea_cierre - 1))p" "$caso" > "$reglas"
    tail -n "+$((linea_cierre + 1))" "$caso" > "$entrada"

    salida="$(java -cp "$BUILD" Main "$entrada" 2>&1)"
    errores_caso=0
    seccion=""

    while IFS= read -r regla; do
        case "$regla" in
            ""|\#*) continue ;;
            "Tercetos:")
                seccion="tercetos"
                continue
                ;;
            Variables\ de\ la\ tabla\ de\ simbolos*)
                seccion="variables"
                continue
                ;;
            Constantes\ de\ la\ tabla\ de\ simbolos*)
                seccion="constantes"
                continue
                ;;
            Cadenas\ de\ la\ tabla\ de\ simbolos*)
                seccion="cadenas"
                continue
                ;;
            "Errores:")
                seccion="errores"
                continue
                ;;
            "Sin errores."|"Sin errores")
                if grep -q "Error:" <<<"$salida"; then
                    echo "  fallo: se esperaban cero errores"
                    errores_caso=$((errores_caso + 1))
                fi
                continue
                ;;
        esac

        if [[ "$regla" =~ ^[0-9]+\.[[:space:]] ]]; then
            # La numeracion esperada no siempre coincide: algunos bloques
            # omiten delimitadores o ret. Se compara el texto del terceto.
            texto="${regla#*. }"
            if ! grep -Fq -- "$texto" <<<"$salida"; then
                echo "  fallo: falta terceto '$texto'"
                errores_caso=$((errores_caso + 1))
            fi
            continue
        fi

        if [[ "$seccion" == "errores" && "$regla" == "- "* ]]; then
            mensaje="${regla#- }"
            if ! grep -Fq -- "$mensaje" <<<"$salida"; then
                echo "  fallo: falta error '$mensaje'"
                errores_caso=$((errores_caso + 1))
            fi
            continue
        fi

        if [[ "$seccion" == "variables" || "$seccion" == "constantes" || "$seccion" == "cadenas" ]] \
           && [[ "$regla" == "- "* && "$regla" == *" -> "* && "$regla" == *", "* ]]; then
            resto="${regla#- }"
            clave="${resto%% -> *}"
            valor="${resto#* -> }"
            tipo="${valor%%,*}"
            uso="${valor#*, }"
            case "$seccion" in
                variables) token="300" ;;
                constantes) token="302" ;;
                cadenas) token="303" ;;
            esac
            obtenido="$(datos_simbolo "$clave" "$token")"
            IFS='|' read -r obt_tipo obt_uso obt_decl <<<"$obtenido"
            declaracion="si"
            if [[ "$tipo" == "indefinido" && "$uso" == "indefinido" ]]; then
                declaracion="no"
            fi
            if [[ "$tipo" == "tipo enumerado" && "$uso" == "tipo" ]]; then
                # Los typedef de enum se esperan como "tipo enumerado, tipo":
                # el tipo de dato real es el base (ulongint) y el uso es el
                # que distingue a la entrada.
                esperado="tipo enumerado|si"
                if [[ "$obt_uso|$obt_decl" != "$esperado" ]]; then
                    echo "  fallo: simbolo '$clave' ($token): se esperaba uso/declarado '$esperado', se obtuvo '$obt_uso|$obt_decl'"
                    errores_caso=$((errores_caso + 1))
                fi
            else
                esperado="$tipo|$uso|$declaracion"
                if [[ "$obtenido" != "$esperado" ]]; then
                    echo "  fallo: simbolo '$clave' ($token): se esperaba '$esperado', se obtuvo '$obtenido'"
                    errores_caso=$((errores_caso + 1))
                fi
            fi
            continue
        fi

        # El resto del bloque es prosa descriptiva, no una regla ejecutable.
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

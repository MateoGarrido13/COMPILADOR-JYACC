# PROMPT-0: baseline reproducible

## Objetivo

Registrar el estado del compilador antes de implementar las semanticas faltantes de TP3,
para poder comparar cada cambio contra una referencia estable.

## Alcance y metodo

- Fecha del relevamiento: 2026-11-13.
- Commit base: `edb23fe Semanticas faltantes`, con cambios locales en `Entorno.java`,
  `TablaSimbolos.java` y `Verificaciones.java` que forman parte de este relevamiento.
- Compilacion:

  ```bash
  javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
  ```

- Ejecucion de cada caso:

  ```bash
  java -cp /tmp/opencode/tp3build Main <archivo>
  ```

- Se recorrieron todos los `.txt` de:
  - `test/TP3 _y_TP4/AMBITO`
  - `test/TP3 _y_TP4/ASIGNACIONES`
  - `test/TP3 _y_TP4/ITERACION`
  - `test/TP3 _y_TP4/SELECCION`
  - `test/tp2/verificacion_r17_asig_expresiones`
  - `test/tp2/verificacion_r19_param_reales`
  - `test/tp2/verificacion_r23_enums`
  - `test/tp2/verificacion_r28_r31_atributos`
  - `test/tp2/verificacion_r33_conversion`
- Total: **65 casos**.
- Cada salida completa quedo guardada en `/tmp/opencode/tp3-baseline/`, conservando la
  ruta relativa del fuente. Esa carpeta es la referencia reproducible del baseline.

## Criterio de validez

La columna **Esperado** indica el comportamiento que deberia tener el caso despues de
completar las semanticas de TP3, no necesariamente el comportamiento actual.

- **Valido**: el programa debe compilar sin errores semanticos y, cuando corresponda,
  generar los tercetos del tema.
- **Invalido**: el programa debe informar al menos un error semantico o sintactico.
- **Mixto**: el archivo contiene sentencias validas e invalidas a proposito; el error
  esperado esta indicado en el comentario del fuente.

## Resumen por directorio

| Directorio | Casos | Validos | Invalidos | Mixtos |
|---|---:|---:|---:|---:|
| `test/TP3 _y_TP4/ITERACION` | 11 | 11 | 0 | 0 |
| `test/TP3 _y_TP4/ASIGNACIONES` | 9 | 4 | 3 | 2 |
| `test/TP3 _y_TP4/SELECCION` | 10 | 10 | 0 | 0 |
| `test/TP3 _y_TP4/AMBITO` | 6 | 3 | 2 | 1 |
| `test/tp2/verificacion_r17_asig_expresiones` | 8 | 2 | 6 | 0 |
| `test/tp2/verificacion_r19_param_reales` | 5 | 1 | 4 | 0 |
| `test/tp2/verificacion_r23_enums` | 5 | 2 | 3 | 0 |
| `test/tp2/verificacion_r28_r31_atributos` | 6 | 4 | 1 | 1 |
| `test/tp2/verificacion_r33_conversion` | 5 | 2 | 3 | 0 |
| **Total** | **65** | **39** | **22** | **4** |

> Nota: `metodo_heredado_prefijado_un_parametro.txt` queda como mixto bajo la decision
> D3 revisada: las llamadas prefijadas son validas y las tradicionales `a1.m(...)`
> deben pasar a ser error.

## Tabla de casos

Las columnas de errores y tercetos resumen la salida actual. Los textos completos estan
en `/tmp/opencode/tp3-baseline/<misma ruta relativa>`.

| Caso | Errores actuales | Tercetos actuales | Esperado |
|---|---|---|---|
| `ITERACION/00_prueba_unasentencia.txt` | Ninguno | 7; ciclo `label_1` + `BF` + `BI` | Valido |
| `ITERACION/00_prueba_varias_sentencias.txt` | Ninguno | 9; dos asignaciones en el cuerpo | Valido |
| `ITERACION/01_repeat_simple.txt` | Ninguno | 9; ciclo y asignacion posterior | Valido |
| `ITERACION/02_repeat_bloque.txt` | Ninguno | 10; cuerpo `begin-end` | Valido |
| `ITERACION/03_repeat_condicion_expresion.txt` | Ninguno | 10; condicion con producto | Valido |
| `ITERACION/04_repeat_anidado.txt` | Ninguno | 15; dos ciclos anidados | Valido |
| `ITERACION/05_if_dentro_de_repeat.txt` | Ninguno | 15; `if-else` dentro del ciclo | Valido |
| `ITERACION/06_repeat_dentro_de_if_else.txt` | Ninguno | 14; ciclo en el `then` | Valido |
| `ITERACION/07_repeat_dentro_de_if_sin_else.txt` | Ninguno | 11; ciclo en el `then` sin `else` | Valido |
| `ITERACION/08_if_sin_else_y_sentencia_en_repeat.txt` | Ninguno | 13; `if` y sentencia dentro del ciclo | Valido |
| `ITERACION/09_repeats_secuenciales.txt` | Ninguno | 15; dos ciclos consecutivos | Valido |
| `ASIGNACIONES/01_sumas_plegado_izquierda.txt` | Ninguno | 9; plegado a izquierda | Valido |
| `ASIGNACIONES/02_unarios.txt` | `-4$ul` fuera de rango; asignacion mal formada | 13; unarios y constantes con signo | Mixto |
| `ASIGNACIONES/03_calculos_combinados.txt` | Ninguno | 25; precedencia y asociatividad | Valido |
| `ASIGNACIONES/04_borde_doble_mas.txt` | Varios errores de operando faltante | 7; recuperacion de `a++1$ul` | Mixto |
| `ASIGNACIONES/05_borde_mas_negativo.txt` | Operando faltante y `-4$ul` fuera de rango | 4; recuperacion sin tercetos de suma | Invalido |
| `ASIGNACIONES/06_alternancia_constantes_variables.txt` | Ninguno | 30; constantes y variables mezcladas | Valido |
| `ASIGNACIONES/07_ulongint_limite_mas_uno_dividido_dos.txt` | Constantes fuera de rango; asignaciones mal formadas | Ninguno | Invalido |
| `ASIGNACIONES/08_ulongint_limite_dividido_dos_en_rango.txt` | Ninguno | 3; division dentro de rango | Valido |
| `ASIGNACIONES/09_lado_izquierdo_no_asignable.txt` | Constante invalida; sentencia mal formada | 1; asignacion valida previa | Invalido |
| `SELECCION/01_if_else_simple.txt` | Ninguno | 10; `BF` + `BI` | Valido |
| `SELECCION/02_if_else_con_codigo_alrededor.txt` | Ninguno | 9; tercetos antes y despues | Valido |
| `SELECCION/03_if_sin_else.txt` | Ninguno | 7; solo `BF` | Valido |
| `SELECCION/04_if_else_bloques.txt` | Ninguno | 12; bloques sin tercetos propios | Valido |
| `SELECCION/05_if_anidado_en_then.txt` | Ninguno | 13; `if` anidado en `then` | Valido |
| `SELECCION/06_if_anidado_en_else.txt` | Ninguno | 13; `if` anidado en `else` | Valido |
| `SELECCION/07_condicion_con_expresion.txt` | Ninguno | 11; condicion con expresiones | Valido |
| `SELECCION/08_comparadores.txt` | Ninguno | 25; seis comparadores | Valido |
| `SELECCION/09_if_sin_else_bloque.txt` | Ninguno | 8; bloque en `then` | Valido |
| `SELECCION/10_if_else_secuenciales.txt` | Ninguno | 15; dos `if-else` | Valido |
| `AMBITO/aa_ambito.txt` | Faltan `begin` y nombre de programa | Ninguno | Invalido |
| `AMBITO/prueba_funcion_anidada.txt` | Ninguno | Ninguno | Valido |
| `AMBITO/prueba_funciones_iguales.txt` | `var_1` fuera de alcance; falta `;` | 3; dos `fun_c` y `var_2` | Mixto |
| `AMBITO/prueba_local_eliminada.txt` | Ninguno | 1; `local := n` | Valido |
| `AMBITO/prueba_variable_en_main.txt` | Ninguno | 2; global modificada en funcion | Valido |
| `AMBITO/test_var_no_declarada copy.txt` | `secreta` y `fantasma` fuera de alcance | 5; incluye usos invalidos | Invalido |
| `r17/anidamiento.txt` | Anidamiento de asignacion | 1; solo asignacion externa | Invalido |
| `r17/anidamiento_indirecto.txt` | Anidamiento de asignacion | 2; expresion y asignacion externa | Invalido |
| `r17/en_invocacion.txt` | Ninguno | 3; `PAR` + `CALL` | Valido |
| `r17/estructura_respetada.txt` | Ninguno | 7; asignacion en expresion y `if` | Valido |
| `r17/igual_como_asignacion_sentencia.txt` | Sentencia mal formada | Ninguno | Invalido |
| `r17/parentesis_anidados.txt` | Asignacion mal formada | Ninguno | Invalido |
| `r17/sin_parentesis.txt` | Asignacion mal formada | Ninguno | Invalido |
| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde va `=` | 1 | Invalido |
| `r19/estructura_respetada.txt` | Ninguno | 10; `PAR`/`CALL` sin copia a formales | Valido |
| `r19/orden_sin_corchetes.txt` | Falta orden; errores sintacticos | 2; asignaciones desde `foo` | Invalido |
| `r19/sin_orden.txt` | Falta orden en dos llamadas | 2; asignaciones desde `foo` | Invalido |
| `r19/futuros/orden_fuera_de_rango.txt` | Posicion fuera de rango; cantidad distinta | 8; llamadas con orden parcial | Invalido |
| `r19/futuros/orden_repetido.txt` | Posicion duplicada; cantidad distinta | 10; llamadas con orden repetido | Invalido |
| `r23/dos_tipos_validos.txt` | Ninguno | 2; inicializaciones | Valido |
| `r23/enum_vacio.txt` | Ausencia de valores | Ninguno | Invalido |
| `r23/estructura_respetada.txt` | Ninguno | 3; asignaciones | Valido |
| `r23/nombre_tipo_primitivo.txt` | Faltan `begin`/nombre de programa | Ninguno | Invalido |
| `r23/tipos_difieren_mayusculas.txt` | Identificador invalido y errores sintacticos | Ninguno | Invalido |
| `r28_r31/clase_con_atributos.txt` | No se puede asignar a `a1.x`/`a1.z` | Ninguno | Valido |
| `r28_r31/clase_sin_atributos.txt` | Ninguno | Ninguno | Valido |
| `r28_r31/desambiguado_tradicional_y.txt` | No se puede asignar a `a1.padre.y`/`a1.hija.y` | Ninguno | Valido |
| `r28_r31/metodo_heredado_prefijado_con_orden.txt` | Ninguno | 4; asignaciones desde `m` | Valido |
| `r28_r31/metodo_heredado_prefijado_sin_orden.txt` | Tres faltas de orden | 3; asignaciones desde `a1` | Invalido |
| `r28_r31/metodo_heredado_prefijado_un_parametro.txt` | Ninguno | 6; llamadas tratadas como asignaciones | Mixto |
| `r33/conversion_vacia.txt` | Asignacion mal formada | Ninguno | Invalido |
| `r33/en_asignacion.txt` | Ninguno | 4; `todf` no genera conversion | Valido |
| `r33/en_condicion_if.txt` | Ninguno | 5; `todf` no genera conversion | Valido |
| `r33/falta_paren_cierre.txt` | Asignacion mal formada | 1 | Invalido |
| `r33/lado_izquierdo.txt` | Sentencia mal formada (dos veces) | 1 | Invalido |

## Hallazgos que justifican las fases siguientes

1. **Ambitos**: las claves actuales usan `MAIN` en mayuscula y las funciones se apilan
   con el nombre en mayuscula. La decision D1 pide minusculas (`main:f:g`).
2. **Cierre de ambitos**: `MonitorAmbito` elimina las entradas al salir. La decision D5
   pide conservarlas y resolver por cadena de ambitos.
3. **Uso de identificadores**: `EntradaTabla` no distingue declaracion de uso. Sin esa
   marca, un uso de un ambito externo crea una entrada que tapa la declaracion real.
4. **Atributos y metodos**: los accesos `objeto.atributo` y las llamadas a metodos no
   generan tercetos ni verifican que el miembro exista. La decision D3 revisada exige
   prefijo explicito para miembros heredados.
5. **Tipos**: no hay igualdad estricta ni conversion implicita; `todf` no emite terceto.
6. **Llamadas**: se emiten `PAR` y `CALL`, pero falta la copia a parametros formales y
   la validacion de tipos.
7. **Funciones sin retorno**: el generador no emite `ret` ni delimitadores por funcion.

## Reproduccion

```bash
rm -rf /tmp/opencode/tp3build /tmp/opencode/tp3-baseline
mkdir -p /tmp/opencode/tp3build
javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java

BASE=/tmp/opencode/tp3-baseline
for dir in \
  "test/TP3 _y_TP4/AMBITO" \
  "test/TP3 _y_TP4/ASIGNACIONES" \
  "test/TP3 _y_TP4/ITERACION" \
  "test/TP3 _y_TP4/SELECCION" \
  "test/tp2/verificacion_r17_asig_expresiones" \
  "test/tp2/verificacion_r19_param_reales" \
  "test/tp2/verificacion_r23_enums" \
  "test/tp2/verificacion_r28_r31_atributos" \
  "test/tp2/verificacion_r33_conversion"; do
  find "$dir" -type f -name '*.txt' -print0 | while IFS= read -r -d '' f; do
    out="$BASE/${f#./}"
    mkdir -p "$(dirname "$out")"
    java -cp /tmp/opencode/tp3build Main "$f" > "$out" 2>&1
  done
done
```

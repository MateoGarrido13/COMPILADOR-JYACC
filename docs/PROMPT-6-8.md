# PROMPT-6-8: Constantes `ulongint` y `doublef`

## Objetivo

Tipar cada constante en el momento del análisis léxico, para que el tipo quede registrado en la Tabla de Símbolos durante el análisis léxico y no recién al usarla como factor. Mantener rango y formato existentes.

## Cambios realizados

### `acciones semanticas/AccionConstanteEntera.java`

- Antes: `buscarOInsertarConstante(lexema, Globals.CONSTANTE_NUMERICA)` — insertaba sin asignar `tipoDato`.
- Después: `insertarConstante(lexema, Tipos.ULONGINT, Globals.numeroLinea)` — asigna `tipoDato = ulongint`, `uso = CONSTANTE`, `declarado = true`.

### `acciones semanticas/AccionConstanteFloat.java`

- Antes: `buscarOInsertarConstante(lexema, Globals.CONSTANTE_NUMERICA)` — insertaba sin asignar `tipoDato`.
- Después: `insertarConstante(lexema, Tipos.DOUBLEF, Globals.numeroLinea)` — asigna `tipoDato = doublef`, `uso = CONSTANTE`, `declarado = true`.
- Se conserva el caso especial lexema `"."` que devuelve el token `'.'` para acceso a atributos; en ese caso no se inserta nada.

### `TablaSimbolos.java`

- Se eliminó `buscarOInsertarConstante(String, int)`, que quedó sin uso y permitía volver a crear constantes sin tipo. `insertarConstante(String, String, int)` es ahora el único punto de alta de constantes numéricas y exige el tipo.
- Las constantes y las cadenas usan claves internas con prefijo (`$const:` y `$cadena:`) distintas de su lexema visible. Sin esto, una cadena como `{if}`, `{1$ul}` o `{x:main}` reutilizaba la entrada de una palabra reservada, de una constante o de una variable y le pisaba tipo y uso. El lexema que muestran la TS y el codegen sigue siendo el original.
- `EntradaTabla.lexema` conserva el texto visible; el prefijo sólo vive en la clave del mapa.

### No se modificó

- `Tipos.rangoValido()`: los rangos y formatos se mantienen sin cambios.
- `Tipos.tipoDeConstante()`: queda como fallback, pero la fuente principal es el tipo asignado en el léxico.
- `Entorno.aplicarSignoNegativo()`: arma el lexema con signo a partir de una constante ya reconocida y le pone el tipo que devuelve `Tipos.tipoDeConstante`. No es el camino de `-1$ul`: ese lexema lo rechaza el léxico antes de insertar.

## Criterios de aceptación

- Al compilar `1$ul`, la TS muestra Tipo de dato: `ulongint`, Uso: `constante`. ✅
- Al compilar `2.5d+3`, la TS muestra Tipo de dato: `doublef`, Uso: `constante`. ✅
- Al compilar `-1$ul`, no hay entrada en la TS. El error es `Constante entera fuera de rango '-1$ul'`. ✅
- Los tests `test/tp1/verificacion_constantes_ulongint/*` y `test/tp1/verificacion_constantes_doublef/*` siguen pasando. ✅
- Un uso posterior de la constante conserva el tipo sin depender de `tiparConstante`. ✅

## Tests de regresión de la iteración 3.1

Se agregó `test/TP3_SEMANTICAS/prompt-3-1-constantes/` con el formato descriptivo usado por el resto de TP3_SEMANTICAS (comentario multilínea al principio con tercetos y tabla de símbolos esperados):

| Archivo | Qué verifica |
|---|---|
| `01_ulongint_tipada_en_lexico.txt` | `1$ul` queda como `ulongint`, uso constante, y llega al terceto. |
| `02_doublef_tipada_en_lexico.txt` | `2.5d+3` queda como `doublef`, uso constante. |
| `03_negativa_ulongint_fuera_de_rango.txt` | `-1$ul` no se inserta. Error: `Constante entera fuera de rango '-1$ul'`. |
| `04_negativa_doublef_valida.txt` | El lexema `-1.5` queda como `doublef`. No se crea además `1.5`. Sin error. |
| `05_limites_validos_de_ambos_tipos.txt` | Extremos válidos de ambos tipos, tipados en el léxico. |
| `06_cadena_y_constante_mismo_lexema.txt` | `{1$ul}` y `1$ul` conviven sin pisarse: cadena 303 y constante 302 con sus tipos. |

## Verificación

Compilación limpia con `javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java`.

Salidas comprobadas:
- `1$ul -> 302`, `Tipo de dato: ulongint`, `Uso: constante`.
- `2.5d+3 -> 302`, `Tipo de dato: doublef`, `Uso: constante`.
- `-1$ul` no aparece en la TS. `Error: Constante entera fuera de rango '-1$ul'`. La asignación que lo usa queda `Asignacion mal formada`.
- `{1$ul}` genera `1$ul -> 303`, `Tipo de dato: cadena`, y no altera la constante `1$ul -> 302`.

Tests existentes ejecutados sin regresiones:
- `test/tp1/verificacion_constantes_ulongint/limites_validos.txt`
- `test/tp1/verificacion_constantes_ulongint/fuera_de_rango.txt`
- `test/tp1/verificacion_constantes_doublef/limites_validos.txt`
- `test/tp1/verificacion_constantes_doublef/fuera_de_rango.txt`
- `test/tp1/verificacion_constantes_doublef/chequeo_formatos.txt`


# Revisión de las iteraciones 3.1 y 3.2

## Alcance

Revisión de los commits:

- `a201aee` — Prompt 6 y 8: tipado de constantes `ulongint` y `doublef` en el léxico.
- `f519e2b` — tests de cadenas y comentarios.
- `c43ce85` — validación del Prompt 9 y 16.

También se tuvo en cuenta el estado del árbol de trabajo al momento de la revisión (cambios sin commitear en `Entorno.java`, `MatrizTransiciones.java`, `Verificaciones.java` y `otras_acciones/AccionesExpresiones.java`).

## Resultado general

| Prompt | Estado | Cambios de código | Tests nuevos |
|---|---|---|---|
| 3.1 — Temas 6 y 8 (`ulongint`, `doublef`) | Cumple, con mejoras de calidad | Sí | `test/TP3_SEMANTICAS/prompt-3-1-constantes/` (6 casos) |
| 3.2 — Temas 9 y 16 (cadenas y comentarios) | Cumple, con un defecto de colisión corregido | Sí, en `TablaSimbolos.java` | `08_cadena_no_pisa_otros_simbolos.txt` |

## Prompt 3.1 — Constantes tipadas en el léxico

### Requisitos

| Requisito | Verificación |
|---|---|
| `AccionConstanteEntera` inserta con `Tipos.ULONGINT` después de validar formato y rango | ✅ `insertarConstante(lexema, Tipos.ULONGINT, ...)` |
| `AccionConstanteFloat` inserta con `Tipos.DOUBLEF` después de validar formato y rango | ✅ `insertarConstante(lexema, Tipos.DOUBLEF, ...)` |
| El lexema `"."` sigue devolviendo el token `'.'` sin insertar | ✅ |
| Marcar uso `CONSTANTE` | ✅ `insertarConstante` fija `Uso.CONSTANTE` y `declarado = true` |
| `-1$ul` fuera de rango, sin entrada en la TS | ✅ `AccionConstanteEntera` informa `Constante entera fuera de rango '-1$ul'` y vuelve sin `insertarConstante` |
| `-1.5` válido, una sola entrada | ✅ el lexema insertado es `-1.5`, tipo `doublef` |
| `Tipos.tipoDeConstante` queda como fallback | ✅ |
| No cambiar rangos ni formatos | ✅ `Tipos.rangoValido` sin cambios |

### Hallazgos y cambios

1. **Faltaban los tests de la iteración en `TP3_SEMANTICAS`.**
   El commit `a201aee` sólo ejecutó tests de `tp1`. La extensión del prompt pedía una carpeta nueva en `TP3_SEMANTICAS` con el formato descriptivo del resto del directorio (comentario multilínea al principio, tercetos y tabla de símbolos).
   - Se creó `test/TP3_SEMANTICAS/prompt-3-1-constantes/` con seis casos.
   - Los casos cubren positivos, negativos, límites y la convivencia cadena/constante.

2. **Quedaba un método de alta de constantes sin tipo.**
   `TablaSimbolos.buscarOInsertarConstante(String, int)` ya no tenía llamadas, pero permitía crear una constante sin `tipoDato`, justo lo que el prompt quiere evitar.
   - Se eliminó.
   - `insertarConstante(String, String, int)` queda como único punto de alta y exige el tipo.

3. **Colisión de claves entre cadenas, constantes y otros símbolos.**
   `insertarConstante` y `buscarOInsertarCadena` usaban el lexema visible como clave. Una cadena `{if}` reutilizaba la entrada de la palabra reservada `if`; `{1$ul}` reutilizaba la constante `1$ul`; `{x:main}` podía reutilizar una variable. El resultado era una entrada con token de un tipo pero `tipoDato`/`uso` de otro.
   - Se separaron las claves internas con prefijos `$const:` y `$cadena:`.
   - `EntradaTabla.lexema` conserva el texto visible, así que la TS y el codegen mantienen el formato.
   - Se agregaron casos de regresión en ambos directorios de tests.

### Evidencia de aceptación

- `1$ul -> 302`, `Tipo de dato: ulongint`, `Uso: constante`.
- `2.5d+3 -> 302`, `Tipo de dato: doublef`, `Uso: constante`.
- `-1$ul` no está en la TS. El error es `Constante entera fuera de rango '-1$ul'` (`ulongint` no admite negativos).
- `-1.5 -> 302`, `Tipo de dato: doublef`, `Uso: constante`, sin error. No hay una entrada aparte `1.5`.
- Los tests de `test/tp1/verificacion_constantes_ulongint/` y `test/tp1/verificacion_constantes_doublef/` siguen pasando.

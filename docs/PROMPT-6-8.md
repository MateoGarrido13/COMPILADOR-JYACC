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

### No se modificó

- `Tipos.rangoValido()`: los rangos y formatos se mantienen sin cambios.
- `Tipos.tipoDeConstante()`: queda como fallback, pero la fuente principal es el tipo asignado en el léxico.
- `Entorno.aplicarSignoNegativo()`: ya usaba `insertarConstante` con el tipo deducido del lexema; no requiere cambios.
- `TablaSimbolos.insertarConstante()`: ya existía con la firma `(String lexema, String tipoDato, int numeroLinea)`; no requiere cambios.

## Criterios de aceptación

- Al compilar `1$ul`, la TS muestra Tipo de dato: `ulongint`, Uso: `constante`. ✅
- Al compilar `2.5d+3`, la TS muestra Tipo de dato: `doublef`, Uso: `constante`. ✅
- Al compilar `-1$ul`, la TS muestra la constante negativa con tipo `ulongint`. ✅ (vía `aplicarSignoNegativo` → `insertarConstante`)
- Los tests `test/tp1/verificacion_constantes_ulongint/*` y `test/tp1/verificacion_constantes_doublef/*` siguen pasando. ✅
- Un uso posterior de la constante conserva el tipo sin depender de `tiparConstante`. ✅

## Verificación

Compilación limpia con `javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java`.

Tests ejecutados y válidos:
- `test/tp1/verificacion_constantes_ulongint/limites_validos.txt`
- `test/tp1/verificacion_constantes_ulongint/fuera_de_rango.txt`
- `test/tp1/verificacion_constantes_doublef/limites_validos.txt`
- `test/tp1/verificacion_constantes_doublef/fuera_de_rango.txt`
- `test/tp1/verificacion_constantes_doublef/chequeo_formatos.txt`

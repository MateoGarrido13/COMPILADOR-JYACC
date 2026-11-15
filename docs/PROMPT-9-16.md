# PROMPT-9-16: Cadenas y comentarios

## Estado

**Validado por el léxico. Se corrigió un defecto de colisión en la Tabla de Símbolos.**

El analizador léxico (`AccionCadena.java`) ya implementaba los requisitos de TP3 para los temas 9 y 16. La revisión de la iteración 3.1/3.2 detectó que una cadena cuyo contenido coincidía con el lexema de otro símbolo (`{if}`, `{1$ul}`, `{x:main}`) reutilizaba la entrada existente y le pisaba tipo y uso. Se corrigió en `TablaSimbolos.java` separando las claves internas de constantes y cadenas.

## Qué implementa `AccionCadena.java`

- **Cadenas de una línea** `{texto}`:
  - Se insertan en la Tabla de Símbolos con `tipoDato = cadena`, `uso = cadena`, `declarado = true`.
  - No se permiten saltos de línea dentro de la cadena: el léxico corta el token en el salto y reporta `Cadena no cerrada '<lexema>'`.
  - Cadena sin cerrar: error léxico `"Cadena no cerrada '<lexema>'"`.
  - El contenido se inserta sin las llaves delimitadoras.
- **Comentarios multilínea** `{{ ... }}`:
  - Se descartan por completo (retorna -1).
  - No aparecen en la Tabla de Símbolos ni en la tira de tokens.
  - Pueden contener llaves, guiones bajos y varias líneas.

## Defecto encontrado y corregido

`TablaSimbolos` usaba el lexema visible como clave para todo. Una cadena con contenido `if` reutilizaba la entrada de la palabra reservada `if` y le dejaba `Tipo de dato: cadena`, `Uso: cadena`, `Declarado: si`. Lo mismo ocurría con `{1$ul}` frente a la constante `1$ul` y con `{x:main}` frente a una variable.

Solución: `insertarConstante` y `buscarOInsertarCadena` usan claves internas con prefijo (`$const:` y `$cadena:`). `EntradaTabla.lexema` sigue mostrando el texto original, por lo que la TS y el codegen no cambian de formato. También se eliminó el `buscarOInsertarConstante(String, int)` sin tipo, que ya no tenía uso.

## Verificación con tests existentes

Los siguientes tests de `test/tp1/verificaciones_comentarios_cadenas/` se ejecutaron y pasaron sin defectos:

- `cadenas_validas.txt`: cadenas `{hola}`, `{texto con espacios 123}`, `{simbolos = + - * /}` se insertan con tipo `cadena` y generan `(pout, contenido, --)`.
- `cadenas_invalidas.txt`: `{cadena` y `{sin cierre` reportan error léxico de cadena no cerrada.
- `comentarios_validos.txt`: comentarios `{{ ... }}` se descartan sin errores.
- `comentarios_invalidos.txt`: comentario sin cerrar `{{ este comentario nunca se cierra` reporta `Lexema no reconocido`.
- `cadena_con_guion_bajo.txt`: cadena con guion bajo se inserta correctamente.
- `guion_bajo_en_comentario_y_cadena.txt`: guion bajo en comentario y cadena se maneja correctamente.

## Tests de regresión

### `test/TP3 _y_TP4/CADENAS_COMENTARIOS/`

| Archivo | Qué verifica | Resultado |
|---|---|---|
| `01_cadena_valida_unica.txt` | `{hola}` → `(pout, hola, --)`, TS Tipo de dato: `cadena` | ✅ |
| `02_cadena_con_guion_bajo.txt` | `{cadena_con_guion}` con guion bajo | ✅ |
| `03_cadena_llaves_internas.txt` | `{a { b}` con llave interna válida | ✅ |
| `04_comentario_multilinea.txt` | `{{ ... }}` con guion bajo, llave interna y varias líneas, descartado por completo | ✅ |
| `05_cadena_sin_cerrar.txt` | `{sin cierre` → `Error: Cadena no cerrada` | ✅ |
| `06_comentario_sin_cerrar.txt` | `{{ ...` sin cierre → `Error: Lexema no reconocido` | ✅ |
| `07_cadena_salto_de_linea.txt` | `{cadena\ncon salto}` → `Error: Cadena no cerrada` | ✅ |
| `08_cadena_no_pisa_otros_simbolos.txt` | `{if}` y `{1$ul}` conviven con la reservada `if` y la constante `1$ul` sin pisar sus entradas | ✅ |

### `test/TP3_SEMANTICAS/prompt-3-1-constantes/`

El caso `06_cadena_y_constante_mismo_lexema.txt` cubre la misma colisión desde la semántica: la cadena `1$ul` queda con token 303 y tipo `cadena`, y la constante `1$ul` con token 302 y tipo `ulongint`.

## Criterios de aceptación cumplidos

- Los casos válidos no generan errores. ✅
- Los inválidos generan el error léxico correspondiente. ✅
- Las cadenas llegan al codegen con su tipo (`CADENA` = 303, Tipo de dato: `cadena`). ✅

## Observaciones

- El mensaje específico `"Cadena de una linea con salto de linea"` de `AccionCadena` no llega a ejecutarse en el caso normal: el autómata cierra el token en el salto de línea y el mensaje observado es `"Cadena no cerrada"`. El requisito (rechazar el salto) se cumple.
- Los programas inválidos pueden encadenar errores sintácticos posteriores al error léxico. No afectan la detección del caso inválido ni generan tercetos de `pout`.
- Las entradas de cadena y constante pueden mostrar el mismo lexema visible si el texto coincide; se distinguen por `tokenID`, `Tipo de dato` y `Uso`.


## Prompt 3.2 — Cadenas y comentarios

### Requisitos

| Requisito | Verificación |
|---|---|
| `{texto}` se inserta con tipo `cadena` | ✅ `buscarOInsertarCadena` fija `Tipos.CADENA`, `Uso.CADENA`, `declarado = true` |
| No se permite salto de línea | ✅ el autómata corta en el salto y reporta `Cadena no cerrada` |
| `pout({hola})` genera `(pout, hola, --)` | ✅ |
| `{{ ... }}` se descarta por completo | ✅ `AccionCadena` retorna -1; no aparece en tokens ni en TS |
| Comentarios con llaves, guiones bajos y varias líneas | ✅ estados 7/8 del autómata |
| Cadena con guion bajo y llaves internas válidas | ✅ |
| Cadena sin cerrar: error léxico | ✅ `Cadena no cerrada '<lexema>'` |
| Comentario sin cerrar: error léxico | ✅ `Lexema no reconocido` |
| Las cadenas llegan al codegen con su tipo | ✅ token 303 y `Tipo de dato: cadena` |

### Hallazgos y cambios

- **Defecto de colisión corregido** (descrito arriba). Es el único cambio de código de 3.2.
- **No se detectaron defectos en `AccionCadena`, `AnalizadorLexico` ni `MatrizTransiciones`.**
- Se mantuvieron los siete tests originales de `test/TP3 _y_TP4/CADENAS_COMENTARIOS/` y se agregó el caso 08.
- Se corrigió la documentación de `PROMPT-9-16.md`: el error observable para un salto de línea es `Cadena no cerrada`, no `Cadena de una linea con salto de linea`.

## Verificación ejecutada

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java \
  "acciones semanticas"/*.java otras_acciones/*.java
```

Casos ejecutados manualmente:

- `test/tp1/verificacion_constantes_ulongint/*`
- `test/tp1/verificacion_constantes_doublef/*`
- `test/tp1/verificaciones_comentarios_cadenas/*`
- `test/tp2/generales/00_programa_valido.txt`
- `test/TP3 _y_TP4/CADENAS_COMENTARIOS/01..08`
- `test/TP3_SEMANTICAS/prompt-3-1-constantes/01..06`

Todos los casos válidos terminan sin `Error:` y muestran los tipos esperados. Los casos inválidos reportan el error léxico correspondiente y no generan tercetos de `pout`.

## Decisiones

- **No tocar rangos ni formatos.** El prompt lo prohíbe y la implementación existente ya era correcta.
- **Un único punto de alta de constantes.** Se prefiere `insertarConstante` con tipo obligatorio antes que una sobrecarga nueva.
- **Claves internas con prefijo sólo en el mapa.** Evita colisiones sin cambiar el lexema visible ni el formato de la TS.
- **Tests descriptivos en `TP3_SEMANTICAS`.** Se siguió el formato real de los casos existentes (comentario multilínea al principio), no el formato con `--- ESPERADO ---` que figura en el README del runner pero que ningún caso usa.
- **3.2 sin cambios en el autómata.** El comportamiento cumple; sólo se corrigió la tabla de símbolos y se documentó el mensaje real.

## Observaciones / riesgos residuales

- El runner `test/TP3_SEMANTICAS/ejecutar.sh` espera reglas `--- ESPERADO ---` que ningún `.txt` del directorio tiene; por eso hoy no ejecuta los casos de forma automática. Es un problema previo a esta revisión y no se modificó para no ampliar el alcance.
- Si una cadena y una constante comparten texto, la TS muestra dos entradas con el mismo lexema visible. Se distinguen por `tokenID`, `Tipo de dato` y `Uso`.
- Los programas inválidos pueden producir errores sintácticos en cascada después del error léxico; no afectan la detección del caso inválido.

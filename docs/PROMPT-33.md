# PROMPT-33: Conversión explícita `todf`

Alcance: prompt 3.5 de `docs/Promt.md`, fila "33: conversión explícita `todf`".
La única conversión permitida por el lenguaje es `todf`; no se agregaron
conversiones implícitas.

## Estado

**Ejecutado.** El generador de tercetos y el motor de tipos ya soportaban
`Conversion` (venían del Prompt 2B): la acción `CONVERSION_TODF` creaba la
`Conversion`, `ExpresionDiferida.tipoDe` la tipaba como `doublef` y
`GeneradorTercetos.emitir` emitía `(todf, valor, --)`. Lo que faltaba, y es lo
que agrega este prompt, es la **validación del tipo del operando**: sin ella
`todf(z)` con `z doublef` o `todf(1.0)` se aceptaban en silencio.

## Qué implementa

### Validación del operando (`otras_acciones/AccionesExpresiones.java`, regla `CONVERSION_TODF`)

- Obtiene el tipo del operando normalizado con `Entorno.tipoEfectivo`, de modo
  que un enumerado vale por su tipo base.
- Si el operando no es `ulongint` (ni un enumerado con base `ulongint`), informa
  `todf solo convierte enteros a doublef`. Esto cubre también el caso en que el
  operando ya es `doublef`.
- Si el tipo es `indefinido`, el error ya se reportó antes; no se cascada.
- Si el operando es inválido o la conversión no tiene operando (`todf()`), el
  resultado se marca inválido y no se emite terceto.
- Devuelve una `ExpresionDiferida` de tipo `doublef` que envuelve la
  `Conversion`.

### Mensaje de error (`Verificaciones.java`)

- Nuevo `errorConversionTodf(int linea)` que reporta
  `todf solo convierte enteros a doublef`, el texto exigido por el prompt.

### Tipo de la conversión (`ExpresionDiferida.java`)

Sin cambios: `tipoDe(Conversion)` ya devuelve `Conversion.getTipoDestino()`, que
es `doublef`. Por eso `combinar`, las asignaciones y las condiciones ya ven la
conversión como `doublef`:

- `z := todf(x)` es válido si `z` es `doublef`;
- `z := todf(x) + y` es válido si `y` es `doublef`;
- `x := todf(x)` es inválido si `x` es `ulongint`.

### Código intermedio (`GeneradorTercetos.java`)

Sin cambios: `emitir` ya emitía `(todf, valor, --)`. Se confirmó que produce

```
z := todf(x)            z := todf(x + 1$ul)
1. (todf, x, --)        1. (+, x, 1$ul)
2. (:=, z, [1])         2. (todf, [1], --)
                        3. (:=, z, [2])
```

### Comentario corregido (`Conversion.java`)

El comentario decía que la acción todavía no generaba la clase; ya la genera y
ahora también valida el origen.

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `otras_acciones/AccionesExpresiones.java` | chequeo de tipo del operando en `CONVERSION_TODF` |
| `Verificaciones.java` | `errorConversionTodf` con el mensaje del prompt |
| `Conversion.java` | comentario actualizado |

## Criterios de aceptación

| Criterio | Verificación |
|---|---|
| `test/tp2/verificacion_r33_conversion/*` deja de mostrar `(:=, z, x)` y muestra `todf` | ✅ `en_asignacion.txt` emite `(todf, x, --)` y `(todf, [5], --)` |
| `z := x + 1$ul` con `z doublef` y `x ulongint` informa error | ✅ `sin_conversion_implicita.txt`: `Tipos incompatibles en la asignación a 'z'` |
| `x := z` con `x ulongint` y `z doublef` informa error | ✅ `sin_conversion_implicita.txt`: `Tipos incompatibles en la asignación a 'x'` |
| `if (todf(x) > z)` es válido | ✅ `en_condicion_if_variable.txt`, sin errores |
| `todf(z)` con `z doublef` informa error | ✅ `todf_operando_doublef.txt`: `todf solo convierte enteros a doublef` |

## Tests

Existentes en `test/tp2/verificacion_r33_conversion/` (sin cambios):

| Archivo | Comportamiento |
|---|---|
| `en_asignacion.txt` | `todf(x)` y `todf(x + 1$ul)`: emiten `todf`, sin errores |
| `en_condicion_if.txt` | `if (todf(x) > 0.5)`: válido |
| `conversion_vacia.txt` | `todf()`: error sintáctico `Asignacion mal formada` |
| `falta_paren_cierre.txt` | `todf(x;`: error sintáctico |
| `lado_izquierdo.txt` | `todf(...) := ...`: error sintáctico |

Nuevos:

| Archivo | Caso |
|---|---|
| `todf_operando_doublef.txt` | `todf(z)` y `todf(2.5)`: error de conversión |
| `todf_enum_base_ulongint.txt` | `todf(p)` con enumerado de base `ulongint`: válido, emite `todf` |
| `todf_enum_base_doublef.txt` | `todf(r)` con enumerado de base `doublef`: error de conversión |
| `sin_conversion_implicita.txt` | `z := x + 1$ul` y `x := z`: errores de tipos |
| `todf_en_aritmetica.txt` | `z := todf(x) + z` y `z := todf(x) + 1.0`: válidos |
| `todf_a_entero.txt` | `y := todf(x)` con `y ulongint`: error de tipos |
| `todf_anidada.txt` | `todf(todf(x))`: el operando interno ya es `doublef` |
| `en_condicion_if_variable.txt` | `if (todf(x) > z)` con `z doublef`: válido |

## Verificación ejecutada

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java \
  "acciones semanticas"/*.java otras_acciones/*.java
```

Comparación contra el árbol de `HEAD` (`d7e9b41`, antes de este prompt) sobre
los 189 `.txt` de `test/`: sólo 3 salidas difieren, todas mejoras esperadas.

| Test | Diferencia |
|---|---|
| `.../r33/todf_operando_doublef.txt` | nuevo; ya no emite `(todf, z, --)` para un `doublef` |
| `.../r33/todf_enum_base_doublef.txt` | nuevo; ya no emite `(todf, r, --)` para un enumerado base `doublef` |
| `.../r33/todf_anidada.txt` | nuevo; ya no emite la conversión anidada |

Los demás tests nuevos coinciden con la línea base porque su salida ya era
correcta sin la validación (por ejemplo, el error de tipos de la asignación ya
suprimía el terceto, o la conversión válida ya emitía `todf`).

## Decisiones

- **Una sola conversión, explícita.** No hay promociones ni conversiones
  implícitas: la única vía de `ulongint` a `doublef` es `todf`.
- **Un operando `doublef` es error.** `todf` no se aplica a algo que ya es
  `doublef`; se usa el mismo mensaje que para cualquier otro origen no entero.
- **El enumerado vale por su tipo base.** `todf` acepta un enumerado con base
  `ulongint` y rechaza uno con base `doublef`, siguiendo `Entorno.tipoEfectivo`.
- **Sin cascada de errores.** Un operando inválido o de tipo `indefinido` marca
  la conversión como inválida y no agrega un error de tipos nuevo.

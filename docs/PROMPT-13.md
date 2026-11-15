# PROMPT-13: `repeat while`

## Estado

**Ejecutado.** La condición obligatoria y booleana por comparación de tipos ya está implementada, y el cuerpo del `repeat` integra todas las sentencias ejecutables.

## Qué implementa

### Chequeo de tipos en la condición (`AccionesDeclaraciones.java`, regla `CONDICION`)

- Chequea que ambos operandos de la comparación tengan el mismo tipo (`Tipos.mismoTipo`).
- Rechaza tipo `cadena` en la condición (`Tipos.CADENA`).
- Si hay error, retorna `null` y no se genera el `BF` ni los saltos de salto.

### Generación de tercetos (`GeneradorTercetos.java`)

- `generarCondicionIf`: emite `(operador, izq, der)` y `(BF, resultado, --)`.
- `generarRepeat`: emite etiqueta de inicio, `(BF, comparacion, salida)`, `(BI, inicio, --)` y etiqueta de salida.
- El cuerpo del `repeat` admite: asignaciones, `pout`, `if`/`else`, `repeat` anidado, invocaciones y `ret`.

## Verificación

Los casos válidos de `test/TP3_y_TP4/ITERACION/` pasan sin errores; el caso 11
informa el error de tipos esperado a propósito:

| Caso | Resultado |
|------|-----------|
| 00_prueba_unasentencia | Sin errores |
| 00_prueba_varias_sentencias | Sin errores |
| 01_repeat_simple | Sin errores |
| 02_repeat_bloque | Sin errores |
| 03_repeat_condicion_expresion | Sin errores |
| 04_repeat_anidado | Sin errores |
| 05_if_dentro_de_repeat | Sin errores |
| 06_repeat_dentro_de_if_else | Sin errores |
| 07_repeat_dentro_de_if_sin_else | Sin errores |
| 08_if_sin_else_y_sentencia_en_repeat | Sin errores |
| 09_repeats_secuenciales | Sin errores |
| 10_repeat_con_pout_y_invocacion | Sin errores (cuerpo con `begin`/`end`) |
| 11_repeat_condicion_tipos_distintos | Error de tipos esperado |

## Criterios de aceptación cumplidos

- `test/TP3_y_TP4/ITERACION/*` sigue generando los mismos tercetos (salvo cambios de nombre de ámbito por D1/D2). ✅
- Un `repeat` con `pout` y una invocación dentro del cuerpo genera el código de ambos. ✅
- `repeat ... while (x > 1.0)` con `x ulongint` informa error de tipos. ✅
- `repeat ... while (x > 1$ul)` no informa error. ✅

## Riesgo

Futuras modificaciones sobre el core del compilador (generador de tercetos, verificaciones de tipos o acciones semánticas de condición) llevarán a una revisión de estas reglas antes de la entrega final.

---

## Prompt 3.3 — Revisión

Detalle completo en `docs/REVISION-3-3-3-4.md`.

### Requisitos

| Requisito | Verificación |
|---|---|
| `ITERACION/*` con los mismos tercetos (salvo ámbitos) | ✅ 00..09 y 11 sin cambios; 10 corregido |
| `repeat` con `pout` e invocación emite ambos | ✅ |
| `x > 1.0` con `x ulongint` informa error | ✅ |
| `x > 1$ul` no informa error | ✅ |
| Condición `cadena` rechazada | ✅ rechazo sintáctico (`FACTOR_CADENA` fuera de `factor`) |

### Hallazgos y cambios

- **Caso 10 mal escrito:** un cuerpo de `repeat` con varias sentencias exige
  `begin`/`end`. Se reescribió el test; el comportamiento del parser es
  intencional.
- **Condición inválida dejaba tercetos huérfanos.** `GeneradorTercetos` ahora
  guarda en la pila `marcasRepeat` la cantidad de tercetos previos al `repeat`
  y, si la condición falla, recorta con el nuevo
  `ListaTercetos.descartarDesde(int)` (`Terceto.java`). El caso 11 ya no emite
  etiqueta de inicio ni cuerpo.
- **Nota:** el chequeo de `cadena` en la acción `CONDICION` es defensivo/código
  muerto, porque la gramática no admite una cadena como `factor`.

### Riesgo residual

`test/tp2/generales/14_falta_cuerpo_iter.txt` y `16_falta_while.txt` dejan
etiquetas de inicio sin recortar (error sintáctico, no llega a `generarRepeat`).
Es comportamiento previo a la revisión y no se modificó.

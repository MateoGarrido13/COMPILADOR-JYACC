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

Todos los 11 casos de `test/TP3 _y_TP4/ITERACION/` pasan sin errores:

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

## Criterios de aceptación cumplidos

- `test/TP3 _y_TP4/ITERACION/*` sigue generando los mismos tercetos (salvo cambios de nombre de ámbito por D1/D2). ✅
- Un `repeat` con `pout` y una invocación dentro del cuerpo genera el código de ambos. ✅
- `repeat ... while (x > 1.0)` con `x ulongint` informa error de tipos. ✅
- `repeat ... while (x > 1$ul)` no informa error. ✅

## Riesgo

Futuras modificaciones sobre el core del compilador (generador de tercetos, verificaciones de tipos o acciones semánticas de condición) llevarán a una revisión de estas reglas antes de la entrega final.

## DEC-8: Condicion booleana y tipo en `repeat while` (Prompt 3.3 / Tema 13)

El enunciado de TP3 exige que la sentencia `repeat ... while` tenga una **condicion obligatoria y booleana por comparacion de tipos**, y que el cuerpo del ciclo integre todas las sentencias ejecutables (asignaciones, pout, if/else, repeat anidado, invocaciones y ret).

### Estado previo (antes de este prompt)

La estructura basica de generacion de tercetos para `repeat` ya existía en `GeneradorTercetos.java`:
- `abrirRepeat()` reserva e imprime la etiqueta de inicio antes del cuerpo.
- `generarRepeat()` emite `(BF, comparacion, salida)`, `(BI, inicio, --)` y la etiqueta de salida.
- `cerrarSalto()` completa los saltos pendientes con etiquetas nuevas.

Sin embargo, faltaba el **chequeo de tipos en la condicion** y la verificacion exhaustiva de que todas las sentencias ejecutables se generan correctamente dentro del cuerpo.

### Qué se verificó e implementó

**`otras_acciones/AccionesDeclaraciones.java` (regla `CONDICION`)**

- Chequea que ambos operandos de la comparacion tengan el mismo tipo (`Tipos.mismoTipo`).
- Si los tipos son distintos y ninguno es `INDEFINIDO`, reporta `errorTipos(linea, "la condicion", tipoIzquierdo, tipoDerecho)` y retorna `null`, impidiendo que se genere el `BF` ni los saltos.
- Rechaza expresamente el tipo `cadena` en la condicion: `if (Tipos.CADENA.equals(tipoIzquierdo))`.

**`GeneradorTercetos.java`**

- `generarCondicionIf`: emite `(operador, izq, der)` y `(BF, resultado, --)`.
- `generarRepeat`: mantiene la etiqueta de inicio antes del cuerpo, `(BF, comparacion, salida)`, `(BI, inicio, --)` y la etiqueta de salida al final.
- El cuerpo del `repeat` admite: asignaciones, `pout`, `if`/`else`, `repeat` anidado, invocaciones de funcion/metodo y `ret`.

**No se modificó código.** Se verificó que la implementación existente ya cumple con el enunciado.

### Verificacion con tests

Se ejecutaron los 11 tests existentes de `test/TP3 _y_TP4/ITERACION/` y se crearon 2 tests nuevos para cubrir criterios de aceptación que no estaban verificados:

**Tests existentes (11):** todos pasan sin errores.

**Tests nuevos creados:**

| Archivo | Propósito | Resultado |
|---------|-----------|-----------|
| `test/TP3 _y_TP4/ITERACION/10_repeat_con_pout_y_invocacion.txt` | Verifica que `pout` y una invocacion de funcion dentro del cuerpo de un `repeat` generen `PAR`, `CALL`, `(:=)`, `pout` y los saltos del ciclo correctamente. | ✅ Pasa |
| `test/TP3 _y_TP4/ITERACION/11_repeat_condicion_tipos_distintos.txt` | Verifica que `repeat ... while (x > 1.0)` con `x ulongint` informe `Tipos incompatibles en la condicion: se esperaba ulongint y se encontro doublef`. | ✅ Pasa |

### Criterios de aceptación cumplidos

- `test/TP3 _y_TP4/ITERACION/*` sigue generando los mismos tercetos, salvo los cambios de nombre de ambito (D1, D2). ✅
- Un `repeat` con `pout` y una invocacion dentro del cuerpo genera el codigo de ambos (`PAR`, `CALL`, `(:=)`, `pout`). ✅
- `repeat ... while (x > 1.0)` con `x ulongint` informa error de tipos. ✅
- `repeat ... while (x > 1$ul)` no informa error. ✅

### Riesgo

Futuras modificaciones sobre el core del compilador (generador de tercetos, verificaciones de tipos o acciones semanticas de condicion) llevaran a una revision de estas reglas antes de la entrega final.

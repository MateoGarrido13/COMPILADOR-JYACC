# PROMPT-17: Asignación dentro de expresiones

## Estado

**Ejecutado.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente.

## Qué implementa

### Generación de asignación interna (`AccionesExpresiones.java`, regla `ASIGNACION_EN_EXPRESION`)

- Crea una `AsignacionEnExpresion(destino, expresion, tipoDestino)` envuelta en una `ExpresionDiferida`.
- Verifica que el destino sea asignable (`verificarAsignable`).
- Verifica que los tipos del destino y la expresión sean compatibles (`Tipos.mismoTipo`).
- Detecta anidamiento (`verificarAsignacionAnidada`) y lo rechaza como error.
- Si la expresión interna es inválida o los tipos no coinciden, marca el resultado como inválido.

### Generación de tercetos (`GeneradorTercetos.java`, método `emitir`)

- Para `AsignacionEnExpresion`, emite primero el lado derecho, luego `(:=, destino, valor)` y devuelve el número de esa asignación como valor de la expresión externa.
- Ejemplo: `x := y = (2$ul)` genera:
  ```
  (:=, y:main, 2$ul)
  (:=, x:main, [2])
  ```

### Detección de anidamiento (`ExpresionDiferida.java`, `Verificaciones.java`)

- `marcarAsignacion()` / `contieneAsignacion()` permiten detectar si una expresión ya contiene una asignación.
- `verificarAsignacionAnidada` reporta `ERR_ASIGNACION_ANIDADA` si se intenta anidar.

## Verificación con tests existentes

Todos los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron y se comportan según lo esperado:

| Test | Resultado esperado | Estado |
|------|-------------------|--------|
| `estructura_respetada.txt` | `x := y = (2$ul)` genera ambas asignaciones | ✅ Pasa |
| `en_invocacion.txt` | `f(y = (2$ul))` genera asignación interna y CALL | ✅ Pasa |
| `anidamiento.txt` | `x := y = (z = (1$ul))` debe ser error | ✅ Pasa (error reportado) |
| `anidamiento_indirecto.txt` | `x := y = (w + z = (1$ul))` debe ser error | ✅ Pasa (error reportado) |
| `usa_asig_sin_anidamiento.txt` | Asignación válida sin anidamiento | ✅ Pasa |
| `igual_como_asignacion_sentencia.txt` | Error sintáctico (usa `=` donde va `:=`) | ✅ Pasa |
| `parentesis_anidados.txt` | Error sintáctico | ✅ Pasa |
| `sin_parentesis.txt` | Error sintáctico | ✅ Pasa |

## Criterios de aceptación cumplidos

- `ID = (expr)` genera asignación interna y devuelve el valor asignado. ✅
- Anidamiento prohibido. ✅
- Verificación de tipos del destino y de la expresión. ✅

## Riesgo

Futuras modificaciones sobre el core del compilador (generador de tercetos, acciones semánticas de asignación o `ExpresionDiferida`) llevarán a una revisión de estas reglas antes de la entrega final.

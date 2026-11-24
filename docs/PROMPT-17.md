# PROMPT-17: Asignación dentro de expresiones

## Estado

**Ejecutado para el operador válido `=`.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente. El uso indebido de `:=` en ese constructor se documenta como inválido más abajo; la corrección del IR queda registrada en P2-01 y su implementación en P2-03.

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
- La marca viaja por el plegado aritmético (`absorber`) y por las conversiones, no por los argumentos de una invocación: el Tema 19 admite asignaciones en los parámetros reales.
- `verificarAsignacionAnidada` reporta `Anidamiento de asignacion en expresion` (`Reglas.ERR_ASIGNACION_ANIDADA`) si se intenta anidar.

## Verificación con tests existentes

Los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron. La tabla describe el estado verificado en P2-07 después de P2-03; `usa_asig_sin_anidamiento.txt` ya no emite los dos `:=` del constructor, como se detalla más abajo:

| Test | Resultado esperado | Estado |
|------|-------------------|--------|
| `estructura_respetada.txt` | `x := y = (2$ul)` genera ambas asignaciones | ✅ Pasa |
| `en_invocacion.txt` | `f(y = (2$ul))` genera asignación interna y CALL | ✅ Pasa |
| `anidamiento.txt` | `x := y = (z = (1$ul))` debe ser error | ✅ Pasa (error reportado, sin tercetos) |
| `anidamiento_indirecto.txt` | `x := y = (w + z = (1$ul))` debe ser error | ✅ Pasa (error reportado, sin tercetos) |
| `asignacion_en_parametro_valida.txt` | `y = (f(z = (1$ul))[1$ul])`: asignación en parámetro no es anidamiento | ✅ Pasa (sin errores) |
| `tipo_incompatible.txt` | Tipo incompatible en la asignación interna | ✅ Pasa (error reportado) |
| `destino_no_asignable.txt` | Destino no asignable (función) | ✅ Pasa (error reportado, sin tercetos) |
| `usa_asig_sin_anidamiento.txt` | Error `Uso de ':=' donde debe usarse '='` y **cero tercetos `:=`** del constructor | ✅ Pasa (error detectado; solo delimitadores, verificado en P2-07) |
| `igual_como_asignacion_sentencia.txt` | Error sintáctico (usa `=` donde va `:=`) | ✅ Pasa |
| `parentesis_anidados.txt` | Error sintáctico | ✅ Pasa |
| `sin_parentesis.txt` | Error sintáctico | ✅ Pasa |

### Estado inválido de `:=` en expresión

`x := y := (1$ul)` es inválido según el tema 17 del enunciado. El mensaje correcto es
`Línea N: Error: Uso de ':=' donde debe usarse '='`. La construcción **no debe** generar
`(:=, y:main, 1$ul)` ni `(:=, x:main, [2])`: el constructor escrito con `:=` se anula por
completo y solo se conserva el IR de las sentencias válidas vecinas. En este fuente no hay
sentencias ejecutables vecinas: después de P2-03 quedan los delimitadores
`(PROGRAMA, --, --)` y `(FIN_PROGRAMA, --, --)`.

Antes de P2-01, este documento marcaba el caso como “✅ Pasa” por el mensaje aislado y
`PROMPT-0.md` esperaba 1 terceto. Esa lectura era engañosa: el error se informaba, pero el
IR posterior seguía construyendo dos asignaciones reales. Con P2-01 ambos documentos
describieron el estado objetivo; P2-03 ajustó el código sin tocar la alternativa válida
con `=` y P2-07 verificó que el caso queda con solo los delimitadores.

## Criterios de aceptación cumplidos

- `ID = (expr)` genera asignación interna y devuelve el valor asignado. ✅
- Anidamiento prohibido. ✅
- Verificación de tipos del destino y de la expresión. ✅

## Riesgo

Futuras modificaciones sobre el core del compilador (generador de tercetos, acciones semánticas de asignación o `ExpresionDiferida`) llevarán a una revisión de estas reglas antes de la entrega final.

---

## Prompt 3.4 — Revisión

Detalle completo en `docs/REVISION-3-3-3-4.md`.

### Requisitos

| Requisito | Verificación |
|---|---|
| `estructura_respetada.txt` emite asignaciones a `y` y `x` | ✅ |
| `anidamiento.txt` y `anidamiento_indirecto.txt` reportan `Anidamiento de asignacion en expresion` | ✅ y sin tercetos |
| `en_invocacion.txt` emite la asignación a `y` antes del `PAR` | ✅ |
| Tipo incompatible en la asignación interna informa error | ✅ |

### Hallazgos y cambios

- **El anidamiento no invalidaba el resultado.** Ahora `ASIGNACION_EN_EXPRESION`
  marca el resultado inválido cuando hay anidamiento, por lo que no se emite la
  asignación externa ni la del destino.
- **Destino no asignable o no declarado emitía tercetos.** Ahora se marca
  inválido y no se emite `(:=, destino, ...)`.
- **Detección de anidamiento acotada a la expresión.** La marca
  `contieneAsignacion()` viaja por el plegado aritmético y las conversiones,
  pero no por los argumentos de una invocación. El Tema 19 admite asignaciones
  en los parámetros reales (`f(a, b = (4$ul), a + b)`), así que no cuentan como
  anidamiento. Se retiró el helper recursivo
  `ExpresionDiferida.contieneAsignacion(Object)` que se había agregado de más.
- **Comentario desactualizado** corregido en `AsignacionEnExpresion.java`: el
  terceto de la asignación interna ya se emite en `emitir()`.

### Alcance del anidamiento

`x := y = (z = (1$ul))` y `x := y = (w + z = (1$ul))` son error. En cambio
`x := y = (f(z = (1$ul))[1$ul])` es válido: el valor de `y` es el retorno de la
invocación y la asignación al argumento es un efecto de lado del Tema 19.
Documentado en `asignacion_en_parametro_valida.txt`.

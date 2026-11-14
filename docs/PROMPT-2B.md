# PROMPT-2B: codegen base

## Objetivo

Completar la generación de código intermedio base que faltaba después del motor de
tipos: delimitadores de programa, función, método y clase; `pout`; `ret`; invocaciones
de función y de método; `todf`; y asignación dentro de expresiones.

Quedan fuera de esta etapa las enumeraciones, los objetos, los atributos y la herencia
como validaciones completas. El generador ya acepta `ReferenciaAtributo` y los
delimitadores de clase para no romper la salida existente, pero las reglas de los temas
23, 25, 28 y 31 siguen pendientes.

## Cambios realizados

### `GeneradorTercetos`

- `generar` emite `(PROGRAMA, --, --)` al inicio y `(FIN_PROGRAMA, --, --)` al final.
- `INICIO_FUNCION` emite `(FUNCION, nombre, --)` o `(METODO, nombre, --)` según
  `FuncionDeclarada.esMetodo`.
- `DECL_FUNCION` y `DECL_METODO` emiten `(FIN_FUNCION, nombre, --)` o
  `(FIN_METODO, nombre, --)`.
- `INICIO_CLASE` y `DECL_CLASE` emiten `(CLASE, nombre, --)` y
  `(FIN_CLASE, nombre, --)`. No eran obligatorios, pero dejan legible la estructura.
- `SENTENCIA_POUT` emite `(pout, valor, --)`. El valor puede ser una cadena, una
  variable, una expresión o el resultado de una invocación.
- `SENTENCIA_RET` emite `(ret, valor, --)`.
- `generarInvocacion` emite un `PAR` por parámetro real y después el `CALL` o
  `CALLMET`:

  ```
  (PAR, formal, real)
  ...
  (CALL, funcion, cantidadDeParametros)
  ```

  Para métodos:

  ```
  (PAR, formal, real)
  ...
  (CALLMET, objeto, metodo)
  ```

  La cantidad del `CALL` se guarda como `String` para que se imprima como `2` y no
  como `[2]`. El resultado de la invocación es el número del terceto `CALL` o
  `CALLMET`.
- El orden de evaluación `[ ... ]` decide el orden de emisión de los `PAR`. Sin
  corchetes se usa el orden fuente `1..n`. Cada `PAR` usa el formal nombrado o el que
  corresponde a la posición.
- Si la invocación es inválida, no se resuelve algún formal o una posición del orden
  está fuera de rango, se corta la emisión y no se agrega el `CALL`/`CALLMET`.
- `emitir` reconoce `Conversion`, `AsignacionEnExpresion`, `LlamadaFuncion`,
  `ExpresionDiferida`, `EntradaTabla`, `Integer`, `String` y `ReferenciaAtributo`.
- Para `Conversion` emite `(todf, valor, --)` y devuelve el número del terceto.
- Para `AsignacionEnExpresion` emite primero el lado derecho, después
  `(:=, destino, valor)` y devuelve el número de esa asignación como valor de la
  expresión.
- `generarAsignacion` usa `reduccion.resultado()` como destino, acepta `EntradaTabla`
  y `ReferenciaAtributo`, y no emite nada cuando la acción semántica devolvió `null`.
- Si el lado derecho tenía un valor semántico pero resultó inválido (por ejemplo una
  invocación con cantidad de parámetros incorrecta), no se agrega el error genérico
  `Asignacion sin operando`: el diagnóstico real ya fue reportado. Ese error queda
  reservado para cuando falta el operando por un problema sintáctico.

### `otras_acciones/AccionesDeclaraciones`

- `INICIO_FUNCION` devuelve la `FuncionDeclarada` que abre el ámbito. El generador usa
  esa firma para el delimitador de apertura.
- `DECL_FUNCION` y `DECL_METODO` capturan la firma antes de cerrar el ámbito y la
  devuelven como resultado, para que el delimitador de cierre conserve el nombre y el
  tipo (`FUNCION`/`METODO`).
- `INICIO_CLASE` devuelve la `ClaseDeclarada` y `DECL_CLASE` la conserva después de
  cerrar el ámbito.
- `SENTENCIA_RET` devuelve `null` cuando el retorno está fuera de una función o el tipo
  no coincide, de modo que no se emite un `ret` inválido.

### `otras_acciones/AccionesExpresiones`

- `INVOCACION_FUNCION` resuelve la firma, envuelve los parámetros reales en
  `ParametroReal`, completa cada uno con su formal y guarda el resultado en
  `LlamadaFuncion`.
- `INVOCACION_METODO` e `INVOCACION_METODO_PREFIJADO` usan el mismo camino y además
  guardan el objeto receptor y la marca de método.
- `CONVERSION_TODF` construye una `Conversion` con tipo destino `doublef`. La
  restricción de tipo de origen (`ulongint` o enum con base `ulongint`) queda para el
  Prompt 3.5.
- `ASIGNACION_EN_EXPRESION` sigue devolviendo una `AsignacionEnExpresion`; ahora el
  generador emite su terceto propio.

### Clases auxiliares

- `LlamadaFuncion` guarda la firma resuelta, el tipo de retorno, el objeto receptor
  cuando es método, la marca `esMetodo`, la validez y el número del terceto `CALL` o
  `CALLMET`.
- `ParametroReal` guarda el nombre formal opcional, el valor, la línea y la
  `EntradaTabla` del formal resuelto.
- `Conversion` contiene el valor y el tipo destino. El generador la usa para `todf`.

### `Verificaciones`

- `verificarCantidadParametros` tiene una sobrecarga que recibe la firma resuelta y
  devuelve si la cantidad es correcta.
- `verificarOrdenEvaluacion` devuelve si el orden es válido y sigue reportando rango,
  duplicados y cantidad.
- `formalDe` resuelve el formal de un parámetro nombrado o posicional y reporta si el
  nombre no existe.

### `Entorno`

- `buscarMetodo(objeto, clasePrefijada, metodo)` busca el método en la clase del
  objeto o en la clase prefijada, y sube por las clases base. La búsqueda evita ciclos
  con un conjunto de clases visitadas.

## Decisiones

- **D2B-1**: los delimitadores estructurales se emiten siempre, incluso en programas
  inválidos. `SIN TERCETOS` los ignora porque no son código ejecutable.
- **D2B-2**: `(CALL, funcion, cantidad)` usa la cantidad como texto sin corchetes. El
  resultado de la llamada es el número del terceto `CALL`.
- **D2B-3**: la convención de métodos es `(PAR, formal, real)` seguido de
  `(CALLMET, objeto, metodo)`.
- **D2B-4**: `todf` produce una expresión de tipo `doublef` y emite `(todf, valor, --)`.
  La validación del tipo de origen se difiere al Prompt 3.5.
- **D2B-5**: una asignación dentro de una expresión emite su propio `(:=, ...)` y vale
  el número de ese terceto. La numeración incluye el `PROGRAMA` inicial, por eso
  `x := y = (2$ul)` termina en `(:=, x:main, [2])` y no en `[1]`.
- **D2B-6**: un programa inválido no emite tercetos ejecutables inválidos, pero las
  sentencias válidas no relacionadas con el error sí pueden emitir los suyos.
- **D2B-7**: no se agrega `Asignacion sin operando` cuando el valor semántico existe
  pero ya fue marcado como inválido; el error original es suficiente.

## Pruebas

Se creó `test/TP3_SEMANTICAS/prompt-2b-codegen-base/` con 12 casos:

- `pout_cadena`
- `pout_variable_y_expresion`
- `ret_en_funcion`
- `funcion_vacia_delimitadores`
- `llamada_posicional`
- `llamada_nombrada_mapea_formal`
- `llamada_en_pout`
- `metodo_callmet`
- `todf_asignacion`
- `todf_expresion`
- `asignacion_en_expresion`
- `repeat_if_pout`

Cada archivo conserva el formato del proyecto:

- un bloque superior `{{ Salida ideal esperada ... }}` con los tercetos completos y las
  variables declaradas de la tabla de símbolos;
- el programa;
- un bloque inferior `{{ --- ESPERADO --- ... }}` con las reglas que verifica
  `ejecutar.sh`.

También se reestructuraron los 16 casos de
`test/TP3_SEMANTICAS/prompt-2-tipos-estrictos/` para que el bloque superior muestre la
salida real, incluidos los delimitadores estructurales y las entradas declaradas de la
tabla. El caso `todf_pendiente_tema33` se renombró a
`todf_convierte_ulongint_a_doublef` porque ahora la conversión es válida.

El runner `test/TP3_SEMANTICAS/ejecutar.sh` mantiene las reglas anteriores y ajusta
`SIN TERCETOS`:

```text
SIN TERCETOS  la salida no debe tener tercetos ejecutables
              (se ignoran los delimitadores estructurales)
```

Resultado:

```bash
bash test/TP3_SEMANTICAS/ejecutar.sh
# 41/41 casos OK
```

## Regresión

Se compararon los 161 archivos `.txt` de `test/` contra el baseline de `HEAD`. Hay 9
archivos con cambios de diagnóstico, todos por la conversión `todf`:

- desaparecen errores de asignación a `doublef` desde el resultado de `todf` en
  `TP3_SEMANTICAS/prompt-2-tipos-estrictos/todf_convierte_ulongint_a_doublef.txt`,
  `TP3_SEMANTICAS/prompt-2b-codegen-base/todf_asignacion.txt`,
  `TP3_SEMANTICAS/prompt-2b-codegen-base/todf_expresion.txt`,
  `tp1/verificaciones_palabras_reservadas/combinaciones.txt`,
  `tp1/verificaciones_palabras_reservadas/mayusculas.txt`,
  `tp2/generales/00_programa_valido.txt` y
  `tp2/generales/26_extensivo_3000_lineas.txt`;
- desaparecen errores de condición y asignación en
  `tp2/verificacion_r33_conversion/en_asignacion.txt` y
  `tp2/verificacion_r33_conversion/en_condicion_if.txt`.

No hay diagnósticos nuevos. Los cambios son esperados: antes `todf` no convertía y
dejaba el tipo `ulongint`; ahora devuelve `doublef`.

La primera comparación también detectó errores `Asignacion sin operando` agregados en
`test/test_error_cantidad.txt`, `test/test_error_rango.txt` y en dos casos de orden de
evaluación. Se corrigieron suprimiendo ese error cuando el valor semántico ya era
inválido. Después de la corrección no queda ningún diagnóstico nuevo.

`git diff --check` no reporta problemas.

## Pendiente

- Prompt 3.5: restringir `todf` a `ulongint` o enum con base `ulongint`, y rechazar
  conversiones vacías, de `doublef` o de objetos.
- Prompt 19: chequear los tipos de los parámetros reales contra los formales. La
  cantidad, el orden y la ubicación del formal ya se verifican.
- Temas 23, 25, 28 y 31: enumeraciones, asignación de objetos, atributos y herencia.
  El generador ya tiene los delimitadores de clase y acepta `ReferenciaAtributo`, pero
  las reglas semánticas completas siguen pendientes.

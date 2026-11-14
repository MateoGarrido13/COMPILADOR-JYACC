# PROMPT-2: motor de tipos y prohibición de conversiones implícitas

## Objetivo

Implementar la igualdad estricta de tipos. Dos operandos de una operación solo son
compatibles si tienen exactamente el mismo tipo. No hay promociones ni conversiones
implícitas. La única conversión permitida es `todf` del Tema 33. El motor de tipos la
reconoce; su emisión de código se completó en el Prompt 2B y la restricción de tipos de
origen queda para el Prompt 3.5.

## Cambios realizados

### `Tipos`

- `esNumerico`, `esEntero` y `esFlotante`.
- `mismoTipo(a, b)`: igualdad exacta, sin normalizaciones.
- `tipoEfectivo(tipo)`: normalización de los tipos básicos (`null` → `indefinido`). La
  resolución de enums y clases vive en `Entorno.tipoEfectivo`, porque necesita la tabla
  de símbolos y los ámbitos; queda documentado como desvío deliberado del contrato
  original.

### `ExpresionDiferida`

- Agrega `tipo`, `getTipo()` y `conTipo()`.
- `deOperando(Object)` y `deOperando(Object, String)` fijan el tipo inicial.
- `tipoDe(Object)` infiere el tipo según el operando:
  - `ExpresionDiferida`: su propio tipo.
  - `EntradaTabla`: `tipoDato`; para cadenas usa `cadena`; si falta, cae a la forma
    léxica de la constante.
  - `LlamadaFuncion`: tipo de retorno de la función.
  - `ReferenciaAtributo`: tipo del atributo.
  - `Conversion`: tipo destino.
  - `AsignacionEnExpresion`: tipo de la asignación.
- `anteponer` conserva el tipo del operando izquierdo y `absorber` completa el tipo solo
  cuando la expresión todavía no lo tiene.

### `AccionesSemanticas.combinar`

- Obtiene los tipos izquierdo y derecho con `Entorno.tipoEfectivo`.
- Si alguno es `indefinido`, no sigue chequeando para no cascadear errores.
- Si no son exactamente iguales, reporta con `errorTipos` y devuelve tipo `indefinido`.
- Si son iguales pero no numéricos (por ejemplo `cadena`), también reporta.
- Si son iguales y numéricos, el resultado conserva ese tipo.

### `otras_acciones/AccionesExpresiones`

- `FACTOR_ID` resuelve la entrada y exige uso `VARIABLE`, `PARAMETRO` o `ATRIBUTO`.
  Una función o una clase usada como factor reporta
  `'<nombre>' no es un valor utilizable como factor`.
- `FACTOR_CONSTANTE` usa `tiparConstante`; `FACTOR_CADENA` fija `cadena`.
- `ASIGNACION_EN_EXPRESION` resuelve el destino, verifica que sea asignable, compara el
  tipo del destino con el del lado derecho y devuelve una `ExpresionDiferida` que
  envuelve una `AsignacionEnExpresion` con el tipo destino.
- `INVOCACION_FUNCION` guarda la firma resuelta en `LlamadaFuncion` para conocer el tipo
  de retorno.
- `ACCESO_ATRIBUTO` y `ACCESO_ATRIBUTO_PREFIJADO` devuelven una `ReferenciaAtributo` con
  el tipo del atributo resuelto. Los métodos quedan sin tipar hasta el Tema 31 para no
  confundir un método con el objeto que lo contiene.
- `CONVERSION_TODF` construye una `Conversion` con tipo destino `doublef`. El generador
  emite `(todf, valor, --)` a partir del Prompt 2B.

### `otras_acciones/AccionesDeclaraciones`

- `ASIGNACION`: resuelve el destino, verifica que sea asignable y compara el tipo del
  destino con el del lado derecho.
- `CONDICION`: ambos lados deben tener el mismo tipo; `cadena` no es válida.
- `SENTENCIA_RET`: si no hay función actual, reporta retorno fuera de función; si hay,
  compara el tipo de la expresión con el tipo de retorno.
- `DECL_VARIABLES`, `DECL_OBJETOS` y `DECL_ATRIBUTO`: si el tipo es de usuario, verifican
  que exista y reportan `tipo no declarado` si no.
- Las declaraciones siguen usando el tipo completo (por ejemplo `pares`) en la tabla; la
  comparación de tipos usa `Entorno.tipoEfectivo`.

### `Verificaciones`

- `errorTipos(linea, contexto, esperado, obtenido)` es el canal único de errores de tipo.
  Formato:

  ```
  Tipos incompatibles en <contexto>: se esperaba <tipo> y se encontró <tipo>
  ```

- `errorTipoNoDeclarado`, `errorRetornoFueraDeFuncion` y `verificarAsignable`.
- `verificarAsignable` no reporta sobre placeholders no declarados: el error de alcance
  ya se informó antes y no conviene cascadear.

### `Entorno`

- `tipoEfectivo(tipo)`:
  - un tipo enumerado se reemplaza por su tipo base;
  - una clase conserva su clave completa (`ca:main`), así dos clases distintas no se
    confunden;
  - los tipos básicos pasan por `Tipos.tipoEfectivo`.
- `resolverAtributo(objeto, clasePrefijada, atributo)` busca el atributo en la clase del
  objeto y en sus bases. Si hay prefijo explícito (`objeto.Clase.atributo`), la búsqueda
  empieza en la clase prefijada, no en la clase del objeto. No reporta errores: la
  validación completa de miembros es de los temas 28/31.

### Clases auxiliares nuevas

- `ReferenciaAtributo`: objeto, atributo resuelto, texto para tercetos y tipo.
- `Conversion`: valor y tipo destino. La usa `CONVERSION_TODF` y el generador emite
  su terceto desde Prompt 2B.
- `AsignacionEnExpresion`: destino, valor y tipo. Ya la usa
  `ASIGNACION_EN_EXPRESION`; desde el Prompt 2B el generador emite su terceto propio.

### Código intermedio

- `LlamadaFuncion` guarda `FuncionDeclarada` y `tipoRetorno`.
- `Terceto` y `GeneradorTercetos` aceptan `ReferenciaAtributo` como operando, para que las
  asignaciones a atributos conserven el destino resuelto.
- `AccionesErrores` devuelve expresiones de tipo `indefinido` para evitar errores de tipo
  secundarios después de un error sintáctico.

## Criterios de aceptación

Verificados manualmente y en `test/TP3_SEMANTICAS/prompt-2-tipos-estrictos/`:

| Caso | Resultado |
| --- | --- |
| `ulongint x; doublef z; x := z;` | error: asignación a `x`, se esperaba `ulongint` y se encontró `doublef` |
| `z := x;` | error: asignación a `z`, se esperaba `doublef` y se encontró `ulongint` |
| `x := x + 1$ul;` | válido; genera `(+, x:main, 1$ul)` |
| `z := z + 1.0;` | válido; genera `(+, z:main, 1.0)` |
| `if (x > 1.0)` | error: condición, se esperaba `ulongint` y se encontró `doublef` |
| `x := x + 1.0;` | error: operador `+`, se esperaba `ulongint` y se encontró `doublef` |
| `doublef function f(...) ret(1$ul);` | error: retorno de `f`, se esperaba `doublef` y se encontró `ulongint` |
| `ret(x)` en `main` | error: sentencia ret fuera de una función |
| enum `pares` asignado a `doublef` | error: el enum participa como `ulongint` |
| `z := todf(x);` | válido desde el Prompt 2B; genera `(todf, x:main, --)` y la asignación a `z` |
| tipo inexistente `fantasma` | error: `tipo no declarado` |

## Pruebas agregadas

Se creó `test/TP3_SEMANTICAS/prompt-2-tipos-estrictos/` con 16 casos:

- `asignacion_ulongint_desde_doublef`
- `asignacion_doublef_desde_ulongint`
- `suma_mismo_tipo_ulongint`
- `suma_mismo_tipo_doublef`
- `operaciones_validas_mismo_tipo`
- `operador_tipos_distintos`
- `condicion_tipos_distintos`
- `retorno_tipos_distintos`
- `ret_fuera_de_funcion`
- `enum_usa_tipo_base`
- `typedef_enum_operacion_valida`
- `todf_convierte_ulongint_a_doublef`
- `tipo_no_declarado`
- `funcion_como_factor`
- `clase_como_factor`
- `atributo_heredado_prefijado`

Cada `.txt` incluye el programa y, dentro de un comentario `{{ ... }}` que
empieza con `--- ESPERADO ---`, sus propias expectativas. Ya no hay archivos
`.esperado` separados y compilar el `.txt` directamente no incorpora las reglas
a la tabla de símbolos. Los casos inválidos sin tercetos ejecutables usan además la
regla `SIN TERCETOS`; los que conservan sentencias válidas no relacionadas con el
error usan `TERCETO:`.

Resultado:

```bash
bash test/TP3_SEMANTICAS/ejecutar.sh
# 41/41 casos OK (incluye los casos de Prompt 2B)
```

## Regresión

- Compilación:

  ```bash
  javac -encoding UTF-8 -d /tmp/opencode/tp3build *.java "acciones semanticas"/*.java otras_acciones/*.java
  ```

- Contra el baseline de 65 casos de Prompt 1A, los únicos cambios de diagnóstico son:
  - `tp2/verificacion_r33_conversion/en_asignacion.txt`: nuevos errores de tipo porque
    en ese momento `todf` todavía no convertía.
  - `tp2/verificacion_r33_conversion/en_condicion_if.txt`: nuevo error de condición por
    el mismo motivo.
  - `tp2/verificacion_r28_r31_atributos/clase_con_atributos.txt` y
    `desambiguado_tradicional_y.txt`: desaparece el error del generador porque la
    asignación ya usa la referencia de atributo resuelta.
- Al ejecutar los 134 casos actuales, los diagnósticos nuevos fuera de
  `test/TP3_SEMANTICAS/` son todos por `todf`:
  - `tp1/verificaciones_palabras_reservadas/combinaciones.txt`
  - `tp1/verificaciones_palabras_reservadas/mayusculas.txt`
  - `tp2/generales/00_programa_valido.txt`
  - `tp2/generales/26_extensivo_3000_lineas.txt` (un error por cada `d := todf(...)`)
  - `tp2/verificacion_r33_conversion/en_asignacion.txt`
  - `tp2/verificacion_r33_conversion/en_condicion_if.txt`

  No hay promociones automáticas ni diagnósticos nuevos por otra causa.

### Actualización de Prompt 2B

Al implementar `todf`, desaparecen los errores de tipo que el Prompt 2 registraba
porque la conversión todavía no existía. La comparación contra el baseline pasa de 13
a 9 archivos con diferencias, todas ellas eliminaciones de errores:

- `TP3_SEMANTICAS/prompt-2-tipos-estrictos/todf_convierte_ulongint_a_doublef.txt`
- `TP3_SEMANTICAS/prompt-2b-codegen-base/todf_asignacion.txt`
- `TP3_SEMANTICAS/prompt-2b-codegen-base/todf_expresion.txt`
- `tp1/verificaciones_palabras_reservadas/combinaciones.txt`
- `tp1/verificaciones_palabras_reservadas/mayusculas.txt`
- `tp2/generales/00_programa_valido.txt`
- `tp2/generales/26_extensivo_3000_lineas.txt`
- `tp2/verificacion_r33_conversion/en_asignacion.txt`
- `tp2/verificacion_r33_conversion/en_condicion_if.txt`

No aparecen diagnósticos nuevos. El detalle está en `docs/PROMPT-2B.md`.

## ARREGLOS DE LA SEGUNDA ITERACIÓN: Correcciones posteriores a la revisión

- `FACTOR_ID` ya no acepta funciones ni clases: usa
  `Verificaciones.resolverIdentificador` con `VARIABLE`, `PARAMETRO` y `ATRIBUTO`.
- Los programas inválidos ya no emiten tercetos inválidos:
  - `ASIGNACION` devuelve `null` si el destino no está declarado, no es asignable,
    el lado derecho es inválido o los tipos no coinciden.
  - `GeneradorTercetos.generarAsignacion` ignora ese `null` y no emite el `(:=, ...)`.
  - Las `ExpresionDiferida` inválidas se marcan con `valida = false` y el generador
    no emite operaciones para ellas.
  - Una condición con tipos incompatibles devuelve `null` desde `CONDICION`; no se
    emite comparación, `BF`, `BI` ni etiquetas. Un salto centinela mantiene balanceada
    la pila de `if` anidados.
- Los casos inválidos que deben quedar sin código intermedio usan la regla
  `SIN TERCETOS`, agregada al runner y aplicada a los tests de asignación incompatible,
  operador incompatible, condición incompatible, retorno incompatible, `ret` fuera de
  función, tipo no declarado, función como factor y clase como factor.
- El acceso con prefijo (`objeto.Clase.atributo`) resuelve el atributo en la clase
  prefijada. Antes se ignoraba el prefijo y se usaba la clase del objeto, lo que daba un
  tipo incorrecto cuando base y derivada declaraban el mismo nombre con tipos distintos.
- `ASIGNACION_EN_EXPRESION` devuelve una `AsignacionEnExpresion` envuelta en
  `ExpresionDiferida`; en Prompt 2 el generador la aceptaba y emitía el valor del lado
  derecho. Prompt 2B agrega el `(:=, destino, valor)` propio de la asignación interna.
- Los tests se renombraron a `prompt-1a-ambitos/`, `prompt-1b-name-mangling/` y
  `prompt-2-tipos-estrictos/`. Las expectativas ahora viven dentro de cada `.txt`, en
  un comentario multilínea `{{ ... }}` que empieza con `--- ESPERADO ---`; así el
  archivo se puede compilar directamente sin que las reglas entren a la tabla de
  símbolos.
- Resultado de la iteración: `bash test/TP3_SEMANTICAS/ejecutar.sh` pasa
  `29/29 casos OK`. Con los 12 casos de Prompt 2B el conjunto completo pasa
  `41/41 casos OK`.

## Pendiente

- Prompt 3.5: restringir `todf` a `ulongint` o enum con base `ulongint`; rechazar
  conversiones vacías, de `doublef` o de objetos.
- Tema 19: chequear tipos de parámetros reales contra los formales. La cantidad y el orden
  ya se verifican; la ubicación del formal también se resuelve desde Prompt 2B.
- Temas 28/31: validar existencia, visibilidad y herencia de atributos y métodos. El motor
  de tipos ya obtiene el tipo cuando la referencia es resoluble, incluido el prefijo
  explícito.

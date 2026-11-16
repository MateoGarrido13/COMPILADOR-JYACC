**Diagnóstico**

Las filas usan el **número de tema del grupo** (regla de la consigna). La iteración del
plan (`Prompt 3.6`, etc.) y el archivo `docs/PROMPT-NN.md` se relacionan en la tabla de
`readme.md` (sección «Convención de nombres»).

| Tema | Estado y faltante |
|---|---|
| **6 y 8: `ulongint`, `doublef`** | Implementado en el Prompt 3.1: el léxico inserta las constantes con tipo `ulongint` o `doublef` y uso constante. Ver `docs/PROMPT-6-8.md`. |
| **9 y 16: cadenas y comentarios** | No detecté un faltante léxico importante: se valida que las cadenas sean de una línea, se les asigna tipo y los comentarios multilínea se descartan. Hay casos TP1 para estos temas. (`AccionCadena.java`) |
| **13: `repeat ... while`** | Implementado en el Prompt 3.3: la condición exige el mismo tipo y rechaza `cadena`; el cuerpo admite las sentencias ejecutables. Ver `docs/PROMPT-13.md`. |
| **17: asignación dentro de expresiones** | Implementado en el Prompt 3.4: la asignación interna emite `(:=, destino, valor)`, compara tipos y rechaza el anidamiento. Ver `docs/PROMPT-17.md`. |
| **19: orden obligatorio de parámetros** | Implementado en el Prompt 3.6: se validan cantidad, orden, formales y tipos; se generan `PAR`/`CALL` o `CALLMET` y se propaga el tipo de retorno. Ver `docs/PROMPT-19.md` y `test/tp2/verificacion_r19_param_reales/`. |
| **23: enumeraciones** | Implementado en el Prompt 3.7: se validan redeclaraciones, ámbito, base homogénea, tipos de variables y pertenencia de constantes; las operaciones usan el tipo base. Ver `docs/PROMPT-23.md` y `test/tp2/verificacion_r23_enums/`. |
| **25: asignación de objetos** | Implementado en el Prompt 3.8: `clasesCompatibles` exige la misma cantidad de atributos y el mismo orden de tipos; la asignación entre objetos emite `(COPY, destino, origen)`. Ver `docs/PROMPT-25.md`, `docs/DEC-9.md` y `test/tp2/verificacion_r25_objetos/`. |
| **28: acceso tradicional a atributos** | Implementado en el Prompt 3.9: `objeto.atributo` exige un receptor de tipo clase y un atributo propio; la asignación emite `(:=, objeto.atributo, valor)`. Al declarar el objeto se materializan sus atributos como entradas propias en la TS. Ver `docs/PROMPT-28.md` y `docs/DEC-10.md`. |
| **31: herencia múltiple con prefijo** | Implementado en el Prompt 3.10: la base se resuelve y se valida, el miembro heredado exige prefijo y la llamada aceptada emite `(CALLMET, objeto, metodo)`. Los atributos heredados se materializan por objeto con el prefijo de la clase declarante. Ver `docs/PROMPT-31.md`, `docs/DEC-10.md` y `test/tp2/verificacion_r28_r31_atributos/`. |
| **33: conversión explícita `todf`** | Implementado en el Prompt 3.5: acepta `ulongint` o un enumerado con esa base, rechaza el resto con `todf solo convierte enteros a doublef` y emite `(todf, valor, --)`. Ver `docs/PROMPT-33.md`. |

**Faltantes transversales de TP03**

- El generador ya emite asignaciones, `if`, `repeat`, `pout`, `ret`, llamadas y conversiones, y delimita funciones y métodos. Aún hay que revisar cobertura completa de los temas TP03. (`GeneradorTercetos.java`)
- `EntradaTabla.Uso` distingue variables, funciones, parámetros, clases y demás identificadores; las firmas de funciones también se validan al invocarlas.
- Las pruebas TP02 cubren llamadas y parámetros reales en `test/tp2/verificacion_r19_param_reales/`, enumeraciones en `test/tp2/verificacion_r23_enums/`, la asignación de objetos en `test/tp2/verificacion_r25_objetos/` y la herencia con prefijo en `test/tp2/verificacion_r28_r31_atributos/`. Las pruebas TP03 cubren además asignaciones, selección e iteración.

**Prioridad sugerida:** los temas `17`, `19`, `23`, `25`, `28`, `31` y `33` ya están implementados. El `repeat while` ya tiene una base de generación funcional.
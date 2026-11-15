**Diagnóstico**

| Tema | Estado y faltante |
|---|---|
| **6 y 8: `ulongint`, `doublef`** | El léxico reconoce los formatos y rangos, pero TP03 exige registrar el tipo de cada constante durante el análisis léxico. Las acciones léxicas insertan las constantes sin asignarles tipo; se tipan después, al procesarlas como factores. (enteros, reales, `EntradaTabla.java:1-22`) |
| **9 y 16: cadenas y comentarios** | No detecté un faltante léxico importante: se valida que las cadenas sean de una línea, se les asigna tipo y los comentarios multilínea se descartan. Hay casos TP1 para estos temas. (`AccionCadena.java`) |
| **13: `repeat ... while`** | La generación básica del ciclo está implementada: una prueba actual produce etiqueta, condición, `BF` y salto de regreso. Falta integrar los chequeos de tipos de condiciones exigidos por TP03; tampoco están cubiertas todas las sentencias ejecutables. (`GeneradorTercetos.java:57-65`, prueba) |
| **17: asignación dentro de expresiones** | Se reconoce la sintaxis y se detecta el anidamiento, pero no se genera la asignación interna ni se chequean sus tipos. En la prueba actual, `y = (2$ul)` se reduce a su valor y no genera una asignación a `y`. (`AccionesExpresiones.java:31-40`, `Verificaciones.java:52-56`, `GeneradorTercetos.java:187-214`) |
| **19: orden obligatorio de parámetros** | Implementado en el Prompt 3.6: se validan cantidad, orden, formales y tipos; se generan `PAR`/`CALL` o `CALLMET` y se propaga el tipo de retorno. Ver `docs/PROMPT-3.6.md` y `test/tp2/verificacion_r19_param_reales/`. |
| **23: enumeraciones** | Implementado en el Prompt 3.7: se validan redeclaraciones, ámbito, base homogénea, tipos de variables y pertenencia de constantes; las operaciones usan el tipo base. Ver `docs/PROMPT-3.7.md` y `test/tp2/verificacion_r23_enums/`. |
| **25: asignación de objetos** | Implementado en el Prompt 3.8: `clasesCompatibles` compara atributos y firmas, y la asignación entre objetos emite `(COPY, destino, origen)`. Ver `docs/PROMPT-25.md`, `docs/DEC-9.md` y `test/tp2/verificacion_r25_objetos/`. |
| **28: acceso tradicional a atributos** | La gramática admite `objeto.atributo`, pero la acción devuelve una referencia sin verificar que el atributo exista y sea visible en la clase del objeto. (`gramatica.y:530-543`, `AccionesExpresiones.java:55-60`) |
| **31: herencia múltiple con prefijo** | Se guardan los nombres de las clases heredadas y se acepta la forma prefijada. Faltan validar las clases base, que el prefijo corresponda a una clase heredada y que el atributo o método exista y sea accesible. Las llamadas a métodos tampoco generan código. ([registro de herencia](Entorno.java#L188-L205), `AccionesExpresiones.java:55-70`) |
| **33: conversión explícita `todf`** | Se reconoce la conversión, pero la acción semántica devuelve la expresión sin transformarla. No se emite un terceto de conversión ni se comprueba la compatibilidad de tipos en expresiones, comparaciones y asignaciones. La ejecución de la prueba muestra `z := todf(x)` como una asignación directa de `x` a `z`. (`AccionesExpresiones.java:42`, `GeneradorTercetos.java:187-214`) |

**Faltantes transversales de TP03**

- El generador ya emite asignaciones, `if`, `repeat`, `pout`, `ret`, llamadas y conversiones, y delimita funciones y métodos. Aún hay que revisar cobertura completa de los temas TP03. (`GeneradorTercetos.java`)
- `EntradaTabla.Uso` distingue variables, funciones, parámetros, clases y demás identificadores; las firmas de funciones también se validan al invocarlas.
- Las pruebas TP02 cubren llamadas y parámetros reales en `test/tp2/verificacion_r19_param_reales/`, enumeraciones en `test/tp2/verificacion_r23_enums/` y la asignación de objetos en `test/tp2/verificacion_r25_objetos/`. Siguen faltando casos de atributos y herencia; las pruebas TP03 cubren además asignaciones, selección e iteración.

**Prioridad sugerida:** los temas `17`, `19`, `23`, `25` y `33` ya están implementados. Continuar con atributos (`28`) y herencia (`31`). El `repeat while` ya tiene una base de generación funcional.
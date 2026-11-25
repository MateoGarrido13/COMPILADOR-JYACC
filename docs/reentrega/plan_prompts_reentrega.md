# Plan de prompts atómicos — reentrega grupo 43

Versión 2. Segunda pasada estática sobre los `.java`. Reemplaza la versión 1.

> Nota de cierre P6-01 (24/11/2026): la decisión de aceptar recursión quedó sin
> efecto. Los enunciados de TP01–TP04 no exigen recursión y el profesor solo
> observó que el test incorporado de factorial no funcionaba; esa observación no
> convierte la recursión en requisito de esta reentrega. Ola 5 no se ejecutó.
> Estado final: una llamada a una función todavía abierta se descarta
> silenciosamente; no genera `PAR`/`CALL` interno ni mensaje de error. El
> programa principal y sus llamadas externas sí se emiten. Las secciones que
> describen P0-03 y P5A son registro histórico, no trabajo pendiente.

Regla: **un prompt = un cambio = un commit = una verificación**. No mezclar léxico con parser, ni parser con IR, ni IR con assembler, ni código con documentación.

---

## 0. Convenciones

### 0.1 Formato

```text
ID:
Tema:
Raíz confirmada:
Archivos:
Cambio permitido:
Prohibido:
Verificación:
Aceptación:
Dependencias:
Confirmación estática:   <- resultado de la segunda pasada sobre el código actual
```

### 0.2 Decisiones ya tomadas

| Tema | Decisión | Consecuencia |
|---|---|---|
| IR con errores | **El programa se entrega completo aunque haya errores.** `Reporte` acumula mensajes; el análisis sigue. Una construcción inválida no genera sus propios tercetos, pero el resto del programa sí. | No hay compuerta global `hayErrores()`. No se descarta IR de TP03. |
| TP03 / `enum_vacio` | **Salida actual correcta. Sin cambios.** | `Ausencia de valores…` + `tipo no declarado 'color'` + `(pout, x, --)` + `(pout, ok, --)` se acepta. Ola 3 queda cerrada sin prompts. |
| Recursión | **Fuera de alcance en esta reentrega.** | Decisión P0-03 dejada sin efecto. No se ejecuta Ola 5. La llamada a función abierta se descarta silenciosamente; sin `PAR`/`CALL` interno y sin mensaje. |
| TP04 layout | **Jump al inicio del main.** | Elegido por ser el de menos líneas y no requerir regenerar `Parser.java` ni reordenar el IR. |

### 0.3 Tabla de raíces

| ID problema | Raíz confirmada | Estado |
|---|---|---|
| TP01 | `MatrizTransiciones.MATRIZ_ESTADOS[16][20] = 99` + lookahead; `errorLexico()` no ve el carácter ni el resto del ID | A corregir |
| TP02-a | `Parser.java:1330-1331` ejecuta `ERR_ASIG_DONDE_IGUAL` y después `ASIGNACION_EN_EXPRESION`, devolviendo wrapper válido | A corregir |
| TP02-b | `Parser.java:1343,1347`: el resultado de `ERR_FALTA_ORDEN_EVALUACION` es `valores[0]` = ID `foo` (`TablaAcciones.java:41`) | A corregir |
| TP02-c | `ExpresionDiferida.deOperando()` no propaga invalidez de `LlamadaFuncion` | Consistencia; no afecta los dos casos |
| TP03 | No es defecto según decisión 0.2 | Sin cambios |
| TP04-a | `GeneradorAssembler.emitirCodigo()` emite `start:` y cae en `fn_*` sin `CALL` | A corregir |
| TP04-b | `Entorno.getFuncion()` solo consulta `funciones` | Fuera de alcance; sin cambio |
| TP04-c | `emitirFinFuncion()` agrega `leave/ret` duplicado | No corregido: P4-04 se revirtió para conservar el epílogo de funciones sin `ret` |
| TP04-d | Documentación afirmaba un diagnóstico de recursión inexistente | Corregido en P6-01 |

---

# 1. Ola 0 — baseline y decisiones

## P0-01 — Congelar baseline

```text
ID: P0-01
Tema: Baseline reproducible antes de tocar código.
Raíz confirmada: No hay una única referencia de salida para casos inválidos.
Archivos: solo /tmp.
Cambio permitido:
  - Copiar fuentes a /tmp/opencode/tp3build_baseline.
  - Guardar consola y .asm de los seis casos críticos en /tmp/opencode/baseline/.
Prohibido:
  - Modificar fuentes, tests o docs del repo.
Verificación:
  - javac -encoding UTF-8 -d /tmp/opencode/tp3build_baseline *.java "acciones semanticas"/*.java otras_acciones/*.java
  - java -cp /tmp/opencode/tp3build_baseline Main <caso>
Aceptación:
  - Baseline guardado para caracter_invalido.txt, 17_asig_donde_igual.txt,
    18_falta_orden.txt, 21_enum_vacio.txt, 01_funcion_ulongint.txt,
    03_recursion_factorial_metodo.txt.
Dependencias: ninguna.
Confirmación estática: Compilador.java:82-107 genera IR y lo imprime siempre; la única compuerta es generarArchivoAsm() en :141-144. El baseline actual es estable.
```

## P0-02 — Política de IR ante errores

```text
ID: P0-02
Tema: Contrato de salida para programas con errores.
Raíz confirmada: No existía política explícita; se interpretaba que error debía borrar IR.
Archivos: docs/reentrega/plan_prompts_reentrega.md, docs/PROMPT-0.md.
Cambio permitido:
  - Escribir: "Los errores se informan todos juntos; el análisis continúa y el
    programa se entrega completo. Una reducción inválida no aporta tercetos;
    las reducciones válidas posteriores sí".
  - Separar en expected results: mensaje de error, IR del constructor inválido,
    IR del resto del programa.
Prohibido:
  - Agregar if (reporte.hayErrores()) en GeneradorTercetos o Compilador.
  - Usar descartarDesde() por error semántico general.
  - Tocar el caso 21_enum_vacio.txt.
Verificación:
  - Revisar filas de docs/PROMPT-0.md.
Aceptación:
  - Política aplicable a TP01, TP02 y TP03 sin borrar IR ajeno al error.
Dependencias: P0-01.
Confirmación estática: GeneradorTercetos.generar() (:147-207) no consulta Reporte; Compilador.java:93,107 imprime contenido() siempre. La política ya es "seguir generando"; solo faltaba escribirla.
```

## P0-03 — Recursión: decisión A (sin efecto)

```text
ID: P0-03
Tema: Soporte de recursión directa.
Raíz confirmada: test_tp_4/Integracion/03_recursion_factorial_metodo.txt es positivo y el informe entregado describe llamadas y stack frames; hoy la llamada recursiva se suprime en silencio.
Archivos: docs/reentrega/plan_prompts_reentrega.md.
Cambio permitido:
  - Fijar A: recursión directa soportada.
  - Declarar que no se instrumentará el chequeo runtime (i): no pertenece al grupo 43 (befh).
Prohibido:
  - Rechazar recursión.
  - Reclasificar el test de factorial como inválido.
Verificación:
  - readme.md:7 y enunciado TP04: chequeo i = recursión; grupo 43 = b,e,f,h.
Aceptación:
  - Rama A obligatoria; P5A-01 .. P5A-04 entran en el orden.
Dependencias: P0-01.
Confirmación estática: ASSETS/recursion.md y DIAGNOSTICO_FUNCIONES_METODOS.md describen rechazo, pero grep "Recursion no soportada" da 0 resultados en *.java. El silencio real está en Verificaciones.java:352-354 y :432-435 cuando firma == null.
```

> Estado final P6-01: esta decisión no se ejecutó. La rama A no es requisito de
> la reentrega y P5A-01 .. P5A-04 no entran en el orden de trabajo.

---

# 2. Ola 1 — TP01: carácter inválido dentro de identificador

## P1-01 — Estado dedicado “ID inválido”

```text
ID: P1-01
Tema: Matriz léxica y recuperación en un solo cambio.
Raíz confirmada: MATRIZ_ESTADOS[16][20] = 99 (ESTADO_F). Con `id#malo`, `esLookahead()` devuelve true para '#': no se agrega al lexema, se retrocede y AccionIdentificador valida solo "id". Después '#' se lee desde estado 0 con lexema vacío.
Archivos: MatrizTransiciones.java, AnalizadorLexico.java.
Cambio permitido:
  - MATRIZ_ESTADOS[16][20] = 19 (nuevo estado ID_INVALUIDO).
  - Fila 19: columnas 0,1,2,16,20 -> 19; resto -> 99.
  - MATRIZ_SEMANTICAS fila 19 = acción nueva 37 en todas las columnas.
  - semCierre(19) = 37.
  - Case 37 en ejecutarAccion(): reporte.error(linea, "Identificador invalido '" + lexema + "'"); return -1.
Prohibido:
  - Cambiar esLookahead().
  - Cambiar AccionIdentificador.java.
  - Convertir el lexema en dos tokens.
Verificación:
  - java ... Main /tmp/opencode/lexprobe/b_rhs.txt
Aceptación:
  - `id#malo` entrega UN error con el lexema completo.
  - Cero tokens ID derivados de ese lexema.
  - No aparece `Lexema no reconocido ''`.
Dependencias: P0-02.
Confirmación estática: AnalizadorLexico.java:56-57 llama errorLexico() antes de appendear; por eso -1 directo daría "Lexema no reconocido 'id'" y dejaría `malo` como token nuevo. El estado 19 evita ambos problemas. LectorFuente.retroceder() (:57-64) admite hasta 16 caracteres, suficiente para IDs de 22+ sufijo si se ampliara; con estado 19 no se usa retroceso para el ID inválido.
```

## P1-02 — Mensaje del error léxico residual

```text
ID: P1-02
Tema: Robustez del mensaje.
Raíz confirmada: errorLexico() (:122-125) usa contexto.lexema, vacío en el camino de ERROR desde estado 0.
Archivos: AnalizadorLexico.java.
Cambio permitido:
  - Si lexema está vacío, incluir contexto.caracter: "Lexema no reconocido '#'".
Prohibido:
  - Cambiar formato de Reporte.
  - Tocar la matriz.
Verificación:
  - Caso con '@' o '$' suelto fuera de sufijo.
Aceptación:
  - Ningún mensaje con lexema vacío.
Dependencias: P1-01.
Confirmación estática: ContextoLexico.columna() (:61) manda cualquier carácter no listado a la columna 20; desde estado 0 esa celda es -1. El mensaje actual es engañoso pero no afecta IR.
```

## P1-03 — Regresión TP01

```text
ID: P1-03
Tema: Pruebas.
Raíz confirmada: Los casos actuales no verifican ausencia de ID fragmentado.
Archivos: test/tp1/verificacion_identificadores/.
Cambio permitido:
  - Agregar casos: ID inválido a la izquierda, a la derecha, en declaración.
  - Agregar caso `{id#malo}` para conservar cadenas de una línea.
  - Verificar: error único, sin ID fragmentado, sin asignación derivada.
Prohibido:
  - Borrar caracter_invalido.txt.
Verificación:
  - java -cp ... Main <cada caso>
Aceptación:
  - `(:=, x:main, id:main)` no aparece.
  - `{id#malo}` sigue emitiendo `(pout, id#malo, --)`.
Dependencias: P1-02.
Confirmación estática: AccionCadena consume desde estado 5/6 hasta '}' (MATRIZ_SEMANTICAS 12-18); el estado 19 no alcanza cadenas.
```

---

# 3. Ola 2 — TP02: constructing inválidos

## P2-01 — Expected results tema 17

```text
ID: P2-01
Tema: Documentación.
Raíz confirmada: docs/PROMPT-0.md:119 espera 1 terceto; con el fix serán 0 ejecutables. PROMPT-17.md:45 da el caso por "pasa" por el mensaje, no por el IR.
Archivos: docs/PROMPT-0.md, docs/PROMPT-17.md.
Cambio permitido:
  - Marcar x := y := (1$ul) como INVÁLIDO.
  - Mensaje: Uso de ':=' donde debe usarse '='.
  - IR del constructor: ningún :='.
  - IR del resto del programa: se mantiene.
Prohibido:
  - Cambiar gramática o Java aquí.
Verificación:
  - grep de 17_asig_donde_igual y usa_asig_sin_anidamiento en docs.
Aceptación:
  - Ningún docs dice que el IR de esa construcción es válido.
Dependencias: P0-02.
```

## P2-02 — Expected results tema 19

```text
ID: P2-02
Tema: Documentación.
Raíz confirmada: TP02:224-236 exige [orden]; docs/PROMPT-19.md:103 y PROMPT-0.md:121-122 ya lo dicen, pero falta referencia al caso general 18.
Archivos: docs/PROMPT-0.md, docs/PROMPT-19.md.
Cambio permitido:
  - Agregar/ajustar fila para 18_falta_orden.txt.
  - Sin PAR, sin CALL, sin (:=, x, foo).
Prohibido:
  - Hacer opcional el corchete.
Verificación:
  - Revisar docs y fuentes de test.
Aceptación:
  - Expected results alineados con el enunciado.
Dependencias: P0-02.
```

## P2-03 — `:=` en expresión no debe devolver asignación utilizable

```text
ID: P2-03
Tema: Gramática tema 17.
Raíz confirmada: Parser.java:1330-1331 ejecuta ERR_ASIG_DONDE_IGUAL y luego ASIGNACION_EN_EXPRESION, sobrescribiendo yyval con un wrapper válido. Modificar solo TablaAcciones no alcanza porque la reducción posterior gana.
Archivos: gramatica.y, Parser.java (regenerado), otras_acciones/AccionesExpresiones.java solo si hace falta.
Cambio permitido:
  - En la alternativa ID ASIG '(' expresion ')', reemplazar la segunda sentencia por un valor inválido explícito, p. ej.
    yyval = new ParserVal(new ExpresionDiferida().marcarInvalida());
  - Regenerar Parser.java desde gramatica.y con BYACC/J. No editar Parser.java a mano.
Prohibido:
  - Aceptar `:=`.
  - Tocar la alternativa correcta con '='.
Verificación:
  - 17_asig_donde_igual.txt.
Aceptación:
  - Error presente.
  - Cero `(:=, y:main, 1$ul)`.
  - Cero `(:=, x:main, [2])`.
  - Otras sentencias válidas del programa siguen emitiendo IR.
Dependencias: P2-01.
Confirmación estática: AccionesExpresiones.java:197-200 marca inválido el wrapper; AccionesDeclaraciones.java:148-150 devuelve null cuando origen no es válida; GeneradorTercetos.java:274-276 y :555-558 descartan ese resultado. Con el valor inválido desde la gramática, la cadena entera se corta sin tocar el generador.
```

## P2-04 — ELIMINADO

```text
ID: P2-04
Estado: eliminado de la versión 1.
Motivo: proponía agregar if (!asignacion.esValida()) dentro de emitir(AsignacionEnExpresion). AsignacionEnExpresion.java no tiene esValida(); la invalidez vive en ExpresionDiferida y GeneradorTercetos.java:555 ya la bloquea. El cambio no compilaría y además es redundante.
```

## P2-05 — Error de orden devuelve expresión inválida

```text
ID: P2-05
Tema: Gramática tema 19, sin regenerar Parser.
Raíz confirmada: Parser.java:1343 y :1347 asignan yyval = new ParserVal(acciones.ejecutar(ERR_FALTA_ORDEN_EVALUACION, ...)). TablaAcciones.java:41 devuelve valores[0] = ID `foo` porque la regla no tiene acción registrada. Ese ID sube como factor y produce (:=, x:main, foo:main).
Archivos: AccionesExpresiones.java (o el archivo donde se registran las acciones de error).
Cambio permitido:
  - Registrar acción para Reglas.ERR_FALTA_ORDEN_EVALUACION que devuelva
    new ExpresionDiferida().marcarInvalida().
  - Mantener el mensaje "Falta del orden de evaluacion y asignacion de parametros".
Prohibido:
  - Cambiar el mensaje.
  - Regenerar Parser.java para este prompt.
  - Tocar la alternativa válida con corchetes.
Verificación:
  - 18_falta_orden.txt, sin_orden.txt, orden_sin_corchetes.txt.
Aceptación:
  - Error presente.
  - Cero `(:=, x:main, foo:main)`.
  - Cero PAR y cero CALL para la invocación inválida.
Dependencias: P2-02.
Confirmación estática: Las seis apariciones de ERR_FALTA_ORDEN_EVALUACION en Parser.java (1343,1347,1383,1387,1395,1399) son todas invocaciones de función/método. Una sola acción registrada cubre todas. AccionesDeclaraciones.java:148 convierte la expresión inválida en null y generarAsignacion() no emite.
```

## P2-06 — Propagar invalidez de LlamadaFuncion al envolverla

```text
ID: P2-06
Tema: Consistencia de expresiones.
Raíz confirmada: ExpresionDiferida.java:29-36 propaga invalidez solo cuando el operando ya es ExpresionDiferida; una LlamadaFuncion marcada inválida queda envuelta como válida.
Archivos: ExpresionDiferida.java.
Cambio permitido:
  - Si operando instanceof LlamadaFuncion y !esValida(), marcar la envoltura inválida.
Prohibido:
  - Marcar inválidas llamadas válidas.
  - Cambiar LlamadaFuncion.java.
Verificación:
  - Casos con invocación con corchetes pero firma inexistente; factorial queda fuera de alcance.
Aceptación:
  - ASIGNACION ve origen inválido desde la envoltura.
Dependencias: P2-05.
Confirmación estática: No es necesario para 17/18: GeneradorTercetos.java:541-543 ya descarta LlamadaFuncion inválida al emitir operandos (:564). Se conserva para que el estado semántico y el IR coincidan.
```

## P2-07 — Regresión TP02

```text
ID: P2-07
Tema: Pruebas.
Raíz confirmada: Casos inválidos aún dejan IR del constructor.
Archivos: test/tp2/generales/, test/tp2/verificacion_r17_asig_expresiones/, test/tp2/verificacion_r19_param_reales/.
Cambio permitido:
  - Comparar con expected results corregidos.
  - Agregar assert de "sin CALL" para falta de orden.
Prohibido:
  - Borrar IR de sentencias válidas vecinas.
Verificación:
  - java -cp ... Main <cada caso>
Aceptación:
  - Válidos: mismos tercetos que baseline.
  - Inválidos: sin tercetos del constructor; resto igual que baseline.
Dependencias: P2-03, P2-05, P2-06.
```

---

# 4. Ola 3 — TP03: SIN CAMBIOS

```text
ID: P3-00
Estado: cerrada sin prompts.
Decisión: La salida observada es la correcta según el criterio del usuario.
Ejecución real confirmada con el test modificado:

  Línea 4: Error: Ausencia de valores para la enumeracion
  Línea 5: Error: tipo no declarado 'color'
  1. (PROGRAMA, --, --)
  2. (pout, x, --)
  3. (pout, ok, --)
  4. (FIN_PROGRAMA, --, --)

Por qué es correcta:
  - Verificaciones.java:486-490 devuelve null para lista vacía y reporta el error.
  - AccionesDeclaraciones.java:93 solo registra el tipo si tipoBase != null;
    por eso `color` no existe y `color x;` produce "tipo no declarado".
  - El análisis continúa y emite los pout: coincide con la política P0-02.

Prohibido:
  - Agregar descartes, compuertas hayErrores() ni cambios en Terceto.java.
  - Modificar test/tp2/generales/21_enum_vacio.txt.
Nota documental conocida y aceptada: docs/PROMPT-0.md:126 dice "Ninguno"; se deja intacto por decisión explícita.
```

---

# 5. Ola 4 — TP04: jump al inicio del main

Elegido sobre "mover funciones al final": menos líneas, sin regenerar Parser, sin reordenar IR, sin tocar `prepararDeclaraciones()` ni `declaracionPorTerceto`.

## P4-01 — `jmp main_inicio` + detección local del main

```text
ID: P4-01
Tema: Layout de .code.
Raíz confirmada: GeneradorAssembler.emitirCodigo() (:459-553) escribe "start:\n" y luego traduce el IR lineal. El primer `$ul` no es FUNCION: es el prólogo de la primera subrutina. No hay CALL previo, así que [ebp+8]/[ebp+12] son basura y `ret` no tiene dirección válida.
Archivos: GeneradorAssembler.java, método emitirCodigo().
Cambio permitido:
  - Tras "start:\n", emitir "    jmp main_inicio\n".
  - Agregar boolean dentroSubrutina = false; boolean mainInicioEmitido = false;
  - En el switch: FUNCION/METODO -> dentroSubrutina = true; FIN_FUNCION/FIN_METODO -> dentroSubrutina = false. Mantener las llamadas actuales a emitirPrologo/emitirFinFuncion.
  - Antes de despachar cada terceto: si !dentroSubrutina && !mainInicioEmitido && operador no es PROGRAMA/FIN_PROGRAMA/CLASE/FIN_CLASE/FUNCION/METODO/FIN_FUNCION/FIN_METODO, emitir "main_inicio:\n" y marcar mainInicioEmitido = true.
Prohibido:
  - Reordenar la lista de tercetos.
  - Agregar Reglas.INICIO_MAIN ni regenerar Parser.java.
  - Cambiar la convención de llamadas.
Verificación:
  - 01_funcion_ulongint.asm y 03_recursion_factorial_metodo.asm.
Aceptación:
  - `start:` seguido de `jmp main_inicio`.
  - `fn_*`/`mt_*` quedan después del jmp y antes de `main_inicio:`.
  - `main_inicio:` precede a la primera instrucción ejecutiva del main.
Dependencias: P0-02.
Confirmación estática: GeneradorTercetos.java:223 y :235 emiten FUNCION/METODO y FIN_*; :245 y :266 emiten CLASE/FIN_CLASE. GeneradorAssembler.java:514-541 ya distingue esos operadores en el switch, así que el flag no necesita nuevos datos. Programa y Fin_programa son creados en :152 y :206.
```

## P4-02 — Verificación ASM del salto

```text
ID: P4-02
Tema: Regresión estática.
Raíz confirmada: snapshots actuales muestran start: -> push ebp.
Archivos: /tmp/opencode/tp3run/*.asm y snapshots de test_tp_4.
Cambio permitido:
  - grep -n -A3 '^start:' en los ASM generados.
Prohibido:
  - Cambiar fuentes .txt.
Verificación:
  - sed -n '18,40p' 01_funcion_ulongint.asm
Aceptación:
  - Ningún caso tiene start: inmediatamente seguido de fn_ o mt_ sin jmp intermedio.
Dependencias: P4-01.
```

## P4-03 — Caída a handlers runtime: verificación, no cambio

```text
ID: P4-03
Tema: Revisión de fin_normal / error_*.
Raíz confirmada: GeneradorAssembler.java:562-566 emite jmp fin_normal y :588-591 fin_normal: solo si hay mensajes runtime; :593 emite ExitProcess 0. Con el jump, el main termina antes de esos labels, así que el layout actual ya es válido.
Archivos: ninguno (solo revisión).
Cambio permitido:
  - Verificar que error_* queden detrás del flujo normal.
Prohibido:
  - Mover ExitProcess.
  - Quitar chequeos befh.
Verificación:
  - Caso con overflow_suma y caso sin chequeos.
Aceptación:
  - Sin entrada accidental a error_* por caída secuencial.
Dependencias: P4-02.
```

## P4-04 — Quitar epílogo duplicado

```text
ID: P4-04
Tema: Assembler.
Raíz confirmada: emitirRet() (:1507-1521) ya emite leave/ret; emitirFinFuncion() (:1496-1505) emite fin_<sufijo>: + otro leave/ret muerto.
Archivos: GeneradorAssembler.java.
Cambio permitido:
  - FIN_FUNCION/FIN_METODO emite solo la etiqueta informativa, o nada.
  - No emite leave/ret adicional.
Prohibido:
  - Cambiar emitirRet() ni emitirPrologo().
Verificación:
  - 01_funcion_ulongint.asm, 03_recursion_factorial_metodo.asm.
Aceptación:
  - No aparece leave / ret / fin_... / leave / ret.
Dependencias: P4-01.
```

## P4-05 — Revisión de frames y argumentos

```text
ID: P4-05
Tema: Revisión guiada.
Raíz confirmada: emitirLlamada() (:1530-1628) calcula slots, sub esp, copia, call, add esp y guarda retorno en @auxN. La convención es coherente cuando se llega por CALL; el fallo era llegar sin CALL.
Archivos: GeneradorAssembler.java, solo revisión salvo bug demostrado.
Cambio permitido:
  - Anotar o corregir únicamente un desalineo demostrado por ASM.
Prohibido:
  - Rediseñar paso de parámetros.
  - Cambiar doublef de DQ o ST(0).
Verificación:
  - 01_funcion_ulongint.asm, 02_funcion_doublef.asm, 05_metodo_con_this.asm.
Aceptación:
  - [ebp+8] primer parámetro de función; [ebp+8] = this en método; caller limpia pila.
Dependencias: P4-04.
```

## P4-06 — Regenerar snapshots ASM

```text
ID: P4-06
Tema: Regresión.
Archivos: snapshots .asm de test_tp_4.
Cambio permitido:
  - Regenerar snapshots con el generador corregido.
Prohibido:
  - Cambiar fuentes .txt del test.
Verificación:
  - diff de snapshots; grep start: + fn_.
Aceptación:
  - Diff explicado por P4-01 únicamente; P4-04 fue revertido.
Dependencias: P4-03, P4-05. P4-04 se evaluó y se revirtió.
```

---

# 6. Ola 5 — no ejecutada

> Esta ola quedó fuera de la reentrega por decisión del usuario. El bloque
> siguiente se conserva solo como registro del análisis original; ninguno de sus
> prompts se ejecutó ni debe interpretarse como pendiente.

## P5A-01 — Fallback de `getFuncion()` a `pilaFunciones` (no ejecutado)

```text
ID: P5A-01
Tema: Resolución de firma abierta.
Raíz confirmada: Entorno.java:174-179 solo consulta funciones.get(clave). El comentario de :165-172 ya documenta el fallback a pilaFunciones, pero no lo implementa. cerrarFuncion() (:283-284) inserta en funciones recién al cerrar, por lo que dentro del cuerpo `fact` no es visible.
Archivos: Entorno.java.
Cambio permitido:
  - Buscar primero en funciones.
  - Si no está, recorrer pilaFunciones y comparar clave().
  - Devolver null si no aparece en ninguno.
Prohibido:
  - Publicar la función abierta en el mapa funciones.
  - Cambiar cerrarFuncion().
Verificación:
  - 03_recursion_factorial_metodo.txt.
Aceptación:
  - IR con (PAR, n:main:fact, ...), (CALL, fact:main, 1) y (*, n:main:fact, [CALL]).
  - Igual al experimento ya realizado en /tmp/opencode/tp3patched/.
Dependencias: P0-03, P4-01.
Confirmación estática: AccionesExpresiones.java:240 obtiene firma = entorno.getFuncion(resuelta.lexema); :249-250 marca la llamada inválida si firma == null; Verificaciones.java:352-354 y :432-435 devuelven false sin mensaje; GeneradorTercetos.java:342-344 descarta la llamada inválida. El parche de una sola función en getFuncion() ya produjo el IR recursivo esperado, así que no hacen falta cambios en GeneradorTercetos ni en GeneradorAssembler para emitir el CALL.
```

## P5A-02 — Alinear documentación de recursión

```text
ID: P5A-02
Tema: Docs.
Raíz confirmada: ASSETS/recursion.md:53-54 y DIAGNOSTICO_FUNCIONES_METODOS.md:390-394 afirman que existe "Recursion no soportada: la funcion no puede invocarse a si misma"; ese mensaje no existe en Java y la salida real era silenciosa.
Archivos: ASSETS/recursion.md, ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md (o sus copias en docs/reentrega/).
Cambio permitido:
  - Declarar recursión directa soportada, con el alcance de P5A-03.
  - Quitar la afirmación de rechazo.
Prohibido:
  - Instrumentar chequeo runtime i.
Verificación:
  - grep -rn "Recursion no soportada" --include=*.md .
Aceptación:
  - Docs y código coinciden.
Dependencias: P5A-01.
```

## P5A-03 — Temporales vivos a través del CALL recursivo

```text
ID: P5A-03
Tema: Corrección de factorial anidado.
Raíz confirmada: El IR corregido evalúa (n - 1) en @aux8, hace CALL y después usa n desde [ebp+8] para el producto. Ese n sobrevive porque está en el frame, pero cualquier @auxN vivo del caller puede ser sobrescrito por la activación interior. El informe_generador_tercetos.md:527-536 ya advierte que @auxN son globales.
Archivos: GeneradorAssembler.java.
Cambio permitido:
  - Estrategia mínima: antes de cada CALL recursivo, guardar en el stack frame los @auxN que el generador marque como vivos después de la llamada, y restaurarlos al volver.
  - Alcance mínimo: expresión n * fact(n-1).
Prohibido:
  - Mover todos los @auxN a .DATA.
  - Cambiar el resultado de llamadas no recursivas.
Verificación:
  - factorial 5 -> 120; escala(120) -> 14400 en MASM32.
Aceptación:
  - Resultado correcto con activaciones anidadas.
Dependencias: P5A-01, P4-01.
Confirmación estática: emitirLlamada() guarda el retorno en @auxN (:1530-1628). Sin preservación, la activación interior reutiliza el mismo nombre global; el frame propio de n está en [ebp+8], por eso el factor n del producto no se pierde, pero los temporales del caller sí pueden perderse en expresiones más largas.
```

## P5A-04 — Regresión factorial e integral

```text
ID: P5A-04
Tema: Pruebas.
Raíz confirmada: El test describe un call recursivo que antes no se generaba.
Archivos: test_tp_4/Integracion/03_recursion_factorial_metodo.txt y .asm, 07_programa_integral_6_2.*.
Cambio permitido:
  - Confirmar expectativa 120 y 14400.
  - Regenerar ASM.
Prohibido:
  - Quitar el caso.
Verificación:
  - MASM32: ml.exe / link.exe / ejecutar.
  - Linux: verificación estática del ASM.
Aceptación:
  - CALL recursivo presente; salto inicial presente; resultados correctos.
Dependencias: P5A-03, P4-06.
```

Rama B (rechazo explícito de recursión) quedó descartada en el plan original por
P0-03 = A, pero la decisión A también quedó sin efecto en P6-01. La recursión no
se implementa ni se documenta como soportada o rechazada con mensaje.

---

# 7. Ola 6 — cierre

## P6-01 — Auditoría final de documentación

```text
ID: P6-01
Tema: Docs.
Raíz confirmada: Dos contradicciones a corregir: diagnóstico de recursión inexistente y conteo de tercetos de PROMPT-0.md:119. La fila de enum_vacio queda intacta por decisión P0-02.
Archivos: docs/PROMPT-0.md, docs/PROMPT-17.md, docs/PROMPT-19.md, ASSETS/*.md, docs/reentrega/*.md.
Cambio permitido:
  - Alinear frases sobre recursión y sobre IR de construcciones inválidas.
Prohibido:
  - Cambiar el caso enum_vacio.
  - Cambiar código.
Verificación:
  - grep -rniE 'recursi|v.lido|terceto' docs ASSETS.
Aceptación:
  - Sin afirmaciones que contradigan el código final.
Dependencias: P1-03, P2-07, P4-06. P5A-04 no se ejecutó.
```

## P6-02 — Suite completa

```text
ID: P6-02
Tema: Validación global.
Archivos: scripts de test_tp_4 y test/TP3_SEMANTICAS.
Cambio permitido:
  - Ejecutar todas las suites en Linux.
  - Regenerar solo snapshots afectados por prompts aprobados.
Prohibido:
  - Aceptar diffs no explicados.
Verificación:
  - bash test_tp_4/probar_prompt6_2.sh y scripts individuales.
Aceptación:
  - Cero fallos; cada diff tiene un ID de prompt.
Dependencias: P6-01.
```

## P6-03 — Checklist MASM32

```text
ID: P6-03
Tema: Validación final en Windows.
Raíz confirmada: No hay MASM en Linux; TP04 exige ejecución real.
Archivos: ninguno; resultado documentado en docs/reentrega/.
Cambio permitido:
  - Ensamblar y ejecutar 00_programa_valido, 01_funcion_ulongint,
    02_funcion_doublef y 07_programa_integral_6_2.
  - El caso 03_recursion_factorial_metodo puede ensamblarse como regresión de
    layout, pero no es prueba de recursión ni se evalúa con factorial 120.
Prohibido:
  - Dar por cerrado TP04 sin esta verificación.
  - Presentar factorial como evidencia de recursión soportada.
Verificación:
  - ml.exe / link.exe / ejecución.
Aceptación:
  - Sin errores de ensamblado.
  - Sin entrada accidental a subrutina.
  - Las salidas de llamadas no recursivas coinciden con el IR generado.
Dependencias: P6-02.
```

---

# 8. Orden de ejecución

```text
P0-01 → P0-02

P0-03 no ejecutado: decisión A dejada sin efecto en P6-01

P1-01 → P1-02 → P1-03

P2-01 → P2-02 → P2-03 → P2-05 → P2-06 → P2-07

P3-00   (sin acciones)

P4-01 → P4-02 → P4-03 → P4-04 → P4-05 → P4-06

P5A-01 → P5A-02 → P5A-03 → P5A-04  (ola no ejecutada)

P6-01 → P6-02 → P6-03
```

P2-03 requiere regenerar `Parser.java`. P2-05 no: se resuelve con una acción registrada en `TablaAcciones`.

---

# 9. Definition of done

1. TP01: `id#malo` produce un error con lexema completo; cero IDs fragmentados; el resto del programa sigue emitiendo IR.
2. TP02: `:=` en expresión y falta de `[orden]` no generan tercetos del constructor; los mensajes siguen presentes; el resto del programa sigue entregándose completo.
3. TP03: sin cambios; salida actual aceptada.
4. TP04: `start:` salta a `main_inicio`; ningún prólogo se ejecuta sin `CALL`.
5. TP04: `fin_normal`/`error_*` no se alcanzan por caída secuencial.
6. TP04: se conserva `leave/ret` en `fin_*` porque P4-04 fue revertido; las funciones sin `ret` necesitan esa salida.
7. Recursión: fuera de alcance; llamada a función abierta descartada silenciosamente, sin `CALL` interno y sin mensaje.
8. Docs: sin diagnósticos inexistentes ni conteos contradichos; `enum_vacio` intacto.
9. MASM32: casos críticos ensamblan y ejecutan sin errores.
</think:6124c78e><tool_call:6124c78e>shell<arg_key:6124c78e>command</arg_key:6124c78e><arg_value:6124c78e>wc -l docs/reentrega/plan_prompts_reentrega.md && git status --short
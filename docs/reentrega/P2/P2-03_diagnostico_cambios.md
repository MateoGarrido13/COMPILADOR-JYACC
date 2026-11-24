# P2-03 — `:=` en expresión no debe devolver asignación utilizable

Fecha: 24/11/2026  
Prompt del plan: `P2-03`, “Gramática tema 17” (`docs/reentrega/plan_prompts_reentrega.md:239-262`).  
Estado inicial: commit `5afeb88` (`2-02 actualización de documentación`), árbol limpio. P2-01 y P2-02 ya commiteados.  
Dependencia: P2-01 (expected results alineados en `PROMPT-0.md` y `PROMPT-17.md`).

---

## 1. Alcance del prompt

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
```

Restricciones respetadas:

- Se editó `gramatica.y`.
- Se regeneró `Parser.java` con BYACC/J; **no se editó a mano**.
- No se tocó la alternativa con `=`.
- No se modificó `AccionesExpresiones.java`: no hizo falta.
- No se modificaron tests ni documentación de expected results (P2-01 ya los dejó listos).

---

## 2. Raíz del defecto

La gramática contenía:

```yacc
asignacion_en_expresion
    : ID '=' '(' expresion ')'
        { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    | ID ASIG '(' expresion ')'
        { acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
    ;
```

En `Parser.java` la reducción 138 era:

```java
case 138:
//#line 507 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
```

Secuencia exacta:

1. `acciones.ejecutar(ERR_ASIG_DONDE_IGUAL)` emite `Uso de ':=' donde debe usarse '='`.
2. Inmediatamente después, la misma reducción llama a `ASIGNACION_EN_EXPRESION` con `y` y `1$ul`.
3. `acciones.ejecutar()` devuelve un wrapper válido y `yyval` queda sobreescrito.
4. Ese wrapper sube por la expresión, `AccionesDeclaraciones.java:148-150` lo convierte en una asignación normal y el generador emite los dos tercetos.

Por eso modificar solo `TablaAcciones` no alcanzaba: la reducción posterior ganaba. El error se informaba, pero el IR seguía construyéndose.

---

## 3. Baseline de ejecución

Antes del cambio, con el build de `HEAD` (`5afeb88`):

```text
== 17_asig_donde_igual.txt
Línea 4: Error: Uso de ':=' donde debe usarse '='
Estructuras:
Línea 4: Asignacion en expresion
Código intermedio:
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)
```

```text
== usa_asig_sin_anidamiento.txt
Línea 5: Error: Uso de ':=' donde debe usarse '='
Estructuras:
Línea 5: Asignacion en expresion
Código intermedio:
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)
```

Esexactamente lo que P2-01 había marcado como inválido en `PROMPT-0.md:119` y `PROMPT-17.md:45`.

---

## 4. Herramienta y comando de regeneración

BYACC/J no está incluido en el repo (`readme.md:137` indica que el parser ya viene generado). Se usó el binario local:

```text
/home/rafael/Downloads/byaccj1.15_linux.tar.gz
```

Extraído a `/tmp/opencode/byaccj/`:

```text
/tmp/opencode/byaccj/yacc.linux
```

`file` lo identifica como ELF de 32 bits. Ejecuta sin problemas en el entorno.

Comando usado, dentro de un árbol temporario con la gramática ya modificada:

```bash
/tmp/opencode/byaccj/yacc.linux -Jvalue=ParserVal gramatica.y
```

**Por qué `-Jvalue=ParserVal`:** la opción declara `ParserVal` como clase de valor definida por el usuario, evitando que el generador reemplace el `ParserVal.java` del repo. El `ParserVal.java` emitido por el generador quedó byte a byte idéntico al del repo, así que tampoco habría habido pérdida; aun así se descartó la copia generada y se conservó la original.

Hash de `ParserVal.java`, sin cambios:

```text
7dfcc31a394a9546abf78e1b50643cbeb1262cec4c5e67a230dba3ab465395e5  ParserVal.java
```

---

## 5. Cambio en `gramatica.y`

Único diff de la gramática:

```diff
@@ -505,7 +505,7 @@ asignacion_en_expresion
         { $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
     | ID ASIG '(' expresion ')'
         { acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
-          $$ = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, $1.obj, $4.obj)); }
+          $$ = new ParserVal(new ExpresionDiferida().marcarInvalida()); }
     ;
```

**Por qué cada parte:**

1. **`acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL)` se conserva**  
   El mensaje exigido por el tema 17 y por P2-01 no se toca.

2. **`new ExpresionDiferida()`**  
   Es el tipo que viaja por las reglas de expresión. No hace falta import porque la clase está en el paquete por defecto, igual que `Parser.java`.

3. **`.marcarInvalida()`**  
   Activa el flag `valida = false` (`ExpresionDiferida.java:32-37`). Según la confirmación estática del plan, `AccionesDeclaraciones.java:148-150` devuelve `null` cuando el origen no es válida, y `GeneradorTercetos.java:274-276` / `:555-558` descartan ese resultado. La cadena se corta sin tocar el generador.

4. **La alternativa con `=` queda intacta**  
   La primera producción (`ID '=' '(' expresion ')'`) no se modificó. `estructura_respetada.txt` sigue emitiendo sus asignaciones internas y externas.

---

## 6. `Parser.java` regenerado

Se copió el `Parser.java` generado por BYACC/J al repo. No se aplicaron parches manuales.

Reducción 138 resultante:

```java
case 138:
//#line 507 "gramatica.y"
{ acciones.ejecutar(Reglas.ERR_ASIG_DONDE_IGUAL);
          yyval = new ParserVal(new ExpresionDiferida().marcarInvalida()); }
break;
```

Reducción 137 (alternativa válida), sin cambios:

```java
case 137:
//#line 505 "gramatica.y"
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ASIGNACION_EN_EXPRESION, val_peek(4).obj, val_peek(1).obj)); }
break;
```

### 6.1 Cambio colateral del generador

La regeneración con el BYACC/J 1.15 disponible produce un esqueleto de pila semántica distinto del `Parser.java` que estaba commiteado. Es salida del generador, no una edición manual:

| Sección | Antes (check-in) | Después (generado) |
|---|---|---|
| Comentario de `ParserVal` | `//## **user defined:ParserVal` | `//public class ParserVal is defined in ParserVal.java` |
| `valstk` | `ParserVal valstk[] = new ParserVal[YYSTACKSIZE];` | `ParserVal valstk[];` + asignación en `val_init()` |
| `val_push` | `final void`, con `catch (ArrayIndexOutOfBoundsException)` y duplicado de pila | `void`, con guarda `if (valptr>=YYSTACKSIZE) return;` |
| `val_pop` / `val_peek` | `final`, sin guarda de pila vacía | sin `final`, devuelven `new ParserVal()` si `ptr<0` |
| `dup_yyval` | `return val;` | copia `ival`, `dval`, `sval` y `obj` a un objeto nuevo |

Diff total de `Parser.java`: **55 líneas modificadas** (33 inserciones, 24 borrados), de las cuales una sola línea es cambio semántico (caso 138); el resto es el esqueleto generado.

**Por qué se conservó:** el prompt prohíbe editar `Parser.java` a mano. Reinsertar la pila dinámica anterior habría sido un parche manual sobre un archivo generado. La pila fija por defecto (`YYSTACKSIZE`) se verificó contra las dos suites completas, incluidos `26_extensivo_3000_lineas.txt` y `27_extensivo_200_varioserrores_unalinea.txt`, sin diferencias de salida.

**Recomendación:** toda regeneración futura debe usar el mismo comando (`-Jvalue=ParserVal`) para no introducir más variaciones de esqueleto.

---

## 7. Verificación

### 7.1 Compilación

```bash
javac -encoding UTF-8 -d /tmp/opencode/tp3build_p2_03 \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Resultado: **sin errores**.

### 7.2 Regresión completa

Se construyeron dos árboles temporales desde `git archive HEAD`:

```bash
git archive HEAD | tar -x -C /tmp/opencode/p2_03_refs/baseline_src
cp -a /tmp/opencode/p2_03_refs/baseline_src/. /tmp/opencode/p2_03_refs/mod_src/
# en mod_src: editar gramatica.y y regenerar Parser.java
```

Se ejecutaron todos los `.txt` de `test/` y `test_tp_4/` copiados en árboles separados (para no escribir `.asm` en el repo):

```text
Casos ejecutados: 299
Archivos con diff: 2
```

Los únicos dos archivos con diff:

```text
test/tp2/generales/17_asig_donde_igual.txt.out
test/tp2/verificacion_r17_asig_expresiones/usa_asig_sin_anidamiento.txt.out
```

Diff exacto (idéntico en ambos, cambia solo el número de línea):

```diff
 Línea 2: Declaracion de variables
-Línea 4: Asignacion en expresion
 Línea 4: Asignación
 Línea 5: Bloque BEGIN - END
 Línea 5: Programa

 Código intermedio:
 1. (PROGRAMA, --, --)
-2. (:=, y:main, 1$ul)
-3. (:=, x:main, [2])
-4. (FIN_PROGRAMA, --, --)
+2. (FIN_PROGRAMA, --, --)
```

Interpretación:

- El error `Uso de ':=' donde debe usarse '='` **sigue presente** (no aparece en el diff).
- Desaparece la estructura “Asignacion en expresion”, coherente con el wrapper inválido.
- Desaparecen los dos tercetos prohibidos.
- Quedan `(PROGRAMA)` y `(FIN_PROGRAMA)`: P0-02 se cumple.
- La línea “Asignación” del parser se mantiene: el análisis continúa y el programa se entrega completo.

### 7.3 Casos válidos y anidamiento

Con el código aplicado al repo:

```text
== estructura_respetada.txt
1. (PROGRAMA, --, --)
2. (:=, y:main, 2$ul)
3. (:=, x:main, [2])
4. (+, x:main, y:main)
5. (:=, y:main, [4])
6. (:=, w:main, 1$ul)
7. (*, [5], [6])
8. (:=, x:main, [7])
9. (:=, x:main, 1$ul)
10. (==, [9], 1$ul)
11. (BF, [10], label_1)
12. (pout, ok, --)
13. (label_1, --, --)
14. (FIN_PROGRAMA, --, --)

== anidamiento.txt
Línea 5: Error: Anidamiento de asignacion en expresion
1. (PROGRAMA, --, --)
2. (FIN_PROGRAMA, --, --)
```

La alternativa correcta con `=` sigue emitiendo IR. El anidamiento sigue anulado. Ninguno de los dos cambió respecto del baseline.

### 7.4 Assembler

```text
ASM en baseline: 161
ASM con diff: 0
```

Hash de muestra:

```text
f8bdc552ab3eb68882d09561cada48989862ce939212c4d498fc66955b1d03f9  01_funcion_ulongint.asm (baseline)
f8bdc552ab3eb68882d09561cada48989862ce939212c4d498fc66955b1d03f9  01_funcion_ulongint.asm (modificado)
```

P2-03 no llega al generador de assembler: los casos afectados tienen errores y no escriben `.asm`.

### 7.5 Criterios de aceptación

| Criterio | Cumplido | Evidencia |
|---|---|---|
| Error presente | sí | `Uso de ':=' donde debe usarse '='` sigue en la salida. |
| Cero `(:=, y:main, 1$ul)` | sí | diff de los 2 casos; IR final solo delimitadores. |
| Cero `(:=, x:main, [2])` | sí | ídem. |
| Otras sentencias válidas siguen emitiendo IR | sí | 297/299 archivos sin diff; `estructura_respetada.txt` con 14 tercetos. |
| Parser regenerado, no editado a mano | sí | copia directa de la salida de `yacc.linux -Jvalue=ParserVal`. |
| Alternativa con `=` intacta | sí | caso 137 sin diff; diff de `gramatica.y` solo en la alternativa `ASIG`. |
| `AccionesExpresiones.java` sin cambios | sí | no aparece en `git diff`. |

---

## 8. Efectos colaterales analizados

- **P2-05**: no se anticipó. La omisión del orden (`18_falta_orden.txt`) sigue emitiendo `(:=, x:main, foo:main)`; se resolverá con una acción registrada, sin regenerar el parser.
- **P2-07**: ya puede usar los expected results de P2-01/P2-02 y el IR real corregido de P2-03.
- **P1-01/P1-02/P1-03**: sin cambios. Los tests de identificadores inválidos siguen pasando; no aparecen en el diff de 299 casos.
- **TP03 (`21_enum_vacio.txt`)**: intacto, sin diff.
- **Stack semántico**: ver 6.1. Cambio de esqueleto por regeneración; sin efecto observable en la suite.
- **ParserVal.java**: sin cambios.
- **Tests y ASM**: ningún archivo de test ni snapshot modificado.

---

## 9. Estado de git al cierre de P2-03

```text
 M Parser.java
 M gramatica.y
```

No se agregaron ni borraron archivos de código. Los documentos de Ola 1 y de P2-01/P2-02 ya están commiteados (`736089b`, `5afeb88`).

Hash del `Parser.java` generado:

```text
047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257  Parser.java
```

---

## 10. Estado de la Ola 2

| Prompt | Estado | Observación |
|---|---|---|
| P2-01 | Ejecutado y commiteado (`736089b`) | Expected results tema 17. |
| P2-02 | Ejecutado y commiteado (`5afeb88`) | Expected results tema 19. |
| P2-03 | **Ejecutado** | Gramática + Parser regenerado; dos casos sin `:=`. |
| P2-04 | Eliminado | Según el plan. |
| P2-05 | Pendiente | Acción para `ERR_FALTA_ORDEN_EVALUACION`. |
| P2-06 | Pendiente | Propagar invalidez de `LlamadaFuncion` al envolverla. |
| P2-07 | Pendiente | Regresión TP02. |

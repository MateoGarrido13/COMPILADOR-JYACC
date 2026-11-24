# P2-05 — Error de orden devuelve expresión inválida

Fecha: 24/11/2026  
Prompt del plan: `P2-05`, “Gramática tema 19, sin regenerar Parser” (`docs/reentrega/plan_prompts_reentrega.md:272-295`).  
Estado inicial: commit `744ca55` (`2-03 Cambio en la gramatica tema 17, regeneracion de Parser`). P2-01 (`736089b`), P2-02 (`5afeb88`) y P2-03 (`744ca55`) ya están commiteados.  
Dependencia: P2-02 (expected results del tema 19 alineados).

---

## 1. Alcance del prompt

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

Restricciones respetadas:

- No se regeneró `Parser.java`. Hash antes y después: `047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257`.
- No se tocó `gramatica.y`. Hash antes y después: `f32a95c5bc0b54a939b4a8574d06c4e78fbca37faace342b740260d83645c6b0`.
- No se cambió el mensaje de error.
- No se modificó la alternativa con corchetes (`INVOCACION_FUNCION`, `INVOCACION_METODO`, `INVOCACION_METODO_PREFIJADO`).
- No se editaron docs de expected results: P2-02 ya los dejó apuntando a este prompt.

---

## 2. Raíz efectiva del defecto

La descripción del plan quedó parcialmente desactualizada por código agregado después. Conviene distinguir tres capas.

### 2.1 Generación sintáctica

Las seis reducciones que invocan `ERR_FALTA_ORDEN_EVALUACION` están en `gramatica.y:520,522,553,555,559,561`. Tras la regeneración de P2-03, en `Parser.java` quedaron en las líneas `1352, 1356, 1392, 1396, 1404, 1408` (las del plan `1343,1347,1383,1387,1395,1399` corresponden al archivo anterior).

Dos son de función:

```java
case 141:
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(5).obj, val_peek(3).obj)); }
case 142:
{ yyval = new ParserVal(acciones.ejecutar(Reglas.ERR_FALTA_ORDEN_EVALUACION, val_peek(3).obj, val_peek(1).obj)); }
```

Cuatro son de método (`objeto.método(...)` y `objeto.Clase.método(...)`, con y sin `[]`).

### 2.2 Registro de acciones

`AccionesSemanticas.java:10-14` cablea en este orden:

```java
AccionesErrores.registrar(tabla, v);
AccionesDeclaraciones.registrar(tabla, entorno, v);
AccionesExpresiones.registrar(tabla, entorno, v);
```

`AccionesErrores.java:7-24` registra una acción genérica para **todas** las reglas `ERR_*`. Esa acción sí haría lo que describe el plan: `resultado = valores[0]` (`TablaAcciones.java:41` es el fallback cuando no hay acción). Pero `AccionesExpresiones.java:285` registra después una acción específica para `ERR_FALTA_ORDEN_EVALUACION` y la sobrescribe (`TablaAcciones.registrar()` hace `acciones.put`, líneas 23-25).

Por lo tanto, la raíz real no es “falta de acción registrada”, sino el **contenido de la acción registrada**.

### 2.3 Acción específica antes del cambio

`AccionesExpresiones.java:285-313`:

```java
tabla.registrar(Reglas.ERR_FALTA_ORDEN_EVALUACION, ctx -> {
    v.reportar(Reglas.ERR_FALTA_ORDEN_EVALUACION, ctx.getLinea());
    if (ctx.cantidad() >= 3) {
        ... resolverMetodo(...) ...
        if (resultado instanceof ExpresionDiferida) {
            return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO).marcarInvalida();
        }
        return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO).marcarInvalida();
    }
    if (resultado instanceof ExpresionDiferida) {
        return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO);
    }
    if (resultado instanceof EntradaTabla) {
        return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO);
    }
    return resultado;
});
```

Para `foo(1$ul)` sin orden, `ctx.cantidad() == 2` y `ctx.valor(0)` es la `EntradaTabla` de `foo`. La rama de método no entra; la rama de función hacía:

```java
return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO);
```

Ese wrapper es **válido** (`valida = true`, `ExpresionDiferida.java:22,29-37`). Sube por `factor` → `termino` → `expresion`, `AccionesDeclaraciones.java:148` lo acepta y el generador emite `(:=, x:main, foo:main)`.

Las invocaciones a método (`cantidad() >= 3`) ya marcaban invalidez y por eso no emitían asignaciones. Se conservó esa rama intacta.

---

## 3. Baseline de ejecución

Build desde el árbol en `744ca55`, antes del cambio. Salidas filtradas a errores y tercetos:

```text
== 18_falta_orden.txt
Línea 8: Error: Falta del orden de evaluacion y asignacion de parametros
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (:=, x:main, foo:main)
6. (FIN_PROGRAMA, --, --)
```

```text
== sin_orden.txt
Línea 9: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 10: Error: Falta del orden de evaluacion y asignacion de parametros
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (:=, x:main, foo:main)
6. (:=, x:main, foo:main)
7. (FIN_PROGRAMA, --, --)
```

```text
== orden_sin_corchetes.txt
Línea 9: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 9: Error: Falta de operador en expresion
Línea 9: Error: Sentencia mal formada
Línea 10: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 10: Error: Falta de operador en expresion
Línea 10: Error: Sentencia mal formada
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (:=, x:main, foo:main)
6. (:=, x:main, foo:main)
7. (FIN_PROGRAMA, --, --)
```

Caso de método, para comprobar que no se rompía:

```text
== metodo_heredado_prefijado_sin_orden.txt
Línea 20: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 21: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 22: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 22: Error: falta de prefijo para 'm' en la clase 'ca'
1. (PROGRAMA, --, --)
...
9. (FIN_PROGRAMA, --, --)
```

Hash del fuente principal, sin cambios:

```text
38561952207f84a5c32e900f2b4ffc931a1e0b2ee78cc6733bc62d8f46caec22  test/tp2/generales/18_falta_orden.txt
```

---

## 4. Cambio aplicado

Único archivo de código modificado por P2-05: `otras_acciones/AccionesExpresiones.java`.

```diff
@@ -303,13 +303,10 @@ public class AccionesExpresiones {
                 }
                 return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO).marcarInvalida();
             }
-            if (resultado instanceof ExpresionDiferida) {
-                return ((ExpresionDiferida) resultado).conTipo(Tipos.INDEFINIDO);
-            }
-            if (resultado instanceof EntradaTabla) {
-                return ExpresionDiferida.deOperando(resultado, Tipos.INDEFINIDO);
-            }
-            return resultado;
+            // Invocacion de funcion sin orden: el ID recuperado por la produccion
+            // de error no es un valor utilizable. Devuelve una expresion invalida
+            // para que la asignacion no emita (:=, x:main, foo:main).
+            return new ExpresionDiferida().marcarInvalida();
         });
```

### Por qué cada parte

1. **`v.reportar(...)` se conserva como primera sentencia**  
   El mensaje `Falta del orden de evaluacion y asignacion de parametros` exigido por el tema 19 y por P2-02 sigue emitiéndose una sola vez por invocación. No se modificó `Reglas.java:93`.

2. **La rama `ctx.cantidad() >= 3` queda intacta**  
   Cubre las cuatro producciones de método. Mantiene `resolverMetodo()`, con lo que se preservan errores adicionales como `falta de prefijo para 'm' en la clase 'ca'`. Un reemplazo global por `new ExpresionDiferida().marcarInvalida()` habría eliminado ese diagnóstico y habría cambiado salidas fuera de los tres casos objetivo.

3. **La rama de función devuelve `new ExpresionDiferida().marcarInvalida()`**  
   Es exactamente el valor sugerido por el plan. El `ID foo` recuperado por la producción de error no se reenvuelve como valor. `marcarInvalida()` (`ExpresionDiferida.java:43-46`) pone `valida = false`.

4. **Cadena de corte**  
   `AccionesDeclaraciones.java:148` consulta `!origen.esValida()` y devuelve `null`; `GeneradorTercetos.java:270-276` no emite la asignación cuando el resultado es `null`. En el camino de operandos, `GeneradorTercetos.java:554-558` también descarta expresiones inválidas. No hizo falta tocar el generador.

5. **No se tocó `AccionesErrores.java:18`**  
   Esa condición especial también envolvía el ID como válido, pero está sobrescrita por la acción de `AccionesExpresiones` (`AccionesSemanticas.java:11,13`). Modificarla habría sido un segundo cambio sin efecto observable.

6. **No se regeneró el parser**  
   La solución vive en la tabla de acciones, así que las seis reducciones existentes se benefician sin tocar `gramatica.y` ni `Parser.java`.

Hashes:

```text
HEAD  otras_acciones/AccionesExpresiones.java  30cda76ed68b4df64518a65c4caa8563b777d3e970bb3e9e750f069afce7f706
work  otras_acciones/AccionesExpresiones.java  79c2c2571ca35a5f3e03fd560df553d0719ea1515c150151d94fdd69e3ae9c1e
HEAD  Parser.java                             047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257
work  Parser.java                             047b057607ab534751d909306a838514562dc12c7b46d7c67bae9e9b97b93257
HEAD  gramatica.y                             f32a95c5bc0b54a939b4a8574d06c4e78fbca37faace342b740260d83645c6b0
work  gramatica.y                             f32a95c5bc0b54a939b4a8574d06c4e78fbca37faace342b740260d83645c6b0
```

---

## 5. Verificación

### 5.1 Compilación

```bash
javac -encoding UTF-8 -d /tmp/opencode/tp3build_p2_05 \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Resultado: **sin errores**.

### 5.2 Regresión completa

Se comparó el árbol en `744ca55` contra el mismo árbol con el cambio aplicado, ejecutando todos los `.txt` de `test/` y `test_tp_4/` en directorios separados (para no escribir `.asm` en el repo):

```text
Casos ejecutados: 299
Archivos con diff: 3
```

Los únicos tres archivos con diff son exactamente los del prompt:

```text
test/tp2/generales/18_falta_orden.txt.out
test/tp2/verificacion_r19_param_reales/sin_orden.txt.out
test/tp2/verificacion_r19_param_reales/orden_sin_corchetes.txt.out
```

Diffs:

```diff
@@ -223,5 +223,4 @@  (18_falta_orden.txt)
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

```diff
@@ -269,6 +269,4 @@  (sin_orden.txt)
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (:=, x:main, foo:main)
-7. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

```diff
@@ -285,6 +285,4 @@  (orden_sin_corchetes.txt)
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (:=, x:main, foo:main)
-7. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

Los errores no aparecen en el diff: se mantienen todas las líneas `Error:` originales, incluidos los errores sintácticos colaterales de `orden_sin_corchetes.txt`. Se conservan `(PROGRAMA)`, la declaración de `foo`, el `ret(n)` interno, `(FIN_FUNCION)` y `(FIN_PROGRAMA)`: política P0-02, análisis continuo y programa entregado completo.

### 5.3 Aceptación directa con el código del repo

```text
== 18_falta_orden.txt
Línea 8: Error: Falta del orden de evaluacion y asignacion de parametros
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (FIN_PROGRAMA, --, --)
PAR=0 CALL=0 asig_foo=0
```

```text
== sin_orden.txt
Línea 9: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 10: Error: Falta del orden de evaluacion y asignacion de parametros
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (FIN_PROGRAMA, --, --)
PAR=0 CALL=0 asig_foo=0
```

```text
== orden_sin_corchetes.txt
Línea 9: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 9: Error: Falta de operador en expresion
Línea 9: Error: Sentencia mal formada
Línea 10: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 10: Error: Falta de operador en expresion
Línea 10: Error: Sentencia mal formada
1. (PROGRAMA, --, --)
2. (FUNCION, foo, --)
3. (ret, n:main:foo, --)
4. (FIN_FUNCION, foo, --)
5. (FIN_PROGRAMA, --, --)
PAR=0 CALL=0 asig_foo=0
```

### 5.4 Casos colaterales

- `metodo_heredado_prefijado_sin_orden.txt`: **sin diff**. Conserva los tres errores de orden y el error adicional `falta de prefijo para 'm' en la clase 'ca'`, y no emite asignaciones. La fila `PROMPT-0.md:135` todavía describe el baseline anterior (`3; asignaciones desde a1`); P2-05 no editó expected results y P2-07 la revisará.
- `estructura_respetada.txt` (tema 19): sin diff. Sigue emitiendo `PAR`, `CALL` y las asignaciones desde `[n]`.
- `17_asig_donde_igual.txt` y `usa_asig_sin_anidamiento.txt` (P2-03): sin diff.
- `21_enum_vacio.txt` (TP03): sin diff, intacto.

### 5.5 Assembler

```text
ASM en baseline: 161
ASM con diff: 0
```

Los casos afectados tienen errores y no escriben `.asm`; el cambio no llega al generador de assembler.

### 5.6 Criterios de aceptación

| Criterio | Cumplido | Evidencia |
|---|---|---|
| Error presente | sí | `Falta del orden de evaluacion y asignacion de parametros` en los tres casos, sin diff en las líneas `Error:`. |
| Cero `(:=, x:main, foo:main)` | sí | `asig_foo=0` en los tres casos; diffs muestran solo la remoción. |
| Cero `PAR` | sí | `PAR=0` en los tres casos. |
| Cero `CALL` | sí | `CALL=0` en los tres casos. |
| Mensaje sin cambios | sí | `Reglas.java:93` intacto; texto idéntico. |
| Parser no regenerado | sí | hash de `Parser.java` y `gramatica.y` idénticos a `HEAD`. |
| Alternativa válida intacta | sí | `estructura_respetada.txt` sin diff. |
| Solo 3 casos cambiaron | sí | 299 ejecutados, 3 diffs. |

---

## 6. Efectos colaterales analizados

- **P2-06**: sigue pendiente. La propagación de invalidez desde una `LlamadaFuncion` al envolverla en `ExpresionDiferida.deOperando()` no se tocó. Para estos tres casos no hace falta: la acción devuelve directamente una expresión inválida y ni siquiera se construye `LlamadaFuncion`.
- **P2-07**: ya tiene los tres expected results satisfechos. Debe además revisar la fila de_methods heredados sin orden, cuyo IR real cambió antes de este prompt y cuyo texto en `PROMPT-0.md:135` quedó desactualizado.
- **P2-03**: sin cambios. Sus dos casos y el `Parser.java` regenerado permanecen idénticos.
- **P2-02**: sin cambios en documentación. `PROMPT-0.md:121-123` y `PROMPT-19.md:107,117-135` describen ahora el estado real, no uno pendiente.
- **P1-01/P1-02/P1-03**: sin cambios; los tests de identificadores inválidos no aparecen en los diffs.
- **TP03**: `test/tp2/generales/21_enum_vacio.txt` intacto.
- **ASM y tests**: ningún `.asm`, `.txt` o `.sh` modificado.

---

## 7. Estado de git al cierre de P2-05

```text
 M otras_acciones/AccionesExpresiones.java
 M docs/reentrega/P2/P2_diagnostico_cambios.md
?? docs/reentrega/P2/P2-05_diagnostico_cambios.md
```

P2-05 queda sin commitear. El commit anterior es `744ca55` (P2-03).

---

## 8. Estado de la Ola 2

| Prompt | Estado | Observación |
|---|---|---|
| P2-01 | Ejecutado y commiteado (`736089b`) | Expected results tema 17. |
| P2-02 | Ejecutado y commiteado (`5afeb88`) | Expected results tema 19. |
| P2-03 | Ejecutado y commiteado (`744ca55`) | Gramática + Parser regenerado; dos casos sin `:=`. |
| P2-04 | Eliminado | Según el plan. |
| P2-05 | **Ejecutado** | Acción de error devuelve expresión inválida; tres casos sin `(:=, x:main, foo:main)`. |
| P2-06 | Pendiente | Propagar invalidez de `LlamadaFuncion` al envolverla. |
| P2-07 | Pendiente | Regresión TP02. |

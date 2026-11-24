# Ola 2 — diagnóstico y cambios

Prompt ejecutado en esta corrida: **P2-01** (expected results del tema 17).  
Prompts `P2-02` a `P2-07`: **pendientes**, con estado registrado al final.  
Fecha: 24/11/2026.

Este archivo concentra la Ola 2. Acá se documentan los cambios, la razón de cada uno, la verificación y lo que queda para los prompts siguientes. La Ola 1 quedó cerrada en `docs/reentrega/P1_diagnostico_cambios.md`.

---

## 1. Alcance de P2-01

Prompt del plan (`docs/reentrega/plan_prompts_reentrega.md:199-218`):

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

Límites respetados:

- No se tocó `gramatica.y`.
- No se tocó ningún `.java`.
- No se tocó ningún test.
- Solo se editaron los dos documentos permitidos.
- P2-03 sigue pendiente: este prompt solo alinea la documentación con el estado objetivo; el código todavía emite los dos tercetos.

### 1.1 Relación con P0-02

P0-02 fija que el error se informa, el análisis continúa y el programa se entrega completo; solo el constructor inválido no genera tercetos. P2-01 aplica esa política al tema 17:

- el mensaje `Uso de ':=' donde debe usarse '='` se conserva;
- el constructor `y := (1$ul)` se anula;
- la asignación externa `x := ...` no recibe un valor utilizable;
- los delimitadores `(PROGRAMA, --, --)` y `(FIN_PROGRAMA, --, --)` permanecen.

No se activa ninguna compuerta global `hayErrores()`. La decisión es por construcción, no por programa.

---

## 2. Estado inicial

### 2.1 Estado de git

La corrida arrancó con:

```text
a0c5780 1-02 actualización de mensaje de error
d7de3bf 1-01 nuevo estado y accion semantica
cedb870 P0, registro de decisiones
```

En el árbol de trabajo había cambios sin commitear de la Ola 1: `docs/reentrega/P1_diagnostico_cambios.md` y seis archivos nuevos en `test/tp1/verificacion_identificadores/`. P2-01 no los modificó.

Los últimos commits contienen los cambios de P1-01 y P1-02:

```text
a0c5780 1-02 actualización de mensaje de error
d7de3bf 1-01 nuevo estado y accion semantica
```

Por eso el baseline de código para P2-01 es `a0c5780`, no `HEAD~1` ni `HEAD~2`.

### 2.2 Fuentes de test

Los dos caminos afectados son el mismo programa:

| Archivo | SHA-256 |
|---|---|
| `test/tp2/generales/17_asig_donde_igual.txt` | `120c0280956a31375ced2b35f5f271ab1dc9b56338370001b7aeb3a26cca4db7` |
| `test/tp2/verificacion_r17_asig_expresiones/usa_asig_sin_anidamiento.txt` | `45fd63f3a1aa1cbb35292ec1c6728c67515ec6b33b80b5ac7c3e507741cd5397` |

Contenido de ambos (difieren solo en el comentario inicial):

```text
prog
ulongint x, y;
begin
    x := y := (1$ul);
end
```

El hash del primer archivo coincide con el registrado en `docs/reentrega/corrida_p0_diagnostico_cambios.md:100`.

### 2.3 Ejecución baseline

```bash
mkdir -p /tmp/opencode/p2_01_baseline_build /tmp/opencode/p2_01_probe
javac -encoding UTF-8 -d /tmp/opencode/p2_01_baseline_build \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
cp test/tp2/generales/17_asig_donde_igual.txt \
   test/tp2/verificacion_r17_asig_expresiones/usa_asig_sin_anidamiento.txt \
   /tmp/opencode/p2_01_probe/
for f in /tmp/opencode/p2_01_probe/*.txt; do
  java -cp /tmp/opencode/p2_01_baseline_build Main "$f"
done
```

Salidas:

```text
== 17_asig_donde_igual.txt
Línea 4: Error: Uso de ':=' donde debe usarse '='
Código intermedio:
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)

== usa_asig_sin_anidamiento.txt
Línea 5: Error: Uso de ':=' donde debe usarse '='
Código intermedio:
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)
```

Conclusión: el error se detecta, pero el IR contiene exactamente los dos tercetos que P2-03 debe eliminar. Coincide con `docs/reentrega/diagnostico_reentrega.md:463-485` y con `docs/reentrega/corrida_p0_diagnostico_cambios.md:67`.

### 2.4 Documentación antes del cambio

| Ubicación | Texto anterior | Problema |
|---|---|---|
| `docs/PROMPT-0.md:119` | `Uso de ':=' donde va '=' \| 1 \| Invalido` | Esperaba 1 terceto. El baseline real emite 2; el objetivo tras el fix es 0 ejecutables. |
| `docs/PROMPT-17.md:34` | “Todos los tests … se comportan según lo esperado” | Falso para este caso si el expected result se corrige. |
| `docs/PROMPT-17.md:45` | `✅ Pasa (Uso de ':=' donde debe usarse '=')` | Marcaba el caso como aprobado solo por el mensaje, ignorando el IR. |
| `docs/PROMPT-17.md:5` | “La asignación dentro de expresiones … funciona correctamente” | No distinguía el operador válido `=` del inválido `:=`. |

`docs/reentrega/diagnostico_reentrega.md:480-485` ya había señalado las dos primeras contradicciones. `docs/reentrega/corrida_p0_diagnostico_cambios.md:130` indicaba además: “Tras P2-03 debe ser 0 ejecutables. Requiere P2-01”.

---

## 3. Cambios efectuados

### 3.1 `docs/PROMPT-0.md`

#### Fila de expected results

Antes:

```markdown
| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde va `=` | 1 | Invalido |
```

Después:

```markdown
| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde debe usarse `=` | 0 ejecutables; sin `(:=, y:main, 1$ul)` ni `(:=, x:main, [2])` (objetivo tras P2-03; baseline: 2) | Invalido |
```

**Por qué cada parte:**

1. **`Uso de ':=' donde debe usarse '='`**  
   Unifica el mensaje con el que emite realmente el compilador y con el pedido del prompt. La versión anterior decía “donde va `=`”.

2. **`0 ejecutables`**  
   Reemplaza el `1` viejo. Cumple la política P0-02: el constructor inválido no genera tercetos. Se dice “ejecutables” para no confundir con los delimitadores `(PROGRAMA)` y `(FIN_PROGRAMA)`, que permanecen.

3. **`sin (:=, y:main, 1$ul) ni (:=, x:main, [2])`**  
   Nombra los dos tercetos concretos que aparecen en el baseline. Así la fila no admite la lectura de que un solo `:=` sería aceptable.

4. **`(objetivo tras P2-03; baseline: 2)`**  
   Evita afirmar que el código actual ya produce 0. La columna se llama “Tercetos actuales” en el encabezado general, pero la sección “Criterio de validez” (`PROMPT-0.md:41-42`) aclara que **Esperado** describe el comportamiento deseado, no el real. La acotación hace explícita esa convención en esta fila.

5. **`Invalido`**  
   Se conserva. El prompt pide marcar la construcción como inválida, no cambiar su clasificación general.

#### Nota agregada después de la tabla

Se insertó después de la última fila (`r33/lado_izquierdo.txt`), antes de `## Hallazgos que justifican las fases siguientes`:

```markdown
> Nota P2-01 (24/11/2026): la fila de `r17/usa_asig_sin_anidamiento.txt` describe el estado
> objetivo después de P2-03. El fuente es el mismo que
> `test/tp2/generales/17_asig_donde_igual.txt`; en el baseline se emitían
> `(:=, y:main, 1$ul)` y `(:=, x:main, [2])`. Ese IR es inválido: el tema 17 exige `=` y el
> constructor escrito con `:=` debe anularse por completo. El mensaje
> `Uso de ':=' donde debe usarse '='` ya se emitía en el baseline. Hasta aplicar P2-03,
> el código sigue emitiendo esos dos tercetos.
```

**Por qué:** deja constancia de que los dos nombres de test apuntan al mismo fuente, separa “error detectado” de “IR emitido” y no deja una fila futura contradiciendo el código vigente. Es la implementación documental de la conclusión `diagnostico_reentrega.md:609`.

### 3.2 `docs/PROMPT-17.md`

#### Estado general

Antes:

```markdown
**Ejecutado.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente.
```

Después:

```markdown
**Ejecutado para el operador válido `=`.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente. El uso indebido de `:=` en ese constructor se documenta como inválido más abajo; la corrección del IR queda registrada en P2-01 y su implementación en P2-03.
```

**Por qué:** acota el éxito declarado al operador que el enunciado autoriza. Sin esa acotación, el primer párrafo seguía contradiciendo la nueva fila de `PROMPT-0.md`.

#### Introducción de la tabla de verificación

Antes:

```markdown
Todos los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron y se comportan según lo esperado:
```

Después:

```markdown
Los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron. La tabla describe el estado esperado después de P2-03; `usa_asig_sin_anidamiento.txt` aún no llega a ese estado con el código vigente, como se detalla más abajo:
```

**Por qué:** la tabla pasa a ser expected results, no un informe de aprobación actual. El único caso que todavía no cumple se nombra explícitamente.

#### Fila del caso

Antes:

```markdown
| `usa_asig_sin_anidamiento.txt` | Sin anidamiento, pero `:=` donde debe usarse `=` | ✅ Pasa (`Uso de ':=' donde debe usarse '='`) |
```

Después:

```markdown
| `usa_asig_sin_anidamiento.txt` | Error `Uso de ':=' donde debe usarse '='` y **cero tercetos `:=`** del constructor | Invalido; error detectado, IR pendiente de P2-03 |
```

**Por qué cada parte:**

1. **`Error Uso de ':=' donde debe usarse '='`**  
   Mantiene el mensaje exigido por el enunciado y por el prompt.

2. **`cero tercetos := del constructor`**  
   Incorpora la condición que faltaba: antes el expected result se agotaba en el mensaje.

3. **`Invalido; error detectado, IR pendiente de P2-03`**  
   Separa las dos dimensiones. No dice “✅ Pasa” porque hoy el IR aún no coincide; tampoco dice que el error faltó, porque sí se informa.

#### Sección nueva

Se agregó entre la tabla y `## Criterios de aceptación cumplidos`:

```markdown
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
describen el estado objetivo; P2-03 ajustará el código sin tocar la alternativa válida
con `=`.
```

**Por qué:** fija en un lugar visible la regla completa (mensaje + IR nulo del constructor + IR del resto) y explica por qué se corrigió la documentación. La referencia a los delimitadores evita que “IR del resto del programa se mantiene” se lea como “deben quedar otras asignaciones”.

#### Lo que no se tocó

- La alternativa válida `ID '=' '(' expresion ')'` sigue documentada en `PROMPT-17.md:9-24`.
- Los criterios de aceptación de `ID = (expr)`, anidamiento y tipos quedaron intactos.
- La sección “Prompt 3.4 — Revisión” quedó intacta.
- No se agregó ninguna fila nueva a la tabla; solo se corrigió la existente.

---

## 4. Diff final

```diff
diff --git a/docs/PROMPT-0.md b/docs/PROMPT-0.md
@@ -116,7 +116,7 @@ en `/tmp/opencode/tp3-baseline/<misma ruta relativa>`.
 | `r17/igual_como_asignacion_sentencia.txt` | Sentencia mal formada | Ninguno | Invalido |
 | `r17/parentesis_anidados.txt` | Asignacion mal formada | Ninguno | Invalido |
 | `r17/sin_parentesis.txt` | Asignacion mal formada | Ninguno | Invalido |
-| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde va `=` | 1 | Invalido |
+| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde debe usarse `=` | 0 ejecutables; sin `(:=, y:main, 1$ul)` ni `(:=, x:main, [2])` (objetivo tras P2-03; baseline: 2) | Invalido |
 | `r19/estructura_respetada.txt` | Ninguno | 10; `PAR`/`CALL` sin copia a formales | Valido |
 | `r19/orden_sin_corchetes.txt` | Falta orden; errores sintacticos | 2; asignaciones desde `foo` | Invalido |
 | `r19/sin_orden.txt` | Falta orden en dos llamadas | 2; asignaciones desde `foo` | Invalido |
@@ -139,6 +139,14 @@ en `/tmp/opencode/tp3-baseline/<misma ruta relativa>`.
 | `r33/falta_paren_cierre.txt` | Asignacion mal formada | 1 | Invalido |
 | `r33/lado_izquierdo.txt` | Sentencia mal formada (dos veces) | 1 | Invalido |
 
+> Nota P2-01 (24/11/2026): la fila de `r17/usa_asig_sin_anidamiento.txt` describe el estado
+> objetivo después de P2-03. El fuente es el mismo que
+> `test/tp2/generales/17_asig_donde_igual.txt`; en el baseline se emitían
+> `(:=, y:main, 1$ul)` y `(:=, x:main, [2])`. Ese IR es inválido: el tema 17 exige `=` y el
+> constructor escrito con `:=` debe anularse por completo. El mensaje
+> `Uso de ':=' donde debe usarse '='` ya se emitía en el baseline. Hasta aplicar P2-03,
+> el código sigue emitiendo esos dos tercetos.
+
 ## Hallazgos que justifican las fases siguientes

diff --git a/docs/PROMPT-17.md b/docs/PROMPT-17.md
@@ -2,7 +2,7 @@
 
 ## Estado
 
-**Ejecutado.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente.
+**Ejecutado para el operador válido `=`.** La asignación dentro de expresiones (`ID = (expr)`) ya está implementada y funciona correctamente. El uso indebido de `:=` en ese constructor se documenta como inválido más abajo; la corrección del IR queda registrada en P2-01 y su implementación en P2-03.
 
 ## Qué implementa
 
@@ -31,7 +31,7 @@
 
 ## Verificación con tests existentes
 
-Todos los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron y se comportan según lo esperado:
+Los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron. La tabla describe el estado esperado después de P2-03; `usa_asig_sin_anidamiento.txt` aún no llega a ese estado con el código vigente, como se detalla más abajo:
 
 | Test | Resultado esperado | Estado |
 |------|-------------------|--------|
@@ -42,11 +42,24 @@ Todos los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron y
 | `asignacion_en_parametro_valida.txt` | `y = (f(z = (1$ul))[1$ul])`: asignación en parámetro no es anidamiento | ✅ Pasa (sin errores) |
 | `tipo_incompatible.txt` | Tipo incompatible en la asignación interna | ✅ Pasa (error reportado) |
 | `destino_no_asignable.txt` | Destino no asignable (función) | ✅ Pasa (error reportado, sin tercetos) |
-| `usa_asig_sin_anidamiento.txt` | Sin anidamiento, pero `:=` donde debe usarse `=` | ✅ Pasa (`Uso de ':=' donde debe usarse '='`) |
+| `usa_asig_sin_anidamiento.txt` | Error `Uso de ':=' donde debe usarse '='` y **cero tercetos `:=`** del constructor | Invalido; error detectado, IR pendiente de P2-03 |
 | `igual_como_asignacion_sentencia.txt` | Error sintáctico (usa `=` donde va `:=`) | ✅ Pasa |
 | `parentesis_anidados.txt` | Error sintáctico | ✅ Pasa |
 | `sin_parentesis.txt` | Error sintáctico | ✅ Pasa |
 
+### Estado inválido de `:=` en expresión
+
+`x := y := (1$ul)` es inválido según el tema 17 del enunciado. El mensaje correcto es
+`Línea N: Error: Uso de ':=' donde debe usarse '='`. La construcción **no debe** generar
+`(:=, y:main, 1$ul)` ni `(:=, x:main, [2])`: el constructor escrito con `:=` se anula por
+completo y solo se conserva el IR de las sentencias válidas vecinas. En este fuente no hay
+sentencias ejecutables vecinas: después de P2-03 quedan los delimitadores
+`(PROGRAMA, --, --)` y `(FIN_PROGRAMA, --, --)`.
+
+Antes de P2-01, este documento marcaba el caso como “✅ Pasa” por el mensaje aislado y
+`PROMPT-0.md` esperaba 1 terceto. Esa lectura era engañosa: el error se informaba, pero el
+IR posterior seguía construyendo dos asignaciones reales. Con P2-01 ambos documentos
+describen el estado objetivo; P2-03 ajustará el código sin tocar la alternativa válida
+con `=`.
+
 ## Criterios de aceptación cumplidos
```

Estadística:

```text
 docs/PROMPT-0.md  | 10 +++++++++-
 docs/PROMPT-17.md | 21 ++++++++++++++++++---
 2 files changed, 27 insertions(+), 4 deletions(-)
```

---

## 5. Verificación

### 5.1 Código y gramática sin cambios

```bash
git diff --name-only -- '*.java' '*.y'
```

Resultado: vacío. `javac` compila sin errores con el mismo árbol de fuentes.

Los dos fuentes de test tampoco se modificaron; sus hashes siguen siendo los de 2.2.

### 5.2 Búsqueda exigida por el plan

```bash
grep -rnE "usa_asig_sin_anidamiento|17_asig_donde_igual|x := y :=|Uso de ':=' donde debe usarse '='" \
     docs/PROMPT-0.md docs/PROMPT-17.md
```

Salida completa:

```text
docs/PROMPT-0.md:119:| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde debe usarse `=` | 0 ejecutables; sin `(:=, y:main, 1$ul)` ni `(:=, x:main, [2])` (objetivo tras P2-03; baseline: 2) | Invalido |
docs/PROMPT-0.md:142:> Nota P2-01 (24/11/2026): la fila de `r17/usa_asig_sin_anidamiento.txt` describe el estado
docs/PROMPT-0.md:144:> `test/tp2/generales/17_asig_donde_igual.txt`; en el baseline se emitían
docs/PROMPT-0.md:147:> `Uso de ':=' donde debe usarse '='` ya se emitía en el baseline. Hasta aplicar P2-03,
docs/PROMPT-17.md:34:Los tests de `test/tp2/verificacion_r17_asig_expresiones/` se ejecutaron. La tabla describe el estado esperado después de P2-03; `usa_asig_sin_anidamiento.txt` aún no llega a ese estado con el código vigente, como se detalla más abajo:
docs/PROMPT-17.md:45:| `usa_asig_sin_anidamiento.txt` | Error `Uso de ':=' donde debe usarse '='` y **cero tercetos `:=`** del constructor | Invalido; error detectado, IR pendiente de P2-03 |
docs/PROMPT-17.md:52:`x := y := (1$ul)` es inválido según el tema 17 del enunciado. El mensaje correcto es
docs/PROMPT-17.md:53:`Línea N: Error: Uso de ':=' donde debe usarse '='`. La construcción **no debe** generar
```

### 5.3 Ningún documento editado dice que el IR es válido

Primera tentativa:

```bash
grep -rnEi "usa_asig_sin_anidamiento.{0,160}(valido|✅)" docs/PROMPT-0.md docs/PROMPT-17.md
```

Devuelve las dos filas, pero es un **falso positivo**: `Invalido` contiene la subcadena `valido`. Se corrigió con límite de palabra:

```bash
grep -rnEi "usa_asig_sin_anidamiento.*\b(Valido|Válido)\b|usa_asig_sin_anidamiento.*✅" \
     docs/PROMPT-0.md docs/PROMPT-17.md
```

Resultado: vacío.

Comprobación complementaria:

```bash
grep -nE "usa_asig_sin_anidamiento.*Invalido" docs/PROMPT-0.md docs/PROMPT-17.md
```

```text
docs/PROMPT-0.md:119:| `r17/usa_asig_sin_anidamiento.txt` | Uso de `:=` donde debe usarse `=` | 0 ejecutables; sin `(:=, y:main, 1$ul)` ni `(:=, x:main, [2])` (objetivo tras P2-03; baseline: 2) | Invalido |
docs/PROMPT-17.md:45:| `usa_asig_sin_anidamiento.txt` | Error `Uso de ':=' donde debe usarse '='` y **cero tercetos `:=`** del constructor | Invalido; error detectado, IR pendiente de P2-03 |
```

### 5.4 Búsqueda global en `docs/`

```bash
grep -rnE "usa_asig_sin_anidamiento|17_asig_donde_igual|x := y :=|x:=y:=" docs | sort
```

Además de los archivos editados, aparecen:

| Archivo | Naturaleza de la mención |
|---|---|
| `docs/reentrega/diagnostico_reentrega.md` | Diagnóstico histórico: cita el texto viejo de `PROMPT-0.md:119` y `PROMPT-17.md:45`, y reproduce la ejecución baseline. No define expected results nuevos. |
| `docs/reentrega/corrida_p0_diagnostico_cambios.md` | Registro de P0: baseline con 2 tercetos y nota “Tras P2-03 debe ser 0 ejecutables”. |
| `docs/reentrega/plan_prompts_reentrega.md` | Plan de prompts; indica justamente que P2-01 debe corregir la documentación. |
| `docs/reentrega/P1_diagnostico_cambios.md` | Registro de la Ola 1: verificó que este caso no cambiaba con P1-01/P1-02. |
| `docs/REVISION-3-3-3-4.md:164` | Lista de tests revisados; no emite juicio de validez sobre el IR de este caso. |

Ninguno de esos archivos fue editado en P2-01. Los textos del diagnóstico que todavía dicen “1 terceto” son citas del estado anterior, no expected results vigentes; se conservan por trazabilidad.

### 5.5 Ejecución posterior al cambio

Se recompiló y se ejecutaron los dos casos otra vez:

```text
== 17_asig_donde_igual.txt
Línea 4: Error: Uso de ':=' donde debe usarse '='
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)

== usa_asig_sin_anidamiento.txt
Línea 5: Error: Uso de ':=' donde debe usarse '='
1. (PROGRAMA, --, --)
2. (:=, y:main, 1$ul)
3. (:=, x:main, [2])
4. (FIN_PROGRAMA, --, --)
```

Idéntico a 2.3. Es el resultado esperado en esta corrida: P2-01 prohíbe tocar Java, así que el IR todavía no puede cambiar. P2-03 verificará el estado objetivo documentado acá.

### 5.6 Hashes finales de los documentos

```text
690586745f2015149fb70e021e3406d42f82904a4db34032a1d91b5e1a561c6c  docs/PROMPT-0.md
614aec80180b5c04b104e7132eb4caebbeb8f5034e03b3afd8944e73f1dfa03e  docs/PROMPT-17.md
```

### 5.7 Criterios de aceptación del plan

| Criterio | Cumplido | Evidencia |
|---|---|---|
| `x := y := (1$ul)` marcado como inválido | sí | `PROMPT-0.md:119` (`Invalido`) y `PROMPT-17.md:45` (`Invalido`). |
| Mensaje `Uso de ':=' donde debe usarse '='` | sí | ambas filas y la sección nueva. |
| IR del constructor: ningún `:=` | sí, como expected result | `0 ejecutables`, sin los dos tercetos; pendiente de P2-03 en código. |
| IR del resto del programa se mantiene | sí | nota en `PROMPT-17.md`: quedan `(PROGRAMA)` y `(FIN_PROGRAMA)`. |
| Sin gramática ni Java modificados | sí | `git diff --name-only -- '*.java' '*.y'` vacío. |
| Ningún doc editado dice que el IR es válido | sí | grep con `\bValido\b` y `✅` vacío. |

---

## 6. Efectos colaterales analizados

- **P2-03**: ahora tiene un expected result documentado contra el cual verificar. El cambio posterior debe eliminar `(:=, y:main, 1$ul)` y `(:=, x:main, [2])` sin tocar la alternativa con `=`.
- **P2-02 y P2-05**: no se anticiparon sus cambios. La fila `r19/orden_sin_corchetes.txt` de `PROMPT-0.md:121` sigue con `2; asignaciones desde foo` hasta que se ejecute P2-02.
- **P1-01 / P1-02 / P1-03**: sin cambios en esta corrida. Sus archivos siguen en el estado registrado en la Ola 1.
- **`21_enum_vacio.txt`**: intacto, según la decisión de TP03.
- **Compilación**: sin cambios; `javac` OK antes y después.
- **Tests**: ningún `.txt` ni `.sh` modificado en P2-01.

---

## 7. Estado de la Ola 2

| Prompt | Estado | Observación |
|---|---|---|
| P2-01 | **Ejecutado** | Expected results del tema 17 alineados. Sin tocar código. Ver secciones 3-5. |
| P2-02 | Pendiente | Alinear `18_falta_orden.txt` y referenciar el caso general 18. |
| P2-03 | Pendiente | Regenerar `Parser.java`: valor inválido en `ID ASIG '(' expresion ')'`. |
| P2-04 | Eliminado | Según el plan, proponía un cambio que no compilaría y era redundante. |
| P2-05 | Pendiente | Registrar acción para `ERR_FALTA_ORDEN_EVALUACION`. |
| P2-06 | Pendiente | Propagar invalidez de `LlamadaFuncion` al envolverla. |
| P2-07 | Pendiente | Regresión TP02. |

---

## 8. Estado de git al cierre de P2-01

```text
 M docs/PROMPT-0.md
 M docs/PROMPT-17.md
 M docs/reentrega/P1_diagnostico_cambios.md
?? test/tp1/verificacion_identificadores/caracter_invalido_suelto.txt
?? test/tp1/verificacion_identificadores/id_invalido_declaracion.txt
?? test/tp1/verificacion_identificadores/id_invalido_derecha.txt
?? test/tp1/verificacion_identificadores/id_invalido_en_cadena.txt
?? test/tp1/verificacion_identificadores/id_invalido_izquierda.txt
?? test/tp1/verificacion_identificadores/verificar.sh
```

Los cambios nuevos de esta corrida son únicamente `docs/PROMPT-0.md` y `docs/PROMPT-17.md`. El resto corresponde a la Ola 1 y se mantiene sin commitear.

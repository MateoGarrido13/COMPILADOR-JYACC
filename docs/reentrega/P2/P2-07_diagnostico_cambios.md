# P2-07 — Regresión TP02

Fecha: 24/11/2026  
Prompt del plan: `P2-07`, “Pruebas” (`docs/reentrega/plan_prompts_reentrega.md:317-335`).  
Estado inicial: commit `8605966` (`2-06 LlamadaFuncion invalida`), con P2-03 (`744ca55`), P2-05 (`646d872`) y P2-06 (`8605966`) ya integrados.  
Dependencias: P2-03, P2-05, P2-06.  
Resultado: Ola 2 cerrada.

---

## 1. Alcance del prompt

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

Límites respetados:

- **No se modificó código Java ni gramática.** `git diff --name-only -- '*.java' '*.y'` está vacío.
- Se agregó un script de asserts: `test/tp2/verificacion_r19_param_reales/verificar.sh` (modo `775`).
- Se ajustaron los expected results ya documentados en `docs/PROMPT-0.md`, `docs/PROMPT-17.md` y `docs/PROMPT-19.md` para que digan “verificado” donde antes decían “objetivo tras P2-xx” o “pendiente”.
- No se borró IR de sentencias válidas: los asserts cubren `(PROGRAMA)`, `(FIN_PROGRAMA)`, la declaración de `foo`, el `ret` interno, los `PAR`/`CALL` válidos y los casos válidos de los temas 17 y 19.
- No se tocó TP03 ni `test/tp1/generales/21_enum_vacio.txt`.

---

## 2. Baseline elegido

Se comparó contra `5afeb88` (`2-02 actualización de documentación`). Ese commit es anterior a las tres correcciones de código de la Ola 2:

| Commit | Cambio |
|---|---|
| `744ca55` | P2-03: gramática + `Parser.java` regenerado. |
| `646d872` | P2-05: acción de `ERR_FALTA_ORDEN_EVALUACION`. |
| `8605966` | P2-06: propagación de invalidez en `ExpresionDiferida`. |

Por eso `5afeb88` sirve como “baseline con expected results corregidos pero código viejo”: permite ver exactamente qué cambió cada corrección y confirmar que no se perdió IR válido.

Preparación:

```bash
git archive 5afeb88 | tar -x -C /tmp/opencode/p2_07_refs/baseline_src
tar --exclude=.git -cf - . | tar -xf - -C /tmp/opencode/p2_07_refs/cur_src

javac -encoding UTF-8 -d /tmp/opencode/p2_07_refs/baseline_build \
      /tmp/opencode/p2_07_refs/baseline_src/*.java \
      /tmp/opencode/p2_07_refs/baseline_src/"acciones semanticas"/*.java \
      /tmp/opencode/p2_07_refs/baseline_src/otras_acciones/*.java

javac -encoding UTF-8 -d /tmp/opencode/p2_07_refs/cur_build \
      /tmp/opencode/p2_07_refs/cur_src/*.java \
      /tmp/opencode/p2_07_refs/cur_src/"acciones semanticas"/*.java \
      /tmp/opencode/p2_07_refs/cur_src/otras_acciones/*.java
```

Ambas compilaciones terminaron sin errores:

```text
BASELINE 5afeb88 COMPILA OK
CURRENT COMPILA OK
```

Hash del código fuente (`.java` + `.y`) usado como baseline: `eab6a7d60d15e9940c67b3debb69d2e8d1ea555aee90364712ea16465d3374ec`.  
Hash del código fuente en `HEAD` actual: `d05b53860a8ebdc61b7e4b9fa41f27b9bc2e88f8c2f20e5c03efd0309f6ec5c5`.  
Archivos de código que difieren entre `5afeb88` y `HEAD`: `ExpresionDiferida.java`, `Parser.java`, `gramatica.y`, `otras_acciones/AccionesExpresiones.java`. Ninguno fue tocado en esta corrida.

---

## 3. Regresión completa de salida

Se ejecutaron todos los `.txt` de `test/` y `test_tp_4/` con ambos builds, en árboles separados:

```text
Casos baseline: 299
Casos current:  299
Archivos con diff: 5
ASM baseline: 161
ASM current:  161
ASM con diff: 0
```

Los cinco diffs son exactamente los casos objetivo. No hay diffs en ningún otro caso ni en ningún `.asm`.

### 3.1 `test/tp2/generales/17_asig_donde_igual.txt.out`

```diff
 Línea 2: Declaracion de variables
-Línea 4: Asignacion en expresion
 Línea 4: Asignación
...
 1. (PROGRAMA, --, --)
-2. (:=, y:main, 1$ul)
-3. (:=, x:main, [2])
-4. (FIN_PROGRAMA, --, --)
+2. (FIN_PROGRAMA, --, --)
```

P2-03. Desaparece el terceto de estructura “Asignacion en expresion” y los dos `:=` del constructor inválido. Queda el mensaje de error y el programa completo.

### 3.2 `test/tp2/verificacion_r17_asig_expresiones/usa_asig_sin_anidamiento.txt.out`

```diff
 Línea 3: Declaracion de variables
-Línea 5: Asignacion en expresion
 Línea 5: Asignación
...
 1. (PROGRAMA, --, --)
-2. (:=, y:main, 1$ul)
-3. (:=, x:main, [2])
-4. (FIN_PROGRAMA, --, --)
+2. (FIN_PROGRAMA, --, --)
```

Mismo caso fuente que `17_asig_donde_igual.txt`, con otra numeración de línea.

### 3.3 `test/tp2/generales/18_falta_orden.txt.out`

```diff
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

P2-05. Se conserva la declaración de `foo`, su `ret` y los delimitadores.

### 3.4 `test/tp2/verificacion_r19_param_reales/sin_orden.txt.out`

```diff
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (:=, x:main, foo:main)
-7. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

Dos invocaciones sin orden; desaparecen las dos asignaciones desde `foo`.

### 3.5 `test/tp2/verificacion_r19_param_reales/orden_sin_corchetes.txt.out`

```diff
 2. (FUNCION, foo, --)
 3. (ret, n:main:foo, --)
 4. (FIN_FUNCION, foo, --)
-5. (:=, x:main, foo:main)
-6. (:=, x:main, foo:main)
-7. (FIN_PROGRAMA, --, --)
+5. (FIN_PROGRAMA, --, --)
```

El corchete sigue siendo obligatorio; el tema 20 no se adoptó.

### 3.6 Caso heredado sin orden: sin diff

`test/tp2/verificacion_r28_r31_atributos/metodo_heredado_prefijado_sin_orden.txt` ya estaba limpio en `5afeb88`: tres faltas de orden, una falta de prefijo, cero `CALLMET` y cero asignaciones desde `a1`. P2-07 lo incluye como control de no regresión. Su único cambio documental es el expected result de `PROMPT-0.md:135`, que antes decía `3; asignaciones desde a1` y ahora describe el IR real.

---

## 4. Script de asserts

Archivo nuevo: `test/tp2/verificacion_r19_param_reales/verificar.sh`, ejecutable (`775`). Sigue el patrón de `test/tp1/verificacion_identificadores/verificar.sh`:

- `TP3_BUILD` permite usar un build ya compilado; si no está, compila el repo.
- Cada caso se copia a un `mktemp -d` para no ensuciar el árbol con `.asm` generados.
- `verificar()` cuenta ocurrencias literales con `grep -F -c`.
- `verificarErrores()` cuenta solo líneas reales con `grep -E -c '^Línea [0-9]+: Error: ...'`.

### 4.1 Por qué `verificarErrores()`

El caso `metodo_heredado_prefijado_sin_orden.txt` repite el texto del error en un comentario:

```text
{{ se esperan 3 errores "Falta del orden de evaluacion y asignacion de parametros" }}
```

El compilador ecodea el fuente, así que un `grep -F` plano contaba 4 apariciones (3 errores reales + 1 comentario). La salida real es:

```text
Línea 20: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 21: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 22: Error: Falta del orden de evaluacion y asignacion de parametros
Línea 22: Error: falta de prefijo para 'm' en la clase 'ca'
```

`verificarErrores()` excluye el eco y fija el esperado en 3. No se modificó el caso de test.

### 4.2 Cobertura

| Grupo | Casos | Asserts | Qué comprueba |
|---|---:|---:|---|
| Falta de orden | 3 | 24 | Error presente; cero `PAR`; cero `CALL`; cero `(:=, x:main, foo:main)`; `(PROGRAMA)`, `(FIN_PROGRAMA)`, `(FUNCION, foo, --)` y `(ret, n:main:foo, --)` conservados. |
| `:=` en expresión | 2 | 10 | Error `Uso de ':=' donde debe usarse '='`; cero `(:=, y:main, 1$ul)`; cero `(:=, x:main, [2])`; delimitadores conservados. |
| Válido r19 | 1 | 5 | Sin errores; 2 `CALL`; 4 `PAR`; `:=` desde `[8]` y `[13]`. |
| Válido r17 | 1 | 5 | Sin errores; `:=` a `y`, a `x` desde `[2]`, a `x` desde `[7]`; `BF` a `label_1`. |
| Resolución de firma | 1 | 3 | Función inexistente, variable usada como función, 1 `CALL` válido. |
| Métodos | 1 | 3 | Método inexistente, tipo incompatible, 1 `CALLMET` válido. |
| Método heredado sin orden | 1 | 4 | 3 errores reales sin contar el eco; cero `CALLMET`; cero asignaciones desde `a1`; `(PROGRAMA)` conservado. |
| **Total** | **10** | **54** | |

Los casos válidos se eligieron porque son los que más fácilmente podrían romperse al eliminar IR inválido: `estructura_respetada.txt` (r19) mantiene 4 `PAR`, 2 `CALL` y las asignaciones `[8]` / `[13]`; `estructura_respetada.txt` (r17) mantiene sus 14 tercetos, incluidos los tres `:=` y el `BF`.

---

## 5. Resultados

### 5.1 Código actual (`8605966` + árbol de trabajo)

```bash
TP3_BUILD=/tmp/opencode/p2_07_refs/cur_build \
    test/tp2/verificacion_r19_param_reales/verificar.sh
```

```text
Casos: 10
Fallos: 0
exit=0
```

Con instrumentación temporal del contador de asserts:

```text
Asserts: 54
Casos: 10
Fallos: 0
```

### 5.2 Control con baseline `5afeb88`

```bash
TP3_BUILD=/tmp/opencode/p2_07_refs/baseline_build \
    test/tp2/verificacion_r19_param_reales/verificar.sh
```

```text
FALLA 18_falta_orden
  sin asignación desde foo: esperado 0, obtenido 1
FALLA sin_orden
  sin asignación desde foo: esperado 0, obtenido 2
FALLA orden_sin_corchetes
  sin asignación desde foo: esperado 0, obtenido 2
FALLA 17_asig_donde_igual
  sin asignación interna: esperado 0, obtenido 1
FALLA 17_asig_donde_igual
  sin asignación externa: esperado 0, obtenido 1
FALLA usa_asig_sin_anidamiento
  sin asignación interna: esperado 0, obtenido 1
FALLA usa_asig_sin_anidamiento
  sin asignación externa: esperado 0, obtenido 1
Casos: 10
Fallos: 7
```

El control falla exactamente donde tenían que fallar las correcciones: 3 asignaciones desde `foo` y 2 pares de asignaciones del tema 17. Antes de usar `verificarErrores()` el control daba 8 fallos; el octavo era el falso positivo del comentario echo.

---

## 6. Expected results actualizados

| Archivo | Antes | Después |
|---|---|---|
| `docs/PROMPT-0.md:119` | “objetivo tras P2-03; baseline: 2” | “verificado en P2-07; baseline: 2”. |
| `docs/PROMPT-0.md:121-123` | “objetivo tras P2-05; baseline: 1 / 2 / 2” | “verificado en P2-07; baseline: 1 / 2 / 2”. |
| `docs/PROMPT-0.md:135` | “Tres faltas de orden — 3; asignaciones desde `a1`” | “Tres faltas de orden y una falta de prefijo — 0 ejecutables; sin `CALLMET` ni asignaciones desde `a1` (verificado en P2-07)”. |
| `docs/PROMPT-0.md:143-157` | Notas en futuro (“hasta aplicar P2-03”, “describen el estado objetivo después de P2-05”) | Notas de cierre P2-07 con el IR real verificado. |
| `docs/PROMPT-17.md:34` | “`usa_asig_sin_anidamiento.txt` aún no llega a ese estado” | “ya no emite los dos `:=` del constructor”. |
| `docs/PROMPT-17.md:45` | “IR pendiente de P2-03” | “✅ Pasa (error detectado; solo delimitadores, verificado en P2-07)”. |
| `docs/PROMPT-17.md:59-63` | “P2-03 ajustará el código” | “P2-03 ajustó el código … y P2-07 verificó…”. |
| `docs/PROMPT-19.md:133-135` | “P2-05 registrará… P2-07 verificará…” | “P2-05 registró… P2-07 verificó…: los tres emiten cero `PAR`, cero `CALL` y cero asignaciones desde `foo`”. |

---

## 7. Criterios de aceptación

| Criterio del plan | Cumplido | Evidencia |
|---|---|---|
| Válidos: mismos tercetos que baseline | sí | 299 casos / 5 diffs, todos en casos inválidos; 161 ASM / 0 diffs; asserts de r17 y r19 válidos. |
| Inválidos: sin tercetos del constructor | sí | 5 diffs muestran la eliminación de los `:=` inválidos; 54 asserts en el código actual, 0 fallos. |
| Resto igual que baseline | sí | `(PROGRAMA)`, `(FIN_PROGRAMA)`, `(FUNCION, foo, --)` y `(ret, n:main:foo, --)` presentes en los cinco casos; cero diffs en los otros 294 casos. |
| Assert de “sin CALL” para falta de orden | sí | `verificar ... "(CALL," 0` en los tres casos de falta de orden. |
| Comparar con expected results corregidos | sí | `PROMPT-0.md`, `PROMPT-17.md` y `PROMPT-19.md` actualizados y contrastados con el script. |
| Sin borrar IR válido vecino | sí | Casos válidos con `PAR`/`CALL`/`CALLMET` y asignaciones intactos; control baseline falla solo en los IR inválidos. |
| Sin cambios de código en P2-07 | sí | `git diff --name-only -- '*.java' '*.y'` vacío. |

---

## 8. Efectos colaterales

- **Ola 2**: cerrada. Los siete prompts con cambio (P2-01, 02, 03, 05, 06, 07; P2-04 eliminado) están ejecutados y documentados.
- **Ola 3 (TP03)**: sin cambios; decisión de salida correcta respetada.
- **Ola 4**: pendiente. Debe introducir `start:` + `jmp main_inicio` y quitar `leave/ret` duplicado sin reordenar funciones.
- **Ola 5**: pendiente. Debe agregar `Entorno.getFuncion()` con `pilaFunciones`; el caso “factorial después de P5A-01” sigue sin existir en el árbol y queda como verificación futura de P2-06.
- **Ola 6**: pendiente; requiere MASM32/Windows. En Linux solo cabe análisis estático.
- **Hallazgo colateral P1 sin resolver**: `ulongint x, id#malo; begin x := 1$ul; end` pierde la declaración válida de `x` por la recuperación de `LISTA_VARIABLES`. Queda fuera de P2-07; no se tocó.
- **`$` suelto**: `AnalizadorLexico.java:42` lo toma como fin de entrada; fuera de alcance.

---

## 9. Estado de git al cierre de P2-07

```text
 M docs/PROMPT-0.md
 M docs/PROMPT-17.md
 M docs/PROMPT-19.md
 M docs/reentrega/P2/P2_diagnostico_cambios.md
?? docs/reentrega/P2/P2-07_diagnostico_cambios.md
?? test/tp2/verificacion_r19_param_reales/verificar.sh
```

P2-07 queda sin commitear. El commit anterior es `8605966` (P2-06).

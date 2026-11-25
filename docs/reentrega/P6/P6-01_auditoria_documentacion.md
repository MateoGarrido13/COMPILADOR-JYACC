# P6-01 — Auditoría final de documentación

Fecha: 24/11/2026  
Rama: `reentrega`  
Prompt del plan: `P6-01`, “Docs.” (`docs/reentrega/plan_prompts_reentrega.md:597-617`).  
Estado inicial: `HEAD` `9a1672a` (`P4-05 chequeo de desalineacion en la pila, ningun error`).  
Decisión aplicada: **recursión fuera de la reentrega**.  
Resultado: documentación alineada; sin cambios de código, tests ni snapshots.

---

## 1. Decisión vigente

El usuario decidió no incluir recursión en esta reentrega. Los enunciados de
TP01–TP04 no la mencionan; el profesor solo vio el test de factorial incorporado
y observó que no funcionaba. Esa observación no convierte la recursión en un
requisito, no habilita Ola 5 y no autoriza a presentarla como soportada.

Estado final que debe reflejar la documentación:

- `Entorno.getFuncion()` solo consulta el mapa de funciones cerradas.
- Una llamada a una función todavía abierta se descarta **silenciosamente**.
- No se emite `PAR`/`CALL` interno para esa llamada.
- No se emite ningún mensaje de error de recursión; el texto
  `Recursion no soportada` no existe en el código Java.
- El análisis continúa: el programa principal, las llamadas externas, los
  métodos y el `.asm` sí se generan.
- No se implementa “rechazo explícito” ni guarda nueva en `Verificaciones.java`.

Por lo tanto quedaron sin efecto la decisión P0-03 = A y los prompts
`P5A-01 .. P5A-04`. Tampoco se adopta la rama B de rechazo con mensaje.

---

## 2. Alcance del prompt

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
Dependencias: P1-03, P2-07, P4-06, P5A-04.
```

Ajuste por decisión posterior: la dependencia `P5A-04` se eliminó porque Ola 5
no se ejecutó. La raíz “diagnóstico de recursión inexistente” sigue siendo
válida; la corrección es descarte silencioso, no rechazo con mensaje.

---

## 3. Archivos modificados

### 3.1 Documentación versionada

| Archivo | Cambio |
|---|---|
| `docs/reentrega/plan_prompts_reentrega.md` | Nota de cierre con la decisión; fila “Recursión: fuera de alcance”; P0-03 marcado sin efecto; Ola 5 marcada no ejecutada; P6-01 sin dependencia P5A-04; P6-03 sin factorial 120/14400; orden y Definition of Done actualizados; P4-06 queda explicado solo por P4-01. |
| `docs/reentrega/corrida_p0_diagnostico_cambios.md` | Nota histórica; P0-03 reclasificado como decisión sin efecto; sección 4 y próximos pasos ajustados. |
| `docs/reentrega/diagnostico_reentrega.md` | Nota de cierre; tabla de clases, documentación desactualizada y conclusiones pasadas a estado final; factorial deja de ser fallo pendiente. |
| `docs/reentrega/P2/P2_diagnostico_cambios.md` | Nota de cierre: el estado “pendiente” de P2-02 es histórico; P2-05/P2-07 son el estado vigente. |
| `docs/reentrega/P2/P2-06_diagnostico_cambios.md` | Ola 5/P5A-01 queda fuera de alcance; el test factorial solo se conserva como regresión de layout. |
| `docs/PROMPT-19.md` | Se corrigió “el IR todavía contiene `(:=, x:main, foo:main)`”: ahora se describe P2-05/P2-07 con cero `PAR`, cero `CALL` y cero asignaciones desde `foo`. |

### 3.2 Documentación en `ASSETS/`

`ASSETS/` está ignorado por `.gitignore:4`, por lo que estos cambios no aparecen
en `git status`, pero sí se modificaron en disco:

| Archivo | Cambio |
|---|---|
| `ASSETS/recursion.md` | Reescrito como estado final. Describe el descarte silencioso, el IR real del caso factorial y el contexto histórico sin afirmar diagnóstico ni soporte. |
| `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md` | Nota histórica; se quitó la afirmación de que `Verificaciones.java` emite `Recursion no soportada`; la propuesta de rechazo explícito quedó marcada como no adoptada. |
| `ASSETS/informe_generador_assembler.md` | “La recursión se rechaza” → “quedó fuera de alcance”; temporales y stack overflow ajustados al estado final. |
| `ASSETS/informe_flujo_generar_assembler.md` | `emitirCodigo()` ya no se describe como etapa que recibe llamadas recursivas rechazadas semánticamente. |
| `ASSETS/informe_generador_tercetos.md` | El riesgo de `@auxN` queda como teórico; la llamada abierta se descarta en resolución de firma, sin mensaje. |

### 3.3 Sin cambios

- `docs/PROMPT-0.md`: ya tenía corregida la fila `r17/usa_asig_sin_anidamiento.txt`
  con `0 ejecutables` y las filas de falta de orden con cero `PAR`/`CALL`.
- `test/tp2/generales/21_enum_vacio.txt`: intacto; la fila
  `r23/enum_vacio.txt` de `docs/PROMPT-0.md:127` también quedó intacta.
- `docs/PROMPT-17.md`: ya describía P2-01/P2-03 correctamente.
- Código Java, gramática, tests y snapshots `.asm`: sin cambios.
- `docs/TP4/*`: no se tocó; contiene notas históricas de la fase TP4 anterior y
  no figura en la lista de archivos de P6-01.
- No se recrearon los documentos P4-04 ni P4-06 que el usuario eliminó.

---

## 4. Verificación de comportamiento

Build fresco desde el estado final:

```bash
rm -rf /tmp/opencode/p6_01_final_build
mkdir -p /tmp/opencode/p6_01_final_build
javac -encoding UTF-8 -d /tmp/opencode/p6_01_final_build *.java \
      "acciones semanticas"/*.java otras_acciones/*.java
java -cp /tmp/opencode/p6_01_final_build Main \
     test_tp_4/Integracion/03_recursion_factorial_metodo.txt \
     > /tmp/opencode/p6_01_factorial_final.out 2>&1
```

Resultado relevante:

```text
390:2. (FUNCION, fact, --)
404:16. (PAR, n:main:fact, 5$ul)
405:17. (CALL, fact:main, 1)
408:20. (CALLMET, c:main, escala:main:calculadora)
mensajes "^Error": 0
```

En el ASM generado:

```text
59:    call fn_fact_main
70:    call mt_escala_main_calculadora
```

No hay `PAR`/`CALL` dentro de `fact`, pero sí hay llamada externa desde
`main_inicio` y `CALLMET`. No hay mensaje de error. Esto confirma que la
documentación debe hablar de descarte silencioso y no de rechazo diagnosticado
ni de ausencia total de `.asm`.

Diagnóstico inexistente en código:

```bash
grep -rniE 'recursion no soportada|no puede invocarse a si misma' --include='*.java' . | wc -l
```

```text
0
```

---

## 5. Verificación documental obligatoria

```bash
grep -rniE 'recursi|v.lido|terceto' docs ASSETS > /tmp/opencode/p6_01_grep_final.txt
wc -l < /tmp/opencode/p6_01_grep_final.txt
```

```text
1182
```

Clasificación de las coincidencias:

1. **Estado vigente correcto:** `ASSETS/recursion.md`, `docs/PROMPT-19.md`,
   `docs/reentrega/plan_prompts_reentrega.md` y los documentos P0/P2 revisados
   usan “fuera de alcance”, “descartada silenciosamente”, “sin `PAR`/`CALL`
   interno”, “sin mensaje”, “P5A no ejecutado”.
2. **Registro histórico señalizado:** `docs/reentrega/diagnostico_reentrega.md`,
   `docs/reentrega/corrida_p0_diagnostico_cambios.md`,
   `docs/reentrega/P2/P2_diagnostico_cambios.md` y
   `ASSETS/DIAGNOSTICO_FUNCIONES_METODOS.md` conservan citas viejas, pero
   incluyen una nota de cierre que identifica lo que quedó sin efecto.
3. **Nombres de archivo:** muchas coincidencias de `recursi` son solo
   `03_recursion_factorial_metodo.*`, usado ahora como regresión de layout.
4. **Fuera de alcance:** `docs/TP4/*` contiene notas de la fase anterior y no
   se modificó en este prompt.

No queda ninguna afirmación vigente que diga que la recursión está soportada,
implementada, rechazada con mensaje, que P5A está pendiente o que el factorial
debe dar 120/14400 como evidencia de recursión.

### IR de construcciones inválidas

Se revisaron las frases con `v.lido` y `terceto` sobre los casos corregidos:

```text
docs/PROMPT-0.md:119  0 ejecutables; sin (:=, y:main, 1$ul) ni (:=, x:main, [2])
docs/PROMPT-0.md:121  0 ejecutables; sin PAR, sin CALL, sin (:=, x:main, foo:main)
docs/PROMPT-0.md:127  r23/enum_vacio.txt → Ninguno | Invalido
```

`docs/PROMPT-19.md` ya no describe la omisión del orden como caso pendiente.
Las referencias a “IR posterior” en `P2_diagnostico_cambios.md` están dentro de
diffs históricos y tienen una nota que remite a P2-05/P2-07.

---

## 6. Aceptación

- Sin diagnósticos inexistentes en el código: verificado, `0` coincidencias Java.
- Sin afirmaciones vigentes que contradigan el código final: verificado por la
  clasificación del grep y por la reescritura de los documentos de estado.
- `enum_vacio` intacto: verificado con `git diff --name-only -- test/tp2/generales/21_enum_vacio.txt docs/PROMPT-0.md | wc -l` → `0`.
- Sin cambios de código: `git diff --name-only -- '*.java' '*.y' 'test/**' 'test_tp_4/**' | wc -l` → `0`.
- `git diff --check`: sin salida.

---

## 7. Estado de git

Tracked:

```text
## Reentrega
 M docs/PROMPT-19.md
 M docs/reentrega/P2/P2-06_diagnostico_cambios.md
 M docs/reentrega/P2/P2_diagnostico_cambios.md
 M docs/reentrega/corrida_p0_diagnostico_cambios.md
 M docs/reentrega/diagnostico_reentrega.md
 M docs/reentrega/plan_prompts_reentrega.md
?? docs/reentrega/P6/P6-01_auditoria_documentacion.md
```

Ignorados pero modificados: los cinco archivos de `ASSETS/` listados en §3.2.

P6-01 no requiere commit de código. Queda para P6-02 la suite completa y para
P6-03 la verificación real en MASM32/Windows, sin usar factorial como prueba de
recursión.

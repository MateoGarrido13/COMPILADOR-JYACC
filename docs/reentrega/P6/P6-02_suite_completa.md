# P6-02 — Suite completa en Linux

Fecha: 24/11/2026  
Rama: `Reentrega`  
Prompt del plan: `P6-02`, “Validación global” (`docs/reentrega/plan_prompts_reentrega.md:620-636`).  
Dependencia: P6-01.  
Estado: **CERRADO en Linux** — cero fallos; cada diff tiene ID de prompt. P6-03 sigue pendiente: requiere MASM32/Windows.

---

## 1. Alcance del prompt

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
```

La recursión sigue fuera de alcance. El caso
`03_recursion_factorial_metodo.*` se trata solo como regresión de layout; la
llamada `call fn_fact_main` que verifican los scripts es la llamada externa
emitida desde `main_inicio`, no una llamada recursiva interna.

---

## 2. Correcciones de scripts aplicadas

Los scripts apuntaban a carpetas y nombres de archivo anteriores al rename
registrado en `d37eb90`. P6-02 ajustó solo esas referencias; no se cambió código
Java, gramática, fuentes `.txt` de programa ni lógica de generación.

| Script | Corrección |
|---|---|
| `test_tp_4/probar_prompt4b.sh` | `Salida por consola/` → `Salida_consola/`. |
| `test_tp_4/probar_prompt4c.sh` | `Funciones y metodos/` → `Funciones_y_metodos/`. |
| `test_tp_4/probar_prompt4d.sh` | `Objetos y atributos/` → `Objetos_atributos/`. |
| `test_tp_4/probar_prompt4e.sh` | Etiquetas “recursiva” → “llamada a fact desde main”; patrones actualizados a `@aux17`/`r_main` y a la multiplicación del método `escala`. El nombre del caso se conserva. |
| `test_tp_4/probar_prompt5_b.sh` | `resta_enteros_negativos/` → `Runtime_chequeos/b_suma_ulongint/`; filenames `01_suma_sin_overflow.asm` y `04_suma_variables_desborda.asm`. |
| `test_tp_4/probar_prompt5_e.sh` | `producto_doublef/` y `producto_doublef_overflow/` → `Runtime_chequeos/e_producto_doublef/`; los cuatro casos quedan en una sola carpeta; `fmul b_main` → `fmul y_main`. |
| `test_tp_4/probar_prompt5_f.sh` | `resta_enteros_negativa/` → `Runtime_chequeos/f_resta_ulongint/`; filenames `01_resta_sin_resultado_negativo.asm` y `03_resta_resultado_negativo.asm`. |
| `test_tp_4/probar_prompt5_h.sh` | `enumerados/` → `Runtime_chequeos/h_enum_asignacion/`; filenames `01_enum_*`, `02_enum_*`, `03_enum_*`, `04_enum_*`. |
| `test_tp_4/probar_prompt6_1.sh` | `Salida por consola/` → `Salida_consola/`. |
| `test_tp_4/probar_prompt6_2.sh` | Rutas de los ocho chequeos runtime actualizadas; `correr_suite` propaga `TP4_BUILD`; etiquetas de recursión ajustadas. |
| `test/TP3_SEMANTICAS/ejecutar.sh` | Runner adaptado al bloque descriptivo `{{ ... }}` que tienen los 47 casos: compara texto de tercetos sin depender de la numeración, valida entradas de TS (300/302/303) y mensajes de `Errores:`. |
| `test/TP3_SEMANTICAS/README.md` | Nota del runner actualizada: ya no describe el formato `--- ESPERADO ---` como vigente. |

### 2.1 Expectativas TP3 alineadas al comportamiento vigente

Cinco `.txt` de `test/TP3_SEMANTICAS` tenían expectativas que no coincidían con
el compilador actual: cuatro esperaban tercetos que ya no se emiten porque el
acceso entre ámbitos hermanos se descarta sin IR (comportamiento corregido en
Ola 1), y uno esperaba un tipo declarado donde hoy queda un placeholder. P6-02
actualizó solo esos bloques descriptivos; el código Java no se tocó.

| Archivo | Ajuste | ID |
|---|---|---|
| `prompt-1a-ambitos/clase_hermana_en_funcion_no_ve_atributo.txt` | Se quitó la asignación a un atributo de clase hermana. | P6-02 |
| `prompt-1a-ambitos/clase_hermana_no_ve_atributo.txt` | Se quitó la asignación a un atributo de clase hermana. | P6-02 |
| `prompt-1a-ambitos/funcion_hermana_no_ve_local.txt` | Se quitó la asignación a una local de función hermana. | P6-02 |
| `prompt-1b-name-mangling/funciones_iguales_local_solo_en_hija.txt` | Se quitó la asignación a una local de la función del padre. | P6-02 |
| `prompt-2-tipos-estrictos/tipo_no_declarado.txt` | `f:main` pasa a esperarse como placeholder `indefinido, indefinido` (`Declarado: no`). | P6-02 |

Verificación sintáctica: `bash -n` sobre todos los scripts → sin errores.

---

## 3. Comandos ejecutados

### 3.1 Baseline

```bash
mkdir -p /tmp/opencode/p6_02
find test test_tp_4 -name '*.asm' -print0 | sort -z | xargs -0 sha256sum > /tmp/opencode/p6_02/asm_before.sha
find test test_tp_4 -name '*.txt' -print0 | sort -z | xargs -0 sha256sum > /tmp/opencode/p6_02/txt_before.sha
```

Conteo inicial: 50 archivos `.asm` y 299 archivos `.txt`.

### 3.2 Build compartido

```bash
export TP4_BUILD=/tmp/opencode/p6_02/build
export TP3_BUILD=/tmp/opencode/p6_02/build
```

### 3.3 Scripts individuales

```bash
for s in \
  test/tp1/verificacion_identificadores/verificar.sh \
  test/tp2/verificacion_r19_param_reales/verificar.sh \
  test/TP3_SEMANTICAS/ejecutar.sh \
  test_tp_4/probar_prompt2.sh \
  test_tp_4/probar_prompt3.sh \
  test_tp_4/probar_prompt4a.sh \
  test_tp_4/probar_prompt4b.sh \
  test_tp_4/probar_prompt4c.sh \
  test_tp_4/probar_prompt4d.sh \
  test_tp_4/probar_prompt4e.sh \
  test_tp_4/probar_prompt5_0.sh \
  test_tp_4/probar_prompt5_b.sh \
  test_tp_4/probar_prompt5_e.sh \
  test_tp_4/probar_prompt5_f.sh \
  test_tp_4/probar_prompt5_h.sh \
  test_tp_4/probar_prompt6_0.sh \
  test_tp_4/probar_prompt6_1.sh; do
  name="$(printf '%s' "$s" | tr '/' '_')"
  TP4_BUILD=/tmp/opencode/p6_02/build TP3_BUILD=/tmp/opencode/p6_02/build \
    bash "$s" > "/tmp/opencode/p6_02/individual/$name.log" 2>&1
done
```

### 3.4 Script consolidado

```bash
TP4_BUILD=/tmp/opencode/p6_02/build TP3_BUILD=/tmp/opencode/p6_02/build \
  bash test_tp_4/probar_prompt6_2.sh > /tmp/opencode/p6_02/consolidado_final.log 2>&1
```

### 3.5 Hashes finales

```bash
find test test_tp_4 -name '*.asm' -print0 | sort -z | xargs -0 sha256sum > /tmp/opencode/p6_02/asm_after.sha
find test test_tp_4 -name '*.txt' -print0 | sort -z | xargs -0 sha256sum > /tmp/opencode/p6_02/txt_after.sha
diff /tmp/opencode/p6_02/asm_before.sha /tmp/opencode/p6_02/asm_after.sha
diff /tmp/opencode/p6_02/txt_before.sha /tmp/opencode/p6_02/txt_after.sha
```

Conteo final: 50 archivos `.asm` y 299 archivos `.txt`.

---

## 4. Resultados

### 4.1 Suite TP3 semántica adaptada

```bash
TP3_BUILD=/tmp/opencode/p6_02/build bash test/TP3_SEMANTICAS/ejecutar.sh
```

```text
47/47 casos OK
exit=0
```

Antes del ajuste del runner, los 47 casos fallaban por `falta la linea
'--- ESPERADO ---'`: el runner esperaba un formato que ningún caso usa. Tras la
adaptación quedaron 6 fallos por numeración/tercetos obsoletos y 1 por el
placeholder de tipo no declarado; alineadas esas expectativas, la suite quedó
en 47/47.

### 4.2 Suites individuales

Todas corrieron con `TP4_BUILD=TP3_BUILD=/tmp/opencode/p6_02/build`; cada
log quedó en `/tmp/opencode/p6_02/individual/`.

| Script | Resultado |
|---|---|
| `test/tp1/verificacion_identificadores/verificar.sh` | `Fallos: 0`, exit 0 |
| `test/tp2/verificacion_r19_param_reales/verificar.sh` | `Fallos: 0`, exit 0 |
| `test/TP3_SEMANTICAS/ejecutar.sh` | `47/47 casos OK`, exit 0 |
| `test_tp_4/probar_prompt2.sh` | exit 0 |
| `test_tp_4/probar_prompt3.sh` | exit 0 |
| `test_tp_4/probar_prompt4a.sh` | 4 casos compilados, exit 0 |
| `test_tp_4/probar_prompt4b.sh` | 11 casos compilados, exit 0 |
| `test_tp_4/probar_prompt4c.sh` | 8 casos compilados, exit 0 |
| `test_tp_4/probar_prompt4d.sh` | 4 casos compilados, exit 0 |
| `test_tp_4/probar_prompt4e.sh` | 7 casos compilados, exit 0 |
| `test_tp_4/probar_prompt5_0.sh` | 1 caso compilado, exit 0 |
| `test_tp_4/probar_prompt5_b.sh` | 4 casos compilados, exit 0 |
| `test_tp_4/probar_prompt5_e.sh` | 4 casos compilados, exit 0 |
| `test_tp_4/probar_prompt5_f.sh` | 5 casos compilados, exit 0 |
| `test_tp_4/probar_prompt5_h.sh` | 4 casos compilados, exit 0 |
| `test_tp_4/probar_prompt6_0.sh` | exit 0 |
| `test_tp_4/probar_prompt6_1.sh` | exit 0; solo estática, sin `ml.exe` |

`grep -l 'FALLA' /tmp/opencode/p6_02/individual/*.log` → sin resultados.

Fallas iniciales: `5_0`, `5_b`, `5_e`, `5_f`, `5_h` y `6_0` heredaban el fallo
de `4e` por los patrones obsoletos de `@aux10`/`imul`; `4e` quedó en 0 tras la
corrección P6-02. `6_1` pasó desde el principio.

### 4.3 Script consolidado

```text
exit=0
FALLA: 0 ocurrencias
OK: todas las verificaciones del Prompt 6.2 pasaron (137 fuentes TP3).
=== Prueba del Prompt 6.2 de TP4 completada (runtime real: Windows) ===
```

Log completo: `/tmp/opencode/p6_02/consolidado_final.log`. El último mensaje
describe el destino de P6-03, no una ejecución hecha acá: en Linux solo se
verificó generación estática y regresiones; no hay `ml.exe`, `link.exe` ni
Windows.

---

## 5. Diffs y atribución

### 5.1 Archivos `.asm` versionados

| Archivo | Diff | ID |
|---|---|---|
| `test/tp2/generales/00_programa_valido.asm` | Se regeneró con el generador vigente. El diff útil son dos líneas: `jmp main_inicio` tras `start:` y `main_inicio:` antes del cuerpo principal. El conteo `172/170` del `git diff --numstat` se debe a que el generador escribe CRLF. | P4-01 |

No hay otros `.asm` versionados con diff. El baseline y el hash final tienen 50
archivos `.asm`.

Durante las corridas, `probar_prompt5_0.sh` regeneró
`test_tp_4/Runtime_chequeos/0_sin_chequeos_activos/01_sin_chequeos_activos.asm`.
Ese archivo no existe en `HEAD` y está cubierto por `.gitignore:7` (`*.asm`),
por lo que nunca fue candidato a entrega. Se eliminó después de capturar los
logs para dejar el árbol en el mismo estado que el baseline: 50 `.asm`.

### 5.2 Archivos `.txt`

| Archivo | Diff | ID |
|---|---|---|
| `test/TP3_SEMANTICAS/prompt-1a-ambitos/clase_hermana_en_funcion_no_ve_atributo.txt` | Expectativa descriptiva ajustada. | P6-02 |
| `test/TP3_SEMANTICAS/prompt-1a-ambitos/clase_hermana_no_ve_atributo.txt` | Expectativa descriptiva ajustada. | P6-02 |
| `test/TP3_SEMANTICAS/prompt-1a-ambitos/funcion_hermana_no_ve_local.txt` | Expectativa descriptiva ajustada. | P6-02 |
| `test/TP3_SEMANTICAS/prompt-1b-name-mangling/funciones_iguales_local_solo_en_hija.txt` | Expectativa descriptiva ajustada. | P6-02 |
| `test/TP3_SEMANTICAS/prompt-2-tipos-estrictos/tipo_no_declarado.txt` | `f:main` esperado como `indefinido, indefinido`. | P6-02 |

No se modificó ningún `.txt` de programa ni `21_enum_vacio.txt`. El baseline y
el hash final tienen 299 archivos `.txt`.

### 5.3 Scripts y runner

| Archivos | Diff | ID |
|---|---|---|
| `probar_prompt4b.sh`, `4c`, `4d`, `4e`, `5_b`, `5_e`, `5_f`, `5_h`, `6_1`, `6_2` | Rutas, filenames y patrones del estado vigente; propagación de `TP4_BUILD`. | P6-02 |
| `test/TP3_SEMANTICAS/ejecutar.sh`, `test/TP3_SEMANTICAS/README.md` | Runner y nota adaptados al bloque `{{ ... }}`. | P6-02 |

### 5.4 Documentación

| Archivos | Diff | ID |
|---|---|---|
| `docs/PROMPT-19.md`, `docs/reentrega/plan_prompts_reentrega.md`, `docs/reentrega/corrida_p0_diagnostico_cambios.md`, `docs/reentrega/diagnostico_reentrega.md`, `docs/reentrega/P2/P2_diagnostico_cambios.md`, `docs/reentrega/P2/P2-06_diagnostico_cambios.md` | Cierre de P6-01: recursión fuera de alcance, P0-03 sin efecto, Ola 5 no ejecutada, P4-04 revertido. | P6-01 |
| `docs/reentrega/P6/P6-01_auditoria_documentacion.md` | Documento nuevo de P6-01. | P6-01 |
| `docs/reentrega/P6/P6-02_suite_completa.md` | Documento nuevo de P6-02. | P6-02 |

`ASSETS/` está ignorado por `.gitignore:4`. P6-01 modificó cinco `.md` internos
(`recursion.md`, `DIAGNOSTICO_FUNCIONES_METODOS.md`,
`informe_generador_assembler.md`, `informe_flujo_generar_assembler.md`,
`informe_generador_tercetos.md`); no aparecen en `git status`, pero el cambio
está registrado en `P6-01_auditoria_documentacion.md`.

No hay diffs en `.java`, gramática ni código de generación.

---

## 6. Aceptación

- `bash -n` sobre todos los scripts: sin errores.
- 17 suites individuales: todas con exit 0; cero ocurrencias de `FALLA` en sus logs.
- Suite TP3 semántica: `47/47 casos OK`.
- Script consolidado `probar_prompt6_2.sh`: exit 0, cero `FALLA:`, `137 fuentes TP3`.
- Hashes: único `.asm` versionado modificado, atribuido a P4-01; cinco `.txt` modificados, atribuidos a P6-02.
- `git diff --name-only -- '*.java'` → vacío. `21_enum_vacio.txt` → sin diff.
- `git diff --check` sobre scripts y docs → limpio.
- `git diff --check` sobre `00_programa_valido.asm` reporta 344 líneas de `trailing whitespace`: son los retornos de carro del generador, no cambios de contenido semanticos. La comparación con `--ignore-space-at-eol` deja solo las dos líneas de P4-01.
- Cada diff del árbol tiene ID de prompt: P6-01 para el cierre documental, P4-01 para el snapshot ASM, P6-02 para scripts, runner, expectativas y este documento.

Queda fuera de esta aceptación la ejecución real MASM32/Windows: es P6-03. La
recursión no se presenta como soportada y `factorial 120` no se usa como
evidencia.

---

## 7. Estado de git

```text
Rama: Reentrega
HEAD: 9a1672a (P4-05 chequeo de desalineacion en la pila, ningun error)
Commits nuevos: ninguno (P6-01 y P6-02 quedan sin commitear)
```

Modificados versionados:

- Documentación P6-01: `docs/PROMPT-19.md`, `docs/reentrega/plan_prompts_reentrega.md`, `docs/reentrega/corrida_p0_diagnostico_cambios.md`, `docs/reentrega/diagnostico_reentrega.md`, `docs/reentrega/P2/P2_diagnostico_cambios.md`, `docs/reentrega/P2/P2-06_diagnostico_cambios.md`.
- Snapshot ASM: `test/tp2/generales/00_programa_valido.asm` (P4-01).
- Runner y expectativas: `test/TP3_SEMANTICAS/ejecutar.sh`, `test/TP3_SEMANTICAS/README.md` y los cinco `.txt` listados en 5.2 (P6-02).
- Scripts TP4: los diez listados en 5.3 (P6-02).

Sin seguimiento:

```text
?? docs/reentrega/P6/
```

Ignorados por `.gitignore` pero modificados en disco: los cinco `.md` de
`ASSETS/` de P6-01. El `.asm` regenerado por la suite 5.0 también quedó cubierto
por `.gitignore:7` y fue eliminado; no aparece en `git status`.

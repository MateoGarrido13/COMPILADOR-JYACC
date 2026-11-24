# P4-02 — Verificación ASM del salto

Fecha: 24/11/2026  
Prompt del plan: `P4-02`, “Regresión estática” (`docs/reentrega/plan_prompts_reentrega.md:398-414`).  
Estado inicial: commit `d998470` (`4-01 emitirCodigo() agrega un jmp al main`), con P4-01 ya integrado y árbol limpio.  
Dependencia: P4-01.  
Resultado: verificación estática OK; sin cambios de código ni de fuentes `.txt`.

---

## 1. Alcance del prompt

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

Límites respetados:

- **No se cambió código Java, gramática ni `Parser.java`.** `git status --short` estaba limpio antes de crear esta documentación.
- **No se cambió ningún `.txt`** de `test/` ni de `test_tp_4/`.
- **No se modificaron los snapshots `.asm` versionados.** P4-01 decidió conservarlos como evidencia del layout anterior; por eso siguen mostrando el defecto que originó este prompt.
- Único artefacto nuevo del repo: este archivo de documentación.

---

## 2. Preparación

Se generó un build fresco desde `HEAD` (`d998470`) y se ejecutaron todos los casos rumbo a `/tmp/opencode/tp3run`, sin usar los `.class` viejos de la raíz:

```bash
rm -rf /tmp/opencode/tp3run /tmp/opencode/p4_02_build /tmp/opencode/p4_02_refs
mkdir -p /tmp/opencode/tp3run /tmp/opencode/p4_02_build /tmp/opencode/p4_02_refs
javac -encoding UTF-8 -d /tmp/opencode/p4_02_build *.java \
      "acciones semanticas"/*.java otras_acciones/*.java
cp -a test test_tp_4 /tmp/opencode/tp3run/
find /tmp/opencode/tp3run -name '*.txt' -print0 | while IFS= read -r -d '' f; do
  rel=${f#/tmp/opencode/tp3run/}
  d=/tmp/opencode/tp3run/out/$(dirname "$rel")
  mkdir -p "$d"
  java -cp /tmp/opencode/p4_02_build Main "$f" > "$d/$(basename "$rel").out" 2>&1
done
```

Salida:

```text
javac HEAD OK
Casos ejecutados: 299
ASM generados: 161
```

Los 161 ASM generados son idénticos a los de la corrida final de P4-01: `diff -rq /tmp/opencode/p4_01_refs/run_repo /tmp/opencode/tp3run` solo reporta `Only in /tmp/opencode/tp3run: out`, que es el directorio de capturas de stdout de esta corrida. No hay diferencias en `.asm`.

---

## 3. `grep -n -A3 '^start:'` sobre los ASM generados

Se ejecutó el grep permitido sobre los 161 ASM:

```bash
find /tmp/opencode/tp3run -name '*.asm' -not -path '*/out/*' -print0 \
  | xargs -0 grep -n -A3 '^start:' > /tmp/opencode/p4_02_refs/grep_start_A3.txt
```

Conteo de la línea siguiente a `start:`:

| Línea inmediatamente después de `start:` | Casos |
|---|---:|
| `jmp main_inicio` | 161 |
| `fn_...:` / `mt_...:` directo | 0 |
| otro | 0 |

Aceptación:

```text
start: -> fn_/mt_ sin jmp intermedio: 0
```

Dos ejemplos del grep, en los casos de aceptación del plan:

```text
.../01_funcion_ulongint.asm:20:start:
.../01_funcion_ulongint.asm-21-    jmp main_inicio
.../01_funcion_ulongint.asm-22-fn_suma_main:
.../01_funcion_ulongint.asm-23-    push ebp

.../03_recursion_factorial_metodo.asm:19:start:
.../03_recursion_factorial_metodo.asm-20-    jmp main_inicio
.../03_recursion_factorial_metodo.asm-21-fn_fact_main:
.../03_recursion_factorial_metodo.asm-22-    push ebp
```

El orden es el pedido: `start:` → `jmp` → prólogo de la subrutina. La subrutina queda entre el salto y `main_inicio:`, sin ejecutarse por caída secuencial.

---

## 4. Verificación pedida: `sed -n '18,40p'`

Salida de `/tmp/opencode/tp3run/test_tp_4/Funciones_y_metodos/01_funcion_ulongint.asm`:

```asm
.code
start:
    jmp main_inicio
fn_suma_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
    jc error_overflow_suma
    mov @aux3, eax
    mov eax, @aux3
    leave
    ret
fin_suma_main:
    leave
    ret
main_inicio:
    mov eax, c_3_ul_ul
    mov x_main, eax
    mov eax, c_4_ul_ul
    mov y_main, eax
    ; (PAR, a:main:suma, x:main)
```

`main_inicio:` aparece después de `fin_suma_main:` y antes del primer `mov` del main. El `ret` de la subrutina ya no puede caer sobre el código principal.

---

## 5. Snapshots versionados

Los 51 `.asm` del repo se dejaron intactos. Sirven como comparación contra el estado anterior a P4-01:

| Conjunto | Total | `start:` → `fn_`/`mt_` directo | `start:` → `jmp main_inicio` |
|---|---:|---:|---:|
| ASM generados por `d998470` | 161 | 0 | 161 |
| Snapshots versionados en el repo | 51 | 14 | 0 |

Los 14 snapshots con `start:` → `fn_`/`mt_` directo son los casos con subrutina, entre ellos `test_tp_4/Funciones_y_metodos/01_funcion_ulongint.asm` y `test_tp_4/Integracion/03_recursion_factorial_metodo.asm`. El resto de snapshots arranca con la primera instrucción del main (`mov eax, ...`, `fld ...`, `invoke crt_printf`, etc.), también sin `jmp`, porque son artefactos generados antes de P4-01.

Esto no es una falla de la aceptación: la aceptación se evalúa sobre los ASM generados por el código actual. Los snapshots se conservan como evidencia de la raíz confirmada y no se regeneran en este prompt, para no mezclar un cambio de artefactos con esta verificación.

---

## 6. Límite de verificación

No se ensambló ni ejecutó ningún `.asm`: el entorno es Linux y no hay MASM32 (`ml.exe`) ni Windows. P4-02 cubre solo la inspección estática del layout. La ejecución real de los dos casos de aceptación sigue pendiente para la ola final en MASM32/Windows.

---

## 7. Estado de git

Antes de este documento:

```text
## Reentrega
```

Sin archivos modificados. Después de escribir esta documentación:

```text
## Reentrega
?? docs/reentrega/P4/P4-02_diagnostico_cambios.md
```

P4-02 no genera cambios de código; no requiere commit de fuentes.

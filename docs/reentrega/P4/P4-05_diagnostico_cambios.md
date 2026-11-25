# P4-05 — Revisión de frames y argumentos

Fecha: 24/11/2026  
Prompt del plan: `P4-05`, “Revisión guiada” (`docs/reentrega/plan_prompts_reentrega.md:454-471`).  
Estado inicial: `HEAD` `e255c56` (`P4-03 .asm actualizados y confirmacion de resultados`), con `GeneradorAssembler.java` limpio.  
Dependencia nominal: P4-04 (ver nota abajo).  
Resultado: revisión estática OK; sin cambios de código.

---

## 1. Alcance del prompt

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

Los números de línea del prompt son anteriores a P4-01. Tras el `jmp main_inicio`, `emitirLlamada()` está en `GeneradorAssembler.java:1554-1652`.

---

## 2. Revert de P4-04

El usuario deshizo el cambio de P4-04 antes de este prompt. `GeneradorAssembler.java` volvió a `HEAD`: `emitirFinFuncion()` vuelve a emitir `fin_<sufijo>:` seguido de `leave`/`ret`.

Razón del revert: existen funciones sin terceto `ret` en el IR. Sin el `leave`/`ret` de `fin_*`, esas subrutinas no tendrían epílogo; el profesor puede ingresar ejemplos propios con funciones sin `ret`, así que conservar esa rutina de salida es más seguro que eliminar el duplicado.

Consecuencia documental: `docs/reentrega/P4/P4-04_diagnostico_cambios.md` describe el cambio aplicado, pero ese cambio ya no está en el código. Queda como registro histórico de la corrida; no se modificó en esta oportunidad porque P4-05 es solo revisión. Si se quiere, debe actualizarse por separado para indicar el revert.

La dependencia “P4-04” se entiende entonces como: se evaluó el cambio, se decidió no conservarlo y se vuelve al estado P4-03.

---

## 3. Verificación de que el revert es completo

Se compiló un build fresco desde `HEAD` y se ejecutaron los 299 casos hacia `/tmp/opencode/p4_05_run` / `/tmp/opencode/p4_05_out`:

```text
javac HEAD OK
Casos: 299
ASM generados: 161
--- ASM vs baseline P4-03 ---
Total ASM diffs: 0
--- stdout vs baseline P4-03 ---
0
```

Los 161 ASM y los 299 stdout son idénticos a la corrida de P4-03 (que ya tenía `jmp main_inicio` y el epílogo de `fin_*` restaurado). No queda rastro del cambio de P4-04 en la salida.

---

## 4. Caso 1 — `01_funcion_ulongint.asm`

Declaración: `ulongint function suma(ulongint a, ulongint b)`. Dos parámetros de 4 bytes.

Callee:

```asm
fn_suma_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    add eax, dword ptr [ebp+12]
```

Caller:

```asm
    sub esp, 8
    mov eax, x_main
    mov dword ptr [esp+0], eax
    mov eax, y_main
    mov dword ptr [esp+4], eax
    call fn_suma_main
    add esp, 8
```

Correspondencia:

| Slot caller | Slot callee | Parámetro |
|---|---|---|
| `[esp+0]` | `[ebp+8]` | `a` |
| `[esp+4]` | `[ebp+12]` | `b` |

Tras `push ebp; mov ebp, esp`, `[ebp+4]` es la dirección de retorno y `[ebp+8]` es el primer argumento. El caller limpia exactamente los 8 bytes reservados con `add esp, 8`. **Sin desalineo.**

---

## 5. Caso 2 — `02_funcion_doublef.asm`

Declaración: dos parámetros `doublef`, 8 bytes cada uno (`DQ`).

Callee:

```asm
fn_escala_main:
    push ebp
    mov ebp, esp
    fld qword ptr [ebp+8]
    fmul qword ptr [ebp+16]
```

Caller:

```asm
    sub esp, 16
    mov eax, dword ptr [a_main+4]
    mov dword ptr [esp+4], eax
    mov eax, dword ptr [a_main]
    mov dword ptr [esp+0], eax
    mov eax, dword ptr [b_main+4]
    mov dword ptr [esp+12], eax
    mov eax, dword ptr [b_main]
    mov dword ptr [esp+8], eax
    call fn_escala_main
    add esp, 16
    fstp @aux10
```

Correspondencia:

| Slot caller | Slot callee | Parámetro |
|---|---|---|
| `[esp+0]` + `[esp+4]` | `qword [ebp+8]` | `x` |
| `[esp+8]` + `[esp+12]` | `qword [ebp+16]` | `y` |

El high dword se escribe en la dirección alta y el low en la baja, coherente con little-endian y con `fld qword ptr`. Total 16 bytes, limpiados por `add esp, 16`. El retorno queda en `ST(0)` y el caller lo baja con `fstp @aux10`; no se usa `eax`. **Sin desalineo** y sin cambios en `DQ`/`ST(0)`.

---

## 6. Caso 3 — `05_metodo_con_this.asm`

Método con `this` implícito más dos parámetros `ulongint`.

Callee:

```asm
mt_m_main_calculadora:
    push ebp
    mov ebp, esp
    ; this: dword ptr [ebp+8]
    mov eax, dword ptr [ebp+12]
    add eax, dword ptr [ebp+16]
```

Caller:

```asm
    sub esp, 12
    mov eax, offset c_main
    mov dword ptr [esp], eax
    mov eax, c_10_ul_ul
    mov dword ptr [esp+4], eax
    mov eax, c_5_ul_ul
    mov dword ptr [esp+8], eax
    call mt_m_main_calculadora
    add esp, 12
```

Correspondencia:

| Slot caller | Slot callee | Rol |
|---|---|---|
| `[esp+0]` | `[ebp+8]` | `this` |
| `[esp+4]` | `[ebp+12]` | `base` |
| `[esp+8]` | `[ebp+16]` | `extra` |

`this` ocupa el primer slot de 4 bytes, así que los formales arrancan en `[ebp+12]`. El caller reserva y limpia 12 bytes. **Sin desalineo.**

---

## 7. Chequeo global de caller-cleanup

Sobre los 161 ASM generados:

```text
calls a subrutinas (fn_/mt_): 36
con add esp inmediato tras call: 36
pares sub esp / add esp con el mismo byte count: 36
anomalías: 0
```

No hay ningún `call` sin su `add esp` correspondiente, ni `sub esp, N` que no se compense con `add esp, N`. La convención “caller limpia pila” se cumple en toda la suite generada.

---

## 8. Conclusión

No apareció ningún desalineo demostrado por ASM. La convención es coherente:

- `[ebp+8]` es el primer parámetro en funciones.
- `[ebp+8]` es `this` en métodos; los formales arrancan en `[ebp+12]`.
- Los `doublef` ocupan 8 bytes y se leen con `fld qword ptr`, sin tocar `DQ` ni `ST(0)`.
- El caller reserva con `sub esp, N` y limpia con `add esp, N` después del `call`.

No se aplica ningún cambio, porque el prompt solo permite corregir un desalineo demostrado y no hay uno. El fallo original descrito en la raíz (llegar a la subrutina sin `CALL`) ya quedó resuelto por P4-01 con `jmp main_inicio`.

---

## 9. Límite de verificación

No se ensambló ni ejecutó ningún `.asm`: el entorno es Linux y no hay MASM32 (`ml.exe`) ni Windows. La comprobación es estática sobre el listado generado; la ejecución real queda para la ola final en MASM32/Windows.

---

## 10. Estado de git

```text
## Reentrega
?? docs/reentrega/P4/P4-04_diagnostico_cambios.md
?? docs/reentrega/P4/P4-05_diagnostico_cambios.md
```

`GeneradorAssembler.java` está limpio; no hay `.asm` modificados. P4-05 no requiere commit de código; ambas documentaciones quedan a criterio del usuario.

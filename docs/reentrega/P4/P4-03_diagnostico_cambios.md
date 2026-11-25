# P4-03 — Caída a handlers runtime: verificación, no cambio

Fecha: 24/11/2026  
Prompt pedido: `P3-03`. No existe en el plan (`P3-00` es el único de la Ola 3); se interpreta como `P4-03`, siguiente después de P4-02 y coherente con los `.asm` de `test_tp_4` regenerados por el usuario.  
Prompt del plan: `P4-03`, “Caída a handlers runtime: verificación, no cambio” (`docs/reentrega/plan_prompts_reentrega.md:416-433`).  
Estado inicial: commit `3c8d8c1` (`4-02 confirmacion de resultados`), más 47 `.asm` regenerados por el usuario en `test_tp_4/`.  
Dependencia: P4-02.  
Resultado: verificación estática OK; sin cambios de código.

---

## 1. Alcance del prompt

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

Límites respetados:

- **No se cambió código Java ni gramática.** El único cambio de código sigue siendo P4-01, ya commiteado en `d998470`.
- **No se movió `invoke ExitProcess, 0`** ni se quitaron los chequeos runtime (`jc`, `jne`, etc.).
- **No se modificó ningún `.asm`**. Los 47 archivos de `test_tp_4/` estaban modificados por el usuario al iniciar este prompt; se usaron como entrada de la revisión y se dejaron intactos.
- Único artefacto nuevo del repo: esta documentación.

---

## 2. Estado de los ASM regenerados

El usuario regeneró los 47 `.asm` de `test_tp_4/` con el generador actual. Se compararon contra los ASM producidos en la corrida fresca de P4-02 (`/tmp/opencode/tp3run`, build de `HEAD` antes del commit documental `3c8d8c1`, código idéntico):

```text
ASM en test_tp_4: 47
idénticos byte a byte: 47
difieren: 0
```

Por tanto, los snapshots regenerados corresponden exactamente al generador con `jmp main_inicio` de P4-01; no hay artefactos editados a mano ni salida de un build viejo.

---

## 3. Caso con `overflow_suma`

Archivo: `test_tp_4/Runtime_chequeos/b_suma_ulongint/03_suma_literal_desborda.asm`.

Cola del `.code`:

```asm
main_inicio:
    mov eax, c_4294967295_ul_ul
    add eax, c_1_ul_ul
    jc error_overflow_suma
    mov @aux2, eax
    mov eax, @aux2
    mov r_main, eax
    invoke crt_printf, offset fmt_ulongint, r_main
    jmp fin_normal
error_overflow_suma:
    invoke crt_printf, offset msg_overflow_suma
    invoke ExitProcess, 1
fin_normal:
    invoke ExitProcess, 0
end start
```

Lectura:

- `jc error_overflow_suma` es un salto **condicional explícito** que solo se toma cuando `add` activa `CF`. No es entrada por caída secuencial.
- Si no hay overflow, la ejecución sigue por `mov @aux2`, `pout` y llega a `jmp fin_normal`. Ese salto incondicional evita caer en `error_overflow_suma:`.
- `error_overflow_suma:` queda detrás del flujo normal, imprime el mensaje y termina con `ExitProcess, 1`.
- `fin_normal:` queda detrás de todos los `error_*` y lleva a `ExitProcess, 0`.

No se movió `ExitProcess` ni se quitó el `jc`; el layout existente ya es correcto.

---

## 4. Caso sin chequeos runtime

Archivo: `test_tp_4/Salida_consola/01_pout_variable_ulongint.asm`.

Cola del `.code`:

```asm
main_inicio:
    mov eax, c_7_ul_ul
    mov x_main, eax
    mov eax, c_4294967295_ul_ul
    mov y_main, eax
    invoke crt_printf, offset fmt_ulongint, x_main
    invoke crt_printf, offset fmt_ulongint, y_main
    invoke ExitProcess, 0
end start
```

No aparece `error_*`, ni `fin_normal:`, ni `jmp fin_normal`. El flujo normal termina directamente en `ExitProcess, 0`. No hay handlers en los que se pueda caer accidentalmente.

---

## 5. Revisión estática de los 47 ASM

Se revisó cada uno de los 47 `.asm` regenerados con estos criterios:

- Con handlers:
  - `main_inicio:` existe y precede a todo `error_*`.
  - Existe exactamente un `jmp fin_normal` y está antes del primer `error_*`.
  - La línea inmediatamente anterior al primer `error_*` es `jmp fin_normal`.
  - Cada `error_*` posterior está inmediatamente precedido por `invoke ExitProcess, N` (`N` entre 1 y 4) del handler anterior, nunca por código del main.
  - `fin_normal:` existe y está detrás de todos los `error_*`.
  - Tras `fin_normal:` viene `invoke ExitProcess, 0` y luego `end start`.
- Sin handlers:
  - No hay `error_*`, `fin_normal:` ni `jmp fin_normal`.
  - Ninguna línea referencia un `error_*`.
  - La cola es `invoke ExitProcess, 0` / `end start`.

Resultado:

```text
ASM revisados: 47
con chequeos runtime: 28
sin chequeos runtime: 19
Fallos por entrada secuencial a error_*: 0
```

Caso especial revisado aparte: los cuatro archivos de `test_tp_4/Runtime_chequeos/h_enum_asignacion/` contienen `jmp error_enum_invalido` después de comparar el valor contra cada elemento válido (`je enum_valido_0`). Ese `jmp` es una rama explícita para el valor inválido, no una caída secuencial: la etiqueta `error_enum_invalido:` sigue estando precedida por `jmp fin_normal`, y `fin_normal:` queda detrás. Un primer checker marcó esos cuatro casos como falsos positivos al tratar cualquier `jmp error_*` como fallo; al distinguir “salto explícito” de “caída secuencial”, pasan los 47.

---

## 6. Límite de verificación

No se ensambló ni ejecutó ningún `.asm`: el entorno es Linux y no hay MASM32 (`ml.exe`) ni Windows. P4-03 confirma solo el layout estático del `.code`. La comprobación en ejecución real sigue pendiente para la ola final en MASM32/Windows.

---

## 7. Estado de git

Al iniciar:

```text
## Reentrega
 M test_tp_4/... (47 archivos .asm regenerados por el usuario)
```

Sin cambios en `.java`, `.y`, `Parser.java` ni en fuentes `.txt`. Después de escribir este documento se agrega:

```text
?? docs/reentrega/P4/P4-03_diagnostico_cambios.md
```

P4-03 no requiere commit de código; los `.asm` regenerados y esta documentación quedan a criterio del usuario.

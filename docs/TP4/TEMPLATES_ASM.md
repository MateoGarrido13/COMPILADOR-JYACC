# Templates de código Assembler implementados (TP4)

Archivo vivo: se agrega un template cada vez que un prompt del plan incorpora una nueva traducción en `GeneradorAssembler.java`. Los templates están escritos con la sintaxis de las filminas de la cátedra (`ASSETS/FilminasSalida/`), en particular:

- `11.1.Assembler_Pentium_Sintaxis.pdf` — `CMP`, `JMP`, `Jcc`, `SAHF`, `FCOM/FCOMP`, `FSTSW`; ejemplo de comparación FPU con `FLD / FCOM / FSTSW / MOV AX / SAHF / JB`.
- `10.2.Generación de Código AS-Ass PI-Ass T-Ass.pdf` — generación de bifurcaciones desde tercetos: `(BF,[n],Label)` → `Jcc Label`, `(BI,Label,-)` → `JMP Label`, tercetos etiqueta → `Label:`.
- `winasmtut.pdf` — estructura `.data`/`.code`, `option casemap:none`, `label:`, `end start`, `cmp`/`jz`/`jnz`/`jmp`.
- `printf-variables.pdf` — impresión con `printf`/`StdOut`, formatos `%u`/`%f`/`%s`, `DQ` para float, y el prefijo/sufijo de pila necesario dentro de funciones.

Convención general del mecanismo de **variables auxiliares** (enunciado TP4): cada terceto que produce valor se materializa como `@auxN` tipado (`DD` para `ulongint`/booleano, `DQ` para `doublef`).

---

## T-01 — Encabezado y esqueleto MASM32

**Prompt**: 2 · **Caso**: todo programa generado.

```asm
.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc

.data
    ...

.code
start:
    ...
    invoke ExitProcess, 0
end start
```

**Fuente filmina**: `winasmtut.pdf` (labels, `option casemap:none`, `end start`),TP4 enunciado (MASM32 32-bit, `masm32rt.inc`).

---

## T-02 — Sección `.DATA` desde la tabla de símbolos

**Prompt**: 2 (extendido en 4a) · **Caso**: variables, constantes, cadenas, temporales.

```asm
.data
    a_main        DD ?          ; ulongint -> 32 bits sin signo
    z_main        DQ ?          ; doublef  -> 64 bits IEEE
    c_5_ul_ul     DD 5          ; constante ulongint materializada
    c_0_5         DQ 0.5        ; constante doublef materializada
    s_hola        DB "hola",0   ; cadena terminada en 0
    @aux3         DQ ?          ; temporal doublef
    @aux4         DD ?          ; temporal ulongint / booleano
    @fpu_status   DW ?          ; estado FPU (solo si hay comparaciones doublef)
```

Notas:
- Los nombres se sanitizan en `TablaSimbolos.nombreAsmDe()` para evitar colisiones con instrucciones/registros MASM.
- `@fpu_status` se emite condicionalmente (`calcularUsoFpuStatus()`).
- Tipos: `DD`/`DQ`/`DB`/`DW` según la filmina 11.1 (registros y memoria de 32/64 bits).

---

## T-03 — Asignación escalar `:=`

**Prompt**: 3 · **Caso**: `x := 5$ul;` / `z := todf(x);`.

```asm
; ulongint
mov eax, c_5_ul_ul
mov a_main, eax

; doublef (origen ya en DQ / @auxN)
fld @aux3
fstp z_main
```

**Fuente filmina**: `11.1.Assembler_Pentium_Sintaxis.pdf` (`MOV` reg/mem, transferencia de datos).

---

## T-04 — Aritmética `ulongint` (`+ - * /`)

**Prompt**: 3 · **Caso**: `y := x + 3$ul;`, `z := x * y;`.

```asm
mov eax, a_main
add eax, c_3_ul_ul
mov @aux3, eax

mov eax, a_main
imul eax, b_main
mov @aux5, eax
```

Nota: la división entera (`div`) y los chequeos `b/e/f/h` (Prompt 5) aún no instrumentan este template.

---

## T-05 — Aritmética `doublef` (`+ - * /`)

**Prompt**: 3 · **Caso**: `a := b + c;` con todos `doublef`.

```asm
fld b_main
fadd c_main
fstp @aux7
```

**Fuente filmina**: `11.1.Assembler_Pentium_Sintaxis.pdf` (instrucciones aritméticas de la FPU 80x87).

---

## T-06 — Conversión explícita `todf`

**Prompt**: 3 · **Caso**: `a := todf(x);` (Tema 33).

```asm
fild x_main        ; entero de 32 bits -> st(0) como real
fstp @aux3         ; @aux3 : DQ
```

**Fuente filmina**: `11.2.Assembler_Pentium_Conversiones.pdf`.

---

## T-07 — Comparador sobre `ulongint` → booleano 0/1

**Prompt**: 4a · **Caso**: `if (a > b)` ⇒ IR `(>, a, b)` con `@aux4`.

```asm
mov eax, a_main
cmp eax, b_main
seta al
movzx eax, al
mov @aux4, eax
```

Mapeo `SETcc` (aux = 1 si la relación es **cierta**):

| IR | `SETcc` |
|---|---|
| `<`  | `setb`  |
| `<=` | `setbe` |
| `>`  | `seta`  |
| `>=` | `setae` |
| `==` | `sete`  |
| `!=` | `setne` |

**Fuente filmina**: `11.1.Assembler_Pentium_Sintaxis.pdf` (`CMP`, flags CF/ZF, saltos sin signo `JA/JAE/JB/JBE`, `SAHF`).

---

## T-08 — Comparador sobre `doublef` → booleano 0/1

**Prompt**: 4a · **Caso**: `if (todf(x) > 0.5)` ⇒ IR `(>, @aux3, 0.5)`.

```asm
fld   @aux3         ; primer operando (DQ)
fcomp c_0_5         ; compara y hace pop (pila FPU balanceada)
fstsw @fpu_status   ; palabra de estado a memoria (DW)
mov   ax, @fpu_status
sahf                ; ZF/CF de la FPU -> flags del CPU
seta al             ; 1 si @aux3 > 0.5
movzx eax, al
mov @aux4, eax
```

**Fuente filmina**: `11.1.Assembler_Pentium_Sintaxis.pdf`, ejemplo "Comparación" (líneas ~832-851):
`FLD mem1 / FCOM mem2 / FSTSW aux_mem_2bytes / MOV AX,aux / SAHF / JB etiqueta`.
Se usa `FCOMP` en lugar de `FCOM` para no dejar un valor en `ST(0)`.

---

## T-09 — Etiqueta de salto IR

**Prompt**: 4a · **Caso**: terceto `(label_2, --, --)`.

```asm
label_2:
```

**Fuente filmina**: `10.2.Generación de Código...` (tercetos etiqueta → `Label20:`), `winasmtut.pdf` (`_label: add eax, ecx`).

---

## T-10 — `BF` sobre comparador `ulongint` (salto inverso)

**Prompt**: 4a · **Caso**: `(BF,[4],label_1)` donde `[4]` es `(>, a, b)`.

```asm
mov eax, a_main
cmp eax, b_main
jbe label_1         ; NOT(>) = <=  -> salta si la condición es falsa
```

Equivalencia de la filmina 10.2: `(BF, [12], 17)` con `(>, [10], [11])` → `JLE Label17`.

---

## T-11 — `BF` sobre comparador `doublef`

**Prompt**: 4a · **Caso**: `(BF,[4],label_1)` donde `[4]` es `(>, @aux3, 0.5)`.

```asm
fld   @aux3
fcomp c_0_5
fstsw @fpu_status
mov   ax, @fpu_status
sahf
jbe label_1         ; NOT(>) = <= , códigos sin signo según filmina 11.1
```

---

## T-12 — `BF` sobre booleano ya materializado

**Prompt**: 4a · **Caso**: condición dada como variable/auxiliar 0/1 (no comparador).

```asm
cmp @aux4, 0
je label_1          ; BF salta si falso
```

**Fuente filmina**: `winasmtut.pdf` (`cmp Sum, 147 / jz _good`), `11.1` (`JZ`/`JNZ`).

---

## T-13 — `BI` (salto incondicional)

**Prompt**: 4a · **Caso**: `(BI, label_2, --)` al cerrar el `then`.

```asm
jmp label_2
```

**Fuente filmina**: `10.2.Generación de Código...` (`(BI,19,-)` → `JMP Label19`), `11.1` (`JMP label`).

---

## T-14 — Formatos de impresión en `.DATA`

**Prompt**: 4b · **Caso**: programa con uno o más `pout`. Se emiten solo los usados.

```asm
.data
    ...
    fmt_ulongint DB "%u",0      ; si hay pout de ulongint
    fmt_doublef DB "%f",0       ; si hay pout de doublef
    fmt_cadena  DB "%s",0       ; si hay pout de cadena
```

**Fuente filmina**: `printf-variables.pdf` (`printf cfm$("%u\n"), ulongi`, `printf cfm$("%.20Lf\n"), floati`). Se usa `%f` en lugar de `%.20Lf` según el enunciado del Prompt 4b; se omiten CRLFs (decisión en `DECISIONES_P4b.md`). No hay buffers temporales: `crt_printf` recibe los valores por valor.

---

## T-15 — `pout` de `ulongint`

**Prompt**: 4b · **Caso**: `(pout, x:main, --)`, `(pout, 5$ul, --)` o `(pout, [9], --)` con `@aux9 : DD`.

```asm
invoke crt_printf, offset fmt_ulongint, x_main
invoke crt_printf, offset fmt_ulongint, c_5_ul_ul
invoke crt_printf, offset fmt_ulongint, @aux9
```

Sin `offset` en el valor: se pasa el dword por valor. Válido para variables, constantes y auxiliares booleanos/resultado de `CALL`.

---

## T-16 — `pout` de `doublef`

**Prompt**: 4b · **Caso**: `(pout, z:main, --)` o `(pout, [11], --)` con `@aux11 : DQ`.

```asm
invoke crt_printf, offset fmt_doublef, z_main
invoke crt_printf, offset fmt_doublef, @aux11
```

El símbolo debe ser `DQ` (64 bits), como advierte la filmina `printf-variables.pdf`. No se carga la FPU: `INVOKE` pasa el qword directamente a `crt_printf`.

---

## T-17 — `pout` de cadena

**Prompt**: 4b · **Caso**: `(pout, hola, --)` con `hola DB "hola",0` en `.DATA`.

```asm
invoke crt_printf, offset fmt_cadena, offset hola
```

Alternativa de la filmina: `invoke StdOut, offset hola`. Se eligió `crt_printf` para uniformidad con los tipos numéricos. El `offset` del valor es obligatorio (si no, se pasarían los primeros 4 bytes del contenido como puntero).

---

## Mapa rápido: comparador → `SETcc` (verdadero) / `Jcc` de BF (falso)

| Comparador | `SETcc` | `Jcc` en BF | Condición de flags |
|---|---|---|---|
| `<`  | `setb`  | `jae` | CF=1 → cierto |
| `<=` | `setbe` | `ja`  | CF=1 o ZF=1 → cierto |
| `>`  | `seta`  | `jbe` | CF=0 y ZF=0 → cierto |
| `>=` | `setae` | `jb`  | CF=0 → cierto |
| `==` | `sete`  | `jne` | ZF=1 → cierto |
| `!=` | `setne` | `je`  | ZF=0 → cierto |

Válido para `ulongint` (`CMP`) y para `doublef` (`FCOMP` + `FSTSW` + `SAHF`), porque la filmina 11.1 indica usar saltos de datos **sin signo** tras la comparación FPU.

---

## Templates pendientes (no implementados aún)

| Template | Prompt | Caso previsto |
|---|---|---|
| Prólogue/epílogue y stack frame | 4c | `push ebp / mov ebp,esp / ... / leave / ret` |
| `CALL` / `CALLMET` / `PAR` / `ret` | 4c | `call fn_x`, `add esp, n*4`, retorno en `EAX`/`ST(0)` |
| Layout de objetos y `COPY` | 4d | slots `DD`/`DQ`, `rep movsd` |
| Guardas runtime `b/e/f/h` | 5 | mensajes + `invoke ExitProcess, codigo` |

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

Nota 6.1: el archivo `.asm` se escribe con terminadores **CRLF** (normalización en `GeneradorAssembler.escribirArchivo()`); el texto interno del generador sigue usando `\n`. No se emite `ALIGN 8`: con `.386` la alineación de segmento es DWORD(4) y en x86 no es requisito de corrección para `DQ`. Ver `DECISIONES_6.1.md`.

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

Nota actualizada: la división entera (`div`) sigue sin chequeo porque la letra **a** (división por cero) no fue asignada al Grupo 43. Los chequeos `b` (jc tras `add`) y `f` (jb tras `sub`) SÍ instrumentan este template desde el Prompt 5. Ver `ESTADO_RUNTIME_P5.md`.

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

## T-18 — Prólogo, frame y epílogo (funciones y métodos)

**Prompt**: 4c · **Caso**: `fn_suma_main:` / `mt_m_main_calculadora:`.

```asm
fn_suma_main:
    push ebp
    mov ebp, esp
    sub esp, 8          ; solo si hay locales (redondeado a 8)
    ; ... cuerpo ...
    leave
    ret
fin_suma_main:
    leave
    ret                 ; epílogo de seguridad para caminos sin ret explícito
```

```text
[ebp+16] pb | [ebp+12] pa | [ebp+8] a | [ebp+0] EBP del caller
[ebp-4]  local ulongint | [ebp-16] local doublef alineada | ESP -> frame
```

Para métodos, `this` ocupa `[ebp+8]` y los formales arrancan en `[ebp+12]`.

**Fuente filmina**: `printf-variables.pdf`, caso especial "Funciones" (`push ebp / mov ebp,esp / sub esp …` y sufijo `mov esp,ebp / pop ebp / ret`). Se usa `leave` como equivalente del sufijo.

---

## T-19 — `PAR` + `CALL` + limpieza (`ulongint`)

**Prompt**: 4c · **Caso**: `r := suma(x, y)[1$ul, 2$ul];`.

```asm
    ; (PAR, a:main:suma, x:main)     ; comentario en orden IR
    ; (PAR, b:main:suma, y:main)
    sub esp, 8                       ; caller reserva por FORMAL
    mov eax, x_main
    mov dword ptr [esp+0], eax       ; a -> primer formal
    mov eax, y_main
    mov dword ptr [esp+4], eax       ; b -> segundo formal
    call fn_suma_main
    add esp, 8                       ; caller limpia
    mov @aux10, eax                  ; retorno ulongint
```

Con orden `[2,1]`, las cargas se emiten en ese orden pero `1$ul` sigue yendo a `[esp+0]` si es el formal `a`.

---

## T-20 — `PAR` de `doublef` (qword en dos dwords)

**Prompt**: 4c · **Caso**: `escala(a, b)` con `a`, `b` `doublef`.

```asm
    sub esp, 16
    mov eax, dword ptr [a_main+4]    ; high del primer qword
    mov dword ptr [esp+4], eax
    mov eax, dword ptr [a_main]      ; low
    mov dword ptr [esp+0], eax
    mov eax, dword ptr [b_main+4]    ; high del segundo qword
    mov dword ptr [esp+12], eax
    mov eax, dword ptr [b_main]
    mov dword ptr [esp+8], eax
    call fn_escala_main
    add esp, 16
    fstp @aux10                      ; retorno doublef desde ST(0)
```

 Dentro del callee: `fld qword ptr [ebp+8]` / `fmul qword ptr [ebp+16]`.

---

## T-21 — `ret` según tipo de retorno

**Prompt**: 4c · **Caso**: `ret(a + b);` y `ret(x * y);`.

```asm
; ulongint
mov eax, dword ptr [ebp+8]
add eax, dword ptr [ebp+12]
leave
ret

; doublef (el valor queda en ST(0))
fld qword ptr [ebp+8]
fmul qword ptr [ebp+16]
fstp @aux3
fld @aux3
leave
ret
```

---

## T-22 — `CALLMET`: `this` + llamada directa

**Prompt**: 4c · **Caso**: `c.m(10$ul, 5$ul)[1$ul, 2$ul];`.

```asm
    sub esp, 12                      ; this (4) + 2 formales (4+4)
    mov eax, offset c_main
    mov dword ptr [esp], eax         ; this
    mov eax, c_10_ul_ul
    mov dword ptr [esp+4], eax       ; base -> ebp+12
    mov eax, c_5_ul_ul
    mov dword ptr [esp+8], eax       ; extra -> ebp+16
    call mt_m_main_calculadora
    add esp, 12
    mov @aux10, eax
```

No hay vtable: el IR trae `m:main:calculadora` ya resuelto. Si el receptor es un parámetro se usa `mov eax, dword ptr [ebp+…]`; si es local, `lea eax, [ebp-…]` en lugar de `offset`.

---

## T-23 — Locales en el stack frame (no en `.DATA`)

**Prompt**: 4c · **Caso**: `ulongint t; doublef d;` declaradas antes del `begin`.

```asm
fn_mezcla_main:
    push ebp
    mov ebp, esp
    sub esp, 16
    mov eax, dword ptr [ebp+8]
    add eax, c_1_ul_ul
    mov @aux3, eax
    mov eax, @aux3
    mov dword ptr [ebp-4], eax      ; t
    fild dword ptr [ebp-4]
    fstp @aux5
    fld @aux5
    fstp qword ptr [ebp-16]         ; d (alineada)
    mov eax, dword ptr [ebp-4]
    leave
    ret
```

`.DATA` no contiene `t_main_mezcla` ni `d_main_mezcla`. El filtro vive en `GeneradorAssembler.emitirData()`; la TS de TP3 no se modifica.

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

## Templates Prompt 4d — Objetos y atributos

---

## T-24 — Layout de atributos de objeto en `.DATA`

**Prompt**: 4d · **Caso**: `class punto { ulongint x; doublef y; } punto p1;`.

```asm
.data
    x_main_punto DD ?
    y_main_punto DQ ?
    p1_x_main DD ?
    p1_y_main DQ ?
```

Cada atributo de objeto es una entrada independiente en `.DATA` con nombre calificado (`p1.x:main` → `p1_x_main`). El tipo ASM es `DD` para `ulongint`/enum y `DQ` para `doublef`.

---

## T-25 — Lectura y escritura de `ReferenciaAtributo` (`obj.atr`)

**Prompt**: 4d · **Caso**: `p1.x := 5$ul;` y `pout(p1.x);`.

```asm
; escritura
mov eax, c_5_ul_ul
mov p1_x_main, eax

; lectura
invoke crt_printf, offset fmt_ulongint, p1_x_main
```

Para `doublef`: `fld <valor> / fstp qword ptr <atributo>` en escritura, y `invoke crt_printf, offset fmt_doublef, <atributo>` en lectura.

---

## T-26 — `COPY` estructural entre objetos compatibles

**Prompt**: 4d (corregido en 4e) · **Caso**: `p2 := p1;` donde ambos son `punto` (`ulongint x; doublef y;`).

```asm
mov eax, p1_x_main
mov p2_x_main, eax
mov eax, dword ptr [p1_y_main+4]
mov dword ptr [p2_y_main+4], eax
mov eax, dword ptr [p1_y_main]
mov dword ptr [p2_y_main], eax
```

Copia **posicional** campo-a-campo: un `mov` por atributo `ulongint`/enum, dos `mov` de dwords (high primero) por atributo `doublef`. Se recorre el layout efectivo en paralelo: bases primero (según `heredaDe`), luego atributos propios; un propio con el mismo nombre reemplaza al heredado sin moverlo de posición. La compatibilidad la decide `Entorno.clasesCompatibles()` por tipos, no por nombres.

Ejemplo de nombres distintos con el mismo layout (`izq { x, d }` → `der { y, e }`):

```asm
mov eax, a_x_main          ; posicion 0 (ulongint)
mov b_y_main, eax
mov eax, dword ptr [a_d_main+4]
mov dword ptr [b_e_main+4], eax
mov eax, dword ptr [a_d_main]
mov dword ptr [b_e_main], eax
```

Los corchetes son obligatorios en la sintaxis MASM usada por el generador (`dword ptr [<sym>+4]`).

---

## T-27 — `CLASE` / `FIN_CLASE`

**Prompt**: 4d · **Caso**: `class punto ... end;`.

No emiten instrucciones en `.CODE` (solo marcadores de comentario). Los atributos de objetos se emiten en `.DATA` vía `emitirData()`.

---

## Templates Prompt 4e — Integración final

---

## T-28 — Llamada recursiva directa (`fact`)

**Prompt**: 4e · **Caso**: `ret(n * fact(n - 1$ul)[1$ul]);`.

```asm
fn_fact_main:
    push ebp
    mov ebp, esp
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    setbe al
    movzx eax, al
    mov @aux3, eax
    mov eax, dword ptr [ebp+8]
    cmp eax, c_1_ul_ul
    ja label_1
    mov eax, c_1_ul_ul
    leave
    ret
label_1:
    mov eax, dword ptr [ebp+8]
    sub eax, c_1_ul_ul
    mov @aux8, eax
    ; (PAR, n:main:fact, [8])
    sub esp, 4
    mov eax, @aux8
    mov dword ptr [esp+0], eax
    call fn_fact_main
    add esp, 4
    mov @aux10, eax
    mov eax, dword ptr [ebp+8]
    imul eax, @aux10
    mov @aux11, eax
    mov eax, @aux11
    leave
    ret
```

`Entorno.getFuncion()` consulta también `pilaFunciones`, por lo que la firma de `fact` está disponible mientras su cuerpo se está generando. Sin ese fallback el IR descartaba la llamada recursiva. El llamador limpia la pila (`add esp, 4`) y el retorno llega en EAX. Los `@auxN` son globales: el real se copia a la pila antes del `call`; no se reasignan por activación.

---

## T-29 — Programa integrado 4e: despacho, saltos y `pout`

**Prompt**: 4e · **Caso**: `if (todf(a) > d) ... else ...` con `pout` al final.

```asm
.code
start:
    fild a_main
    fstp @aux5
    fld @aux5
    fcomp d_main
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    seta al
    movzx eax, al
    mov @aux6, eax
    fld @aux5
    fcomp d_main
    fstsw @fpu_status
    mov ax, @fpu_status
    sahf
    jbe label_3
    ; ... rama then / else con etiquetas label_1..label_4 ...
label_4:
    invoke crt_printf, offset fmt_ulongint, k_main
    invoke ExitProcess, 0
end start
```

`PROGRAMA` y `FIN_PROGRAMA` son no-op: no aparecen ni como instrucción ni como comentario. El `default` de `emitirCodigo()` solo materializa `label_N:` y marca operadores desconocidos; ningún operador ya implementado cae ahí. Los `.asm` de todas las etapas se regeneraron para reflejar este cambio.

Casos 4e relacionados: `01_if_doublef_anidado`, `02_repeat_while_anidado`, `03_recursion_factorial_metodo`, `04_objetos_multiherencia_copy`, `05_pout_expresiones_llamadas` y `06_copy_layout_compat_distinto_nombre`. Matriz completa en `DECISIONES_P4.md`.

---

## Templates pendientes (no implementados aún)

| Template | Prompt | Caso previsto |
|---|---|---|
| Guardas runtime `b/e/f/h` | 5 | mensajes + `invoke ExitProcess, codigo` |

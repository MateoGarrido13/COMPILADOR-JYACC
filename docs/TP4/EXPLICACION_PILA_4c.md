# Explicación paso a paso de la pila y el "stack frame" (Prompt 4c)

Este documento explica **qué es la pila**, **por qué se usa**, **qué hace cada instrucción** y **qué significa cada nombre** (EBP, ESP, `[ebp+8]`, etc.) en la convención de llamada que implementamos.

---

## 1. ¿Qué es la pila (stack) en x86 de 32 bits?

La pila es una zona de memoria que crece **hacia direcciones más bajas** (de números grandes a números chicos). El registro **ESP** (Stack Pointer) siempre apunta a la **cima** de la pila (el último dato que se pusheó).

Operaciones básicas:
- `push eax`  →  ESP = ESP - 4;  memoria[ESP] = EAX
- `pop eax`   →  EAX = memoria[ESP];  ESP = ESP + 4
- `call etiqueta` → hace `push` de la dirección de retorno y salta a `etiqueta`
- `ret`       → saca la dirección de retorno de la pila y salta ahí

---

## 2. ¿Por qué necesitamos un "stack frame" (marco de pila)?

Cuando una función se llama:
1. El **caller** (quien llama) pone los argumentos en la pila.
2. Hace `call fn_funcion` → esto pushea la **dirección de retorno** (dónde seguir después).
3. Dentro de la función, necesitamos:
   - Acceder a los argumentos (están **arriba** de la dirección de retorno).
   - Guardar variables locales (las ponemos **abajo** de donde estamos).
   - Poder llamar a otras funciones sin perder nuestros datos.
   - Restaurar todo al volver (`ret`).

Para esto usamos **EBP** (Base Pointer) como **ancla fija** dentro de la función.

---

## 3. El prólogo estándar (entrada a la función)

```asm
fn_suma_main:
    push ebp          ; 1. Guardamos el EBP del caller
    mov ebp, esp      ; 2. EBP = ESP actual → ahora EBP es nuestra ancla
    sub esp, 8        ; 3. Reservamos espacio para LOCALES (opcional)
```

### Qué pasa en memoria (direcciones crecen hacia abajo):

```
ANTES del call (caller):
  ESP → [ arg2 ]        ← tope de pila
        [ arg1 ]
        [ dir.retorno ] ← call pushea esto
        [ ... ]

DESPUÉS del prólogo (callee):
  EBP → [ EBP_antiguo ]  ← push ebp guardó esto en [EBP]
  ESP → [ local2 ]       ← sub esp,8 reservó 8 bytes
        [ local1 ]
        [ arg1 ]         ← está en [EBP+8]
        [ arg2 ]         ← está en [EBP+12] (si son 4 bytes c/u)
        [ dir.retorno ]  ← está en [EBP+4]
        [ EBP_antiguo ]  ← está en [EBP+0]  (== [EBP])
```

---

## 4. Nombres y qué significan

| Nombre | Qué es | Para qué sirve |
|--------|--------|----------------|
| **ESP** | Stack Pointer | Siempre apunta a la cima actual de la pila. Cambia con push/pop/sub/ add. |
| **EBP** | Base Pointer | Fijo durante toda la función. Sirve de referencia: argumentos = `EBP + n`, locales = `EBP - n`. |
| `[EBP+8]` | Primer argumento (ulongint) | En cdecl/stdcall 32-bit, los args empiezan en EBP+8 (porque EBP+4 = dir.retorno, EBP+0 = EBP viejo). |
| `[EBP+12]` | Segundo argumento (ulongint) | 4 bytes después del primero. |
| `[EBP+16]` | Primer argumento doublef (high dword) | doublef = 8 bytes → ocupa dos slots. Low dword en `[EBP+8]`, high en `[EBP+12]`; el siguiente arg arranca en `[EBP+16]`. |
| `[EBP-4]` | Primera variable local (ulongint) | Espacio reservado con `sub esp`. |
| `[EBP-16]` | Variable local doublef (alineada a 8) | Se alinea para que `fld qword ptr [EBP-16]` funcione bien. |
| `leave` | `mov esp, ebp` + `pop ebp` | Deshace el frame: restaura ESP y EBP del caller de golpe. |
| `ret` | Pop dirección de retorno + jump | Vuelve al caller. **No** limpia argumentos (eso lo hace el caller con `add esp, N`). |

---

## 5. Diagrama completo: función `ulongint suma(ulongint a, ulongint b)`

### En el CALLER (antes del call):
```asm
    sub esp, 8           ; reserva 8 bytes para 2 args (4+4)
    mov eax, x_main
    mov dword ptr [esp+0], eax   ; a → slot 0
    mov eax, y_main
    mov dword ptr [esp+4], eax   ; b → slot 1
    call fn_suma_main
    add esp, 8           ; CALLER limpia (cdecl-like)
    mov @aux10, eax      ; recoge retorno en EAX
```

### En el CALLEE (`fn_suma_main`):
```asm
fn_suma_main:
    push ebp             ; guarda EBP del caller
    mov ebp, esp         ; EBP = ancla
    ; sub esp, 0         ; no hay locales en este ejemplo

    mov eax, dword ptr [ebp+8]     ; carga 'a'
    add eax, dword ptr [ebp+12]    ; suma 'b'
    ; resultado en EAX

    leave              ; mov esp,ebp / pop ebp → restaura pila y EBP
    ret                ; salta a dirección de retorno
fin_suma_main:
    leave
    ret                ; epílogo de seguridad (por si hay camino sin ret)
```

### Memoria DENTRO de `fn_suma_main` (con EBP fijado):

```
Dirección alta
[EBP+16]  ... (basura / args de caller)
[EBP+12]  b (segundo argumento)        ← dword ptr [EBP+12]
[EBP+8]   a (primer argumento)         ← dword ptr [EBP+8]
[EBP+4]   dirección de retorno         ← call pusheó esto
[EBP+0]   EBP del caller (valor viejo) ← push ebp guardó esto
[EBP-4]   (locales si los hubiera)     ← sub esp los reserva abajo
Dirección baja (ESP apunta acá o más abajo)
```

---

## 6. ¿Por qué el CALLER limpia? (cdecl-like)

En `stdcall` de Windows, la función hace `ret 8` y limpia ella misma.
Nosotros **no usamos `PROC` ni `ret n`**, usamos etiquetas simples y `ret` seco.
Ventajas:
- Igual que `crt_printf` (varargs, limpieza por caller).
- No hay desajuste si el caller apila más o menos bytes de los que la función declara.
- `add esp, N` es explícito y visible en el `.asm`.

`N = suma de tamaños reales`:
- `ulongint` = 4 bytes
- `doublef` = 8 bytes (dos dwords)
- `this` en métodos = 4 bytes

---

## 7. Métodos: el argumento oculto `this`

Un método `ulongint m(ulongint pa, ulongint pb)` se ve en ASM como:
```asm
mt_m_main_calculadora:
    push ebp
    mov ebp, esp
    ; this está en [EBP+8]
    ; pa  está en [EBP+12]
    ; pb  está en [EBP+16]
    mov eax, dword ptr [ebp+12]   ; pa
    add eax, dword ptr [ebp+16]   ; pb
    leave
    ret
```

### En el caller (CALLMET):
```asm
    sub esp, 12                ; this(4) + pa(4) + pb(4)
    mov eax, offset c_main     ; dirección del objeto receptor
    mov dword ptr [esp+0], eax ; this
    mov eax, c_10_ul_ul
    mov dword ptr [esp+4], eax ; pa
    mov eax, c_5_ul_ul
    mov dword ptr [esp+8], eax ; pb
    call mt_m_main_calculadora
    add esp, 12                ; caller limpia todo
```

---

## 8. Parámetros `doublef` (64 bits = dos dwords)

En 32 bits no hay `push qword`. Se copian **dos dwords**, high dword en la dirección **más alta**:

```asm
; caller pushea un doublef en el primer slot (8 bytes):
mov eax, dword ptr [a_main+4]    ; high dword
mov dword ptr [esp+4], eax
mov eax, dword ptr [a_main]      ; low dword
mov dword ptr [esp+0], eax
```

Dentro del callee se usa `fld qword ptr [ebp+8]` que lee **8 bytes** desde `[EBP+8]` (low) y `[EBP+12]` (high).

El segundo `doublef` arranca en `[EBP+16]` (low) y `[EBP+20]` (high).

---

## 9. Variables locales (no van en `.DATA`)

En TP3/TP4 las variables declaradas **dentro de la función** (antes del `begin`) son **locales**.
Viven en el stack frame, no en la sección `.DATA` global.

```asm
fn_mezcla_main:
    push ebp
    mov ebp, esp
    sub esp, 16          ; 4 bytes para 't' + 12 bytes (alineación+8) para 'd'
    ...
    mov dword ptr [ebp-4], eax      ; t (ulongint)
    fstp qword ptr [ebp-16]         ; d (doublef, alineada a 8)
    ...
    leave
    ret
```

`operandoAsm()` resuelve `t` → `dword ptr [ebp-4]` y `d` → `qword ptr [ebp-16]` **solo mientras `funcionActual != null`**. En `main` o en otra función, el mismo símbolo seguiría en `.DATA`.

---

## 10. Retorno según tipo

| Tipo | En callee | En caller |
|------|-----------|-----------|
| `ulongint` | `mov eax, valor` + `leave` + `ret` | `mov @auxN, eax` |
| `doublef` | `fld valor` (deja en ST(0)) + `leave` + `ret` | `fstp @auxN` (saca de ST(0) a memoria) |

El `leave; ret` restaura ESP/EBP y salta. El caller ya sabe cuántos bytes limpiar.

---

## 11. Nombres de etiquetas que verás en el `.asm`

| Patrón | Qué es |
|--------|--------|
| `fn_foo_main:` | Entrada de función `foo` en ámbito `main` |
| `fin_foo_main:` | Epílogo de seguridad de esa función |
| `mt_m_main_padre:` | Entrada de método `m` de clase `padre` en ámbito `main` |
| `fin_m_main_padre:` | Epílogo de seguridad del método |

El sanitizador convierte `:` `.` `$` en `_` y evita colisiones.

---

## 12. Resumen visual: pila completa en una llamada anidada

```
CALLER (main)                          CALLEE (suma)
────────────────────────────────────────────────────────────
sub esp, 8          ──────────►  [esp]   a (slot 0)
                         [esp+4]  b (slot 1)
                         call → push dir.ret
                                               fn_suma_main:
                                               push ebp
                                               mov ebp, esp
                                               ; EBP fijo aquí
                                               mov eax, [ebp+8]  ; a
                                               add eax, [ebp+12] ; b
                                               leave
                                               ret  ◄─── pop dir.ret
add esp, 8          ◄──────────  caller limpia
mov @aux, eax       ◄──────────  recoge EAX
```

---

## 13. Qué NO hace esta convención (y por qué)

| No hace | Por qué / delegado a |
|---------|---------------------|
| No usa `PROC` / `ENDP` / `ret n` | Para que el caller controle la limpieza (cdecl-like) y sea compatible con `printf`. |
| No hace vtable / despacho dinámico | El IR ya trae el método resuelto (`m:main:padre`). 4d hará layout real. |
| No guarda EBX/ESI/EDI en el prólogo | MASM32 `stdcall` dice que EBP/ESP/EDI/ESI/EBX son **callee-saved**, pero nuestros temporales viven en `@auxN` globales (`.DATA`) y no usamos esos registros. Si en el futuro se usan, hay que pushearlos. |
| No maneja recursión profunda con `@auxN` | Los `@auxN` son globales. Una recursión que necesite dos temporales vivos a la vez se pisa. Pendiente para 4e/6. |

---

## 14. Cómo leer el `.asm` generado y entender qué pasa

Busca estos patrones:

```asm
fn_<nombre>:          ; ← empieza la función
    push ebp
    mov ebp, esp
    sub esp, <N>      ; ← N = bytes de locales (0 si no hay)

    ; [EBP+8]  = primer arg (o this en métodos)
    ; [EBP+12] = segundo arg (o primer arg en métodos)
    ; [EBP-4]  = primera local
    ; [EBP-16] = local doublef alineada

    leave
    ret
fin_<nombre>:         ; ← epílogo de seguridad
    leave
    ret
```

En el caller:
```asm
    sub esp, <total>  ; total = suma de tamaños (4 o 8 por arg, +4 si método)
    mov eax, <valor>
    mov dword ptr [esp+<offset>], eax   ; escribe en slot del FORMAL
    ...
    call fn_<nombre>
    add esp, <total>  ; caller limpia
    mov @auxN, eax    ; o fstp @auxN si doublef
```

---

## 15. Glosario rápido

| Término | Significado |
|---------|-------------|
| **Caller** | La función que hace el `call`. |
| **Callee** | La función a la que se llama. |
| **Frame** | La porción de pila que pertenece a una invocación (desde EBP hacia abajo). |
| **Slot / ranura** | Un hueco de 4 bytes en el área de argumentos (`[esp+0]`, `[esp+4]`, …). |
| **Callee-saved** | Registros que la función llamada debe restaurar (EBP, EBX, ESI, EDI, ESP). |
| **Caller-saved** | Registros que el caller debe salvar si los necesita después (EAX, ECX, EDX). |
| **Prologue** | Código de entrada (`push ebp / mov ebp,esp / sub esp,N`). |
| **Epilogue** | Código de salida (`leave / ret`). |
| **Leave** | Instrucción única = `mov esp,ebp` + `pop ebp`. |
| **Doublef** | `double` de 64 bits (IEEE 754). En pila = dos dwords consecutivos. |

---

¿Dudas? Relee la sección 3 (prólogo) y la 5 (diagrama de `suma`). Es el corazón de todo.
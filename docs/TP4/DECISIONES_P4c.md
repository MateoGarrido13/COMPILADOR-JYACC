# Decisiones y cambios — Prompt 4c (TP4, funciones/métodos y convención de llamada, MASM32 32-bit)

## Resumen

Se implementó la traducción completa de invocaciones y definiciones de función/método en `GeneradorAssembler.java`. El backend ya no deja `FUNCION`, `METODO`, `PAR`, `CALL`, `CALLMET`, `ret`, `FIN_FUNCION` ni `FIN_METODO` como comentarios: emite un **stack frame con EBP**, argumentos en pila, retorno en `EAX` (`ulongint`) o `ST(0)` (`doublef`), llamadas directas y un argumento oculto `this` para métodos.

La convención elegida es **cdecl-like de 32 bits**: el **caller reserva y limpia** el área de argumentos. Se documenta completa abajo, con el diagrama del frame.

No se modificó la salida observable de TP3 (fuente numerado, tokens, estructuras, errores, TS, tercetos). El único cambio en `.asm` respecto de 4b es esperado: parámetros y variables locales dejaron de estar en `.DATA` y ahora se direccionan como `[ebp+…]` / `[ebp-…]`. Por eso se ajustó una verificación del script de 4b que esperaba el parámetro como símbolo de datos.

## Archivos modificados / creados

| Archivo | Cambio |
|---|---|
| `GeneradorAssembler.java` | Pre-scan de declaraciones y frames; `emitirPrologo`, `emitirFinFuncion`, `emitirRet`, `emitirPar`, `emitirLlamada`; resolución de parámetros/locales en `operandoAsm`; filtro de `.DATA`; placeholders `DD` para receptores de `CALLMET`; dispatch en `emitirCodigo()`. |
| `docs/TP4/PlanTP4.md` | Prompt 4c marcado como ejecutado. |
| `docs/TP4/TEMPLATES_ASM.md` | Templates T-18..T-23; filas de 4c removidas de "pendientes". |
| `test_tp_4/probar_prompt4c.sh` | Script de verificación del Prompt 4c (creado en este prompt). |
| `test_tp_4/Funciones y metodos/*.txt` | 8 casos manuales nuevos (creados en este prompt). |
| `test_tp_4/probar_prompt4b.sh` | Una expectativa actualizada: el parámetro de `eco` ahora se lee desde `[ebp+8]`. |

## Contrato IR consumido

| Terceto | Forma | Uso en ASM |
|---|---|---|
| `FUNCION` | `(FUNCION, nombreCorto, --)` | `fn_<clave>:` + prologue. |
| `METODO` | `(METODO, nombreCorto, --)` | `mt_<clave>:` + prologue (con `this`). |
| `PAR` | `(PAR, formal, real)` | Registra `formal` y `real` en orden IR; la escritura se materializa en el `CALL`. |
| `CALL` | `(CALL, claveFuncion, cant)` | Reserva `cant` ranuras, `call fn_<clave>`, limpieza, resultado a `@auxN`. |
| `CALLMET` | `(CALLMET, objeto, claveMetodo)` | Reserva ranura de `this` + formales, `call mt_<clave>`, limpieza. |
| `ret` | `(ret, valor, --)` | `mov eax, valor` o `fld valor`, luego `leave; ret`. |
| `FIN_FUNCION` / `FIN_METODO` | `(FIN_…, nombreCorto, --)` | `fin_<clave>:` + `leave; ret` de seguridad. |

Datos relevados del repo (sin cambiarlos):

```text
2. (FUNCION, foo, --)
3. (ret, a:main:foo, --)
4. (FIN_FUNCION, foo, --)
7. (PAR, b:main:foo, x:main)
8. (PAR, a:main:foo, z:main)
9. (CALL, foo:main, 2)

10. (METODO, m, --)                 ; m:main:cb y m:main:cc comparten nombre corto
11. (PAR, pa:main:cb:m, 1$ul)
12. (CALLMET, a1:main, m:main:cb)
```

La ambigüedad de nombres cortos (dos `m` en clases distintas) se resuelve con colas por nombre en orden de declaración: la TS/Entorno entrega las declaraciones en orden fuente y cada terceto `FUNCION`/`METODO`/`FIN_*` consume la suya. El `CALL`/`CALLMET` trae la clave completa y se resuelve directo desde `declaracionPorClave`.

## Convención de llamada (decisión central)

### 1. Caller limpia (cdecl-like), aunque el modelo diga `stdcall`

Se usa `.model flat, stdcall` por el encabezado heredado del Prompt 2, pero las funciones internas **no se declaran con `PROC`**. Se emiten etiquetas simples (`fn_foo_main:`) y `ret` sin inmediato; el caller hace `add esp, totalBytes` después del `call`.

Motivos:

- Es uniforme con `crt_printf` (varargs, limpieza por caller) y con la filmina `printf-variables.pdf`, que advierte que `printf` toca la pila y rompe el `RET` si no hay preservación con EBP.
- No depender de que MASM genere `ret n` por un `PROC STDCALL` evita desajustes entre cantidad declarada y cantidad realmente apilada.
- `totalBytes` se calcula por tipos reales (`ulongint` = 4, `doublef` = 8), no por `cant*4`. Para funciones con todos `ulongint`, coincide con `cant*4` como pide el enunciado.

### 2. Diagrama del stack frame

Funcion (`ulongint f(ulongint a, doublef b)`, con local `ulongint t` y local `doublef d`):

```text
  direcciones altas
  [ebp+16]  b (low dword)      ; doublef formal #2
  [ebp+12]  b (high dword)
  [ebp+ 8]  a                  ; primer formal (ulongint)
  [ebp+ 0]  EBP del caller     ; push ebp / mov ebp,esp
  [ebp- 4]  t                  ; local ulongint
  [ebp-12]  d (low dword)      ; local doublef, alineada a 8
  [ebp-16]  d (high dword)
  [ebp-16]  ESP despues de sub esp,16  (frame redondeado a 8)
  direcciones bajas
```

Metodo (`ulongint m(ulongint pa, ulongint pb)`):

```text
  [ebp+16]  pb                 ; segundo formal
  [ebp+12]  pa                 ; primer formal
  [ebp+ 8]  this (offset del receptor)
  [ebp+ 0]  EBP del caller
  [ebp-…]   locales
```

En el caller, justo antes del `call`:

```text
  [esp+0]    a / this
  [esp+4]    b / pa
  [esp+8]    …  / pb
  ESP -> primer byte del area reservation
```

`leave` equivale a `mov esp, ebp; pop ebp`, así que restaura tanto las locales como el EBP del caller sin importar cuánto haya avanzado `ESP` dentro del cuerpo (por ejemplo, por un `pout`).

### 3. Orden de evaluación vs. orden de formales

El IR emite los `PAR` en el orden pedido por `[…]` (Tema 19). Ejemplo real:

```text
(PAR, b:main:elegir, 1$ul)     ; se evalúa primero
(PAR, a:main:elegir, 2$ul)
(CALL, elegir:main, 2)
```

Si se hiciera `push` directo, `b` quedaría en `[esp+0]` y el callee leería `a` desde `[ebp+8]`: **incorrecto**. Decisión:

- El `CALL` reserva todas las ranuras de una vez (`sub esp, totalBytes`).
- Cada `PAR` escribe su valor en la ranura de su **formal declarado**, no en la siguiente ranura libre.
- El orden de emisión de las instrucciones de carga sigue siendo el orden IR (orden de evaluación). En el ejemplo: `c_1` → `[esp+4]` (b) y después `c_2` → `[esp+0]` (a).

Esto mantiene `ebp+8` = primer formal para cualquier call site, sin swaps de pila ni registros temporales extra. Los valores ya vienen materializados en el IR (`@auxN`, variables, constantes), así que diferir la escritura hasta el `CALL` no pierde efecto de lado: la pila no es observable entre `PAR` y `CALL`.

`PAR` deja un comentario en su posición original para trazabilidad:

```asm
    ; (PAR, b:main:elegir, 1$ul)
```

### 4. Parámetros `doublef`

`push` de 64 bits no existe en 32-bit MASM, y además necesitamos ranuras fijas. Se copia el qword como dos dwords, **high en la dirección alta**:

```asm
mov eax, dword ptr [a_main+4]
mov dword ptr [esp+4], eax
mov eax, dword ptr [a_main]
mov dword ptr [esp+0], eax
```

Dentro del callee se usa `fld qword ptr [ebp+8]` / `fmul qword ptr [ebp+16]`. `totalBytes` cuenta 8 por parámetro `doublef`; el `add esp` del caller usa ese total.

### 5. Retorno

| Tipo | En el callee | En el caller |
|---|---|---|
| `ulongint` | `mov eax, valor` | `mov @auxN, eax` |
| `doublef` | `fld valor` (deja ST(0)) | `fstp @auxN` |

El tipo se toma de `FuncionDeclarada.tipoRetorno` y se confirma con `ResultadoTerceto.getTipo()`. Como `ret` ya emite `leave; ret`, el `FIN_FUNCION` agrega una segunda copia inalcanzable del epilogue detrás de `fin_<clave>:` para caminos sin `ret` explícito.

### 6. Llamadas a métodos y `this`

- **Despacho directo**, sin vtable. El IR ya resolvió la herencia y el prefijo (`m:main:padre`). Se emite `call mt_m_main_padre:`.
- `this` es un argumento oculto de 4 bytes en `[esp]` del caller y `[ebp+8]` del callee. Los formales empiezan en `[esp+4]` / `[ebp+12]`.
- El receptor se pasa como **offset**: `mov eax, offset c_main; mov dword ptr [esp], eax`. Si el receptor es un parámetro/`this` del frame, se usa `mov eax, dword ptr [ebp+…]` (el slot ya guarda un offset). Si es un objeto local `[ebp-…]`, se usa `lea eax, [ebp-…]` mientras 4d no defina su layout.
- Hasta el Prompt 4d no existe layout de objetos: `colectarSimbolosData` no emite variables de clase. Se agrega un placeholder `c_main DD ? ; this (layout en 4d)` solo para los objetos que aparecen como receptores de `CALLMET`. Esa es la única concesión de 4c con objetos.

### 7. Parámetros y locales fuera de `.DATA`

`prepararStackFrames()` recorre las declaraciones y arma `slotStackPorSimbolo`:

- Formales: `dword ptr [ebp+8]`, `qword ptr [ebp+16]`, … según tipo y orden de declaración (base 12 en métodos).
- Locales: EntradaTabla con `uso == VARIABLE` y `ambito == funcion.ambitoPropio`; se asignan `[ebp-4]`, `[ebp-16]`, … con alineación de 8 para `doublef`.
- `emitirData()` omite esos símbolos cuando vienen como categoría `dato`.
- `operandoAsm()` los resuelve a su desplazamiento **solo mientras `funcionActual != null`**. En `main` o en otra función, el mismo `EntradaTabla` sigue resolviendo a su nombre de `.DATA`.

La TS y la salida por consola no se tocaron: el filtro es exclusivo del backend ASM.

### 8. Nombres de etiquetas

`fn_` para funciones y `mt_` para métodos, seguido de la clave sanitizada (`foo:main` → `fn_foo_main`, `m:main:padre` → `mt_m_main_padre`). `FIN_*` usa `fin_` + el mismo sufijo (`fin_foo_main`). El sanitizador reemplaza `:`/`.`/`$` por `_`, colapsa guiones bajos y evita que empiece con dígito. Se eligió la clave completa en vez del nombre corto para no colisionar entre clases.

## Correlación con los prompts 4d/4e y 5/6

| Prompt | Punto de contacto | Preparación dejada |
|---|---|---|
| **4d** (objetos) | `this` hoy es un `DD` placeholder y se pasa con `offset`. | 4d debe reemplazarlo por el layout real (`STRUCT`/slots) y ajustar `operandoAsm` de `ReferenciaAtributo`. Las etiquetas `mt_*` ya son estables. |
| **4e** (integración) | `emitirCodigo()` ya despacha todo salvo `CLASE`, `FIN_CLASE` y `COPY`. | Casos de if/repeat con llamadas y `pout` dentro de funciones ya funcionan juntos. |
| **5** (runtime) | Guardas `b/e/f/h` deben insertarse antes de `div`/`fmul`/finito y en asignaciones a enum. | El frame con EBP preserva el `RET` aunque las guardas impriman mensajes con `crt_printf`. |
| **6** (contrato final) | Revisar sintaxis MASM real en Windows y CRLF de los `fmt_*`. | Ver riesgos abajo. |

## Verificación realizada

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/tp4p4c_build *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Script nuevo:

```bash
bash test_tp_4/probar_prompt4c.sh   # OK (incluye regresiones 4b, 4a, 3 y 2)
```

Casos manuales (todos compilan y generan `.asm` en `test_tp_4/Funciones y metodos/`):

| Caso | Qué valida | Resultado observado |
|---|---|---|
| `01_funcion_ulongint.txt` | 2 formales, suma, retorno EAX | `fn_suma_main:`, `[ebp+8]`, `[ebp+12]`, `sub esp,8`, `add esp,8`, `mov @aux10,eax` |
| `02_funcion_doublef.txt` | 2 qwords, retorno ST(0) | `fld qword ptr [ebp+8]`, `fmul qword ptr [ebp+16]`, `sub esp,16`, `fstp @aux10` |
| `03_orden_evaluacion_PAR.txt` | `[2,1]` con formales nombrados | PAR b primero → `[esp+4]`; PAR a después → `[esp+0]`; `ret` lee `[ebp+8]` |
| `04_llamada_anidada.txt` | limpieza del call interno | `call fn_doble_main` / `add esp,4` / `@aux11` usado por el PAR externo |
| `05_metodo_con_this.txt` | receptor + formales | `mt_m_main_calculadora:`, `this` en `[esp]`, formales en `[ebp+12]/[ebp+16]`, `call mt_…`, `add esp,12` |
| `06_variables_locales_stack.txt` | locales en frame | `sub esp,16`, `[ebp-4]`, `fstp qword ptr [ebp-16]`, ausentes de `.DATA` |
| `07_pout_dentro_de_funcion.txt` | printf con EBP | `fn_eco_main:` antes de los `invoke`, parámetro vía `[ebp+8]` |
| `08_metodo_heredado.txt` | resolución estática | `call mt_m_main_padre`, `offset h_main`, `add esp,12` |

Regresiones:

```bash
bash test_tp_4/probar_prompt4b.sh   # OK (una expectativa ajustada: [ebp+8])
bash test_tp_4/probar_prompt4a.sh   # OK
bash test_tp_4/probar_prompt3.sh    # OK
bash test_tp_4/probar_prompt2.sh    # OK
```

## Casos cubiertos / no cubiertos

**Cubiertos**: prologue/epilogue, `leave; ret`, caller cleanup, retorno en EAX y en ST(0), parámetros de 4 y 8 bytes, orden de evaluación distinto del orden de formales, llamada anidada, método con `this`, herencia resuelta estáticamente, locales en stack, `pout` dentro de función, nombres cortos duplicados en clases distintas.

**No cubiertos (por diseño)**: vtable/despacho dinámico (el lenguaje no lo pide; el IR trae el método resuelto), layout real de objetos y `ReferenciaAtributo` (4d), `COPY` (4d), guards runtime (5), formato final del `.asm` y ensamblado en Windows (6), recursión profunda con temporales globales `@auxN` (ver riesgo 3).

## Riesgos abiertos

1. **MASM real no verificado**: este entorno solo valida texto `.asm` y compilación Java. Revisar en Windows `mov eax, dword ptr [a_main+4]`, `lea eax, [ebp-4]`, `fld qword ptr [ebp+8]` e `INVOKE crt_printf, …, qword ptr [ebp+8]`.
2. **Convención mixta**: `.model flat, stdcall` con limpieza estilo cdecl es válida mientras las funciones internas no se declaren `PROC`. Si en el futuro se usa `PROTO`, hay que declararlas `C` o pasar a `ret n`.
3. **Recursión y `@auxN` globales**: los temporales siguen siendo símbolos de `.DATA` compartidos. Casos simples de recursión pueden funcionar porque el valor se consume apenas vuelve el call, pero una expresión que necesite dos temporales vivos de distintos niveles se sobrescribiría. Pendiente de 4e/6.
4. **`this` como `DD`**: si un método se invoca sobre un objeto local (no global), el placeholder no se emite porque `operandoAsm` devuelve `[ebp-…]`; se usa `lea` sobre ese slot. 4d debe asegurar que el slot tenga el layout completo y decidir si el receptor se pasa por valor o por dirección de forma uniforme.
5. **Alineación del frame**: se redondea a 8 bytes. Suficiente para `doublef`; si se agregan tipos con requisitos mayores, revisar `prepararStackFrames()`.

# Decisiones y cambios — Prompt 4b (TP4, salida por consola `pout`, MASM32 32-bit)

## Resumen

Se implementó la traducción del terceto `pout` en `GeneradorAssembler.java`. El backend ya no deja `pout` como comentario: emite `INVOKE crt_printf` con un formato elegido según el **tipo del operando** (`ulongint`, `doublef` o `cadena`) y declara ese formato en `.DATA` solo cuando se usa.

Se respetó el contrato IR existente: `pout` es no productor, con el valor a imprimir en `op1` (`(pout, valor, --)`). No se modificó la salida por consola de TP3 (fuente numerado, tokens, estructuras, errores, TS, tercetos).

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `GeneradorAssembler.java` | `emitirPout(Object)`, `calcularFormatosPout()`,constantes de formatos y flags `usaFmt*`; `.DATA` con `fmt_*`; despacho de `pout` en `emitirCodigo()`. |
| `docs/TP4/PlanTP4.md` | Prompt 4b marcado como ejecutado. |
| `docs/TP4/TEMPLATES_ASM.md` | Templates T-14..T-17 de `pout`; fila de `pout` removida de "pendientes". |
| `test_tp_4/probar_prompt4b.sh` | Script de verificación del Prompt 4b (creado en este prompt). |
| `test_tp_4/Salida por consola/*.txt` | 11 casos de prueba manuales (creados en este prompt). |

## Contrato IR consumido

| Terceto | Forma | Resolución en ASM |
|---|---|---|
| `pout` | `(pout, op, --)`, no productor | `op` puede ser `EntradaTabla` (variable, constante o cadena), `Integer` (ref `[n]` → `@auxN`) o `ReferenciaAtributo` (preparado en 4a). |

Ejemplos reales relevados del repositorio de tests:

```text
(pout, hola, --)        ; cadena -> EntradaTabla, uso CADENA, tipoDato = cadena
(pout, 5$ul, --)        ; constante -> EntradaTabla, uso CONSTANTE
(pout, 1.5, --)         ; constante doublef
(pout, x:main, --)      ; variable
(pout, [9], --)         ; resultado de expresión (auxiliar @aux9)
(pout, [7], --)         ; resultado de CALL (auxiliar @aux7, se llena en 4c)
(pout, a1.x, --)        ; ReferenciaAtributo -> nombre ASM del atributo materializado
```

## Convención de formatos y buffers

### Formatos emitidos en `.DATA`

Se declaran **solo los formatos que realmente usa el programa** (`calcularFormatosPout()` recorre los tercetos antes de emitir `.DATA`):

| Símbolo | Definición | Cuándo se emite |
|---|---|---|
| `fmt_ulongint` | `DB "%u",0` | existe algún `pout` de tipo `ulongint` |
| `fmt_doublef` | `DB "%f",0` | existe algún `pout` de tipo `doublef` |
| `fmt_cadena` | `DB "%s",0` | existe algún `pout` de tipo `cadena` |

Nombres fijos, en minúsculas, `DB` terminado en 0. No colisionan con los nombres generados por `TablaSimbolos` (`v_`, `c_`, prefijos de cadena) ni con `@auxN`/`@fpu_status`.

**Decisión**: no se agregó `13,10` (CRLF) a los formatos. El enunciado del prompt pide literalmente `fmt_ulongint db "%u",0`, `fmt_doublef db "%f",0`, `fmt_cadena db "%s",0`, y se respetó esa convención. La filmina `printf-variables.pdf` usa `"%u\n"`; si se quiere salida con salto de línea, alcanza con cambiar esas tres cadenas (o agregar un `fmt_nl` e invocarlo después). Queda registrado como ajuste menor para el Prompt 6.

### Buffers temporales

**No se emiten buffers temporales.** Motivo:

- `ulongint`: el operando ya es un dword en memoria (`v_x`, `c_5_ul_ul`, `@aux9`); `crt_printf` lo recibe por valor en la pila.
- `doublef`: el operando ya es un qword (`DQ`) en memoria. `INVOKE` con un símbolo `DQ` pasa 8 bytes, que es exactamente lo que `%f` espera. No hace falta `FLD`/`FSTP` ni convertir a `REAL10`.
- `cadena`: el símbolo ya es `DB "…",0`; se pasa `offset`.

Esto evita copias innecesarias y mantiene la pila FPU balanceada (no importa si el mismo programa tiene comparadores 4a o aritmética 3).

## Decisiones

### 1. `INVOKE crt_printf` (no `StdOut`, no `printf` crudo)

El prompt permite `invoke StdOut`/`printf` compatible con `masm32rt.inc`. Se eligió `crt_printf` porque:

- es el nombre que expone `masm32rt.inc` para el CRT en los ejemplos de la cátedra/MASM32;
- mantiene **una sola forma** para los tres tipos, con formato explícito;
- `StdOut` queda como alternativa válida para cadenas puras (filmina: *"Para imprimir string, pueden usar directamente el printf o el stdout"*), pero usarla aquí haría dos caminos distintos y no cubriría números.

Se mantuvo el `include \masm32\include\masm32rt.inc` del Prompt 2 sin agregar `PROTO`s propios, para no redeclarar símbolos que ya vienen en el include.

### 2. El tipo se resuelve con el mismo helper del Prompt 4a

`emitirPout` y `calcularFormatosPout` usan `tipoDeOperando(Object)`, ya implementado para comparaciones:

- `Integer` → `resultados.get(n).getTipo()` (por eso un `pout` de `[n]` hereda el tipo de la expresión, de un `todf` o del retorno de un `CALL`).
- `EntradaTabla` → `entorno.tipoEfectivo(tipoDato)` (cadenas quedan como `cadena`; enums ya resueltos a `ulongint`).
- `ReferenciaAtributo` → `entorno.tipoEfectivo(getTipo())`.

Elegir el formato por el **operando** y no por el texto evita confundir `"hola"` (cadena) con una constante numérica y hace que el soporte de enums (Tema 23) y atributos (Tema 28) llegue gratis.

### 3. Cadenas: `offset` del símbolo DB

`operandoAsm` devuelve el nombre ASM de la TS (`hola`, `cadena_…`). Para `pout` de cadena se prepende `offset`:

```asm
invoke crt_printf, offset fmt_cadena, offset hola
```

Si se pasara `hola` sin `offset`, `crt_printf` recibiría los primeros 4 bytes del contenido como puntero. Los números, en cambio, **no** llevan `offset` porque se pasan por valor.

### 4. `doublef` sin FPU en el `pout`

La filmina advierte que los float deben declararse a 64 bits (`DQ`) para `printf`. Como `TablaSimbolos` y `ResultadoTerceto.tipoAsmDato()` ya emiten `DQ`, no se necesita convertir a 80 bits ni usar la pila 80x87 en la impresión. La filmina usa `%.20Lf`; se eligió `%f` porque el prompt lo pide explícitamente y porque el dato es `double` de 64 bits, no `long double` de 80. Ver riesgo 1.

### 5. Formato condicional = cero ruido en programas sin `pout`

Igual que `@fpu_status` en 4a, los `fmt_*` se calculan antes de `emitirData()`. Un programa solo aritmético no ve cambios en su `.DATA`. Esto facilita las regresiones de los Prompts 2 y 3.

### 6. `ReferenciaAtributo`: reutiliza el mapeo de 4a

`operandoAsm` ya devuelve `tablaSimbolos.nombreAsmDe(ref.atributo)` desde el Prompt 4a (antes devolvía `obj.atr`, inválido en MASM). `pout(a1.x)` genera por ejemplo:

```asm
invoke crt_printf, offset fmt_ulongint, x_main_hija
```

No se implementó layout de objetos (4d); se imprime el atributo materializado que ya existe en la TS.

## Correlación con los prompts 4c/4d/4e y 5/6

| Prompt | Punto de contacto | Preparación dejada |
|---|---|---|
| **4c** (funciones) | `pout(foo(x)[1$ul])` → `(pout, [7], --)` donde `[7]` es `CALL`. | `tipoDeOperando` ya lee el `tipoRet` del `ResultadoTerceto` del `CALL`, así que se emite `fmt_doublef`/`fmt_ulongint` correcto y `invoke … , @aux7`. Falta que 4c llene `@aux7` con el retorno. |
| **4c** (pila) | La filmina `printf-variables` advierte que `printf` toca el stack y rompe `RET` dentro de funciones sin prefijo/sufijo. | Los casos de `pout` dentro de función se entregan con esa nota; el prologue `push ebp / mov ebp,esp / … / pop ebp / ret` del 4c resuelve el problema. |
| **4d** (objetos) | `pout(a1.x)`. | Tipo y nombre ASM del atributo ya resueltos; el layout físico se define en 4d. |
| **4e** (integración) | `emitirCodigo()` ya despacha `pout` además de comparadores/BF/BI. | Quedan como comentario `PAR`, `CALL`, `CALLMET`, `ret`, `FUNCION`, `METODO`, `COPY`, `CLASE`. |
| **5** (runtime b,e,f,h) | Los mensajes de error necesitarán imprimir cadenas. | Reutilizables: `fmt_cadena` + `INVOKE crt_printf, offset fmt_cadena, offset msg_*` (o `StdOut`). |
| **6** (contrato de salida) | Formato final del `.asm` y verificación en Windows. | Ajuste pendiente: CRLF en los formatos si la cátedra lo exige. |

## Verificación realizada

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/tp4p4b_build *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Casos ejecutados (todos compilan y generan `.asm`):

| Caso | Qué valida | Resultado observado |
|---|---|---|
| `/tmp/prog_p4b_scratch.txt` (barrido de tipos) | cadena, constante ulongint/doublef, variable, expresión, `todf` | 3 formatos en `.DATA`; 7 `invoke crt_printf` con `offset`/valor correctos |
| `test/TP3_SEMANTICAS/prompt-2b-codegen-base/llamada_en_pout.txt` | `pout` de resultado de `CALL` | `invoke crt_printf, offset fmt_ulongint, @aux7` |
| `test/tp2/verificacion_r28_r31_atributos/miembro_propio_con_herencia.txt` | `pout` de atributo | `invoke crt_printf, offset fmt_ulongint, x_main_hija` |
| `test_tp_4/Salida por consola/*.txt` | 11 casos manuales nuevos | Ver `test_tp_4/probar_prompt4b.sh` |

Regresión:

```bash
bash test_tp_4/probar_prompt4a.sh   # OK
bash test_tp_4/probar_prompt3.sh    # OK
bash test_tp_4/probar_prompt2.sh    # OK
```

La salida por consola (fuente numerado, tokens, estructuras, errores, TS, tercetos) no se modificó.

## Casos cubiertos / no cubiertos

**Cubiertos**: `pout` de variable `ulongint`/`doublef`, de constante de ambos tipos, de cadena literal (incluida repetida), de expresión aritmética, de `todf`, de resultado de función (emisión del formato y del `@auxN`), de atributo materializado, dentro de `if/else` y `repeat`, y mezclas de todos los tipos en un mismo programa.

**No cubiertos (por diseño)**: ejecución real del retorno de funciones (4c), layout de objetos (4d), prefijo/sufijo de pila para `printf` dentro de funciones (4c), mensajes de error runtime (5), salto de línea en la salida (6). Tampoco se verificó el ensamblado con MASM32 en Windows: este entorno solo valida el texto `.asm` y la compilación Java.

## Riesgos abiertos

1. `%f` vs `%.20Lf`: se sigue el prompt (`%f`). Si `crt_printf` de MASM32 interpreta `%Lf` como `double` (no `long double`), la filmina y esta implementación difieren solo en precisión; verificar en Windows.
2. `INVOKE` con un operando `DQ` en varargs: válido en los ejemplos de la filmina (`printf, …, floati` con `floati DQ`); confirmar con la versión de `masm32rt.inc` del aula.
3. `pout` dentro de funciones sin prologue/sufijo puede corromper el `RET` (advertencia de la filmina). Resuelto en 4c, no aquí.
4. `pout` de resultado de `CALL` imprime un `@auxN` que hoy no se inicializa; es una limitación conciente hasta 4c, no un error de formato.
5. Sin CRLF, varias impresiones consecutivas salen pegadas. Decisión registrada arriba; ajuste trivial en 6.

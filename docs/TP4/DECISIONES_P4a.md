# Decisiones y cambios — Prompt 4a (TP4, control de flujo MASM32 32-bit)

## Resumen

Se completó la traducción del **Tema 13** (sentencias de control) en `GeneradorAssembler.java`: comparadores, etiquetas `label_N`, `BF` y `BI`. El backend pasó de emitir estos tercetos como comentario a generar `CMP`/`SETcc`/`Jcc`/`JMP` y marcas de etiqueta reales.

El contrato IR no se modificó: se consumen los tercetos tal como los produce hoy `GeneradorTercetos` (`(<, izq, der)` con resultado booleano, `(BF, [n], label)`, `(BI, label, --)`, `(label_N, --, --)`). No se tocó la salida por consola de TP3.

## Archivos modificados

| Archivo | Cambio |
|---|---|
| `GeneradorAssembler.java` | Nuevos emisores `emitirComparacion`, `emitirBF`, `emitirBI`, `emitirEtiqueta`; helpers de tipo y de mapeo `SETcc`/`Jcc`; `@fpu_status` condicional en `.DATA`; `operandoAsm` resuelve `ReferenciaAtributo` a nombre ASM de la TS. |
| `docs/TP4/PlanTP4.md` | Prompt 4a marcado como ejecutado. |
| `docs/TP4/TEMPLATES_ASM.md` | Archivo vivo de templates ASM (creado en este prompt). |
| `test_tp_4/probar_prompt4a.sh` | Script de verificación del Prompt 4a (creado en este prompt). |

## Contrato IR consumido

| Terceto | Forma | Uso en ASM |
|---|---|---|
| Comparador | `(op, izq, der)` con `op ∈ {<, >, <=, >=, ==, !=}` | `izq`/`der` = `EntradaTabla` o `Integer` (ref `@auxN`) o `ReferenciaAtributo`. Produce `@auxN` **DD** con 0/1. |
| `BF` | `(BF, cond, destino)`; `cond` = `Integer` ref al comparador (o valor booleano); `destino` = `String label_N`, completado después de cerrar el `then`/`while` | Salto inverso del comparador sobre los operandos originales. |
| `BI` | `(BI, destino, --)`; `destino` = `String label_N` en `op1` | `jmp destino`. |
| Etiqueta | `(label_N, --, --)` | `label_N:` en `.CODE`. |

`Terceto.destinoSalto()` ya documenta esta convención (BF en `op2`, BI en `op1`), y se respetó sin cambios.

## Decisiones

### 1. El comparador materializa 0/1 en su `@auxN`

Aunque el IR registra el resultado del comparador con `ResultadoTerceto.tipo = ulongint`, antes del Prompt 4a ese `@auxN` nunca se escribía. Ahora se emite:

```asm
mov eax, <izq>
cmp eax, <der>
set<cc> al
movzx eax, al
mov @auxN, eax
```

`SETcc` (386+) es válido porque el encabezado ya declara `.386`. Se usa `MOVZX` para evitar depender del valor previo de los bytes altos de `EAX`.

**Motivo**: el contrato del IR (`IR_CONTRATO.md` §3.3, `DECISIONES_P1.md`) dice que el comparador *produce valor* y ese valor puede referenciarse por `[n]`. Dejar el auxiliar sin inicializar sería incorrecto para cualquier uso futuro (por ejemplo, una comparación dentro de una expresión).

### 2. `BF` sobre un comparador = salto inverso (patrón de la catedra)

La filmina `10.2.Generación de Código...` muestra `(BF, [12], 17)` → `JLE Label17` para un `>`: **BF emite el `Jcc` inverso del comparador**, no prueba un booleano. Se adoptó ese patrón:

- `emitirBF` detecta que `cond` es un `Integer` que apunta a un terceto comparador.
- Toma los operandos originales de ese comparador, rehace `CMP` (o la secuencia FPU) y salta con el `Jcc` inverso.

| Comparador IR | `SETcc` (aux = 1 si cierto) | `Jcc` de BF (salta si **falso**) |
|---|---|---|
| `<`  | `setb`  (CF=1)          | `jae` (CF=0)              |
| `<=` | `setbe` (CF=1 o ZF=1)   | `ja`  (CF=0 y ZF=0)       |
| `>`  | `seta`  (CF=0 y ZF=0)   | `jbe` (CF=1 o ZF=1)       |
| `>=` | `setae` (CF=0)          | `jb`  (CF=1)              |
| `==` | `sete`  (ZF=1)          | `jne` (ZF=0)              |
| `!=` | `setne` (ZF=0)          | `je`  (ZF=1)              |

Los mnemónicos sin signo (`ja/jae/jb/jbe`) son los correctos para `ulongint` y también para la FPU después de `FSTSW`/`SAHF` (filmina 11.1: *"Efectuar la bifurcación condicional, usando los códigos de bifurcación para datos sin signo"*).

### 3. `BF` sobre un booleano materializado = `CMP aux,0 / JE`

Si `cond` **no** referencia a un comparador (por ejemplo, una variable/auxiliar que ya contiene 0/1), se usa la otra forma pedida en el plan:

```asm
cmp <aux>, 0
je  label_N
```

Esto cubre el caso "BF salta si falso = 0" y deja la puerta abierta a condiciones booleanas producidas por otros tercetos en prompts posteriores.

### 4. Comparaciones `doublef`: `FLD` / `FCOMP` / `FSTSW` / `MOV AX` / `SAHF`

Secuencia literal de la filmina 11.1:

```asm
fld   <izq>
fcomp <der>
fstsw @fpu_status
mov   ax, @fpu_status
sahf
set<cc> al        ; o jcc para BF
```

- Se usa **`FCOMP`** (compara y hace pop) en lugar de `FCOM` + `FSTP`: deja la pila 80x87 balanceada, algo crítico porque `BF` rehace la comparación y porque los prompts 4b/4c usarán la FPU para `pout` y retornos `doublef`.
- `FSTSW` va a memoria (`@fpu_status DW ?`), como en la filmina, y luego se copia a `AX` para `SAHF`. `SAHF` coloca `ZF`/`CF` en los flags del procesador, habilitando `SETcc`/`Jcc`.
- `@fpu_status` se emite en `.DATA` **solo si** algún comparador o `BF` compara `doublef` (`calcularUsoFpuStatus()`), para no contaminar programas que no usan FPU en condiciones.

### 5. El tipo de la comparación se toma de los operandos, no del `ResultadoTerceto`

`emitirComparacion` de `GeneradorTercetos` fija `Tipos.ULONGINT` como tipo del comparador (porque el resultado es un booleano). Por eso `emitirComparacion`/`emitirBF` consultan `tipoDeOperando()`:

- `Integer` → `resultados.get(n).getTipo()` (ej. `@aux3` de un `todf` es `doublef`).
- `EntradaTabla` → `entorno.tipoEfectivo(tipoDato)` (enums ya resueltos a su base).
- `ReferenciaAtributo` → `entorno.tipoEfectivo(getTipo())`.
- `String` → `Tipos.tipoDeConstante(...)` (fallback).

Esto respeta el requisito "respetar tipo de la comparación" y hace que la implementación ya funcione para comparaciones con atributos (Tema 28) y enums (Tema 23) sin trabajo extra en 4d/5.

### 6. `operandoAsm` de `ReferenciaAtributo` apunta al símbolo materializado

Antes devolvía `ref.texto` (`obj.atr`), que no es identificador MASM válido. Ahora devuelve `tablaSimbolos.nombreAsmDe(ref.atributo)` cuando el atributo está disponible. Es un cambio *preparatorio* para el Prompt 4d: no implementa layout de objetos, solo corrige la referencia para que una comparación/aritmética sobre `a1.x` no genere un símbolo inválido.

### 7. redundancia consciente comparador + BF

En el IR actual todo comparador precede a su `BF`, por lo que el código generado calcula dos veces la comparación: una para llenar `@auxN` y otra para decidir el salto. Se mantuvo así deliberadamente:

- respeta el contrato IR (el `@auxN` existe y debe tener valor);
- sigue el patrón de la catedra para `BF` (salto inverso directo);
- evita un análisis de usos frágil para detectar cuándo el auxiliar es prescindible.

El costo es pequeño (unas pocas instrucciones por `if`/`while`) y no hay riesgo semántico: ambas comparaciones usan los mismos operandos y la segunda se ejecuta inmediatamente antes del salto.

## Correlación con los prompts 4b/4c/4d/4e y 5

| Prompt | Punto de contacto | Cómo queda preparado |
|---|---|---|
| **4b** (`pout`) | `EAX`/`AX` son scratch usados por comparaciones y `BF`; `invoke crt_printf` también usa `EAX`. | `BF` rehace `CMP` justo antes del `Jcc`, así que no depende de flags dejados por un `invoke` previo. `pout` deberá recargar `EAX` (ya lo hace en P3 para asignaciones). |
| **4c** (funciones) | Los `label_N` conviven con `fn_*`/`mt_*` en el mismo `.CODE`; los saltos son locales al procedimiento. | No hay colisión de nombres. Los `Jcc`/`JMP` no cambian `EBP`/`ESP`; los prologues/epilogues del 4c se insertan alrededor de este código sin conflictos. |
| **4d** (objetos) | Comparaciones sobre `ReferenciaAtributo`. | `tipoDeOperando()` ya devuelve el tipo efectivo del atributo y `operandoAsm()` ya resuelve su nombre ASM; el layout físico se define en 4d. |
| **4e** (integración) | `emitirCodigo()` ya no deja `BF`/`BI`/labels/comparadores como comentario. | Falta solo `pout`, llamadas, `COPY` y marcadores de clase. |
| **5** (runtime b,e,f,h) | Las guardas se insertarán en `+`, `*`, `/`, `todf` y `:=` a enum. | `BF` saltará a etiquetas de error con el mismo mecanismo `Jcc`/`JMP`; `@fpu_status` ya existe como plantilla de lectura del estado FPU (`FXAM`/`FSTSW` para el chequeo `f`). |

## Verificación realizada

Compilación:

```bash
javac -encoding UTF-8 -d /tmp/tp4p4a_build *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Casos ejecutados (todos sin errores de compilación):

| Caso | Qué valida | Resultado observado en `.asm` |
|---|---|---|
| `/tmp/prog_p4a_if.txt` (if/else `ulongint`) | `seta`, `jbe label_1`, `jmp label_2`, `label_1:`, `label_2:` | OK |
| `test/TP3_y_TP4/SELECCION/08_comparadores.txt` | Los 6 comparadores y sus 6 `Jcc` inversos | OK (`jbe`, `jae`, `jb`, `ja`, `jne`, `je`) |
| `test/TP3_y_TP4/ITERACION/04_repeat_anidado.txt` | Labels de inicio/salida anidados + `jmp` de retorno | OK (`label_1`..`label_4`) |
| `test/tp2/verificacion_r33_conversion/en_condicion_if.txt` | `todf(x) > 0.5`: rama FPU, `@fpu_status DW ?` | OK (`fld`/`fcomp`/`fstsw`/`mov ax`/`sahf` + `jbe`) |

Regresión:

```bash
bash test_tp_4/probar_prompt3.sh   # 18/18 verificaciones OK
bash test_tp_4/probar_prompt2.sh   # OK
```

La salida por consola (fuente numerado, tokens, estructuras, errores, TS, tercetos) no se modificó.

## Casos cubiertos / no cubiertos

**Cubiertos**: los seis comparadores sobre `ulongint` y `doublef`; `IF`/`ELSE`/`END_IF`; `REPEAT ... WHILE` simple y anidado; condiciones con expresiones (`@auxN`); condiciones con `todf`; `BF` con destino diferido y con destino directo; `BI` en cierre de `then` y en `repeat`.

**No cubiertos (por diseño, prompts posteriores)**: `pout` (4b), invoked/retornos (4c), `COPY` y direccionamiento de atributos (4d), mensajes y guardas de errores runtime `b/e/f/h` (5), formato final de salida del compilador (6). Tampoco se instrumentó recursión (chequeo `i`, no asignado al grupo 43).

## Riesgos abiertos

1. `SETcc`/`MOVZX` requieren 386+; ya declarado, pero conviene confirmar con MASM32 real en Windows (este entorno solo valida el texto `.asm`).
2. `FCOMP` con operando `doublef` en memoria es válido en MASM; se usa `DQ` de 64 bits (no `REAL8` explícito). Verificar ensamblado con `masm32rt.inc`.
3. La duplicación de la comparación (decisión 7) puede optimizarse en 4e si se decide analizar usos del `@auxN`.
4. En condiciones con `ReferenciaAtributo`, el nombre ASM resuelto depende de que el atributo esté materializado en la TS; los casos actuales de TP3 lo materializan al declarar el objeto.

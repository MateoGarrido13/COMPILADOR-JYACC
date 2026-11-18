# Estado general — Chequeos en tiempo de ejecución (Prompt 5, Grupo 43)

**Repositorio**: `/home/rafael/Documents/Compiladores/COMPILADOR/TP3`
**Rama**: `tp4alt` · **HEAD auditado originalmente**: `b9250e0` (`Plan 6`) · **HEAD actual**: `909f197` (correcciones B1–B5 + extensión `doublef`, commits `6b1b1ff` y `909f197`) · **Fecha**: 2026-11-18
**Scope**: práctico TP4 y sus filminas de generación de salida únicamente (`ASSETS/TPs`, `ASSETS/FilminasSalida`).

---

## 1. Fuentes de verdad y asignación de letras

`ASSETS/TPs/Tp04-2026-1.pdf` (4 páginas, extraído con `pdftotext -layout`) es la autoridad. La tabla «GRUPO / CHEQUEOS» indica:

> **Grupo 43 → `befh`**

Definiciones literales del PDF y su correlación con el código:

| Letra | Chequeo según PDF | ¿Asignado? | Interpretación usada en el plan | Estado |
|------:|-------------------|:----------:|---------------------------------|--------|
| a | División por cero (enteros y punto flotante) | NO | — | **No implementado ni requerido** |
| b | Overflow en sumas de enteros | SÍ | overflow de suma `ulongint` | Implementado + corregido (B1) |
| c | Overflow en sumas de punto flotante | NO | — | No requerido |
| d | Overflow en productos de enteros | NO | — | No requerido |
| e | Overflow en productos de punto flotante | SÍ | overflow de producto `doublef` | Implementado + corregido (B1, B2) |
| f | Resultados negativos en restas de enteros sin signo | SÍ | resta `ulongint` | Implementado + corregido (B1) |
| g | Pérdida de información en conversión float→entero | NO | — | No requerido |
| h | Asignación inválida a variables de tipo enumeración | SÍ | valor fuera del enum | Implementado + corregido (B1, B3, B4 parcial, B5) y **extendido a enums `doublef`** |
| i | Recursión en invocaciones de funciones | NO | — | No requerido |

**Extensión `doublef` (ejecutada a pedido del usuario, posterior a la auditoría original)**: `tipoEnumeradoChequeable()` acepta ahora bases `ulongint` y `doublef`; `emitirChequeoEnum()` ramifica la comparación (entera vía `CMP eax`, flotante vía `FLD`/`FCOMP`/`FSTSW`/`SAHF`). Ver §4.5 y §5 (B5).

**Conclusión importante**: la premisa «b = división por cero doublef» es incorrecta. La división por cero es la letra **a**, ajena al grupo 43. El PDF también exige que, al producirse la condición de error, el programa **emita un mensaje y finalice la ejecución**; además exige mensajes claros y la salida estándar TP4 (errores + IR + TS + `.asm`, sin tokens/estructuras salvo reentrega).

De `ASSETS/FilminasSalida/` se extrajeron los patrones relevantes:

- `winasmtut.pdf`: bloque de error con impresión y salto `jmp _quit` **antes** de las etiquetas de error. Es el patrón que faltaba (B1).
- `temas 10.2 / 11.1`: comparación FPU con `FSTSW` + `SAHF` + `Jcc` sin signo; `C0` (bit 8 del status word) queda en `CF`.
- `printf-variables`: `INVOKE crt_printf` con `offset fmt_*` y el valor por pila. No aparece `crt_printf` en las filminas, pero `masm32rt.inc` lo expone; la validación real con `ml.exe` sigue pendiente (Prompt 6.1).

---

## 2. Resumen ejecutivo

| Paquete | Intención | Activación | Inyección | Resultado de la auditoría |
|---------|-----------|------------|-----------|---------------------------|
| 5.0 | Infraestructura (mensajes, labels, helper) | `usaMsg*` calculados después | `emitirData()` `:227`, `emitirCodigo()` `:386-520` | **Defecto B1** en los labels; helper `emitirGuardia` documentado como usado pero sin llamadas |
| 5.b | `jc` tras `add` de `ulongint` | `:1076-1097` | `emitirAritmetica()` `:647-651` | Correcto; fallaba en ejecución por B1 |
| 5.e | `fxam/fstsw/sahf/jc` tras `fmul` de `doublef` | `:1102-1124` | `emitirAritmetica()` `:625-640` | **Defecto B2** (fuga de pila FPU) + B1 |
| 5.f | `jb` tras `sub` de `ulongint` | `:1129-1150` | `emitirAritmetica()` `:653-658` | Correcto; fallaba en ejecución por B1 |
| 5.h | `CMP eax` (base `ulongint`) / `FLD`+`FCOMP` (base `doublef`) contra cada valor del enum | `:1155-1167` | `emitirChequeoEnum()` y helpers `:703-808` | **Defectos B3, B4 (parcial), B5** + B1; B5 resuelto por extensión `doublef` |

**Estado final tras las correcciones de esta auditoría**: los cuatro chequeos se instrumentan, se activan solo cuando el fuente lo requiere, imprimen un mensaje y terminan con códigos 1/2/3/4; el flujo normal ya no cae en los labels de error; la pila FPU queda balanceada; la guardia de enum cubre las dos formas de `:=`, atributos y ambas bases soportadas (`ulongint`/`doublef`). Quedan brechas documentadas en §7 (no son regresiones nuevas, son casos no explorados).

---

## 3. Historial de commits analizado

Rango `77b2963..b9250e0` (10 commits). Cambios reales sobre `GeneradorAssembler.java`:

| Commit | Mensaje | Cambio real relevante |
|--------|---------|-----------------------|
| `77b2963` | Atomizacion p5, 5.0 | Constantes `MSG_*`, flags `usaMsg*`, `emitirMensajesErrorRuntime()`, 4 labels de fallo con `ExitProcess 1..4`, helper `emitirGuardia()`. **Aquí se introdujo B1**: los labels se emitían pegados al final del flujo normal. |
| `4f0bb68` | solo salidas por consola | Agregó la guarda de resta (`jb`) y casos en `resta_enteros_negativa/`. Título confuso: anticipó 5.f antes de 5.b. |
| `67d8f5b` | Resta entre dos ulongint, semantica b) | `calcularChequeosRuntime()` + guarda `jc` de suma. El mensaje del commit dice «resta» pero el código instrumenta **suma** (5.b). Origen de la confusión b/f en nombres de carpetas. |
| `eeb6ca2` | 5.e: verificación en ejecución | Secuencia `fld destino / fxam / fstsw ax / sahf / jc` tras `fmul`; activación por `* doublef`. **Aquí se introdujo B2** (sin `fstp`). |
| `d8f5407` | Desacoplar asignaciones | Extrajo `emitirAsignacionExpresion()` / `emitirAsignacionSentencia()`. Al extraer, la rama de expresión quedó sin guardia de enum (B3 latente). |
| `eebe3d2` | EmitirData por simbolo | Refactor de `.DATA` (`emitirSimbolosDeTablaDeSimbolos`, `emitirReceptoresThis`, etc.). Sin impacto en checks. |
| `085334d` | pruebas_5e y documentacion | Creó `DECISIONES_5.e.md`, `producto_doublef/` y ajustó el script 5.e. No corrigió el caso «desborda», que en realidad no desborda. |
| `7e468d2` | 5f: Restas ulongint | Guarda `jb error_negativo_resta` + activación por `- ulongint`. |
| `313d164` | 5h: Asignacion enums | `BigInteger`, `contadorEnumChequeo`, bloque de guardia en `emitirAsignacionSentencia()` y activación en `calcularChequeosRuntime()`. Solo `EntradaTabla`; solo base `ulongint`; no se aplicó a la rama de expresión (B3/B4/B5). |
| `b9250e0` | Plan 6 | Solo docs: dividió Prompt 6 en 6.0–6.4, pero dejó un bloque duplicado del Prompt 6 y datos de HEAD obsoletos. |

`git stash@{0}` = `f443116 WIP on tp4alt: 7e468d2 ...` existe pero **no fue aplicado**.

---

## 4. Implementación actual, punto por punto

Números de línea posteriores a las correcciones de esta auditoría.

### 4.1 Infraestructura 5.0

- Constantes de mensaje `:111-114`; flags `:115-118`; contador de enum `:121`.
- `.DATA`: `emitirMensajesErrorRuntime()` `:318-336` emite `DB "...",0` solo si el flag está activo.
  - `msg_overflow_suma` → `"Error: overflow en suma de ulongint",0`
  - `msg_overflow_float` → `"Error: overflow en producto de doublef",0`
  - `msg_negativo_resta` → `"Error: resultado negativo en resta de ulongint",0`
  - `msg_enum_invalido` → `"Error: asignacion invalida a variable de tipo enumeracion",0`
- `.CODE`: labels `:492-518`, cada uno con `invoke crt_printf, offset <msg>` + `invoke ExitProcess, <1|2|3|4>`; `jmp fin_normal` y `fin_normal:` en `:492` / `:517`.
- `generar()` `:170-202` llama a `calcularUsoFpuStatus()`, `calcularFormatosPout()` y `calcularChequeosRuntime()` antes de emitir `.DATA`/`.CODE`, de modo que los flags ya están definidos.
- `emitirGuardia()` `:1777-1779` existe, pero **ninguna guardia lo invoca** (los emisores escriben el salto directamente para mantenerlo pegado a la instrucción que fija flags). Se corrigió el comentario que decía lo contrario.

### 4.2 5.b — overflow de suma `ulongint`

- Activación `:1076-1097`: existe un terceto `+` cuyo `ResultadoTerceto.tipo == Tipos.ULONGINT`.
- Emisión `:647-651`: `mov eax, op1` / `add eax, op2` / `jc error_overflow_suma` / `mov aux, eax`.
- Corrección: `CF=1` tras `ADD` es exactamente el acarreo de 32 bits sin signo; suficiente para detectar `> 4294967295`. No se chequean literales plegados porque el IR ya los materializa como constantes `DD` y la suma se emite siempre en ASM.

### 4.3 5.e — overflow de producto `doublef`

- Activación `:1102-1124`: terceto `*` con tipo `doublef`.
- Emisión `:625-640`: `fld op1` / `fmul op2` / `fstp @auxN` / `fld @auxN` / `fxam` / `fstsw ax` / **`fstp st(0)`** / `sahf` / `jc error_overflow_float`.
- `C0` = bit 8 del status word → `AH` → `CF` vía `SAHF`. Para FXAM, `C0=1` marca ∞/NaN/empty **y también desnormales**; los finitos normales y el cero dejan `C0=0`. El caso desnormal es un falso positivo documentado en §7.
- **B2 corregido**: sin el `fstp st(0)` cada producto dejaba un valor en la pila FPU; la 8.ª multiplicación desbordaba ST y generaba un falso error en un programa válido. `FSTP` no altera `AX` ni EFLAGS, por lo que `C0` capturado se conserva.
- **Limitación conocida**: no se detecta underflow a desnormal/cero (`C0=0`) y `C0=1` también cubre `unsupported`/`empty`. No hay casos de NaN/Inf de entrada ni de productos encadenados.

### 4.4 5.f — resultado negativo en resta `ulongint`

- Activación `:1129-1150`: terceto `-` con tipo `ulongint`.
- Emisión `:653-658`: `mov eax, op1` / `sub eax, op2` / `jb error_negativo_resta` / `mov aux, eax`.
- `JB` (CF=1) es el préstamo sin signo, equivalente a `op1 < op2`. Funciona igual con operandos `EntradaTabla`, `@auxN`, `ReferenciaAtributo` y slots `[ebp±n]` porque todos se resuelven primero a texto ASM en `operandoAsm()` `:534-599`.

### 4.5 5.h — asignación inválida a enum

- Activación `:1155-1167`: terceto `:=` cuyo destino (variable **o atributo**) tiene un `TipoEnumerado` de base `ulongint` **o `doublef`**.
- Emisión centralizada en `emitirChequeoEnum()` con dos ramas:

  **Base `ulongint`** (`emitirChequeoEnumEntero()`):
  ```
  mov eax, <src>
  cmp eax, <v1> / je enum_valido_N
  ... (uno por valor del enum)
  jmp error_enum_invalido
  enum_valido_N:
  mov <dst>, eax        ; solo si pertenece
  ```

  **Base `doublef`** (`emitirChequeoEnumDoublef()`), reutilizando `emitirComparacionFpu()` de la filmina 11.1:
  ```
  fld <src> / fcomp <c_v1 DQ> / fstsw @fpu_status / mov ax, @fpu_status / sahf
  jp error_enum_invalido        ; C2=1: NaN / desordenado
  jz enum_valido_N              ; C3=1: igual
  ... (uno por valor del enum)
  jmp error_enum_invalido
  enum_valido_N:
  fld <src> / fstp <dst>        ; copia DOUBLEF
  ```
  `FCOMP` hace `POP` de `ST(0)`, así que la pila FPU vuelve a 0 tras cada comparación (no se repite B2). `@fpu_status DW ?` se emite porque `calcularUsoFpuStatus()` detecta ahora este caso. Las constantes se referencian por su símbolo `c_*` ya materializado en `.DATA` (`operandoConstanteEnum()`).
- `tipoDeDestinoAsignacion()` resuelve `EntradaTabla` y `ReferenciaAtributo`.
- `tipoEnumeradoChequeable()` acepta ambas bases soportadas; activación y emisión comparten el predicado (no hay label muerto, B5 resuelto).
- Lo invocan **ambos** emisores de `:=` (sentencia y expresión; B3).
- Etiquetas `enum_valido_N` únicas; el contador se reinicia en `generar()`.

---

## 5. Defectos encontrados y correcciones aplicadas (justificación)

| ID | Sev | Ubicación original | Problema | Corrección y justificación |
|----|-----|--------------------|----------|----------------------------|
| **B1** | CRÍTICA | `emitirCodigo()` final (antes `:473-497`, ahora `:386-520`, bloque `:492-518`) | Los 4 labels de error se emitían inmediatamente después del último terceto normal, sin `jmp`. Todo programa instrumentado —incluidos los casos «válidos»— imprimía el mensaje y terminaba con `ExitProcess 1..4`. Ningún script lo detectaba porque solo grepan texto. | Emitir `jmp fin_normal` **solo si** hay algún chequeo activo, y `fin_normal:` después del último label, antes de `ExitProcess, 0`. Justificación: el PDF exige mensaje+fin **cuando se produzca la situación de error**; el patrón de `winasmtut.pdf` es `jmp _quit` antes del bloque de error. Sin el salto, el requisito se viola exactamente al revés. |
| **B2** | ALTA | `emitirAritmetica()` rama FPU (antes `:604-610`, ahora `:625-640`) | `FLD @auxN / FXAM / FSTSW AX / SAHF / JC` sin `FSTP`: cada `fmul` filtraba un valor en ST. Tras 8 productos se desbordaba la pila FPU (maskada) y aparecía un falso overflow. | Insertar `fstp st(0)` después de `fstsw ax` y antes de `sahf`. Justificación: balance de pila exigido por el uso del 80x87 en el TP4; `FSTP` no modifica `AX` ni EFLAGS, por lo que `C0` ya copiado se mantiene y `JC` sigue siendo correcto. |
| **B3** | MEDIA | `emitirAsignacionExpresion()` (antes `:666-680`) | El dispatcher `:415-422` envía `:=` con `produceValor=true` a este emisor, que no chequeaba enum; sin embargo `calcularChequeosRuntime()` ya activaba 5.h. Resultado: mensaje y label emitidos, guardia nunca saltada. | Extraer `emitirChequeoEnum()` y llamarlo desde ambos emisores. Justificación: la condición `h` del PDF no distingue forma de asignación; activación y emisión deben ser simétricas. Se eliminaron dos locales muertos (`aux`, `flotante`) en el método tocado. |
| **B4** | MEDIA | `emitirAsignacionSentencia()` y activación 5.h | `ReferenciaAtributo` (obj.atr) no recibía guardia; la activación solo miraba `EntradaTabla`. | `tipoDeDestinoAsignacion()` + activación actualizada. Justificación: el TP4 incluye temas 25/28/31 (atributos y herencia) dentro del mismo práctico; un atributo de tipo enum es una «variable de tipo enumeración» según el PDF. **Cobertura aún parcial**: `PAR`, `ret`, `COPY` siguen sin guardia (§7). |
| **B5** | BAJA | activación 5.h + emisión | Enums con base `doublef` activaban mensaje y `error_enum_invalido:` pero el emisor exigía `Tipos.ULONGINT`, dejando código muerto. | Unificar activación y emisión en `tipoEnumeradoChequeable()`. Justificación original de auditoría: no emitir infraestructura nunca alcanzable. **Extensión posterior (pedido explícito del usuario)**: el mismo predicado acepta ahora `ULONGINT` y `DOUBLEF`, y `emitirChequeoEnum()` ramifica la comparación (§4.5), con lo que el label de error deja de ser código muerto **y** el enum `doublef` queda cubierto en runtime. Se reutiliza `emitirComparacionFpu()` y `FCOMP` hace `POP`, evitando repetir B2; se agregó detección en `calcularUsoFpuStatus()` para declarar `@fpu_status DW ?`. |
| **R1** | BAJA | `generar()` `:170-179` | `contadorEnumChequeo`, `cadenasErrorPorTexto` y `contadorCadenasError` no se reiniciaban al reutilizar una instancia del generador. | Reiniciar los tres al comienzo de `generar()`. Justificación: evita `enum_valido_N` duplicados entre dos fuentes y mensajes heredados; `cadenasErrorPorTexto` se puebla dentro de `emitirCodigo()`, así que limpiarlo antes es seguro. |
| **R2** | DOC | headers y `emitirGuardia()` | Comentarios decían «ningún chequeo activo» y que los paquetes usaban `emitirGuardia()`. | Actualizar header de clase `:42-47` y Javadoc `:1762-1776`. Justificación: los comentarios describían un estado anterior y podían inducir a error sobre dónde está la guardia real. |
| **R3** | TEST | `probar_prompt5_f.sh:74,81` | Patrones `sub eax, c_10_ul_ul` / `c_20_ul_ul` obsoletos; los `.txt` actuales restan `y_main`. Script terminaba EXIT 1. | Cambiar a `sub eax, y_main` (el operando real). Justificación: el assertEquals debía reflejar el IR vigente, no una versión anterior; el `jb` ya estaba presente. |
| **R4** | TEST | `probar_prompt5_e.sh` | Ejecutaba solo `producto_doublef/`, cuyo caso «desborda» es `2e12` (finito). La carpeta con el desborde real (`max*max`) estaba huérfana. | Incluir `producto_doublef_overflow/*.txt` en el loop y verificar su `.asm`. Justificación: sin eso, jamás se ejercita el caso que dispara `jc error_overflow_float`. |
| **R5** | TEST | scripts 5.0/5.b/5.e/5.f/5.h | Ninguno comprobaba el salto de skip; 5.h no incluía 5.f en regresiones; `no_verificar` era código muerto en b/e/f/h. | Agregar aserciones `jmp fin_normal` / `fin_normal:` (y `fstp st(0)` en 5.e), `no_verificar` en 5.0, y 5.f a las regresiones de 5.h. Justificación: fijar las correcciones B1/B2 como regresión y cerrar la suite verde engañosa. No se agregó 5.h a las regresiones de los demás para evitar ciclos (5.h ya llama a 5.f). |
| **R6** | DOC | `DECISIONES_P4.md:145`, `TEMPLATES_ASM.md:93`, docs 5.0/5.e duplicados, `PlanTP4.md` | «Chequeos pendientes», «división por cero en doublef», docs duplicados y contradictorios, HEAD obsoleto, Prompt 6 duplicado. | Corregir estados, agregar avisos de no-canonicidad en los duplicados, quitar el bloque duplicado del Prompt 6 y actualizar rutas. Justificación: el informe debe poder seguirse sin contradecir el código ni el PDF. |

**Snapshots regenerados**: con el generador corregido se regeneraron **33 `.asm`** (32 bajo `test_tp_4/` y `test/tp2/generales/00_programa_valido.asm`). La extensión `doublef` agregó **2 pares `.txt`/`.asm` nuevos** en `test_tp_4/enumerados/`, por lo que el directorio pasó de 51 a **53 pares**. En las carpetas Prompt 5 los cambios son exactamente los esperados (`jmp fin_normal`, `fin_normal:`, `fstp st(0)`, y en los nuevos casos `fcomp`/`jp`/`jz`). En `Control de flujo y comparadores/` y algunas carpetas 4b/4c/4e los snapshots estaban **desactualizados desde prompts anteriores** (comentarios `(PROGRAMA...)`, `includelib`, `@tp4_fmt_*`, `@tp4_main`) y quedaron sincronizados con el generador actual. No se cambió ninguna lógica por esto; se documenta para explicar el volumen del diff.

---

## 6. Verificación ejecutada

Compilación Java limpia:

```bash
javac -encoding UTF-8 -d /tmp/opencode/build_final *.java \
  "acciones semanticas"/*.java otras_acciones/*.java
# exit 0
```

Chequeo de sanidad de la suite: los **51 `.txt`** originales de `test_tp_4/` se compilaron con el generador corregido (en una copia bajo `/tmp/opencode/check_all/`) y **ninguno** produjo `Error:` en stdout (`TOTAL=51 CON_ERROR=0`). Tras la extensión `doublef` se incorporaron 2 casos nuevos (`03_doublef_valida.txt`, `04_doublef_invalida.txt`), ambos compilados sin `Error:`; el directorio quedó en **53 pares `.txt`/`.asm`**. Esto confirma que los comentarios aclarados, los snapshots regenerados y la nueva rama FPU no rompieron ningún caso.

Comprobaciones manuales de salida (`/tmp/opencode/verificacion_*.asm`, generados con `java -cp /tmp/opencode/build_final Main …`):

- Programa con suma, resta y producto `doublef`: aparecen `jc error_overflow_suma`, `jb error_negativo_resta`, `fxam / fstsw ax / fstp st(0) / sahf / jc error_overflow_float`, y al final `jmp fin_normal` → labels → `fin_normal:` → `ExitProcess, 0`.
- Programa con enum `ulongint`: `mov eax, x_main / cmp eax, 1..3 / je enum_valido_0 / jmp error_enum_invalido / enum_valido_0: / ... / jmp fin_normal / ... / fin_normal:`. Sin regresión: `01_asignacion_valida.asm` conserva ese patrón.
- Programa con enum `doublef` (`typedef medidas = [1.5, 2.5]; medidas m; doublef v; m := v;`): emite `c_1_5 DQ 1.5`, `c_2_5 DQ 2.5`, `@fpu_status DW ?`, `msg_enum_invalido`, y por cada valor `fld v_main / fcomp c_1_5 (o c_2_5) / fstsw @fpu_status / mov ax, @fpu_status / sahf / jp error_enum_invalido / jz enum_valido_0`, cerrando con `jmp error_enum_invalido / enum_valido_0: / fld v_main / fstp m_main / jmp fin_normal / ... / fin_normal:`. `FCOMP` deja la pila balanceada (no reaparece B2). Casos: `/tmp/opencode/enum_doublef_valida.asm` y `enum_doublef_invalida.asm`, más los snapshots `test_tp_4/enumerados/03_*` y `04_*`.
- Asignación de enum dentro de expresión (`p := p = (x)`): emite **dos** guardias con etiquetas `enum_valido_0` y `enum_valido_1` (B3).

Scripts (resultado final tras las correcciones):

| Script | Resultado | Detalle |
|--------|-----------|---------|
| `probar_prompt5_0.sh` | ✅ EXIT 0 | 13 verificaciones + 7 regresiones; ahora también comprueba ausencia de `jmp fin_normal`/`fin_normal:` cuando no hay checks |
| `probar_prompt5_b.sh` | ✅ EXIT 0 | verificaciones + regresiones 5.0 y suite Prompt 4 |
| `probar_prompt5_e.sh` | ✅ EXIT 0 | incluye el caso real `max*max` y `fstp st(0)` |
| `probar_prompt5_f.sh` | ✅ EXIT 0 | patrón corregido a `sub eax, y_main`; `jb` presente; 10 regresiones OK |
| `probar_prompt5_h.sh` | ✅ EXIT 0 | 4 casos (2 `ulongint` + 2 `doublef`), aserciones `fcomp`/`jp`/`jz`/`@fpu_status`, guardias `enum_valido_0` + B1; incluye 5.f en regresiones |
| `probar_prompt4e.sh` | ✅ EXIT 0 | regresión general Prompt 4 |

> Nota: resultado final de la batería en §10.

**Limitación de verificación**: no hay `ml.exe`/`link` ni MASM32 en este entorno. Todo lo anterior es compilación Java + inspección estática del `.asm`. El ensamblado/ejecución real es tarea del paquete 6.1.

---

## 7. Inconsistencias y casos no explorados en `test_tp_4`

Auditoría de los 5 scripts y las 6 carpetas (14 pares `.txt`/`.asm` después de la extensión `doublef`; antes 12):

| Ítem | Hallazgo | Tratamiento |
|------|----------|-------------|
| Scripts | Los 5 usan `grep -qF` sobre `.asm`: validan **texto**, no comportamiento runtime. Ninguno ensambla ni ejecuta. | Documentado; acceso real queda para 6.1/6.2 |
| 5.f | EXIT 1 antes de la corrección: patrones de constantes obsoletos. | Corregido (R3) |
| 5.e | `producto_doublef/02_producto_desborda.txt` es `1e6 * 2e6 = 2e12` → **no desborda**. | Corregido (R4): se ejecuta la carpeta con desborde real; el comentario del `.txt` se aclaró |
| Nomenclatura | `resta_enteros_negativos/` contiene casos de **suma** (5.b); `resta_enteros_negativa/` contiene restas (5.f). Difieren en una `s`. | Documentado en `PlanTP4.md`, en los `.txt` y aquí; no se renombraron carpetas para no romper rutas de scripts/docs |
| Duplicados | `producto_doublef/` y `producto_doublef_overflow/` repiten el caso válido y tienen «02_producto_desborda» con significados opuestos. | Documentado; ahora ambas se ejecutan |
| Huérfanos | `producto_doublef_overflow/` no era ejecutado por ningún script; `test_resta_negativa.*` se compilaba pero nunca se verificaba. | Primera resuelta (R4); segunda documentada |
| Docs duplicados | `DECISIONES_5.e.md` vs `DECISIONES_5_e.md`; `DECISIONES_5_h.md` vs `DECISIONES_P5h.md`. | Avisos de no-canonicidad + consolidación aquí |
| Comentarios `.txt` | `01_suma_valida` y `01_producto_valido` decían que **no** debía generarse la guardia; `01_sin_chequeos_activos` decía que los paquetes «aún no activan nada». | Corregidos (no afectan el ASM) |
| Regresiones | 5.h omitía 5.f; ningún script incluía 5.h (evitar ciclos). | 5.f agregado a 5.h; se documenta la asimetría deliberada |

### Brechas de cobertura (casos no explorados)

| # | Chequeo | Caso faltante | Riesgo |
|--:|---------|---------------|--------|
| 1 | e | Producto por 0, por negativo, productos múltiples/anidados en un mismo programa | No se valida unicidad de label ni guardia en cada `fmul` |
| 2 | e | NaN, ±Inf de entrada, subnormales | `FXAM` marca `C0=1` también para esos estados: posibles falsos positivos |
| 3 | b | Suma encadenada `r := x + y + 1$ul`, operando constante directo, bordes `max+0` / `max-1+1` | Guardia probada solo con una forma de IR |
| 4 | f | Igualdad `10-10` (CF=0), `0-1`, restas encadenadas | No se valida el no-disparo límite |
| 5 | h | Asignación desde expresión (`p := p = (x)`) en el script | Verificada manualmente (B3) pero **no** agregada al suite: el backend emite `[auxN]` sin declarar para asignación-en-expresión (defecto preexistente de Prompt 4, fuera de este scope) |
| 6 | h | Atributo enum (`obj.atr`), parámetro enum, retorno enum, enum local en `[ebp+n]` | Cobertura parcial (B4) |
| 7 | h | Enum con un solo valor, valores no contiguos/duplicados/0/`max`, enum vacío | Ramas de comparación y el antiguo camino `algunoValido == false` sin test |
| 8 | h | Múltiples asignaciones a enum: se hardcodea `enum_valido_0` en el script | Unicidad de `enum_valido_N` no probada por el script (sí verificada en /tmp) |
| 9 | h | Constante fuera de rango directa | Es error **estático** (`Verificaciones.java`); el runtime solo cubre valores indirectos. Confusión estático/runtime a documentar en el informe final |
| 10 | transversal | `COPY` de objetos con atributos enum | La copia campo-a-campo no pasa por `emitirChequeoEnum()`: posible bypass (B4) |
| 11 | h | Enums con base `doublef` | **Resuelto por extensión**: guardia runtime vía `FLD`/`FCOMP`/`FSTSW`/`SAHF` + `jp`/`jz`, con constantes `c_* DQ` y `@fpu_status`. Queda por explorar: enum `doublef` con un solo valor, NaN/±Inf de entrada, valores duplicados/0 y atributos/parámetros de enum `doublef` |
| 12 | transversal | Comparación `.asm` generado vs snapshot, ensamblado y ejecución | Toda la verificación es texto; ver §6 |

---

## 8. Estado frente al enunciado y trabajo pendiente

**Cumplido dentro del Prompt 5**: infraestructura común, cuatro chequeos del grupo 43 con activación condicional, mensajes claros, terminación con códigos distintos, flujo normal protegido, pila FPU balanceada, guardia de enum compartida y consistente con su activación (incluida la extensión a enums `doublef`), snapshots regenerados, scripts corregidos y documentación consolidada.

**No incluido por no corresponder al grupo 43**: división por cero (a), overflow de suma float (c), overflow de producto entero (d), pérdida en conversión float→entero (g), recursión (i). El PDF los define pero la tabla no los asigna a este grupo.

**Estado del Prompt 6 al cierre del paquete 6.0**:

1. **6.0** ✅ Compuerta y ruta del `.asm` ejecutadas; ⚠️ stdout TP4 revertido a TP3 por pedido del usuario (él lo recortará antes de entregar). Snapshots y `probar_prompt4e.sh` restaurados a `HEAD`. Detalle en `DECISIONES_6.0.md`.
2. **6.1** ✅ validación estática ejecutada: includes, `.data`/`.code`, `end start`, `DQ` sin `ALIGN 8`, `crt_printf` con `%f` y CRLF al escribir el `.asm` (`probar_prompt6_1.sh`, exit 0). ⚠️ Ensamblado/enlace real con `ml.exe`/`link` pendiente de Windows. Detalle en `DECISIONES_6.1.md`.
3. **6.2** ⏳ batería de regresión y suite con disparo real de b/e/f/h + casos límite; cubrir las brechas 1–12 de §7.
4. **6.3** ⏳ limpieza de TODOs resueltos **y desarrollo profundo de comentarios** (precondiciones, efectos, convenciones y decisiones no obvias), docs de uso.
5. **6.4** ⏳ eliminación de comentarios: **último paquete**, requiere confirmación explícita («**BORRAR COMENTARIOS**»).

---

## 9. Archivos tocados por esta auditoría

- Código: `GeneradorAssembler.java` (B1–B5, R1, R2; extensión `doublef` en `tipoEnumeradoChequeable()`, `emitirChequeoEnum()`, helpers `emitirChequeoEnumEntero()`/`emitirChequeoEnumDoublef()`, `operandoConstanteEnum()` y `calcularUsoFpuStatus()`).
- Scripts: `probar_prompt5_0.sh`, `probar_prompt5_b.sh`, `probar_prompt5_e.sh`, `probar_prompt5_f.sh`, `probar_prompt5_h.sh` (R3–R5; este último ampliado con aserciones `doublef` y 4 casos).
- Casos: comentarios aclarados en `Runtime chequeos/01_sin_chequeos_activos.txt`, `resta_enteros_negativos/01_suma_valida.txt`, `producto_doublef/01_producto_valido.txt`; pares nuevos `test_tp_4/enumerados/03_doublef_valida.{txt,asm}` y `04_doublef_invalida.{txt,asm}`.
- Docs: este archivo (nuevo), `PlanTP4.md`, `DECISIONES_P4.md`, `TEMPLATES_ASM.md`, `DECISIONES_5.0.md`, `DECISIONES_5.e.md`, `DECISIONES_5_e.md`, `DECISIONES_5_h.md`, `DECISIONES_P5h.md` (estos dos últimos actualizados con la extensión).
- Snapshots: 33 `.asm` regenerados con el generador corregido (32 en `test_tp_4/` + `test/tp2/generales/00_programa_valido.asm`) + 2 nuevos del caso `doublef`; `test_tp_4/` quedó en 53 pares `.txt`/`.asm`.

---

## 10. Resultado final de la batería

Batería **post-extensión `doublef`** ejecutada con `bash test_tp_4/<script>` (cada script recompila Java en su propio build bajo `/tmp`). Logs: `/tmp/opencode/run3_*.log`. Resultados confirmados:

| Script | EXIT | `OK` | `FALLA:` | Verificación |
|--------|:----:|:----:|:--------:|--------------|
| `probar_prompt5_0.sh` | **0** | 25 | 0 | sin labels ni `jmp fin_normal` cuando no hay checks |
| `probar_prompt5_b.sh` | **0** | 25 | 0 | suma con `add eax, y_main` + `jc`, mensaje, label, `ExitProcess, 1`, `jmp fin_normal`/`fin_normal:` |
| `probar_prompt5_e.sh` | **0** | 38 | 0 | `fxam / fstsw ax / fstp st(0) / sahf / jc` en ambos casos + caso real `max*max` |
| `probar_prompt5_f.sh` | **0** | 27 | 0 | `sub eax, y_main` + `jb`, mensaje, label, `ExitProcess, 3`, `jmp fin_normal`/`fin_normal:` |
| `probar_prompt5_h.sh` | **0** | 56 | 0 | 4 casos: guardias `cmp`/`je` (`ulongint`) y `fld`/`fcomp`/`fstsw`/`sahf`/`jp`/`jz` (`doublef`), `jmp error_enum_invalido`, `ExitProcess, 4`, `jmp fin_normal`/`fin_normal:`; incluye 5.f |
| `probar_prompt4e.sh` | **0** | 112 | 0 | regresión general Prompt 4 |

**Resultado final: 6/6 scripts con EXIT 0 y cero `FALLA:` en los logs** (`/tmp/opencode/run3_*.log`). Las regresiones anidadas se ejecutaron todas sin errores. Quedan fijadas como regresión B1 (`jmp fin_normal` / `fin_normal:`), B2 (`fstp st(0)`), B3/B4 (guardia compartida y atributos), B5 con **ambas bases** (`cmp eax` para `ulongint`, `fcomp`/`jp`/`jz` para `doublef`) y las correcciones de patrones obsoletos de 5.f y de la carpeta huérfana de 5.e.

Recordatorio: esta batería valida **texto ASM**, no ensamblado ni ejecución. `ml.exe`/`link`/MASM32 no están disponibles aquí; la validación real queda para el paquete 6.1.

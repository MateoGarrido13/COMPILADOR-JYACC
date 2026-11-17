# Prompt 4 (TP4) — Control de flujo y temas particulares (13, 19, 25, 28, 31) en ASM

## Objetivo

Completar el backend MASM32 32-bit (`GeneradorAssembler.java`) para traducir
las construcciones del Grupo 43 que faltaban después del Prompt 3:

- Tema 13: `IF/ELSE/END_IF`, `REPEAT-WHILE`, etiquetas, `BF/BI`, comparadores.
- Tema 19: funciones, métodos, `PAR`, `CALL`, `CALLMET`, `ret` y orden de
  evaluación de parámetros.
- Tema 25: asignación estructural de objetos (`COPY`).
- Tema 28: lectura/escritura de atributos de objetos.
- Tema 31: atributos y métodos heredados (prefijo de clase).
- `pout` de `ulongint`, `doublef` y `cadena`.
- Marcadores `CLASE/FIN_CLASE` como no-op controlado.

Sin alterar la salida observable de TP3 (consola, tokens, estructuras, errores,
TS y tercetos).

## Archivos modificados / agregados

| Archivo | Cambio |
|---|---|
| `GeneradorAssembler.java` | Backend P4 completo: separación main/bloques, control de flujo, comparadores, `pout`, funciones/métodos, `COPY`, atributos, etiquetas y slots de retorno. |
| `Entorno.java` | Aditivo: `AtributoEfectivo`, `atributosEfectivosConDeclarante`, visibilidad pública de `claseDeObjeto` y `clasePorNombre`. No cambia la semántica existente. |
| `test_tp_4/01_if_else_anidado.txt` … `11_funciones_anidadas.txt` | Once casos dirigidos de P4. |
| `test_tp_4/probar_prompt4.sh` | Script de compilación, ejecución y verificación de los casos + regresión TP3. |
| `test_tp_4/guia.md` | Se agregó la sección de uso del Prompt 4. |
| `docs/TP4/DECISIONES_P4.md` | Convención de llamada, layout de objetos, ejemplos y limitaciones. |
| `docs/TP4/PlanTP4.md` | Estado del Prompt 4 marcado como ejecutado. |

## Implementación por tarea del enunciado

### 1) Control de flujo (Tema 13)

En `GeneradorAssembler`:

- `emitirComparacion()`: enteros con `mov/cmp/setcc/movzx`; `doublef` con
  `fld/fcomp/fstsw ax/sahf/setcc`.
- `emitirBF()`: `cmp <cond>, 0` + `je <label>`.
- `emitirBI()`: `jmp <label>`.
- Las etiquetas del IR (`label_N`) se emiten como `label_N:`.
- `IF/ELSE/END_IF` y `REPEAT-WHILE` no requieren lowering adicional: el IR ya
  emite los `BF/BI` y las etiquetas en el orden correcto, incluido el
  anidamiento.
- Los marcadores estructurales no ejecutables no generan código.

### 2) `pout`

Formatos en `.DATA` y llamadas a `printf`:

```asm
@tp4_fmt_uint   DB "%u",10,0
@tp4_fmt_double DB "%.20Lf",10,0
@tp4_fmt_string DB "%s",10,0
```

```asm
invoke printf, addr @tp4_fmt_uint, x_main
invoke printf, addr @tp4_fmt_double, d_main
invoke printf, addr @tp4_fmt_string, addr hola_mundo
```

Se agregaron al encabezado `includelib kernel32.lib`, `includelib masm32.lib`,
`dll_dllcrt0 PROTO C` y `printf PROTO C : VARARG`.

### 3) Funciones, métodos e invocaciones (Tema 19)

Convención propia documentada en `DECISIONES_P4.md`:

- parámetros formales en slots globales `.DATA` provistos por la TS;
- `PAR formal, real` copia real→formal en el orden del IR;
- `CALL`/`CALLMET` hacen `call <label>`;
- `ret` copia al slot `@tp4_ret_<label>` y ejecuta el epílogo;
- el llamador copia el slot de retorno al `@auxN` del terceto de llamada;
- prólogo/epílogo estándar con `ebp/edi/esi`;
- el epílogo de seguridad final se omite si el cuerpo ya terminó en `ret`;
- `PAR` de un formal de tipo clase copia sus atributos materializados, igual
  que `COPY`.
- La entrada de definición se resuelve por **ámbito propio exacto**
  (`main`, `main:a`, `main:a:b`, `main:ca`, ...) con una pila unificada de
  ámbitos de clases y funciones/métodos. Así, dos funciones homónimas en
  scopes anidados generan labels y slots de retorno distintos
  (`fun_c_main_a_b` vs `fun_c_main_a`); ver `11_funciones_anidadas.txt`.

Ejemplo de definición y llamada:

```asm
; FUNCION mitad
mitad_main:
    push ebp
    mov ebp, esp
    push edi
    push esi
    fld d_main_mitad
    fdiv c_2_0
    fstp @aux3
    fld @aux3
    fstp @tp4_ret_mitad_main
    pop esi
    pop edi
    mov esp, ebp
    pop ebp
    ret

@tp4_main:
    fld c_5_0
    fstp d_main_mitad
    call mitad_main
    fld @tp4_ret_mitad_main
    fstp @aux7
```

### 4) Objetos, atributos y COPY (Temas 25, 28, 31)

- No hay objeto contiguo: la TS materializa un slot `.DATA` por atributo y por
  objeto.
- Atributo propio: `objeto.atr:ambito` → `a1_x_main`.
- Atributo heredado: `objeto.declarante.atr:ambito` → `a1_padre_y_main`.
- `ReferenciaAtributo` se resuelve por `texto` + ámbito, con respaldo por uso
  `ATRIBUTO` y lexema calificado.
- `COPY` copia posicionalmente los atributos efectivos en orden base-primero,
  con el tipo del atributo destino.
- `CLASE/FIN_CLASE` no emiten instrucciones ni memoria.

Ejemplo de `COPY`:

```asm
; COPY h1:main := b1:main (copia posicional)
    mov eax, b1_a_main
    mov h1_padre_x_main, eax
    fld b1_b_main
    fstp h1_y_main
```

### 5) No-op de marcadores estructurales

`PROGRAMA`, `FIN_PROGRAMA`, `CLASE`, `FIN_CLASE`, `FUNCION`, `FIN_FUNCION`,
`METODO` y `FIN_METODO` se ignoran en la emisión de instrucciones. Las clases
solo aportan información de layout a través de `Entorno`/TS.

### 6) Extra: epílogo no duplicado

La primera versión de P4 emitía el epílogo del `ret` y además el epílogo de
seguridad al final del bloque. Se agregó el estado `epilogoAlFinal`: el
epílogo final solo se emite si el último código del bloque no fue ya el de un
`ret`.

## Casos de prueba

Ubicación: `test_tp_4/`.

| Caso | Qué cubre | Verificación principal en `.asm` |
|---|---|---|
| `01_if_else_anidado.txt` | `IF/ELSE/END_IF` anidado | `setb`, `sete`, `je label_3`, `jmp label_4`, `label_1:` |
| `02_repeat_while.txt` | `REPEAT-WHILE` con if/else | `label_1:`, `je label_4`, `jmp label_1` y orden cuerpo→condición |
| `03_funciones_orden_parametros.txt` | `PAR/CALL/ret` y Tema 19 | `call foo_main`, slot `@tp4_ret_foo_main`, orden `b` antes que `a` |
| `04_metodo_heredado.txt` | `CALLMET` y herencia múltiple | `call m_main_cb`, `call m_main_cc`, slots por clase, orden `[2,1]` |
| `05_atributos_propios_heredados.txt` | atributos propios/heredados, `doublef` | `a1_x_main`, `a1_padre_y_main`, `fstp a1_padre_z_main`, `pout` de ambos |
| `06_copy_objetos.txt` | `COPY` entre estructuras compatibles | `; COPY h1:main := b1:main`, `mov`, `fld/fstp` |
| `07_pout_tipos.txt` | `pout` de los tres tipos | formatos `%u`/`%.20Lf`/`%s` y `addr` para cadena |
| `08_parametro_clase.txt` | formal de tipo clase | copia posicional real→formal (`p_x_main_leer`, `p_y_main_leer`) |
| `09_funcion_doublef.txt` | retorno `doublef` | slot `DQ`, `fdiv`, `fld @tp4_ret_mitad_main` |
| `10_if_doublef.txt` | comparación `doublef` en if/else | `fcomp`, `fstsw ax`, `sahf`, `seta al` |
| `11_funciones_anidadas.txt` | funciones homónimas en scopes anidados | `c_main_a`/`c_main_b` únicos, slots de retorno separados |

## Cómo ejecutar

Desde la raíz del proyecto:

```bash
# Compilar
javac -encoding UTF-8 -d /tmp/tp4p4 *.java "acciones semanticas"/*.java otras_acciones/*.java

# Ejecutar un caso (genera el .asm junto al .txt)
java -cp /tmp/tp4p4 Main test_tp_4/06_copy_objetos.txt
cat test_tp_4/06_copy_objetos.asm

# Suite completa del Prompt 4 (copia los casos a /tmp, no ensucia el repo)
bash test_tp_4/probar_prompt4.sh
```

Resultado esperado del script: `RESULTADO: todas las verificaciones del
Prompt 4 pasaron.` El script verifica que los 11 casos compilan sin `Error:`,
que cada `.asm` contiene las instrucciones esperadas y que el caso TP3 de
`repeat` conserva sus tercetos `BF`/`BI`.

## Verificación realizada

- Compilación Java: OK.
- `probar_prompt4.sh`: 11 casos + 57 verificaciones de `.asm` + regresión TP3,
  todas OK.
- Regresión de salida TP3: se compiló el `HEAD` (P3) en `/tmp` y se comparó el
  stdout de los **248** `.txt` de `test/` y `test_tp_4/` contra el estado P4.
  **0 diferencias** en fuente, tokens, estructuras, errores, TS y tercetos.
- Casos adicionales revisados manualmente: función con retorno `doublef`,
  método con parámetro, clase/objeto en ámbito de función, atributos
  heredados transitivos, `COPY` con herencia y funciones homónimas en scopes
  anidados (labels `c_main_a`/`c_main_b` y
  `fun_c_main_a`/`fun_c_main_a_b` únicos, cada una con su slot de retorno).

## Casos cubiertos

- `IF/ELSE/END_IF` simple, con bloques y anidado; `REPEAT-WHILE` simple y con
  control anidado.
- Comparadores enteros sin signo y `doublef` como flag 0/1.
- `pout` de `ulongint`, `doublef` y `cadena`.
- Funciones globales, funciones anidadas por ámbito (labels únicos), métodos,
  métodos heredados con prefijo y orden de parámetros.
- Retorno `ulongint` y `doublef`.
- Parámetros formales escalares y de tipo clase.
- Atributos propios, heredados y `doublef`; lectura y escritura.
- `COPY` entre clases de igual estructura, con herencia y nombres distintos.
- `CLASE/FIN_CLASE` sin código.

## Casos no cubiertos / limitaciones conocidas

- **Recursión y reentrancia**: no soportadas. Parámetros y slot de retorno son
  globales; el chequeo `i` de profundidad no pertenece al Grupo 43.
- **Retorno de objetos**: la gramática no acepta `clase function f(...)`, por
  lo que no se genera retorno de clase. No hay `pout` de objetos.
- **`this` implícito**: el IR de `CALLMET` no lleva receptor y el lenguaje no
  tiene `self/this`; un atributo sin prefijo dentro de un método usa el slot de
  la clase (`x_main_ca`), no el del objeto llamador (`a1_x_main`). Los métodos
  deben recibir explícitamente el objeto/parámetro si necesitan estado de
  instancia.
- **Atributos de tipo objeto anidado**: el frontend rechaza prefijos que no sean
  clases heredadas (`b1.inner.x`), por lo que no hay layout recursivo.
- **`todf` de `ulongint` grandes**: `fild` interpreta el entero como con signo;
  valores mayores que `2^31-1` se convierten mal. Es una limitación de la
  conversión FPU elegida, fuera de los chequeos runtime asignados.
- **Chequeos runtime `b/e/f/h`**: corresponden al Prompt 5; este prompt no los
  implementa.
- **MASM real**: los `.asm` se verifican por estructura e instrucciones, no se
  ensamblan en este entorno (no hay MASM32 instalado). El encabezado y las
  rutinas usan `masm32rt.inc`/`printf` de la convención MASM32 32-bit.

## Referencias

- `docs/TP4/DECISIONES_P4.md`: convención de llamada, layout de objetos,
  ejemplos y limitaciones detalladas.
- `docs/TP4/IR_CONTRATO.md`: contrato del IR consumido por el backend.
- `docs/TP4/DECISIONES_P3.md`: base de asignación/aritmética/todf.
- `test_tp_4/probar_prompt4.sh`: verificación automatizada.

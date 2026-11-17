# Decisiones y cambios — Prompt 4 (TP4, ASM MASM32 32-bit)

## Resumen

Se completó la traducción MASM32 32-bit de las construcciones del Grupo 43 en
`GeneradorAssembler.java`:

- control de flujo del Tema 13 (`IF/ELSE/END_IF`, `REPEAT-WHILE`, etiquetas,
  `BF/BI`, comparadores);
- `pout` de `ulongint`, `doublef` y `cadena`;
- funciones, métodos e invocaciones del Tema 19 (`PAR`, `CALL`, `CALLMET`,
  `ret`, `FUNCION/FIN_FUNCION`, `METODO/FIN_METODO`);
- objetos, atributos y herencia de los Temas 25/28/31 (`COPY`,
  `ReferenciaAtributo`, atributos propios y heredados);
- `CLASE/FIN_CLASE` como marcadores no ejecutables (no-op controlado).

`Entorno.java` se amplió de forma aditiva para exponer el layout efectivo de
atributos (`AtributoEfectivo`, `atributosEfectivosConDeclarante`) y las clases
de un objeto (`claseDeObjeto`, `clasePorNombre`). La salida observable de TP3
(consola, tokens, estructuras, errores, TS y tercetos) no cambia.

## 1. Organización del `.CODE`

En el IR las definiciones de funciones, métodos y clases aparecen antes del
cuerpo principal. Para que `start` no caiga dentro de una definición:

```asm
.code
start:
    jmp @tp4_main

; FUNCION foo
foo_main:
    ...

; METODO m
m_main_ca:
    ...

@tp4_main:
    ; cuerpo principal
    invoke ExitProcess, 0
end start
```

`preparar()` recorre los tercetos una vez y separa:

- `principales`: tercetos fuera de `FUNCION/METODO`;
- `bloques`: un `BloqueFuncion` por `FUNCION/FIN_FUNCION` y
  `METODO/FIN_METODO`, con nombre, clase, ámbito, tipo de retorno, label ASM y
  el cuerpo en orden.

Los delimitadores `PROGRAMA`, `FIN_PROGRAMA`, `CLASE`, `FIN_CLASE`,
`FUNCION`, `FIN_FUNCION`, `METODO` y `FIN_METODO` no emiten instrucciones. En
particular, `CLASE/FIN_CLASE` no reservan memoria ni ejecutan código: el layout
de los objetos se materializa por atributo en `.DATA` desde la Tabla de
Símbolos.

## 2. Control de flujo (Tema 13)

### Etiquetas

Cada terceto etiqueta (`label_N`, `--`, `--`) se traduce a `label_N:` en la
posición correspondiente. No se agregan etiquetas propias: se usan las que ya
reservó `GeneradorTercetos`, manteniendo el anidamiento de
`IF/ELSE/END_IF` y `REPEAT-WHILE`.

### Comparadores

Los comparadores `< > <= >= == !=` producen un resultado lógico `0/1` en su
`@auxN` (tipo `ulongint` en `ResultadoTerceto`).

Enteros (`ulongint`), sin signo:

```asm
mov eax, op1
cmp eax, op2
setb al          ; <  (seta >, setbe <=, setae >=, sete ==, setne !=)
movzx eax, al
mov @auxN, eax
```

`doublef`, con FPU x87:

```asm
fld op1
fcomp op2
fstsw ax
sahf
setb al          ; mismas condiciones; CF=C0, ZF=C3, PF=C2
movzx eax, al
mov @auxN, eax
```

`fcomp` descarta el operando comparado, por lo que no queda basura en la pila
FPU. Las condiciones elegidas son las mismas para enteros y para la bandera
FPU luego de `SAHF`.

### Saltos

- `BF` (salto si falso): `cmp <cond>, 0` + `je <label>`.
- `BI` (salto incondicional): `jmp <label>`.
- Un `BF` con condición inválida o sin etiqueta se emite como comentario; no
  genera salto a una etiqueta inexistente.

### IF/ELSE/END_IF

No requiere traducción especial: el IR ya emite comparación, `BF` al `else`,
`BI` al cierre y las etiquetas. Ejemplo real de
`test_tp_4/01_if_else_anidado.txt`:

```asm
    mov eax, x_main
    cmp eax, y_main
    setb al
    movzx eax, al
    mov @aux5, eax
    cmp @aux5, 0
    je label_3          ; else externo
    ...
    jmp label_2         ; cierre del then interno
label_1:                ; else interno
    ...
label_2:
    jmp label_4         ; cierre del then externo
label_3:                ; else externo
    ...
label_4:
```

### REPEAT-WHILE

La etiqueta de inicio, el `BF` de salida y el `BI` de retorno también vienen
del IR. Ejemplo de `test_tp_4/02_repeat_while.txt`:

```asm
label_1:
    ; cuerpo, incluido un if/else con label_2/label_3
    ...
    cmp @aux..., 0
    je label_4          ; while false -> sale
    jmp label_1         ; vuelve al cuerpo
label_4:
```

## 3. `pout` (salida por consola)

Se usan rutinas de `masm32rt.inc` y `printf` de la CRT:

```asm
printf PROTO C : VARARG
```

Formatos fijos en `.DATA`:

```asm
@tp4_fmt_uint   DB "%u",10,0
@tp4_fmt_double DB "%.20Lf",10,0
@tp4_fmt_string DB "%s",10,0
```

Traducciones:

| Tipo | IR | ASM |
|---|---|---|
| `ulongint` (o enum con base entera) | `(pout, x, --)` | `invoke printf, addr @tp4_fmt_uint, x_main` |
| `doublef` | `(pout, d, --)` | `invoke printf, addr @tp4_fmt_double, d_main` |
| `cadena` | `(pout, hola, --)` | `invoke printf, addr @tp4_fmt_string, addr hola_mundo` |

Se usa `addr` para el formato y para la cadena; los escalares se pasan por
valor (el símbolo `.DATA` ya es la dirección del valor). En MSVCRT,
`long double` es de 64 bits, por eso `%Lf` es compatible con el `DQ` que genera
el lenguaje para `doublef`. `%.20Lf` muestra hasta 20 decimales.

Un `pout` de un tipo no soportado (por ejemplo un objeto) se emite como
comentario, sin generar una llamada inválida.

## 4. Funciones, métodos e invocaciones (Tema 19)

### Convención propia, no reentrante

No se pasan argumentos por pila ni se usa `stdcall` interno. El estado de
parámetros vive en variables globales `.DATA` y el retorno en un slot único por
función/método:

- Cada parámetro formal ya está materializado por la TS como variable global
  (`n_main_foo`, `p_x_main_leer`, ...).
- `PAR formal, real` copia el valor real al slot del formal con `mov`
  (`ulongint`) o `fld/fstp` (`doublef`). El orden de los `PAR` es exactamente el
  orden en que `GeneradorTercetos` evaluó los reales; el emisor no reordena
  nada.
- `CALL` y `CALLMET` son un `call` al label ASM del llamado.
- `ret` copia el valor al slot `@tp4_ret_<label>` y ejecuta el epílogo.
- Luego del `call`, el llamador copia el slot de retorno al `@auxN` del terceto
  de llamada (`mov` para enteros, `fld/fstp` para `doublef`).

Ejemplo (`test_tp_4/03_funciones_orden_parametros.txt`):

```asm
; foo(b = x, a = z)[1, 2]  -> primero se evalúa el real 2 (a=z)
    mov eax, x_main
    mov b_main_foo, eax
    mov eax, z_main
    mov a_main_foo, eax
    call foo_main
    mov eax, @tp4_ret_foo_main
    mov @aux10, eax
```

Los slots de retorno se declaran en `.DATA` con el tipo del retorno:

```asm
@tp4_ret_foo_main        DD ?
@tp4_ret_mitad_main      DQ ?
@tp4_ret_m_main_ca       DD ?
```

### Labels de funciones y métodos

El label ASM sale de `TablaSimbolos.nombreAsmDe(entrada)`, el mismo nombre que
usa la TS y que se usa en la definición y en `CALL/CALLMET`. Ejemplos:

- función global `foo` → `foo_main`
- método `m` de `ca` → `m_main_ca`
- métodos homónimos en clases distintas → labels distintos.

La entrada de definición se busca por **ámbito propio exacto**, no por un
ámbito esperado aproximado. `preparar()` mantiene una pila unificada de
ámbitos abiertos (clases y funciones/métodos, en el orden del IR):

- al abrir `FUNCION`/`METODO`, el ámbito de declaración es el tope de la pila
  (`main` en el nivel global, `main:<clase>` dentro de una clase,
  `main:a` dentro de la función `a`, `main:a:b` dentro de `b`, etc.);
- el bloque recibe `ambitoPropio = ambitoDeclaracion:nombre`, que es
  exactamente la clave de `Entorno.FuncionDeclarada`;
- para funciones se resuelve con `entorno.getFuncion(ambitoPropio)`; para
  métodos con `entorno.getClase(ambitoDeclaracion).metodos.get(nombre)`;
- si `Entorno` no conserva la entrada (clase descartada), se busca en la TS por
  nombre, uso y ámbito exactos, sin aceptar una homónima de otro scope.

Esto mantiene labels únicos cuando hay funciones homónimas en scopes anidados,
por ejemplo:

```text
fun_c:main:a:b  -> fun_c_main_a_b   (retorno @tp4_ret_fun_c_main_a_b)
fun_c:main:a    -> fun_c_main_a     (retorno @tp4_ret_fun_c_main_a)
```

El tipo de retorno sale de `Entorno.FuncionDeclarada`/`ClaseDeclarada.metodos`,
con respaldo en `EntradaTabla.tipoDato`.

### Prólogo y epílogo

```asm
label:
    push ebp
    mov ebp, esp
    push edi
    push esi
    ; cuerpo
    pop esi
    pop edi
    mov esp, ebp
    pop ebp
    ret
```

El prólogo/epílogo es defensivo: el estado del programa está en `.DATA`, pero
`printf` puede modificar registros de propósito general. El epílogo de
seguridad se emite al final del bloque solo si el último código emitido no fue
ya el epílogo de un `ret` (evita el `ret` duplicado que aparecía en la primera
versión de P4).

### Parámetros formales de tipo clase

Un formal de tipo clase no tiene un `DD/DQ` único en `.DATA`; la TS materializa
sus atributos (`p_x_main_leer`, `p_y_main_leer`). `PAR` detecta que el tipo
efectivo del formal es una clase y hace la misma copia posicional que `COPY`,
usando los atributos efectivos del formal y del objeto real.

### Limitaciones de la convención

- **No hay recursión ni reentrancia**: los parámetros y el slot de retorno son
  globales; una segunda activación de la misma función pisaría la primera. El
  chequeo de profundidad `i` no está asignado al Grupo 43.
- Llamadas anidadas a funciones distintas son válidas: cada una tiene sus
  propios slots.
- **No hay `this` implícito**: el IR de `CALLMET` no transporta el receptor y el
  lenguaje no tiene `self/this`. Un atributo sin prefijo dentro de un método
  resuelve al slot de la clase (`x_main_ca`), no al objeto receptor
  (`a1_x_main`). Los métodos que necesiten estado de instancia deben recibir un
  objeto o parámetro explícito. El backend traduce fielmente el IR actual.
- `CALLMET` es estático: el prefijo `objeto.clase.metodo` o la clase receptor
  eligen el label; no hay despacho dinámico.
- El retorno de tipo clase no está disponible en la gramática actual (el
  parser no acepta `clase function f(...)`), por lo que no se genera slot de
  retorno para objetos. El retorno de `ulongint`/`doublef` sí está cubierto.

## 5. Objetos, atributos y COPY (Temas 25, 28, 31)

### Layout de objeto

No se reserva un bloque contiguo por objeto. La Tabla de Símbolos materializa
un slot global por cada atributo escalar de cada objeto, siguiendo el orden
base-primero de la jerarquía:

- atributo propio de `ca` en `a1`: `a1_x_main` (`DD`/`DQ` según tipo);
- atributo heredado declarado en `padre` para `hija h1`:
  `h1_padre_x_main`;
- atributo propio de `hija`: `h1_y_main`.

El nombre calificado del atributo (`objeto.atr` u `objeto.declarante.atr`) y su
ámbito identifican la entrada en la TS. Un objeto es, a efectos de ASM, el
conjunto de sus slots; no hay puntero ni descriptor en tiempo de ejecución.

### Lectura/escritura de atributos

`ReferenciaAtributo` (`obj.atr`, `obj.clase.atr`) se resuelve a la entrada
materializada usando `texto` y el ámbito del objeto:

1. búsqueda exacta `texto` + ámbito en la TS;
2. búsqueda por `uso == ATRIBUTO`, lexema calificado y ámbito.

Así, `a1.x := 4$ul` escribe `a1_x_main` y `a1.padre.y := 7$ul` escribe
`a1_padre_y_main`, con `mov` o `fld/fstp` según el tipo.

### COPY

`COPY destino, origen` copia posicionalmente los atributos efectivos de ambas
clases:

- se obtiene la lista efectiva base-primero de cada clase
  (`atributosEfectivosConDeclarante`);
- se exige la misma cantidad de atributos (la compatibilidad semántica ya fue
  validada por `clasesCompatibles`);
- para cada posición `i`, se copia `origen[i]` a `destino[i]` respetando el tipo
  del atributo destino.

Ejemplo de `test_tp_4/06_copy_objetos.txt` (misma estructura con nombres
distintos: `hija { hereda x; doublef y }` ← `cb { ulongint a; doublef b }`):

```asm
; COPY h1:main := b1:main (copia posicional)
    mov eax, b1_a_main
    mov h1_padre_x_main, eax
    fld b1_b_main
    fstp h1_y_main
```

Si falta un slot materializado, se emite un comentario de diagnóstico y se
continúa con el resto; no se genera una referencia a un símbolo inexistente.

### Herencia múltiple

El orden efectivo recorre las bases en el orden declarado en `extends` y luego
los atributos propios; un atributo propio con el mismo nombre reemplaza al
heredado sin mover su posición. El prefijo de acceso debe ser la clase que
declara el atributo, tal como valida el frontend (por ejemplo `a1.abuelo.y`,
no `a1.padre.y` si `y` se declaró en `abuelo`).

### Limitaciones de objetos

- No hay atributos de tipo objeto anidado: el frontend rechaza un prefijo que
  no sea una clase heredada (`b1.inner.x` no es válido).
- No hay `pout` de objetos ni retorno de objetos por función.
- `COPY` no hace copia profunda: solo copia los slots escalares materializados.
  Al no existir objetos anidados, en la práctica es una copia completa.
- No se generan descriptores de tamaño ni `STRUCT` de MASM: el layout es la
  lista de slots de la TS.

## 6. Variables auxiliares

- `ResultadoTerceto` decide qué tercetos producen valor y su tipo.
- Cada resultado con tipo ASM conocido se materializa como `@auxN DD ?` o
  `@auxN DQ ?` en `.DATA`.
- Los comparadores usan `@auxN` (`ulongint`) para el flag 0/1; los resultados
  aritméticos y de llamada usan el tipo correspondiente.
- No se crean entradas `@auxN` en la TS: son exclusivamente del backend, por lo
  que la TS y los tercetos de TP3 no cambian.

## 7. Otros detalles de traducción

- Aritmética entera: `add/sub/imul`; división con `xor edx, edx` + `div`
  (sin signo, coherente con `ulongint`).
- Aritmética `doublef`: `fld` + `fadd/fsub/fmul/fdiv` + `fstp`.
- `todf`: `fild` + `fstp`. `fild` interpreta el entero como **con signo**, por
  lo que valores `ulongint > 2^31-1` no se convierten correctamente. Queda como
  limitación documentada (el Tema 33 no asigna un chequeo runtime específico y
  el Grupo 43 no incluye ese caso).
- `:=` en expresión: además de escribir el destino, copia el valor al `@auxN`
  del terceto, porque el resultado puede usarse en la expresión contenedora.
- Asignación `doublef` en expresión: `fld src; fst dst; fstp aux` para dejar el
  valor en ambos lados.

## 8. Verificación

- Compilación: `javac -encoding UTF-8 -d /tmp/tp4p4b *.java "acciones semanticas"/*.java otras_acciones/*.java` (OK).
- Casos dirigidos: `test_tp_4/01_*.txt` a `11_*.txt` (ver `PROMPT-4.md`).
- Script: `bash test_tp_4/probar_prompt4.sh` (todas las verificaciones OK).
- Regresión TP3: se compiló el estado `HEAD` (P3) en `/tmp` y se comparó la
  salida de consola de los **248** archivos `.txt` de `test/` y `test_tp_4/`
  contra el estado P4. No hubo diferencias de stdout (fuente, tokens,
  estructuras, errores, TS y tercetos).

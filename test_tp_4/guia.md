# Guía de prueba — Prompt 2 de TP4 (GeneradorAssembler esqueleto)

Este documento explica cómo verificar que el **Prompt 2 de TP4** funciona correctamente.

## Qué debe hacer el Prompt 2

Al ejecutar el compilador sobre un archivo fuente `.txt`, debe generarse un archivo `.asm` del mismo nombre (extensión cambiada a `.asm`) con:

1. **Encabezado MASM32 32-bit**:
   - `.386`
   - `.model flat, stdcall`
   - `option casemap:none`
   - `include \masm32\include\masm32rt.inc`

2. **Sección `.DATA`**:
   - Variables `ulongint` → `DD ?`
   - Variables `doublef` → `DQ ?`
   - Constantes numéricas → `DD <valor>`
   - Cadenas → `DB "contenido",0`
   - Temporales `@auxN` con tipo conocido → `DD ?` / `DQ ?`

3. **Sección `.CODE` esqueleto**:
   - `start:`
   - `invoke ExitProcess, 0`
   - `end start`
   - Comentario `TODO` para traducción real de tercetos (Prompt 3)

## Qué NO debe cambiar

- La salida por consola de TP3 (fuente numerado, tokens, estructuras, errores, TS, tercetos) debe ser idéntica a antes del Prompt 2.
- No se rompe ningún caso existente de TP3.

## Cómo probar

Desde la raíz del proyecto:

```bash
# 1. Compilar
javac -encoding UTF-8 -d /tmp/tp4p2 *.java "acciones semanticas"/*.java otras_acciones/*.java

# 2. Ejecutar el script de prueba
bash test_tp_4/probar_prompt2.sh
```

O ejecutar manualmente:

```bash
java -cp /tmp/tp4p2 Main test/tp2/generales/00_programa_valido.txt
cat test/tp2/generales/00_programa_valido.asm
```

## Resultados esperados

| Elemento | Resultado esperado |
|---|---|
| Archivo `.asm` generado | Sí, al lado del archivo fuente |
| Encabezado `.386` | Presente |
| `.model flat, stdcall` | Presente |
| `option casemap:none` | Presente |
| `include \masm32\include\masm32rt.inc` | Presente |
| Sección `.DATA` | Presente con símbolos |
| Sección `.CODE` | Presente con `start:` y `end start` |
| `invoke ExitProcess, 0` | Presente |
| Salida por consola | Sin errores, tercetos/TS iguales a TP3 |

## Casos de prueba sugeridos

- `test/tp2/generales/00_programa_valido.txt` — programa mínimo con variables
- Cualquier archivo con `ulongint`, `doublef`, cadena y `pout`

## Archivos involucrados

- `GeneradorAssembler.java` (nuevo)
- `Compilador.java` (modificado: invoca al generador)
- `docs/TP4/DECISIONES_P2.md` (decisiones de representación)

---

# Guía de prueba — Prompt 4 de TP4 (control de flujo, pout, funciones, objetos)

## Qué debe hacer el Prompt 4

Además del encabezado y `.DATA` del Prompt 2 y de la aritmética/asignación del
Prompt 3, el `.asm` debe traducir:

1. **Control de flujo (Tema 13)**:
   - comparadores enteros con `cmp`/`setcc` y `doublef` con FPU;
   - `BF` → `cmp <cond>, 0` + `je <etiqueta>`;
   - `BI` → `jmp <etiqueta>`;
   - etiquetas `label_N` y anidamiento de `IF/ELSE/END_IF` y `REPEAT-WHILE`.
2. **`pout`**:
   - `ulongint` → `printf` con `@tp4_fmt_uint` (`"%u",10,0`);
   - `doublef` → `printf` con `@tp4_fmt_double` (`"%.20Lf",10,0`);
   - `cadena` → `printf` con `@tp4_fmt_string` (`"%s",10,0`) y `addr`.
3. **Funciones/métodos (Tema 19)**:
   - `PAR` copia real→formal en el orden del IR;
   - `CALL`/`CALLMET` llaman al label ASM del llamado;
   - `ret` guarda en `@tp4_ret_<label>` y el llamador recupera en `@auxN`;
   - prólogo/epílogo con `ebp/edi/esi`; sin recursión/reentrancia.
4. **Objetos/atributos (Temas 25/28/31)**:
   - un slot `.DATA` por atributo y objeto (`a1_x_main`,
     `a1_padre_y_main`);
   - `COPY` copia posicionalmente en orden base-primero;
   - `CLASE/FIN_CLASE` no emiten código.
5. **Epílogo no duplicado**: si la función termina en `ret`, no se agrega otro
   epílogo de seguridad.

## Qué NO debe cambiar

- La salida de TP3 (fuente numerado, tokens, estructuras, errores, TS y
  tercetos) debe ser idéntica.
- No se rompe ningún caso existente.

## Cómo probar

Desde la raíz del proyecto:

```bash
# Suite completa de Prompt 4 (recomendado)
bash test_tp_4/probar_prompt4.sh
```

El script compila, copia los casos de `test_tp_4/[0-9][0-9]_*.txt` a `/tmp`,
verifica que no haya errores, revisa las instrucciones esperadas en cada
`.asm` y comprueba que el caso TP3 de `repeat` conserve sus tercetos.

Para probar un caso puntual:

```bash
javac -encoding UTF-8 -d /tmp/tp4p4 *.java "acciones semanticas"/*.java otras_acciones/*.java
java -cp /tmp/tp4p4 Main test_tp_4/05_atributos_propios_heredados.txt
cat test_tp_4/05_atributos_propios_heredados.asm
```

## Casos incluidos

| Archivo | Tema |
|---|---|
| `01_if_else_anidado.txt` | IF/ELSE/END_IF anidado |
| `02_repeat_while.txt` | REPEAT-WHILE con if/else |
| `03_funciones_orden_parametros.txt` | PAR/CALL/ret y orden del Tema 19 |
| `04_metodo_heredado.txt` | CALLMET y herencia múltiple |
| `05_atributos_propios_heredados.txt` | atributos propios/heredados y doublef |
| `06_copy_objetos.txt` | COPY estructural |
| `07_pout_tipos.txt` | pout uint/double/cadena |
| `08_parametro_clase.txt` | parámetro formal de tipo clase |
| `09_funcion_doublef.txt` | retorno doublef |
| `10_if_doublef.txt` | comparación doublef en if/else |
| `11_funciones_anidadas.txt` | funciones homónimas en scopes anidados |

## Documentación

- `docs/TP4/PROMPT-4.md` — alcance, implementación, pruebas y limitaciones.
- `docs/TP4/DECISIONES_P4.md` — convención de llamada, layout de objetos,
  ejemplos de `.asm` y limitaciones detalladas.

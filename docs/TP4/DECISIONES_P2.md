# Decisiones y cambios — Prompt 2 (TP4, ASM MASM32 32-bit)

## Resumen
Se creó `GeneradorAssembler.java` e integró su invocación en `Compilador.java` para emitir un archivo `.asm` esqueleto MASM32 32-bit con encabezado, sección `.DATA` y sección `.CODE` mínima. No se alteró la salida observable de TP3 (consola, tercetos, TS).

## Archivos nuevos

### `GeneradorAssembler.java`
- Recibe: `List<Terceto>` (desde `GeneradorTercetos.getTercetos()`), `TablaSimbolos`, `Entorno` y `Map<Integer, ResultadoTerceto>` (desde `GeneradorTercetos.getResultados()`).
- Genera el archivo `.asm` con:
  - `.386`, `.model flat, stdcall`, `option casemap:none`, `include \masm32\include\masm32rt.inc`
  - Sección `.DATA` construida desde `TablaSimbolos.colectarSimbolosData(Entorno)`:
    - constantes numéricas → `DD <valor>` (literal normalizado o `0`)
    - cadenas → `DB "<escape>",0`
    - variables/parámetros/atributos escalares declarados → `DD ?` / `DQ ?` según tipo efectivo (`ulongint`/`enum` → `DD`; `doublef` → `DQ`)
  - Temporales `@auxN` con `produceValor=true` y `tipoAsmDato()` no nulo (`DD`/`DQ`) → `?`
  - Sección `.CODE` esqueleto: `start:` con `invoke ExitProcess, 0` y comentario `TODO` para traducción real de tercetos (Prompt 3+).
- Método `escribirArchivo(String ruta)` crea directorios padre si es necesario y escribe en UTF-8.

## Archivos modificados

### `Compilador.java`
- Tras generar tercetos, calcula la ruta del `.asm` (reemplaza la extensión del fuente por `.asm`, o agrega `.asm` si no tiene).
- Instancia `GeneradorAssembler` con los tercetos, TS, entorno y metadata de resultados.
- Llama a `escribirArchivo(ruta)`; si falla, imprime `System.err` y continúa (no rompe la compilación TP3).

## Decisiones de representación ASM-level

| Elemento | Decisión Prompt 2 | Nota |
|---|---|---|
| Encabezado MASM32 | `.386` / `.model flat, stdcall` / `option casemap:none` / `include \masm32\include\masm32rt.inc` | Estándar MASM32 32-bit |
| `ulongint` / enum efectivo | `DD` | 32-bit sin signo |
| `doublef` | `DQ` | 64-bit IEEE |
| `cadena` | `DB "<contenido_escape>",0` | Escape de `"`, `\`, `\n`, `\r`, `\t` |
| Variable/parámetro/atributo escalar no inicializado | `DD ?` / `DQ ?` | valorInicial `"?"` |
| Constante numérica | `DD <literal>` | `Tipos.valorEntero` o `0` si no parseable |
| Temporal `@auxN` productor | `DD ?` / `DQ ?` | derivado de `ResultadoTerceto` |
| Etiquetas IR `label_N` | aún no emitidas en `.CODE` | pendiente Prompt 3 |
| Traducción de tercetos a instrucciones | comentario `TODO` en `.CODE` | pendiente Prompt 3 |

## Verificación realizada
- Compilación Java OK: `javac -encoding UTF-8 -d /tmp/tp4p2 *.java "acciones semanticas"/*.java otras_acciones/*.java`.
- Ejecución sobre `test/tp2/generales/00_programa_valido.txt` genera archivo `.asm` con encabezado, `.DATA` (vars/cadenas/ctes) y `.CODE` mínimo sin errores de consola.

## Riesgos y puntos abiertos para prompts siguientes
1. Traducción de tercetos (`:=`, `+ - * /`, `todf`, `BF/BI`, `pout`, `ret`, `PAR`, `CALL`, `CALLMET`, `COPY`) a instrucciones MASM32: Pendiente (Prompt 3 y 4).
2. Convención de llamada y uso de registros (EAX, EBX, ECX, EDX, FPU): Pendiente.
3. Layout físico de objetos y atributos en memoria: Pendiente (Prompt 4).
4. Chequeos runtime grupo 43 (`b`, `e`, `f`, `h`): Pendiente (Prompt 5).

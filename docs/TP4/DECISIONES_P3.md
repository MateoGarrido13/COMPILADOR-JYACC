# Decisiones y cambios — Prompt 3 (TP4, ASM MASM32 32-bit)

## Resumen
Se implementó la traducción básica de tercetos a instrucciones MASM32 32-bit en `GeneradorAssembler.java`: asignación `:=`, aritmética `+ - * /` y conversión `todf`. Los demás operadores (estructurales, control de flujo, IO, llamadas) se emiten como comentario pendiente para Prompt 4+.

## Archivos modificados

### `GeneradorAssembler.java`
- `emitirCodigo()`: recorre los tercetos en orden (1-based) y traduce:
  - `+` / `-` / `*` / `/`:
    - Entero (`ulongint`): `mov eax, op1; <add|sub|imul|div> eax, op2; mov destino, eax`
    - Flotante (`doublef`): `fld op1; <fadd|fsub|fmul|fdiv> op2; fstp destino`
  - `todf`: `fild origen; fstp destino` (entero a doublef)
  - `:=` sentencia: `mov eax, src; mov dst, eax` (enteros) / `fld src; fstp dst` (doublef, detectado por tipo del destino en TS)
  - `:=` expresión: igual que sentencia, pero el destino recibe el valor del auxiliar `@auxN` del terceto de origen
  - Otros operadores: emitidos como comentario `; (op, op1, op2)`
- `operandoAsm(Object)`: resuelve operandos de terceto a referencias ASM:
  - `EntradaTabla` → `tablaSimbolos.nombreAsmDe()`
  - `Integer` → nombre del auxiliar `@auxN` del terceto referenciado
  - `ReferenciaAtributo` → `texto` (objeto.atributo)
  - `String` → literal tal cual
- `auxPorTerceto`: mapeo número de terceto → nombre de auxiliar (`@auxN`), construido desde `ResultadoTerceto`

## Decisiones de representación ASM-level

| Elemento | Decisión Prompt 3 | Nota |
|---|---|---|
| Enteros (`ulongint`) | Registros `EAX` + instrucciones `mov/add/sub/imul/div` | 32-bit sin signo |
| Flotantes (`doublef`) | FPU `fld/fadd/fsub/fmul/fdiv/fstp` | 64-bit IEEE, pila FPU ST(0) |
| Conversión `todf` | `fild` (entero) + `fstp` (doublef) | De entero a punto flotante |
| Temporales `@auxN` | Materializados en `.DATA` como `DD ?` / `DQ ?` | Referenciados desde `[n]` |
| Asignación `:=` destino `doublef` | Detectado por `tipoDato` de la `EntradaTabla` destino | Usa FPU si es flotante |
| Asignación `:=` destino `ulongint` | Usa `EAX` (mov) | Default para enteros |
| Operadores no implementados | Comentario `; (op, op1, op2)` | Pendiente Prompt 4+ |
| Saltos `BF`/`BI` | No traducidos en Prompt 3 | Pendiente Prompt 4 |

## Verificación realizada
- Compilación Java OK: `javac -encoding UTF-8 -d /tmp/tp4p3 *.java "acciones semanticas"/*.java otras_acciones/*.java`
- Caso de prueba con `ulongint`, `doublef`, `+`, `*`, `todf` y asignaciones: `.asm` generado con instrucciones MASM32 correctas

## Riesgos y puntos abiertos para prompts siguientes
1. División entera `div`: requiere `dx:ax` y manejo de `edx` (no implementado aún)
2. Chequeos runtime (`b`, `e`, `f`, `h`): no traducidos (Prompt 5)
3. Saltos `BF`/`BI` y control de flujo: no traducidos (Prompt 4)
4. Llamadas `PAR`/`CALL`/`CALLMET`/`ret`: no traducidas (Prompt 4)
5. Acceso a atributos `ReferenciaAtributo`: se emite el texto, pero no hay direccionamiento de memoria de objetos (Prompt 4)
6. Asignación estructural `COPY`: no traducida (Prompt 4)

## Cómo testear el Prompt 3

### Script automatizado
Desde la raíz del proyecto:

```bash
bash test_tp_4/probar_prompt3.sh
```

El script:
1. Compila el proyecto con `javac -encoding UTF-8`
2. Crea un caso de prueba con `ulongint`, `doublef`, `+`, `*`, `todf` y asignaciones
3. Ejecuta el compilador sobre el caso
4. Verifica 18 puntos en el archivo `.asm` generado (encabezado, secciones, símbolos, instrucciones `mov`/`add`/`imul`/`fild`/`fstp`/`fld`)

### Prueba manual
Crear un archivo fuente, por ejemplo `/tmp/prog_p3.txt`:

```text
prog
ulongint x, y, z;
doublef a;
begin
    x := 5$ul;
    y := x + 3$ul;
    z := x * y;
    a := todf(x);
end
```

Ejecutar:

```bash
javac -encoding UTF-8 -d /tmp/build *.java "acciones semanticas"/*.java otras_acciones/*.java
java -cp /tmp/build Main /tmp/prog_p3.txt
cat /tmp/prog_p3.asm
```

### Resultados esperados en el `.asm`

| Elemento | Qué buscar |
|---|---|
| Encabezado | `.386`, `.model flat, stdcall`, `option casemap:none`, `include \masm32\include\masm32rt.inc` |
| `.DATA` | `x_main DD ?`, `a_main DQ ?`, `c_5_ul_ul DD 5`, `@aux3 DD ?`, `@aux7 DQ ?` |
| `.CODE` | `start:`, `invoke ExitProcess, 0`, `end start` |
| Aritmética entera | `mov eax, c_5_ul_ul`, `add eax, c_3_ul_ul`, `imul eax, y_main`, `mov @aux3, eax` |
| Conversión `todf` | `fild x_main`, `fstp @aux7` |
| Asignación doublef | `fld @aux7`, `fstp a_main` |
| Salida por consola | Sin mensajes `Error:` |

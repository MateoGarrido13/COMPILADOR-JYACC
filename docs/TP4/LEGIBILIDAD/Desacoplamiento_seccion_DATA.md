# Separación de la sección .DATA en métodos privados por categoría — GeneradorAssembler.java

## Resumen

El método `emitirData()` realizaba en un solo cuerpo la emisión de siete categorías distintas de símbolos para la sección `.DATA` del archivo MASM generado. Se separó en métodos privados de un solo propósito, cada uno responsable de una categoría de símbolo, mejorando la cohesión y reduciendo el acoplamiento entre la lógica de recolectar símbolos y la lógica de emitir instrucciones ASM.

## Archivo .java modificado

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | `emitirData()` se redujo a un orquestador que llama a siete métodos privados. Se agregaron siete métodos privados: `emitirSimbolosDeTablaDeSimbolos()`, `emitirReceptoresThis()`, `emitirTemporalesAux()`, `emitirMarcadorEstadoFPU()`, `emitirFormatosPout()`, `emitirMensajesErrorRuntime()` y `emitirCadenasErrorGeneradas()`. |

## BEFORE | AFTER

### GeneradorAssembler.java — método `emitirData()`

**BEFORE** (un solo cuerpo de ~65 líneas con siete categorías mezcladas):

```java
    private void emitirData() {
        salida.append(".data\n");
        List<TablaSimbolos.SimboloDataAsm> simbolos = tablaSimbolos.colectarSimbolosData(entorno);
        Set<String> nombresEnData = new HashSet<>();
        for (TablaSimbolos.SimboloDataAsm s : simbolos) {
            // 4c: parametros y variables locales viven en el stack frame.
            if ("dato".equals(s.categoria) && esSimboloDeStack(s.entrada)) {
                continue;
            }
            salida.append("    ").append(s.nombreAsm).append(" ")
                  .append(s.tipoAsm).append(" ")
                  .append(valorInicial(s)).append("\n");
            nombresEnData.add(s.nombreAsm);
        }
        // Receptores de CALLMET: solo un puntero mientras 4d no defina el layout.
        for (String nombre : objetosThis) {
            if (!nombresEnData.contains(nombre)) {
                salida.append("    ").append(nombre).append(" DD ?")
                      .append(" ; this (layout en 4d)\n");
            }
        }
        // Temporales implicitos @auxN con tipo ASM conocido (DD/DQ)
        for (ResultadoTerceto r : resultados.values()) {
            if (r.produceValor() && r.tipoAsmDato() != null) {
                salida.append("    ").append(r.nombreAuxiliar()).append(" ")
                      .append(r.tipoAsmDato()).append(" ?\n");
            }
        }
        // Palabra de estado de la FPU, solo si hay comparaciones doublef.
        if (usaFpuStatus) {
            salida.append("    ").append(FPU_STATUS).append(" DW ?\n");
        }
        // Formatos de impresion, solo los que necesitan los tercetos pout.
        if (usaFmtUlongint) {
            salida.append("    ").append(FMT_ULONGINT).append(" DB \"%u\",0\n");
        }
        ...
        salida.append("\n");
    }
```

**AFTER** (orquestador de 10 líneas):

```java
    private void emitirData() {
        salida.append(".data\n");
        Set<String> nombresEnData = emitirSimbolosDeTablaDeSimbolos();
        emitirReceptoresThis(nombresEnData);
        emitirTemporalesAux();
        emitirMarcadorEstadoFPU();
        emitirFormatosPout();
        emitirMensajesErrorRuntime();
        emitirCadenasErrorGeneradas();
        salida.append("\n");
    }
```

### Métodos privados creados

| Método | Categoría de símbolo emitida | Condición de activación |
|--------|------------------------------|-------------------------|
| `emitirSimbolosDeTablaDeSimbolos()` | Variables, parámetros, atributos, constantes y cadenas de la TS (excepto locales de stack) | Siempre |
| `emitirReceptoresThis(Set)` | Receptores de `CALLMET` (`DD ?`) | Si hay objetos receptor y no están ya en `.DATA` |
| `emitirTemporalesAux()` | Temporales `@auxN` (`DD`/`DQ`) | Si producen valor y tienen tipo ASM conocido |
| `emitirMarcadorEstadoFPU()` | `@fpu_status` (`DW ?`) | Solo si `usaFpuStatus` |
| `emitirFormatosPout()` | `fmt_ulongint`, `fmt_doublef`, `fmt_cadena` | Solo los que usan los tercetos `pout` |
| `emitirMensajesErrorRuntime()` | Mensajes de error del Prompt 5 | Solo los chequeos activos (5.b, 5.e, 5.f, 5.h) |
| `emitirCadenasErrorGeneradas()` | Cadenas de error detectadas en generación de ASM | Si hay cadenas de error pendientes |

## Utilidad del cambio

1. **Alta cohesión:** cada método privado tiene una sola responsabilidad: emitir una categoría de símbolo en `.DATA`. No hay mezclas de lógica de tabla de símbolos con lógica de formatos de impresión ni con mensajes de error runtime.

2. **Bajo acoplamiento:** un cambio en los mensajes de error del Prompt 5 (por ejemplo, añadir el chequeo 5.f) solo toca `emitirMensajesErrorRuntime()`. Un cambio en los formatos de `pout` solo toca `emitirFormatosPout()`. Los demás métodos no se ven afectados.

3. **Legibilidad:** `emitirData()` se convierte en un índice legible de qué contiene la sección `.DATA`, sin necesidad de leer decenas de líneas de lógica de emisión interleaved.

4. **Mantenibilidad:** si en el futuro se añade una nueva categoría de símbolo (por ejemplo, buffers de runtime para chequeos 5.e/5.f), solo se añade un nuevo método privado y una línea en `emitirData()`, sin tocar el resto.

5. **Restricción del TP4 preservada:** el orden de emisión en `.DATA` es idéntico al anterior, por lo que los archivos `.asm` generados son bit-a-bit iguales.

## Verificación realizada

- Compilación del proyecto: `javac -encoding UTF-8 -d /tmp/tp4p5b_build *.java "acciones semanticas"/*.java otras_acciones/*.java` → OK.
- Ejecución de `bash test_tp_4/probar_prompt5_b.sh` → EXIT: 0, todas las verificaciones OK.
- Regresiones: `probar_prompt5_0.sh`, `probar_prompt4e.sh`, `probar_prompt4d.sh`, `probar_prompt4c.sh`, `probar_prompt4b.sh`, `probar_prompt4a.sh`, `probar_prompt3.sh`, `probar_prompt2.sh` → todas OK.
- Los archivos `.asm` generados son idénticos a los de antes del cambio (el refactor no altera la salida del compilador).

# Desacoplamiento de copias por tipo en asignaciones — GeneradorAssembler.java

## Resumen

Se eliminó la duplicación de la lógica que elige entre instrucciones de punto flotante (`fld`/`fstp`) y de entero (`mov eax`/`mov`) al copiar un valor en una asignación. Se creó un método privado `emitirCopiaPorTipo(String tipo, String origen, String destino)` que concentra esa decisión en un solo lugar, y se reutiliza desde `emitirAsignacionExpresion()` y `emitirAsignacionSentencia()`.

## Archivos .java modificados

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | Método nuevo `emitirCopiaPorTipo(String, String, String)`. Se reemplaza el bloque `if (flotante) { fld/fstp } else { mov eax/mov }` en `emitirAsignacionExpresion()` y `emitirAsignacionSentencia()` por una llamada a `emitirCopiaPorTipo(...)`. |

## BEFORE | AFTER

### GeneradorAssembler.java — bloque duplicado en `emitirAsignacionExpresion()`

**BEFORE**:

```java
        boolean flotante = esFlotante(tipoResultado);

        if (flotante) {
            // El origen ya es un auxiliar DQ; copiarlo al destino
            salida.append("    fld ").append(src).append("\n");
            salida.append("    fstp ").append(dst).append("\n");
        } else {
            // Copiar entero de 32 bits
            salida.append("    mov eax, ").append(src).append("\n");
            salida.append("    mov ").append(dst).append(", eax\n");
        }
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
```

**AFTER**:

```java
        emitirCopiaPorTipo(tipoResultado, src, dst);
    }

    private void emitirAsignacionSentencia(Object destino, Object origen) {
```

### GeneradorAssembler.java — bloque duplicado en `emitirAsignacionSentencia()`

**BEFORE**:

```java
        if (flotante) {
            salida.append("    fld ").append(src).append("\n");
            salida.append("    fstp ").append(dst).append("\n");
        } else {
            salida.append("    mov eax, ").append(src).append("\n");
            salida.append("    mov ").append(dst).append(", eax\n");
        }
    }

    /* ---------- Control de flujo y comparaciones (Prompt 4a) ---------- */
```

**AFTER**:

```java
        emitirCopiaPorTipo(flotante ? Tipos.DOUBLEF : Tipos.ULONGINT, src, dst);
    }

    /* ---------- Control de flujo y comparaciones (Prompt 4a) ---------- */
```

### GeneradorAssembler.java — método nuevo (insertado antes de `emitirAsignacionExpresion()`)

**AFTER** (no existía antes):

```java
    private void emitirCopiaPorTipo(String tipo, String origen, String destino) {
        if (esFlotante(tipo)) {
            // Copiar doublef: cargar en FPU y guardar en destino DQ
            salida.append("    fld ").append(origen).append("\n");
            salida.append("    fstp ").append(destino).append("\n");
        } else {
            // Copiar ulongint: cargar en EAX y guardar en destino DD
            salida.append("    mov eax, ").append(origen).append("\n");
            salida.append("    mov ").append(destino).append(", eax\n");
        }
    }
```

## Utilidad del cambio

1. **Alta cohesión:** la decisión de qué instrucciones ASM usar para copiar un valor según su tipo (`doublef` → FPU, `ulongint` → registro general EAX) vive en un solo método. Cualquier cambio futuro en esa convención (por ejemplo, añadir un tercer tipo o cambiar la forma de copiar `doublef`) se hace en un solo sitio.

2. **Bajo acoplamiento:** `emitirAsignacionExpresion()` y `emitirAsignacionSentencia()` ya no necesitan saber si el tipo es flotante o entero; solo delegan en `emitirCopiaPorTipo(...)`. Esto reduce el acoplamiento entre la lógica de determinación de tipos y la lógica de emisión de instrucciones.

3. **Eliminación de código duplicado:** el bloque de 6 líneas (`fld/fstp` vs `mov eax/mov`) aparecía dos veces idénticas. Ahora aparece una sola vez. Esto reduce la superficie de cambio y el riesgo de inconsistencia (por ejemplo, si alguien modifica un bloque y olvida modificar el otro).

4. **Legibilidad:** los métodos emisores de asignación se vuelven más cortos y declarativos: en lugar de ver la mecánica de copiar bytes, ven la intención "copiar por tipo".

5. **Restricción del TP4 preservada:** el método sigue usando `EAX` para enteros (registro del procesador) y `fld`/`fstp` para `doublef` (coprocesador 80x87), cumpliendo la convención del proyecto.

## Verificación realizada

- Compilación del proyecto: `javac -encoding UTF-8 -d /tmp/tp4p5b_build *.java "acciones semanticas"/*.java otras_acciones/*.java` → OK.
- Ejecución de `bash test_tp_4/probar_prompt5_b.sh` → EXIT: 0, todas las verificaciones OK.
- Regresiones: `probar_prompt5_0.sh`, `probar_prompt4e.sh`, `probar_prompt4d.sh`, `probar_prompt4c.sh`, `probar_prompt4b.sh`, `probar_prompt4a.sh`, `probar_prompt3.sh`, `probar_prompt2.sh` → todas OK.
- Los archivos `.asm` generados son idénticos a los de antes del cambio (el refactor no altera la salida del compilador).

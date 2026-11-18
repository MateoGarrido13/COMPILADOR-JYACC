# Desacoplamiento de generación de etiquetas — GeneradorAssembler.java

## Resumen

Se desacopló la generación de etiquetas de funciones y métodos (`fn_*` / `mt_*`) y el saneado de sus nombres de la instancia de `GeneradorAssembler`, convirtiendo los métodos `nombreEtiqueta()` y `sanitizarEtiqueta()` en métodos `static`.

## Archivos .java modificados

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | `nombreEtiqueta(Entorno.FuncionDeclarada)` y `sanitizarEtiqueta(String)` pasaron de ser métodos de instancia (`private`) a métodos de clase (`private static`). Todas las llamadas internas se actualizaron automáticamente (son llamadas estáticas dentro de la misma clase). |

## BEFORE | AFTER

### GeneradorAssembler.java — declaración de métodos

**BEFORE** (métodos de instancia, acoplados a la instancia aunque no usaban ningún campo):

```java
private String nombreEtiqueta(Entorno.FuncionDeclarada f) {
    String clave = f.entrada != null && f.entrada.lexema != null
            ? f.entrada.lexema : f.clave();
    return (f.esMetodo ? "mt_" : "fn_") + sanitizarEtiqueta(clave);
}

private String sanitizarEtiqueta(String texto) {
    if (texto == null) {
        return "anon";
    }
    ...
    return Character.isDigit(limpio.charAt(0)) ? "s" + limpio : limpio;
}
```

**AFTER** (métodos estáticos, independientes de la instancia):

```java
private static String nombreEtiqueta(Entorno.FuncionDeclarada f) {
    String clave = f.entrada != null && f.entrada.lexema != null
            ? f.entrada.lexema : f.clave();
    return (f.esMetodo ? "mt_" : "fn_") + sanitizarEtiqueta(clave);
}

private static String sanitizarEtiqueta(String texto) {
    if (texto == null) {
        return "anon";
    }
    ...
    return Character.isDigit(limpio.charAt(0)) ? "s" + limpio : limpio;
}
```

### Sitios de llamada (sin cambios funcionales, solo contexto de llamada estática)

Antes y después las llamadas dentro de la misma clase siguen igual sintácticamente (Java permite llamar a métodos estáticos desde contexto de instancia), pero semánticamente ahora no dependen de `this`:

```java
// Registro de etiquetas al preparar declaraciones
etiquetaPorDeclaracion.put(f, nombreEtiqueta(f));

// Generación de etiqueta de prólogo cuando no hay declaración precargada
etiqueta = ("METODO".equals(operador) ? "mt_" : "fn_")
        + sanitizarEtiqueta(textoDeOperando(nombre));

// Resolución de etiqueta en CALL / CALLMET
etiqueta = (esMetodo ? "mt_" : "fn_") + sanitizarEtiqueta(clave);
```

## Utilidad del cambio

1. **Alta cohesión:** `nombreEtiqueta` y `sanitizarEtiqueta` son operaciones puras sobre strings: dada una entrada (clave o texto), producen una etiqueta ASM válida. No leen ni modifican ningún campo de instancia (`funcionActual`, `slotStackPorSimbolo`, `auxPorTerceto`, etc.). Al declararlos `static` se hace explícito que no pertenecen al estado del generador.

2. **Bajo acoplamiento:** al no depender de la instancia, estos métodos pueden ser reutilizados o probados de forma aislada sin necesidad de construir un `GeneradorAssembler` completo con `TablaSimbolos`, `Entorno`, `List<Terceto>` y `Map<Integer, ResultadoTerceto>`. Cualquier cambio futuro en la lógica de sanitización (por ejemplo, soportar más caracteres o cambiar el prefijo de anónimos) se aislada en un método estático sin riesgo de efectos colaterales en el estado de generación.

3. **Claridad de diseño:** cualquier desarrollador que lea el código ve de inmediato que `sanitizarEtiqueta` es una utilidad de transformación de cadenas, no una operación que consulta o modifica el estado interno del generador de código assembler.

4. **Mantenibilidad:** si en el futuro se decide mover estas utilidades a una clase aparte (por ejemplo, `UtilidadesAsm` o `EtiquetasAsm`), el refactor es trivial porque ya no dependen de la instancia de `GeneradorAssembler`.

## Verificación realizada

- Compilación del proyecto: `javac -encoding UTF-8 -d /tmp/tp4p5b_build *.java "acciones semanticas"/*.java otras_acciones/*.java` → OK.
- Ejecución de `bash test_tp_4/probar_prompt5_b.sh` → EXIT: 0, todas las verificaciones OK.
- Regresiones: `probar_prompt5_0.sh`, `probar_prompt4e.sh`, `probar_prompt4d.sh`, `probar_prompt4c.sh`, `probar_prompt4b.sh`, `probar_prompt4a.sh`, `probar_prompt3.sh`, `probar_prompt2.sh` → todas OK.
- Los archivos `.asm` generados son idénticos a los de antes del cambio (el refactor no altera la salida del compilador).

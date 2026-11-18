# Decisiones y cambios — Paquete 5.0 (TP4, Grupo 43, MASM32 32-bit)

> **Actualización de auditoría** (ver `ESTADO_RUNTIME_P5.md`): este documento refleja el estado original del commit `77b2963`. Dos afirmaciones quedaron desactualizadas: (1) `emitirGuardia()` existe pero **no tiene llamadas reales**; los emisores escriben el salto directamente; (2) los labels de fallo se emitían sin un `jmp` previo, por lo que el flujo normal caía en ellos. La corrección **B1** agregó `jmp fin_normal` antes del bloque y `fin_normal:` después. Los booleanos `usaMsg*` se calculan recién en `calcularChequeosRuntime()` (5.b/5.e/5.f/5.h), no en 5.0.

## Resumen

El Paquete 5.0 es la **infraestructura común de chequeos runtime** para el Prompt 5. Solo proviene los elementos que los paquetes 5.b/5.e/5.f/5.h reutilizarán:

* **Mensajes de error en `.DATA`**: cuatro cadenas terminadas en 0, una por chequeo según las condiciones del Grupo 43:
  - `b`: overflow en sumas de enteros `ulongint`
  - `e`: overflow en productos de punto flotante `doublef`
  - `f`: resultados negativos en restas de enteros sin signo `ulongint`
  - `h`: asignación inválida a variables de tipo enumeración
* **Labels de fallo en `.CODE`**: cuatro labels (`error_overflow_suma`, `error_overflow_float`, `error_negativo_resta`, `error_enum_invalido`) que imprimen su mensaje e invocan `invoke ExitProcess, <codigo>` (códigos 1, 2, 3, 4 respectivamente). Se emiten solo si son necesarios.
* **Helper `emitirGuardia(condicionSalto, labelError)`**: emite un salto condicional MASM (`je`, `jne`, `jc`, `jb`, etc.) al label de fallo correspondiente. Los paquetes 5.b/5.e/5.f/5.h lo usarán para insertar guardas antes/después de operaciones.

**Ningún chequeo está activo aún.** El Paquete 5.0 es puramente infraestructura: no instrumenta suma, resta, multiplicación ni asignación. Esa labor le corresponde a los paquetes 5.b, 5.e, 5.f y 5.h.

No se modificó la salida observable de TP3 (fuente numerado, tokens, estructuras, errores, TS, tercetos). Un programa sin chequeos activos genera un `.asm` idéntico al de antes (salvo que ahora puede contener labels de fallo si algún paquete posterior los activa).

## Archivos modificados / creados

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | Constantes de mensajes de error (`MSG_OVERFLOW_SUMA`, `MSG_OVERFLOW_FLOAT`, `MSG_NEGATIVO_RESTA`, `MSG_ENUM_INVALIDO`), booleanos de activación (`usaMsg*`), emisión de mensajes en `.DATA` (vía `emitirData()`), emisión de labels de fallo en `.CODE` (al final de `emitirCodigo()`, antes de `invoke ExitProcess, 0`), método helper `emitirGuardia(condicionSalto, labelError)`. |
| `docs/TP4/DECISIONES_5.0.md` | Este documento (creado en este prompt). |
| `test_tp_4/Runtime chequeos/01_sin_chequeos_activos.txt` | Caso de prueba nuevo (creado en este prompt). |
| `test_tp_4/Runtime chequeos/01_sin_chequeos_activos.asm` | Snapshot generado del caso de prueba (creado en este prompt). |
| `test_tp_4/probar_prompt5_0.sh` | Script de verificación del Paquete 5.0 (creado en este prompt). |

## Contrato de emisión

### Mensajes de error (`.DATA`)

| Simbolo ASM | Contenido | Chequeo que lo activa | Código de salida |
|-------------|-----------|----------------------|------------------|
| `msg_overflow_suma` | `"Error: overflow en suma de ulongint",0` | 5.b | 1 |
| `msg_overflow_float` | `"Error: overflow en producto de doublef",0` | 5.e | 2 |
| `msg_negativo_resta` | `"Error: resultado negativo en resta de ulongint",0` | 5.f | 3 |
| `msg_enum_invalido` | `"Error: asignacion invalida a variable de tipo enumeracion",0` | 5.h | 4 |

Cada símbolo se emite con `DB "...",0` solo si su booleano `usaMsg*` está activo.

### Labels de fallo (`.CODE`)

| Label | Secuencia emitida | Chequeo que lo activa |
|-------|-------------------|----------------------|
| `error_overflow_suma:` | `invoke crt_printf, offset msg_overflow_suma` + `invoke ExitProcess, 1` | 5.b |
| `error_overflow_float:` | `invoke crt_printf, offset msg_overflow_float` + `invoke ExitProcess, 2` | 5.e |
| `error_negativo_resta:` | `invoke crt_printf, offset msg_negativo_resta` + `invoke ExitProcess, 3` | 5.f |
| `error_enum_invalido:` | `invoke crt_printf, offset msg_enum_invalido` + `invoke ExitProcess, 4` | 5.h |

Los labels se emiten al final de `.CODE`, antes del `invoke ExitProcess, 0` de salida normal.

### Helper `emitirGuardia`

```java
private void emitirGuardia(String condicionSalto, String labelError) {
    salida.append("    ").append(condicionSalto).append(" ").append(labelError).append("\n");
}
```

Emite una instrucción de salto condicional MASM (ej. `    jc error_overflow_suma`). La condición específica (`jc`, `jb`, `je`, `jne`, etc.) la decide el llamante según el tipo de comparación previa (FPU con `SAHF`, enteros con `CMP`/`SUB`, etc.).

## Caso de prueba

| Archivo | Qué valida |
|---------|------------|
| `test_tp_4/Runtime chequeos/01_sin_chequeos_activos.txt` | Programa simple (`ulongint x, y; begin x:=10; y:=20; pout(x); pout(y); end`) sin chequeos activos. El `.asm` generado NO debe contener labels de fallo ni mensajes de error. |

## Resultados esperados

Ejecutar:

```bash
bash test_tp_4/probar_prompt5_0.sh
```

- **Código de salida**: `0`.
- **Compilación Java**: exitosa sin errores.
- **Caso 01**: compila sin errores (`Error:` ausente en la salida).
- **Verificaciones `.asm`**: todos los patrones `verificar` / `no_verificar` deben reportar `OK`. En particular, deben faltar `error_overflow_suma:`, `error_overflow_float:`, `error_negativo_resta:`, `error_enum_invalido:` y los cuatro mensajes de error.
- **Regresiones**: los scripts `probar_prompt4e.sh`, `probar_prompt4d.sh`, `probar_prompt4c.sh`, `probar_prompt4b.sh`, `probar_prompt4a.sh`, `probar_prompt3.sh`, `probar_prompt2.sh` deben reportar `OK`.

## Riesgos abiertos

1. **Códigos de salida de `ExitProcess`**: los valores 1, 2, 3, 4 son provisionales; la cátedra/Grupo 43 puede exigir otros. Ajustar si hay un contrato definido.
2. **Ningún chequeo activo**: el Paquete 5.0 por sí solo no detecta errores. Solo activa la infraestructura que consumirán 5.b/5.e/5.f/5.h.
3. **Idioma de los mensajes**: los mensajes están en español; revisar si la cátedra requiere inglés u otro idioma.

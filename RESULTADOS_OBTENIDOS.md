# RESULTADOS_OBTENIDOS — TP4 (Prompt 4d y regresiones)

Repositorio: `/home/mateo/Documentos/COMPILADORES_E_INTERPRETES/primer_entrega_compiladores_2026_grupo43/COMPILADOR-GRUPO43`

Ejecutado: `bash test_tp_4/probar_prompt4d.sh` (que internamente corre las regresiones de 4c, 4b, 4a, 3 y 2), además de cada script de regresión de forma individual.

## Resumen ejecutivo

**Estado general: TODAS LAS PRUEBAS PASAN.**

Se ejecutaron 6 suites de prueba del TP4 y todas finalizaron con código de salida `0` y sin verificaciones en estado `FALLA`:

| Resultado | Cantidad |
|---|---|
| Scripts ejecutados | 6 |
| Scripts con código de salida 0 (éxito) | 6 |
| Verificaciones OK | 234 |
| Verificaciones FALLA | 0 |
| Fallos en ejecución | Ninguno |

El prompt 4d (objetos y atributos, layout y COPY) pasó completamente, incluyendo sus 5 regresiones automáticas (4c, 4b, 4a, 3, 2). Cada regresión ejecutada de forma individual también pasó en verde. No se observó ninguna regresión introducida por el cambio del Prompt 4d.

## Resultados por prueba

| # | Script | Código de salida | Verificaciones OK | Verificaciones FALLA | Observaciones |
|---|---|---|---|---|---|
| 1 | `test_tp_4/probar_prompt4d.sh` | 0 | 34 | 0 | Objetos y atributos: 26 verificaciones de acceso/escritura/COPY de atributos (incluida herencia), más compilación, ejecución de 4 casos y 5 regresiones internas. |
| 2 | `test_tp_4/probar_prompt4c.sh` | 0 | 82 | 0 | Funciones y métodos: convención de llamada, frames EBP, parámetros en pila, retorno EAX/ST(0), PAR, llamadas anidadas, métodos con `this`, herencia estática, locales en stack, 8 casos sin `CALL`/`ret`/`FUNCION` comentados, más 4 regresiones internas. |
| 3 | `test_tp_4/probar_prompt4b.sh` | 0 | 45 | 0 | `pout`: formatos en `.DATA`, constantes, variables, `@auxN`, doublef, cadenas, control de flujo, atributos y pout dentro de funciones; 11 casos sin `pout` comentado. |
| 4 | `test_tp_4/probar_prompt4a.sh` | 0 | 35 | 0 | Control de flujo: if/else con `>` (SETcc, movzx, Jcc inverso, jmp), 6 comparadores (seta/setb/setae/setbe/sete/setne y saltos condicionales), repeat anidado y condición `doublef` (FLD/FCOMP/FSTSW/SAHF/jbe). |
| 5 | `test_tp_4/probar_prompt3.sh` | 0 | 24 | 0 | Traducción a `.CODE`: encabezado `.386`, modelo flat stdcall, secciones `.DATA`/`.CODE`, `start`, `ExitProcess`, `end start`, variables DD/DQ, temporales `@aux3`/`@aux7`, instrucciones enteras y de punto flotante. |
| 6 | `test_tp_4/probar_prompt2.sh` | 0 | 14 | 0 | Generador Assembler esqueleto: `.asm` generado con encabezado, modelo, casemap:none, include, secciones `.DATA`/`.CODE`, `start`, `ExitProcess`, `end start`, sin errores en consola. |

**Total: 234 verificaciones OK, 0 FALLA.**

## Riesgos

Riesgos abiertos listados en `docs/TP4/DECISIONES_P4c.md` y `docs/TP4/DECISIONES_P4d.md`, más los observados en esta ejecución:

### Desde docs/TP4/DECISIONES_P4c.md

1. **MASM real no verificado**: el entorno solo valida texto `.asm` y compilación Java. Pendiente revisar en Windows `mov eax, dword ptr [a_main+4]`, `lea eax, [ebp-4]`, `fld qword ptr [ebp+8]` e `INVOKE crt_printf, …, qword ptr [ebp+8]`.
2. **Convención mixta**: `.model flat, stdcall` con limpieza estilo cdecl es válida mientras las funciones internas no se declaren `PROC`. Si se usa `PROTO` en el futuro, hay que declararlas `C` o pasar a `ret n`.
3. **Recursión y `@auxN` globales**: los temporales siguen siendo símbolos de `.DATA` compartidos. Expresiones con dos temporales vivos de distintos niveles de recursión pueden sobrescribirse. Pendiente de 4e/6.
4. **`this` como `DD` placeholder**: si un método se invoca sobre un objeto local, `operandoAsm` devuelve `[ebp-…]` y se usa `lea`; 4d debe asegurar layout completo y decidir paso por valor vs. dirección de forma uniforme.
5. **Alineación del frame**: redondeo a 8 bytes suficiente para `doublef`; si se agregan tipos con alineación mayor, revisar `prepararStackFrames()`.

### Desde docs/TP4/DECISIONES_P4d.md

1. **Atributos dispersos en `.DATA`**: los atributos de un objeto son símbolos planos dispersos, no un bloque contiguo; no se usa `rep movsd`. Si se requiere layout contiguo (`STRUCT` MASM), hay que reescribir `emitirCopiar()` y `emitirData()`.
2. **MASM real no verificado**: idéntico al riesgo 1 de 4c; pendiente revisar en Windows acceso a atributos doublef (`mov eax, <atributo>+4`).
3. **Colisión de nombres ASM de atributos**: `sanitizarBaseAsm()` reemplaza `.` y `:` por `_`; colisiones tras sanitización se resuelven con sufijos numéricos en `TablaSimbolos.nombreAsmDe()`.
4. **COPY sobre objetos locales en stack**: `operandoAsm` resuelve atributos vía `nombreAsmDe` (siempre `.DATA`), incluso dentro de funciones. Objetos locales quedan en `.DATA`, consistente con 4c pero a revisar si cambia el modelo de objetos locales.

### Observados en esta ejecución

- **Ninguno.** Todas las verificaciones de los 6 scripts pasaron en verde; no se detectó diferencia de comportamiento ni salida inesperada en stdout/stderr. Los únicos riesgos abiertos son los documentados arriba, heredados de diseño y pendientes de prompts futuros (4e, 5, 6) o de validación en MASM real (Windows).

## Conclusión y próximos pasos

### Conclusión

El Prompt 4d está completo y verificado: objetos y atributos, lectura/escritura de atributos, COPY estructural, CLASE/FIN_CLASE y layout en `.DATA` funcionan correctamente, y no rompen ninguna regresión previa (4c, 4b, 4a, 3, 2). El repositorio está en verde para todo el TP4 entregado hasta ahora.

### Próximos pasos

1. **Validación en MASM real (Windows)**: verificar la sintaxis de las instrucciones emitidas (`[ebp+…]`, `[ebp-…]`, `fld qword ptr`, `INVOKE crt_printf`, etc.) y el CRLF de los `fmt_*` (riesgo compartido 4c/4d, punto de contacto con Prompt 6).
2. **Prompt 4e (integración)**: integrar objetos, atributos y COPY con control de flujo y llamadas; verificar casos combinados (if/repeat con pout dentro de funciones y métodos).
3. **Prompt 5 (runtime)**: insertar guardas `b/e/f/h` antes de `div`/`fmul`/finito y en asignaciones a enum; confirmar que el frame con EBP preserva el `RET` ante mensajes con `crt_printf`.
4. **Prompt 6 (contrato final)**: revisar sintaxis MASM real en Windows y formato final del `.asm`.
5. **Riesgos pendientes de diseño**: recursión con `@auxN` globales (riesgo 3 de 4c) y `this` sobre objetos locales (riesgo 4 de 4c / riesgo 4 de 4d).

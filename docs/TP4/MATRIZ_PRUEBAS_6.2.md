# Matriz de pruebas — Paquete 6.2 (regresión TP3 + suite TP4/Grupo 43)

**Fuente**: `docs/TP4/PlanTP4.md`, paquete 6.2.
**Script consolidado**: `bash test_tp_4/probar_prompt6_2.sh`.
**Resultado global**: `EXIT 0`, 0 `FALLA:`. 137 fuentes TP3 procesadas.
**Log de la corrida**: `/tmp/opencode/run6_2.log` (archivo local; se puede regenerar con el comando de abajo).

> Convención de "obtenido": se registra el resultado observado en la corrida del script consolidado. Los chequeos son de **compilación Java y texto del `.asm`**. No hay `ml.exe`/`link` en este entorno: el disparo real de los chequeos en ejecución y la impresión de `%f` siguen pendientes de Windows (ver `DECISIONES_6.1.md` §5).

---

## 1. Cómo reproducir

```bash
cd /home/rafael/Documents/Compiladores/COMPILADOR/TP3
rm -rf /tmp/tp4p62_build
bash test_tp_4/probar_prompt6_2.sh
```

El script compila en `/tmp/tp4p62_build`, copia cada fuente a un `/tmp` aislado y ejecuta las suites existentes con `TP4_BUILD` apuntando a ese build. No modifica el código fuente.

---

## 2. Regresión TP3 (tarea 1)

Criterio por fuente: `java Main <copia en /tmp>` termina sin excepción, imprime el orden TP3 (`Codigo fuente:` → `Tokens detectados` → …), no emite stack trace y produce **stdout byte-identical en dos corridas consecutivas**. Los 5 snapshots canónicos se comparan aparte en la fila 4e.

| Caso | Comando | Esperado | Obtenido | Resultado |
|------|---------|----------|----------|-----------|
| Tema 13 — `test/tp2/verificacion_r13_estructura_while` (4 fuentes) | `java Main <copia>` ×2 | Sin excepción, stdout TP3 determinista | 4/4 OK, 0 fallas | OK |
| Tema 17 — `verificacion_r17_asig_expresiones` (11 fuentes) | ídem | ídem | 11/11 OK, 0 fallas | OK |
| Tema 19 — `verificacion_r19_param_reales` (10 fuentes) | ídem | ídem | 10/10 OK, 0 fallas | OK |
| Tema 23 — `verificacion_r23_enums` (14 fuentes) | ídem | ídem | 14/14 OK, 0 fallas | OK |
| Tema 25 — `verificacion_r25_objetos` (13 fuentes) | ídem | ídem | 13/13 OK, 0 fallas | OK |
| Temas 28 y 31 — `verificacion_r28_r31_atributos` (25 fuentes) | ídem | ídem | 25/25 OK, 0 fallas | OK |
| Tema 33 — `verificacion_r33_conversion` (13 fuentes) | ídem | ídem | 13/13 OK, 0 fallas | OK |
| `test/TP3_SEMANTICAS` (47 fuentes, "si aplica") | ídem | Sin excepción, stdout TP3 determinista | 47/47 OK, 0 fallas | OK |
| **Total** | — | 137 fuentes sin regresión | **137/137, 0 fallas** | **OK** |

Nota: los temas 28 y 31 comparten directorio en el repositorio; se cuentan juntos y se declaran explícitamente en el script.

---

## 3. Chequeos runtime b/e/f/h (tarea 2)

| Caso | Comando | Esperado | Obtenido | Resultado |
|------|---------|----------|----------|-----------|
| b — suite completa | `bash test_tp_4/probar_prompt5_b.sh` | Guardia `add` + `jc error_overflow_suma` en `.asm` | EXIT 0, 0 `FALLA:` | OK |
| b — disparador `4294967295$ul + 1$ul` | inspección de `resta_enteros_negativos/02_suma_desborda.asm` | `jc error_overflow_suma` presente | presente | OK |
| b — válido límite `10$ul + 20$ul` | inspección de `01_suma_valida.asm` | Guardia instrumentada presente (CF=0 en ejecución) | presente | OK |
| e — suite completa | `bash test_tp_4/probar_prompt5_e.sh` | `fmul` + `fxam/fstsw/sahf/jc error_overflow_float` | EXIT 0, 0 `FALLA:` | OK |
| e — disparador `max_doublef * max_doublef` | inspección de `producto_doublef_overflow/02_producto_desborda.asm` | `jc error_overflow_float` presente | presente | OK |
| e — válido límite `1.5 * 2.0` | inspección de `01_producto_valido.asm` | Guardia instrumentada presente (C0=0 en ejecución) | presente | OK |
| f — suite completa | `bash test_tp_4/probar_prompt5_f.sh` | `sub` + `jb error_negativo_resta` | EXIT 0, 0 `FALLA:` | OK |
| f — disparador `10$ul - 20$ul` | inspección de `resta_enteros_negativa/02_resta_negativa.asm` | `jb error_negativo_resta` presente | presente | OK |
| f — válido límite `20$ul - 10$ul` | inspección de `01_resta_valida.asm` | Guardia instrumentada presente | presente | OK |
| h — suite completa | `bash test_tp_4/probar_prompt5_h.sh` | Guardias enum ulongint y doublef + `error_enum_invalido` | EXIT 0, 0 `FALLA:` | OK |
| h — disparador enum ulongint (`5 ∉ [1,2,3]`) | inspección de `enumerados/02_asignacion_invalida.asm` | Label `error_enum_invalido` | presente | OK |
| h — disparador enum doublef (`3.5 ∉ [1.5,2.5]`) | inspección de `enumerados/04_doublef_invalida.asm` | Label `error_enum_invalido` | presente | OK |
| h — válido límite (`2 ∈ [1,2,3]`) | inspección de `enumerados/01_asignacion_valida.asm` | Compila y cierra con `end start` | presente | OK |

**Limitación**: las guardias se instrumentan estáticamente en todos los casos; que el salto se tome o no depende de valores en ejecución. Aquí se verifica la **presencia** de la guardia y los valores del fuente, no el salto real. Ejecutar los binarios en Windows para completar esta fila.

---

## 4. Programa integral (tarea 3)

Fuente: `test_tp_4/Integracion/07_programa_integral_6_2.txt`. Combina `repeat` con `if` anidado, función recursiva `fact`, dos clases base con herencia múltiple, `COPY` posicional, acceso prefijado y `pout` de cadena/ulongint/doublef.

| Caso | Comando | Esperado | Obtenido | Resultado |
|------|---------|----------|----------|-----------|
| Compilación del integral | `java Main <copia en /tmp>` | Sin `Error:`, `.asm` generado | 0 errores, `.asm` generado | OK |
| Estructura MASM | inspección del `.asm` | `.data`, `.code`, `end start` | presentes | OK |
| Control de flujo | ídem | `label_3:` y `jmp label_3` (cierre del `repeat`) | presentes | OK |
| Recursión | ídem | `fn_fact_main:` y `call fn_fact_main` | presentes | OK |
| Objetos y COPY | ídem | `s2_basea_u_main`, `s2_baseb_v_main DQ ?`, `mov eax, dword ptr [s1_baseb_v_main+4]` | presentes | OK |
| `pout` mixto | ídem | invokes con `fmt_cadena`, `fmt_ulongint` y `fmt_doublef` | presentes | OK |

---

## 5. Regresiones de paquetes previos (tarea 5 del script)

| Caso | Comando | Esperado | Obtenido | Resultado |
|------|---------|----------|----------|-----------|
| Snapshots TP3 de consola | `bash test_tp_4/probar_prompt4e.sh` | 5/5 `IGUAL`, 0 `FALLA:` | EXIT 0, 0 `FALLA:` | OK |
| Compuerta del `.asm` y stdout TP3 | `bash test_tp_4/probar_prompt6_0.sh` | EXIT 0, 0 `FALLA:` | EXIT 0, 0 `FALLA:` | OK |
| Validación MASM32 estática | `bash test_tp_4/probar_prompt6_1.sh` | EXIT 0, 0 `FALLA:` | EXIT 0, 0 `FALLA:` | OK |

---

## 6. Decisiones del paquete

1. **Sin snapshots nuevos por tema**: no existen salidas golden para los ~90 fuentes TP3 de los temas 13–33. Se usa determinismo entre dos corridas + presencia del orden TP3 + los 5 snapshots canónicos de 4e. Esto evita inventar salidas esperadas.
2. **Dos corridas por fuente**: detecta tanto crashes como salida no determinista (tabla de símbolos o iteración desordenada), con costo bajo.
3. **El integral se genera en `/tmp`**: el script 6.2 no depende de fixtures `.asm` versionados. El `.asm` que aparece junto al `.txt` en `Integracion/` lo regenera `probar_prompt4e.sh` al recorrer esa carpeta; es un artefacto, no una expectativa.
4. **No se ejecuta ningún `.asm`**: sin `ml.exe`/`link`, "disparar" se interpreta como presencia de la guardia en el texto ensamblado y valores de fuente que la activarían. La confirmación runtime queda documentada como pendiente.
5. **6.4 no se ejecuta**: requiere la confirmación literal «**BORRAR COMENTARIOS**».

---

## 7. Estado

✅ Paquete 6.2 ejecutado. ⚠️ Único pendiente heredado: ensamblar/enlazar/ejecutar en Windows (tareas 6.1 y verificación runtime de b/e/f/h).

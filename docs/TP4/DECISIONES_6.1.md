# Decisiones y cambios — Paquete 6.1 (validación MASM32 del `.asm`)

**Estado**: ✅ revisión estática y generación CRLF ejecutadas; ⚠️ ensamblado y enlazado con `ml.exe`/`link` **pendientes de ejecutarse en Windows**.
**Fuente**: `docs/TP4/PlanTP4.md`, paquete 6.1.
**Scope**: `GeneradorAssembler.escribirArchivo()`, `test_tp_4/probar_prompt6_1.sh`, documentación. No se tocaron las tareas 6.2–6.4.

---

## 1. Entorno

| Herramienta | Disponible aquí | Consecuencia |
|-------------|-----------------|--------------|
| `javac` / `java` | sí | generación y verificación textual del `.asm` |
| `ml.exe` / `ml64.exe` | **no** | no se puede ensamblar |
| MASM32 SDK (`masm32rt.inc`, librerías) | **no** (`/masm32` inexistente) | no se puede enlazar ni ejecutar |
| `link`, `wine` | sí | insuficientes sin `ml.exe` ni el SDK |

La validación realizada es, por lo tanto, **estática**: sintaxis de includes/directivas, terminadores y forma de las instrucciones. El comportamiento real del runtime queda para Windows.

---

## 2. Revisiones y decisiones

### 2.1 Includes y encabezado — sin cambios

El generador ya emite exactamente:

```asm
.386
.model flat, stdcall
option casemap:none
include \masm32\include\masm32rt.inc
```

Comprobado contra el `masm32rt.inc` del MASM32 SDK: ese include ya trae `.486`, `.model flat, stdcall`, `option casemap:none`, `msvcrt.inc`, `kernel32.lib` y `msvcrt.lib`. La barra invertida es la correcta en rutas MASM. No se agregaron `includelib` duplicados.

Fuentes:
- https://github.com/tonado/MASM32-SDK/blob/master/include/masm32rt.inc
- https://raw.githubusercontent.com/tonado/MASM32-SDK/master/include/msvcrt.inc

### 2.2 Secciones y cierre — sin cambios

`.data` y `.code` están presentes en todo caso generado; `start:` precede al código y el archivo termina con `end start`. Ese cierre es el correcto porque `start` es la entrada definida en `.code`. No se usa la forma `end` sola.

### 2.3 Alineación de `DQ` — no se agrega `ALIGN 8`

Decisión: **mantener `DQ` y no insertar `ALIGN 8`**.

Razones:

1. La documentación de Microsoft para `ALIGN` exige que el parámetro sea una potencia de 2 **menor o igual que la alineación del segmento**. Con `.386`, la alineación de segmento vigente es DWORD (4); con `.486` o superior pasa a PARA (16). Un `ALIGN 8` explícito puede rechazarse (`X` mayor que la alineación del segmento) o ser innecesario.
2. En x86 no es requisito de corrección: la FPU (`fld`/`fstp qword ptr`) y `printf("%f")` funcionan con `DQ` sin alineación de 8 bytes; solo hay una posible penalización de rendimiento.
3. La guía MASM 6.1 indica que `DQ` con un literal real codifica el mismo IEEE double que `REAL8`; la diferencia es solo el tag de CodeView. Cambiar `DQ` por `REAL8` no aporta corrección y rompería los snapshots y los tests que aseveran `DQ`.

Fuentes:
- https://learn.microsoft.com/en-us/cpp/assembler/masm/align-masm?view=msvc-170
- https://learn.microsoft.com/en-us/cpp/assembler/masm/dq?view=msvc-170
- https://learn.microsoft.com/en-us/cpp/assembler/masm/real8?view=msvc-170
- https://www.mikrocontroller.net/attachment/450367/MASM61PROGUIDE.pdf
- https://faculty.kfupm.edu.sa/coe/mudawar/coe205/manuals/MASMProgrGuide/06-Floating%26BCD.pdf

### 2.4 Terminadores: LF → CRLF al escribir

Decisión: **el `.asm` escrito usa CRLF**.

La documentación clásica de MASM indica que cada línea del fuente se separa con una combinación retorno de carro/salto de línea. Aunque no se encontró evidencia de que `ml.exe` rechace LF, no hay toolchain MASM aquí para comprobarlo; elegir CRLF elimina la duda y coincide con la convención del SDK de la cátedra.

Implementación: `GeneradorAssembler.escribirArchivo()` normaliza el texto antes de escribirlo:

```java
String contenido = generar().replace("\r\n", "\n").replace("\n", "\r\n");
writer.write(contenido);
```

`generar()` sigue devolviendo texto con `\n`, de modo que las verificaciones internas y la lógica del generador no cambian; la conversión ocurre **solo en la escritura del archivo**. La normalización previa evita duplicar `\r` si alguna cadena ya trajera CRLF.

Impacto: los `.asm` regenerados pasan de LF a CRLF. Los tests usan `grep -F`, que sigue coincidiendo porque la búsqueda no incluye el fin de línea; las snapshots de consola no se tocan. Los fixtures `.asm` ya versionados se restauraron a `HEAD` después de correr la batería para no introducir un diff de decenas de archivos por solo terminadores; el script 6.1 genera sus propios casos en `/tmp`.

### 2.5 `invoke crt_printf` con `doublef` — sin cambios

El generador emite:

```asm
fmt_doublef DB "%f",0
...
invoke crt_printf, offset fmt_doublef, z_main      ; z_main DQ ?
```

`crt_printf` es un alias de `_imp__printf` declarado con `PROTO C :VARARG` en `msvcrt.inc`, incluido por `masm32rt.inc`. Al pasar un símbolo declarado `DQ`, `INVOKE` coloca el operando completo (8 bytes) en la pila, que es lo que `%f` espera. No se carga la FPU ni se mueve a EAX: el valor viaja por valor en la pila.

Se conserva `fmt_doublef DB "%f",0` sin salto de línea, decisión histórica del Prompt 4b documentada en `DECISIONES_P4b.md`. Cambiar el formato alteraría la salida de todos los casos `pout` y requiere una revisión de regresiones fuera del scope de 6.1.

Fuentes:
- https://stackoverflow.com/questions/27562047/how-can-i-print-array-on-masm32
- https://stackoverflow.com/questions/34209579/crt-printf-and-exitprocess-in-masm32-with-lib
- https://learn.microsoft.com/en-us/cpp/assembler/masm/invoke?view=msvc-170

**Limitación**: esta conclusión es estática. La comprobación real de que `%f` imprime el doble correcto (incluido el orden de las dos palabras de 32 bits en la pila) requiere ejecutar los tres casos en Windows.

---

## 3. Archivos modificados

| Archivo | Cambio |
|---------|--------|
| `GeneradorAssembler.java` | Normalización LF→CRLF en `escribirArchivo()`, con comentario que explica la decisión. Sin cambios en `emitirEncabezado()`, `DQ`, `fmt_doublef` ni `end start`. |
| `test_tp_4/probar_prompt6_1.sh` | Nuevo. Verifica encabezado, secciones, `end start`, `DQ`, `crt_printf`, ausencia de `ALIGN 8`/`REAL8` y CRLF en 3 casos. |
| `docs/TP4/PlanTP4.md` | Paquetes 6.0 y 6.1 actualizados: batería post-revert cerrada; 6.1 marcado como ejecutado con la salvedad de Windows. |
| `docs/TP4/DECISIONES_6.0.md` | §4 con el resultado de la batería 7/7 y fila 6.1 actualizada: CRLF resuelto; ensamblado real pendiente. |
| `docs/TP4/TEMPLATES_ASM.md` | Nota 6.1 en T-01 sobre CRLF y `ALIGN 8`. |
| `docs/TP4/ESTADO_RUNTIME_P5.md` | Estado del Prompt 6 ajustado: 6.1 con validación estática hecha. |
| `test_tp_4/guia.md` | Actualización 6.1 con el nuevo script y la salvedad de Windows. |

---

## 4. Comandos y resultados

Compilación y suite nueva:

```bash
rm -rf /tmp/tp4p61_build
bash test_tp_4/probar_prompt6_1.sh
```

Resultado: `EXIT 0`, 0 `FALLA:`. Casos: `01_pout_variable_ulongint`, `04_pout_doublef` y `04_objetos_multiherencia_copy`, generados en un `/tmp` aislado.

Chequeo manual de terminadores sobre un `.asm` recién generado en un directorio temporal:

```bash
f="/tmp/ejemplo_generado.asm"
tr -cd '\n' < "$f" | wc -c   # 23 para 04_pout_doublef
tr -cd '\r' < "$f" | wc -c   # 23
head -1 "$f" | od -An -tx1   # 2e 33 38 36 0d 0a  -> ".386\r\n"
```

Baterías:

| Batería | Scripts | Resultado |
|---------|---------|-----------|
| Post-revert de stdout (anterior al cambio CRLF) | 5.0, 5.b, 5.e, 5.f, 5.h, 4e, 6.0 | 7/7 `EXIT 0`, 0 `FALLA:`; detalle en `DECISIONES_6.0.md` §4 |
| Post-CRLF (6.1) | 4b, 4e, 6.0, 6.1 | 4/4 `EXIT 0`, 0 `FALLA:` |

Los scripts regeneran los `.asm` versionados; como el único cambio es el terminador de línea, se verificó con `git diff --ignore-cr-at-eol -- '*.asm'` que no hay diferencias de contenido y se restauraron los fixtures a `HEAD`. `git diff --check` final: exit 0.

---

## 5. Pendiente en Windows

Con el MASM32 SDK instalado y `ml.exe`/`link.exe` en el `PATH`:

```bat
ml.exe /c /coff /Fo caso.obj caso.asm
link.exe /SUBSYSTEM:CONSOLE /OUT:caso.exe caso.obj
caso.exe
```

Aplicar a los tres casos representativos (ulongint, doublef, objetos). Registrar en este archivo: salida de `ml.exe`/`link`, códigos de salida y la impresión real de `%f`. Hasta entonces el paquete 6.1 queda **validado estáticamente, no ensamblado**.

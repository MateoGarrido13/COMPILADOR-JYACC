# Guía de prueba — Prompt 2 de TP4 (GeneradorAssembler esqueleto)

Este documento explica cómo verificar que el **Prompt 2 de TP4** funciona correctamente.

> **Actualización 6.0 (con revert posterior)**: se agregó la compuerta que evita generar `.asm` con errores. El cambio de stdout a formato TP4 fue revertido por pedido del usuario: hoy se imprimen fuente numerado, tokens, estructuras, errores, tabla de símbolos y tercetos, y él eliminará ese bloque manualmente antes de entregar. Ver `docs/TP4/DECISIONES_6.0.md` y `probar_prompt6_0.sh`. Lo demás de esta guía (estructura del `.asm`) sigue vigente.

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

- La salida por consola sigue siendo la de TP3; el intento de cambiarla en 6.0 fue revertido por pedido del usuario.
- No se rompe ningún caso existente de TP3; los snapshots bajo `Integracion/consola_tp3/` volvieron a su contenido original.

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
| Salida por consola | TP3: fuente numerado + tokens + estructuras + errores + TS + tercetos (recorte manual antes de entregar) |

## Casos de prueba sugeridos

- `test/tp2/generales/00_programa_valido.txt` — programa mínimo con variables
- Cualquier archivo con `ulongint`, `doublef`, cadena y `pout`

## Archivos involucrados

- `GeneradorAssembler.java` (nuevo)
- `Compilador.java` (modificado: invoca al generador)
- `docs/TP4/DECISIONES_P2.md` (decisiones de representación)

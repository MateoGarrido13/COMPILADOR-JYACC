# Guía de prueba — GeneradorAssembler esqueleto TP4

Este documento explica cómo verificar que el GeneradorAssembler esqueleto de TP4 funciona correctamente.

## Qué debe hacer el GeneradorAssembler

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
   - Traducción real de tercetos: ya implementada; no queda comentario `TODO` pendiente

## Qué NO debe cambiar

- La salida por consola sigue siendo la de TP3.
- No se rompe ningún caso existente de TP3; los snapshots bajo `Integracion/consola_tp3/` volvieron a su contenido original.

## Cómo probar

Desde la raíz del proyecto:

```bash
# 1. Compilar
javac -encoding UTF-8 -d /tmp/tp4p2 *.java "acciones semanticas"/*.java otras_acciones/*.java

# 2. Ejecutar manualmente
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
| Salida por consola | TP3: fuente numerado + tokens + estructuras + errores + TS + tercetos |

## Casos de prueba sugeridos

- `test/tp2/generales/00_programa_valido.txt` — programa mínimo con variables
- Cualquier archivo con `ulongint`, `doublef`, cadena y `pout`

## Archivos involucrados

- `GeneradorAssembler.java` (nuevo)
- `Compilador.java` (modificado: invoca al generador)
- `docs/TP4/DECISIONES_P2.md` (decisiones de representación)

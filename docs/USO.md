# Guía de uso — compilador TP3 / generador ASM TP4

Esta guía cubre desde compilar el proyecto en Java hasta ejecutar el `.asm`
generado en Windows con MASM32. El compilador **no ensambla ni enlaza**: solo
produce el texto assembler.

---

## 1. Requisitos

| Herramienta | Para qué | Estado en este repositorio |
|-------------|----------|-----------------------------|
| JDK con `javac`/`java` | Compilar y ejecutar el compilador | necesario |
| MASM32 SDK (`ml.exe`, `link.exe`, `masm32rt.inc`) | Ensamblar y enlazar el `.asm` | **no disponible en Linux**; instalar en Windows |
| Bash | Correr los scripts de prueba | opcional |

El `.asm` generado incluye `\masm32\include\masm32rt.inc`. Si el SDK está en
otra ruta, agregar `/I C:\masm32\include` a la línea de `ml.exe`.

---

## 2. Compilar el proyecto

Desde la raíz del repositorio:

```bash
mkdir -p /tmp/tp3build
javac -encoding UTF-8 -d /tmp/tp3build \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

`-encoding UTF-8` es obligatorio: los fuentes y las docs tienen acentos y
comentarios en español. El directorio de destino puede ser cualquiera; los
scripts usan `/tmp/tp4p62_build` por defecto y permiten cambiarlo con
`TP4_BUILD`.

---

## 3. Compilar un programa fuente

```bash
java -cp /tmp/tp3build Main archivo.txt
```

- Sin argumento, `Main` usa `input.txt` del directorio actual.
- Si el fuente tiene errores, **no se escribe el `.asm`** (compuerta del
  paquete 6.0). El estado de salida del proceso es siempre 0: los errores se
  informan en la consola, no con `exit`.
- El `.asm` se crea junto al fuente, con la misma base y extensión `.asm`
  (`caso.txt` → `caso.asm`). `rutaAsmPara()` busca el último punto después del
  último separador, así que un punto en el nombre de una carpeta no se confunde
  con la extensión.
- El archivo se escribe con terminadores **CRLF** (paquete 6.1), como espera
  MASM. El texto devuelto por `GeneradorAssembler.generar()` sigue usando LF
  para que las pruebas textuales sean estables.

### Salida por consola

Actualmente se imprime el listado completo del TP3 (fuente numerado, tokens,
estructuras, errores, tabla de símbolos y tercetos). Es un pedido explícito:
el usuario recortará ese bloque manualmente antes de entregar. Está marcado en
`Compilador.java` con:

```java
// --- Eliminar estas lineas antes de entregar, para que la salida sea solo el .asm. ---
```

No se agregaron mensajes nuevos (`ASM generado:`, `ASM no generado:`, etc.)
para no alterar ese contrato.

---

## 4. Ensamblar y ejecutar en Windows

Con el MASM32 SDK instalado y `ml.exe`/`link.exe` en el `PATH`:

```bat
ml.exe /c /coff /Fo caso.obj caso.asm
link.exe /SUBSYSTEM:CONSOLE /OUT:caso.exe caso.obj
caso.exe
```

Notas:

- Usar **`ml.exe` de 32 bits**, no `ml64.exe`: el encabezado es `.386` y
  `.model flat, stdcall`.
- `masm32rt.inc` ya aporta `msvcrt.inc`, `kernel32.lib` y `msvcrt.lib`; no se
  necesitan `includelib` adicionales.
- `crt_printf` muestra `%u` para `ulongint`, `%f` para `doublef` y `%s` para
  cadenas. La impresión real de `%f` aún no se verificó en este entorno.
- Los chequeos runtime b/e/f/h terminan con códigos distintos (overflow de
  suma, overflow float, resta negativa, enum inválido).

---

## 5. Scripts de verificación

```bash
bash test_tp_4/probar_prompt6_1.sh   # validación estática MASM32 y CRLF
bash test_tp_4/probar_prompt6_2.sh   # regresión TP3 + b/e/f/h + integral
```

`probar_prompt6_2.sh` compila en `/tmp/tp4p62_build`, procesa 137 fuentes TP3 y
verifica guardias en el texto del `.asm`. No ejecuta los binarios. El detalle
está en `docs/TP4/MATRIZ_PRUEBAS_6.2.md`.

Para regenerar un `.asm` de ejemplo sin correr todas las suites:

```bash
java -cp /tmp/tp3build Main test_tp_4/Integracion/07_programa_integral_6_2.txt
```

---

## 6. Limitaciones conocidas

1. Sin `ml.exe`/`link` no se puede ensamblar, enlazar ni ejecutar aquí. Esa
   verificación queda para Windows.
2. Las guardias b/e/f/h se instrumentan estáticamente; su presencia en el texto
   no demuestra que el salto se tome en ejecución.
3. No se agregó `ALIGN 8`: con `.386` la alineación de segmento es DWORD (4) y
   los `DQ` se manipulan por mitades. Ver `docs/TP4/DECISIONES_6.1.md`.
4. No hay vtable: el despacho de métodos es directo porque el IR ya trae el
   método resuelto.
5. 6.4 (eliminación de comentarios) no se ejecutó; requiere la confirmación
   literal «**BORRAR COMENTARIOS**».

Resumen de decisiones y estado final: `docs/TP4/DECISIONES_P6.md` y
`docs/TP4/ESTADO_FINAL.md`.

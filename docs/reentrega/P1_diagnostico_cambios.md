# Ola 1 — diagnóstico y cambios

Prompts ejecutados: **P1-01** (estado dedicado “ID inválido”, 23/11/2026) y **P1-02** (mensaje del error léxico residual, 24/11/2026).  
Prompt `P1-03`: **pendiente**.  
P1-01 quedó commiteado como `d7de3bf`; P1-02 se documenta en la sección 8.  
Fechas: 23/11/2026 y 24/11/2026.

Este archivo concentra toda la Ola 1. Acá se documentan los cambios efectuados, la razón de cada uno, la verificación y lo que queda para los prompts siguientes.

---

## 1. Alcance de P1-01

Según `docs/reentrega/plan_prompts_reentrega.md`, P1-01 debía:

- cambiar `MATRIZ_ESTADOS[16][20]` de `99` a un nuevo estado `19`;
- agregar la fila 19 con transiciones que sigan consumiendo caracteres de identificador;
- agregar la fila 19 de `MATRIZ_SEMANTICAS` con acción `37`;
- hacer que `semCierre(19)` devuelva `37`;
- implementar la acción `37` en `AnalizadorLexico.ejecutarAccion()` para reportar `Identificador invalido '<lexema>'` y devolver `-1`.

No se modificaron `esLookahead()`, `AccionIdentificador.java`, `errorLexico()`, `Parser.java`, `LectorFuente.java`, tests ni docs existentes.

---

## 2. Cambios efectuados

### 2.1 `MatrizTransiciones.java`

#### Cambio A — celda de salida del estado ID

Antes:

```java
{   16,  16,  16,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 16,  99,  99,  99, 99,  99,  99}, // 16 id
```

Después:

```java
{   16,  16,  16,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 16,  99,  99,  99, 19,  99,  99}, // 16 id
```

**Por qué:** la columna 20 es “otro” (`ContextoLexico.columna():61`). Con `99`, un `#` después de un ID se trataba como delimitador: `esLookahead()` devolvía `true`, el `#` no se agregaba al lexema y se devolvía a la fuente. `AccionIdentificador` validaba solo `id`. Ahora el carácter inválido deriva a un estado que puede absorber el resto del lexema.

#### Cambio B — nueva fila 19 en `MATRIZ_ESTADOS`

Se agregó después de la fila 18:

```java
{   19,  19,  19,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 19,  99,  99,  99, 19,  99,  99}  // 19 id invalido
```

Columnas que permanecen en 19:

| Columna | Significado |
|---|---|
| 0 | dígito |
| 1 | minúscula |
| 2 | mayúscula |
| 16 | `_` |
| 20 | otro (carácter inválido) |

Todas las demás van a `99 = ESTADO_F`.

**Por qué:** el lexema inválido debe continuar hasta un delimitador real. Si `id#malo` se cortara en `#`, quedarían dos tokens; si solo se consumiera `#`, quedaría `malo` como ID nuevo. Al permanecer en 19 para letras, dígitos, `_` y otros caracteres inválidos, se entrega un solo lexema `id#malo`. Los delimitadores (`;`, `(`, `+`, blanco, `\n`, etc.) cierran el token y se devuelven como lookahead.

**Nota:** la columna 2 (mayúscula) también se consume. El enunciado prohíbe mayúsculas en identificadores; que queden dentro del lexema inválido evita que aparezcan como token separado.

#### Cambio C — nueva fila 19 en `MATRIZ_SEMANTICAS`

Se agregó después de la fila 18:

```java
{   37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37, 37,  37,  37,  37, 37,  37,  37}  // 19 id invalido
```

**Por qué:** cuando el estado 19 llega a `ESTADO_F`, `AnalizadorLexico.recorrerMatriz():71` consulta `MATRIZ_SEMANTICAS[estadoAnterior][columna]`. Con `37` en todas las columnas se garantiza que cualquier delimitador dispare la acción de error, sin importar con qué carácter termine el lexema inválido. En las columnas de continuación el valor no se usa porque `siguiente != ESTADO_F`.

#### Cambio D — cierre por fin de archivo

Antes:

```java
case 15: return 7;
case 16: return 9;
default: return -1;
```

Después:

```java
case 15: return 7;
case 16: return 9;
case 19: return 37;
default: return -1;
```

**Por qué:** si el archivo termina mientras el lexer está en estado 19, `recorrerMatriz():44` usa `semCierre(estado)`. Sin este caso caería en `default: -1`, que llevaría a `errorLexico()` con el mensaje genérico. Con `37` se reporta el mismo error `Identificador invalido '<lexema>'`.

### 2.2 `AnalizadorLexico.java`

#### Cambio E — acción semántica 37

Antes:

```java
case 21: case 27: case 28: case 29: case 30: case 31: case 32: case 33: case 34: case 35:
    return new AccionConstanteFloat(contexto).ejecutar();
default:
    return new AccionSimbolo(contexto).ejecutar();
```

Después:

```java
case 21: case 27: case 28: case 29: case 30: case 31: case 32: case 33: case 34: case 35:
    return new AccionConstanteFloat(contexto).ejecutar();
case 37:
    contexto.reporte.error(contexto.fuente.linea(),
            "Identificador invalido '" + contexto.lexema + "'");
    return -1;
default:
    return new AccionSimbolo(contexto).ejecutar();
```

**Por qué:**

- `contexto.reporte` y `contexto.fuente.linea()` ya son públicos (`ContextoLexico.java:2-10`); no hace falta una clase nueva.
- El mensaje incluye el lexema completo, que es justamente la información que faltaba antes.
- Devolver `-1` hace que `siguienteToken()` siga buscando (`AnalizadorLexico.java:33-36`) y que el parser vea un token de error, en lugar de un ID falso.
- Se implementa inline para mantener el cambio mínimo; no se agregó `AccionIdentificadorInvalido.java`.

### 2.3 Diff final

```diff
--- a/AnalizadorLexico.java
+++ b/AnalizadorLexico.java
@@ -114,6 +114,10 @@
             case 21: case 27: case 28: case 29: case 30: case 31: case 32: case 33: case 34: case 35:
                 return new AccionConstanteFloat(contexto).ejecutar();
+            case 37:
+                contexto.reporte.error(contexto.fuente.linea(),
+                        "Identificador invalido '" + contexto.lexema + "'");
+                return -1;
             default:
                 return new AccionSimbolo(contexto).ejecutar();
--- a/MatrizTransiciones.java
+++ b/MatrizTransiciones.java
@@ -36,9 +36,10 @@
-        {   16,  16,  16,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 16,  99,  99,  99, 99,  99,  99}, // 16 id
+        {   16,  16,  16,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 16,  99,  99,  99, 19,  99,  99}, // 16 id
@@ -38,7 +39,8 @@
-        {   99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99,  99,  99,  99, 99,  99,  99}  // 18 $ul
+        {   99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 99,  99,  99,  99, 99,  99,  99}, // 18 $ul
+        {   19,  19,  19,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99,  99, 19,  99,  99,  99, 19,  99,  99}  // 19 id invalido
@@ -61,7 +63,8 @@
-        {   25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25, 25,  25,  25,  25, 25,  25,  25}  // 18
+        {   25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25,  25, 25,  25,  25,  25, 25,  25,  25}, // 18
+        {   37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37,  37, 37,  37,  37,  37, 37,  37,  37}  // 19 id invalido
@@ -79,6 +82,7 @@
             case 15: return 7;
             case 16: return 9;
+            case 19: return 37;
             default: return -1;
```

---

## 3. Incidencia de la corrida: celda omitida y corrección

Primera pasada de edición: se agregaron la fila 19, la fila semántica, `semCierre(19)` y el `case 37`. Se compiló y se ejecutaron los casos. **La salida era idéntica al baseline**.

Causa: todavía no se había cambiado `MATRIZ_ESTADOS[16][20]`; seguía en `99`, por lo que el automaton nunca entraba al estado 19.

Corrección: se reemplazó esa celda por `19`, se recompiló y se repitieron las pruebas. Ahí aparecieron los mensajes `Identificador invalido 'id#malo'` y desaparecieron los tokens fragmentados.

Esta incidencia queda registrada porque explica por qué el cambio A es indispensable: los otros cuatro cambios son infraestructura, pero sin la transición desde el estado 16 no se alcanzan.

---

## 4. Verificación

### 4.1 Compilación

```bash
mkdir -p /tmp/opencode/tp3build_p1 /tmp/opencode/lexprobe
javac -encoding UTF-8 -d /tmp/opencode/tp3build_p1 \
      *.java "acciones semanticas"/*.java otras_acciones/*.java
```

Resultado: **sin errores**.

### 4.2 Casos ejecutados

```bash
for f in caracter_invalido.txt b_rhs.txt declaracion.txt cadena.txt validos.txt; do
  java -cp /tmp/opencode/tp3build_p1 Main "/tmp/opencode/lexprobe/$f" \
      > "/tmp/opencode/lexprobe/$f.p1.out" 2>&1
done
```

Fuentes de prueba usadas:

- `caracter_invalido.txt`: copia de `test/tp1/verificacion_identificadores/caracter_invalido.txt`.
- `b_rhs.txt`: `x := id#malo + 1$ul;`.
- `declaracion.txt`: `ulongint id#malo; begin id := 1$ul; end`.
- `cadena.txt`: `pout({id#malo});`.
- `validos.txt`: identificadores válidos con dígito y `_`.

### 4.3 Antes / después

| Caso | Baseline | Con P1-01 |
|---|---|---|
| `caracter_invalido.txt` | `Lexema no reconocido ''` ×4; tokens `id`, `malo`, `otro`, `nombre`; IR `(pout, mal, --)` | `Identificador invalido 'id#malo'` e `Identificador invalido 'otro@nombre'`; cero tokens derivados; IR `(pout, mal, --)` |
| `b_rhs.txt` | tokens `id`, `malo` en la expresión; `(:=, x:main, id:main)` | sin tokens de la expresión inválida; IR solo `(PROGRAMA)` y `(FIN_PROGRAMA)` |
| `declaracion.txt` | `(:=, id:main, 1$ul)` | IR solo `(PROGRAMA)` y `(FIN_PROGRAMA)` |
| `cadena.txt` | `(pout, id#malo, --)` | idéntico |
| `validos.txt` | 6 tercetos | idéntico |

Diff relevante de `b_rhs.txt`:

```diff
 [300] Identificador x:main
 [301] Operador de asignacion :=
-[300] Identificador id:main
-[300] Identificador malo:main
 [43] +
@@
-Línea 4: Error: Lexema no reconocido ''
-Línea 4: Error: Falta ';' al final de la sentencia
-Línea 4: Error: Sentencia mal formada
+Línea 4: Error: Identificador invalido 'id#malo'
+Línea 4: Error: Asignacion mal formada
@@
 Código intermedio:
 1. (PROGRAMA, --, --)
-2. (:=, x:main, id:main)
-3. (FIN_PROGRAMA, --, --)
+2. (FIN_PROGRAMA, --, --)
```

Diff relevante de `caracter_invalido.txt`:

```diff
-[300] Identificador id:main
-[300] Identificador malo:main
 [44] ,
-[300] Identificador otro:main
-[300] Identificador nombre:main
@@
-Línea 3: Error: Lexema no reconocido ''
-Línea 3: Error: Lexema no reconocido ''
-Línea 3: Error: Falta de ',' en declaracion de variables
-Línea 3: Error: Falta de ',' en declaracion de variables
+Línea 3: Error: Identificador invalido 'id#malo'
+Línea 3: Error: Identificador invalido 'otro@nombre'
+Línea 3: Error: Lista de variables mal formada
```

### 4.4 Regresión sobre los seis casos baseline

```bash
for f in 01_funcion_ulongint.txt 03_recursion_factorial_metodo.txt \
         17_asig_donde_igual.txt 18_falta_orden.txt 21_enum_vacio.txt \
         caracter_invalido.txt; do
  java -cp /tmp/opencode/tp3build_p1 Main "/tmp/opencode/baseline/$f" \
      > "/tmp/opencode/baseline/$f.p1.out" 2>&1
  diff -q "/tmp/opencode/baseline/$f.out" "/tmp/opencode/baseline/$f.p1.out"
done
```

| Caso | Resultado |
|---|---|
| `01_funcion_ulongint.txt` | sin cambio |
| `03_recursion_factorial_metodo.txt` | sin cambio |
| `17_asig_donde_igual.txt` | sin cambio |
| `18_falta_orden.txt` | sin cambio |
| `21_enum_vacio.txt` | sin cambio |
| `caracter_invalido.txt` | cambio esperado |

Los hashes de los dos `.asm` TP04 siguen siendo `f8bdc552…` y `aac7910f…`. Ningún cambio léxico llegó al assembler.

### 4.5 Criterios de aceptación de P1-01

| Criterio | Cumplido |
|---|---|
| `id#malo` entrega un error con el lexema completo | sí |
| Cero tokens ID derivados de ese lexema | sí (en la ocurrencia inválida; los IDs de la declaración `ulongint x, id, malo` son legítimos y siguen apareciendo) |
| No aparece `Lexema no reconocido ''` para `id#malo` / `otro@nombre` | sí |
| No se emite `(:=, x:main, id:main)` | sí |
| Cadenas `{id#malo}` intactas | sí |

---

## 5. Efectos colaterales analizados

- **`esLookahead()`**: no se tocó. Con estado 19 y `siguiente = ESTADO_F`, los delimitadores siguen devolviéndose a la fuente. El blanco se agrega al lexema pero `quitarBlancosFinales():127-131` lo elimina antes de la acción.
- **Cadenas**: los estados 5/6 no se tocan; `{id#malo}` sigue siendo cadena de una línea.
- **Identificadores válidos**: la fila 16 solo cambió la columna 20. Las columnas 0, 1, 16 y los delimitadores siguen igual. `abc_123` y `otro` no se ven afectados.
- **`AccionIdentificador.java`**: no se invoca para lexemas inválidos; por eso su regex nunca ve `#`. El nuevo estado reporta directamente.
- **`TablaSimbolos`**: ya no se insertan `id`, `malo`, `otro`, `nombre` desde los lexemas inválidos (se ve en el diff de la tabla de símbolos).
- **IR**: se sigue entregando completo según P0-02; solo desaparecen los tercetos derivados del identificador inválido.
- **`Parser.java`**: no se regeneró. El cambio es puramente léxico.

---

## 6. Estado de la Ola 1

| Prompt | Estado | Observación |
|---|---|---|
| P1-01 | **Ejecutado y commiteado** | Estado 19 + acción 37 + celda `[16][20]`. Verificado. Commit `d7de3bf`. |
| P1-02 | **Ejecutado** | `errorLexico()` usa `contexto.caracter` cuando el lexema está vacío. Ver sección 8. |
| P1-03 | Pendiente | Faltan casos de regresión en `test/tp1/verificacion_identificadores/` con aserciones sobre ausencia de ID fragmentado. |

---

## 7. Estado de git al cierre de P1-01

```text
 M AnalizadorLexico.java
 M MatrizTransiciones.java
```

Este documento era nuevo dentro de `docs/reentrega/` al cierre de P1-01. No se modificaron tests ni otros archivos del repositorio en esa corrida.

Nota posterior: el estado de git mostrado arriba fue commiteado por el usuario en `d7de3bf` (`1-01 nuevo estado y accion semantica`), junto con este documento renombrado a `P1_diagnostico_cambios.md`. La sección 8 registra el estado después de P1-02.

---

## 8. P1-02 — mensaje del error léxico residual

Fecha: 24/11/2026.  
Prompt del plan ejecutado: `P1-02`, “Robustez del mensaje”.  
Estado inicial: `d7de3bf` ya contiene P1-01. El árbol de trabajo solo tenía sin commitear este documento; P1-02 se aplicó sobre esa base.

### 8.1 Alcance según el plan

El plan indica:

```text
Raíz confirmada: errorLexico() (:122-125) usa contexto.lexema, vacío en el camino de ERROR desde estado 0.
Cambio permitido:
  - Si lexema está vacío, incluir contexto.caracter: "Lexema no reconocido '#'".
Prohibido:
  - Cambiar formato de Reporte.
  - Tocar la matriz.
Verificación:
  - Caso con '@' o '$' suelto fuera de sufijo.
Aceptación:
  - Ningún mensaje con lexema vacío.
Dependencias: P1-01.
```

Se respetaron los cuatro límites: `Reporte` intacto, `MatrizTransiciones.java` intacto, `Parser.java` intacto, tests intactos.

### 8.2 Raíz confirmada

Código antes del cambio (`AnalizadorLexico.java:126-129`):

```java
private int errorLexico(ContextoLexico contexto) { 
    reporte.error(fuente.linea(), "Lexema no reconocido '" + contexto.lexema + "'");
    return -1;
}
```

Recorrido exacto para `x := @1$ul;`:

1. `recorrerMatriz():41` lee `@`.
2. `ContextoLexico.columna():61` lo clasifica como columna 20 (“otro”).
3. `MATRIZ_ESTADOS[0][20] = -1` (`MatrizTransiciones.java:23`).
4. `recorrerMatriz():56-58` detecta `ERROR` y llama a `errorLexico()` **antes** de `append` (`:61-63`) y antes de cualquier `retroceder()` (`:64-66`).
5. `contexto.lexema` sigue siendo `""`.
6. Mensaje emitido: `Lexema no reconocido ''`.

P1-01 no cubre este camino: el estado 19 solo se alcanza desde el estado 16 (ID en curso). Con `@` suelto se entra por el estado 0 directo a `ERROR`. Por eso la evidencia de P1-01 (`x := @1$ul;` todavía con mensaje vacío) seguía vigente al iniciar P1-02.

### 8.3 Cambio efectuado

`AnalizadorLexico.java`, único archivo de código modificado.

Antes:

```java
private int errorLexico(ContextoLexico contexto) { 
    reporte.error(fuente.linea(), "Lexema no reconocido '" + contexto.lexema + "'");
    return -1;
}
```

Después:

```java
private int errorLexico(ContextoLexico contexto) { 
    String texto = contexto.lexema.length() == 0
            ? String.valueOf(contexto.caracter)
            : contexto.lexema.toString();
    reporte.error(fuente.linea(), "Lexema no reconocido '" + texto + "'");
    return -1;
}
```

**Por qué cada parte:**

1. **`String texto = ...`**  
   Centraliza la decisión sin duplicar el llamado a `reporte.error()`. Hay un solo mensaje, con una sola línea (`fuente.linea()`).

2. **`contexto.lexema.length() == 0`**  
   Es la condición exacta de la raíz. Si el lexema ya tiene contenido, el comportamiento anterior es correcto y no se toca. Evita reemplazar mensajes como `Identificador invalido 'id#malo'` o `Constante entera invalida '1'`.

3. **`String.valueOf(contexto.caracter)`**  
   `contexto.caracter` ya fue leído de la fuente y, en este camino, nunca se agregó al lexema. Es la única información disponible para identificar el carácter inválido. `String.valueOf()` explicita la conversión a texto y deja el resto de la concatenación igual.

4. **`contexto.lexema.toString()`**  
   Conserva byte a byte los mensajes existentes para lexemas no vacíos. Es la garantía de regresión: P1-02 solo cambia el caso vacío.

5. **`reporte.error(fuente.linea(), "Lexema no reconocido '" + texto + "'")`**  
   Mantiene el prefijo, las comillas y el número de línea. No se agregó ni quitó ningún campo en `Reporte`.

6. **`return -1`**  
   La recuperación sigue siendo la misma: `siguienteToken():30-36` descarta el token y sigue buscando. No se consume ni se devuelve carácter adicional.

**Caso límite analizado:** `errorLexico()` podría recibir `caracter == 0` y lexema vacío solo si se invocara en EOF. Eso no ocurre: `recorrerMatriz():42-43` devuelve `0` antes de `columnaActual()` cuando el lexema está vacío. Por eso no se emite un mensaje con NUL.

### 8.4 Diff final

```diff
--- a/AnalizadorLexico.java
+++ b/AnalizadorLexico.java
@@ -124,7 +124,10 @@ public class AnalizadorLexico {
     }
 
     private int errorLexico(ContextoLexico contexto) { 
-        reporte.error(fuente.linea(), "Lexema no reconocido '" + contexto.lexema + "'");
+        String texto = contexto.lexema.length() == 0
+                ? String.valueOf(contexto.caracter)
+                : contexto.lexema.toString();
+        reporte.error(fuente.linea(), "Lexema no reconocido '" + texto + "'");
         return -1;
     }
 
```

No hay diff en `MatrizTransiciones.java`. La fila 19 sigue:

```java
{19,19,19,99,99,99,99,99,99,99,99,99,99,99,99,99,19,99,99,99,19,99,99}
```

### 8.5 Verificación

#### 8.5.1 Reconstrucción de referencias

`/tmp` fue limpiado por el entorno entre el 23 y el 24/11: desaparecieron `/tmp/opencode/tp3build_baseline/`, `/tmp/opencode/tp3build_p1/` y `/tmp/opencode/baseline/`. Se reconstruyeron tres variantes:

```bash
mkdir -p /tmp/opencode/p1_02_refs/{pre_p1_src,build_pre_p1,p1_01_src,build_p1_01,build_baseline}
git archive HEAD~1 | tar -x -C /tmp/opencode/p1_02_refs/pre_p1_src     # cedb870, pre-P1-01
git archive HEAD   | tar -x -C /tmp/opencode/p1_02_refs/p1_01_src      # d7de3bf, con P1-01
cp -a *.java "acciones semanticas" otras_acciones /tmp/opencode/p1_02_refs/p1_01_src/
# en esa copia se revierte solo errorLexico() al texto de HEAD
javac -encoding UTF-8 -d /tmp/opencode/p1_02_refs/build_pre_p1  ...
javac -encoding UTF-8 -d /tmp/opencode/p1_02_refs/build_p1_01   ...
javac -encoding UTF-8 -d /tmp/opencode/tp3build_p1_02           ...   # árbol actual con P1-02
```

`build_p1_01` se construyó dos veces: desde `git archive HEAD` y desde una copia del árbol actual con P1-02 revertido. Ambas variantes dieron salidas idénticas; se usa la segunda como referencia P1-01 en los cuadros siguientes.

#### 8.5.2 Casos dirigidos

Fuentes creadas en `/tmp/opencode/lexprobe2/` (fuera del repo):

```text
suelto_ar.txt       x := @1$ul;
suelto_varios.txt   x := @#~?1$ul;
suelto_dolar.txt    x := 1 + $ul;
sufijo_valido.txt   x := 1$ul;
no_vacio.txt        x := 1e#;
```

Resultados:

| Caso | Pre-P1-01 | P1-01 | P1-02 |
|---|---|---|---|
| `suelto_ar.txt` | `Lexema no reconocido ''` | `Lexema no reconocido ''` | `Lexema no reconocido '@'` |
| `suelto_varios.txt` | cuatro veces `''` | cuatro veces `''` | `'@'`, `'#'`, `'~'`, `'?'` |
| `suelto_dolar.txt` | sin error léxico | sin error léxico | sin error léxico |
| `sufijo_valido.txt` | sin errores | sin errores | sin errores |
| `no_vacio.txt` | `Constante entera invalida '1'` + `Lexema no reconocido ''` | `Constante entera invalida '1'` + `Identificador invalido 'e#'` | igual a P1-01 |

Salida exacta de `suelto_varios.txt` con P1-02:

```text
Línea 4: Error: Lexema no reconocido '@'
Línea 4: Error: Lexema no reconocido '#'
Línea 4: Error: Lexema no reconocido '~'
Línea 4: Error: Lexema no reconocido '?'
```

Con P1-01, las cuatro líneas idénticas terminaban en `''`.

**Nota sobre `no_vacio.txt`:** el paso de `Lexema no reconocido ''` a `Identificador invalido 'e#'` es efecto de P1-01 (el `#` pegado a `e` ahora deriva al estado 19), no de P1-02. P1-02 lo deja intacto porque el lexema no está vacío.

**Nota sobre `$` suelto:** `x := 1 + $ul;` no produce error léxico en ninguna variante. `AnalizadorLexico.java:42` intercepta `$` en estado 0 como centinela de fin de entrada y devuelve `0`; el parser informa errores sintácticos (`Constante entera invalida '1'`, `Asignacion mal formada`, `Falta ';'...`, `Falta de delimitador END...`), pero no hay mensaje con lexema vacío. Ese comportamiento es previo y queda fuera de P1-02: cambiarlo implicaría tocar `:42` o la matriz, ambos fuera del prompt. La aceptación “ningún mensaje con lexema vacío” se cumple también acá, de forma vacua.

#### 8.5.3 Suite completa

Se copiaron `test/` y `test_tp_4/` a `/tmp/opencode/p1_02_refs/` para no escribir `.asm` dentro del repo. Se ejecutaron los 294 archivos `.txt` con P1-01 y con P1-02.

```bash
find /tmp/opencode/p1_02_refs/test /tmp/opencode/p1_02_refs/test_tp_4 -name '*.txt' -print0 |
while IFS= read -r -d '' f; do
  java -cp /tmp/opencode/p1_02_refs/build_p1_01 Main "$f" > ".../$rel.out" 2>&1
  java -cp /tmp/opencode/tp3build_p1_02           Main "$f" > ".../$rel.out" 2>&1
done
```

Comparación tras eliminar las líneas `Lexema no reconocido`:

```text
Total .txt ejecutados: 294
Diffs tras quitar lineas "Lexema no reconocido": 0
Archivos con cambio solo en mensaje léxico: 2
```

Los dos únicos archivos con diff:

| Archivo | P1-01 | P1-02 |
|---|---|---|
| `test/TP3_y_TP4/CADENAS_COMENTARIOS/07_cadena_salto_de_linea.txt` | `Línea 11: Error: Lexema no reconocido ''` | `Línea 11: Error: Lexema no reconocido '}'` |
| `test/tp1/verificaciones_comentarios_cadenas/cadenas_invalidas.txt` | `Línea 7: Error: Lexema no reconocido ''` | `Línea 7: Error: Lexema no reconocido '}'` |

En ambos casos el carácter residual era un `}` sobrante tras una cadena no cerrada. Eran los dos últimos mensajes con lexema vacío que quedaban en toda la suite después de P1-01.

Además, la búsqueda directa sobre la suite completa:

```text
P1-01 mensajes con lexema vacío: 2
P1-02 mensajes con lexema vacío: 0
```

#### 8.5.4 ASM

Generados en árboles separados para no sobrescribirlos:

```text
P1-01  f8bdc552ab3eb68882d09561cada48989862ce939212c4d498fc66955b1d03f9  01_funcion_ulongint.asm
P1-01  aac7910fcdfe8bccf5b1675545296b849b815a9a0249b99c1f018937df2dd442  03_recursion_factorial_metodo.asm
P1-02  f8bdc552ab3eb68882d09561cada48989862ce939212c4d498fc66955b1d03f9  01_funcion_ulongint.asm
P1-02  aac7910fcdfe8bccf5b1675545296b849b815a9a0249b99c1f018937df2dd442  03_recursion_factorial_metodo.asm
```

Idénticos a los hashes registrados en la corrida P0. P1-02 no llega al generador de assembler.

#### 8.5.5 Criterios de aceptación del plan

| Criterio | Cumplido | Evidencia |
|---|---|---|
| Si el lexema está vacío, se incluye `contexto.caracter` | sí | `suelto_ar.txt`, `suelto_varios.txt` |
| Formato de `Reporte` sin cambios | sí | mismo prefijo, comillas y `fuente.linea()` |
| Matriz sin cambios | sí | `git diff -- MatrizTransiciones.java` vacío |
| Ningún mensaje con lexema vacío en la suite | sí | 2 → 0 en 294 archivos |
| IR sin cambios | sí | 0 diffs tras quitar líneas de mensaje |
| ASM sin cambios | sí | hashes idénticos |

### 8.6 Efectos colaterales analizados

- **`Reporte`**: no se agregó campo ni se cambió nivel. Solo cambia el contenido del string en un caso.
- **Recuperación**: `return -1` y `siguienteToken():30-36` intactos. No se consume ni se devuelve ningún carácter extra.
- **`case 37`**: intacto. Los IDs inválidos pegados siguen reportando el lexema completo, no el carácter suelto.
- **Matriz / fila 19**: intacta. La discusión previa sobre `[16][20] = -1` queda descartada por fragmentación; P1-02 ataca el otro camino, el del carácter suelto desde estado 0.
- **Cadenas y comentarios**: los estados 5/6 no se tocan. El cambio en los dos tests de cadena no cerrada es solo el texto del mensaje; el `}` no se inserta en ningún lexema ni tabla de símbolos.
- **`Parser.java`**: no se regeneró.
- **Tests del repo**: no se modificaron; las copias usadas para verificación quedaron en `/tmp`.

### 8.7 Estado de la Ola 1 después de P1-02

| Prompt | Estado | Observación |
|---|---|---|
| P1-01 | Ejecutado y commiteado (`d7de3bf`) | Estado 19 + acción 37 + celda `[16][20]`. |
| P1-02 | **Ejecutado** | `errorLexico()` con fallback a `contexto.caracter`. Suite 294/294 sin diffs de IR. |
| P1-03 | Pendiente | Agregar casos y aserciones en `test/tp1/verificacion_identificadores/`; debe cubrir ID fragmentado y también carácter suelto. |

### 8.8 Estado de git al cierre de P1-02

```text
 M AnalizadorLexico.java
 M docs/reentrega/P1_diagnostico_cambios.md
```

`MatrizTransiciones.java` ya no aparece como modificado porque P1-01 fue commiteado. El único diff de código pendiente es el de `errorLexico()`. No se agregaron ni borraron archivos de test.

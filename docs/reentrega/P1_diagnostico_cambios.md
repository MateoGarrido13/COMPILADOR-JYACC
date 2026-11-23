# Ola 1 — diagnóstico y cambios

Prompt ejecutado en esta corrida: **P1-01** (estado dedicado “ID inválido”).  
Prompts `P1-02` y `P1-03`: **pendientes**, con estado registrado al final.  
Fecha: 23/11/2026.

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
| P1-01 | **Ejecutado** | Estado 19 + acción 37 + celda `[16][20]`. Verificado. |
| P1-02 | Pendiente | Falta incluir `contexto.caracter` en `errorLexico()` para caracter inválido **suelto**. Evidencia actual: `x := @1$ul;` todavía da `Lexema no reconocido ''`. |
| P1-03 | Pendiente | Faltan casos de regresión en `test/tp1/verificacion_identificadores/` con aserciones sobre ausencia de ID fragmentado. |

---

## 7. Estado de git al cierre de P1-01

```text
 M AnalizadorLexico.java
 M MatrizTransiciones.java
```

Este documento es nuevo dentro de `docs/reentrega/`. No se modificaron tests ni otros archivos del repositorio.

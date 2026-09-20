# Informe de pruebas TP1 — verificaciones léxico-semánticas

**Proyecto:** compilador (temas 6, 8, 9, 16)  
**Fecha de ejecución:** 20/09/2026  
**Estado del código:** snapshot actual del repositorio  
**Entradas:** `test/<caso>/fuente.txt`  
**Salidas del compilador:** `tokens.txt`, `errores.txt`, `tabla_simbolos.txt` y demás reportes en la misma carpeta de cada caso.

Este informe comprueba si el estado actual contempla las verificaciones pedidas para constantes, identificadores, palabras reservadas, comentarios y cadenas. Cada caso se ejecutó con `java Main test/<caso>/fuente.txt`. El veredicto se basa en esos archivos de salida, no en una lectura estática del código.

## Tipos y rangos que implementa el compilador

| Tipo | Forma léxica | Rango aceptado |
| --- | --- | --- |
| `ulongint` (tema 6) | dígitos + sufijo `$ul` / `$UL` | `0` … `4294967295` (`2^32 - 1`) |
| `doublef` (tema 8) | punto decimal obligatorio; exponente opcional con `d` / `D` | `0` y magnitudes en `[2.2250738585072014e-308 , 1.7976931348623157e+308]` |
| identificador | empieza en minúscula; letras, dígitos y `_` | longitud máxima 22 (exceso: warning + truncado) |
| cadena (tema 9) | `{ … }` en una sola línea | no admite salto de línea interno |
| comentario (tema 16) | `{{ … }}` multilínea | se descarta; no genera token |

## Resumen de resultados

| # | Caso | ¿Lo contempla? | Evidencia principal |
| --- | --- | --- | --- |
| 01 | `ulongint` primer y último valor **dentro** de rango | **Sí** | 0 errores; tokens `0$ul` y `4294967295$ul` |
| 02 | `ulongint` primer y último valor **fuera** de rango | **Sí** | errores de rango en `4294967296$ul` y `-1$ul` |
| 03 | `doublef` primer y último valor **dentro** de rango (también 0 y extremos negativos) | **Sí** | 0 errores; los cinco literales aparecen como `Constante doublef` |
| 04 | `doublef` primer y último valor **fuera** de rango (positivo y negativo) | **Sí** | underflow `2.2d-308` y overflow `1.8d+308`, con y sin signo |
| 05 | Formatos de `doublef` | **Parcial** | 7/8 formas válidas; `12.d+2` no se reconoce como un solo literal |
| 06 | Identificadores de menos y más de 22 caracteres | **Sí** | `<22` y `=22` OK; `23` → warning y truncado |
| 07 | Identificadores con letras, dígitos y `_` | **Sí** | `var_1`, `abc123`, `a_b_c`, `x1_y2_z3` sin errores |
| 08 | Carácter ajeno a letra, dígito o `_` | **Parcial** | `#` y `@` se rechazan, pero partiendo el lexema; el mensaje es `Lexema no reconocido ''` |
| 09 | Palabras reservadas en minúsculas | **Sí** | 0 errores; `if`, `else`, `end_if`, `begin`, `repeat`, `while`, `todf`, … |
| 10 | Palabras reservadas en mayúsculas | **Sí** | 0 errores; `IF`, `BEGIN`, `ULONGINT`, `END_IF`, `$UL`, … se aceptan |
| 11 | Comentarios bien escritos | **Sí** | 0 errores; el cuerpo del comentario no aparece en `tokens.txt` |
| 12 | Comentarios mal escritos (sin cierre) | **Sí** | 1 error: comentario absorbido hasta EOF |
| 13 | Cadenas bien escritas | **Sí** | 0 errores; `{hola}`, `{texto con espacios 123}`, `{simbolos = + - * /}` |
| 14 | Cadenas mal escritas | **Sí** | `Cadena no cerrada` ante salto de línea y ante EOF |
| 15 | Comentario que contiene `_` | **Sí** | 0 errores; el comentario `{{ … guion_bajo … }}` se descarta |
| 16 | Cadena que contiene `_` | **Sí** | 0 errores; token `Cadena {cadena_con_guion}` |

**Conclusión corta:** el compilador **sí contempla** rangos de constantes (ambos tipos, extremos internos y externos), longitud de identificadores, forma alfanumérica con `_`, reservadas en ambos casos, comentarios `{{ }}` y cadenas `{ }` de una línea (incluido `_` en el texto). Quedan **dos huecos**: exponente sobre `N.` sin dígito decimal, y caracteres inválidos en identificadores reportados de forma opaca.

---

## 1. Constantes `ulongint` — extremos del rango

### 01 — Dentro de rango (`test/01_ulongint_limites_validos`)

Programa: asigna `0$ul` (mínimo) y `4294967295$ul` (máximo).

```
Errores: 0 - Warnings: 0
```

`tokens.txt`:

```
Constante ulongint 0$ul
Constante ulongint 4294967295$ul
```

**Veredicto: cumple.** Los dos bordes legales se aceptan y se tipan como `ulongint`.

### 02 — Fuera de rango (`test/02_ulongint_limites_invalidos`)

Programa: `4294967296$ul` (máximo + 1) y `-1$ul` (mínimo − 1, signo unario).

```
Linea 6: Error: Constante entera fuera de rango '4294967296$ul'
Linea 6: Error: Asignacion mal formada
Linea 7: Error: Constante entera fuera de rango '-1$ul'
Linea 7: Error: Asignacion mal formada
```

Ninguna de las dos constantes llega a `tokens.txt` (solo quedan `:=` y el resto del programa). El segundo error de cada línea es recuperación del parser, no un falso positivo de rango.

**Veredicto: cumple.** Detecta el primer valor por debajo de 0 y el primero por encima de `2^32-1`. El signo negativo se valida en la misma acción léxica cuando el autómata consume `-` junto al literal.

---

## 2. Constantes `doublef` — extremos del rango

### 03 — Dentro de rango (`test/03_doublef_limites_validos`)

Se probaron `0.0`, el mínimo normal IEEE-754, el máximo, y ambos extremos con signo negativo.

```
Errores: 0 - Warnings: 0
```

`tokens.txt` entrega los cinco literales:

```
Constante doublef 0.0
Constante doublef 2.2250738585072014d-308
Constante doublef 1.7976931348623157d+308
Constante doublef -2.2250738585072014d-308
Constante doublef -1.7976931348623157d+308
```

**Veredicto: cumple.**

### 04 — Fuera de rango (`test/04_doublef_limites_invalidos`)

| Literal | Motivo |
| --- | --- |
| `2.2d-308` | menor que el mínimo normal (underflow no nulo) |
| `1.8d+308` | mayor que el máximo (overflow → infinito) |
| `-2.2d-308` / `-1.8d+308` | mismos extremos con signo |

```
Linea 6: Error: Constante doublef fuera de rango '2.2d-308'
Linea 7: Error: Constante doublef fuera de rango '1.8d+308'
Linea 8: Error: Constante doublef fuera de rango '-2.2d-308'
Linea 9: Error: Constante doublef fuera de rango '-1.8d+308'
```

**Veredicto: cumple.**

---

## 3. Formatos de punto flotante

Caso `test/05_doublef_formatos`. Combinaciones pedidas por el enunciado:

| Forma | Ejemplo | ¿Aceptada? | Token observado |
| --- | --- | --- | --- |
| Parte entera **con** decimal | `12.34` | Sí | `Constante doublef 12.34` |
| Parte entera **sin** decimal | `12.` | Sí | `Constante doublef 12.` |
| Parte decimal **sin** entera | `.34` | Sí | `Constante doublef .34` |
| Sin exponente | `12.34` | Sí | (misma fila) |
| Con exponente sin signo | `12.34d10` | Sí | `Constante doublef 12.34d10` |
| Exponente **positivo** | `12.34d+10` | Sí | `Constante doublef 12.34d+10` |
| Exponente **negativo** | `12.34d-10` | Sí | `Constante doublef 12.34d-10` |
| Decimal sin entera + exp. negativo | `.5d-1` | Sí | `Constante doublef .5d-1` |
| Entera sin dígito decimal + exponente | `12.d+2` | **No** | se parte en `12.` + `d` + error `'+2'` |

Errores del caso (solo la última combinación):

```
Linea 12: Error: Falta ';' al final de la sentencia
Linea 12: Error: Constante entera invalida '+2'
Linea 12: Error: Sentencia mal formada
```

`tokens.txt` correspondiente:

```
Constante doublef 12.
Identificador d
```

La letra `d` solo se toma como exponente si **ya hay un dígito después del punto**. Por eso `12.0d+2` sería legal y `12.d+2` no.

**Veredicto: parcial.** Cubre todas las formas pedidas por separado; falla el cruce “parte entera sin dígitos decimales **y** exponente”.

---

## 4. Identificadores

### 06 — Longitud (`test/06_identificadores_longitud`)

| Lexema | Longitud | Resultado |
| --- | --- | --- |
| `corto` | 5 | aceptado |
| `abcdefghij1234567890ab` | 22 | aceptado tal cual |
| `abcdefghij1234567890abc` | 23 | warning + truncado a 22 |

```
Linea 4: Warning: El identificador abcdefghij1234567890abc fue truncado a: abcdefghij1234567890ab
Linea 8: Warning: El identificador abcdefghij1234567890abc fue truncado a: abcdefghij1234567890ab
```

En `tokens.txt` y `tabla_simbolos.txt` solo queda `abcdefghij1234567890ab` (las dos declaraciones colisionan en la misma entrada). No hay error, solo warning.

**Veredicto: cumple.**

### 07 — Letras, dígitos y `_` (`test/07_identificadores_alfanumericos`)

```
Errores: 0 - Warnings: 0
```

Tokens: `var_1`, `abc123`, `a_b_c`, `x1_y2_z3`.

**Veredicto: cumple.** El `_` es legal **dentro de un identificador**.

### 08 — Carácter inválido (`test/08_identificador_caracter_invalido`)

Fuente: `id#malo` y `otro@nombre`.

```
Linea 3: Error: Lexema no reconocido ''
Linea 5: Error: Lexema no reconocido ''
Linea 6: Error: Lexema no reconocido ''
```

`tokens.txt` muestra el corte:

```
Identificador id
Identificador malo
Identificador otro
Identificador nombre
```

`#` y `@` no entran al lexema: el autómata cierra el identificador y después marca “otro” como error, con mensaje vacío. No se emite `Identificador invalido 'id#malo'`.

**Veredicto: parcial.** El carácter ilegal no se acepta (correcto), pero el diagnóstico es pobre y el nombre se parte en dos identificadores.

---

## 5. Palabras reservadas

Reservadas ejercitadas: `ulongint`, `doublef`, `begin`, `end`, `if`, `else`, `end_if`, `pout`, `repeat`, `while`, `todf`.

### 09 — Minúsculas (`test/09_reservadas_minusculas`)

```
Errores: 0 - Warnings: 0
```

Todos los tokens salen como `Palabra reservada …`.

**Veredicto: cumple.**

### 10 — Mayúsculas (`test/10_reservadas_mayusculas`)

Fuente con `ULONGINT`, `DOUBLEF`, `BEGIN`, `TODF`, `IF`, `POUT`, `ELSE`, `END_IF`, `REPEAT`, `WHILE`, `END` y sufijo `$UL`.

```
Errores: 0 - Warnings: 0
```

El léxico las resuelve por `toLowerCase()`: en `tokens.txt` figuran en minúscula (`Palabra reservada if`, `Constante ulongint 1$UL`, …) y el parser las acepta.

**Veredicto: cumple.** El lenguaje es insensible a mayúsculas/minúsculas en reservadas y en el sufijo `$ul`.

---

## 6. Comentarios

### 11 — Bien escritos (`test/11_comentarios_validos`)

Un comentario de una línea, uno multilínea y uno embebido en el bloque `begin`.

```
Errores: 0 - Warnings: 0
```

`tokens.txt` no contiene texto de comentario (15 tokens: solo el programa).

**Veredicto: cumple.**

### 12 — Mal escritos (`test/12_comentarios_invalidos`)

`{{` sin `}}`. El resto del archivo se come como comentario. Al EOF:

```
Linea 8: Error: Lexema no reconocido '{{ este comentario nunca se cierra
    x := 0$ul;
    pout({mal});
end
'
```

Solo se tokenizaron `prog`, `ulongint`, `x`, `;`, `begin`.

**Veredicto: cumple.** Se detecta el comentario abierto.

### 15 — `_` dentro de un comentario (`test/15_guion_bajo_en_comentario_y_cadena`)

Re-ejecutado tras corregir la columna `_` (16) en los estados 5–8 de `MatrizTransiciones`.

```
Errores: 0 - Warnings: 0
```

El comentario `{{ comentario con guion_bajo interno }}` se descarta (no aparece en `tokens.txt`). El `_` permanece en estado 7 (comentario) y no aborta el lexema.

**Veredicto: cumple.**

---

## 7. Cadenas

### 13 — Bien escritas (`test/13_cadenas_validas`)

```
Errores: 0 - Warnings: 0
```

```
Cadena {hola}
Cadena {texto con espacios 123}
Cadena {simbolos = + - * /}
```

**Veredicto: cumple.**

### 14 — Mal escritas (`test/14_cadenas_invalidas`)

1. Salto de línea interno: `{cadena` / `con salto}`  
2. Sin cierre hasta EOF: `{sin cierre`

```
Linea 6: Error: Cadena no cerrada '{cadena'
Linea 8: Error: Cadena no cerrada '{sin cierre'
```

Tras el salto, `con` y `salto` se leen como identificadores sueltos (el `}` cierra tarde y ya no forma parte del literal).

**Veredicto: cumple.** Se rechazan cadena con newline y cadena no cerrada.

### 16 — `_` dentro de una cadena (`test/16_cadena_con_guion_bajo`)

```
Errores: 0 - Warnings: 0
```

```
Cadena {cadena_con_guion}
```

El `_` permanece en estado 6 (cadena). El caso 15 también entrega el mismo token al imprimir `{cadena_con_guion}`.

**Veredicto: cumple.**

---

## Hallazgos que no estaban en la lista, pero salieron al ejecutar

1. **Errores en cascada.** Cuando una constante se descarta, el parser suele sumar `Asignacion mal formada` o `Sentencia mal formada`. El error léxico-semántico de rango es el relevante.
2. **Sufijo y reservadas case-insensitive.** `$UL` y `IF` funcionan; el informe de tokens normaliza la reservada a minúscula y deja el lexema original de la constante.
3. **`_` en comentarios y cadenas.** Corregido: la columna 16 de los estados 5–8 ahora sigue en cadena/comentario (antes era error). Casos 15 y 16 pasan con 0 errores.
4. **Exponente sobre `N.`.** `12.d+2` no es un `doublef`; `12.0d+2` sí lo sería. Documentar o relajar `tieneDigitoTrasPunto`.

---

## Conclusión

El estado actual **contempla las verificaciones de TP1** sobre:

- rangos de `ulongint` y `doublef` (bordes internos y externos, incluidos negativos);
- formatos habituales de `doublef` (entera/decimal, con/sin exponente, exponente `+`/`-`);
- identificadores cortos, de 22 y de más de 22 caracteres (truncado con warning);
- identificadores con letras, dígitos y `_`;
- palabras reservadas en minúsculas y mayúsculas;
- comentarios `{{ }}` bien y mal cerrados, también con `_` en el texto;
- cadenas `{ }` bien escritas (incluido `_`), con newline y sin cierre.

**No queda cubierto del todo:**

| Hueco | Caso | Qué habría que ajustar |
| --- | --- | --- |
| `12.d+2` no es un literal | 05 | Tratar `d` como exponente también después de `N.` |
| `#` / `@` parten el identificador y el error va vacío | 08 | Mensaje con el carácter y, si se desea, lexema completo |

Para repetir la corrida:

```bash
javac -encoding UTF-8 *.java "acciones semanticas"/*.java otras_acciones/*.java
for d in test/*/; do java -cp ".:acciones semanticas:otras_acciones" Main "${d}fuente.txt"; done
```

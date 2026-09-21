# Fuente de prueba y resultado esperado (TP2)

## Comando

Desde la raíz del proyecto (`compiUltimo`):

```bash
javac -encoding UTF-8 *.java "acciones semanticas"/*.java otras_acciones/*.java
java -cp ".:acciones semanticas:otras_acciones" Main input.txt
```

Si no se pasa parámetro, `Main` usa `input.txt` por defecto:

```bash
java -cp ".:acciones semanticas:otras_acciones" Main
```

## Qué cubre `input.txt`

Programa sintácticamente válido (sin errores léxicos ni sintácticos) con un warning de truncamiento. Temas del grupo:

| Tema | Construcción en el fuente |
|------|---------------------------|
| 6 | Constantes `ulongint` con `$ul` (incluye el máximo `4294967295$ul`) |
| 8 | `doublef` (`1.0`, `.6d-1`) y constante negativa `-2.5` |
| 9 / 16 | Cadenas `{ok}` / `{hola}` y comentarios `{{ ... }}` |
| 13 | `repeat ... while (condicion);` |
| 17 | Asignación en expresión `y = (expr)` |
| 19 | Invocación `foo(n=1$ul, 2.5)[1$ul, 2$ul]` |
| 23 | `typedef pares = [2$ul, 4$ul, 6$ul];` y `pares p1, p2;` |
| 25 | Clases `base` / `hija` sin código de estructura |
| 28 | Acceso `obj.a` |
| 31 | `extends base` y acceso prefijado `obj.base.a` |
| 33 | `todf(x)` |

El identificador `contador_identificador_muy_largo` supera 22 caracteres: el léxico lo trunca a `contador_identificador` y emite warning (líneas 4 y 38).

## Forma de la salida

Cuatro bloques en consola (y los mismos cuerpos en `tokens.txt`, `estructuras.txt`, `errores.txt` y `tabla_simbolos.txt` al lado de `input.txt`):

1. Tokens del léxico
2. Estructuras sintácticas con número de línea
3. Errores / warnings (acá solo warnings de truncamiento)
4. Tabla de símbolos

No debe aparecer `Compilando:`, ni conteos (`Tokens detectados: N`), ni dumps de reglas.

## Salida esperada en consola

```
Tokens detectados por el Analizador Léxico:
Identificador demo_tp2
Palabra reservada ulongint
Identificador x
,
Identificador y
,
Identificador contador_identificador
;
Palabra reservada doublef
Identificador z
;
Palabra reservada typedef
Identificador pares
=
[
Constante ulongint 2$ul
,
Constante ulongint 4$ul
,
Constante ulongint 6$ul
]
;
Identificador pares
Identificador p1
,
Identificador p2
;
Palabra reservada class
Identificador base
Palabra reservada begin
Palabra reservada ulongint
Identificador a
;
Palabra reservada ulongint
Identificador m
(
Palabra reservada ulongint
Identificador pa
)
Palabra reservada begin
Palabra reservada ret
(
Identificador pa
)
;
Palabra reservada end
;
Palabra reservada end
;
Palabra reservada class
Identificador hija
Palabra reservada begin
Palabra reservada ulongint
Identificador b
;
Palabra reservada extends
Identificador base
;
Palabra reservada end
;
Identificador hija
Identificador obj
;
Palabra reservada ulongint
Palabra reservada function
Identificador foo
(
Palabra reservada ulongint
Identificador n
,
Palabra reservada doublef
Identificador d
)
Palabra reservada begin
Palabra reservada ret
(
Identificador n
)
;
Palabra reservada end
;
Palabra reservada begin
Identificador x
Operador de asignacion :=
Constante ulongint 0$ul
;
Identificador y
Operador de asignacion :=
Constante ulongint 4294967295$ul
;
Identificador z
Operador de asignacion :=
Constante doublef 1.0
;
Identificador z
Operador de asignacion :=
Constante doublef .6d-1
;
Identificador z
Operador de asignacion :=
Constante doublef -2.5
;
Identificador x
Operador de asignacion :=
Identificador y
=
(
Constante ulongint 2$ul
)
;
Identificador x
Operador de asignacion :=
Identificador y
=
(
Identificador x
+
Identificador y
)
*
Identificador contador_identificador
=
(
Constante ulongint 1$ul
)
;
Palabra reservada if
(
Identificador x
=
(
Constante ulongint 1$ul
)
Comparador ==
Constante ulongint 1$ul
)
Palabra reservada pout
(
Cadena {ok}
)
;
Palabra reservada end_if
;
Identificador z
Operador de asignacion :=
Palabra reservada todf
(
Identificador x
)
;
Identificador x
Operador de asignacion :=
Identificador foo
(
Identificador n
=
Constante ulongint 1$ul
,
Constante doublef 2.5
)
[
Constante ulongint 1$ul
,
Constante ulongint 2$ul
]
;
Identificador p1
Operador de asignacion :=
Constante ulongint 2$ul
;
Identificador obj
.
Identificador a
Operador de asignacion :=
Constante ulongint 3$ul
;
Identificador obj
.
Identificador base
.
Identificador a
Operador de asignacion :=
Constante ulongint 4$ul
;
Identificador x
Operador de asignacion :=
Identificador obj
.
Identificador m
(
Constante ulongint 5$ul
)
;
Palabra reservada repeat
Identificador x
Operador de asignacion :=
Identificador x
+
Constante ulongint 1$ul
;
Palabra reservada while
(
Identificador x
<
Constante ulongint 3$ul
)
;
Palabra reservada if
(
Identificador x
>
Constante ulongint 0$ul
)
Palabra reservada pout
(
Cadena {hola}
)
;
Palabra reservada else
Palabra reservada pout
(
Identificador x
)
;
Palabra reservada end_if
;
Palabra reservada end

Línea 4: Declaracion de variables
Línea 5: Declaracion de variables
Línea 6: Definicion de tipo enumerado
Línea 7: Declaracion de objetos
Línea 11: Declaracion de atributo
Línea 14: Sentencia RET
Línea 15: Declaracion de metodo
Línea 16: Declaracion de clase
Línea 20: Declaracion de atributo
Línea 21: Sentencia EXTENDS
Línea 22: Declaracion de clase
Línea 24: Declaracion de objetos
Línea 28: Sentencia RET
Línea 29: Declaracion de funcion
Línea 32: Asignación
Línea 33: Asignación
Línea 34: Asignación
Línea 35: Asignación
Línea 36: Asignación
Línea 37: Asignacion en expresion
Línea 37: Asignación
Línea 38: Asignacion en expresion
Línea 38: Asignacion en expresion
Línea 38: Asignación
Línea 39: Asignacion en expresion
Línea 40: Sentencia POUT
Línea 41: Sentencia IF
Línea 42: Conversion explicita todf
Línea 42: Asignación
Línea 43: Invocacion a funcion
Línea 43: Asignación
Línea 44: Asignación
Línea 45: Acceso a atributo
Línea 45: Asignación
Línea 46: Acceso a atributo con prefijo de clase
Línea 46: Asignación
Línea 47: Invocacion a metodo
Línea 47: Asignación
Línea 49: Asignación
Línea 50: Sentencia REPEAT - WHILE
Línea 52: Sentencia POUT
Línea 54: Sentencia POUT
Línea 55: Sentencia IF con ELSE
Línea 56: Bloque BEGIN - END
Línea 56: Programa

Línea 4: Warning: El identificador contador_identificador_muy_largo fue truncado a: contador_identificador
Línea 38: Warning: El identificador contador_identificador_muy_largo fue truncado a: contador_identificador

Contenido de la tabla de simbolos:
if -> 400
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
else -> 401
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
end_if -> 402
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
begin -> 403
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
end -> 404
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
pout -> 405
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
ret -> 406
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
class -> 407
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
function -> 408
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
repeat -> 409
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
while -> 410
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
todf -> 411
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
typedef -> 412
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
extends -> 413
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
ulongint -> 414
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
doublef -> 415
Numero de linea: 0
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
demo_tp2 -> 300
Numero de linea: 3
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
x -> 300
Numero de linea: 4
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
y -> 300
Numero de linea: 4
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
contador_identificador -> 300
Numero de linea: 4
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
z -> 300
Numero de linea: 5
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
pares -> 300
Numero de linea: 6
Tipo de dato: tipo enumerado
Direccion de memoria: 0
--------------------------------
2$ul -> 302
Numero de linea: 6
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
4$ul -> 302
Numero de linea: 6
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
6$ul -> 302
Numero de linea: 6
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
p1 -> 300
Numero de linea: 7
Tipo de dato: pares
Direccion de memoria: 0
--------------------------------
p2 -> 300
Numero de linea: 7
Tipo de dato: pares
Direccion de memoria: 0
--------------------------------
base -> 300
Numero de linea: 9
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
a -> 300
Numero de linea: 11
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
m -> 300
Numero de linea: 12
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
pa -> 300
Numero de linea: 12
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
hija -> 300
Numero de linea: 18
Tipo de dato: indefinido
Direccion de memoria: 0
--------------------------------
b -> 300
Numero de linea: 20
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
obj -> 300
Numero de linea: 24
Tipo de dato: hija
Direccion de memoria: 0
--------------------------------
foo -> 300
Numero de linea: 26
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
n -> 300
Numero de linea: 26
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
d -> 300
Numero de linea: 26
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
0$ul -> 302
Numero de linea: 32
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
4294967295$ul -> 302
Numero de linea: 33
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
1.0 -> 302
Numero de linea: 34
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
.6d-1 -> 302
Numero de linea: 35
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
-2.5 -> 302
Numero de linea: 36
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
1$ul -> 302
Numero de linea: 38
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
ok -> 303
Numero de linea: 40
Tipo de dato: cadena
Direccion de memoria: 0
--------------------------------
2.5 -> 302
Numero de linea: 43
Tipo de dato: doublef
Direccion de memoria: 0
--------------------------------
3$ul -> 302
Numero de linea: 45
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
5$ul -> 302
Numero de linea: 47
Tipo de dato: ulongint
Direccion de memoria: 0
--------------------------------
hola -> 303
Numero de linea: 52
Tipo de dato: cadena
Direccion de memoria: 0
--------------------------------
```

## Criterio rápido de éxito

- Cero líneas `Error:`.
- Exactamente dos `Warning:` de truncamiento (líneas 4 y 38).
- Aparecen `Asignación`, `Sentencia IF`, `Sentencia REPEAT - WHILE`, `Invocacion a funcion`, `Acceso a atributo con prefijo de clase` y `Conversion explicita todf`.
- En la tabla: `contador_identificador` (truncado), constante `-2.5` con tipo `doublef`, `obj` con tipo `hija`.

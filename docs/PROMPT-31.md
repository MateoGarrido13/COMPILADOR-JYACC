# Prompt 3.10: herencia múltiple con prefijo

> **Nomenclatura:** **tema 31** (regla del grupo) = **Prompt 3.10** del plan en
> `docs/Promt.md`. Este archivo (`PROMPT-31.md`) es el registro de implementación; la
> decisión D3 previa está en `docs/PROMPT-3.10.md`.

## Alcance

Se implementó el Tema 31 con la decisión D3 ya cerrada en `docs/DEC-5.md` y
`docs/PROMPT-3.10.md`. Un miembro propio se usa sin prefijo. Un miembro
heredado exige `objeto.clase.miembro`. El prefijo nombra la clase que declara
el miembro, y esa clase tiene que ser la del objeto o una base directa o
transitiva.

## Herencia

`SENTENCIA_EXTENDS` resuelve cada base con Name Mangling desde el ámbito de la
clase. La base tiene que existir y tener uso `CLASE`. No puede ser la misma
clase, no puede cerrar un ciclo y no puede repetirse. Las bases válidas se
guardan en `heredaDe` como clave `nombre:ambito`, en el orden del fuente.

Mensajes:

- `clase base no declarada 'noexiste'`
- `'x' no es una clase`
- `herencia circular entre 'a' y 'a'`
- `clase base repetida 'padre'`

Si `a` nombra a `b` antes de que `b` exista, se informa la base no declarada y
se recuerda el nombre. Cuando `b` después hereda de `a`, el error es
`herencia circular entre 'b' y 'a'`.

## Atributos y métodos

`objeto.miembro` busca solo en la clase del objeto. Si el miembro no está ahí
y sí en un ancestro, el error es `falta de prefijo para 'y' en la clase 'hija'`.
Si no está en la cadena, sigue el error de inexistencia:
`atributo 'x' no existe en la clase 'ca'`.

`objeto.clase.miembro` exige que `clase` sea la del objeto o una base, y que
el miembro esté declarado en esa clase. No alcanza con que un ancestro de la
clase prefijada lo declare.

- Prefijo ajeno: `el prefijo 'nohereda' no es una clase heredada de 'hija'`
- Atributo ausente en la clase nombrada: `atributo 'noexiste' no existe en la clase 'padre'`
- Método ausente en la clase nombrada: `metodo 'noexiste' no existe en la clase 'padre'`

La misma regla vale para la lectura, la asignación y la invocación. Dos bases
con el mismo miembro no se aceptan sin prefijo: el error es falta de prefijo,
y el prefijo elige la base. Un miembro propio gana sobre uno heredado del
mismo nombre y sigue usándose sin prefijo.

Al declarar un objeto se materializan sus atributos en la Tabla de Símbolos.
El atributo propio queda como `objeto.atributo:ambito`; el heredado agrega la
clase que lo declara: `objeto.clase.atributo:ambito`. Así `hija a1` con un
atributo propio `x` y una base `padre` con `y` produce `a1.x:main` y
`a1.padre.y:main`, ambos con su tipo y uso `atributo`. Dos bases con el mismo
miembro generan entradas separadas (`a1.b1.y:main` y `a1.b2.y:main`). El
detalle está en `docs/DEC-10.md`.

## Llamadas

Una invocación aceptada chequea cantidad, orden y tipos como el Tema 19, y
emite `(PAR, formal, real)` seguido de `(CALLMET, objeto, metodo)`. El método
es la entrada de la clase que lo declara, por ejemplo
`(CALLMET, a1:main, m:main:padre)`.

`a1.m(...)` sobre un método solo heredado es `falta de prefijo para 'm' en la clase 'hija'`
y no emite `CALLMET`. Si además falta el orden, se informan los dos errores.
`GeneradorTercetos`, `LlamadaFuncion` y `ParametroReal` ya tenían esa
convención; este tema les entrega la firma resuelta.

## Pruebas

Casos en `test/tp2/verificacion_r28_r31_atributos/`.

| Caso | Resultado |
|---|---|
| `desambiguado_tradicional_y.txt` | Válido. `(:=, a1.padre.y, 1$ul)`, `(:=, a1.hija.y, 2$ul)`, `(pout, a1.padre.y, --)`. |
| `metodo_heredado_prefijado_con_orden.txt` | Válido. `PAR` en el orden indicado y `(CALLMET, a1:main, m:main:cb)` o `m:main:cc`. |
| `metodo_heredado_prefijado_un_parametro.txt` | `a1.padre.m(...)` emite `CALLMET`. `a1.m(...)` es falta de prefijo y no emite `CALLMET`. |
| `metodo_heredado_prefijado_sin_orden.txt` | Tres errores de orden. `a1.m(...)` suma falta de prefijo. Sin `CALLMET` y sin `:=`. |
| `atributo_heredado_sin_prefijo.txt` | `falta de prefijo para 'y' en la clase 'hija'`. Sin `:=`. |
| `prefijo_no_heredado.txt` | `el prefijo 'nohereda' no es una clase heredada de 'hija'`. |
| `atributo_prefijado_inexistente.txt` | `atributo 'noexiste' no existe en la clase 'padre'`. |
| `dos_bases_mismo_miembro.txt` | `a1.y` es falta de prefijo. `(:=, a1.b1.y, 2$ul)` y `(:=, a1.b2.y, 3$ul)`. |
| `herencia_transitiva.txt` | `(:=, a1.abuelo.y, 1$ul)`. `a1.padre.y` no existe en `padre`. `a1.y` es falta de prefijo. |
| `miembro_propio_con_herencia.txt` | `(:=, a1.x, 1$ul)`, `(:=, a1.padre.y, 2$ul)`, `(pout, a1.x, --)`. TS con `a1.x:main` y `a1.padre.y:main`. |
| `atributos_heredados_en_tabla.txt` | Dos objetos de `hija`: `a1.x:main`, `a1.padre.y:main`, `a1.padre.z:main` y las mismas entradas para `a2`, todas tipadas. |
| `atributos_objeto_ambito_funcion.txt` | Objeto local: `a1.x:main:leer` y `a1.y:main:leer`, con el tipo de cada atributo. |
| `herencia_circular.txt` | `herencia circular entre 'a' y 'a'`. |
| `herencia_circular_mutua.txt` | `clase base no declarada 'b'` y `herencia circular entre 'b' y 'a'`. |
| `clase_base_no_declarada.txt` | `clase base no declarada 'noexiste'` y `'x' no es una clase`. |
| `base_repetida.txt` | `clase base repetida 'padre'`. |

`atributo_heredado_prefijado.txt` sigue emitiendo `(:=, a1.padre.y, 1.0)` y
`(:=, a1.hija.y, 2$ul)`. El método propio de `metodos.txt` y de
`metodo_callmet.txt` sigue emitiendo `CALLMET`. La invocación de función sin
orden no cambió.

# Prompt 3.9: acceso tradicional a atributos

## Alcance

Se implementó el Tema 28: `objeto.atributo`. El receptor es una variable, un
parámetro o un atributo cuyo tipo es una clase. El atributo se busca solo
entre los miembros propios de esa clase (D3/DEC-5).

Queda afuera la forma `objeto.clase.atributo` y la validación de herencia.
Eso es el Tema 31. La acción prefijada sigue armando la referencia sin
reportar errores nuevos.

## Resolución

`ACCESO_ATRIBUTO` devuelve una `ExpresionDiferida` cuyo único operando es una
`ReferenciaAtributo` con el tipo del atributo.

- Si el nombre del objeto no se alcanza, sigue el error de ámbito.
- Si el nombre no es variable, parámetro o atributo, o su tipo no es una clase:
  `'x' no es un objeto`.
- Si la clase no declara ese atributo:
  `atributo 'x' no existe en la clase 'ca'`.
- Un atributo que solo existe en un ancestro no se ve sin prefijo y usa ese
  mismo error. El Tema 31 puede reemplazarlo por el error de falta de prefijo.

La clave del atributo declarado sigue siendo `x:main:ca`, con su tipo y uso
`atributo` (DEC-5). El uso `a1.x` ocurre después de cerrar la clase, con ámbito
`main`, así que el léxico deja además el placeholder `x:main`. DEC-4 lo imprime
con uso indefinido y `Declarado: no`. No recibe el tipo del atributo: la
declaración y el uso son claves distintas, y el Prompt 1A solo copia el tipo
cuando reutiliza el placeholder del mismo ámbito. El chequeo y el terceto usan
la entrada declarada, no ese placeholder.

## Asignación y tercetos

La asignación a un atributo propio compara el tipo del atributo con el lado
derecho y devuelve una `Asignacion` cuyo destino es la `ReferenciaAtributo`.
`GeneradorTercetos` emite `(:=, objeto.atributo, valor)`. Una asignación entre
variables de clase sigue emitiendo `(COPY, destino, origen)`.

El mismo tipo se usa en operadores y en `pout`. El terceto muestra
`objeto.atributo`, por ejemplo `(pout, a1.x, --)` o `(+, a1.x, 1$ul)`.

## Pruebas

Casos en `test/tp2/verificacion_r28_r31_atributos/`.

| Caso | Resultado |
|---|---|
| `clase_con_atributos.txt` | Válido. `(:=, a1.x, 3$ul)`, `(:=, a1.z, 3$ul)`, `(pout, a1.x, --)`. `x:main:ca` y `z:main:ca` declarados como `ulongint`. `x:main` y `z:main` quedan placeholder, uso indefinido, `Declarado: no`. |
| `atributo_inexistente.txt` | `atributo 'noexiste' no existe en la clase 'ca'`. Sin `:=`. |
| `no_es_objeto.txt` | `'x' no es un objeto`. Sin `:=`. |
| `asignacion_tipo_distinto.txt` | Tipos incompatibles (`ulongint` contra `doublef`). Sin `:=`. |
| `operacion_atributo.txt` | `(+, a1.x, 1$ul)` y `(pout, [5], --)`. La suma con `1.0` es error de tipos. |
| `atributo_heredado_sin_prefijo.txt` | `y` heredado: `atributo 'y' no existe en la clase 'hija'`. |
| `receptor_parametro.txt` | `(:=, p.x, 4$ul)` y `(ret, p.x, --)`. |
| `receptor_atributo.txt` | Dentro del método, `(:=, c.n, 2$ul)`. |

`desambiguado_tradicional_y.txt` y
`prompt-2-tipos-estrictos/atributo_heredado_prefijado.txt` siguen emitiendo
`(:=, a1.padre.y, ...)` sin errores nuevos. `copia_misma_clase.txt` sigue
emitiendo `(COPY, a1:main, a2:main)`.
